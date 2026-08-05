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

      <el-tooltip :content="isRecording ? '点击发送' : '点击录音'" placement="top">
        <el-icon
          class="toolbar-btn"
          :class="{ 'recording': isRecording }"
          @click="toggleVoiceRecord"
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
      <span class="tip">点击麦克风发送</span>
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
import { isMobile as checkIsMobile } from '@/utils/platform'

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

// 检测是否是移动设备（使用平台工具）
const isMobile = () => {
  return checkIsMobile()
}

// 检测是否是 iOS
const isIOS = () => {
  return /iPhone|iPad|iPod/i.test(navigator.userAgent) ||
    (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1)
}

// 拍照
const takePhoto = () => {
  if (isIOS()) {
    // iOS：使用模板中的 input，设置 capture 属性后触发
    // iOS Safari 需要在用户手势的同步调用栈中触发 click
    const input = imageInputRef.value
    if (input) {
      // 设置 capture 属性打开相机
      input.setAttribute('capture', 'environment')
      input.click()
      // 拍照完成后移除 capture 属性（下次点击打开相册）
      setTimeout(() => {
        input.removeAttribute('capture')
      }, 1000)
    }
  } else {
    // 非 iOS：使用模板中的 input
    triggerImageUpload()
  }
}

// 录制视频
const recordVideo = () => {
  if (isIOS()) {
    const input = videoInputRef.value
    if (input) {
      input.setAttribute('capture', 'environment')
      input.click()
      setTimeout(() => {
        input.removeAttribute('capture')
      }, 1000)
    }
  } else {
    triggerVideoUpload()
  }
}

// 切换录音状态（点击一次开始，再点击一次发送）
const toggleVoiceRecord = async () => {
  if (isRecording.value) {
    // 如果正在录音，停止并发送
    stopVoiceRecord()
  } else {
    // 如果没有录音，开始录音
    await startVoiceRecord()
  }
}

