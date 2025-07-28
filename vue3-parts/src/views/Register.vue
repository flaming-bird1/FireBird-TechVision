<template>
  <div class="register-page">
    <!-- 背景装饰元素 -->
    <div class="bg-shapes">
      <div class="shape-1"></div>
      <div class="shape-2"></div>
      <div class="shape-3"></div>
    </div>

    <!-- 主注册卡片 -->
    <div class="register-card">
      <!-- 左侧品牌展示区 -->
      <div class="brand-section">
        <div class="brand-content">
          <h1 class="brand-title">烈焰飞鸟·梦工厂</h1>
          <p class="brand-subtitle">开启您的技术探索之旅</p>

          <!-- 特色功能展示 -->
          <div class="features">
            <div class="feature-item">
              <el-icon :size="20" color="#409EFF"><MagicStick /></el-icon>
              <span>AI项目展示</span>
            </div>
            <div class="feature-item">
              <el-icon :size="20" color="#67C23A"><Cpu /></el-icon>
              <span>机器学习实践</span>
            </div>
            <div class="feature-item">
              <el-icon :size="20" color="#E6A23C"><DataAnalysis /></el-icon>
              <span>前端技术探索</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧注册表单 -->
      <div class="form-section">
        <div class="form-container">
          <h2 class="form-title">创建账户</h2>
          <p class="form-subtitle">加入我们的技术社区</p>

          <!-- 注册表单 -->
          <el-form
              ref="registerFormRef"
              :model="registerForm"
              :rules="registerRules"
              @keyup.enter="handleRegister"
              class="register-form"
          >
            <el-form-item prop="username">
              <el-input
                  v-model="registerForm.username"
                  placeholder="用户名"
                  size="large"
                  class="custom-input"
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="密码"
                  size="large"
                  class="custom-input"
                  show-password
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="确认密码"
                  size="large"
                  class="custom-input"
                  show-password
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <div class="form-agreement">
              <el-checkbox v-model="agreementChecked">
                我已阅读并同意
                <el-link type="primary" underline="never" @click="router.push('/terms')">服务条款</el-link>
                和
                <el-link type="primary" underline="never" @click="router.push('/privacy')">隐私政策</el-link>
              </el-checkbox>
            </div>

            <el-button
                type="primary"
                size="large"
                class="register-btn"
                :loading="loading"
                @click="handleRegister"
            >
              注册
              <template #loading>
                <span class="loading-text">注册中...</span>
              </template>
            </el-button>
          </el-form>

          <div class="login-link">
            已有账号?
            <el-link
                type="primary"
                underline="never"
                @click="router.push('/login')"
            >
              立即登录
            </el-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 页脚 -->
    <div class="register-footer">
      <span>© 2025 烈焰飞鸟·梦工厂 版权所有</span>
      <el-divider direction="vertical" />
      <el-link underline="never" @click="router.push('/privacy')">隐私政策</el-link>
      <el-divider direction="vertical" />
      <el-link underline="never" @click="router.push('/terms')">服务条款</el-link>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User,
  Lock,
  MagicStick,
  Cpu,
  DataAnalysis
} from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()

const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: ''
})

