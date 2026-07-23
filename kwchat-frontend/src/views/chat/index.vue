<template>
  <div class="chat-container">
    <!-- 左侧会话列表（桌面端始终显示，移动端无选中会话时显示） -->
    <div class="conversation-list" :class="{ 'mobile-hidden': chatStore.currentConversation }">
      <div class="conversation-header">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索会话"
          prefix-icon="Search"
          clearable
        />
      </div>

      <div class="conversation-items">
        <!-- 置顶会话区域 -->
        <div v-if="pinnedConversations.length > 0" class="pinned-section">
          <div class="section-label">置顶会话</div>
          <ConversationItem
            v-for="conversation in pinnedConversations"
            :key="conversation.id"
            :conversation="conversation"
            :is-active="chatStore.currentConversation?.id === conversation.id"
            @select="handleSelectConversation"
            @delete="handleDeleteConversation"
          />
        </div>

        <!-- 普通会话区域 -->
        <div v-if="unpinnedConversations.length > 0" class="unpinned-section">
          <div v-if="pinnedConversations.length > 0" class="section-label">最近会话</div>
          <ConversationItem
            v-for="conversation in unpinnedConversations"
            :key="conversation.id"
            :conversation="conversation"
            :is-active="chatStore.currentConversation?.id === conversation.id"
            @select="handleSelectConversation"
            @delete="handleDeleteConversation"
          />
        </div>

        <div v-if="filteredConversations.length === 0" class="empty-tip">
          <span>暂无会话</span>
        </div>
      </div>
    </div>

    <!-- 右侧聊天窗口（桌面端始终显示，移动端有选中会话时显示） -->
    <div class="chat-window" :class="{ 'mobile-fullscreen': chatStore.currentConversation }">
      <template v-if="chatStore.currentConversation">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <!-- 移动端返回按钮 -->
          <el-icon class="back-btn hide-desktop" @click="goBack">
            <ArrowLeft />
          </el-icon>
          <div class="chat-title">
            <span class="name">{{ chatStore.currentConversation.name }}</span>
            <span v-if="chatStore.currentConversation.conversationType === 2" class="member-count">
              ({{ chatStore.currentConversation.memberCount }})
            </span>
          </div>
          <div class="chat-actions">
            <el-tooltip content="搜索消息" placement="bottom">
              <el-icon class="action-btn" @click="showMessageSearch = !showMessageSearch">
                <Search />
              </el-icon>
            </el-tooltip>
            <el-tooltip content="AI功能" placement="bottom">
              <el-icon class="action-btn" @click="aiFeaturesRef?.toggleQuickActions()">
                <Opportunity />
              </el-icon>
            </el-tooltip>
            <el-tooltip :content="isGroupChat ? '群信息' : '用户信息'" placement="bottom">
              <el-icon class="action-btn" @click="showConversationInfo">
                <InfoFilled />
              </el-icon>
            </el-tooltip>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="message-list" ref="messageListRef" @scroll="handleScroll">
          <div v-if="chatStore.loading" class="loading-tip">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>

          <div v-if="!chatStore.hasMore && chatStore.messages.length > 0" class="no-more-tip">
            — 没有更多消息了 —
          </div>

          <MessageBubble
            v-for="message in displayMessages"
            :key="message.id"
            :message="message"
            :is-self="message.senderId === userInfo?.id"
            :is-selected="isMultiSelectMode && isMessageSelected(message)"
            @play-voice="handlePlayVoice"
            @download="handleDownload"
            @play-video="handlePlayVideo"
            @click-avatar="handleAvatarClick"
            @recall="handleRecallMessage"
            @forward="handleForwardMessage"
            @reply="handleReplyMessage"
            @delete="handleDeleteMessage"
            @translate="handleTranslateMessage"
            @multi-select="handleMultiSelectFromAction"
            @click="(e) => handleMessageClick(message, e)"
          />
        </div>

        <!-- 消息搜索框 -->
        <div class="message-search" v-if="showMessageSearch">
          <el-input
            v-model="messageSearchKeyword"
            placeholder="搜索聊天记录..."
            prefix-icon="Search"
            clearable
            size="small"
          />
          <span class="search-count" v-if="messageSearchKeyword">
            {{ filteredMessages.length }} 条结果
          </span>
        </div>

        <!-- 多选工具栏 -->
        <div class="multi-select-toolbar" v-if="isMultiSelectMode">
          <div class="toolbar-left">
            <el-icon class="check-all-icon" @click="selectAllMessages">
              <CircleCheck />
            </el-icon>
            <span class="selected-count">已选择 {{ selectedMessages.length }} 条消息</span>
          </div>
          <div class="toolbar-actions">
            <el-button size="small" @click="batchForwardMessages" :disabled="selectedMessages.length === 0">
              转发
            </el-button>
            <el-button size="small" @click="batchFavoriteMessages" :disabled="selectedMessages.length === 0">
              收藏
            </el-button>
            <el-button type="danger" size="small" @click="batchDeleteMessages" :disabled="selectedMessages.length === 0">
              删除
            </el-button>
            <el-button size="small" @click="cancelMultiSelect">退出</el-button>
          </div>
        </div>

        <!-- 正在输入提示 -->
        <div class="typing-indicator" v-if="typingText">
          <span>{{ typingText }}</span>
        </div>

        <!-- 输入区域 -->
        <ChatInput
          ref="chatInputRef"
          :disabled="false"
          :is-group="isGroupChat"
          :members="groupMembers"
          :reply-message="replyMessage"
          @send="handleSendText"
          @send-image="handleSendImage"
          @send-file="handleSendFile"
          @send-video="handleSendVideo"
          @send-voice="handleSendVoice"
          @typing="handleTyping"
          @cancel-reply="cancelReply"
        />

        <AiFeatures
          ref="aiFeaturesRef"
          :conversation-id="chatStore.currentConversation?.id"
          :recent-messages="recentTextMessages"
          @select-suggestion="handleSelectSuggestion"
        />
      </template>

      <template v-else>
        <div class="empty-chat">
          <div class="empty-icon">
            <el-icon :size="48"><ChatDotRound /></el-icon>
          </div>
          <p>选择一个会话开始聊天</p>
        </div>
      </template>
    </div>

    <!-- 群信息面板 -->
    <GroupInfoPanel
      v-if="chatStore.currentConversation?.conversationType === 2"
      v-model:visible="groupInfoVisible"
      :conversation-id="chatStore.currentConversation?.id"
    />

    <!-- 用户信息面板 -->
    <UserProfile
      v-model:visible="userProfileVisible"
      :user="selectedUser"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useUserStore } from '@/store/user'
