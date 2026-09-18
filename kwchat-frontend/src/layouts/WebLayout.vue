<template>
  <!--
    Web 端三栏布局骨架
    布局结构：
    ┌──────────────────────────────────────────────────────────┐
    │  左栏 (WebSidebar)  │  中栏 (ConversationPanel)  │  右栏 (ChatArea)  │
    │  240px              │  320px                    │  自适应           │
    └──────────────────────────────────────────────────────────┘
    
    响应式：
    - ≥ 1024px：三栏布局
    - 768px ~ 1023px：两栏布局（左栏 + 右栏，会话列表内嵌）
    - < 768px：单栏布局（兼容移动端）
    
    当前为骨架版本，子组件将在后续步骤中逐步创建并接入
  -->
  <div class="web-layout">
    <!-- 左栏：导航区 -->
    <aside class="web-sidebar">
      <WebSidebar />
    </aside>

    <!-- 中栏：会话列表区 -->
    <aside class="web-conversation-panel">
      <WebConversationPanel />
    </aside>

    <!-- 右栏：聊天主区域（内部已含 header / messages / input / empty 状态） -->
    <main class="web-chat-area">
      <WebChatArea />
    </main>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, defineAsyncComponent } from 'vue'

// 异步加载 Web 端三大组件（构建产物隔离：仅 Web 环境打包）
const WebSidebar = defineAsyncComponent(() => import('@/components/web/WebSidebar.vue'))
const WebConversationPanel = defineAsyncComponent(() => import('@/components/web/WebConversationPanel.vue'))
const WebChatArea = defineAsyncComponent(() => import('@/components/web/WebChatArea.vue'))

onMounted(() => {
  console.log('[WebLayout] mounted, Web 端三栏布局已就绪')
})

onUnmounted(() => {
  console.log('[WebLayout] unmounted')
})
</script>

<style lang="scss" scoped>
/* ========== 布局容器 ========== */
.web-layout {
  display: grid;
  grid-template-columns: 240px 320px 1fr;
  grid-template-rows: 100vh;
  height: 100vh;
  overflow: hidden;
  background: var(--bg-secondary, #f5f7fa);
}

/* ========== 左栏 ========== */
.web-sidebar {
  background: var(--bg-primary, #ffffff);
  border-right: 1px solid var(--border-color, #e4e7ed);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--text-placeholder, #c0c4cc);
  font-size: 14px;

  small {
    font-size: 12px;
    color: var(--text-placeholder, #c0c4cc);
  }
}

/* ========== 中栏 ========== */
.web-conversation-panel {
  background: var(--bg-secondary, #f5f7fa);
  border-right: 1px solid var(--border-color, #e4e7ed);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ========== 右栏：聊天主区域 ========== */
.web-chat-area {
  display: flex;
  flex-direction: column;
  background: var(--bg-primary, #ffffff);
  overflow: hidden;
}

/* ========== 响应式：768px ~ 1023px（两栏布局） ========== */
@media (max-width: 1023px) {
  .web-layout {
    grid-template-columns: 72px 320px 1fr;
  }
}

/* ========== 响应式：< 768px（单栏/移动端兼容） ========== */
@media (max-width: 767px) {
  .web-layout {
    grid-template-columns: 1fr;
    grid-template-rows: 56px 1fr;
  }

  .web-sidebar {
    display: none; /* 移动端隐藏侧栏，复用底部导航 */
  }

  .web-conversation-panel {
    display: none; /* 移动端隐藏会话列表面板 */
  }

  .chat-header {
    height: 48px;
    min-height: 48px;
    padding: 0 16px;
  }

  .chat-messages {
    padding: 12px 16px;
  }

  .chat-footer {
    min-height: 100px;
  }
}
</style>