const { contextBridge } = require('electron')

// 通过 contextBridge 安全地暴露 API 给渲染进程
// 当前为最小集，后续可扩展：系统通知、文件保存等
contextBridge.exposeInMainWorld('kwchatDesktop', {
  version: '1.0.0',
  platform: process.platform,
  isDesktop: true
})