import { useChatStore } from '@/store/chat'
import { ElMessage } from 'element-plus'
import { uploadImage, uploadFile, uploadVideo } from '@/api/file'
import { getConversationMembers } from '@/api/conversation'
import { recallMessage, deleteMessage } from '@/api/message'
import websocketManager from '@/utils/websocket'
import ConversationItem from '@/components/chat/ConversationItem.vue'
import MessageBubble from '@/components/chat/MessageBubble.vue'
import ChatInput from '@/components/chat/ChatInput.vue'
import AiFeatures from '@/components/chat/AiFeatures.vue'
import GroupInfoPanel from '@/components/chat/GroupInfoPanel.vue'
import UserProfile from '@/components/chat/UserProfile.vue'

const userStore = useUserStore()
const chatStore = useChatStore()
const userInfo = computed(() => userStore.userInfo)

const searchKeyword = ref('')
const messageSearchKeyword = ref('')
const showMessageSearch = ref(false)
const messageListRef = ref(null)
const chatInputRef = ref(null)
const aiFeaturesRef = ref(null)
const groupInfoVisible = ref(false)
const groupMembers = ref([])
const userProfileVisible = ref(false)
const selectedUser = ref(null)
const typingUsers = ref([])
const replyMessage = ref(null)

// 多选模式
const isMultiSelectMode = ref(false)
const selectedMessages = ref([])

// 是否为群聊
const isGroupChat = computed(() => chatStore.currentConversation?.conversationType === 2)

