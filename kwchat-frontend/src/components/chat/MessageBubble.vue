<template>
  <div class="message-bubble-wrapper" :class="{ 'is-self': isSelf, 'is-selected': isSelected }">
    <el-avatar
      :size="34"
      :src="message.senderAvatar"
      shape="circle"
      class="message-avatar"
      :class="{ 'clickable': !isSelf }"
      :style="!message.senderAvatar ? getAvatarStyle(message.senderName) : {}"
      @click="handleAvatarClick"
    >
      {{ getAvatarFallback(message.senderName) }}
    </el-avatar>

    <div class="message-content">
      <!-- 引用消息预览 -->
      <div class="reply-preview" v-if="message.replyMessageId">
        <div class="reply-content">
          <span class="reply-name">{{ message.replySenderName || '未知用户' }}：</span>
          <span class="reply-text">{{ getReplyPreviewText() }}</span>
        </div>
      </div>

      <div class="message-sender" v-if="!isSelf">{{ message.senderName }}</div>

      <div class="message-bubble" :class="bubbleClass">
        <!-- 文本消息 -->
        <template v-if="message.messageType === 1">
          <div class="message-text" v-html="formatText(message.content)"></div>
        </template>

        <!-- 图片消息 -->
        <template v-else-if="message.messageType === 2">
          <el-image
            :src="message.fileUrl"
            :preview-src-list="[message.fileUrl]"
            class="message-image"
            fit="cover"
          >
            <template #error>
              <div class="image-error">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </template>

        <!-- 文件消息 -->
        <template v-else-if="message.messageType === 3">
          <div class="message-file" @click="downloadFile">
            <div class="file-icon">
              <el-icon :size="28" :color="'#2b7fff'"><Document /></el-icon>
            </div>
            <div class="file-info">
              <div class="file-name text-ellipsis">{{ message.fileName }}</div>
              <div class="file-size">{{ formatFileSize(message.fileSize) }}</div>
            </div>
          </div>
        </template>

        <!-- 视频消息 -->
        <template v-else-if="message.messageType === 4">
          <div class="message-video-wrapper">
            <video
              :src="message.fileUrl"
              class="message-video"
              controls
              preload="metadata"
              playsinline
            />
          </div>
        </template>

        <!-- 语音消息 -->
        <template v-else-if="message.messageType === 5">
          <div class="message-voice" @click="playVoice">
            <el-icon :size="18" :class="{ 'playing': isPlaying }">
              <VideoPlay v-if="!isPlaying" />
              <VideoPause v-else />
            </el-icon>
            <span class="voice-duration">{{ message.duration || 0 }}s</span>
            <div class="voice-wave" v-if="isPlaying">
              <span></span><span></span><span></span>
            </div>
          </div>
        </template>

        <!-- 系统消息 -->
        <template v-else-if="message.messageType === 6">
          <div class="message-system">{{ message.content }}</div>
        </template>

        <!-- 撤回消息 -->
        <template v-else-if="message.messageType === 7">
          <div class="message-recalled">
            <el-icon><InfoFilled /></el-icon>
            <span>{{ isSelf ? '你' : '对方' }}撤回了一条消息</span>
          </div>
        </template>
      </div>

      <div class="message-meta">
        <span class="message-time">{{ formatTime(message.createTime) }}</span>
        <span class="message-status" v-if="isSelf">
          <el-icon v-if="message.isRead" color="#2b7fff" :size="14"><Select /></el-icon>
          <el-icon v-else color="#ccc" :size="14"><Check /></el-icon>
        </span>
      </div>
    </div>

    <!-- 消息操作菜单 -->
    <MessageActions
      v-if="message.messageType !== 6"
      :message="message"
      :is-self="isSelf"
      @recall="handleRecall"
      @forward="handleForward"
      @reply="handleReply"
      @delete="handleDeleteMessage"
      @multi-select="handleMultiSelect"
      @translate="handleTranslateMessage"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import MessageActions from './MessageActions.vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps({
  message: { type: Object, required: true },
  isSelf: { type: Boolean, default: false },
  isSelected: { type: Boolean, default: false }
})

const emit = defineEmits(['play-voice', 'download', 'play-video', 'click-avatar', 'recall', 'forward', 'reply', 'delete', 'translate','multi-select'])
const isPlaying = ref(false)

// 获取头像 fallback 文字（显示最后两个字）
const getAvatarFallback = (name) => {
  if (!name) return '用户'
  return name.length >= 2 ? name.slice(-2) : name
}

// 根据名称生成头像背景色
const getAvatarStyle = (name) => {
  if (!name) return { backgroundColor: '#409eff' }

  const colors = [
    '#409eff', '#67c23a', '#e6a23c', '#f56c6c',
    '#909399', '#00b42a', '#2b7fff', '#722ed1',
    '#13c2c2', '#eb2f96'
  ]

  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  const index = Math.abs(hash) % colors.length

  return {
    backgroundColor: colors[index],
    color: '#fff'
  }
}

