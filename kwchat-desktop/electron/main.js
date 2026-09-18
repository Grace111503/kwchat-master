const { app, BrowserWindow, Menu, shell } = require('electron')
const path = require('path')
const fs = require('fs')

// 判断是否开发模式（通过环境变量）
const isDev = process.env.ELECTRON_IS_DEV === '1'

// 单实例锁（防止重复启动）
const gotTheLock = app.requestSingleInstanceLock()
if (!gotTheLock) {
  app.quit()
}

let mainWindow = null

function getFrontendPath() {
  if (isDev) {
    // 开发模式：从源工程加载
    return path.join(__dirname, '../../kwchat-frontend/dist/index.html')
  }
  // 生产模式：从打包资源加载
  return path.join(process.resourcesPath, 'frontend-dist/index.html')
}

function getFrontendUrl() {
  const frontendPath = getFrontendPath()
  if (fs.existsSync(frontendPath)) {
    return `file://${frontendPath.replace(/\\/g, '/')}`
  }
  // 兜底：开发模式下可改成 http://localhost:3000 联调
  console.warn('[KwChat] 前端 dist 未找到，请先执行 npm run build:', frontendPath)
  return `file://${frontendPath.replace(/\\/g, '/')}`
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 800,
    minWidth: 1024,
    minHeight: 600,
    title: '快伟通',
    icon: path.join(__dirname, '../build/icon.ico'),
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true
    }
  })

  // 隐藏菜单栏（Windows 下按 Alt 可唤出）
  Menu.setApplicationMenu(null)

  // 加载前端
  mainWindow.loadURL(getFrontendUrl())

  // 开发模式打开开发者工具
  if (isDev) {
    mainWindow.webContents.openDevTools()
  }

  // 外部链接用系统浏览器打开
  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url)
    return { action: 'deny' }
  })

  mainWindow.on('closed', () => {
    mainWindow = null
  })
}

app.whenReady().then(() => {
  createWindow()

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow()
    }
  })
})

// 第二个实例启动时，聚焦主窗口
app.on('second-instance', () => {
  if (mainWindow) {
    if (mainWindow.isMinimized()) mainWindow.restore()
    mainWindow.focus()
  }
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})