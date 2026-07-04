<template>
  <div class="chat-container">
    <!-- 左侧会话列表 -->
    <div class="conversation-list">
      <div class="conversation-header">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索会话"
          prefix-icon="Search"
          clearable
        />
      </div>

      <div class="conversation-items">
        <ConversationItem
          v-for="conversation in filteredConversations"
          :key="conversation.id"
          :conversation="conversation"
          :is-active="chatStore.currentConversation?.id === conversation.id"
          @select="handleSelectConversation"
        />

        <div v-if="filteredConversations.length === 0" class="empty-tip">
          <span>暂无会话</span>
        </div>
      </div>
    </div>

    <!-- 右侧聊天窗口 -->
    <div class="chat-window">
      <template v-if="chatStore.currentConversation">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="chat-title">
            <span class="name">{{ chatStore.currentConversation.name }}</span>
            <span v-if="chatStore.currentConversation.conversationType === 2" class="member-count">
              ({{ chatStore.currentConversation.memberCount }})
            </span>
          </div>
          <div class="chat-actions">
            <el-tooltip content="群信息" placement="bottom">
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
            v-for="message in chatStore.messages"
            :key="message.id"
            :message="message"
            :is-self="message.senderId === userInfo?.id"
            @play-voice="handlePlayVoice"
            @download="handleDownload"
            @play-video="handlePlayVideo"
          />
        </div>

        <!-- 输入区域 -->
        <ChatInput
          ref="chatInputRef"
          :disabled="false"
          @send="handleSendText"
          @send-image="handleSendImage"
          @send-file="handleSendFile"
          @typing="handleTyping"
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useUserStore } from '@/store/user'
import { useChatStore } from '@/store/chat'
import { ElMessage } from 'element-plus'
import { uploadImage, uploadFile } from '@/api/file'
import ConversationItem from '@/components/chat/ConversationItem.vue'
import MessageBubble from '@/components/chat/MessageBubble.vue'
import ChatInput from '@/components/chat/ChatInput.vue'

const userStore = useUserStore()
const chatStore = useChatStore()
const userInfo = computed(() => userStore.userInfo)

const searchKeyword = ref('')
const messageListRef = ref(null)
const chatInputRef = ref(null)

const filteredConversations = computed(() => {
  if (!searchKeyword.value) return chatStore.conversations
  return chatStore.conversations.filter(item =>
    item.name?.includes(searchKeyword.value)
  )
})

const handleSelectConversation = async (conversation) => {
  await chatStore.selectConversation(conversation)
  scrollToBottom()
}

const handleSendText = async (content) => {
  if (!chatStore.currentConversation) return
  await chatStore.sendMessage(chatStore.currentConversation.id, 1, content)
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

const handleTyping = () => {}
const handlePlayVoice = (message) => ElMessage.info('语音播放功能开发中')
const handleDownload = (message) => { if (message.fileUrl) window.open(message.fileUrl, '_blank') }
const handlePlayVideo = (message) => { if (message.fileUrl) window.open(message.fileUrl, '_blank') }
const showConversationInfo = () => ElMessage.info('会话信息功能开发中')

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

onMounted(() => { chatStore.loadConversations() })
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
</style>
