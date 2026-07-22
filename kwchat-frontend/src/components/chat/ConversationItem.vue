<template>
  <div
    class="conversation-item"
    :class="{ active: isActive }"
    @click="$emit('select', conversation)"
    @contextmenu.prevent="showContextMenu"
  >
    <div class="conversation-avatar">
      <el-avatar
        :size="42"
        :src="avatarUrl"
        shape="circle"
        :style="!avatarUrl ? getAvatarStyle(conversation.name) : {}"
      >
        {{ getAvatarFallback(conversation.name) }}
      </el-avatar>
      <span class="online-dot" v-if="showOnlineDot && isOnline"></span>
      <span class="pin-icon" v-if="conversation.isTop">📌</span>
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

    <!-- 右键菜单 -->
    <el-popover
      v-model:visible="showMenu"
      placement="right-start"
      :width="140"
      trigger="manual"
      :virtual-ref="menuRef"
      virtual-triggering
    >
      <div class="context-menu">
        <div class="menu-item" @click="handlePin">
          <span>{{ conversation.isTop ? '取消置顶' : '置顶' }}</span>
        </div>
        <div class="menu-item" @click="handleMute">
          <span>{{ conversation.doNotDisturb ? '取消免打扰' : '免打扰' }}</span>
        </div>
        <div class="menu-item danger" @click="handleDelete">
          <span>删除会话</span>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { setTop, setDoNotDisturb, getConversationMembers } from '@/api/conversation'
import { generateGroupAvatar } from '@/utils/groupAvatar'
import { ElMessage } from 'element-plus'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps({
  conversation: { type: Object, required: true },
  isActive: { type: Boolean, default: false },
  isOnline: { type: Boolean, default: false },
  showOnlineDot: { type: Boolean, default: true }
})

const emit = defineEmits(['select', 'pin', 'mute', 'delete'])

const showMenu = ref(false)
const menuRef = ref(null)
const generatedAvatar = ref(null)

const showContextMenu = (e) => {
  menuRef.value = e.target
  showMenu.value = true
}

const handlePin = async () => {
  showMenu.value = false
  try {
    const newIsTop = props.conversation.isTop ? 0 : 1
    await setTop(props.conversation.id, newIsTop)
    props.conversation.isTop = newIsTop
    ElMessage.success(newIsTop ? '已置顶' : '已取消置顶')
    emit('pin', { conversation: props.conversation, isTop: newIsTop })
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleMute = async () => {
  showMenu.value = false
  try {
    const newDoNotDisturb = props.conversation.doNotDisturb ? 0 : 1
    await setDoNotDisturb(props.conversation.id, newDoNotDisturb)
    props.conversation.doNotDisturb = newDoNotDisturb
    ElMessage.success(newDoNotDisturb ? '已开启免打扰' : '已关闭免打扰')
    emit('mute', { conversation: props.conversation, doNotDisturb: newDoNotDisturb })
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = () => {
  showMenu.value = false
  emit('delete', props.conversation)
}

// 获取头像 fallback 文字（显示最后两个字）
const getAvatarFallback = (name) => {
  if (!name) return '会话'
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

  // 根据名称计算颜色索引
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

const displayName = computed(() => {
  const { conversationType, name, memberCount } = props.conversation
  if (conversationType === 1) return name
  return `${name} (${memberCount})`
})

// 生成群聊头像
const generateAvatar = async () => {
  console.log('尝试生成群头像:', props.conversation.id, props.conversation.conversationType, props.conversation.avatar)

  // 只处理群聊（conversationType === 2）且没有自定义头像的情况
  if (props.conversation.conversationType !== 2 || props.conversation.avatar) {
    console.log('跳过生成:', props.conversation.conversationType !== 2 ? '不是群聊' : '已有头像')
    return
  }

  try {
    // 获取群成员列表
    console.log('获取群成员:', props.conversation.id)
    const res = await getConversationMembers(props.conversation.id)
    console.log('群成员响应:', res)

    if (res.code === 200 && res.data && res.data.length > 0) {
      // 提取前9个成员的头像URL
      const avatars = res.data
        .slice(0, 9)
        .map(member => member.avatar)
        .filter(avatar => avatar) // 过滤空头像

      console.log('成员头像:', avatars)

      if (avatars.length > 0) {
        // 生成群头像
        console.log('开始生成群头像...')
        generatedAvatar.value = await generateGroupAvatar(avatars, 200)
        console.log('群头像生成成功:', generatedAvatar.value?.substring(0, 50) + '...')
      } else {
        console.log('没有可用的头像')
      }
    } else {
      console.log('获取群成员失败或为空')
    }
  } catch (error) {
    console.error('生成群头像失败:', error)
  }
}

// 计算最终显示的头像URL
const avatarUrl = computed(() => {
  const url = props.conversation.avatar || generatedAvatar.value
  console.log('avatarUrl:', url ? url.substring(0, 50) + '...' : 'null')
  return url
})

// 监听会话变化，重新生成头像
watch(() => props.conversation.id, () => {
  if (props.conversation.conversationType === 2 && !props.conversation.avatar) {
    generateAvatar()
  }
}, { immediate: true })

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

.pin-icon {
  position: absolute;
  top: -4px;
  right: -4px;
  font-size: 12px;
}

.context-menu {
  .menu-item {
    padding: 8px 12px;
    cursor: pointer;
    border-radius: 4px;
    transition: background 0.15s;

    &:hover {
      background: #f5f5f5;
    }

    span {
      font-size: 13px;
      color: #333;
    }

    &.danger {
      span {
        color: #f56c6c;
      }

      &:hover {
        background: #fef0f0;
      }
    }
  }
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
