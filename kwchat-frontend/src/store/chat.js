import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import websocketManager from '@/utils/websocket'
import { getConversationList } from '@/api/conversation'
import { getMessageList, sendMessage as sendMessageApi, markMessageAsRead, markConversationMessagesAsRead } from '@/api/message'
import { getReceivedFriendRequests } from '@/api/friend'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

export const useChatStore = defineStore('chat', () => {
  const userStore = useUserStore()
  const currentUserId = computed(() => userStore.userInfo?.id)
  // ========== 状态 ==========

  // 会话列表
  const conversations = ref([])

  // 当前会话
  const currentConversation = ref(null)

  // 当前会话的消息列表
  const messages = ref([])

  // 消息加载状态
  const loading = ref(false)

  // 是否还有更多消息
  const hasMore = ref(true)

  // 当前页码
  const currentPage = ref(1)

  // WebSocket连接状态
  const wsConnected = computed(() => websocketManager.isConnected.value)

  // 未读消息总数
  const totalUnread = computed(() => {
    return conversations.value.reduce((sum, conv) => sum + (conv.unreadCount || 0), 0)
  })

  // 未读好友请求数
  const unreadFriendRequests = ref(0)

  // ========== 初始化 ==========

  /**
   * 初始化WebSocket连接
   */
  function initWebSocket() {
    const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:9092/ws'
    websocketManager.connect(wsUrl)

    // 监听新消息
    websocketManager.on('new_message', handleNewMessage)

    // 监听已读回执
    websocketManager.on('read_receipt', handleReadReceipt)

    // 监听好友申请
    websocketManager.on('friend_request', handleFriendRequestNotify)

    // 监听连接成功
    websocketManager.on('connect', () => {
      console.log('WebSocket连接成功，加载会话列表')
      loadConversations()
      loadUnreadFriendRequests()
    })
  }

  /**
   * 断开WebSocket连接
   */
  function disconnectWebSocket() {
    websocketManager.off('new_message', handleNewMessage)
    websocketManager.off('read_receipt', handleReadReceipt)
    websocketManager.off('friend_request', handleFriendRequestNotify)
    websocketManager.disconnect()
  }

  // ========== 会话管理 ==========

  /**
   * 加载会话列表
   */
  async function loadConversations() {
    try {
      const res = await getConversationList()
      if (res.code === 200) {
        conversations.value = res.data || []
      }
    } catch (error) {
      console.error('加载会话列表失败:', error)
    }
  }

  /**
   * 选择会话
   */
  async function selectConversation(conversation) {
    currentConversation.value = conversation

    // 清除未读数
    conversation.unreadCount = 0

    // 调用接口标记消息为已读
    try {
      await markConversationMessagesAsRead(conversation.id)
    } catch (error) {
      console.error('标记消息已读失败:', error)
    }

    // 加载消息
    await loadMessages(conversation.id, true)

    // 给对方发送已读回执（让对方知道消息已被阅读）
    sendReadReceiptForConversation(conversation)
  }

  /**
   * 为会话中的对方消息发送已读回执
   */
  function sendReadReceiptForConversation(conversation) {
    // 确保用户信息已加载
    if (!userStore.userInfo) {
      // 等用户信息加载完成后再发送
      const unwatch = watch(() => userStore.userInfo, (info) => {
        if (info) {
          unwatch()
          doSendReadReceipt(conversation)
        }
      }, { immediate: true })
      return
    }
    doSendReadReceipt(conversation)
  }

  /**
   * 实际发送已读回执
   */
  function doSendReadReceipt(conversation) {
    if (!messages.value || messages.value.length === 0) return

    const uid = userStore.userInfo.id

    // 找到最后一条对方发的消息
    const lastOtherMessage = [...messages.value].reverse().find(m => m.senderId !== uid)
    if (!lastOtherMessage) return

    console.log('[已读回执] 发送已读回执, messageId:', lastOtherMessage.id, 'to:', lastOtherMessage.senderId)
    websocketManager.sendReadReceipt(
      lastOtherMessage.id,
      conversation.id,
      lastOtherMessage.senderId
    )
  }

  /**
   * 更新会话最后消息
   */
  function updateConversationLastMessage(conversationId, message) {
    const conversation = conversations.value.find(c => c.id === conversationId)
    if (conversation) {
      conversation.lastMessageId = message.id
      conversation.lastMessageContent = getLastMessageContent(message)
      conversation.lastMessageTime = message.createTime
      conversation.lastMessageSenderId = message.senderId

      // 将会话移到顶部
      const index = conversations.value.indexOf(conversation)
      if (index > 0) {
        conversations.value.splice(index, 1)
        conversations.value.unshift(conversation)
      }
    }
  }

  // ========== 消息管理 ==========

  /**
   * 加载消息列表
   */
  async function loadMessages(conversationId, refresh = false) {
    if (loading.value) return

    if (refresh) {
      currentPage.value = 1
      messages.value = []
      hasMore.value = true
    }

    if (!hasMore.value) return

    loading.value = true
    try {
      const res = await getMessageList(conversationId, currentPage.value, 20)
      if (res.code === 200) {
        const newMessages = res.data || []

        if (refresh) {
          messages.value = newMessages.reverse()
        } else {
          messages.value = [...newMessages.reverse(), ...messages.value]
        }

        hasMore.value = newMessages.length === 20
        currentPage.value++
      }
    } catch (error) {
      console.error('加载消息失败:', error)
    } finally {
      loading.value = false
    }
  }

  /**
   * 发送消息
   */
  async function sendMessage(conversationId, messageType, content, extra = {}) {
    try {
      const res = await sendMessageApi({
        conversationId,
        messageType,
        content,
        ...extra
      })

      if (res.code === 200) {
        const message = res.data

        // 添加到消息列表
        messages.value.push(message)

        // 更新会话最后消息
        updateConversationLastMessage(conversationId, message)

        // 通过WebSocket通知对方
        const conversation = currentConversation.value
        if (conversation) {
          const receiverId = conversation.conversationType === 1
            ? getReceiverId(conversation)
            : null

          websocketManager.sendMessage(
            conversationId,
            receiverId,
            messageType,
            content,
            extra
          )
        }

        return message
      }
    } catch (error) {
      console.error('发送消息失败:', error)
      ElMessage.error('发送失败')
      return null
    }
  }

  /**
   * 处理新消息
   */
  function handleNewMessage(message) {
    console.log('收到新消息:', message)

    const { conversationId } = message

    // 如果是当前会话的消息，添加到消息列表
    if (currentConversation.value && currentConversation.value.id === conversationId) {
      // 检查消息是否已存在
      const exists = messages.value.some(m => m.id === message.id || m.clientMessageId === message.clientMessageId)
      if (!exists) {
        messages.value.push(message)

        // 发送已读回执
        websocketManager.sendReadReceipt(
          message.id,
          conversationId,
          message.senderId
        )

        // 调用接口标记消息为已读
        markMessageAsRead(message.id).catch(err => console.error('标记消息已读失败:', err))
      }
    }

    // 更新会话列表
    updateConversationLastMessage(conversationId, message)

    // 增加未读数
    if (!currentConversation.value || currentConversation.value.id !== conversationId) {
      const conversation = conversations.value.find(c => c.id === conversationId)
      if (conversation) {
        conversation.unreadCount = (conversation.unreadCount || 0) + 1
      }
    }
  }

  /**
   * 处理已读回执
   */
  function handleReadReceipt(data) {
    console.log('[已读回执] 收到已读回执:', data)

    // 如果收到的已读回执属于当前打开的会话，把自己发的消息全部标为已读
    if (currentConversation.value && data.conversationId === currentConversation.value.id) {
      const uid = userStore.userInfo?.id
      if (uid) {
        let updated = 0
        messages.value.forEach(m => {
          if (m.senderId === uid && !m.isRead) {
            m.isRead = 1
            updated++
          }
        })
        console.log('[已读回执] 已将', updated, '条消息标记为已读')
      }
    }
  }

  /**
   * 处理好友申请通知（WebSocket推送）
   */
  function handleFriendRequestNotify(data) {
    console.log('收到好友申请通知:', data)
    unreadFriendRequests.value++
    ElMessage({
      message: `${data.senderName || '有人'}请求添加您为好友`,
      type: 'info',
      duration: 3000
    })
  }

  /**
   * 加载未读好友请求数（初始化时调用）
   */
  async function loadUnreadFriendRequests() {
    try {
      const res = await getReceivedFriendRequests()
      if (res.code === 200) {
        // 统计状态为0（待处理）的好友请求数
        const pendingRequests = (res.data || []).filter(r => r.status === 0)
        unreadFriendRequests.value = pendingRequests.length
      }
    } catch (error) {
      console.error('加载好友请求数失败:', error)
    }
  }

  /**
   * 清除未读好友请求数（进入通讯录页面时调用）
   */
  function clearUnreadFriendRequests() {
    unreadFriendRequests.value = 0
  }

  /**
   * 获取最后消息内容
   */
  function getLastMessageContent(message) {
    const { messageType, content, fileName } = message
    switch (messageType) {
      case 1: return content
      case 2: return '[图片]'
      case 3: return `[文件] ${fileName || ''}`
      case 4: return '[视频]'
      case 5: return '[语音]'
      case 6: return content
      case 7: return '消息已撤回'
      default: return content || ''
    }
  }

  /**
   * 获取接收者ID（单聊时）
   */
  function getReceiverId(conversation) {
    // 从会话成员中获取对方ID
    // 这里需要根据实际情况实现
    return null
  }

  return {
    // 状态
    conversations,
    currentConversation,
    messages,
    loading,
    hasMore,
    wsConnected,
    totalUnread,
    unreadFriendRequests,

    // 方法
    initWebSocket,
    disconnectWebSocket,
    loadConversations,
    selectConversation,
    loadMessages,
    sendMessage,
    clearUnreadFriendRequests
  }
})
