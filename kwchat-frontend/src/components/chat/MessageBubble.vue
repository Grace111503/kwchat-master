<template>
  <div class="message-bubble-wrapper" :class="{ 'is-self': isSelf, 'is-selected': isSelected }">
    <el-avatar
      :size="34"
      :src="message.senderAvatar"
      shape="square"
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

const emit = defineEmits(['play-voice', 'download', 'play-video', 'click-avatar', 'recall', 'forward', 'reply', 'delete', 'multi-select'])
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
const playVoice = () => {
  if (!props.message.fileUrl) {
    ElMessage.error('语音文件不存在')
    return
  }

  if (isPlaying.value && audio) {
    audio.pause()
    isPlaying.value = false
    return
  }

  audio = new Audio(props.message.fileUrl)
  audio.onended = () => {
    isPlaying.value = false
  }
  audio.onerror = () => {
    isPlaying.value = false
    ElMessage.error('语音播放失败')
  }
  audio.play()
  isPlaying.value = true
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
    background: rgba(0, 0, 0, 0.02);
  }

  &.is-selected {
    background: #e8f0fe;

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
      background: #d1eaff;
      border-color: #bae0ff;
    }

    .message-meta {
      flex-direction: row-reverse;
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
  background: #f5f7fa;
  border-left: 3px solid #2b7fff;
  padding: 6px 10px;
  margin-bottom: 6px;
  border-radius: 0 4px 4px 0;

  .reply-content {
    font-size: 12px;
    color: #999;
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
  color: #999;
  margin-bottom: 3px;
}

.message-bubble {
  background: #fff;
  padding: 8px 12px;
  word-break: break-word;
  border: 1px solid #e5e5e5;

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
    background: #f5f5f5;
    border: none;
    text-align: center;
    font-size: 12px;
    color: #999;
  }

  &.bubble-recalled {
    background: #f5f5f5;
    border: none;
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #999;
  }
}

.message-text {
  font-size: 14px;
  line-height: 1.6;
  color: #1a1a1a;

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
  background: #f5f5f5;
  color: #999;
}

.message-file {
  display: flex;
  align-items: center;
  padding: 4px;
  cursor: pointer;
  min-width: 180px;

  &:hover {
    background: rgba(0, 0, 0, 0.02);
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
  color: #333;
  margin-bottom: 2px;
}

.file-size {
  font-size: 11px;
  color: #999;
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
  gap: 6px;
  cursor: pointer;
  padding: 2px 4px;
  min-width: 100px;

  .el-icon.playing {
    color: #2b7fff;
  }
}

.voice-duration {
  font-size: 13px;
  color: #333;
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
  color: #bbb;
}

.message-status {
  display: flex;
  align-items: center;
}
</style>
