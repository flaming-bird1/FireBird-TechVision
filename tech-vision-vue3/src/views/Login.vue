<template>
  <div class="login-page">
    <!-- 背景装饰元素 -->
    <div class="bg-shapes">
      <div class="shape-1"></div>
      <div class="shape-2"></div>
      <div class="shape-3"></div>
    </div>

    <!-- 主登录卡片 -->
    <div class="login-card">
      <!-- 左侧品牌展示区 -->
      <div class="brand-section">
        <div class="brand-content">
          <h1 class="brand-title">焰翼码栈</h1>
          <p class="brand-subtitle">展示我的技术探索之旅</p>

          <!-- 特色功能展示 -->
          <div class="features">
            <div class="feature-item">
              <el-icon :size="20" color="#409EFF"><MagicStick /></el-icon>
              <span>AI项目展示</span>
            </div>
            <div class="feature-item">
              <el-icon :size="20" color="#67C23A"><Cpu /></el-icon>
              <span>深度学习实践</span>
            </div>
            <div class="feature-item">
              <el-icon :size="20" color="#E6A23C"><DataAnalysis /></el-icon>
              <span>前端技术探索</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="form-section">
        <div class="form-container">
          <h2 class="form-title">欢迎回来</h2>
          <p class="form-subtitle">请登录您的账户</p>

          <!-- 社交登录按钮 -->
          <div class="social-login">
            <el-button
                circle
                class="social-btn github"
                @click="socialLogin('github')"
            >
            </el-button>
            <el-button
                circle
                class="social-btn google"
                @click="socialLogin('google')"
            >
            </el-button>
            <el-button
                circle
                class="social-btn wechat"
                @click="socialLogin('wechat')"
            >
            </el-button>
          </div>

          <div class="divider">
            <span class="divider-line"></span>
            <span class="divider-text">或</span>
            <span class="divider-line"></span>
          </div>

          <!-- 登录表单 -->
          <el-form
              ref="loginFormRef"
              :model="loginForm"
              :rules="loginRules"
              @keyup.enter="handleLogin"
              class="login-form"
          >
            <el-form-item prop="email">
              <el-input
                  v-model="loginForm.email"
                  placeholder="邮箱"
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
                  v-model="loginForm.password"
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

            <div class="form-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <el-link
                  type="primary"
                  underline="never"
                  @click="router.push('/forgot-password')"
              >
                忘记密码?
              </el-link>
            </div>

            <el-button
                type="primary"
                size="large"
                class="login-btn"
                :loading="loading"
                @click="handleLogin"
            >
              登录
              <template #loading>
                <span class="loading-text">登录中...</span>
              </template>
            </el-button>
          </el-form>

          <div class="register-link">
            还没有账号?
            <el-link
                type="primary"
                underline="never"
                @click="router.push('/register')"
            >
              立即注册
            </el-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 页脚 -->
    <div class="login-footer">
      <span>© 2025 焰翼码栈 版权所有</span>
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

const loginForm = ref({
  email: '2739967874@qq.com',
  password: '123456'
})

const loginFormRef = ref(null)
const rememberMe = ref(false)
const loading = ref(false)

const loginRules = ref({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
})

const handleLogin = async () => {
  try {
    // 验证表单
    await loginFormRef.value.validate()

    loading.value = true
    const response = await axios.post('/spring/login', {
      email: loginForm.value.email,
      password: loginForm.value.password
    })

    if (response.data.code === 200) {
      ElMessage.success('登录成功')
      // 存储token和用户信息
      localStorage.setItem('token', response.data.data.token)
      localStorage.setItem('userInfo', JSON.stringify(response.data.data.userInfo))

      // 跳转到首页
      await router.push('/home')
    } else {
      ElMessage.error(response.data.msg || '登录失败')
    }
  } catch (error) {
    if (error.response) {
      // 处理HTTP错误响应
      const errorMsg = error.response.data?.msg || '登录请求失败'
      ElMessage.error(errorMsg)
    } else if (error.message && !error.message.includes('validation')) {
      // 表单验证错误不显示消息，其他错误显示
      ElMessage.error(error.message)
    }
  } finally {
    loading.value = false
  }
}

const socialLogin = (provider) => {
  ElMessage.info(`即将通过${provider}登录`)
  // 实际项目中这里会跳转到对应的OAuth认证页面
}
</script>

<style scoped>
.login-page {
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

.login-page .bg-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.login-page .bg-shapes .shape-1 {
  position: absolute;
  top: -100px;
  right: -100px;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: linear-gradient(45deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0) 100%);
}

.login-page .bg-shapes .shape-2 {
  position: absolute;
  bottom: -150px;
  left: -150px;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: linear-gradient(45deg, rgba(103, 194, 58, 0.1) 0%, rgba(103, 194, 58, 0) 100%);
}

