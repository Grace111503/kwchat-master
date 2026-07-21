<template>
  <el-drawer
    v-model="visible"
    title="用户信息"
    direction="rtl"
    size="320px"
    @open="loadUserInfo"
  >
    <div class="user-profile" v-loading="loading">
      <template v-if="userInfo">
        <!-- 用户头像和基本信息 -->
        <div class="profile-header">
          <el-avatar :size="72" :src="userInfo.avatar" shape="square">
            {{ getAvatarFallback(userInfo.nickname) }}
          </el-avatar>
          <h2 class="profile-name">{{ userInfo.nickname || userInfo.username || '-' }}</h2>
          <p class="profile-username" v-if="userInfo.username">@{{ userInfo.username }}</p>
          <p class="profile-signature" v-if="userInfo.signature">{{ userInfo.signature }}</p>
        </div>

        <!-- 详细信息 -->
        <div class="profile-info">
          <div class="info-item">
            <span class="info-label">昵称</span>
            <span class="info-value">{{ userInfo.nickname || '-' }}</span>
          </div>
          <div class="info-item" v-if="userInfo.phone">
            <span class="info-label">手机号</span>
            <span class="info-value">{{ userInfo.phone }}</span>
          </div>
          <div class="info-item" v-if="userInfo.email">
            <span class="info-label">邮箱</span>
            <span class="info-value">{{ userInfo.email }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">性别</span>
            <span class="info-value">{{ formatGender(userInfo.gender) }}</span>
          </div>
          <div class="info-item" v-if="userInfo.birthday">
            <span class="info-label">生日</span>
            <span class="info-value">{{ formatBirthday(userInfo.birthday) }}</span>
          </div>
          <div class="info-item" v-if="userInfo.department">
            <span class="info-label">部门</span>
            <span class="info-value">{{ userInfo.department }}</span>
          </div>
          <div class="info-item" v-if="userInfo.signature">
            <span class="info-label">签名</span>
            <span class="info-value signature">{{ userInfo.signature }}</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="profile-actions" v-if="!isSelf">
          <el-button type="primary" @click="startChat">
            <el-icon><ChatDotRound /></el-icon>
            发消息
          </el-button>
        </div>
      </template>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useChatStore } from '@/store/chat'
import { getOrCreatePrivateConversation } from '@/api/conversation'
import { getUserDetail } from '@/api/user'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const props = defineProps({
  user: { type: Object, default: null }
})

const visible = defineModel('visible', { type: Boolean, default: false })

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

const loading = ref(false)
const userInfo = ref(null)

const isSelf = computed(() => {
  return userInfo.value?.id === userStore.userInfo?.id
})

const getAvatarFallback = (name) => {
  if (!name) return '用户'
  return name.length >= 2 ? name.slice(-2) : name
}

const formatGender = (gender) => {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '-'
}

const formatBirthday = (birthday) => {
  if (!birthday) return '-'
  return dayjs(birthday).format('YYYY年MM月DD日')
}

const loadUserInfo = async () => {
  if (!props.user?.id) return

  // 如果是自己，直接用 store 中的数据
  if (props.user.id === userStore.userInfo?.id) {
    userInfo.value = userStore.userInfo
    return
  }

  // 如果已有完整信息，直接使用
  if (props.user.phone || props.user.email || props.user.birthday) {
    userInfo.value = props.user
    return
  }

  // 从 API 获取完整用户信息
  loading.value = true
  try {
    const res = await getUserDetail(props.user.id)
    if (res.code === 200 && res.data) {
      userInfo.value = res.data
    } else {
      // 使用传入的基础信息
      userInfo.value = props.user
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    userInfo.value = props.user
  } finally {
    loading.value = false
  }
}

const startChat = async () => {
  if (!userInfo.value?.id) return
  try {
    const res = await getOrCreatePrivateConversation(userInfo.value.id)
    if (res.code === 200) {
      const conversation = res.data
      const exists = chatStore.conversations.find(c => c.id === conversation.id)
      if (!exists) chatStore.conversations.unshift(conversation)
      await chatStore.selectConversation(conversation)
      visible.value = false
      router.push('/chat')
    }
  } catch (error) {
    ElMessage.error('创建会话失败')
  }
}

// 监听 user 变化，重新加载
watch(() => props.user, (newUser) => {
  if (newUser && visible.value) {
    loadUserInfo()
  }
})
</script>

<style lang="scss" scoped>
.user-profile {
  padding: 0;
}

.profile-header {
  text-align: center;
  padding: 24px 20px;
  border-bottom: 1px solid #eee;

  .profile-name {
    font-size: 20px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 12px 0 4px;
  }

  .profile-username {
    font-size: 13px;
    color: #999;
    margin: 0 0 8px;
  }

  .profile-signature {
    font-size: 13px;
    color: #666;
    margin: 0;
    line-height: 1.5;
  }
}

.profile-info {
  padding: 16px 20px;
}

.info-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  width: 70px;
  font-size: 13px;
  color: #999;
}

.info-value {
  flex: 1;
  font-size: 13px;
  color: #333;

  &.signature {
    line-height: 1.5;
    white-space: pre-wrap;
  }
}

.profile-actions {
  padding: 20px;
  text-align: center;
}
</style>
