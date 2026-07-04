<template>
  <div class="register-container">
    <div class="register-left">
      <div class="brand">
        <div class="brand-logo">KW</div>
        <h1 class="brand-name">快伟通</h1>
        <p class="brand-desc">企业级即时通讯平台</p>
      </div>
    </div>

    <div class="register-right">
      <div class="register-card">
        <h2 class="register-title">注册</h2>
        <p class="register-subtitle">创建您的账号</p>

        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="register-form"
          @keyup.enter="handleRegister"
        >
          <el-form-item prop="username">
            <el-input v-model="registerForm.username" placeholder="用户名" size="large" prefix-icon="User" autocomplete="off" />
          </el-form-item>

          <el-form-item prop="department">
            <el-input v-model="registerForm.department" placeholder="部门" size="large" prefix-icon="OfficeBuilding" autocomplete="off" />
          </el-form-item>

          <el-form-item prop="phone">
            <el-input v-model="registerForm.phone" placeholder="手机号" size="large" prefix-icon="Phone" autocomplete="off" />
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="registerForm.password" type="password" placeholder="密码" size="large" prefix-icon="Lock" show-password autocomplete="new-password" />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" size="large" prefix-icon="Lock" show-password autocomplete="new-password" />
          </el-form-item>

          <el-form-item prop="agreement">
            <el-checkbox v-model="registerForm.agreement">
              我已阅读并同意
              <el-link type="primary" underline="never">《用户协议》</el-link>
              和
              <el-link type="primary" underline="never">《隐私政策》</el-link>
            </el-checkbox>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" size="large" class="register-btn" :loading="loading" @click="handleRegister">
              注 册
            </el-button>
          </el-form-item>

          <div class="register-footer">
            <span>已有账号？</span>
            <el-link type="primary" underline="never" @click="goToLogin">立即登录</el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '', department: '', phone: '', password: '', confirmPassword: '', agreement: false
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') callback(new Error('请再次输入密码'))
  else if (value !== registerForm.password) callback(new Error('两次输入密码不一致'))
  else callback()
}

const validateAgreement = (rule, value, callback) => {
  if (!value) callback(new Error('请同意用户协议'))
  else callback()
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }],
  department: [{ required: true, message: '请输入部门', trigger: 'blur' }, { min: 1, max: 50, message: '部门名称长度在 1 到 50 个字符', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
  agreement: [{ validator: validateAgreement, trigger: 'change' }]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.registerAction(registerForm)
        if (success) router.push({ name: 'Login' })
      } finally {
        loading.value = false
      }
    }
  })
}

const goToLogin = () => router.push({ name: 'Login' })
</script>

<style lang="scss" scoped>
.register-container {
  height: 100vh;
  display: flex;
}

.register-left {
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

.register-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.register-card {
  width: 380px;
}

.register-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.register-subtitle {
  font-size: 14px;
  color: #8a8a8a;
  margin-bottom: 32px;
}

.register-form {
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
    margin-bottom: 20px;
  }
}

.register-btn {
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

.register-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #8a8a8a;

  .el-link {
    margin-left: 4px;
  }
}
</style>
