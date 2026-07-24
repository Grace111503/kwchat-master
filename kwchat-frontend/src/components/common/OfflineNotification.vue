<template>
  <transition name="slide-down">
    <div class="offline-notification" v-if="!isOnline">
      <div class="notification-content">
        <el-icon><WarningFilled /></el-icon>
        <span>网络连接已断开，消息可能无法发送</span>
      </div>
    </div>
  </transition>
  <transition name="slide-down">
    <div class="online-notification" v-if="showReconnect">
      <div class="notification-content">
        <el-icon><CircleCheckFilled /></el-icon>
        <span>网络已恢复连接</span>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { WarningFilled, CircleCheckFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const isOnline = ref(navigator.onLine)
const showReconnect = ref(false)
const wasOffline = ref(false)

// 网络状态变化处理
const handleOnline = () => {
  isOnline.value = true
  if (wasOffline.value) {
    showReconnect.value = true
    ElMessage.success('网络已恢复连接')
    // 3秒后隐藏恢复提示
    setTimeout(() => {
      showReconnect.value = false
    }, 3000)
  }
  wasOffline.value = false
}

const handleOffline = () => {
  isOnline.value = false
  wasOffline.value = true
  ElMessage.warning('网络连接已断开')
}

onMounted(() => {
  window.addEventListener('online', handleOnline)
  window.addEventListener('offline', handleOffline)
  // 初始状态
  isOnline.value = navigator.onLine
})

onUnmounted(() => {
  window.removeEventListener('online', handleOnline)
  window.removeEventListener('offline', handleOffline)
})
</script>

<style lang="scss" scoped>
.offline-notification,
.online-notification {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  padding: 12px;
  text-align: center;
  transition: transform 0.3s ease;
}

.offline-notification {
  background-color: #fdf6ec;
  border-bottom: 1px solid #faecd8;
}

.online-notification {
  background-color: #f0f9eb;
  border-bottom: 1px solid #e1f3d8;
}

.notification-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;

  .el-icon {
    font-size: 16px;
  }
}

.offline-notification .notification-content {
  color: #e6a23c;
}

.online-notification .notification-content {
  color: #67c23a;
}

// 动画
.slide-down-enter-active,
.slide-down-leave-active {
  transition: transform 0.3s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  transform: translateY(-100%);
}

// 移动端适配
@media (max-width: 768px) {
  .offline-notification,
  .online-notification {
    padding: 10px;
  }

  .notification-content {
    font-size: 13px;
  }
}
</style>
