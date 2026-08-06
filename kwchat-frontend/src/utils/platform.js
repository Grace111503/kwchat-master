/**
 * 平台检测工具
 * 用于检测运行环境（Web、Capacitor、Android、iOS）
 */

// 缓存检测结果，避免重复计算
let _isCapacitor = null
let _isAndroid = null
let _isiOS = null
let _isMobile = null

/**
 * 检测是否在 Capacitor 环境中
 */
export function isCapacitor() {
  if (_isCapacitor !== null) return _isCapacitor

  console.log('[Platform] Detecting Capacitor environment...')

  // 方法1: 检查 Capacitor 是否存在
  if (typeof window !== 'undefined' && window.Capacitor) {
    console.log('[Platform] Method 1: window.Capacitor exists')
    _isCapacitor = true
    return true
  }

  // 方法2: 检查 URL 协议（兼容不同配置）
  if (typeof window !== 'undefined') {
    const protocol = window.location.protocol
    const hostname = window.location.hostname
    const userAgent = navigator.userAgent

    console.log('[Platform] Protocol:', protocol, 'Hostname:', hostname)
    console.log('[Platform] UserAgent:', userAgent)

    // capacitor:// 协议
    if (protocol === 'capacitor:') {
      console.log('[Platform] Method 2a: capacitor:// protocol detected')
      _isCapacitor = true
      return true
    }

    // https 协议 + localhost（Capacitor 的 androidScheme: "https" 配置）
    if (protocol === 'https:' && hostname === 'localhost') {
      console.log('[Platform] Method 2b: https://localhost detected')
      // 额外检查：是否在 Capacitor 的 WebView 中
      // Capacitor WebView 通常有特定的 user agent
      const lowerUA = userAgent.toLowerCase()
      if (lowerUA.includes('capacitor') || lowerUA.includes('wv')) {
        console.log('[Platform] Method 2b: Capacitor/WV user agent detected')
        _isCapacitor = true
        return true
      }
    }
  }

  // 方法3: 检查其他原生桥接标志（兼容不同 Capacitor 版本）
  if (typeof window !== 'undefined') {
    if (window.Android || window.webkit?.messageHandlers || window.cordova) {
      console.log('[Platform] Method 3: Native bridge detected')
      _isCapacitor = true
      return true
    }
  }

  console.log('[Platform] Capacitor not detected')
  _isCapacitor = false
  return false
}

/**
 * 检测是否是 Android 设备
 */
export function isAndroid() {
  if (_isAndroid !== null) return _isAndroid
  _isAndroid = /Android/i.test(navigator.userAgent)
  return _isAndroid
}

/**
 * 检测是否是 iOS 设备
 */
export function isIOS() {
  if (_isiOS !== null) return _isiOS
  _isiOS = /iPhone|iPad|iPod/i.test(navigator.userAgent) ||
    (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1) // iPad on iOS 13+
  return _isiOS
}

/**
 * 检测是否是移动设备
 */
export function isMobile() {
  if (_isMobile !== null) return _isMobile
  _isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
  return _isMobile
}

/**
 * 获取 API 基础地址
 */
export function getApiBaseUrl() {
  const isCapacitorEnv = isCapacitor()
  console.log('[Platform] isCapacitor:', isCapacitorEnv)

  if (isCapacitorEnv) {
    // Capacitor 环境，直连后端 HTTP 端口
    const apiUrl = import.meta.env.VITE_API_SERVER_URL
    console.log('[Platform] VITE_API_SERVER_URL:', apiUrl)
    // 优先使用环境变量，否则使用默认值
    const baseUrl = apiUrl || 'http://118.25.44.250:8080/api'
    console.log('[Platform] Using API baseUrl:', baseUrl)
    return baseUrl
  }
  // Web 环境，通过代理
  console.log('[Platform] Using /api (proxy)')
  return '/api'
}

/**
 * 获取 WebSocket URL
 */
export function getWsUrl() {
  if (isCapacitor()) {
    // Capacitor 环境，直连后端 WebSocket 端口
    return import.meta.env.VITE_WS_SERVER_URL || 'ws://118.25.44.250:9092/ws'
  }
  // Web 环境
  return `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws`
}

