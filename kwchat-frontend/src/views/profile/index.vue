<template>
  <div class="profile-container">
    <div class="profile-header">
      <div class="profile-avatar">
        <el-avatar :size="80" :src="userInfo?.avatar" shape="square">
          {{ getAvatarFallback(userInfo?.nickname || userInfo?.username) }}
        </el-avatar>
        <div class="avatar-overlay" @click="uploadAvatar">
          <el-icon><Camera /></el-icon>
          <span>修改头像</span>
        </div>
        <input
          ref="fileInputRef"
          type="file"
          accept="image/jpeg,image/png,image/gif,image/webp"
          style="display: none"
          @change="handleFileChange"
        />
      </div>
      <h2 class="profile-name">{{ userInfo?.username || '用户' }}</h2>
      <p class="profile-department">{{ userInfo?.department || '暂未设置部门' }}</p>
      <p class="profile-signature">{{ userInfo?.signature || '暂无签名' }}</p>
    </div>

    <div class="profile-content">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="个人信息" name="info">
          <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="80px" class="profile-form">
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="部门" prop="department">
              <el-input v-model="profileForm.department" placeholder="请输入部门" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="profileForm.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="生日">
              <el-date-picker v-model="profileForm.birthday" type="date" placeholder="选择日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
            </el-form-item>
            <el-form-item label="个性签名">
              <el-input v-model="profileForm.signature" type="textarea" :rows="3" placeholder="请输入个性签名" maxlength="100" show-word-limit />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存修改</el-button>
              <el-button @click="resetProfile">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="修改密码" name="password">
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="80px" class="password-form">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword">修改密码</el-button>
              <el-button @click="resetPassword">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="账号安全" name="security">
          <div class="security-section">
            <h3>登录设备管理</h3>
            <div class="device-list">
              <div class="device-item">
                <div class="device-info">
                  <el-icon :size="20"><Monitor /></el-icon>
                  <div>
                    <div class="device-name">Windows PC</div>
                    <div class="device-time">最后登录: 2024-03-15 10:30:00</div>
                  </div>
                </div>
                <el-tag type="success" size="small">当前设备</el-tag>
              </div>
            </div>
          </div>

          <div class="security-section">
            <h3>安全设置</h3>
            <div class="security-options">
              <div class="option-item">
                <div>
                  <div class="option-title">两步验证</div>
                  <div class="option-desc">登录时需要验证码</div>
                </div>
                <el-switch v-model="securityForm.twoFactorAuth" />
              </div>
              <div class="option-item">
                <div>
                  <div class="option-title">登录通知</div>
                  <div class="option-desc">新设备登录时发送通知</div>
                </div>
                <el-switch v-model="securityForm.loginNotification" />
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { uploadAvatar as uploadAvatarApi, updateUserInfo as updateUserInfoApi, changePassword as changePasswordApi } from '@/api/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const userInfo = ref(userStore.userInfo)
const activeTab = ref('info')
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const fileInputRef = ref(null)

const profileForm = reactive({ username: '', department: '', phone: '', email: '', gender: 1, birthday: '', signature: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const securityForm = reactive({ twoFactorAuth: false, loginNotification: true })

const profileRules = {
  department: [{ required: true, message: '请输入部门', trigger: 'blur' }, { min: 1, max: 50, message: '部门名称长度在 1 到 50 个字符', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') callback(new Error('请再次输入密码'))
  else if (value !== passwordForm.newPassword) callback(new Error('两次输入密码不一致'))
  else callback()
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }]
}

onMounted(() => {
  if (userInfo.value) {
    Object.assign(profileForm, {
      username: userInfo.value.username, department: userInfo.value.department, phone: userInfo.value.phone,
      email: userInfo.value.email, gender: userInfo.value.gender || 1, birthday: userInfo.value.birthday, signature: userInfo.value.signature
    })
  }
})

const saveProfile = async () => {
  if (!profileFormRef.value) return
  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await updateUserInfoApi(profileForm)
        if (res.code === 200) {
          ElMessage.success('保存成功')
          // 更新本地 store
          userStore.updateUserInfo(res.data)
          // 更新 userInfo 引用
          userInfo.value = { ...userInfo.value, ...res.data }
        } else {
          ElMessage.error(res.message || '保存失败')
        }
      } catch (error) {
        ElMessage.error('保存失败，请重试')
      }
    }
  })
}