.login-page .bg-shapes .shape-3 {
  position: absolute;
  top: 50%;
  right: 50%;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: linear-gradient(45deg, rgba(230, 162, 60, 0.1) 0%, rgba(230, 162, 60, 0) 100%);
  transform: translate(50%, -50%);
}

.login-page .login-card {
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

.login-page .login-card .brand-section {
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

.login-page .login-card .brand-section .brand-content {
  max-width: 320px;
}

.login-page .login-card .brand-section .brand-content .brand-logo {
  width: 80px;
  height: 80px;
  margin-bottom: 20px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  padding: 10px;
}

.login-page .login-card .brand-section .brand-content .brand-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 10px;
}

.login-page .login-card .brand-section .brand-content .brand-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 30px;
}

.login-page .login-card .brand-section .brand-content .features {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 40px;
}

.login-page .login-card .brand-section .brand-content .features .feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(255, 255, 255, 0.1);
  padding: 12px 20px;
  border-radius: 8px;
  transition: all 0.3s;
}

.login-page .login-card .brand-section .brand-content .features .feature-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateX(5px);
}

.login-page .login-card .brand-section .brand-content .features .feature-item span {
  font-size: 14px;
}

.login-page .login-card .form-section {
  flex: 1;
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-page .login-card .form-section .form-container {
  max-width: 360px;
  margin: 0 auto;
  width: 100%;
}

.login-page .login-card .form-section .form-container .form-title {
  font-size: 24px;
  color: #303133;
  margin: 0 0 5px;
  text-align: center;
}

.login-page .login-card .form-section .form-container .form-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0 0 30px;
  text-align: center;
}

.login-page .login-card .form-section .form-container .social-login {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-bottom: 30px;
}

.login-page .login-card .form-section .form-container .social-login .social-btn {
  width: 44px;
  height: 44px;
  font-size: 20px;
  border: none;
  transition: all 0.3s;
}

.login-page .login-card .form-section .form-container .social-login .social-btn:hover {
  transform: translateY(-3px);
}

.login-page .login-card .form-section .form-container .social-login .social-btn.github {
  background: #24292e;
  color: white;
}

.login-page .login-card .form-section .form-container .social-login .social-btn.google {
  background: #db4437;
  color: white;
}

.login-page .login-card .form-section .form-container .social-login .social-btn.wechat {
  background: #07C160;
  color: white;
}

.login-page .login-card .form-section .form-container .divider {
  display: flex;
  align-items: center;
  margin: 20px 0;
  color: #909399;
  font-size: 12px;
}

.login-page .login-card .form-section .form-container .divider .divider-line {
  flex: 1;
  height: 1px;
  background: #e4e7ed;
}

.login-page .login-card .form-section .form-container .divider .divider-text {
  padding: 0 10px;
}

.login-page .login-card .form-section .form-container .login-form .custom-input :deep(.el-input__wrapper) {
  padding: 0 15px;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e4e7ed;
  transition: all 0.3s;
}

.login-page .login-card .form-section .form-container .login-form .custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc;
}

.login-page .login-card .form-section .form-container .login-form .custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409EFF;
}

.login-page .login-card .form-section .form-container .login-form .form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.login-page .login-card .form-section .form-container .login-form .login-btn {
  width: 100%;
  border-radius: 8px;
  height: 44px;
  font-weight: 500;
  margin-bottom: 20px;
  transition: all 0.3s;
}

.login-page .login-card .form-section .form-container .login-form .login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.login-page .login-card .form-section .form-container .login-form .login-btn .loading-text {
  margin-left: 8px;
}

.login-page .login-card .form-section .form-container .register-link {
  text-align: center;
  font-size: 14px;
  color: #909399;
}

.login-page .login-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 30px;
  font-size: 12px;
  color: #909399;
  position: relative;
  z-index: 1;
}

.login-page .login-footer .el-divider {
  background-color: #dcdfe6;
  margin: 0 10px;
}

.login-page .login-footer .el-link {
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .login-page .login-card {
    width: 100%;
    flex-direction: column;
    min-height: auto;
  }

  .login-page .login-card .brand-section {
    padding: 30px;
    display: none; /* 在小屏幕上隐藏品牌区 */
  }

  .login-page .login-card .brand-section .brand-content {
    max-width: 100%;
  }

  .login-page .login-card .form-section {
    padding: 40px 30px;
  }
}

@media (max-width: 576px) {
  .login-page {
    padding: 10px;
  }

  .login-page .login-card .form-section {
    padding: 30px 20px;
  }

  .login-page .login-card .form-section .form-container .social-login {
    margin-bottom: 20px;
  }

  .login-page .login-footer {
    flex-direction: column;
    gap: 5px;
  }

  .login-page .login-footer .el-divider {
    display: none;
  }
}
</style>