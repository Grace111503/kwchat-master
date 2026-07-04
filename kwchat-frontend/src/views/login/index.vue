<template>
  <div class="login-container">
    <div class="login-left">
      <div class="brand">
        <div class="brand-logo">KW</div>
        <h1 class="brand-name">快伟通</h1>
        <p class="brand-desc">企业级即时通讯平台</p>
      </div>
    </div>

    <div class="login-right">
      <div class="login-card">
        <h2 class="login-title">登录</h2>
        <p class="login-subtitle">欢迎回来，请输入您的账号信息</p>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="用户名"
              size="large"
              prefix-icon="User"
              autocomplete="off"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              size="large"
              prefix-icon="Lock"
              show-password
              autocomplete="new-password"
            />
          </el-form-item>

          <el-form-item>
            <div class="login-options">
              <el-checkbox v-model="loginForm.remember">记住密码</el-checkbox>
              <el-link type="primary" underline="never">忘记密码</el-link>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>

          <div class="login-footer">
            <span>还没有账号？</span>
            <el-link type="primary" underline="never" @click="goToRegister">立即注册</el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.loginAction(loginForm)
        if (success) {
          const redirect = route.query.redirect || '/chat'
          router.push(redirect)
        }
      } finally {
        loading.value = false
      }
    }
  })
}

const goToRegister = () => {
  router.push({ name: 'Register' })
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  display: flex;
}

.login-left {
  width: 45%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #e5e5e5;
}

.brand {
  text-align: center;
  color: #1a1a1a;
}

.brand-logo {
  width: 64px;
  height: 64px;
  background: #2b7fff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 2px;
  margin: 0 auto 24px;
}

.brand-name {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 8px;
  letter-spacing: 4px;
}

.brand-desc {
  font-size: 14px;
  color: #86909c;
  letter-spacing: 2px;
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.login-card {
  width: 380px;
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #8a8a8a;
  margin-bottom: 36px;
}

.login-form {
  :deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #e0e0e0 inset;
    border-radius: 0;

    &:hover {
      box-shadow: 0 0 0 1px #c0c0c0 inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 1px #2b7fff inset;
    }
  }

  .el-form-item {
    margin-bottom: 24px;
  }
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 4px;
  background: #2b7fff;
  border: none;
  border-radius: 0;

  &:hover {
    background: #1a6fe0;
  }
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #8a8a8a;

  .el-link {
    margin-left: 4px;
  }
}
</style>