/**
 * 获取客户端类型标识
 */
export function getClientType() {
  if (isCapacitor()) {
    return isAndroid() ? 'android' : isIOS() ? 'ios' : 'capacitor'
  }
  return 'web'
}

/**
 * 检测是否支持触摸事件
 */
export function supportsTouch() {
  return 'ontouchstart' in window || navigator.maxTouchPoints > 0
}

/**
 * 获取安全区域信息
 */
export function getSafeAreaInsets() {
  if (typeof window === 'undefined') {
    return { top: 0, bottom: 0, left: 0, right: 0 }
  }

  // 尝试从 CSS 环境变量获取
  const style = getComputedStyle(document.documentElement)

  const top = parseInt(style.getPropertyValue('env(safe-area-inset-top)')) ||
    parseInt(style.getPropertyValue('--sat')) || 0

  const bottom = parseInt(style.getPropertyValue('env(safe-area-inset-bottom)')) ||
    parseInt(style.getPropertyValue('--sab')) || 0

  const left = parseInt(style.getPropertyValue('env(safe-area-inset-left)')) ||
    parseInt(style.getPropertyValue('--sal')) || 0

  const right = parseInt(style.getPropertyValue('env(safe-area-inset-right)')) ||
    parseInt(style.getPropertyValue('--sar')) || 0

  return { top, bottom, left, right }
}

/**
 * 判断是否应该使用原生导航
 * 在 Capacitor iOS 中，可以使用原生返回手势
 */
export function useNativeNavigation() {
  return isCapacitor() && isIOS()
}

/**
 * 获取应用版本号
 */
export function getAppVersion() {
  return typeof __APP_VERSION__ !== 'undefined' ? __APP_VERSION__ : '1.0.0'
}

/**
 * 获取完整的文件URL
 * 在 Capacitor 环境下，将相对路径拼接上服务器地址
 * @param {string} url - 文件路径（可能是相对路径或完整URL）
 * @returns {string} 完整的文件URL
 */
export function getFullFileUrl(url) {
  if (!url) return ''

  // 如果已经是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }

  // 获取服务器地址
  const getServerUrl = () => {
    if (isCapacitor()) {
      const apiUrl = import.meta.env.VITE_API_SERVER_URL || 'http://118.25.44.250:8080/api'
      // 确保以 /api 结尾
      return apiUrl.endsWith('/api') ? apiUrl : apiUrl + '/api'
    }
    return ''
  }

  // 如果是相对路径（以/开头）
  if (url.startsWith('/')) {
    if (isCapacitor()) {
      // 头像文件使用 /api/file/avatar/ 端点（保持原有逻辑）
      if (url.startsWith('/uploads/avatar/')) {
        const fileName = url.split('/').pop()
        return getServerUrl() + '/file/avatar/' + fileName
      }
      // 图片文件使用 /api/file/image/ 端点
      if (url.startsWith('/uploads/image/')) {
        const fileName = url.split('/').pop()
        return getServerUrl() + '/file/image/' + fileName
      }
      // 视频文件使用 /api/file/video/ 端点
      if (url.startsWith('/uploads/video/')) {
        const fileName = url.split('/').pop()
        return getServerUrl() + '/file/video/' + fileName
      }
      // 语音文件使用 /api/file/voice/ 端点
      if (url.startsWith('/uploads/voice/')) {
        const fileName = url.split('/').pop()
        return getServerUrl() + '/file/voice/' + fileName
      }
      // 其他文件使用 /api/file/document/ 端点
      if (url.startsWith('/uploads/file/')) {
        const fileName = url.split('/').pop()
        return getServerUrl() + '/file/document/' + fileName
      }
      // 其他文件使用静态资源路径
      return getServerUrl() + url
    }
    return url
  }

  // 如果是旧的MinIO格式，转换为本地路径
  const minioPattern = /https?:\/\/[^/]+:\d+\/[^/]+\/([^?]+)/
  const match = url.match(minioPattern)
  if (match) {
    const path = match[1]
    return getFullFileUrl('/uploads/' + path)
  }

  // 其他情况直接返回
  return url
}
