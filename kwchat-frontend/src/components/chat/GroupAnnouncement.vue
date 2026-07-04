<template>
  <div class="group-announcement">
    <!-- 公告显示 -->
    <div class="announcement-bar" v-if="announcement && !showEditor" @click="showDetail = true">
      <el-icon><Bell /></el-icon>
      <span class="announcement-text">{{ announcement }}</span>
      <el-icon class="arrow"><ArrowRight /></el-icon>
    </div>

    <!-- 公告详情 -->
    <el-dialog
      v-model="showDetail"
      title="群公告"
      width="500px"
    >
      <div class="announcement-detail">
        <div class="detail-content">{{ announcement }}</div>
        <div class="detail-meta">
          <span>发布者：{{ announcer }}</span>
          <span>发布时间：{{ formatTime(announcementTime) }}</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
        <el-button type="primary" @click="showEditor = true" v-if="canEdit">
          编辑公告
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑公告 -->
    <el-dialog
      v-model="showEditor"
      title="编辑群公告"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="editor-content">
        <el-input
          v-model="editContent"
          type="textarea"
          :rows="6"
          placeholder="请输入群公告内容..."
          maxlength="500"
          show-word-limit
        />

        <div class="editor-tips">
          <el-icon><InfoFilled /></el-icon>
          <span>公告内容将发送给所有群成员</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="showEditor = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          发布公告
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const props = defineProps({
  conversationId: {
    type: Number,
    required: true
  },
  announcement: {
    type: String,
    default: ''
  },
  announcer: {
    type: String,
    default: ''
  },
  announcementTime: {
    type: [String, Date],
    default: null
  },
  canEdit: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['save'])

const showDetail = ref(false)
const showEditor = ref(false)
const editContent = ref(props.announcement)
const saving = ref(false)

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

// 保存公告
const handleSave = async () => {
  if (!editContent.value.trim()) {
    ElMessage.warning('请输入公告内容')
    return
  }

  saving.value = true
  try {
    emit('save', editContent.value.trim())
    showEditor.value = false
    ElMessage.success('公告发布成功')
  } catch (error) {
    ElMessage.error('公告发布失败')
  } finally {
    saving.value = false
  }
}

// 监听公告变化
watch(() => props.announcement, (newVal) => {
  editContent.value = newVal
})
</script>

<style lang="scss" scoped>
.announcement-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #fef6e6;
  border-bottom: 1px solid #f5d99e;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #fdecc8;
  }

  .el-icon {
    color: #e6a23c;
    flex-shrink: 0;
  }

  .announcement-text {
    flex: 1;
    font-size: 13px;
    color: #333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .arrow {
    color: #999;
  }
}

.announcement-detail {
  .detail-content {
    font-size: 14px;
    line-height: 1.8;
    color: #333;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 4px;
    min-height: 100px;
    white-space: pre-wrap;
  }

  .detail-meta {
    display: flex;
    gap: 16px;
    margin-top: 12px;
    font-size: 12px;
    color: #999;
  }
}

.editor-content {
  .editor-tips {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-top: 8px;
    font-size: 12px;
    color: #909399;
  }
}
</style>