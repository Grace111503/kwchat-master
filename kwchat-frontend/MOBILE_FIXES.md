# 移动端兼容性修复说明

## 问题概述

您的 kwchat 项目在网页端正常运行，但在移动端（Capacitor Android/iOS）出现各种问题。主要原因是：

1. **Capacitor 环境检测不准确**
2. **API/WebSocket URL 硬编码**
3. **localStorage 在 Capacitor 中不可靠**
4. **键盘处理配置不当**
5. **CSS 安全区域处理不完整**
6. **移动端触摸交互优化不足**

## 修复内容

### 1. 新增平台检测工具 (`src/utils/platform.js`)

**修复问题**: Capacitor 环境检测不准确

```javascript
// 新的检测逻辑，更可靠
export function isCapacitor() {
  // 方法1: 检查 Capacitor 是否存在
  if (window.Capacitor) return true

  // 方法2: 检查 URL 协议
  if (protocol === 'capacitor:') return true

  // 方法3: https + localhost + Capacitor WebView
  if (protocol === 'https:' && hostname === 'localhost') {
    const userAgent = navigator.userAgent.toLowerCase()
    if (userAgent.includes('capacitor') || userAgent.includes('wv')) {
      return true
    }
  }

  return false
}
```

**新增功能**:
- `isCapacitor()` - 检测是否在 Capacitor 环境
- `isAndroid()` - 检测是否是 Android 设备
- `isIOS()` - 检测是否是 iOS 设备
- `isMobile()` - 检测是否是移动设备
- `getApiBaseUrl()` - 获取 API 基础地址
- `getWsUrl()` - 获取 WebSocket URL
- `getClientType()` - 获取客户端类型标识

### 2. 修复 Token 存储 (`src/utils/auth.js`)

**修复问题**: localStorage 在 Capacitor 中不可靠

```javascript
// 现在支持 Capacitor Preferences
export async function setToken(token) {
  // 始终保存到 localStorage（作为备份）
  localStorage.setItem(TokenKey, token)

  // 在 Capacitor 中使用 Preferences
  if (isCapacitor()) {
    const { Preferences } = await import('@capacitor/preferences')
    await Preferences.set({ key: TokenKey, value: token })
  }
}
```

**重要**: Token 相关函数现在是异步的，需要使用 `await`：

```javascript
// 旧代码
const token = getToken()

// 新代码
const token = await getToken()
```

### 3. 修复 API 和 WebSocket URL (`src/utils/request.js`, `src/store/chat.js`)

**修复问题**: URL 硬编码，不便维护

```javascript
// 使用平台工具获取 URL
import { getApiBaseUrl, getWsUrl } from '@/utils/platform'

const baseURL = getApiBaseUrl()
const wsUrl = getWsUrl()
```

### 4. 修复 WebSocket 客户端类型 (`src/utils/websocket.js`)

**修复问题**: 发送的客户端类型不准确

```javascript
import { getClientType } from './platform'

sendAuth() {
  this.send({
    type: 'auth',
    data: {
      token,
      clientType: getClientType() // 现在会正确返回 'android', 'ios', 'web' 等
    }
  })
}
```

### 5. 修复键盘处理配置 (`capacitor.config.json`)

**修复问题**: 键盘弹出时页面布局跳动

```json
"Keyboard": {
  "resize": "none",  // 从 "body" 改为 "none"
  "style": "dark",
  "resizeOnFullScreen": true
}
```

**说明**: 
- `"body"` 会调整 body 高度，导致布局跳动
- `"none"` 不自动调整，由我们自己控制布局

### 6. 新增移动端通用样式 (`src/styles/mobile.scss`)

**修复问题**: 移动端 CSS 兼容性不足

包含以下优化：

#### 安全区域处理
```css
:root {
  --sat: env(safe-area-inset-top, 0px);
  --sab: env(safe-area-inset-bottom, 0px);
}
```

#### 触摸优化
```css
.touchable-item {
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
}
```

