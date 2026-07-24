<template>
  <OfflineNotification />
  <router-view />
</template>

<script setup>
import { onMounted, onUnmounted, watch } from 'vue'
import { useUserStore } from '@/store/user'
import { useChatStore } from '@/store/chat'
import OfflineNotification from '@/components/common/OfflineNotification.vue'

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