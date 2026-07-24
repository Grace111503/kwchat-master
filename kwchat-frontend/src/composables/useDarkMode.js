import { ref, watch, onMounted } from 'vue'

// 深色模式状态
const isDarkMode = ref(false)

// 初始化深色模式
const initDarkMode = () => {
  // 从localStorage读取用户偏好
  const savedMode = localStorage.getItem('darkMode')
  if (savedMode !== null) {
    isDarkMode.value = savedMode === 'true'
  } else {
    // 如果没有保存的偏好，检查系统设置
    isDarkMode.value = window.matchMedia('(prefers-color-scheme: dark)').matches
  }
  applyDarkMode()
}

// 应用深色模式
const applyDarkMode = () => {
  if (isDarkMode.value) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

// 切换深色模式
const toggleDarkMode = () => {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
  applyDarkMode()
}

// 监听系统主题变化
const watchSystemTheme = () => {
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
    // 只有用户没有手动设置时才跟随系统
    if (localStorage.getItem('darkMode') === null) {
      isDarkMode.value = e.matches
      applyDarkMode()
    }
  })
}

export function useDarkMode() {
  onMounted(() => {
    initDarkMode()
    watchSystemTheme()
  })

  return {
    isDarkMode,
    toggleDarkMode
  }
}
