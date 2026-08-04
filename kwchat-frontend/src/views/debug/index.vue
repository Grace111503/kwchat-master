<template>
  <div class="debug-container">
    <h2>网络调试工具</h2>

    <div class="info-card">
      <h3>环境信息</h3>
      <div class="info-item">
        <span class="label">平台类型:</span>
        <span class="value">{{ platformInfo.isCapacitor ? 'Capacitor App' : 'Web Browser' }}</span>
      </div>
      <div class="info-item">
        <span class="label">Android设备:</span>
        <span class="value">{{ platformInfo.isAndroid ? '是' : '否' }}</span>
      </div>
      <div class="info-item">
        <span class="label">User Agent:</span>
        <span class="value small">{{ platformInfo.userAgent }}</span>
      </div>
    </div>

    <div class="info-card">
      <h3>API配置</h3>
      <div class="info-item">
        <span class="label">API Base URL:</span>
        <span class="value">{{ apiBaseUrl }}</span>
      </div>
      <div class="info-item">
        <span class="label">WebSocket URL:</span>
        <span class="value">{{ wsUrl }}</span>
      </div>
    </div>

    <div class="info-card">
      <h3>网络测试</h3>
      <el-button type="primary" :loading="testing" @click="testConnection">
        测试连接
      </el-button>
      <div v-if="testResult" class="test-result" :class="testResult.success ? 'success' : 'error'">
        {{ testResult.message }}
      </div>
    </div>

    <div class="info-card">
      <h3>请求日志</h3>
      <div class="log-list">
        <div v-for="(log, index) in logs" :key="index" class="log-item" :class="log.type">
          <span class="time">{{ log.time }}</span>
          <span class="message">{{ log.message }}</span>
        </div>
        <div v-if="logs.length === 0" class="empty">暂无日志</div>
      </div>
      <el-button size="small" @click="clearLogs">清除日志</el-button>
    </div>

    <div class="back-link">
      <router-link to="/login">返回登录</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { isCapacitor, isAndroid, getApiBaseUrl, getWsUrl } from '@/utils/platform'
import axios from 'axios'

const apiBaseUrl = getApiBaseUrl()
const wsUrl = getWsUrl()

const platformInfo = reactive({
  isCapacitor: false,
  isAndroid: false,
  userAgent: ''
})

const testing = ref(false)
const testResult = ref(null)
const logs = ref([])

const addLog = (message, type = 'info') => {
  const now = new Date()
  const time = now.toLocaleTimeString()
  logs.value.unshift({ time, message, type })
  if (logs.value.length > 50) {
    logs.value.pop()
  }
  console.log(`[Debug ${type}]`, message)
}

const testConnection = async () => {
  testing.value = true
  testResult.value = null
  addLog('开始测试连接...')

  try {
    const testUrl = `${apiBaseUrl}/user/login`
    addLog(`测试URL: ${testUrl}`)

    const response = await axios.post(testUrl, {
      username: 'test',
      password: 'test'
    }, {
      timeout: 10000
    })

    addLog(`连接成功! 状态码: ${response.status}`, 'success')
    testResult.value = {
      success: true,
      message: `连接成功! 状态码: ${response.status}`
    }
  } catch (error) {
    const errorMsg = error.message || '未知错误'
    addLog(`连接失败: ${errorMsg}`, 'error')

    if (error.code === 'ECONNABORTED') {
      addLog('请求超时，服务器可能无响应', 'error')
    } else if (error.response) {
      addLog(`服务器响应: ${error.response.status}`, 'error')
    } else if (error.request) {
      addLog('无法连接到服务器，请检查网络', 'error')
    }

    testResult.value = {
      success: false,
      message: `连接失败: ${errorMsg}`
    }
  } finally {
    testing.value = false
  }
}

const clearLogs = () => {
  logs.value = []
}

onMounted(() => {
  platformInfo.isCapacitor = isCapacitor()
  platformInfo.isAndroid = isAndroid()
  platformInfo.userAgent = navigator.userAgent

  addLog('调试页面已加载')
  addLog(`API地址: ${apiBaseUrl}`)
  addLog(`WebSocket地址: ${wsUrl}`)
})
</script>

<style lang="scss" scoped>
.debug-container {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;

  h2 {
    margin-bottom: 20px;
    color: #333;
  }
}

.info-card {
  background: #f5f5f5;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;

  h3 {
    margin: 0 0 12px 0;
    font-size: 16px;
    color: #666;
  }
}

.info-item {
  display: flex;
  margin-bottom: 8px;

  .label {
    width: 120px;
    color: #999;
    flex-shrink: 0;
  }

  .value {
    color: #333;
    word-break: break-all;

    &.small {
      font-size: 12px;
    }
  }
}

.test-result {
  margin-top: 12px;
  padding: 12px;
  border-radius: 4px;

  &.success {
    background: #e6f7e6;
    color: #52c41a;
  }

  &.error {
    background: #fff2f0;
    color: #ff4d4f;
  }
}

.log-list {
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 12px;
}

.log-item {
  padding: 4px 0;
  font-size: 12px;
  border-bottom: 1px solid #eee;

  .time {
    color: #999;
    margin-right: 8px;
  }

  &.error .message {
    color: #ff4d4f;
  }

  &.success .message {
    color: #52c41a;
  }
}

.empty {
  text-align: center;
  color: #999;
  padding: 20px;
}

.back-link {
  text-align: center;
  margin-top: 20px;

  a {
    color: #2b7fff;
    text-decoration: none;
  }
}
</style>