// 过滤消息（用于搜索）
const filteredMessages = computed(() => {
  if (!messageSearchKeyword.value) return chatStore.messages
  const keyword = messageSearchKeyword.value.toLowerCase()
  return chatStore.messages.filter(msg =>
    msg.content?.toLowerCase().includes(keyword) ||
    msg.fileName?.toLowerCase().includes(keyword)
  )
})

// 显示的消息列表
const displayMessages = computed(() => {
  return messageSearchKeyword.value ? filteredMessages.value : chatStore.messages
})

// 最近的文本消息（用于翻译选择）
const recentTextMessages = computed(() => {
  return chatStore.messages
    .filter(msg => msg.messageType === 1 && msg.content)
    .slice(-20)
    .reverse()
})

const filteredConversations = computed(() => {
  if (!searchKeyword.value) return chatStore.conversations
  return chatStore.conversations.filter(item =>
    item.name?.includes(searchKeyword.value)
  )
})

// 置顶会话
const pinnedConversations = computed(() => {
  return filteredConversations.value.filter(c => c.isTop)
})

// 普通会话
const unpinnedConversations = computed(() => {
  return filteredConversations.value.filter(c => !c.isTop)
})

// 移动端返回会话列表
const goBack = () => {
  chatStore.currentConversation = null
}

const handleDeleteConversation = (conversation) => {
  ElMessageBox.confirm('确定要删除该会话吗？', '删除会话', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = chatStore.conversations.findIndex(c => c.id === conversation.id)
    if (index !== -1) {
      chatStore.conversations.splice(index, 1)
    }
    // 如果删除的是当前选中的会话，清空当前会话
    if (chatStore.currentConversation?.id === conversation.id) {
      chatStore.currentConversation = null
    }
    ElMessage.success('会话已删除')
  }).catch(() => {})
}

const handleSelectConversation = async (conversation) => {
  await chatStore.selectConversation(conversation)
  // 如果是群聊，加载群成员列表
  if (conversation.conversationType === 2) {
    await loadGroupMembers(conversation.id)
  } else {
    groupMembers.value = []
  }
  scrollToBottom()
}

const loadGroupMembers = async (conversationId) => {
  try {
    const res = await getConversationMembers(conversationId)
    if (res.code === 200) {
      groupMembers.value = res.data || []
    }
  } catch (error) {
    console.error('加载群成员失败:', error)
  }
}

const handleSendText = async (content) => {
  if (!chatStore.currentConversation) return
  const extra = {}
  // 如果有引用消息，添加引用信息
  if (replyMessage.value) {
    extra.replyMessageId = replyMessage.value.id
    extra.replyContent = replyMessage.value.content
    extra.replySenderName = replyMessage.value.senderName
  }
  await chatStore.sendMessage(chatStore.currentConversation.id, 1, content, extra)
  // 清除引用状态
  replyMessage.value = null
  scrollToBottom()
}

const handleSendImage = async (file) => {
  if (!chatStore.currentConversation) return
  try {
    const res = await uploadImage(file)
    if (res.code === 200) {
      await chatStore.sendMessage(chatStore.currentConversation.id, 2, null, {
        fileUrl: res.data.url,
        fileName: res.data.originalFileName,
        fileSize: res.data.fileSize,
        fileType: res.data.fileType
      })
      scrollToBottom()
    }
  } catch (error) {
    ElMessage.error('图片上传失败')
  }
}

const handleSendFile = async (file) => {
  if (!chatStore.currentConversation) return
  try {
    const res = await uploadFile(file)
    if (res.code === 200) {
      await chatStore.sendMessage(chatStore.currentConversation.id, 3, null, {
        fileUrl: res.data.url,
        fileName: res.data.originalFileName,
        fileSize: res.data.fileSize,
        fileType: res.data.fileType
      })
      scrollToBottom()
    }
  } catch (error) {
    ElMessage.error('文件上传失败')
  }
}

