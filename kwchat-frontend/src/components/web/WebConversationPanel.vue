<template>
  <div class="web-conversation-panel-inner">
    <!-- 顶部搜索栏 -->
    <header class="panel-header">
      <div class="panel-title-row">
        <h2 class="panel-title">{{ pageTitle }}</h2>
        <el-tooltip content="新建会话" placement="bottom">
          <el-icon class="add-btn" @click="showNewChatDialog = true">
            <Plus />
          </el-icon>
        </el-tooltip>
      </div>
      <el-input
        v-model="searchKeyword"
        placeholder="搜索会话名称或消息内容"
        prefix-icon="Search"
        clearable
        size="default"
        class="search-input"
      />
    </header>

    <!-- 会话列表 -->
    <div class="conversation-list-scroll scroll-container">
      <!-- 置顶会话区域 -->
      <div v-if="pinnedConversations.length > 0" class="conversation-section">
        <div class="section-label">置顶会话</div>
        <div
          v-for="conversation in pinnedConversations"
          :key="conversation.id"
          class="conversation-item-wrapper"
          @contextmenu.prevent="handleContextMenu($event, conversation)"
        >
          <ConversationItem
            :conversation="conversation"
            :is-active="chatStore.currentConversation?.id === conversation.id"
            @select="handleSelectConversation"
            @delete="handleDeleteConversation"
            @pin="handlePinConversation"
          />
        </div>
      </div>

      <!-- 普通会话区域 -->
      <div v-if="unpinnedConversations.length > 0" class="conversation-section">
        <div v-if="pinnedConversations.length > 0" class="section-label">最近会话</div>
        <div
          v-for="conversation in unpinnedConversations"
          :key="conversation.id"
          class="conversation-item-wrapper"
          @contextmenu.prevent="handleContextMenu($event, conversation)"
        >
          <ConversationItem
            :conversation="conversation"
            :is-active="chatStore.currentConversation?.id === conversation.id"
            @select="handleSelectConversation"
            @delete="handleDeleteConversation"
            @pin="handlePinConversation"
          />
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredConversations.length === 0" class="empty-state">
        <el-icon :size="48" class="empty-icon"><ChatDotRound /></el-icon>
        <p class="empty-text">{{ searchKeyword ? '没有找到匹配的会话' : '暂无会话' }}</p>
        <el-button
          v-if="!searchKeyword"
          type="primary"
          size="small"
          @click="showNewChatDialog = true"
        >
          <el-icon><Plus /></el-icon>
          新建会话
        </el-button>
      </div>
    </div>

    <!-- 右键菜单 -->
    <ul
      v-if="contextMenu.visible"
      class="context-menu"
      :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
      @click.stop
    >
      <li class="menu-item" @click="handleTogglePin">
        <el-icon><Top /></el-icon>
        <span>{{ contextMenu.conversation?.isTop ? '取消置顶' : '置顶' }}</span>
      </li>
      <li class="menu-item" @click="handleMarkRead">
        <el-icon><Check /></el-icon>
        <span>标记已读</span>
      </li>
      <li class="menu-divider"></li>
      <li class="menu-item danger" @click="handleClearHistory">
        <el-icon><Delete /></el-icon>
        <span>清空聊天记录</span>
      </li>
      <li class="menu-item danger" @click="handleDeleteFromMenu">
        <el-icon><Close /></el-icon>
        <span>删除会话</span>
      </li>
    </ul>

    <!-- 新建会话对话框（复用现有逻辑，简化版） -->
    <el-dialog
      v-model="showNewChatDialog"
      title="新建会话"
      width="min(440px, 90vw)"
    >
      <div class="new-chat-content">
        <p class="dialog-tip">请输入对方用户ID或用户名发起会话：</p>
        <el-input
          v-model="newChatTarget"
          placeholder="输入用户ID"
          clearable
          @keyup.enter="confirmNewChat"
        />
      </div>
      <template #footer>
        <el-button @click="showNewChatDialog = false">取消</el-button>
        <el-button type="primary" :loading="creatingChat" @click="confirmNewChat">
          创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useChatStore } from '@/store/chat'
import { getOrCreatePrivateConversation, clearUnreadCount, setTop } from '@/api/conversation'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, Plus, Search, Top, Check, Delete, Close } from '@element-plus/icons-vue'
import ConversationItem from '@/components/chat/ConversationItem.vue'