#### 键盘适配
```css
.input-fixed-bottom {
  position: fixed;
  bottom: 0;
  padding-bottom: var(--sab);
}
```

#### 对话框适配
```css
@media (max-width: 768px) {
  .el-dialog {
    width: 100% !important;
    height: 100% !important;
  }
}
```

#### 表单优化
```css
@media (max-width: 768px) {
  .el-input__wrapper {
    min-height: 44px !important; /* 增大点击区域 */
  }

  .el-textarea__inner {
    font-size: 16px !important; /* 防止 iOS 缩放 */
  }
}
```

### 7. 修复文件上传兼容性 (`src/components/chat/ChatInput.vue`)

**修复问题**: `capture` 属性在某些浏览器不支持

```javascript
// 安全地设置 capture 属性
if (isMobile()) {
  try {
    input.capture = 'environment'
  } catch (e) {
    console.warn('浏览器不支持 capture 属性:', e)
  }
}
```

### 8. 修复各页面移动端布局

#### MainLayout.vue
```css
@media (max-width: 768px) {
  .main-content {
    height: calc(100vh - var(--bottom-nav-height) - var(--sab));
  }

  .bottom-nav-item {
    min-height: 44px; /* 增大点击区域 */
  }
}
```

#### chat/index.vue
```css
@media (max-width: 768px) {
  .chat-container {
    height: calc(100vh - var(--bottom-nav-height) - var(--sab));
    overflow: hidden;
  }

  .back-btn {
    padding: 8px; /* 增大点击区域 */
    margin: -8px 8px -8px 0;
  }

  .message-list {
    overscroll-behavior-y: contain; /* 防止弹性滚动 */
  }
}
```

#### login/index.vue
```css
@media (max-width: 768px) {
  .login-container {
    padding-top: var(--sat);
    padding-bottom: var(--sab);
  }

  .el-textarea__inner {
    font-size: 16px !important; /* 防止 iOS 缩放 */
  }
}
```

#### profile/index.vue
```css
@media (max-width: 768px) {
  .profile-container {
    height: calc(100vh - var(--bottom-nav-height) - var(--sab));
    padding-top: var(--sat);
  }

  .el-input__wrapper {
    min-height: 44px !important;
  }
}
```

## 使用注意事项

### 1. Token 函数现在是异步的

```javascript
// ❌ 错误
const token = getToken()
removeToken()

// ✅ 正确
const token = await getToken()
await removeToken()
```

### 2. 检查所有调用 `getToken()` 的地方

确保使用 `await`：
- `src/utils/request.js` - 请求拦截器
- `src/utils/websocket.js` - WebSocket 连接
- `src/store/chat.js` - 初始化 WebSocket
- `src/store/user.js` - 用户认证

### 3. 环境变量配置

在 `.env.production` 中配置服务器地址：

```bash
# API 服务器地址（Capacitor 直连）
VITE_API_SERVER_URL=http://118.25.44.250:8080/api

# WebSocket 服务器地址（Capacitor 直连）
VITE_WS_SERVER_URL=ws://118.25.44.250:9092/ws
```

### 4. 重新构建和部署

```bash
# 1. 清理构建产物
rm -rf dist

# 2. 重新构建
npm run build

# 3. 同步到 Capacitor
npx cap sync

# 4. 运行应用
npx cap run android
```

## 测试清单

### 基础功能测试
- [ ] 登录/登出功能
- [ ] 会话列表加载
- [ ] 聊天消息发送/接收
- [ ] 文件上传（图片、视频、文件）
- [ ] 语音录制和播放
- [ ] 消息已读回执
- [ ] 好友申请通知

### 移动端特定测试
- [ ] 键盘弹出/收起时不跳动
- [ ] 安全区域正确显示（iPhone 刘海屏）
- [ ] 底部导航栏不遮挡内容
- [ ] 页面转场动画流畅
- [ ] 触摸反馈正常
- [ ] 返回按钮工作正常
- [ ] 状态栏颜色正确
- [ ] 深色模式正常

