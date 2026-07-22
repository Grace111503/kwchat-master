<template>
  <div class="favorites-container">
    <div class="favorites-header">
      <h2>我的收藏</h2>
      <span class="count">共 {{ favorites.length }} 条</span>
    </div>

    <div class="favorites-list" v-loading="loading">
      <div
        v-for="message in favorites"
        :key="message.id"
        class="favorite-item"
        @click="goToMessage(message)"
      >
        <div class="message-sender">
          <el-avatar :size="32" :src="message.senderAvatar" shape="square">
            {{ getAvatarFallback(message.senderName) }}
          </el-avatar>
          <span class="sender-name">{{ message.senderName }}</span>
          <span class="message-time">{{ formatTime(message.createTime) }}</span>
        </div>
        <div class="message-content">
          <span v-if="message.messageType === 1">{{ message.content }}</span>
          <span v-else-if="message.messageType === 2">[图片]</span>
          <span v-else-if="message.messageType === 3">[文件] {{ message.fileName }}</span>
          <span v-else-if="message.messageType === 4">[视频]</span>
          <span v-else-if="message.messageType === 5">[语音]</span>
          <span v-else>[消息]</span>
        </div>
        <el-button
          type="danger"
          link
          size="small"
          @click.stop="handleUnfavorite(message)"
        >
          取消收藏
        </el-button>
      </div>

      <div v-if="favorites.length === 0 && !loading" class="empty-tip">
        <el-icon :size="48"><Star /></el-icon>
        <p>暂无收藏消息</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFavoritedMessages, unfavoriteMessage } from '@/api/message'
import { useChatStore } from '@/store/chat'
import dayjs from 'dayjs'

const router = useRouter()
const chatStore = useChatStore()

const loading = ref(false)
const favorites = ref([])

const getAvatarFallback = (name) => {
  if (!name) return '用户'
  return name.length >= 2 ? name.slice(-2) : name
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

const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await getFavoritedMessages()
    if (res.code === 200) {
      favorites.value = res.data || []
    }
  } catch (error) {
    console.error('加载收藏列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleUnfavorite = async (message) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏吗？', '取消收藏', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await unfavoriteMessage(message.id)
    favorites.value = favorites.value.filter(m => m.id !== message.id)
    ElMessage.success('已取消收藏')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const goToMessage = (message) => {
  // 跳转到聊天页面并定位到该消息
  router.push({
    path: '/chat',
    query: { conversationId: message.conversationId, messageId: message.id }
  })
}

onMounted(() => {
  loadFavorites()
})
</script>

<style lang="scss" scoped>
.favorites-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.favorites-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #e5e5e5;

  h2 {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 0;
  }

  .count {
    font-size: 13px;
    color: #999;
  }
}

.favorites-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.favorite-item {
  background: #fff;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

.message-sender {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;

  .sender-name {
    font-size: 14px;
    font-weight: 500;
    color: #333;
  }

  .message-time {
    font-size: 12px;
    color: #999;
    margin-left: auto;
  }
}

.message-content {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: #ccc;

  .el-icon {
    margin-bottom: 12px;
  }

  p {
    font-size: 14px;
  }
}
</style>