const bubbleClass = computed(() => {
  const { messageType } = props.message
  return {
    'bubble-text': messageType === 1,
    'bubble-image': messageType === 2,
    'bubble-file': messageType === 3,
    'bubble-video': messageType === 4,
    'bubble-voice': messageType === 5,
    'bubble-system': messageType === 6,
    'bubble-recalled': messageType === 7
  }
})

const formatText = (text) => {
  if (!text) return ''
  // 渲染自定义表情 [emoji:name]
  text = text.replace(/\[emoji:([^\]]+)\]/g, '<img src="/emoji/$1.png" class="custom-emoji" alt="$1" onerror="this.style.display=\'none\'" />')
  text = text.replace(/(https?:\/\/[^\s]+)/g, '<a href="$1" target="_blank">$1</a>')
  text = text.replace(/\n/g, '<br>')
  return text
}

const formatTime = (time) => {
  if (!time) return ''
  const date = dayjs(time)
  const now = dayjs()
  if (date.isSame(now, 'day')) return date.format('HH:mm')
  if (date.isSame(now.subtract(1, 'day'), 'day')) return '昨天 ' + date.format('HH:mm')
  if (date.isSame(now, 'year')) return date.format('MM-DD HH:mm')
  return date.format('YYYY-MM-DD HH:mm')
}

const formatFileSize = (size) => {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let index = 0
  let fileSize = size
  while (fileSize >= 1024 && index < units.length - 1) {
    fileSize /= 1024
    index++
  }
  return `${fileSize.toFixed(1)} ${units[index]}`
}

let audio = null
const playVoice = async () => {
  if (!props.message.fileUrl) {
    ElMessage.error('语音文件不存在')
    return
  }

  // 停止当前播放
  if (isPlaying.value && audio) {
    audio.pause()
    audio.currentTime = 0
    isPlaying.value = false
    return
  }

  console.log('尝试播放语音:', props.message.fileUrl)

  try {
    // 获取音频文件并创建 blob URL
    const response = await fetch(props.message.fileUrl)
    console.log('响应状态:', response.status, response.headers.get('content-type'))

    if (!response.ok) throw new Error('文件获取失败: ' + response.status)

    const blob = await response.blob()
    console.log('文件大小:', blob.size, '类型:', blob.type)

    // 检查文件大小（太小可能是空文件或损坏）
    if (blob.size < 100) {
      ElMessage.error('语音文件无效（文件过小）')
      return
    }

    // 创建 blob URL
    const blobUrl = URL.createObjectURL(blob)
    console.log('Blob URL:', blobUrl)

    // 根据音频类型创建不同的播放器
    const audioType = blob.type || 'audio/webm'
    console.log('音频类型:', audioType)

    // 检查浏览器是否支持该音频格式
    const audioElement = document.createElement('audio')
    const canPlayType = audioElement.canPlayType(audioType)
    console.log('浏览器支持该格式:', canPlayType)

    if (canPlayType === '') {
      console.warn('浏览器不支持该音频格式，尝试使用默认格式播放')
    }

    audio = new Audio()
    audio.src = blobUrl
    audio.type = audioType

    audio.onended = () => {
      isPlaying.value = false
      URL.revokeObjectURL(blobUrl)
    }

    audio.onerror = (e) => {
      isPlaying.value = false
      URL.revokeObjectURL(blobUrl)
      const errorCode = e.target.error?.code
      const errorMsg = e.target.error?.message || '未知错误'
      console.error('音频播放错误:', errorCode, errorMsg)
      ElMessage.error('语音播放失败: ' + errorMsg)
    }

    // 预加载音频
    audio.load()

    await audio.play()
    isPlaying.value = true
  } catch (e) {
    isPlaying.value = false
    console.error('语音播放失败:', e)
    ElMessage.error('语音播放失败: ' + e.message)
  }
}
const downloadFile = () => emit('download', props.message)
const playVideo = () => emit('play-video', props.message)

const handleAvatarClick = () => {
  if (!props.isSelf) {
    emit('click-avatar', {
      senderId: props.message.senderId,
      senderName: props.message.senderName,
      senderAvatar: props.message.senderAvatar
    })
  }
}

const handleRecall = (message) => {
  emit('recall', message)
}

const handleForward = (data) => {
  emit('forward', data)
}

const handleReply = (message) => {
  emit('reply', message)
}

const handleDeleteMessage = (message) => {
  emit('delete', message)
}

const handleMultiSelect = () => {
  emit('multi-select', props.message)
}

const handleTranslateMessage = (message) => {
  emit('translate', message)
}

