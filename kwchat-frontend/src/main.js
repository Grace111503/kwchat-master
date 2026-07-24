import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import App from './App.vue'
import router from './router'

import './styles/index.scss'
import './styles/responsive.scss'

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// 抑制 Element Plus 在路由切换时产生的 parentNode 错误
// 这是 Vue 3 + Element Plus 已知问题：组件卸载时 teleport 的 DOM 元素已被移除
app.config.errorHandler = (err) => {
  if (err.message?.includes('parentNode') || err.message?.includes('Cannot read properties of null')) {
    return // 静默忽略这些无害的 DOM 清理错误
  }
  console.error(err)
}

app.mount('#app')