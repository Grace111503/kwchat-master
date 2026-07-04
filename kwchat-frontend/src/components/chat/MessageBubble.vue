<template>
  <div class="message-bubble-wrapper" :class="{ 'is-self': isSelf }">
    <el-avatar :size="34" :src="message.senderAvatar" shape="square" class="message-avatar">
      {{ getAvatarFallback(message.senderName) }}
    </el-avatar>

    <div class="message-content">
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
            <video :src="message.fileUrl" class="message-video" preload="metadata" @click="playVideo" />
            <div class="video-play-btn" @click="playVideo">
              <el-icon :size="36"><VideoPlay /></el-icon>
            </div>
          </div>
        </template>

        <!-- 语音消息 -->
        <template v-else-if="message.messageType === 5">
          <div class="message-voice" @click="playVoice">
            <el-icon :size="18"><Microphone /></el-icon>
            <span class="voice-duration">{{ message.duration || 0 }}s</span>
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
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps({
  message: { type: Object, required: true },
  isSelf: { type: Boolean, default: false }
})

const emit = defineEmits(['play-voice', 'download', 'play-video'])
const isPlaying = ref(false)

// 获取头像 fallback 文字（显示最后两个字）
const getAvatarFallback = (name) => {
  if (!name) return '用户'
  return name.length >= 2 ? name.slice(-2) : name
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

const playVoice = () => { isPlaying.value = true; emit('play-voice', props.message); setTimeout(() => { isPlaying.value = false }, (props.message.duration || 1) * 1000) }
const downloadFile = () => emit('download', props.message)
const playVideo = () => emit('play-video', props.message)
</script>

<style lang="scss" scoped>
.message-bubble-wrapper {
  display: flex;
  margin-bottom: 12px;
  padding: 0 16px;

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
}

.message-content {
  display: flex;
  flex-direction: column;
  margin-left: 10px;
  max-width: 60%;
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
}

.video-play-btn {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 48px;
  height: 48px;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: background 0.15s;

  &:hover {
    background: rgba(0, 0, 0, 0.7);
  }
}

.message-voice {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 2px 4px;
}

.voice-duration {
  font-size: 13px;
  color: #333;
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
