# Android 网络请求失败排查指南

## 问题现象
- APK 安装后能打开界面
- 点击登录等操作时显示"网络请求失败"

## 原因分析

### 最常见原因：服务器不可达
手机无法访问 `http://118.25.44.250:8080`，可能原因：
1. 服务器未运行
2. 防火墙阻止了 8080/9092 端口
3. 手机和服务器不在同一网络
4. 服务器 IP 地址变更

### 次要原因：Android 安全限制
Android 9+ 默认阻止 HTTP 明文流量（已配置允许）

---

## 排查步骤

### 步骤 1：在手机浏览器测试

在手机浏览器中直接访问：
```
http://118.25.44.250:8080
```

- ✅ 能看到内容 → 服务器可达，问题在 App 配置
- ❌ 无法访问 → 服务器问题，继续步骤 2

### 步骤 2：在电脑上测试

```powershell
# 测试服务器是否在线
ping 118.25.44.250

# 测试 8080 端口
curl http://118.25.44.250:8080
```

### 步骤 3：检查服务器防火墙

在服务器上执行：

```bash
# 查看端口监听状态
sudo netstat -tlnp | grep -E "8080|9092"

# 如果是 Ubuntu/Debian，检查 UFW 防火墙
sudo ufw status
sudo ufw allow 8080/tcp
sudo ufw allow 9092/tcp

# 如果是 CentOS/RedHat，检查 firewalld
sudo firewall-cmd --list-all
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=9092/tcp
sudo firewall-cmd --reload
```

### 步骤 4：检查服务器应用是否运行

```bash
# 查看 Java 进程（Spring Boot 应用）
ps aux | grep java

# 查看端口占用
lsof -i :8080
lsof -i :9092
```

---

## 快速修复方案

### 方案 A：服务器未运行

启动服务器：

```bash
# 进入服务器目录
cd /path/to/your/server

# 启动应用（假设是 Spring Boot）
java -jar your-app.jar

# 或者使用 Docker
docker start your-container
```

### 方案 B：防火墙未开放端口

```bash
# Ubuntu/Debian
sudo ufw allow 8080/tcp
sudo ufw allow 9092/tcp
sudo ufw reload

# CentOS/RedHat
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=9092/tcp
sudo firewall-cmd --reload
```

### 方案 C：服务器 IP 变更

如果服务器 IP 已变更，需要修改以下文件：

1. **前端环境变量** - `kwchat-frontend/.env.production`
```bash
VITE_API_SERVER_URL=http://新IP:8080/api
VITE_WS_SERVER_URL=ws://新IP:9092/ws
```

2. **平台工具** - `kwchat-frontend/src/utils/platform.js`
```javascript
export function getApiBaseUrl() {
  if (isCapacitor()) {
    return import.meta.env.VITE_API_SERVER_URL || 'http://新IP:8080/api'
  }
  return '/api'
}

export function getWsUrl() {
  if (isCapacitor()) {
    return import.meta.env.VITE_WS_SERVER_URL || 'ws://新IP:9092/ws'
  }
  return `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws`
}
```

然后重新构建：
```bash
npm run build
npx cap sync
```

### 方案 D：手机和服务器不在同一网络

确保手机和服务器在同一局域网，或者：
- 服务器有公网 IP（你的情况）
- 手机能访问互联网

---

## 测试服务器可达性

在服务器上创建一个测试页面：

```bash
# 在服务器上创建测试文件
echo "<h1>Server is running!</h1>" | sudo tee /var/www/html/test.html
```

然后在手机浏览器访问：
```
http://118.25.44.250/test.html
```

如果能看到 "Server is running!"，说明服务器可达。

---

## 查看 App 日志

### 方法 1：使用 Android Studio

1. 打开 Android Studio
2. 连接手机或启动模拟器
3. 点击 **View → Tool Windows → Logcat**
4. 过滤 `com.kwp.chat`
5. 查看网络请求相关的错误日志

### 方法 2：使用 ADB 命令

```powershell
# 查看 App 日志
adb logcat | grep -i "kwchat\|capacitor\|chromium"

# 只看网络相关日志
adb logcat | grep -i "net\|http\|socket\|connect"
```

### 方法 3：在 App 中添加调试信息

修改 `kwchat-frontend/src/utils/request.js`，在请求拦截器中添加日志：

```javascript
service.interceptors.request.use(
  config => {
    console.log('=== API Request ===')
    console.log('URL:', config.baseURL + config.url)
    console.log('Method:', config.method)
    console.log('Headers:', config.headers)
    
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)
```

然后重新构建并运行，查看 Logcat 输出。

---

## 环境变量配置检查

确保 `.env.production` 文件内容正确：

```bash
# 生产环境配置

# API基础路径
VITE_API_BASE_URL=/api

# WebSocket地址（使用IP访问）
VITE_WS_URL=ws://118.25.44.250/ws

# 应用标题
VITE_APP_TITLE=快伟通

# 服务器地址（App直连用）
VITE_API_SERVER_URL=http://118.25.44.250:8080/api
VITE_WS_SERVER_URL=ws://118.25.44.250:9092/ws
```

检查构建产物是否包含正确的地址：

```bash
# 在前端目录执行
grep -o "http://118.25.44.250[^\"']*" dist/assets/*.js
```

应该看到：
```
dist/assets/index-xxxxx.js:http://118.25.44.250:8080/api
```

---

## 常见错误排查

### 错误 1：net::ERR_CONNECTION_REFUSED

**原因**：服务器未运行或端口未开放

**解决**：
```bash
# 检查服务器是否运行
ps aux | grep java

# 检查端口是否监听
netstat -tlnp | grep 8080
```

### 错误 2：net::ERR_CONNECTION_TIMED_OUT

**原因**：网络不通或防火墙阻止

**解决**：
```bash
# 检查防火墙
sudo ufw status

# 开放端口
sudo ufw allow 8080/tcp
sudo ufw allow 9092/tcp
```

### 错误 3：net::ERR_NAME_NOT_RESOLVED

**原因**：DNS 解析失败（不应该发生，因为用的是 IP）

**解决**：检查 IP 地址是否正确

### 错误 4：net::ERR_CLEARTEXT_NOT_PERMITTED

**原因**：Android 阻止 HTTP 明文流量

**解决**：确保 `network_security_config.xml` 配置正确（已配置）

---

## 重新构建和测试

如果修改了配置，需要重新构建：

```powershell
# 1. 进入前端目录
cd D:\KuaiTong\kwchat\kwchat-frontend

# 2. 清理旧构建
Remove-Item -Recurse -Force dist

# 3. 重新构建
npm run build

# 4. 同步到 Android
npx cap sync

# 5. 重新运行
npx cap run android
```

---

## 联系支持

如果以上方法都无法解决问题，请提供：

1. 手机浏览器访问 `http://118.25.44.250:8080` 的截图
2. 电脑上 `ping 118.25.44.250` 的结果
3. 电脑上 `curl http://118.25.44.250:8080` 的结果
4. Android Logcat 中的错误日志