### 不同设备测试
- [ ] Android 手机
- [ ] Android 平板
- [ ] iPhone（有刘海）
- [ ] iPhone（无刘海）
- [ ] iPad

## 常见问题排查

### 1. API 请求失败

**症状**: 网页端正常，移动端请求 404 或连接失败

**排查**:
```javascript
// 检查当前环境检测
console.log('isCapacitor:', isCapacitor())
console.log('API URL:', getApiBaseUrl())
console.log('WS URL:', getWsUrl())
```

### 2. Token 存储问题

**症状**: 登录后刷新丢失 Token

**排查**:
```javascript
// 检查 Token 存储
const token = await getToken()
console.log('Token:', token)

// 检查 Preferences 是否可用
if (isCapacitor()) {
  const { Preferences } = await import('@capacitor/preferences')
  const result = await Preferences.get({ key: 'crm_chat_token' })
  console.log('Preferences Token:', result.value)
}
```

### 3. 键盘问题

**症状**: 键盘弹出时布局跳动

**排查**:
- 检查 `capacitor.config.json` 中的 `Keyboard.resize` 设置
- 确保使用了 `"none"` 而不是 `"body"`

### 4. 样式问题

**症状**: 移动端样式错乱

**排查**:
- 检查 `mobile.scss` 是否正确导入
- 检查 CSS 变量是否正确定义
- 使用 Chrome DevTools 远程调试查看样式

## 回滚方案

如果修复引入了新问题，可以快速回滚：

```bash
# 1. 恢复 auth.js
git checkout HEAD -- src/utils/auth.js

# 2. 恢复 request.js
git checkout HEAD -- src/utils/request.js

# 3. 恢复 chat.js
git checkout HEAD -- src/store/chat.js

# 4. 删除新增文件
rm src/utils/platform.js
rm src/styles/mobile.scss

# 5. 恢复 capacitor.config.json
git checkout HEAD -- capacitor.config.json
```

## 后续优化建议

1. **添加错误边界**: 捕获 Capacitor API 调用失败
2. **实现离线模式**: 使用 Capacitor 的 Filesystem 插件
3. **推送通知**: 使用 Capacitor 的 Push Notifications 插件
4. **生物认证**: 使用 Capacitor 的 Biometric 插件
5. **应用内购买**: 如果需要付费功能

## 参考资源

- [Capacitor 官方文档](https://capacitorjs.com/docs)
- [Capacitor Preferences 插件](https://capacitorjs.com/docs/plugins/preferences)
- [Capacitor Keyboard 插件](https://capacitorjs.com/docs/plugins/keyboard)
- [Capacitor App 插件](https://capacitorjs.com/docs/plugins/app)
- [Vue.js 移动端开发指南](https://vuejs.org/guide/extras/web-components.html)



📋 测试步骤

方式一：Android Studio 测试（推荐）

# 1. 进入前端目录
cd D:\KuaiTong\kwchat\kwchat-frontend

# 2. 构建前端项目
npm run build

# 3. 同步到 Android 项目
npx cap sync

# 4. 打开 Android Studio
npx cap open android

然后在 Android Studio 中：
1. 等待 Gradle 同步完成（首次可能需要几分钟）
2. 连接 Android 手机或启动模拟器
3. 点击 Run 按钮（▶️）运行应用


语音播放：
- iOS Safari 的自动播放策略限制了通过 fetch()
  获取音频后用 blob URL 播放
- 新方案先直接用 URL 播放，如果失败再用 fetch + blob 方式
- 根据文件扩展名（.webm/.m4a/.mp4）自动设置正确的 MIME   
  类型

拍照/视频：
- iOS Safari 不允许动态创建的 <input> 元素触发文件选择器
- 改为使用模板中已存在的 <input ref="imageInputRef"> 和  
  <input ref="videoInputRef">
- 拍照时动态添加 capture 属性，拍完后自动移除