// 下载语音
const downloadVoice = () => {
  if (!props.message.fileUrl) return
  // 创建下载链接
  const link = document.createElement('a')
  link.href = props.message.fileUrl
  link.download = `voice_${props.message.id}.audio`
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  ElMessage.success('语音已下载，请用播放器打开')
}

const getReplyPreviewText = () => {
  if (props.message.replyContent) {
    return props.message.replyContent.substring(0, 40) + (props.message.replyContent.length > 40 ? '...' : '')
  }
  return '[消息]'
}
</script>

<style lang="scss" scoped>
.message-bubble-wrapper {
  display: flex;
  margin-bottom: 12px;
  padding: 0 16px;
  cursor: pointer;
  transition: background 0.15s;

  &:hover {
    background: var(--bg-hover);
  }

  &.is-selected {
    background: var(--bg-secondary);

    .message-bubble {
      border-color: #2b7fff;
    }
  }

  &.is-self {
    flex-direction: row-reverse;

    .message-content {
      align-items: flex-end;
      margin-left: 0;
      margin-right: 10px;
    }

    .message-sender {
      text-align: right;
    }

    .message-bubble {
      background: #2b7fff;
      color: #fff;
      border-color: #2b7fff;
    }

    .message-meta {
      flex-direction: row-reverse;

      .message-time {
        color: var(--text-placeholder);
      }
    }
  }
}

.message-avatar {
  flex-shrink: 0;

  &.clickable {
    cursor: pointer;

    &:hover {
      opacity: 0.8;
    }
  }
}

.message-content {
  display: flex;
  flex-direction: column;
  margin-left: 10px;
  max-width: 60%;
}

.reply-preview {
  background: var(--bg-secondary);
  border-left: 3px solid #2b7fff;
  padding: 6px 10px;
  margin-bottom: 6px;
  border-radius: 0 4px 4px 0;

  .reply-content {
    font-size: 12px;
    color: var(--text-placeholder);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .reply-name {
    color: #2b7fff;
  }
}

.message-sender {
  font-size: 12px;
  color: var(--text-placeholder);
  margin-bottom: 3px;
}

.message-bubble {
  background: var(--bg-primary);
  padding: 8px 12px;
  word-break: break-word;
  border: 1px solid var(--border-color);

  &.bubble-image {
    padding: 2px;
    background: transparent;
    border: none;
  }

  &.bubble-video {
    padding: 0;
    overflow: hidden;
    border: none;
  }

  &.bubble-system {
    background: var(--bg-secondary);
    border: none;
    text-align: center;
    font-size: 12px;
    color: var(--text-placeholder);
  }

  &.bubble-recalled {
    background: var(--bg-secondary);
    border: none;
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: var(--text-placeholder);
  }
}

.message-text {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-primary);

  :deep(a) {
    color: #2b7fff;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  :deep(.custom-emoji) {
    width: 20px;
    height: 20px;
    vertical-align: middle;
    margin: 0 2px;
  }
}

.message-image {
  max-width: 200px;
  max-height: 200px;
  cursor: pointer;
  display: block;
}

.image-error {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  color: var(--text-placeholder);
}

.message-file {
  display: flex;
  align-items: center;
  padding: 4px;
  cursor: pointer;
  min-width: 180px;

  &:hover {
    background: var(--bg-hover);
  }
}

.file-icon {
  margin-right: 10px;
}

.file-info {
  flex: 1;
  overflow: hidden;
}

.file-name {
  font-size: 13px;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.file-size {
  font-size: 11px;
  color: var(--text-placeholder);
}

.message-video-wrapper {
  position: relative;
  cursor: pointer;
}

.message-video {
  max-width: 280px;
  max-height: 200px;
  display: block;
  border-radius: 4px;
}

.message-voice {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  min-width: 100px;
  cursor: pointer;
  background: var(--bg-secondary);
  border-radius: 16px;
  transition: background 0.2s;

  &:hover {
    background: var(--bg-hover);
  }

  .el-icon.playing {
    color: #2b7fff;
  }

  .voice-duration {
    font-size: 13px;
    color: var(--text-primary);
  }
}

.voice-duration {
  font-size: 13px;
  color: var(--text-primary);
}

.voice-wave {
  display: flex;
  align-items: center;
  gap: 2px;
  height: 16px;

  span {
    width: 3px;
    height: 100%;
    background: #2b7fff;
    animation: wave 0.5s ease-in-out infinite;

    &:nth-child(2) {
      animation-delay: 0.1s;
    }

    &:nth-child(3) {
      animation-delay: 0.2s;
    }
  }
}

@keyframes wave {
  0%, 100% {
    height: 4px;
  }
  50% {
    height: 14px;
  }
}

.message-system {
  padding: 4px 12px;
}

.message-recalled {
  display: flex;
  align-items: center;
  gap: 4px;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 3px;
}

.message-time {
  font-size: 11px;
  color: var(--text-placeholder);
}

.message-status {
  display: flex;
  align-items: center;
}
</style>