const handleSendVideo = async (file) => {
  if (!chatStore.currentConversation) return
  try {
    const res = await uploadVideo(file)
    if (res.code === 200) {
      await chatStore.sendMessage(chatStore.currentConversation.id, 4, null, {
        fileUrl: res.data.url,
        fileName: res.data.originalFileName,
        fileSize: res.data.fileSize,
        fileType: res.data.fileType
      })
      scrollToBottom()
    }
  } catch (error) {
    ElMessage.error('视频上传失败')
  }
}

const handleSendVoice = async (file) => {
  if (!chatStore.currentConversation) return
  try {
    const res = await uploadFile(file)
    if (res.code === 200) {
      // 使用录音时长，如果有的话
      const duration = file.recordingDuration || await getAudioDuration(file)
      await chatStore.sendMessage(chatStore.currentConversation.id, 5, null, {
        fileUrl: res.data.url,
        fileName: res.data.originalFileName,
        fileSize: res.data.fileSize,
        fileType: res.data.fileType,
        duration: duration
      })
      scrollToBottom()
    }
  } catch (error) {
    ElMessage.error('语音发送失败')
  }
}

// 获取音频时长
const getAudioDuration = (file) => {
  return new Promise((resolve) => {
    const audio = new Audio()
    audio.onloadedmetadata = () => {
      resolve(Math.round(audio.duration))
    }
    audio.onerror = () => {
      resolve(0)
    }
    audio.src = URL.createObjectURL(file)
  })
}

let typingTimer = null
const handleTyping = () => {
  if (!chatStore.currentConversation) return
  // 发送正在输入事件
  websocketManager.sendTyping(
    chatStore.currentConversation.id,
    chatStore.currentConversation.targetUserId
  )
}

// 监听正在输入事件
const handleTypingEvent = (data) => {
  if (data.conversationId !== chatStore.currentConversation?.id) return
  if (data.senderId === userInfo.value?.id) return

  // 添加到正在输入列表
  const existing = typingUsers.value.find(u => u.senderId === data.senderId)
  if (!existing) {
    typingUsers.value.push({
      senderId: data.senderId,
      senderName: data.senderName
    })
  }

  // 3秒后自动移除
  setTimeout(() => {
    typingUsers.value = typingUsers.value.filter(u => u.senderId !== data.senderId)
  }, 3000)
}

const typingText = computed(() => {
  if (typingUsers.value.length === 0) return ''
  if (typingUsers.value.length === 1) {
    return `${typingUsers.value[0].senderName} 正在输入...`
  }
  return '多人正在输入...'
})
const handlePlayVoice = (message) => {
  // 语音播放已在 MessageBubble 中处理
}
const handleDownload = (message) => { if (message.fileUrl) window.open(message.fileUrl, '_blank') }
const handlePlayVideo = (message) => { if (message.fileUrl) window.open(message.fileUrl, '_blank') }

const handleRecallMessage = async (message) => {
  try {
    const res = await recallMessage(message.id)
    if (res.code === 200) {
      // 更新消息状态为已撤回
      const msg = chatStore.messages.find(m => m.id === message.id)
      if (msg) {
        msg.messageType = 7
        msg.content = null
      }
      ElMessage.success('消息已撤回')
    }
  } catch (error) {
    ElMessage.error('撤回失败')
  }
}

const handleForwardMessage = (data) => {
  ElMessage.success('转发成功')
}

// 多选模式相关
const toggleMultiSelectMode = () => {
  isMultiSelectMode.value = !isMultiSelectMode.value
  if (!isMultiSelectMode.value) {
    selectedMessages.value = []
  }
}

const handleMultiSelectMessage = (message) => {
  console.log('处理多选消息:', message.id, '多选模式:', isMultiSelectMode.value)
  if (!isMultiSelectMode.value) {
    console.log('多选模式未启用，跳过')
    return
  }

  const index = selectedMessages.value.findIndex(m => m.id === message.id)
  console.log('消息索引:', index, '当前选中数量:', selectedMessages.value.length)
  if (index === -1) {
    selectedMessages.value.push(message)
    console.log('添加消息，当前选中:', selectedMessages.value.length)
    ElMessage.success('已选择')
  } else {
    selectedMessages.value.splice(index, 1)
    console.log('移除消息，当前选中:', selectedMessages.value.length)
    ElMessage.info('已取消选择')
  }
}