const registerFormRef = ref(null)
const agreementChecked = ref(false)
const loading = ref(false)

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else {
    if (registerForm.value.confirmPassword !== '') {
      registerFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.value.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = ref({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名长度不能少于3位', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

const handleRegister = async () => {
  try {
    // 验证表单
    await registerFormRef.value.validate()

    if (!agreementChecked.value) {
      ElMessage.warning('请先同意服务条款和隐私政策')
      return
    }

    loading.value = true
    const response = await axios.post('/api/register', {
      username: registerForm.value.username,
      password: registerForm.value.password
    })

    if (response.data.code === 200) {
      ElMessage.success('注册成功')
      // 自动登录
      localStorage.setItem('user', JSON.stringify(response.data.data))
      await router.push('/home')
    } else {
      ElMessage.error(response.data.message || '注册失败')
    }
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data?.message || '注册请求失败')
    } else if (error.message) {
      // 表单验证错误不显示消息
      if (!error.message.includes('validation')) {
        ElMessage.error(error.message)
      }
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.register-page .bg-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.register-page .bg-shapes .shape-1 {
  position: absolute;
  top: -100px;
  right: -100px;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: linear-gradient(45deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0) 100%);
}

.register-page .bg-shapes .shape-2 {
  position: absolute;
  bottom: -150px;
  left: -150px;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: linear-gradient(45deg, rgba(103, 194, 58, 0.1) 0%, rgba(103, 194, 58, 0) 100%);
}

.register-page .bg-shapes .shape-3 {
  position: absolute;
  top: 50%;
  right: 50%;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: linear-gradient(45deg, rgba(230, 162, 60, 0.1) 0%, rgba(230, 162, 60, 0) 100%);
  transform: translate(50%, -50%);
}

.register-page .register-card {
  display: flex;
  width: 900px;
  min-height: 550px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.register-page .register-card .brand-section {
  flex: 1;
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  text-align: center;
}

.register-page .register-card .brand-section .brand-content {
  max-width: 320px;
}

.register-page .register-card .brand-section .brand-content .brand-logo {
  width: 80px;
  height: 80px;
  margin-bottom: 20px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  padding: 10px;
}

.register-page .register-card .brand-section .brand-content .brand-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 10px;
}

.register-page .register-card .brand-section .brand-content .brand-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 30px;
}

.register-page .register-card .brand-section .brand-content .features {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 40px;
}

.register-page .register-card .brand-section .brand-content .features .feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(255, 255, 255, 0.1);
  padding: 12px 20px;
  border-radius: 8px;
  transition: all 0.3s;
}

.register-page .register-card .brand-section .brand-content .features .feature-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateX(5px);
}

.register-page .register-card .brand-section .brand-content .features .feature-item span {
  font-size: 14px;
}

.register-page .register-card .form-section {
  flex: 1;
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.register-page .register-card .form-section .form-container {
  max-width: 360px;
  margin: 0 auto;
  width: 100%;
}

.register-page .register-card .form-section .form-container .form-title {
  font-size: 24px;
  color: #303133;
  margin: 0 0 5px;
  text-align: center;
}

.register-page .register-card .form-section .form-container .form-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0 0 30px;
  text-align: center;
}

.register-page .register-card .form-section .form-container .register-form .custom-input :deep(.el-input__wrapper) {
  padding: 0 15px;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e4e7ed;
  transition: all 0.3s;
}

.register-page .register-card .form-section .form-container .register-form .custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc;
}

.register-page .register-card .form-section .form-container .register-form .custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409EFF;
}

.register-page .register-card .form-section .form-container .register-form .form-agreement {
  margin-bottom: 20px;
  font-size: 14px;
  color: #606266;
}

.register-page .register-card .form-section .form-container .register-form .register-btn {
  width: 100%;
  border-radius: 8px;
  height: 44px;
  font-weight: 500;
  margin-bottom: 20px;
  transition: all 0.3s;
}

.register-page .register-card .form-section .form-container .register-form .register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.register-page .register-card .form-section .form-container .register-form .register-btn .loading-text {
  margin-left: 8px;
}

.register-page .register-card .form-section .form-container .login-link {
  text-align: center;
  font-size: 14px;
  color: #909399;
}

.register-page .register-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 30px;
  font-size: 12px;
  color: #909399;
  position: relative;
  z-index: 1;
}

.register-page .register-footer .el-divider {
  background-color: #dcdfe6;
  margin: 0 10px;
}

.register-page .register-footer .el-link {
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .register-page .register-card {
    width: 100%;
    flex-direction: column;
    min-height: auto;
  }

  .register-page .register-card .brand-section {
    padding: 30px;
    display: none; /* 在小屏幕上隐藏品牌区 */
  }

  .register-page .register-card .brand-section .brand-content {
    max-width: 100%;
  }

  .register-page .register-card .form-section {
    padding: 40px 30px;
  }
}

@media (max-width: 576px) {
  .register-page {
    padding: 10px;
  }

  .register-page .register-card .form-section {
    padding: 30px 20px;
  }

  .register-page .register-footer {
    flex-direction: column;
    gap: 5px;
  }

  .register-page .register-footer .el-divider {
    display: none;
  }
}
</style>