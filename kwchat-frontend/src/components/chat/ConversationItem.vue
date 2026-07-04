<template>
  <div
    class="conversation-item"
    :class="{ active: isActive }"
    @click="$emit('select', conversation)"
  >
    <div class="conversation-avatar">
      <el-avatar :size="42" :src="conversation.avatar" shape="square">
        {{ getAvatarFallback(conversation.name) }}
      </el-avatar>
      <span class="online-dot" v-if="showOnlineDot && isOnline"></span>
    </div>

    <div class="conversation-info">
      <div class="conversation-header">
        <span class="conversation-name text-ellipsis">
          {{ displayName }}
        </span>
        <span class="conversation-time">{{ formatTime(conversation.lastMessageTime) }}</span>
      </div>

      <div class="conversation-footer">
        <span class="last-message text-ellipsis">
          {{ conversation.lastMessageContent || '暂无消息' }}
        </span>
        <el-badge
          :value="conversation.unreadCount"
          :max="99"
          :hidden="!conversation.unreadCount"
          class="unread-badge"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps({
  conversation: { type: Object, required: true },
  isActive: { type: Boolean, default: false },
  isOnline: { type: Boolean, default: false },
  showOnlineDot: { type: Boolean, default: true }
})

defineEmits(['select'])

// 获取头像 fallback 文字（显示最后两个字）
const getAvatarFallback = (name) => {
  if (!name) return '会话'
  return name.length >= 2 ? name.slice(-2) : name
}

const displayName = computed(() => {
  const { conversationType, name, memberCount } = props.conversation
  if (conversationType === 1) return name
  return `${name} (${memberCount})`
})

const formatTime = (time) => {
  if (!time) return ''
  const date = dayjs(time)
  const now = dayjs()

  if (date.isSame(now, 'day')) return date.format('HH:mm')
  if (date.isSame(now.subtract(1, 'day'), 'day')) return '昨天'
  if (date.isSame(now, 'week')) {
    const days = ['日', '一', '二', '三', '四', '五', '六']
    return '周' + days[date.day()]
  }
  if (date.isSame(now, 'year')) return date.format('M/D')
  return date.format('YY/M/D')
}
</script>

<style lang="scss" scoped>
.conversation-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  cursor: pointer;
  transition: background 0.1s;
  border-bottom: 1px solid #f0f0f0;

  &:hover {
    background: #f0f0f0;
  }

  &.active {
    background: #e8f0fe;
  }
}

.conversation-avatar {
  position: relative;
  flex-shrink: 0;
}

.online-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 8px;
  height: 8px;
  background: #00b42a;
  border: 2px solid #fff;
}

.conversation-info {
  flex: 1;
  margin-left: 10px;
  overflow: hidden;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conversation-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  flex: 1;
}

.conversation-time {
  font-size: 11px;
  color: #bbb;
  flex-shrink: 0;
  margin-left: 8px;
}

.conversation-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.last-message {
  font-size: 12px;
  color: #999;
  flex: 1;
}

.unread-badge {
  flex-shrink: 0;
  margin-left: 8px;
}
</style>
