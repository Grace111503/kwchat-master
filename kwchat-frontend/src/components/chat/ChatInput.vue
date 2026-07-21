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
      <el-tooltip :content="isMobile() ? '拍照' : '选择图片'" placement="top">
        <el-icon class="toolbar-btn" @click="takePhoto">
          <Camera />
        </el-icon>
      </el-tooltip>
      <el-tooltip content="文件" placement="top">
        <el-icon class="toolbar-btn" @click="triggerFileUpload">
          <Folder />
        </el-icon>
      </el-tooltip>
      <el-tooltip content="视频" placement="top">
        <el-icon class="toolbar-btn" @click="triggerVideoUpload">
          <VideoCamera />
        </el-icon>
      </el-tooltip>
      <el-tooltip :content="isMobile() ? '录制视频' : '选择视频'" placement="top">
        <el-icon class="toolbar-btn" @click="recordVideo">
          <VideoPlay />
        </el-icon>
      </el-tooltip>
      <el-tooltip content="语音" placement="top">
        <el-icon
          class="toolbar-btn"
          :class="{ 'recording': isRecording }"
          @mousedown="startVoiceRecord"
          @mouseup="stopVoiceRecord"
          @mouseleave="cancelVoiceRecord"
        >
          <Microphone />
        </el-icon>
      </el-tooltip>
      <el-tooltip content="@提醒" placement="top" v-if="isGroup">
        <el-icon class="toolbar-btn" @click="showMentionList = !showMentionList">
          <Bell />
        </el-icon>
      </el-tooltip>
    </div>

    <!-- 录音提示 -->
    <div class="recording-tip" v-if="isRecording">
      <span class="recording-dot"></span>
      <span>正在录音... {{ recordingTime }}s</span>
      <span class="tip">松开发送</span>
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
      @paste="handlePaste"
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
    <input ref="videoInputRef" type="file" accept="video/*" style="display: none" @change="handleVideoSelect" />
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

const emit = defineEmits(['send', 'send-image', 'send-file', 'send-video', 'send-voice', 'typing', 'cancel-reply'])

const inputRef = ref(null)
const imageInputRef = ref(null)
const fileInputRef = ref(null)
const videoInputRef = ref(null)
const messageContent = ref('')
const showEmojiPanel = ref(false)
const showMentionList = ref(false)

// 录音相关
const isRecording = ref(false)
const recordingTime = ref(0)
let mediaRecorder = null
let audioChunks = []
let recordingTimer = null

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
  if (member.id === 'all' || member.userId === 'all') {
    messageContent.value += '@所有人 '
  } else {
    messageContent.value += `@${member.nickname || member.username} `
  }
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
const triggerVideoUpload = () => { videoInputRef.value?.click() }

// 检测是否是移动设备
const isMobile = () => {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
}

// 拍照
const takePhoto = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  if (isMobile()) {
    input.capture = 'environment' // 手机使用后置摄像头
  }
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (file) {
      if (file.size > 10 * 1024 * 1024) {
        ElMessage.error('图片大小不能超过10MB')
        return
      }
      emit('send-image', file)
    }
  }
  input.click()
}

// 录制视频
const recordVideo = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'video/*'
  if (isMobile()) {
    input.capture = 'environment'
  }
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (file) {
      if (file.size > 50 * 1024 * 1024) {
        ElMessage.error('视频大小不能超过50MB')
        return
      }
      emit('send-video', file)
    }
  }
  input.click()
}

// 开始录音
const startVoiceRecord = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })

    // 按优先级尝试支持的音频格式
    const supportedTypes = [
      'audio/mp4',
      'audio/webm;codecs=opus',
      'audio/webm',
      'audio/ogg;codecs=opus',
      'audio/wav'
    ]

    let mimeType = ''
    for (const type of supportedTypes) {
      if (MediaRecorder.isTypeSupported(type)) {
        mimeType = type
        break
      }
    }

    mediaRecorder = mimeType ? new MediaRecorder(stream, { mimeType }) : new MediaRecorder(stream)
    audioChunks = []

    mediaRecorder.ondataavailable = (e) => {
      if (e.data.size > 0) {
        audioChunks.push(e.data)
      }
    }

    mediaRecorder.onstop = () => {
      const blob = new Blob(audioChunks, { type: mediaRecorder.mimeType || 'audio/mp4' })
      // 根据格式确定扩展名
      let ext = 'm4a'
      if (mediaRecorder.mimeType?.includes('webm')) ext = 'webm'
      else if (mediaRecorder.mimeType?.includes('ogg')) ext = 'ogg'
      else if (mediaRecorder.mimeType?.includes('wav')) ext = 'wav'

      const file = new File([blob], `voice_${Date.now()}.${ext}`, { type: mediaRecorder.mimeType || 'audio/mp4' })
      emit('send-voice', file)
      stream.getTracks().forEach(track => track.stop())
    }

    mediaRecorder.start()
    isRecording.value = true
    recordingTime.value = 0

    // 计时
    recordingTimer = setInterval(() => {
      recordingTime.value++
      if (recordingTime.value >= 60) {
        stopVoiceRecord()
      }
    }, 1000)
  } catch (error) {
    ElMessage.error('无法访问麦克风，请检查权限')
  }
}

// 停止录音
const stopVoiceRecord = () => {
  if (mediaRecorder && mediaRecorder.state === 'recording') {
    mediaRecorder.stop()
  }
  isRecording.value = false
  if (recordingTimer) {
    clearInterval(recordingTimer)
    recordingTimer = null
  }
}

// 取消录音
const cancelVoiceRecord = () => {
  if (isRecording.value) {
    if (mediaRecorder && mediaRecorder.state === 'recording') {
      mediaRecorder.stop()
      audioChunks = [] // 清空数据，不发送
    }
    isRecording.value = false
    if (recordingTimer) {
      clearInterval(recordingTimer)
      recordingTimer = null
    }
  }
}

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

const handleVideoSelect = (event) => {
  const file = event.target.files[0]
  if (!file) return
  if (!file.type.startsWith('video/')) { ElMessage.error('请选择视频文件'); return }
  if (file.size > 50 * 1024 * 1024) { ElMessage.error('视频大小不能超过50MB'); return }

  // 检查视频格式，HEVC/H.265 浏览器不支持
  const supportedTypes = ['video/mp4', 'video/webm', 'video/ogg']
  if (!supportedTypes.includes(file.type)) {
    ElMessage.warning('建议使用 MP4 格式（H.264编码），其他格式可能无法播放')
  }

  emit('send-video', file)
  event.target.value = ''
}

const handlePaste = (event) => {
  const items = event.clipboardData?.items
  if (!items) return

  for (const item of items) {
    if (item.type.startsWith('image/')) {
      event.preventDefault()
      const file = item.getAsFile()
      if (file) {
        if (file.size > 10 * 1024 * 1024) {
          ElMessage.error('图片大小不能超过10MB')
          return
        }
        emit('send-image', file)
      }
      break
    }
  }
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

  &.recording {
    color: #f56c6c;
    animation: pulse 1s infinite;
  }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 16px;
}

.recording-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #fff3f3;
  border-top: 1px solid #fde2e2;
  font-size: 13px;
  color: #f56c6c;

  .recording-dot {
    width: 8px;
    height: 8px;
    background: #f56c6c;
    border-radius: 50%;
    animation: blink 1s infinite;
  }

  .tip {
    margin-left: auto;
    font-size: 12px;
    color: #999;
  }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
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