const handleMultiSelectFromAction = (message) => {
  console.log('从菜单触发多选模式')
  isMultiSelectMode.value = true
  selectedMessages.value = [message]
  console.log('当前选中消息:', selectedMessages.value)
  ElMessage.info('已进入多选模式，点击消息选择')
}

const cancelMultiSelect = () => {
  isMultiSelectMode.value = false
  selectedMessages.value = []
}

const selectAllMessages = () => {
  if (selectedMessages.value.length === chatStore.messages.length) {
    selectedMessages.value = []
  } else {
    selectedMessages.value = [...chatStore.messages]
  }
}

const batchForwardMessages = () => {
  if (selectedMessages.value.length === 0) {
    ElMessage.warning('请先选择消息')
    return
  }
  ElMessage.info('转发功能开发中')
}

const batchFavoriteMessages = async () => {
  if (selectedMessages.value.length === 0) {
    ElMessage.warning('请先选择消息')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要收藏选中的 ${selectedMessages.value.length} 条消息吗？`, '批量收藏', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    // TODO: 调用收藏 API
    ElMessage.success(`已收藏 ${selectedMessages.value.length} 条消息`)
    cancelMultiSelect()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('收藏失败')
    }
  }
}

const batchDeleteMessages = async () => {
  if (selectedMessages.value.length === 0) {
    ElMessage.warning('请先选择消息')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedMessages.value.length} 条消息吗？`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 逐个删除
    for (const message of selectedMessages.value) {
      try {
        await deleteMessage(message.id)
      } catch (e) {
        console.error('删除失败:', e)
      }
    }

    // 从列表中移除
    const selectedIds = selectedMessages.value.map(m => m.id)
    chatStore.messages = chatStore.messages.filter(m => !selectedIds.includes(m.id))

    ElMessage.success(`已删除 ${selectedMessages.value.length} 条消息`)
    selectedMessages.value = []
    isMultiSelectMode.value = false
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const isMessageSelected = (message) => {
  return selectedMessages.value.some(m => m.id === message.id)
}

const handleMessageClick = (message, event) => {
  console.log('点击消息:', message.id, '多选模式:', isMultiSelectMode.value)

  // 如果点击的是操作菜单区域，不处理
  if (event?.target?.closest('.message-actions') || event?.target?.closest('.action-trigger') || event?.target?.closest('.el-popover')) {
    console.log('点击了菜单区域，跳过')
    return
  }

  if (isMultiSelectMode.value) {
    console.log('执行多选操作')
    handleMultiSelectMessage(message)
  } else if (message.messageType === 1 && message.content) {
    aiFeaturesRef.value?.selectMessageForTranslate(message)
  }
}

const handleDeleteMessage = (message) => {
  // 从消息列表中移除
  const index = chatStore.messages.findIndex(m => m.id === message.id)
  if (index !== -1) {
    chatStore.messages.splice(index, 1)
  }
}

// 多选模式相关
const toggleMultiSelectMode = () => {
  isMultiSelectMode.value = !isMultiSelectMode.value
  if (!isMultiSelectMode.value) {
    selectedMessages.value = []
  }
}

const handleMultiSelectMessage = (message) => {
  if (!isMultiSelectMode.value) return

  const index = selectedMessages.value.findIndex(m => m.id === message.id)
  if (index === -1) {
    selectedMessages.value.push(message)
    ElMessage.success('已选择')
  } else {
    selectedMessages.value.splice(index, 1)
    ElMessage.info('已取消选择')
  }
}

const handleMultiSelectFromAction = (message) => {
  isMultiSelectMode.value = true
  selectedMessages.value = [message]
  ElMessage.info('已进入多选模式，点击消息选择')
}

const batchDeleteMessages = async () => {
  if (selectedMessages.value.length === 0) {
    ElMessage.warning('请先选择消息')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedMessages.value.length} 条消息吗？`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 逐个删除
    for (const message of selectedMessages.value) {
      const index = chatStore.messages.findIndex(m => m.id === message.id)
      if (index !== -1) {
        chatStore.messages.splice(index, 1)
      }
    }

    ElMessage.success(`已删除 ${selectedMessages.value.length} 条消息`)
    selectedMessages.value = []
    isMultiSelectMode.value = false
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const cancelMultiSelect = () => {
  isMultiSelectMode.value = false
  selectedMessages.value = []
}

const selectAllMessages = () => {
  if (selectedMessages.value.length === chatStore.messages.length) {
    selectedMessages.value = []
  } else {
    selectedMessages.value = [...chatStore.messages]
  }
}

// 点击消息处理
const handleMessageClick = (message, event) => {
  // 如果点击的是操作菜单区域，不处理
  if (event?.target?.closest('.message-actions') || event?.target?.closest('.action-trigger') || event?.target?.closest('.el-popover')) {
    return
  }

  if (isMultiSelectMode.value) {
    handleMultiSelectMessage(message)
  } else if (message.messageType === 1 && message.content) {
    aiFeaturesRef.value?.selectMessageForTranslate(message)
  }
}

// 检查消息是否被选中
const isMessageSelected = (message) => {
  return selectedMessages.value.some(m => m.id === message.id)
}

const handleReplyMessage = (message) => {
  replyMessage.value = {
    ...message,
    senderName: message.senderName || '未知用户'
  }
}

const cancelReply = () => {
  replyMessage.value = null
}

// 翻译消息（从三点菜单触发）
const handleTranslateMessage = (message) => {
  // 直接打开翻译对话框并设置选中的消息
  aiFeaturesRef.value?.translateDirectly(message)
}

const showConversationInfo = () => {
  if (chatStore.currentConversation?.conversationType === 2) {
    // 群聊 - 显示群信息
    groupInfoVisible.value = true
  } else {
    // 单聊 - 显示对方用户信息
    showUserProfile(targetUser.value)
  }
}

// 获取对方用户信息（单聊时）
const targetUser = computed(() => {
  if (!chatStore.currentConversation || chatStore.currentConversation.conversationType !== 1) {
    return null
  }
  // 从会话名称中获取对方信息
  return {
    id: chatStore.currentConversation.targetUserId,
    nickname: chatStore.currentConversation.name,
    avatar: chatStore.currentConversation.avatar
  }
})

const showUserProfile = (user) => {
  if (!user) return
  selectedUser.value = user
  userProfileVisible.value = true
}

const handleAvatarClick = (data) => {
  // 如果是自己，不弹出
  if (data.senderId === userInfo.value?.id) return
  selectedUser.value = {
    id: data.senderId,
    nickname: data.senderName,
    avatar: data.senderAvatar
  }
  userProfileVisible.value = true
}

const handleSelectSuggestion = async (suggestion) => {
  if (!chatStore.currentConversation) return
  await chatStore.sendMessage(chatStore.currentConversation.id, 1, suggestion)
  scrollToBottom()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const handleScroll = () => {
  if (!messageListRef.value) return
  const { scrollTop } = messageListRef.value
  if (scrollTop === 0 && chatStore.hasMore && !chatStore.loading) {
    const oldHeight = messageListRef.value.scrollHeight
    chatStore.loadMessages(chatStore.currentConversation.id, false).then(() => {
      nextTick(() => {
        const newHeight = messageListRef.value.scrollHeight
        messageListRef.value.scrollTop = newHeight - oldHeight
      })
    })
  }
}

watch(() => chatStore.messages.length, (newLen, oldLen) => {
  if (newLen > oldLen && messageListRef.value) {
    const { scrollTop, scrollHeight, clientHeight } = messageListRef.value
    if (scrollHeight - scrollTop - clientHeight < 100) {
      scrollToBottom()
    }
  }
})

onMounted(() => {
  // 如果会话列表为空或没有当前选中的会话，才重新加载
  if (chatStore.conversations.length === 0 || !chatStore.currentConversation) {
    chatStore.loadConversations()
  }
  // 如果当前是群聊，加载群成员
  if (chatStore.currentConversation?.conversationType === 2) {
    loadGroupMembers(chatStore.currentConversation.id)
  }
  // 监听正在输入事件
  websocketManager.on('typing', handleTypingEvent)
})

onUnmounted(() => {
  websocketManager.off('typing', handleTypingEvent)
})
</script>

<style lang="scss" scoped>
.chat-container {
  display: flex;
  height: 100%;
  background: #fff;
}

.conversation-list {
  width: 300px;
  border-right: 1px solid #e5e5e5;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.conversation-header {
  padding: 12px;
  border-bottom: 1px solid #e5e5e5;

  :deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #e0e0e0 inset;
    border-radius: 0;
    background: #fff;

    &.is-focus {
      box-shadow: 0 0 0 1px #2b7fff inset;
    }
  }
}

.conversation-items {
  flex: 1;
  overflow-y: auto;
}

.section-label {
  font-size: 12px;
  color: #999;
  padding: 8px 12px 4px;
  background: #f5f5f5;
  border-bottom: 1px solid #eee;
}

.pinned-section {
  background: #fafafa;
  border-bottom: 8px solid #f0f0f0;
}

.empty-tip {
  padding: 60px 0;
  text-align: center;
  color: #bbb;
  font-size: 13px;
}

.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  height: 56px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e5e5e5;
  background: #fff;
}

.chat-title {
  display: flex;
  align-items: center;

  .name {
    font-size: 15px;
    font-weight: 600;
    color: #1a1a1a;
  }

  .member-count {
    font-size: 13px;
    color: #999;
    margin-left: 6px;
  }
}

.chat-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  font-size: 18px;
  color: #999;
  cursor: pointer;
  transition: color 0.15s;

  &:hover {
    color: #2b7fff;
  }
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
  background: #f5f5f5;
}

.loading-tip,
.no-more-tip {
  text-align: center;
  padding: 12px;
  font-size: 12px;
  color: #bbb;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.empty-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #ccc;

  .empty-icon {
    margin-bottom: 16px;
  }

  p {
    font-size: 14px;
  }
}

.typing-indicator {
  padding: 4px 16px;
  background: #fff;
  border-top: 1px solid #f0f0f0;

  span {
    font-size: 12px;
    color: #999;
  }
}

.multi-select-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: #e8f0fe;
  border-top: 1px solid #d0e0f5;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .check-all-icon {
    font-size: 18px;
    color: #2b7fff;
    cursor: pointer;

    &:hover {
      color: #1a6fe0;
    }
  }

  .selected-count {
    font-size: 13px;
    color: #333;
  }

  .toolbar-actions {
    display: flex;
    gap: 8px;
  }
}

.message-search {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: #fff;
  border-top: 1px solid #f0f0f0;

  :deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #e0e0e0 inset;
    border-radius: 4px;

    &.is-focus {
      box-shadow: 0 0 0 1px #2b7fff inset;
    }
  }

  .search-count {
    font-size: 12px;
    color: #999;
    white-space: nowrap;
  }
}

// 移动端响应式
@media (max-width: 768px) {
  .chat-container {
    height: calc(100vh - 56px);
    position: relative;
  }

  .conversation-list {
    width: 100%;
    border-right: none;
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 10;
    transition: transform 0.3s;

    &.mobile-hidden {
      transform: translateX(-100%);
    }
  }

  .chat-window {
    width: 100%;
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 5;
    transform: translateX(100%);
    transition: transform 0.3s;

    &.mobile-fullscreen {
      transform: translateX(0);
    }
  }

  .chat-header {
    height: 48px;
    padding: 0 12px;
  }

  .back-btn {
    font-size: 20px;
    cursor: pointer;
    margin-right: 8px;
    color: #333;
  }

  .chat-title .name {
    font-size: 14px;
  }

  .message-content {
    max-width: 75%;
  }

  .multi-select-toolbar {
    padding: 6px 12px;

    .toolbar-actions {
      gap: 4px;

      .el-button {
        padding: 4px 8px;
        font-size: 12px;
      }
    }
  }
}
</style>