// 开始录音
const startVoiceRecord = async () => {
  try {
    // 检测是否在 Capacitor 环境中
    const isCapacitorApp = window.Capacitor || window.location.protocol === 'capacitor:' ||
      (window.location.protocol === 'https:' && window.location.hostname === 'localhost' &&
       /Android|iPhone|iPad|iPod/i.test(navigator.userAgent))

    // 检查浏览器支持
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      if (isCapacitorApp) {
        ElMessage.error('录音功能不可用，请重启应用后重试')
      } else if (window.location.protocol !== 'https:' && window.location.hostname !== 'localhost') {
        ElMessage.error('录音功能需要 HTTPS 环境，请使用 HTTPS 访问或联系管理员配置 SSL 证书')
      } else {
        ElMessage.error('您的浏览器不支持录音功能')
      }
      return
    }

    if (typeof MediaRecorder === 'undefined') {
      ElMessage.error('您的浏览器不支持录音功能')
      return
    }

    // 请求麦克风权限
    let stream
    try {
      // 先检查权限状态
      if (navigator.permissions && navigator.permissions.query) {
        try {
          const permissionStatus = await navigator.permissions.query({ name: 'microphone' })
          console.log('麦克风权限状态:', permissionStatus.state)
          if (permissionStatus.state === 'denied') {
            if (isCapacitorApp) {
              ElMessage.error('麦克风权限被系统拒绝。请在手机"设置 → 应用 → 快伟通 → 权限"中开启麦克风权限，然后重启应用')
            } else {
              ElMessage.error('麦克风权限被拒绝。请在浏览器地址栏左侧的锁图标中允许麦克风权限')
            }
            return
          }
        } catch (e) {
          // permissions.query 不支持 microphone 时忽略，继续尝试 getUserMedia
          console.log('无法查询麦克风权限状态，继续尝试录音:', e.message)
        }
      }

      stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    } catch (permError) {
      console.error('麦克风权限错误:', permError)
      if (permError.name === 'NotAllowedError' || permError.name === 'PermissionDeniedError') {
        if (isCapacitorApp) {
          ElMessage.error('麦克风权限被拒绝。请在手机"设置 → 应用 → 快伟通 → 权限"中开启麦克风权限，然后重启应用')
        } else {
          ElMessage.error('麦克风权限被拒绝。请在浏览器地址栏左侧的锁图标中允许麦克风权限')
        }
      } else if (permError.name === 'NotFoundError') {
        ElMessage.error('未找到麦克风设备，请确认设备有麦克风')
      } else {
        ElMessage.error('录音启动失败: ' + permError.message)
      }
      return
    }

    // 使用兼容性更好的格式（优先mp4，因为Safari不支持webm）
    let mimeType = ''
    let fileExtension = 'webm'

    // 优先使用mp4格式（兼容性最好）
    if (MediaRecorder.isTypeSupported('audio/mp4')) {
      mimeType = 'audio/mp4'
      fileExtension = 'm4a'
    } else if (MediaRecorder.isTypeSupported('audio/webm;codecs=opus')) {
      mimeType = 'audio/webm;codecs=opus'
      fileExtension = 'webm'
    } else if (MediaRecorder.isTypeSupported('audio/webm')) {
      mimeType = 'audio/webm'
      fileExtension = 'webm'
    }

    console.log('使用录音格式:', mimeType || '默认', '文件扩展名:', fileExtension)

    mediaRecorder = mimeType ? new MediaRecorder(stream, { mimeType }) : new MediaRecorder(stream)
    audioChunks = []

    mediaRecorder.ondataavailable = (e) => {
      console.log('录音数据:', e.data.size, '字节')
      if (e.data.size > 0) {
        audioChunks.push(e.data)
      }
    }

    mediaRecorder.onstop = () => {
      const totalSize = audioChunks.reduce((sum, chunk) => sum + chunk.size, 0)
      console.log('录音完成，总大小:', totalSize, '字节，时长:', recordingTime.value, '秒')

      if (totalSize < 100) {
        ElMessage.warning('录音数据过短，请长按麦克风录音')
        stream.getTracks().forEach(track => track.stop())
        return
      }

      const blob = new Blob(audioChunks, { type: mediaRecorder.mimeType || 'audio/webm' })
      const file = new File([blob], `voice_${Date.now()}.${fileExtension}`, { type: mediaRecorder.mimeType || 'audio/webm' })
      // 将录音时长传递给父组件
      file.recordingDuration = recordingTime.value
      emit('send-voice', file)
      stream.getTracks().forEach(track => track.stop())
    }

    // 不使用 timeslice，一次性收集所有数据
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

    ElMessage.success('开始录音，点击麦克风发送')
  } catch (error) {
    console.error('录音启动失败:', error)
    const isCapacitorApp = window.Capacitor || window.location.protocol === 'capacitor:' ||
      (window.location.protocol === 'https:' && window.location.hostname === 'localhost' &&
       /Android|iPhone|iPad|iPod/i.test(navigator.userAgent))

    if (error.name === 'NotAllowedError') {
      if (isCapacitorApp) {
        ElMessage.error('麦克风权限被拒绝。请在手机"设置 → 应用 → 快伟通 → 权限"中开启麦克风权限')
      } else {
        ElMessage.error('麦克风权限被拒绝。请在浏览器地址栏左侧的锁图标中允许麦克风权限')
      }
    } else if (error.name === 'NotFoundError') {
      ElMessage.error('未找到麦克风设备')
    } else {
      ElMessage.error('录音启动失败: ' + error.message)
    }
  }
}

// 停止录音并发送
const stopVoiceRecord = () => {
  if (!isRecording.value || !mediaRecorder || mediaRecorder.state !== 'recording') {
    return
  }

  // 检查录音时长
  if (recordingTime.value < 1) {
    ElMessage.warning('录音时间太短')
    cancelVoiceRecord()
    return
  }

  mediaRecorder.stop()
  isRecording.value = false
  if (recordingTimer) {
    clearInterval(recordingTimer)
    recordingTimer = null
  }
}

// 取消录音（不发送）
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
    ElMessage.info('已取消录音')
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
  border-top: 1px solid var(--border-color);
  background: var(--bg-primary);
  position: relative;
}

.reply-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 16px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);

  .reply-content {
    flex: 1;
    overflow: hidden;

    .reply-label {
      font-size: 12px;
      color: #2b7fff;
    }

    .reply-text {
      font-size: 12px;
      color: var(--text-secondary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .reply-close {
    cursor: pointer;
    color: var(--text-placeholder);
    margin-left: 8px;

    &:hover {
      color: var(--text-primary);
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
  color: var(--text-placeholder);
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
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-color);
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
    color: var(--text-placeholder);
  }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.input-tip {
  font-size: 12px;
  color: var(--text-placeholder);
}

:deep(.el-textarea__inner) {
  border: none;
  box-shadow: none;
  padding: 0 16px;
  border-radius: 0;
  background: var(--bg-primary) !important;
  color: var(--text-primary) !important;

  &::placeholder {
    color: var(--text-placeholder) !important;
  }
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
