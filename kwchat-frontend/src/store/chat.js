import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getConversationList, clearUnreadCount, getConversationMembers } from '@/api/conversation'
import { getMessageList, sendMessage as sendMessageApi, markConversationMessagesAsRead } from '@/api/message'
import { getUserDetail } from '@/api/user'
import { checkBlacklistStatus } from '@/api/friend'
import websocketManager from '@/utils/websocket'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/store/user'
import { getWsUrl } from '@/utils/platform'
import { ElMessage } from 'element-plus'

// 用户信息缓存
const userCache = new Map()

export const useChatStore = defineStore('chat', () => {
  const userStore = useUserStore()
  // 状态
  const conversations = ref([])
  const currentConversation = ref(null)
  const messages = ref([])
  const loading = ref(false)
  const hasMore = ref(true)
  const currentPage = ref(1)
  const pageSize = 20
  const unreadFriendRequests = ref(0)

  // 黑名单状态
  const isBlocked = ref(false)       // 我是否拉黑了对方
  const isBlockedBy = ref(false)     // 对方是否拉黑了我

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

    // 获取 WebSocket URL
    const wsUrl = getWsUrl()
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

  // 检查黑名单状态
  const checkBlacklist = async (targetUserId) => {
    try {
      const res = await checkBlacklistStatus(targetUserId)
      if (res.code === 200) {
        isBlocked.value = res.data?.isBlocked || false
        isBlockedBy.value = res.data?.isBlockedBy || false
      }
    } catch (error) {
      console.error('检查黑名单状态失败:', error)
    }
  }

  // 清除黑名单状态（切换会话时调用）
  const clearBlacklistStatus = () => {
    isBlocked.value = false
    isBlockedBy.value = false
  }

  // 加载会话列表
  const loadConversations = async () => {
    try {
      const res = await getConversationList()
      if (res.code === 200) {
        // 确保置顶会话排在前面
        const list = res.data || []
        console.log('会话列表数据:', list.map(c => ({id: c.id, name: c.name, isTop: c.isTop})))
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
    // 清除之前的黑名单状态
    clearBlacklistStatus()
    // 如果是单聊，检查黑名单状态
    if (conversation.conversationType === 1 && conversation.targetUserId) {
      await checkBlacklist(conversation.targetUserId)
    }
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
        let newMessages = res.data?.records || res.data || []

        // 获取会话成员信息，用于填充senderName和senderAvatar
        try {
          const membersRes = await getConversationMembers(conversationId)
          if (membersRes.code === 200 && membersRes.data) {
            // 为每个成员查询用户信息获取头像
            const memberMap = {}
            for (const member of membersRes.data) {
              // 检查缓存
              if (userCache.has(member.userId)) {
                const cachedUser = userCache.get(member.userId)
                memberMap[member.userId] = {
                  senderName: member.nickname || cachedUser.nickname || cachedUser.username || '未知用户',
                  senderAvatar: cachedUser.avatar || null
                }
                continue
              }

              // 查询用户信息
              try {
                const userRes = await getUserDetail(member.userId)
                if (userRes.code === 200 && userRes.data) {
                  // 缓存用户信息
                  userCache.set(member.userId, userRes.data)
                  memberMap[member.userId] = {
                    senderName: member.nickname || userRes.data.nickname || userRes.data.username || '未知用户',
                    senderAvatar: userRes.data.avatar || null
                  }
                } else {
                  memberMap[member.userId] = {
                    senderName: member.nickname || '未知用户',
                    senderAvatar: null
                  }
                }
              } catch (e) {
                console.warn('获取用户信息失败:', member.userId, e)
                memberMap[member.userId] = {
                  senderName: member.nickname || '未知用户',
                  senderAvatar: null
                }
              }
            }

            // 为每条消息填充senderName、senderAvatar和replySenderName
            newMessages = newMessages.map(msg => {
              // 查找被回复消息的发送者
              let replySenderName = '未知用户'
              if (msg.replyMessageId) {
                // 在当前消息列表中查找被回复的消息
                const repliedMsg = newMessages.find(m => m.id === msg.replyMessageId)
                if (repliedMsg) {
                  replySenderName = repliedMsg.senderName || memberMap[repliedMsg.senderId]?.senderName || '未知用户'
                }
              }

              return {
                ...msg,
                senderName: msg.senderName || memberMap[msg.senderId]?.senderName || '未知用户',
                senderAvatar: msg.senderAvatar || memberMap[msg.senderId]?.senderAvatar || null,
                replySenderName
              }
            })
          }
        } catch (e) {
          console.warn('获取会话成员信息失败:', e)
        }

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
      // 检查黑名单（单聊会话）
      const conv = conversations.value.find(c => c.id === conversationId)
      if (conv && conv.conversationType === 1 && conv.targetUserId) {
        // 检查黑名单状态
        await checkBlacklist(conv.targetUserId)

        if (isBlockedBy.value) {
          ElMessage.error('你已被对方拉黑，无法发送消息')
          return
        }
        if (isBlocked.value) {
          ElMessage.warning('你已拉黑对方，对方将无法收到你的消息')
        }
      }

      const res = await sendMessageApi({
        conversationId,
        messageType,
        content,
        ...extra
      })
      if (res.code === 200) {
        // 立即将消息添加到本地列表，让用户立即看到
        const newMessage = {
          id: res.data?.id || Date.now(), // 使用服务器返回的ID，如果没有则用时间戳
          conversationId,
          senderId: userStore.userInfo?.id,
          senderName: userStore.userInfo?.nickname || userStore.userInfo?.username || '我',
          senderAvatar: userStore.userInfo?.avatar,
          messageType,
          content,
          createTime: new Date().toISOString(),
          readStatus: 0,
          ...extra
        }
        messages.value.push(newMessage)

        // 更新会话列表的最后消息时间
        const conv = conversations.value.find(c => c.id === conversationId)
        if (conv) {
          conv.lastMessage = content || '[多媒体消息]'
          conv.lastMessageTime = newMessage.createTime
          // 重新排序：置顶优先，然后按最后消息时间排序
          conversations.value.sort((a, b) => {
            if (a.isTop && !b.isTop) return -1
            if (!a.isTop && b.isTop) return 1
            return new Date(b.lastMessageTime || 0) - new Date(a.lastMessageTime || 0)
          })
        }

        // 通过WebSocket发送（用于通知其他用户）
        websocketManager.sendMessage(conversationId, conv?.targetUserId || null, messageType, content, extra)
      }
    } catch (error) {
      console.error('发送消息失败:', error)
    }
  }

  // 处理新消息
  const handleNewMessage = (message) => {
    // 为新消息填充senderName和senderAvatar
    const enhancedMessage = {
      ...message,
      senderName: message.senderName || '未知用户',
      senderAvatar: message.senderAvatar || null
    }

    // 如果是当前会话的消息，添加到消息列表
    if (currentConversation.value && message.conversationId === currentConversation.value.id) {
      messages.value.push(enhancedMessage)
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
      conversations.value.unshift(enhancedMessage)
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

  // 移除会话（删除好友时调用）
  const removeConversation = (conversationId) => {
    const index = conversations.value.findIndex(c => c.id === conversationId)
    if (index !== -1) {
      conversations.value.splice(index, 1)
      // 如果当前选中的会话被移除，清空当前会话
      if (currentConversation.value?.id === conversationId) {
        currentConversation.value = null
        messages.value = []
      }
    }
  }

  // 根据用户ID查找并移除单聊会话
  const removeConversationByUserId = (userId) => {
    const conversation = conversations.value.find(c =>
      c.conversationType === 1 && c.targetUserId === userId
    )
    if (conversation) {
      removeConversation(conversation.id)
    }
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
    isBlocked,
    isBlockedBy,
    initWebSocket,
    disconnectWebSocket,
    loadConversations,
    selectConversation,
    loadMessages,
    sendMessage,
    checkBlacklist,
    clearBlacklistStatus,
    handleNewMessage,
    handleReadReceipt,
    handleFriendRequestNotify,
    handleFriendRequestHandle,
    clearUnreadFriendRequests,
    removeConversation,
    removeConversationByUserId
  }
})
