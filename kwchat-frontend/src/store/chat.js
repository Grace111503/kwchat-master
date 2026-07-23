import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getConversationList, clearUnreadCount } from '@/api/conversation'
import { getMessageList, sendMessage as sendMessageApi, markConversationMessagesAsRead } from '@/api/message'
import websocketManager from '@/utils/websocket'
import { getToken } from '@/utils/auth'

export const useChatStore = defineStore('chat', () => {
  // 状态
  const conversations = ref([])
  const currentConversation = ref(null)
  const messages = ref([])
  const loading = ref(false)
  const hasMore = ref(true)
  const currentPage = ref(1)
  const pageSize = 20
  const unreadFriendRequests = ref(0)

  // 计算属性
  const totalUnread = computed(() => {
    return conversations.value.reduce((sum, conv) => sum + (conv.unreadCount || 0), 0)
  })

  // 置顶会话
  const pinnedConversations = computed(() => {
    return conversations.value.filter(c => c.isTop)
  })

  // 普通会话
  const unpinnedConversations = computed(() => {
    return conversations.value.filter(c => !c.isTop)
  })

  // WebSocket连接
  const initWebSocket = () => {
    const token = getToken()
    if (!token) return

    const wsUrl = `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws`
    websocketManager.connect(wsUrl)

    // 监听新消息
    websocketManager.on('new_message', handleNewMessage)
    // 监听已读回执
    websocketManager.on('read_receipt', handleReadReceipt)
    // 监听好友申请
    websocketManager.on('friend_request', handleFriendRequestNotify)
    // 监听好友申请处理
    websocketManager.on('friend_request_handle', handleFriendRequestHandle)
  }

  const disconnectWebSocket = () => {
    websocketManager.off('new_message', handleNewMessage)
    websocketManager.off('read_receipt', handleReadReceipt)
    websocketManager.off('friend_request', handleFriendRequestNotify)
    websocketManager.off('friend_request_handle', handleFriendRequestHandle)
    websocketManager.disconnect()
  }

  // 加载会话列表
  const loadConversations = async () => {
    try {
      const res = await getConversationList()
      if (res.code === 200) {
        // 确保置顶会话排在前面
        const list = res.data || []
        console.log('会话列表数据:', list.map(c => ({ id: c.id, name: c.name, isTop: c.isTop })))
        conversations.value = list.sort((a, b) => {
          // 置顶优先
          if (a.isTop && !b.isTop) return -1
          if (!a.isTop && b.isTop) return 1
          // 然后按最后消息时间排序
          return new Date(b.lastMessageTime || 0) - new Date(a.lastMessageTime || 0)
        })
      }
    } catch (error) {
      console.error('加载会话列表失败:', error)
    }
  }

  // 选择会话
  const selectConversation = async (conversation) => {
    currentConversation.value = conversation
    messages.value = []
    currentPage.value = 1
    hasMore.value = true
    await loadMessages(conversation.id, true)
    // 清除未读数
    await clearUnreadCount(conversation.id)
    // 标记已读
    await markConversationMessagesAsRead(conversation.id)
    // 更新会话列表中的未读数
    const conv = conversations.value.find(c => c.id === conversation.id)
    if (conv) {
      conv.unreadCount = 0
    }
  }

  // 加载消息
  const loadMessages = async (conversationId, isRefresh = true) => {
    if (loading.value) return
    loading.value = true
    try {
      const page = isRefresh ? 1 : currentPage.value
      const res = await getMessageList(conversationId, page, pageSize)
      if (res.code === 200) {
        const newMessages = res.data?.records || res.data || []
        if (isRefresh) {
          messages.value = newMessages.reverse()
        } else {
          messages.value = [...newMessages.reverse(), ...messages.value]
        }
        hasMore.value = newMessages.length >= pageSize
        if (!isRefresh && newMessages.length > 0) {
          currentPage.value = page + 1
        }
      }
    } catch (error) {
      console.error('加载消息失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 发送消息
  const sendMessage = async (conversationId, messageType, content, extra = {}) => {
    try {
      const res = await sendMessageApi({
        conversationId,
        messageType,
        content,
        ...extra
      })
      if (res.code === 200) {
        // 通过WebSocket发送
        const conv = conversations.value.find(c => c.id === conversationId)
        websocketManager.sendMessage(conversationId, conv?.targetUserId || null, messageType, content, extra)
      }
    } catch (error) {
      console.error('发送消息失败:', error)
    }
  }

  // 处理新消息
  const handleNewMessage = (message) => {
    // 如果是当前会话的消息，添加到消息列表
    if (currentConversation.value && message.conversationId === currentConversation.value.id) {
      messages.value.push(message)
    }
    // 更新会话列表
    const conv = conversations.value.find(c => c.id === message.conversationId)
    if (conv) {
      conv.lastMessage = message.content
      conv.lastMessageTime = message.createTime
      if (currentConversation.value?.id !== message.conversationId) {
        conv.unreadCount = (conv.unreadCount || 0) + 1
      }
      // 重新排序：置顶优先，然后按最后消息时间排序
      conversations.value.sort((a, b) => {
        // 置顶优先
        if (a.isTop && !b.isTop) return -1
        if (!a.isTop && b.isTop) return 1
        // 然后按最后消息时间排序
        return new Date(b.lastMessageTime || 0) - new Date(a.lastMessageTime || 0)
      })
    } else {
      // 新会话
      conversations.value.unshift(message)
      // 重新排序
      conversations.value.sort((a, b) => {
        // 置顶优先
        if (a.isTop && !b.isTop) return -1
        if (!a.isTop && b.isTop) return 1
        // 然后按最后消息时间排序
        return new Date(b.lastMessageTime || 0) - new Date(a.lastMessageTime || 0)
      })
    }
  }

  // 处理已读回执
  const handleReadReceipt = (data) => {
    const msg = messages.value.find(m => m.id === data.messageId)
    if (msg) {
      msg.readStatus = 1
    }
  }

  // 处理好友申请通知
  const handleFriendRequestNotify = (data) => {
    console.log('收到好友申请通知:', data)
    unreadFriendRequests.value++
  }

  // 处理好友申请处理通知
  const handleFriendRequestHandle = (data) => {
    console.log('好友申请已处理:', data)
  }

  // 清除未读好友申请数
  const clearUnreadFriendRequests = () => {
    unreadFriendRequests.value = 0
  }

  return {
    conversations,
    currentConversation,
    messages,
    loading,
    hasMore,
    currentPage,
    pageSize,
    unreadFriendRequests,
    totalUnread,
    pinnedConversations,
    unpinnedConversations,
    initWebSocket,
    disconnectWebSocket,
    loadConversations,
    selectConversation,
    loadMessages,
    sendMessage,
    handleNewMessage,
    handleReadReceipt,
    handleFriendRequestNotify,
    handleFriendRequestHandle,
    clearUnreadFriendRequests
  }
})
