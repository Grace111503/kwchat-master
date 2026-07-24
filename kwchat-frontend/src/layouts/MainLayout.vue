<template>
  <div class="main-layout">
    <!-- 左侧导航栏（桌面端） -->
    <div class="sidebar hide-mobile">
      <div class="sidebar-header">
        <div class="user-avatar">
          <el-avatar :size="36" :src="userInfo?.avatar" shape="square">
            {{ getAvatarFallback(userInfo?.nickname || userInfo?.username) }}
          </el-avatar>
        </div>
      </div>

      <div class="sidebar-menu">
        <div
            v-for="item in menuItems"
            :key="item.path"
            class="menu-item"
            :class="{ active: isActive(item.path) }"
            @click="navigateTo(item.path)"
        >
          <div class="menu-icon-wrapper">
            <el-icon :size="20">
              <component :is="item.icon" />
            </el-icon>
            <span v-if="item.badge" class="menu-badge">{{ item.badge > 99 ? '99+' : item.badge }}</span>
          </div>
          <span class="menu-label">{{ item.label }}</span>
        </div>
      </div>

      <div class="sidebar-footer">
        <div class="menu-item" @click="handleLogout">
          <div class="menu-icon-wrapper">
            <el-icon :size="20">
              <SwitchButton />
            </el-icon>
          </div>
          <span class="menu-label">退出</span>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <router-view />
    </div>

    <!-- 底部导航栏（移动端） -->
    <div class="bottom-nav hide-desktop safe-area-bottom">
      <div
          v-for="item in menuItems"
          :key="item.path"
          class="bottom-nav-item"
          :class="{ active: isActive(item.path) }"
          @click="navigateTo(item.path)"
      >
        <div class="nav-icon-wrapper">
          <el-icon :size="22">
            <component :is="item.icon" />
          </el-icon>
          <span v-if="item.badge" class="nav-badge">{{ item.badge > 99 ? '99+' : item.badge }}</span>
        </div>
        <span class="nav-label">{{ item.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useChatStore } from '@/store/chat'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const chatStore = useChatStore()

const userInfo = computed(() => userStore.userInfo)

const menuItems = computed(() => [
  {
    path: '/chat',
    icon: 'ChatDotRound',
    label: '聊天',
    badge: chatStore.totalUnread
  },
  {
    path: '/contacts',
    icon: 'User',
    label: '通讯录',
    badge: chatStore.unreadFriendRequests
  },
  {
    path: '/favorites',
    icon: 'Star',
    label: '收藏',
    badge: 0
  },
  {
    path: '/profile',
    icon: 'Setting',
    label: '设置',
    badge: 0
  }
])

const isActive = (path) => {
  return route.path.startsWith(path)
}

// 获取头像 fallback 文字（显示最后两个字）
const getAvatarFallback = (name) => {
  if (!name) return '用户'
  return name.length >= 2 ? name.slice(-2) : name
}

const navigateTo = (path) => {
  if (route.path === path) return
  nextTick(() => {
    router.push(path).catch(() => {})
  })
}

const handleLogout = () => {
  ElMessageBox.confirm(
      '确定要退出登录吗？',
      '系统提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    await userStore.logoutAction()
    router.push({ name: 'Login' })
  })
}
</script>

<style lang="scss" scoped>
.main-layout {
  display: flex;
  height: 100vh;
  background: #f0f2f5;
}

.sidebar {
  width: 72px;
  background: #f0f2f5;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
  user-select: none;
  border-right: 1px solid #e5e5e5;
}

.sidebar-header {
  margin-bottom: 24px;
}

.user-avatar {
  cursor: pointer;
  transition: opacity 0.2s;

  &:hover {
    opacity: 0.8;
  }
}

.sidebar-menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.sidebar-footer {
  margin-top: auto;
}

.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  cursor: pointer;
  color: #86909c;
  transition: all 0.15s;
  position: relative;

  &:hover {
    color: #2b7fff;
  }

  &.active {
    color: #2b7fff;
    background: #e8f0fe;
  }
}

.menu-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-label {
  font-size: 11px;
  margin-top: 2px;
  letter-spacing: 0.5px;
}

.menu-badge {
  position: absolute;
  top: -6px;
  right: -10px;
  background: #f53f3f;
  color: #fff;
  font-size: 10px;
  min-width: 16px;
  height: 16px;
  line-height: 16px;
  text-align: center;
  padding: 0 4px;
  font-weight: 600;
}

.main-content {
  flex: 1;
  overflow: hidden;
}

// 底部导航栏（移动端）
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: #fff;
  border-top: 1px solid #e5e5e5;
  display: flex;
  align-items: center;
  justify-content: space-around;
  z-index: 100;
}

.bottom-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 100%;
  cursor: pointer;
  color: #86909c;
  transition: color 0.15s;

  &:active {
    opacity: 0.7;
  }

  &.active {
    color: #2b7fff;
  }
}

.nav-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-label {
  font-size: 10px;
  margin-top: 2px;
}

.nav-badge {
  position: absolute;
  top: -6px;
  right: -10px;
  background: #f53f3f;
  color: #fff;
  font-size: 10px;
  min-width: 16px;
  height: 16px;
  line-height: 16px;
  text-align: center;
  padding: 0 4px;
  font-weight: 600;
  border-radius: 8px;
}

// 移动端适配
@media (max-width: 768px) {
  .main-layout {
    padding-bottom: 56px;
  }

  .main-content {
    height: calc(100vh - 56px);
  }
}
</style>