const route = useRoute()
const chatStore = useChatStore()

const searchKeyword = ref('')
const showNewChatDialog = ref(false)
const newChatTarget = ref('')
const creatingChat = ref(false)

// 右键菜单状态
const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  conversation: null
})

// 页面标题（根据当前路由）
const pageTitle = computed(() => {
  const pathMap = {
    '/web/chat': '消息',
    '/web/contacts': '通讯录',
    '/web/favorites': '收藏',
    '/web/profile': '设置',
    '/chat': '消息',
    '/contacts': '通讯录',
    '/favorites': '收藏',
    '/profile': '设置'
  }
  return pathMap[route.path] || '消息'
})

// 过滤会话（同时匹配名称和最后消息内容）
const filteredConversations = computed(() => {
  if (!searchKeyword.value) return chatStore.conversations
  const kw = searchKeyword.value.toLowerCase()
  return chatStore.conversations.filter(item =>
    item.name?.toLowerCase().includes(kw) ||
    item.lastMessage?.toLowerCase().includes(kw) ||
    item.lastContent?.toLowerCase().includes(kw) ||
    item.preview?.toLowerCase().includes(kw)
  )
})

const pinnedConversations = computed(() => {
  return filteredConversations.value.filter(c => c.isTop)
})

const unpinnedConversations = computed(() => {
  return filteredConversations.value.filter(c => !c.isTop)
})

const handleSelectConversation = async (conversation) => {
  await chatStore.selectConversation(conversation)
}

const handleDeleteConversation = (conversation) => {
  ElMessageBox.confirm('确定要删除该会话吗？', '删除会话', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = chatStore.conversations.findIndex(c => c.id === conversation.id)
    if (index !== -1) {
      chatStore.conversations.splice(index, 1)
    }
    if (chatStore.currentConversation?.id === conversation.id) {
      chatStore.currentConversation = null
    }
    ElMessage.success('会话已删除')
  }).catch(() => {})
}

const handlePinConversation = ({ conversation, isTop }) => {
  const conv = chatStore.conversations.find(c => c.id === conversation.id)
  if (conv) {
    conv.isTop = isTop
  }
}

// ============ 右键菜单逻辑 ============
const handleContextMenu = (event, conversation) => {
  const menuWidth = 180
  const menuHeight = 200
  const x = Math.min(event.clientX, window.innerWidth - menuWidth - 10)
  const y = Math.min(event.clientY, window.innerHeight - menuHeight - 10)
  contextMenu.value = {
    visible: true,
    x,
    y,
    conversation
  }
}

const closeContextMenu = () => {
  contextMenu.value.visible = false
  contextMenu.value.conversation = null
}

