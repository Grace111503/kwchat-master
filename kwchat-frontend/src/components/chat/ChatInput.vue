<template>
  <div class="chat-input">
    <!-- 引用消息提示 -->
    <div class="reply-preview" v-if="replyMessage">
      <div class="reply-content">
        <span class="reply-label">回复 {{ replyMessage.senderName }}：</span>
        <span class="reply-text">{{ getReplyPreview(replyMessage) }}</span>
      </div>
      <el-icon class="reply-close" @click="cancelReply"><Close /></el-icon>
    </div>

    <!-- 工具栏 -->
    <div class="input-toolbar">
      <el-tooltip content="表情" placement="top">
        <el-icon class="toolbar-btn" @click="showEmojiPanel = !showEmojiPanel">
          <ChatDotRound />
        </el-icon>
      </el-tooltip>
      <el-tooltip content="图片" placement="top">
        <el-icon class="toolbar-btn" @click="triggerImageUpload">
          <Picture />
        </el-icon>
      </el-tooltip>
      <el-tooltip content="文件" placement="top">
        <el-icon class="toolbar-btn" @click="triggerFileUpload">
          <Folder />
        </el-icon>
      </el-tooltip>
      <el-tooltip content="@提醒" placement="top" v-if="isGroup">
        <el-icon class="toolbar-btn" @click="showMentionList = !showMentionList">
          <Bell />
        </el-icon>
      </el-tooltip>
    </div>

    <!-- 表情面板 -->
    <EmojiPanel :visible="showEmojiPanel" @select="insertEmoji" @select-custom="insertCustomEmoji" />

    <!-- @成员列表 -->
    <MentionList :visible="showMentionList" :members="members" @select="insertMention" />

    <!-- 输入框 -->
    <el-input
      ref="inputRef"
      v-model="messageContent"
      type="textarea"
      :rows="3"
      placeholder="输入消息..."
      resize="none"
      @keydown.enter.exact.prevent="handleSend"
      @keydown.enter.ctrl.except="handleNewLine"
      @input="handleInput"
      @focus="handleFocus"
    />

    <!-- 底部 -->
    <div class="input-footer">
      <span class="input-tip">Enter 发送 / Ctrl+Enter 换行</span>
      <el-button type="primary" size="small" @click="handleSend" :disabled="!canSend">
        发 送
      </el-button>
    </div>

    <!-- 隐藏的文件输入 -->
    <input ref="imageInputRef" type="file" accept="image/*" style="display: none" @change="handleImageSelect" />
    <input ref="fileInputRef" type="file" style="display: none" @change="handleFileSelect" />
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import EmojiPanel from './EmojiPanel.vue'
import MentionList from './MentionList.vue'

const props = defineProps({
  disabled: { type: Boolean, default: false },
  isGroup: { type: Boolean, default: false },
  members: { type: Array, default: () => [] },
  replyMessage: { type: Object, default: null }
})

const emit = defineEmits(['send', 'send-image', 'send-file', 'typing', 'cancel-reply'])

const inputRef = ref(null)
const imageInputRef = ref(null)
const fileInputRef = ref(null)
const messageContent = ref('')
const showEmojiPanel = ref(false)
const showMentionList = ref(false)

const canSend = computed(() => messageContent.value.trim().length > 0 && !props.disabled)

const handleSend = () => {
  if (!canSend.value) return
  emit('send', messageContent.value.trim())
  messageContent.value = ''
  showEmojiPanel.value = false
  showMentionList.value = false
  nextTick(() => { inputRef.value?.focus() })
}

const handleNewLine = () => { messageContent.value += '\n' }

let typingTimer = null
const handleInput = () => {
  if (typingTimer) clearTimeout(typingTimer)
  typingTimer = setTimeout(() => { emit('typing') }, 1000)
}

const handleFocus = () => {
  showEmojiPanel.value = false
  showMentionList.value = false
}

const insertEmoji = (emoji) => { messageContent.value += emoji; inputRef.value?.focus() }
const insertCustomEmoji = (emoji) => { messageContent.value += `[emoji:${emoji.name}]`; showEmojiPanel.value = false; inputRef.value?.focus() }
const insertMention = (member) => {
  if (member.id === 'all') { messageContent.value += '@所有人 ' }
  else { messageContent.value += `@${member.nickname} ` }
  showMentionList.value = false
  inputRef.value?.focus()
}

const cancelReply = () => { emit('cancel-reply') }

const getReplyPreview = (message) => {
  const { messageType, content, fileName } = message
  switch (messageType) {
    case 1: return content?.substring(0, 30) || ''
    case 2: return '[图片]'
    case 3: return `[文件] ${fileName || ''}`
    case 4: return '[视频]'
    case 5: return '[语音]'
    default: return content || ''
  }
}

const triggerImageUpload = () => { imageInputRef.value?.click() }
const triggerFileUpload = () => { fileInputRef.value?.click() }

const handleImageSelect = (event) => {
  const file = event.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) { ElMessage.error('请选择图片文件'); return }
  if (file.size > 10 * 1024 * 1024) { ElMessage.error('图片大小不能超过10MB'); return }
  emit('send-image', file)
  event.target.value = ''
}

const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (!file) return
  if (file.size > 100 * 1024 * 1024) { ElMessage.error('文件大小不能超过100MB'); return }
  emit('send-file', file)
  event.target.value = ''
}

watch(messageContent, (newVal) => {
  const lastAtIndex = newVal.lastIndexOf('@')
  if (lastAtIndex >= 0) {
    const textAfterAt = newVal.substring(lastAtIndex + 1)
    showMentionList.value = !textAfterAt.includes(' ') && textAfterAt.length < 20
  } else {
    showMentionList.value = false
  }
})

defineExpose({
  focus: () => inputRef.value?.focus(),
  clear: () => { messageContent.value = '' }
})
</script>

<style lang="scss" scoped>
.chat-input {
  border-top: 1px solid #e5e5e5;
  background: #fff;
  position: relative;
}

.reply-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 16px;
  background: #f5f5f5;
  border-bottom: 1px solid #e5e5e5;

  .reply-content {
    flex: 1;
    overflow: hidden;

    .reply-label {
      font-size: 12px;
      color: #2b7fff;
    }

    .reply-text {
      font-size: 12px;
      color: #666;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .reply-close {
    cursor: pointer;
    color: #999;
    margin-left: 8px;

    &:hover {
      color: #333;
    }
  }
}

.input-toolbar {
  display: flex;
  gap: 14px;
  padding: 6px 16px;
}

.toolbar-btn {
  font-size: 18px;
  color: #999;
  cursor: pointer;
  transition: color 0.15s;

  &:hover {
    color: #2b7fff;
  }
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 16px;
}

.input-tip {
  font-size: 12px;
  color: #ccc;
}

:deep(.el-textarea__inner) {
  border: none;
  box-shadow: none;
  padding: 0 16px;
  border-radius: 0;
}

:deep(.el-button--primary) {
  background: #2b7fff;
  border: none;
  border-radius: 0;

  &:hover {
    background: #1a6fe0;
  }
}
</style>
