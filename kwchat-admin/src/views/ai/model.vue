<template>
  <div class="ai-model-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>AI模型配置</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            添加模型
          </el-button>
        </div>
      </template>

      <el-table :data="modelList" v-loading="loading" stripe>
        <el-table-column prop="modelName" label="模型名称" width="150" />
        <el-table-column prop="provider" label="提供商" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.provider }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="modelId" label="模型标识" width="180" />
        <el-table-column prop="apiUrl" label="API地址" show-overflow-tooltip />
        <el-table-column prop="maxTokens" label="最大Token" width="100" />
        <el-table-column prop="temperature" label="温度" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              :active-value="1"
              :inactive-value="0"
              @change="handleToggleEnabled(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="默认" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault === 1" type="success">默认</el-tag>
            <el-button
              v-else
              type="primary"
              link
              @click="handleSetDefault(row)"
            >
              设为默认
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleTest(row)">
              测试
            </el-button>
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row)"
              :disabled="row.isDefault === 1"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑模型' : '添加模型'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="form.modelName" placeholder="请输入模型名称" />
        </el-form-item>

        <el-form-item label="提供商" prop="provider">
          <el-select v-model="form.provider" placeholder="请选择提供商" style="width: 100%">
            <el-option label="OpenAI" value="openai" />
            <el-option label="Anthropic" value="anthropic" />
            <el-option label="百度文心" value="baidu" />
            <el-option label="阿里通义" value="alibaba" />
            <el-option label="讯飞星火" value="xfyun" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>

        <el-form-item label="API地址" prop="apiUrl">
          <el-input v-model="form.apiUrl" placeholder="请输入API地址" />
        </el-form-item>

        <el-form-item label="API密钥" prop="apiKey">
          <el-input
            v-model="form.apiKey"
            type="password"
            placeholder="请输入API密钥"
            show-password
          />
        </el-form-item>

        <el-form-item label="模型标识" prop="modelId">
          <el-input v-model="form.modelId" placeholder="如：gpt-3.5-turbo" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最大Token">
              <el-input-number v-model="form.maxTokens" :min="1" :max="100000" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="温度">
              <el-slider v-model="form.temperature" :min="0" :max="2" :step="0.1" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  getAllAiModelConfigs,
  saveAiModelConfig,
  updateAiModelConfig,
  deleteAiModelConfig,
  setDefaultAiModel,
  testAiModelConnection
} from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const modelList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const form = ref({
  modelName: '',
  provider: '',
  apiUrl: '',
  apiKey: '',
  modelId: '',
  maxTokens: 4096,
  temperature: 0.7,
  remark: ''
})

const rules = {
  modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  provider: [{ required: true, message: '请选择提供商', trigger: 'change' }],
  apiUrl: [{ required: true, message: '请输入API地址', trigger: 'blur' }],
  apiKey: [{ required: true, message: '请输入API密钥', trigger: 'blur' }],
  modelId: [{ required: true, message: '请输入模型标识', trigger: 'blur' }]
}

// 获取模型列表
const fetchModels = async () => {
  loading.value = true
  try {
    const res = await getAllAiModelConfigs()
    if (res.code === 200) {
      modelList.value = res.data || []
    }
  } catch (error) {
    console.error('获取模型列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  editId.value = null
  form.value = {
    modelName: '',
    provider: '',
    apiUrl: '',
    apiKey: '',
    modelId: '',
    maxTokens: 4096,
    temperature: 0.7,
    remark: ''
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.value = { ...row }
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true

    if (isEdit.value) {
      await updateAiModelConfig(editId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await saveAiModelConfig(form.value)
      ElMessage.success('添加成功')
    }

    dialogVisible.value = false
    fetchModels()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该模型配置吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteAiModelConfig(row.id)
    ElMessage.success('删除成功')
    fetchModels()
  } catch {
    // 取消
  }
}

// 设置默认
const handleSetDefault = async (row) => {
  try {
    await setDefaultAiModel(row.id)
    ElMessage.success('设置成功')
    fetchModels()
  } catch (error) {
    console.error('设置失败:', error)
  }
}

// 切换启用状态
const handleToggleEnabled = async (row) => {
  try {
    await updateAiModelConfig(row.id, { enabled: row.enabled })
    ElMessage.success(row.enabled ? '已启用' : '已禁用')
  } catch (error) {
    row.enabled = row.enabled ? 0 : 1
    console.error('更新失败:', error)
  }
}

// 测试连接
const handleTest = async (row) => {
  try {
    const res = await testAiModelConnection(row.id)
    if (res.code === 200 && res.data) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error('连接测试失败')
    }
  } catch (error) {
    ElMessage.error('连接测试失败')
  }
}

onMounted(() => {
  fetchModels()
})
</script>

<style lang="scss" scoped>
.ai-model-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>