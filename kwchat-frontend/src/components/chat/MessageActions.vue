<template>
  <div class="message-actions">
    <!-- 消息右键菜单 -->
    <el-popover
      v-model:visible="showMenu"
      placement="bottom"
      :width="160"
      trigger="click"
    >
      <template #reference>
        <div class="action-trigger" @click.stop>
          <el-icon><MoreFilled /></el-icon>
        </div>
      </template>

      <div class="action-menu">
        <!-- 回复/引用 -->
        <div class="menu-item" @click="handleReply">
          <el-icon><ChatLineSquare /></el-icon>
          <span>回复</span>
        </div>

        <!-- 转发 -->
        <div class="menu-item" @click="handleForward">
          <el-icon><Share /></el-icon>
          <span>转发</span>
        </div>

        <!-- 复制 -->
        <div class="menu-item" @click="handleCopy" v-if="message.messageType === 1">
          <el-icon><CopyDocument /></el-icon>
          <span>复制</span>
        </div>

        <!-- 收藏 -->
        <div class="menu-item" @click="handleFavorite">
          <el-icon><Star /></el-icon>
          <span>收藏</span>
        </div>

        <!-- 多选 -->
        <div class="menu-item" @click="handleMultiSelect">
          <el-icon><Finished /></el-icon>
          <span>多选</span>
        </div>

        <!-- 撤回（只能撤回自己的消息） -->
        <div
          class="menu-item danger"
          @click="handleRecall"
          v-if="isSelf && canRecall"
        >
          <el-icon><Delete /></el-icon>
          <span>撤回</span>
        </div>

        <!-- 删除 -->
        <div class="menu-item danger" @click="handleDelete">
          <el-icon><Delete /></el-icon>
          <span>删除</span>
        </div>
      </div>
    </el-popover>

    <!-- 转发对话框 -->
    <el-dialog
      v-model="showForward"
      title="转发消息"
      width="400px"
    >
      <div class="forward-content">
        <div class="forward-preview">
          <div class="preview-label">消息内容：</div>
          <div class="preview-text">{{ getForwardPreview() }}</div>
        </div>

        <div class="forward-target">
          <div class="target-label">转发给：</div>
          <el-select
            v-model="forwardTarget"
            filterable
            placeholder="选择会话"
            style="width: 100%"
          >
            <el-option
              v-for="conversation in conversations"
              :key="conversation.id"
              :label="conversation.name"
              :value="conversation.id"
            >
              <div class="conversation-option">
                <el-avatar :size="24" :src="conversation.avatar">
                  {{ getAvatarFallback(conversation.name) }}
                </el-avatar>
                <span>{{ conversation.name }}</span>
              </div>
            </el-option>
          </el-select>
        </div>
      </div>

      <template #footer>
        <el-button @click="showForward = false">取消</el-button>
        <el-button type="primary" @click="confirmForward" :disabled="!forwardTarget">
          转发
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useChatStore } from '@/store/chat'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  message: {
    type: Object,
    required: true
  },
  isSelf: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['reply', 'forward', 'recall', 'delete', 'copy', 'favorite'])

const chatStore = useChatStore()
const showMenu = ref(false)
const showForward = ref(false)
const forwardTarget = ref(null)

// 获取头像 fallback 文字（显示最后两个字）
const getAvatarFallback = (name) => {
  if (!name) return '会话'
  return name.length >= 2 ? name.slice(-2) : name
}

// 会话列表
const conversations = computed(() => chatStore.conversations)

// 是否可以撤回（2分钟内）
const canRecall = computed(() => {
  if (!props.message.createTime) return false
  const timeDiff = Date.now() - new Date(props.message.createTime).getTime()
  return timeDiff < 2 * 60 * 1000
})

// 回复/引用消息
const handleReply = () => {
  showMenu.value = false
  emit('reply', props.message)
}

// 转发消息
const handleForward = () => {
  showMenu.value = false
  showForward.value = true
}

// 确认转发
const confirmForward = async () => {
  if (!forwardTarget.value) return

  try {
    await chatStore.sendMessage(
      forwardTarget.value,
      props.message.messageType,
      props.message.content,
      {
        fileUrl: props.message.fileUrl,
        fileName: props.message.fileName,
        fileSize: props.message.fileSize,
        fileType: props.message.fileType,
        isForward: true,
        originalMessageId: props.message.id
      }
    )

    showForward.value = false
    forwardTarget.value = null
    ElMessage.success('转发成功')
    emit('forward', { message: props.message, targetId: forwardTarget.value })
  } catch (error) {
    ElMessage.error('转发失败')
  }
}

// 获取转发预览
const getForwardPreview = () => {
  const { messageType, content, fileName } = props.message
  switch (messageType) {
    case 1: return content?.substring(0, 50) || ''
    case 2: return '[图片]'
    case 3: return `[文件] ${fileName || ''}`
    case 4: return '[视频]'
    case 5: return '[语音]'
    default: return content || ''
  }
}

// 复制消息
const handleCopy = () => {
  showMenu.value = false

  if (props.message.content) {
    navigator.clipboard.writeText(props.message.content)
      .then(() => {
        ElMessage.success('已复制到剪贴板')
        emit('copy', props.message)
      })
      .catch(() => {
        ElMessage.error('复制失败')
      })
  }
}

// 收藏消息
const handleFavorite = () => {
  showMenu.value = false
  ElMessage.success('已收藏')
  emit('favorite', props.message)
}

// 多选
const handleMultiSelect = () => {
  showMenu.value = false
  emit('multi-select', props.message)
}

// 撤回消息
const handleRecall = async () => {
  showMenu.value = false

  try {
    await ElMessageBox.confirm('确定要撤回这条消息吗？', '撤回消息', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    emit('recall', props.message)
  } catch {
    // 取消
  }
}

// 删除消息
const handleDelete = async () => {
  showMenu.value = false

  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '删除消息', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    emit('delete', props.message)
  } catch {
    // 取消
  }
}
</script>

<style lang="scss" scoped>
.action-trigger {
  opacity: 0;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover {
    background: #f0f0f0;
  }

  .el-icon {
    font-size: 16px;
    color: #666;
  }
}

.message-bubble-wrapper:hover .action-trigger {
  opacity: 1;
}

.action-menu {
  .menu-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    cursor: pointer;
    border-radius: 4px;
    transition: background 0.2s;

    &:hover {
      background: #f5f5f5;
    }

    .el-icon {
      font-size: 16px;
      color: #666;
    }

    span {
      font-size: 14px;
      color: #333;
    }

    &.danger {
      .el-icon,
      span {
        color: #f56c6c;
      }

      &:hover {
        background: #fef0f0;
      }
    }
  }
}

.forward-content {
  .forward-preview {
    margin-bottom: 16px;

    .preview-label {
      font-size: 12px;
      color: #999;
      margin-bottom: 4px;
    }

    .preview-text {
      padding: 8px 12px;
      background: #f5f7fa;
      border-radius: 4px;
      font-size: 14px;
      color: #333;
    }
  }

  .forward-target {
    .target-label {
      font-size: 12px;
      color: #999;
      margin-bottom: 4px;
    }
  }
}

.conversation-option {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>