const resetProfile = () => {
  if (userInfo.value) {
    Object.assign(profileForm, {
      username: userInfo.value.username, department: userInfo.value.department, phone: userInfo.value.phone,
      email: userInfo.value.email, gender: userInfo.value.gender || 1, birthday: userInfo.value.birthday, signature: userInfo.value.signature
    })
  }
}

const changePassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await changePasswordApi(passwordForm)
        if (res.code === 200) {
          ElMessage.success('密码修改成功')
          resetPassword()
        } else {
          ElMessage.error(res.message || '修改失败')
        }
      } catch (error) {
        ElMessage.error('修改失败，请重试')
      }
    }
  })
}

const resetPassword = () => { passwordForm.oldPassword = ''; passwordForm.newPassword = ''; passwordForm.confirmPassword = '' }

// 获取头像 fallback 文字（显示最后两个字）
const getAvatarFallback = (name) => {
  if (!name) return '用户'
  return name.length >= 2 ? name.slice(-2) : name
}

// 点击头像触发文件选择
const uploadAvatar = () => {
  fileInputRef.value?.click()
}

// 处理文件选择
const handleFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只能上传 JPG/PNG/GIF/WebP 格式的图片')
    return
  }

  // 验证文件大小（2MB）
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }

  try {
    const res = await uploadAvatarApi(file)
    if (res.code === 200) {
      // 更新头像
      userInfo.value.avatar = res.data
      userStore.updateUserInfo({ avatar: res.data })
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error('上传失败，请重试')
  }

  // 清空 input 值，允许重复上传同一文件
  event.target.value = ''
}
</script>

<style lang="scss" scoped>
.profile-container {
  height: 100%;
  overflow-y: auto;
  background: #f5f5f5;
}

.profile-header {
  background: #f0f2f5;
  padding: 32px;
  text-align: center;
  color: #1a1a1a;
  border-bottom: 1px solid #e5e5e5;
}

.profile-avatar {
  position: relative;
  display: inline-block;
  cursor: pointer;

  .avatar-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.6);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.2s;

    &:hover { opacity: 1; }
    span { font-size: 11px; margin-top: 4px; }
  }
}

.profile-name {
  font-size: 20px;
  margin: 12px 0 4px;
  font-weight: 600;
}

.profile-department {
  font-size: 14px;
  color: #666;
  margin: 4px 0;
}

.profile-signature {
  font-size: 13px;
  color: #86909c;
}

.profile-content {
  max-width: 720px;
  margin: 16px auto;
  padding: 20px;
  background: #fff;
  border: 1px solid #e5e5e5;
}

.profile-form,
.password-form {
  max-width: 480px;
  margin-top: 16px;

  :deep(.el-input__wrapper) {
    border-radius: 0;
  }

  :deep(.el-button) {
    border-radius: 0;
  }
}

.security-section {
  margin-bottom: 24px;

  h3 {
    font-size: 15px;
    color: #1a1a1a;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #e5e5e5;
    font-weight: 600;
  }
}

.device-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.device-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
}

.device-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.device-name {
  font-size: 14px;
  color: #333;
}

.device-time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.security-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
}

.option-title {
  font-size: 14px;
  color: #333;
}

.option-desc {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

// 移动端响应式
@media (max-width: 768px) {
  .profile-container {
    height: calc(100vh - 56px);
  }

  .profile-header {
    padding: 20px;
  }

  .profile-content {
    margin: 8px;
    padding: 12px;
  }

  .profile-form,
  .password-form {
    max-width: 100%;
    padding: 0 8px;
  }
}
</style>
