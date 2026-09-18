<template>
  <div class="web-sidebar-inner" :class="{ collapsed: isCollapsed }">
    <!-- 用户信息区 -->
    <div class="sidebar-user" @click="goProfile">
      <el-avatar :size="40" :src="getFullFileUrl(userInfo?.avatar)" shape="square" class="user-avatar">
        {{ getAvatarFallback(userInfo?.nickname || userInfo?.username) }}
      </el-avatar>
      <div class="user-info" v-show="!isCollapsed">
        <div class="user-name">{{ userInfo?.nickname || userInfo?.username || '用户' }}</div>
        <div class="user-dept">{{ userInfo?.department || '未设置部门' }}</div>
      </div>
    </div>

    <!-- 主菜单 -->
    <nav class="sidebar-menu">
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
          <span v-if="item.badge && item.badge > 0" class="menu-badge">
            {{ item.badge > 99 ? '99+' : item.badge }}
          </span>
        </div>
        <span class="menu-label" v-show="!isCollapsed">{{ item.label }}</span>
        <span class="menu-shortcut" v-show="!isCollapsed">{{ item.shortcut }}</span>
      </div>
    </nav>

    <!-- 底部操作区 -->
    <div class="sidebar-footer">
      <div class="menu-item" @click="toggleCollapse" :title="isCollapsed ? '展开侧栏' : '收起侧栏'">
        <div class="menu-icon-wrapper">
          <el-icon :size="20">
            <Expand v-if="isCollapsed" />
            <Fold v-else />
          </el-icon>
        </div>
        <span class="menu-label" v-show="!isCollapsed">收起</span>
      </div>

      <div class="menu-item" @click="toggleDarkMode" :title="isDarkMode ? '浅色模式' : '深色模式'">
        <div class="menu-icon-wrapper">
          <el-icon :size="20">
            <Sunny v-if="isDarkMode" />
            <Moon v-else />
          </el-icon>
        </div>
        <span class="menu-label" v-show="!isCollapsed">{{ isDarkMode ? '浅色' : '深色' }}</span>
      </div>

      <div class="menu-item logout" @click="handleLogout" title="退出登录">
        <div class="menu-icon-wrapper">
          <el-icon :size="20"><SwitchButton /></el-icon>
        </div>
        <span class="menu-label" v-show="!isCollapsed">退出</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useChatStore } from '@/store/chat'
import { useDarkMode } from '@/composables/useDarkMode'
import { getFullFileUrl } from '@/utils/platform'
import { ElMessageBox } from 'element-plus'
import {
  ChatDotRound, User, Star, Setting, Sunny, Moon,
  SwitchButton, Expand, Fold
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const chatStore = useChatStore()
const { isDarkMode, toggleDarkMode } = useDarkMode()

const isCollapsed = ref(false)
const userInfo = computed(() => userStore.userInfo)

const menuItems = computed(() => [
  { path: '/chat', icon: ChatDotRound, label: '聊天', badge: chatStore.totalUnread, shortcut: 'Ctrl+1' },
  { path: '/contacts', icon: User, label: '通讯录', badge: chatStore.unreadFriendRequests, shortcut: 'Ctrl+2' },
  { path: '/favorites', icon: Star, label: '收藏', badge: 0, shortcut: 'Ctrl+3' },
  { path: '/profile', icon: Setting, label: '设置', badge: 0, shortcut: 'Ctrl+4' }
])

const isActive = (path) => route.path.startsWith(path)

const getAvatarFallback = (name) => {
  if (!name) return '用户'
  return name.length >= 2 ? name.slice(-2) : name
}

const navigateTo = (path) => {
  if (route.path !== path) router.push(path)
}

const goProfile = () => {
  router.push('/profile')
}

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await userStore.logoutAction()
    router.push({ name: 'Login' })
  })
}

// 键盘快捷键
const handleKeydown = (e) => {
  if (e.ctrlKey || e.metaKey) {
    const map = { '1': '/chat', '2': '/contacts', '3': '/favorites', '4': '/profile' }
    if (map[e.key]) {
      e.preventDefault()
      navigateTo(map[e.key])
    }
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
  // 初始化侧栏状态（根据屏幕宽度）
  if (window.innerWidth < 1200) isCollapsed.value = true
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style lang="scss" scoped>
.web-sidebar-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px 12px;
  transition: width 0.25s ease;
  overflow: hidden;

  &.collapsed {
    padding: 16px 8px;
  }
}

/* 用户信息 */
.sidebar-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 24px;
  transition: background 0.15s;

  &:hover { background: var(--bg-hover, #f0f2f5); }

  .user-avatar { flex-shrink: 0; }

  .user-info {
    flex: 1;
    min-width: 0;
    overflow: hidden;
  }

  .user-name {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary, #303133);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .user-dept {
    font-size: 12px;
    color: var(--text-secondary, #909399);
    margin-top: 2px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

/* 主菜单 */
.sidebar-menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-secondary, #606266);
  transition: all 0.15s;
  user-select: none;
  position: relative;

  &:hover {
    background: var(--bg-hover, #f0f2f5);
    color: var(--text-primary, #303133);
  }

  &.active {
    background: rgba(43, 127, 255, 0.1);
    color: #2b7fff;
    font-weight: 500;
  }

  &.logout {
    color: var(--danger, #f56c6c);
    &:hover { background: rgba(245, 108, 108, 0.1); }
  }

  .menu-icon-wrapper {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    flex-shrink: 0;
  }

  .menu-label {
    flex: 1;
    font-size: 14px;
    white-space: nowrap;
  }

  .menu-shortcut {
    font-size: 11px;
    color: var(--text-placeholder, #c0c4cc);
    background: var(--bg-secondary, #f5f7fa);
    padding: 2px 6px;
    border-radius: 4px;
  }

  .menu-badge {
    position: absolute;
    top: -4px;
    right: -8px;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    background: var(--danger, #f56c6c);
    color: #fff;
    font-size: 10px;
    line-height: 16px;
    text-align: center;
    border-radius: 8px;
    font-weight: 500;
  }
}

/* 底部操作区 */
.sidebar-footer {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color, #e4e7ed);
  margin-top: 12px;
}
</style>