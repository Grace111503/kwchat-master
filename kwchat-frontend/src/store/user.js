import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getUserInfo, logout } from '@/api/user'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(getToken() || '')
  const userInfo = ref(null)
  const isLoggedIn = computed(() => !!token.value)

  // 初始化用户状态
  const initUserState = () => {
    if (token.value) {
      fetchUserInfo()
    }
  }

  // 登录
  const loginAction = async (loginForm) => {
    try {
      const res = await login(loginForm)
      if (res.code === 200) {
        token.value = res.data.token
        setToken(res.data.token)
        await fetchUserInfo()
        ElMessage.success('登录成功')
        return true
      } else {
        ElMessage.error(res.message || '登录失败')
        return false
      }
    } catch (error) {
      ElMessage.error('登录失败，请检查网络')
      return false
    }
  }

  // 注册
  const registerAction = async (registerForm) => {
    try {
      const res = await register(registerForm)
      if (res.code === 200) {
        ElMessage.success('注册成功，请登录')
        return true
      } else {
        ElMessage.error(res.message || '注册失败')
        return false
      }
    } catch (error) {
      ElMessage.error('注册失败，请检查网络')
      return false
    }
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    try {
      const res = await getUserInfo()
      if (res.code === 200) {
        userInfo.value = res.data
      } else {
        // Token失效，清除登录状态
        logoutAction()
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  // 登出
  const logoutAction = async () => {
    try {
      await logout()
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      token.value = ''
      userInfo.value = null
      removeToken()
    }
  }

  // 更新用户信息
  const updateUserInfo = (newUserInfo) => {
    userInfo.value = { ...userInfo.value, ...newUserInfo }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    initUserState,
    loginAction,
    registerAction,
    fetchUserInfo,
    logoutAction,
    updateUserInfo
  }
})