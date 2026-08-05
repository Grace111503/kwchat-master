import { ref, reactive } from 'vue'
import { getToken } from './auth'
import { ElNotification } from 'element-plus'
import { getClientType, isCapacitor } from './platform'

/**
 * WebSocket管理类
 */
class WebSocketManager {
  constructor() {
    this.ws = null
    this.url = ''
    this.isConnected = ref(false)
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 10
    this.reconnectInterval = 3000
    this.heartbeatInterval = 30000
    this.heartbeatTimer = null
    this.reconnectTimer = null
    this.listeners = new Map()
    this.messageQueue = []
    this.isInBackground = false

    // 监听页面可见性变化（Capacitor Android 后台/前台切换）
    this._setupVisibilityListener()
  }

  /**
   * 设置页面可见性监听器
   */
  _setupVisibilityListener() {
    // 监听 document visibilitychange（标准浏览器）
    document.addEventListener('visibilitychange', () => {
      if (document.hidden) {
        this.isInBackground = true
        console.log('WebSocket: 应用进入后台')
      } else {
        this.isInBackground = false
        console.log('WebSocket: 应用回到前台')
        this._handleForegroundResume()
      }
    })

    // 监听 Capacitor appStateChange（更可靠）
    this._setupCapacitorListeners()
  }

  /**
   * 设置 Capacitor 生命周期监听
   */
  async _setupCapacitorListeners() {
    try {
      const { App } = await import('@capacitor/app')
      App.addListener('appStateChange', ({ isActive }) => {
        if (isActive) {
          console.log('WebSocket: Capacitor 应用回到前台')
          this.isInBackground = false
          this._handleForegroundResume()
        } else {
          console.log('WebSocket: Capacitor 应用进入后台')
          this.isInBackground = true
        }
      })
    } catch (e) {
      // 非 Capacitor 环境，忽略
      console.log('WebSocket: 非 Capacitor 环境，使用 visibilitychange 监听')
    }
  }

  /**
   * 应用回到前台时的处理
   */
  _handleForegroundResume() {
    // 重置重连次数
    this.reconnectAttempts = 0

    // 检查 WebSocket 连接状态
    if (!this.ws || this.ws.readyState === WebSocket.CLOSED || this.ws.readyState === WebSocket.CLOSING) {
      console.log('WebSocket: 连接已断开，重新连接...')
      this.connect()
    } else if (this.ws.readyState === WebSocket.OPEN) {
      // 连接仍然打开，发送心跳验证
      console.log('WebSocket: 连接仍打开，发送心跳验证')
      this.sendHeartbeat()
    }
  }

  /**
   * 连接WebSocket
   */
  connect(url) {
    if (this.ws && (this.ws.readyState === WebSocket.CONNECTING || this.ws.readyState === WebSocket.OPEN)) {
      console.log('WebSocket已连接')
      return
    }

    this.url = url || this.url
    const token = getToken()
    if (!token) {
      console.error('未找到Token，无法连接WebSocket')
      return
    }

    // 构建WebSocket URL
    const wsUrl = `${this.url}?token=${token}`
    console.log('正在连接WebSocket:', wsUrl)

    try {
      this.ws = new WebSocket(wsUrl)
      this.setupEventHandlers()
    } catch (error) {
      console.error('WebSocket连接失败:', error)
      this.scheduleReconnect()
    }
  }

