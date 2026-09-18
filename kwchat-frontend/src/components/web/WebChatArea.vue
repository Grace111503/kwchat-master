<template>
  <div class="web-chat-area-inner">
    <!-- 空状态：未选中会话 -->
    <div v-if="!chatStore.currentConversation" class="chat-empty">
      <div class="empty-icon">
        <el-icon :size="80"><ChatDotRound /></el-icon>
      </div>
      <h2 class="empty-title">快伟通</h2>
      <p class="empty-desc">选择左侧的会话开始聊天</p>
      <p class="empty-tip">
        <el-icon><InfoFilled /></el-icon>
        支持文字、图片、文件、视频、语音消息
      </p>
    </div>

    <!-- 聊天主区域 -->
    <template v-else>
      <!-- 顶部工具栏 -->
      <header class="chat-header">
        <div class="chat-header-left">
          <el-avatar
            :size="36"
            :src="getFullFileUrl(chatStore.currentConversation.avatar)"
            shape="square"
            class="header-avatar"
          >
            {{ getAvatarFallback(chatStore.currentConversation.name) }}
          </el-avatar>
          <div class="header-info">
            <div class="header-name">
              {{ chatStore.currentConversation.name }}
              <span v-if="isGroupChat" class="member-count">
                ({{ chatStore.currentConversation.memberCount }})
              </span>
            </div>
            <div class="header-desc">
              {{ isGroupChat ? '群聊' : '私聊' }}
              <span v-if="typingText" class="typing-text">{{ typingText }}</span>
            </div>
          </div>
        </div>
        <div class="chat-header-right">
          <el-tooltip content="搜索消息" placement="bottom">
            <el-icon class="action-btn" @click="showMessageSearch = !showMessageSearch">
              <Search />
            </el-icon>
          </el-tooltip>
          <el-tooltip content="AI 助手" placement="bottom">
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
      </header>

      <!-- 消息搜索框 -->
      <div class="message-search-bar" v-if="showMessageSearch">
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
          @play-voice="handlePlayVoice"
          @download="handleDownload"
          @play-video="handlePlayVideo"
          @click-avatar="handleAvatarClick"
          @recall="handleRecallMessage"
          @forward="handleForwardMessage"
          @reply="handleReplyMessage"
          @delete="handleDeleteMessage"
          @translate="handleTranslateMessage"
        />
      </div>

      <!-- 黑名单提示 -->
      <div v-if="isChatBlocked" class="blocked-tip">
        <el-icon><Warning /></el-icon>
        <span>你已被对方拉黑，无法发送消息</span>
      </div>
      <div v-else-if="isChatBlockedByMe" class="blocked-by-me-tip">
        <el-icon><Warning /></el-icon>
        <span>你已拉黑对方，对方将无法收到你的消息</span>
        <el-button type="primary" link @click="handleUnblock">取消拉黑</el-button>
      </div>

      <!-- 输入区域 -->
      <ChatInput
        ref="chatInputRef"
        :disabled="isChatBlocked"
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

      <!-- AI 功能 -->
      <AiFeatures
        ref="aiFeaturesRef"
        :conversation-id="chatStore.currentConversation?.id"
        :recent-messages="recentTextMessages"
        @select-suggestion="handleSelectSuggestion"
      />

      <!-- 拖拽上传遮罩 -->
      <div
        v-if="isDragging"
        class="drag-overlay"
        @dragover.prevent
        @drop.prevent="handleDrop"
        @dragleave.prevent="isDragging = false"
      >
        <el-icon :size="48"><UploadFilled /></el-icon>
        <p>松开鼠标以上传文件</p>
      </div>
    </template>

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
import { uploadImage, uploadFile, uploadVideo, uploadVoice } from '@/api/file'
import { getConversationMembers } from '@/api/conversation'
import { recallMessage, deleteMessage, favoriteMessage } from '@/api/message'
import websocketManager from '@/utils/websocket'
import { getFullFileUrl } from '@/utils/platform'
import MessageBubble from '@/components/chat/MessageBubble.vue'
import ChatInput from '@/components/chat/ChatInput.vue'
import AiFeatures from '@/components/chat/AiFeatures.vue'
import GroupInfoPanel from '@/components/chat/GroupInfoPanel.vue'
import UserProfile from '@/components/chat/UserProfile.vue'
import {
  ChatDotRound, InfoFilled, Search, Opportunity, Loading,
  Warning, UploadFilled
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const chatStore = useChatStore()

const userInfo = computed(() => userStore.userInfo)

// 响应式状态
const messageListRef = ref(null)
const chatInputRef = ref(null)
const aiFeaturesRef = ref(null)
const groupInfoVisible = ref(false)
const userProfileVisible = ref(false)
const selectedUser = ref(null)
const groupMembers = ref([])
const typingUsers = ref([])
const replyMessage = ref(null)
const showMessageSearch = ref(false)
const messageSearchKeyword = ref('')
const isDragging = ref(false)

// 计算属性
const isGroupChat = computed(() => chatStore.currentConversation?.conversationType === 2)

const isChatBlocked = computed(() => {
  return chatStore.currentConversation?.conversationType === 1 && chatStore.isBlockedBy
})

const isChatBlockedByMe = computed(() => {
  return chatStore.currentConversation?.conversationType === 1 && chatStore.isBlocked
})

const filteredMessages = computed(() => {
  if (!messageSearchKeyword.value) return chatStore.messages
  const kw = messageSearchKeyword.value.toLowerCase()
  return chatStore.messages.filter(msg =>
    msg.content?.toLowerCase().includes(kw) ||
    msg.fileName?.toLowerCase().includes(kw)
  )
})

const displayMessages = computed(() => {
  return messageSearchKeyword.value ? filteredMessages.value : chatStore.messages
})

const recentTextMessages = computed(() => {
  return chatStore.messages
    .filter(msg => msg.messageType === 1 && msg.content)
    .slice(-20)
    .reverse()
})

const typingText = computed(() => {
  if (typingUsers.value.length === 0) return ''
  if (typingUsers.value.length === 1) {
    return `· ${typingUsers.value[0].senderName} 正在输入...`
  }
  return '· 多人正在输入...'
})

// 工具函数
const getAvatarFallback = (name) => {
  if (!name) return '用户'
  return name.length >= 2 ? name.slice(-2) : name
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

// 滚动加载更多
const handleScroll = () => {
  if (messageListRef.value && messageListRef.value.scrollTop < 50 && chatStore.hasMore && !chatStore.loading) {
    const oldHeight = messageListRef.value.scrollHeight
    chatStore.loadMoreMessages().then(() => {
      nextTick(() => {
        if (messageListRef.value) {
          messageListRef.value.scrollTop = messageListRef.value.scrollHeight - oldHeight
        }
      })
    })
  }
}

// 显示会话信息
const showConversationInfo = async () => {
  if (chatStore.currentConversation?.conversationType === 2) {
    groupInfoVisible.value = true
  } else if (chatStore.currentConversation?.conversationType === 1) {
    await showTargetUserInfo()
  }
}

const showTargetUserInfo = async () => {
  if (!chatStore.currentConversation) return
  try {
    const res = await getConversationMembers(chatStore.currentConversation.id)
    if (res.code === 200 && res.data) {
      const currentUserId = userStore.userInfo?.id
      const targetMember = res.data.find(member => (member.userId || member.id) !== currentUserId)
      if (targetMember) {
        const targetId = targetMember.userId || targetMember.id
        selectedUser.value = {
          id: targetId,
          nickname: targetMember.nickname || chatStore.currentConversation.name,
          avatar: targetMember.avatar || chatStore.currentConversation.avatar
        }
        await nextTick()
        userProfileVisible.value = true
      }
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

const handleAvatarClick = (data) => {
  if (data.senderId === userInfo.value?.id) return
  selectedUser.value = {
    id: data.senderId,
    nickname: data.senderName,
    avatar: data.senderAvatar
  }
  userProfileVisible.value = true
}

// 消息发送
const handleSendText = async (content) => {
  if (!chatStore.currentConversation) return
  const extra = {}
  if (replyMessage.value) {
    extra.replyMessageId = replyMessage.value.id
    extra.replyContent = replyMessage.value.content
    extra.replySenderName = replyMessage.value.senderName
  }
  await chatStore.sendMessage(chatStore.currentConversation.id, 1, content, extra)
  replyMessage.value = null
  scrollToBottom()
}

const handleSendImage = async (file) => {
  if (!chatStore.currentConversation) return
  try {
    const res = await uploadImage(file)
    if (res.code === 200) {
      await chatStore.sendMessage(chatStore.currentConversation.id, 2, null, {
        fileUrl: res.data.url, fileName: res.data.originalFileName,
        fileSize: res.data.fileSize, fileType: res.data.fileType
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
        fileUrl: res.data.url, fileName: res.data.originalFileName,
        fileSize: res.data.fileSize, fileType: res.data.fileType
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
        fileUrl: res.data.url, fileName: res.data.originalFileName,
        fileSize: res.data.fileSize, fileType: res.data.fileType
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
    const res = await uploadVoice(file)
    if (res.code === 200) {
      const duration = file.recordingDuration || await getAudioDuration(file)
      await chatStore.sendMessage(chatStore.currentConversation.id, 5, null, {
        fileUrl: res.data.url, fileName: res.data.originalFileName,
        fileSize: res.data.fileSize, fileType: res.data.fileType, duration
      })
      scrollToBottom()
    }
  } catch (error) {
    ElMessage.error('语音发送失败')
  }
}

const getAudioDuration = (file) => {
  return new Promise((resolve) => {
    const audio = new Audio()
    audio.onloadedmetadata = () => resolve(Math.round(audio.duration))
    audio.onerror = () => resolve(0)
    audio.src = URL.createObjectURL(file)
  })
}

// 输入状态
let typingTimer = null
const handleTyping = () => {
  if (!chatStore.currentConversation) return
  websocketManager.sendTyping(
    chatStore.currentConversation.id,
    chatStore.currentConversation.targetUserId
  )
}

const cancelReply = () => { replyMessage.value = null }

const handleReplyMessage = (message) => {
  replyMessage.value = {
    ...message,
    senderName: message.senderName || '未知用户'
  }
}

const handleSelectSuggestion = async (suggestion) => {
  if (!chatStore.currentConversation) return
  await chatStore.sendMessage(chatStore.currentConversation.id, 1, suggestion)
  scrollToBottom()
}

// 消息操作
const getFileUrl = (url) => getFullFileUrl(url)
const handlePlayVoice = () => {}
const handleDownload = (message) => { if (message.fileUrl) window.open(getFileUrl(message.fileUrl), '_blank') }
const handlePlayVideo = (message) => { if (message.fileUrl) window.open(getFileUrl(message.fileUrl), '_blank') }

const handleRecallMessage = async (message) => {
  try {
    const res = await recallMessage(message.id)
    if (res.code === 200) {
      const msg = chatStore.messages.find(m => m.id === message.id)
      if (msg) { msg.messageType = 7; msg.content = null }
      ElMessage.success('消息已撤回')
    }
  } catch (error) {
    ElMessage.error('撤回失败')
  }
}

const handleForwardMessage = () => { ElMessage.success('转发成功') }

const handleDeleteMessage = async (message) => {
  try {
    const res = await deleteMessage(message.id)
    if (res.code === 200) {
      const idx = chatStore.messages.findIndex(m => m.id === message.id)
      if (idx !== -1) chatStore.messages.splice(idx, 1)
      ElMessage.success('消息已删除')
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleTranslateMessage = (message) => {
  aiFeaturesRef.value?.translateDirectly(message)
}

// 取消拉黑
const handleUnblock = async () => {
  if (!chatStore.currentConversation?.targetUserId) return
  try {
    const { unblackFriend } = await import('@/api/friend')
    const res = await unblackFriend(chatStore.currentConversation.targetUserId)
    if (res.code === 200) {
      ElMessage.success('已取消拉黑')
      await chatStore.checkBlacklist(chatStore.currentConversation.targetUserId)
    }
  } catch (error) {
    ElMessage.error('取消拉黑失败')
  }
}

// ============ 拖拽上传（Web 端特有） ============
const handleDragEnter = (e) => {
  e.preventDefault()
  if (e.dataTransfer?.types?.includes('Files')) {
    isDragging.value = true
  }
}

const handleDrop = (e) => {
  isDragging.value = false
  const files = Array.from(e.dataTransfer?.files || [])
  if (files.length === 0) return
  files.forEach(file => {
    if (file.type.startsWith('image/')) handleSendImage(file)
    else if (file.type.startsWith('video/')) handleSendVideo(file)
    else if (file.type.startsWith('audio/')) handleSendVoice(file)
    else handleSendFile(file)
  })
}

// 监听当前会话变化
watch(() => chatStore.currentConversation, async (conv) => {
  if (conv) {
    if (conv.conversationType === 2) {
      const res = await getConversationMembers(conv.id)
      if (res.code === 200) groupMembers.value = res.data || []
    } else {
      groupMembers.value = []
    }
    scrollToBottom()
  }
})

onMounted(() => {
  window.addEventListener('dragenter', handleDragEnter)
  window.addEventListener('dragover', (e) => e.preventDefault())
  window.addEventListener('drop', handleDrop)
  console.log('[WebChatArea] mounted')
})

onUnmounted(() => {
  window.removeEventListener('dragenter', handleDragEnter)
  window.removeEventListener('drop', handleDrop)
})
</script>

<style lang="scss" scoped>
.web-chat-area-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
  overflow: hidden;
}

/* 空状态 */
.chat-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-placeholder, #c0c4cc);
  user-select: none;

  .empty-icon {
    opacity: 0.4;
    margin-bottom: 24px;
  }

  .empty-title {
    font-size: 28px;
    font-weight: 300;
    color: var(--text-secondary, #909399);
    margin: 0 0 8px;
  }

  .empty-desc {
    font-size: 14px;
    margin: 0 0 20px;
  }

  .empty-tip {
    font-size: 12px;
    color: var(--text-placeholder, #c0c4cc);
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

/* 顶部工具栏 */
.chat-header {
  flex-shrink: 0;
  height: 64px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--border-color, #e4e7ed);
  background: var(--bg-primary, #ffffff);
}

.chat-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex: 1;
}

.header-avatar { flex-shrink: 0; }

.header-info {
  min-width: 0;
  overflow: hidden;
}

.header-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #303133);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  .member-count {
    font-size: 13px;
    color: var(--text-secondary, #909399);
    font-weight: 400;
    margin-left: 4px;
  }
}

.header-desc {
  font-size: 12px;
  color: var(--text-secondary, #909399);
  margin-top: 2px;
}

.typing-text {
  color: #2b7fff;
  margin-left: 4px;
}

.chat-header-right {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.action-btn {
  font-size: 20px;
  color: var(--text-secondary, #909399);
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: all 0.15s;

  &:hover {
    color: #2b7fff;
    background: rgba(43, 127, 255, 0.08);
  }
}

/* 消息搜索栏 */
.message-search-bar {
  flex-shrink: 0;
  padding: 8px 24px;
  background: var(--bg-secondary, #f5f7fa);
  border-bottom: 1px solid var(--border-color, #e4e7ed);
  display: flex;
  align-items: center;
  gap: 12px;

  .search-count {
    font-size: 12px;
    color: var(--text-secondary, #909399);
    white-space: nowrap;
  }
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 24px;
  scrollbar-width: thin;
  scrollbar-color: var(--border-color, #c0c4cc) transparent;

  &::-webkit-scrollbar { width: 8px; }
  &::-webkit-scrollbar-thumb {
    background: var(--border-color, #c0c4cc);
    border-radius: 4px;
  }
  &::-webkit-scrollbar-track { background: transparent; }
}

.loading-tip, .no-more-tip {
  text-align: center;
  font-size: 12px;
  color: var(--text-placeholder, #c0c4cc);
  padding: 12px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

/* 黑名单提示 */
.blocked-tip, .blocked-by-me-tip {
  flex-shrink: 0;
  padding: 8px 24px;
  background: rgba(245, 108, 108, 0.08);
  color: var(--danger, #f56c6c);
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

/* 拖拽上传遮罩 */
.drag-overlay {
  position: absolute;
  inset: 0;
  background: rgba(43, 127, 255, 0.1);
  border: 2px dashed #2b7fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  color: #2b7fff;
  pointer-events: auto;

  p { margin-top: 12px; font-size: 16px; font-weight: 500; }
}
</style>