import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, getUserInfo, logout } from '@/api/user'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(null)
  const isLoggedIn = computed(() => !!token.value)

  const initUserState = () => {
    if (token.value) {
      fetchUserInfo()
    }
  }

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
      ElMessage.error('登录失败')
      return false
    }
  }

  const fetchUserInfo = async () => {
    try {
      const res = await getUserInfo()
      if (res.code === 200) {
        userInfo.value = res.data
      } else {
        logoutAction()
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  const logoutAction = async () => {
    try {
      await logout()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      token.value = ''
      userInfo.value = null
      removeToken()
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    initUserState,
    loginAction,
    fetchUserInfo,
    logoutAction
  }
})