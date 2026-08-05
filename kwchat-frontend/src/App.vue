<template>
  <OfflineNotification />
  <router-view />
</template>

<script setup>
import { onMounted, onUnmounted, watch } from 'vue'
import { useUserStore } from '@/store/user'
import { useChatStore } from '@/store/chat'
import { ElNotification } from 'element-plus'
import OfflineNotification from '@/components/common/OfflineNotification.vue'
import { isCapacitor } from '@/utils/platform'

const userStore = useUserStore()
const chatStore = useChatStore()

// 监听登录状态，初始化WebSocket
watch(() => userStore.isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    chatStore.initWebSocket()
  } else {
    chatStore.disconnectWebSocket()
  }
}, { immediate: true })

onMounted(() => {
  // 初始化用户状态
  userStore.initUserState()

  // 检查是否有热更新提示
  if (isCapacitor()) {
    const updateNotes = localStorage.getItem('kwchat_update_notes')
    const newVersion = localStorage.getItem('kwchat_new_version')
    if (updateNotes && newVersion) {
      // 延迟显示，等页面加载完成
      setTimeout(() => {
        ElNotification({
          title: '应用已更新',
          message: `版本 ${newVersion}：${updateNotes}`,
          type: 'success',
          duration: 5000,
          position: 'top-right'
        })
        // 清除提示，避免重复显示
        localStorage.removeItem('kwchat_update_notes')
        localStorage.removeItem('kwchat_new_version')
      }, 2000)
    }
  }
})

onUnmounted(() => {
  // 断开WebSocket连接
  chatStore.disconnectWebSocket()
})
</script>

<style lang="scss">
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

#app {
  height: 100%;
}
</style>