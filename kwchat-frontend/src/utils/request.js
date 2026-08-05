import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'
import router from '@/router'
import { getApiBaseUrl, isCapacitor, isAndroid } from '@/utils/platform'

// 获取 API 基础地址
const baseURL = getApiBaseUrl()

// 打印环境信息
console.log('=============================')
console.log('[API] 环境信息:')
console.log('[API] isCapacitor:', isCapacitor())
console.log('[API] isAndroid:', isAndroid())
console.log('[API] baseURL:', baseURL)
console.log('[API] full login URL:', baseURL + '/user/login')
console.log('[API] window.Capacitor:', typeof window !== 'undefined' ? !!window.Capacitor : 'N/A')
console.log('[API] location.protocol:', typeof window !== 'undefined' ? window.location.protocol : 'N/A')
console.log('[API] location.hostname:', typeof window !== 'undefined' ? window.location.hostname : 'N/A')
console.log('[API] userAgent:', typeof navigator !== 'undefined' ? navigator.userAgent : 'N/A')
console.log('=============================')

// 创建axios实例
const service = axios.create({
  baseURL,
  timeout: 30000, // 30秒超时，适应移动端网络环境
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    console.log('[API 请求]', config.method?.toUpperCase(), config.baseURL + config.url)
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data

    // 如果是文件下载，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }

    // 状态码判断
    if (res.code === 200) {
      return res
    } else if (res.code === 1001 || res.code === 1003 || res.code === 1004) {
      // Token相关错误，跳转登录页
      ElMessageBox.confirm(
        '登录状态已过期，请重新登录',
        '系统提示',
        {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        removeToken()
        router.push({ name: 'Login' })
      })
      return Promise.reject(new Error(res.message || '认证失败'))
    } else {
      // 某些业务错误不自动弹出提示，由调用方处理
      const silentCodes = [3004] // 已发送过好友请求
      if (!silentCodes.includes(res.code)) {
        ElMessage.error(res.message || '请求失败')
      }
      const error = new Error(res.message || '请求失败')
      error.code = res.code
      return Promise.reject(error)
    }
  },
  error => {
    console.error('=== 响应错误 ===')
    console.error('error.message:', error.message)
    console.error('error.code:', error.code)
    console.error('error.config:', JSON.stringify(error.config, null, 2))
    console.error('error.response:', error.response ? JSON.stringify({ status: error.response.status, data: error.response.data }, null, 2) : 'undefined')
    console.error('error.request:', error.request ? 'exists' : 'undefined')

    let message = '网络错误，请稍后重试'

    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          message = '未授权，请登录'
          removeToken()
          router.push({ name: 'Login' })
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 408:
          message = '请求超时'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 501:
          message = '服务未实现'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务不可用'
          break
        case 504:
          message = '网关超时'
          break
        default:
          message = `连接错误${error.response.status}`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时'
    }

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service