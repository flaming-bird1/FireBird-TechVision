<template>
  <div class="word-layout">
    <!-- 顶部导航栏 -->
    <header class="layout-header">
      <div class="header-content">
        <!-- Logo和标题 -->
        <div class="logo-title" @click="goHome">
          <img
              src="../assets/images/logo.png"
              alt="Logo"
              class="logo-image"
          >
          <h1 class="system-title">单词学习系统</h1>
        </div>

        <!-- 用户信息 -->
        <div class="user-info">
          <el-dropdown>
            <span class="el-dropdown-link">
              <el-avatar :size="36" :src="userAvatar" class="user-avatar">{{ userInitials }}</el-avatar>
              <span class="user-name">欢迎，{{ userName }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleProfile">个人资料</el-dropdown-item>
                <el-dropdown-item @click="handleSettings">系统设置</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <div class="layout-main">
      <!-- 侧边栏导航 -->
      <aside class="sidebar">
        <!-- 返回首页按钮 -->
        <div class="sidebar-header">
          <router-link to="/home" class="back-button">
            <el-icon><HomeFilled /></el-icon>
            <span>返回首页</span>
          </router-link>
        </div>

        <nav class="sidebar-nav">
          <router-link
              v-for="item in navItems"
              :key="item.path"
              :to="item.path"
              class="nav-item"
              :class="{ active: isActive(item.path) }"
          >
            <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
            <span class="nav-text">{{ item.text }}</span>
          </router-link>
        </nav>
      </aside>

      <!-- 内容区域 -->
      <main class="content-area">
        <div class="content-wrapper">
          <router-view />
        </div>
      </main>
    </div>

    <!-- 底部信息 -->
    <footer class="layout-footer">
      <p>© 2025 单词学习系统 - 让学习更高效</p>
    </footer>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  HomeFilled,
  Reading,
  Refresh,
  Document,
  Collection,
  DataAnalysis,
  Setting,
  ArrowDown,
  User,
  Notebook
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'WordLayout',
  components: {
    HomeFilled,
    ArrowDown
  },
  setup() {
    const route = useRoute()
    const router = useRouter()

    // 用户信息
    const userInfo = ref({})
    const userName = ref('学习者')
    const userAvatar = ref('')
    const userInitials = ref('')

    // 导航菜单项
    const navItems = ref([
      { path: '/word-study-system/home', icon: HomeFilled, text: '首页' },
      { path: '/word-study-system/learn', icon: Reading, text: '学习' },
      { path: '/word-study-system/collection', icon: Notebook, text: '单词本' },
      { path: '/word-study-system/statistics', icon: DataAnalysis, text: 'PK' },
    ])

    // 检查当前激活的路由
    const isActive = computed(() => (path) => {
      return route.path === path
    })

    // 从localStorage获取用户信息
    const getUserInfo = () => {
      try {
        const userInfoStr = localStorage.getItem('userInfo')
        if (userInfoStr) {
          userInfo.value = JSON.parse(userInfoStr)

          // 设置用户名
          if (userInfo.value.username) {
            userName.value = userInfo.value.username
          } else if (userInfo.value.email) {
            userName.value = userInfo.value.email.split('@')[0]
          }

          // 设置用户头像或 initials
          if (userInfo.value.avatar) {
            userAvatar.value = userInfo.value.avatar
          } else if (userName.value) {
            // 生成用户 initials
            userInitials.value = userName.value.charAt(0).toUpperCase()
          }
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        ElMessage.error('用户信息加载失败')
      }
    }

    // 返回首页
    const goHome = () => {
      router.push('/home')
    }

    // 处理个人资料
    const handleProfile = () => {
      console.log('打开个人资料')
      router.push('/profile')
    }

    // 处理设置
    const handleSettings = () => {
      console.log('打开设置')
      router.push('/settings')
    }

    // 退出登录
    const handleLogout = () => {
      // 清除本地存储的token和用户信息
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')

      ElMessage.success('已退出登录')
      router.push('/login')
    }

    // 组件挂载时获取用户信息
    onMounted(() => {
      getUserInfo()
    })

    return {
      navItems,
      userName,
      userAvatar,
      userInitials,
      isActive,
      goHome,
      handleProfile,
      handleSettings,
      handleLogout
    }
  }
}
</script>

<style scoped>
.word-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4efe9 100%);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* 头部样式 */
.layout-header {
  background: linear-gradient(90deg, #4361ee 0%, #3a0ca3 100%);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  padding: 0.6rem 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 2rem;
}

.logo-title {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.3s;
}

.logo-title:hover {
  transform: scale(1.02);
}

.logo-image {
  width: 42px;
  height: 42px;
  object-fit: contain;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.system-title {
  font-size: 1.6rem;
  color: white;
  font-weight: 700;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.user-info {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  color: white;
  cursor: pointer;
  gap: 8px;
}

.user-avatar {
  background-color: #7209b7;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.user-name {
  color: white;
  font-size: 0.95rem;
}

/* 主内容区样式 */
.layout-main {
  display: flex;
  flex: 1;
  max-width: 1280px;
  margin: 2rem auto;
  padding: 0 2rem;
  width: 100%;
  gap: 2rem;
}

/* 侧边栏样式 */
.sidebar {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  background: linear-gradient(135deg, #4cc9f0 0%, #4361ee 100%);
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(67, 97, 238, 0.3);
  overflow: hidden;
  margin-bottom: 1.5rem;
  padding: 1.2rem;
}

.back-button {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: white;
  font-weight: 500;
  padding: 0.8rem;
  border-radius: 10px;
  transition: background-color 0.3s;
  width: 100%;
  justify-content: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.15);
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.25);
}

.sidebar-nav {
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  margin-bottom: 1.5rem;
  flex-grow: 1;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 1.2rem 1.8rem;
  text-decoration: none;
  color: #2b2d42;
  border-left: 4px solid transparent;
  transition: all 0.3s ease;
}

.nav-item:hover {
  background-color: #f0f7ff;
  color: #4361ee;
  transform: translateX(5px);
}

.nav-item.active {
  background: linear-gradient(90deg, rgba(67, 97, 238, 0.1) 0%, rgba(67, 97, 238, 0.05) 100%);
  color: #4361ee;
  border-left-color: #4361ee;
  font-weight: 600;
}

.nav-icon {
  font-size: 1.3rem;
  margin-right: 1rem;
  width: 24px;
  text-align: center;
  color: inherit;
}

.nav-text {
  font-weight: 500;
  font-size: 1rem;
}

/* 内容区域样式 */
.content-area {
  flex: 1;
  min-width: 0;
}

.content-wrapper {
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
  padding: 2.5rem;
  min-height: 600px;
}

/* 底部样式 */
.layout-footer {
  background: linear-gradient(90deg, #4361ee 0%, #3a0ca3 100%);
  padding: 1.5rem 0;
  text-align: center;
  color: white;
  margin-top: auto;
  font-size: 0.9rem;
}

/* 响应式设计 */
@media (max-width: 968px) {
  .layout-main {
    flex-direction: column;
    padding: 1rem;
    margin: 1rem auto;
  }

  .sidebar {
    width: 100%;
    order: 2;
  }

  .content-area {
    order: 1;
  }

  .header-content {
    padding: 0 1rem;
  }

  .system-title {
    font-size: 1.4rem;
  }
}

@media (max-width: 640px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
    padding: 0.5rem;
  }

  .logo-title {
    order: -1;
  }

  .content-wrapper {
    padding: 1.5rem;
  }

  .sidebar-nav {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 0.5rem;
    padding: 1rem;
  }

  .nav-item {
    border-left: none;
    border-bottom: 3px solid transparent;
    padding: 1rem;
    flex-direction: column;
    text-align: center;
    border-radius: 10px;
  }

  .nav-item.active {
    border-left-color: transparent;
    border-bottom-color: #4361ee;
  }

  .nav-icon {
    margin-right: 0;
    margin-bottom: 0.5rem;
    font-size: 1.5rem;
  }

  .nav-text {
    font-size: 0.85rem;
  }

  .user-name {
    display: none;
  }
}
</style>