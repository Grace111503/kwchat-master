<template>
  <div class="notification-alert">
    <!-- 声音提醒 -->
    <audio ref="audioRef" preload="auto">
      <source src="@/assets/sounds/message.mp3" type="audio/mpeg">
    </audio>

    <!-- 桌面通知权限请求 -->
    <el-dialog
      v-model="showPermissionDialog"
      title="开启通知"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="permission-content">
        <el-icon :size="48" color="#409eff"><Bell /></el-icon>
        <p>开启桌面通知，及时接收新消息提醒</p>
      </div>
      <template #footer>
        <el-button @click="showPermissionDialog = false">稍后再说</el-button>
        <el-button type="primary" @click="requestPermission">开启通知</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useChatStore } from '@/store/chat'
import { ElNotification } from 'element-plus'

const chatStore = useChatStore()
const audioRef = ref(null)
const showPermissionDialog = ref(false)

// 通知设置
const notificationSettings = ref({
  sound: true,        // 声音提醒
  desktop: true,      // 桌面通知
  badge: true,        // 角标显示
  flash: true         // 闪烁提醒
})

// 初始化
onMounted(() => {
  // 检查通知权限
  checkNotificationPermission()

  // 监听新消息
  chatStore.$onAction(({ name, args }) => {
    if (name === 'handleNewMessage') {
      handleNewMessage(args[0])
    }
    if (name === 'handleFriendRequestNotify') {
      // 好友申请通知，更新角标
      if (notificationSettings.value.badge) {
        updateBadge()
      }
    }
  })
})

// 检查通知权限
const checkNotificationPermission = () => {
  if (!('Notification' in window)) {
    console.log('浏览器不支持桌面通知')
    return
  }

  if (Notification.permission === 'default') {
    showPermissionDialog.value = true
  }
}

// 请求通知权限
const requestPermission = async () => {
  if (!('Notification' in window)) {
    return
  }

  const permission = await Notification.requestPermission()
  if (permission === 'granted') {
    showPermissionDialog.value = false
    console.log('通知权限已获取')
  }
}

// 处理新消息
const handleNewMessage = (message) => {
  // 如果页面在前台且是当前会话，不提醒
  if (!document.hidden && chatStore.currentConversation?.id === message.conversationId) {
    return
  }

  // 播放声音
  if (notificationSettings.value.sound) {
    playSound()
  }

  // 显示桌面通知
  if (notificationSettings.value.desktop) {
    showDesktopNotification(message)
  }

  // 更新角标
  if (notificationSettings.value.badge) {
    updateBadge()
  }

  // 闪烁标题
  if (notificationSettings.value.flash && document.hidden) {
    startFlashTitle()
  }
}

// 播放提示音
const playSound = () => {
  if (audioRef.value) {
    audioRef.value.currentTime = 0
    audioRef.value.play().catch(e => console.log('播放声音失败:', e))
  }
}

// 显示桌面通知
const showDesktopNotification = (message) => {
  if (Notification.permission !== 'granted') {
    return
  }

  const title = message.senderName || '新消息'
  const body = getNotificationBody(message)
  const icon = message.senderAvatar || '/favicon.ico'

  const notification = new Notification(title, {
    body,
    icon,
    tag: `message-${message.id}`,
    renotify: true
  })

  notification.onclick = () => {
    window.focus()
    // 跳转到对应会话
    const conversation = chatStore.conversations.find(c => c.id === message.conversationId)
    if (conversation) {
      chatStore.selectConversation(conversation)
    }
    notification.close()
  }

  // 5秒后自动关闭
  setTimeout(() => notification.close(), 5000)
}

// 获取通知内容
const getNotificationBody = (message) => {
  const { messageType, content, fileName } = message
  switch (messageType) {
    case 1: return content?.substring(0, 50) || '新消息'
    case 2: return '[图片]'
    case 3: return `[文件] ${fileName || ''}`
    case 4: return '[视频]'
    case 5: return '[语音]'
    default: return '新消息'
  }
}

// 更新角标
const updateBadge = () => {
  if ('setAppBadge' in navigator) {
    const total = chatStore.totalUnread + chatStore.unreadFriendRequests
    navigator.setAppBadge(total > 0 ? total : 0)
  }
}

// 闪烁标题
let flashTimer = null
const originalTitle = document.title
const startFlashTitle = () => {
  if (flashTimer) return

  let flash = false
  flashTimer = setInterval(() => {
    document.title = flash ? originalTitle : '【新消息】' + originalTitle
    flash = !flash
  }, 1000)

  // 页面可见时停止闪烁
  const handleVisibilityChange = () => {
    if (!document.hidden) {
      stopFlashTitle()
      document.removeEventListener('visibilitychange', handleVisibilityChange)
    }
  }
  document.addEventListener('visibilitychange', handleVisibilityChange)
}

const stopFlashTitle = () => {
  if (flashTimer) {
    clearInterval(flashTimer)
    flashTimer = null
    document.title = originalTitle
  }
}

// 暴露方法
defineExpose({
  playSound,
  updateBadge,
  settings: notificationSettings
})
</script>

<style lang="scss" scoped>
.notification-alert {
  display: none;
}

.permission-content {
  text-align: center;
  padding: 20px;

  p {
    margin-top: 16px;
    color: #666;
  }
}
</style>