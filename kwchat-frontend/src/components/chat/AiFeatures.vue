<template>
  <div class="ai-features">
    <!-- 智能摘要 -->
    <el-dialog
      v-model="showSummary"
      title="智能摘要"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="summary-options">
        <el-radio-group v-model="summaryType">
          <el-radio-button value="brief">简要</el-radio-button>
          <el-radio-button value="detailed">详细</el-radio-button>
          <el-radio-button value="key_points">要点</el-radio-button>
        </el-radio-group>

        <el-input-number
          v-model="messageLimit"
          :min="10"
          :max="500"
          :step="10"
          size="small"
          placeholder="消息数量"
        />
      </div>

      <div class="summary-result" v-if="summaryResult">
        <el-divider />
        <div class="result-content" v-html="formatContent(summaryResult.content)"></div>
        <div class="result-meta">
          <span>处理时间: {{ summaryResult.processingTime }}ms</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="showSummary = false">取消</el-button>
        <el-button type="primary" @click="handleGenerateSummary" :loading="summaryLoading">
          生成摘要
        </el-button>
      </template>
    </el-dialog>

    <!-- 消息翻译 -->
    <el-dialog
      v-model="showTranslate"
      title="消息翻译"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="translate-form">
        <div class="translate-header">
          <el-select v-model="targetLanguage" placeholder="目标语言" style="width: 120px">
            <el-option
              v-for="lang in languages"
              :key="lang.code"
              :label="lang.name"
              :value="lang.code"
            />
          </el-select>
        </div>

        <div class="translate-content" v-if="translateResult">
          <el-divider />
          <div class="original-text">
            <div class="label">原文：</div>
            <div class="text">{{ translateResult.originalContent }}</div>
          </div>
          <div class="translated-text">
            <div class="label">译文：</div>
            <div class="text">{{ translateResult.content }}</div>
          </div>
          <div class="result-meta">
            <span>处理时间: {{ translateResult.processingTime }}ms</span>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showTranslate = false">取消</el-button>
        <el-button type="primary" @click="handleTranslate" :loading="translateLoading">
          翻译
        </el-button>
      </template>
    </el-dialog>

    <!-- 快捷操作菜单 -->
    <div class="quick-actions" v-if="showQuickActions">
      <div class="action-item" @click="openSummary">
        <el-icon><Document /></el-icon>
        <span>智能摘要</span>
      </div>
      <div class="action-item" @click="openTranslate">
        <el-icon><Translate /></el-icon>
        <span>翻译</span>
      </div>
      <div class="action-item" @click="getSuggestions">
        <el-icon><ChatDotRound /></el-icon>
        <span>智能回复</span>
      </div>
    </div>

    <!-- 智能回复建议 -->
    <div class="reply-suggestions" v-if="suggestions.length > 0">
      <div class="suggestions-header">
        <span>智能回复建议</span>
        <el-icon class="close-btn" @click="suggestions = []"><Close /></el-icon>
      </div>
      <div class="suggestions-list">
        <div
          v-for="(suggestion, index) in suggestions"
          :key="index"
          class="suggestion-item"
          @click="$emit('select-suggestion', suggestion)"
        >
          {{ suggestion }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { generateSummary, translateMessage, suggestReplies, supportedLanguages } from '@/api/ai'
import { ElMessage } from 'element-plus'

const props = defineProps({
  conversationId: {
    type: Number,
    default: null
  },
  messageId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['select-suggestion'])

// 语言列表
const languages = supportedLanguages

// 摘要相关
const showSummary = ref(false)
const summaryType = ref('brief')
const messageLimit = ref(100)
const summaryLoading = ref(false)
const summaryResult = ref(null)

// 翻译相关
const showTranslate = ref(false)
const targetLanguage = ref('en')
const translateLoading = ref(false)
const translateResult = ref(null)

// 智能回复
const suggestions = ref([])

// 快捷操作
const showQuickActions = ref(false)

// 打开摘要对话框
const openSummary = () => {
  if (!props.conversationId) {
    ElMessage.warning('请先选择会话')
    return
  }
  showSummary.value = true
  summaryResult.value = null
  showQuickActions.value = false
}

// 生成摘要
const handleGenerateSummary = async () => {
  summaryLoading.value = true
  try {
    const res = await generateSummary(props.conversationId, messageLimit.value, summaryType.value)
    if (res.code === 200) {
      summaryResult.value = res.data
    }
  } catch (error) {
    ElMessage.error('生成摘要失败')
  } finally {
    summaryLoading.value = false
  }
}

// 打开翻译对话框
const openTranslate = () => {
  if (!props.messageId) {
    ElMessage.warning('请先选择要翻译的消息')
    return
  }
  showTranslate.value = true
  translateResult.value = null
  showQuickActions.value = false
}

// 翻译消息
const handleTranslate = async () => {
  translateLoading.value = true
  try {
    const res = await translateMessage(props.messageId, targetLanguage.value)
    if (res.code === 200) {
      translateResult.value = res.data
    }
  } catch (error) {
    ElMessage.error('翻译失败')
  } finally {
    translateLoading.value = false
  }
}

// 获取智能回复建议
const getSuggestions = async () => {
  if (!props.conversationId) {
    ElMessage.warning('请先选择会话')
    return
  }

  try {
    const res = await suggestReplies(props.conversationId)
    if (res.code === 200) {
      suggestions.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('获取回复建议失败')
  }
  showQuickActions.value = false
}

// 格式化内容
const formatContent = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

// 切换快捷操作显示
const toggleQuickActions = () => {
  showQuickActions.value = !showQuickActions.value
}

// 暴露方法
defineExpose({
  toggleQuickActions,
  openSummary,
  openTranslate,
  getSuggestions
})
</script>

<style lang="scss" scoped>
.ai-features {
  position: relative;
}

.summary-options {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.summary-result {
  .result-content {
    font-size: 14px;
    line-height: 1.8;
    color: #333;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 4px;
    max-height: 300px;
    overflow-y: auto;
  }

  .result-meta {
    margin-top: 8px;
    font-size: 12px;
    color: #999;
    text-align: right;
  }
}

.translate-form {
  .translate-header {
    margin-bottom: 16px;
  }

  .translate-content {
    .original-text,
    .translated-text {
      margin-bottom: 12px;

      .label {
        font-size: 12px;
        color: #999;
        margin-bottom: 4px;
      }

      .text {
        font-size: 14px;
        line-height: 1.6;
        color: #333;
        padding: 8px 12px;
        background: #f5f7fa;
        border-radius: 4px;
      }
    }

    .translated-text .text {
      background: #e8f4ff;
    }

    .result-meta {
      font-size: 12px;
      color: #999;
      text-align: right;
    }
  }
}

.quick-actions {
  position: absolute;
  bottom: 100%;
  left: 0;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 8px 0;
  z-index: 100;
  min-width: 120px;

  .action-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: #f5f5f5;
    }

    .el-icon {
      font-size: 16px;
      color: #409eff;
    }

    span {
      font-size: 14px;
      color: #333;
    }
  }
}

.reply-suggestions {
  padding: 8px 16px;
  border-top: 1px solid #e8e8e8;
  background: #fff;

  .suggestions-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    span {
      font-size: 12px;
      color: #999;
    }

    .close-btn {
      cursor: pointer;
      color: #999;

      &:hover {
        color: #333;
      }
    }
  }

  .suggestions-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .suggestion-item {
    padding: 6px 12px;
    background: #f0f9ff;
    border: 1px solid #b3d8ff;
    border-radius: 16px;
    font-size: 13px;
    color: #409eff;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: #409eff;
      color: #fff;
    }
  }
}
</style>