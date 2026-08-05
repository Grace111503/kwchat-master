import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import App from './App.vue'
import router from './router'
import { isCapacitor } from './utils/platform'

import './styles/index.scss'
import './styles/responsive.scss'
import './styles/mobile.scss'

// 热更新：检查新版本
async function checkForUpdates() {
  console.log('[Update] ===== Starting update check =====')

  // 仅在 Capacitor 环境下检查更新
  if (!isCapacitor()) {
    console.log('[Update] Not in Capacitor environment, skipping update check')
    return
  }

  console.log('[Update] Capacitor environment detected, checking for updates...')

  try {
    const { CapacitorUpdater } = await import('@capgo/capacitor-updater')

    // 从服务器获取最新版本
    const response = await fetch('http://118.25.44.250:8080/api/uploads/updates/app-version.json')
    const latest = await response.json()
    console.log('[Update] Latest version:', latest.version)

    const current = await CapacitorUpdater.currentVersion()
    console.log('[Update] Current version:', current.version)

    if (current.version !== latest.version) {
      console.log(`[Update] New version available: ${latest.version}`)

      // 下载新版本（静默）
      const bundle = await CapacitorUpdater.download({
        url: latest.url,
        version: latest.version
      })

      // 设置下次启动使用新版本
      await CapacitorUpdater.set(bundle)
      console.log('[Update] Update downloaded, will apply on next restart')

      // 保存更新信息到 localStorage，下次进入时提示
      localStorage.setItem('kwchat_update_notes', latest.notes || '新版本已更新')
      localStorage.setItem('kwchat_new_version', latest.version)
    } else {
      console.log('[Update] App is up to date')
    }
  } catch (error) {
    console.error('[Update] Check failed:', error.message)
    // 更新检查失败不影响 App 正常使用
  }
}

// Capacitor 环境下禁用 Service Worker，防止缓存干扰 API 请求
if (typeof window !== 'undefined') {
  const isCapEnv = isCapacitor()

  if (isCapEnv) {
    console.log('[App] Capacitor environment detected, disabling Service Worker')
    // 注销已有的 Service Worker
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.getRegistrations().then(registrations => {
        for (const registration of registrations) {
          console.log('[App] Unregistering Service Worker:', registration.scope)
          registration.unregister()
        }
      }).catch(err => {
        console.warn('[App] Failed to unregister Service Worker:', err)
      })
    }

    // 检查热更新
    checkForUpdates()
  }
}

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')