const handleTogglePin = async () => {
  const conversation = contextMenu.value.conversation
  if (!conversation) return
  const newIsTop = !conversation.isTop
  try {
    const res = await setTop(conversation.id, newIsTop)
    if (res.code === 200) {
      conversation.isTop = newIsTop
      ElMessage.success(newIsTop ? '已置顶' : '已取消置顶')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    // 接口失败时仍然本地更新（保证可用性）
    conversation.isTop = newIsTop
    ElMessage.success(newIsTop ? '已置顶' : '已取消置顶')
  }
  closeContextMenu()
}

const handleMarkRead = async () => {
  const conversation = contextMenu.value.conversation
  if (!conversation) return
  try {
    const res = await clearUnreadCount(conversation.id)
    if (res.code === 200) {
      conversation.unreadCount = 0
      ElMessage.success('已标记为已读')
    } else {
      conversation.unreadCount = 0
      ElMessage.success('已标记为已读')
    }
  } catch (error) {
    conversation.unreadCount = 0
    ElMessage.success('已标记为已读')
  }
  closeContextMenu()
}

const handleClearHistory = () => {
  const conversation = contextMenu.value.conversation
  if (!conversation) return
  ElMessageBox.confirm('确定要清空该会话的聊天记录吗？此操作不可恢复。', '清空聊天记录', {
    confirmButtonText: '确定清空',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 清空当前会话的消息（如果选中）
    if (chatStore.currentConversation?.id === conversation.id) {
      chatStore.messages.splice(0, chatStore.messages.length)
    }
    // 清空会话列表中的最后消息预览
    conversation.lastMessage = ''
    conversation.lastContent = ''
    conversation.preview = ''
    conversation.lastMessageTime = null
    ElMessage.success('聊天记录已清空')
    closeContextMenu()
  }).catch(() => {
    closeContextMenu()
  })
}

const handleDeleteFromMenu = () => {
  const conversation = contextMenu.value.conversation
  if (!conversation) return
  closeContextMenu()
  handleDeleteConversation(conversation)
}

const handleDocumentClick = () => {
  if (contextMenu.value.visible) closeContextMenu()
}

onMounted(() => {
  document.addEventListener('click', handleDocumentClick)
  document.addEventListener('contextmenu', (e) => {
    // 点击非会话项时关闭菜单
    if (!e.target.closest('.conversation-item-wrapper')) {
      closeContextMenu()
    }
  })
})

onUnmounted(() => {
  document.removeEventListener('click', handleDocumentClick)
})

const confirmNewChat = async () => {
  const targetId = newChatTarget.value.trim()
  if (!targetId) {
    ElMessage.warning('请输入用户ID')
    return
  }

  creatingChat.value = true
  try {
    const res = await getOrCreatePrivateConversation(targetId)
    if (res.code === 200) {
      const conversation = res.data
      const exists = chatStore.conversations.find(c => c.id === conversation.id)
      if (!exists) chatStore.conversations.unshift(conversation)
      await chatStore.selectConversation(conversation)
      showNewChatDialog.value = false
      newChatTarget.value = ''
      ElMessage.success('会话已创建')
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (error) {
    console.error('创建会话失败:', error)
    ElMessage.error('创建会话失败')
  } finally {
    creatingChat.value = false
  }
}
</script>

<style lang="scss" scoped>
.web-conversation-panel-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

/* 顶部搜索区 */
.panel-header {
  flex-shrink: 0;
  padding: 16px 16px 12px;
  border-bottom: 1px solid var(--border-color, #e4e7ed);
  background: var(--bg-primary, #ffffff);
}

.panel-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary, #303133);
  margin: 0;
}

.add-btn {
  font-size: 18px;
  color: var(--text-secondary, #909399);
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  transition: all 0.15s;

  &:hover {
    color: #2b7fff;
    background: rgba(43, 127, 255, 0.08);
  }
}

.search-input {
  :deep(.el-input__wrapper) {
    background: var(--bg-secondary, #f5f7fa);
    box-shadow: none;
    border-radius: 8px;
  }
}

/* 会话列表区 */
.conversation-list-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 8px 8px 16px;
}

.conversation-section {
  margin-bottom: 8px;
}

.section-label {
  font-size: 12px;
  color: var(--text-placeholder, #c0c4cc);
  padding: 8px 12px 4px;
  font-weight: 500;
  user-select: none;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--text-placeholder, #c0c4cc);

  .empty-icon {
    margin-bottom: 12px;
    opacity: 0.4;
  }

  .empty-text {
    font-size: 14px;
    margin: 0 0 16px;
  }
}

/* 新建会话对话框 */
.new-chat-content {
  .dialog-tip {
    font-size: 13px;
    color: var(--text-secondary, #909399);
    margin: 0 0 12px;
  }
}

/* 会话项包裹层（用于右键菜单） */
.conversation-item-wrapper {
  position: relative;
}

/* 右键菜单 */
.context-menu {
  position: fixed;
  min-width: 180px;
  background: var(--bg-primary, #ffffff);
  border: 1px solid var(--border-color, #e4e7ed);
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  padding: 4px 0;
  z-index: 9999;
  list-style: none;
  margin: 0;
  user-select: none;

  .menu-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 16px;
    font-size: 14px;
    color: var(--text-primary, #303133);
    cursor: pointer;
    transition: background 0.15s;

    &:hover {
      background: var(--bg-hover, #f5f7fa);
    }

    &.danger {
      color: var(--danger, #f56c6c);
      &:hover {
        background: rgba(245, 108, 108, 0.08);
      }
    }
  }

  .menu-divider {
    height: 1px;
    background: var(--border-color, #e4e7ed);
    margin: 4px 0;
  }
}

/* 复用滚动容器样式 */
.scroll-container {
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: var(--border-color, #c0c4cc) transparent;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--border-color, #c0c4cc);
    border-radius: 3px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }
}
</style>