  /**
   * 设置事件处理器
   */
  setupEventHandlers() {
    // 连接成功
    this.ws.onopen = () => {
      console.log('WebSocket连接成功')
      this.isConnected.value = true
      this.reconnectAttempts = 0

      // 发送认证消息
      this.sendAuth()

      // 启动心跳
      this.startHeartbeat()

      // 发送队列中的消息
      this.flushMessageQueue()

      // 触发连接成功回调
      this.emit('connect')
    }

    // 接收消息
    this.ws.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data)
        this.handleMessage(message)
      } catch (error) {
        console.error('解析WebSocket消息失败:', error)
      }
    }

    // 连接关闭
    this.ws.onclose = (event) => {
      console.log('WebSocket连接关闭:', event.code, event.reason)
      this.isConnected.value = false
      this.stopHeartbeat()

      // 触发断开连接回调
      this.emit('disconnect', event)

      // 尝试重连
      if (event.code !== 1000) {
        this.scheduleReconnect()
      }
    }

    // 连接错误
    this.ws.onerror = (error) => {
      console.error('WebSocket错误:', error)
      this.emit('error', error)
    }
  }

  /**
   * 处理消息
   */
  handleMessage(message) {
    const { type, data, senderId, receiverId, conversationId } = message

    console.log('收到WebSocket消息:', type, message)

    switch (type) {
      case 'auth_success':
        console.log('WebSocket认证成功')
        this.emit('auth_success', data)
        break

      case 'heartbeat':
        // 心跳响应，不做处理
        break

      case 'new_message':
        // 新消息
        this.emit('new_message', data)
        this.showMessageNotification(data)
        break

      case 'read_receipt':
        // 已读回执
        this.emit('read_receipt', data)
        break

      case 'friend_request':
        // 好友申请
        this.emit('friend_request', data)
        this.showFriendRequestNotification(data)
        break

      case 'friend_request_handle':
        // 好友申请处理
        this.emit('friend_request_handle', data)
        break

      case 'typing':
        // 正在输入
        this.emit('typing', { senderId, conversationId })
        break

      case 'error':
        console.error('WebSocket错误消息:', data)
        this.emit('error', data)
        break

      default:
        console.warn('未知的WebSocket消息类型:', type)
    }
  }

  /**
   * 发送认证消息
   */
  sendAuth() {
    const token = getToken()
    this.send({
      type: 'auth',
      data: {
        token,
        clientType: getClientType()
      }
    })
  }

  /**
   * 发送消息
   */
  send(message) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message))
    } else {
      console.log('WebSocket未连接，消息加入队列')
      this.messageQueue.push(message)
    }
  }

  /**
   * 发送聊天消息
   */
  sendMessage(conversationId, receiverId, messageType, content, extra = {}) {
    this.send({
      type: 'message',
      data: {
        conversationId,
        messageType,
        content,
        ...extra
      },
      receiverId,
      conversationId
    })
  }

  /**
   * 发送已读回执
   */
  sendReadReceipt(messageId, conversationId, receiverId) {
    this.send({
      type: 'read',
      data: { messageId },
      receiverId,
      conversationId
    })
  }

  /**
   * 发送正在输入状态
   */
  sendTyping(conversationId, receiverId) {
    this.send({
      type: 'typing',
      receiverId,
      conversationId
    })
  }

  /**
   * 发送心跳
   */
  sendHeartbeat() {
    this.send({
      type: 'heartbeat',
      timestamp: Date.now()
    })
  }

  /**
   * 启动心跳
   */
  startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatTimer = setInterval(() => {
      // 后台时暂停心跳，节省电量
      if (this.isInBackground) {
        console.log('WebSocket: 后台模式，暂停心跳')
        return
      }
      this.sendHeartbeat()
    }, this.heartbeatInterval)
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 计划重连
   */
  scheduleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.log('WebSocket重连次数超限')
      this.emit('reconnect_failed')
      return
    }

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
    }

    this.reconnectAttempts++
    const delay = this.reconnectInterval * Math.pow(2, this.reconnectAttempts - 1)
    console.log(`WebSocket将在${delay}ms后重连 (第${this.reconnectAttempts}次)`)

    this.reconnectTimer = setTimeout(() => {
      this.connect()
    }, delay)
  }

  /**
   * 断开连接
   */
  disconnect() {
    this.stopHeartbeat()

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    if (this.ws) {
      // 先移除事件处理器，避免断开时触发重连
      this.ws.onclose = null
      this.ws.onerror = null
      this.ws.close(1000, '用户断开连接')
      this.ws = null
    }

    this.isConnected.value = false
    this.reconnectAttempts = 0
  }

  /**
   * 发送队列中的消息
   */
  flushMessageQueue() {
    while (this.messageQueue.length > 0) {
      const message = this.messageQueue.shift()
      this.send(message)
    }
  }

  /**
   * 注册事件监听器
   */
  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(callback)
  }

  /**
   * 移除事件监听器
   */
  off(event, callback) {
    if (this.listeners.has(event)) {
      const callbacks = this.listeners.get(event)
      const index = callbacks.indexOf(callback)
      if (index > -1) {
        callbacks.splice(index, 1)
      }
    }
  }

  /**
   * 触发事件
   */
  emit(event, data) {
    if (this.listeners.has(event)) {
      const callbacks = this.listeners.get(event)
      callbacks.forEach(callback => {
        try {
          callback(data)
        } catch (error) {
          console.error(`事件处理错误 [${event}]:`, error)
        }
      })
    }
  }

  /**
   * 显示新消息通知
   */
  showMessageNotification(message) {
    // 如果页面不在前台，显示通知
    if (document.hidden) {
      ElNotification({
        title: message.senderName || '新消息',
        message: this.getNotificationContent(message),
        type: 'info',
        duration: 3000,
        onClick: () => {
          window.focus()
          this.emit('notification_click', message)
        }
      })
    }
  }

  /**
   * 显示好友申请通知
   */
  showFriendRequestNotification(data) {
    ElNotification({
      title: '好友申请',
      message: `${data.senderName || '有人'}请求添加您为好友`,
      type: 'info',
      duration: 5000
    })
  }

  /**
   * 获取通知内容
   */
  getNotificationContent(message) {
    const { messageType, content, fileName } = message
    switch (messageType) {
      case 1: return content
      case 2: return '[图片]'
      case 3: return `[文件] ${fileName}`
      case 4: return '[视频]'
      case 5: return '[语音]'
      default: return content || '新消息'
    }
  }

  /**
   * 获取连接状态
   */
  getConnectionState() {
    return this.isConnected.value
  }
}

// 创建单例
const websocketManager = new WebSocketManager()

export default websocketManager
