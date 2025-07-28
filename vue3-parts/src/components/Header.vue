<template>
  <header class="app-header">
    <div class="header-container">
      <!-- Logo区域 -->
      <div class="logo-area" @click="goHome">
        <img
            src="../assets/images/logo.png"
            alt="Logo"
            class="logo-image"
        >
        <span class="logo-text">烈焰飞鸟·梦工厂</span>
      </div>

      <!-- 导航菜单 -->
      <nav class="nav-menu">
        <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            active-class="active"
        >
          {{ item.name }}
          <el-tag
              v-if="item.tag"
              :type="item.tag.type"
              size="small"
              effect="dark"
              class="nav-tag"
          >
            {{ item.tag.text }}
          </el-tag>
        </router-link>
      </nav>

      <!-- 右侧功能区 -->
      <div class="action-area">
        <!-- 搜索框 -->
        <el-input
            v-model="searchQuery"
            placeholder="搜索组件..."
            class="search-input"
            size="large"
            :prefix-icon="Search"
            clearable
        />

        <!-- 用户头像/登录 -->
        <div class="user-info">
          <el-dropdown>
            <div class="user-avatar">
              <el-avatar :size="36" :src="userAvatar" />
              <span class="user-name">{{ userName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
                <el-dropdown-item>设置</el-dropdown-item>
                <el-dropdown-item divided @click="gotoLogin">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <!-- 主题切换 -->
        <el-switch
            v-model="darkMode"
            class="theme-switch"
            inline-prompt
            :active-icon="Moon"
            :inactive-icon="Sunny"
            @change="toggleTheme"
        />
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  Search,
  Sunny,
  Moon,
  ArrowDown
} from '@element-plus/icons-vue'
import avatarImage from '@/assets/images/烈焰飞鸟.png'

const router = useRouter()

// 导航菜单项
const navItems = ref([
  { path: '/home', name: '首页' },
  { path: '/blogs', name: '博客', tag: { type: 'success', text: 'New' } },
  { path: '/templates', name: '模板' },
  { path: '/notes', name: '笔记' }
])

// 搜索查询
const searchQuery = ref('')

// 用户信息
const userName = ref('管理员')
const userAvatar = avatarImage

// 主题模式
const darkMode = ref(false)

const goHome = () => {
  router.push('/home')
}

const toggleTheme = (val) => {
  // 这里实现主题切换逻辑
  document.documentElement.classList.toggle('dark', val)
}

 const gotoLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.app-header {
  --header-height: 60px;
  --primary-color: #409eff;
  --bg-color: #ffffff;
  --text-color: #303133;
  --border-color: #e4e7ed;

  height: var(--header-height);
  background-color: var(--bg-color);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-container {
  height: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* Logo区域 */
.logo-area {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  &:hover {
    opacity: 0.8;
  }
}

.logo-image {
  width: 36px;
  height: 36px;
  margin-right: 12px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color);
  white-space: nowrap;
}

/* 导航菜单 */
.nav-menu {
  display: flex;
  height: 100%;
  margin-left: 40px;
}

.nav-item {
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 16px;
  color: var(--text-color);
  text-decoration: none;
  font-size: 15px;
  position: relative;
  transition: all 0.3s;

  &:hover {
    color: var(--primary-color);
  }

  &.active {
    color: var(--primary-color);
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 16px;
      right: 16px;
      height: 3px;
      background-color: var(--primary-color);
      border-radius: 3px 3px 0 0;
    }
  }
}

.nav-tag {
  margin-left: 6px;
  transform: translateY(-2px);
}

/* 右侧功能区 */
.action-area {
  display: flex;
  align-items: center;
  gap: 20px;
}

.search-input {
  width: 240px;
  transition: all 0.3s;

  &:focus-within {
    width: 280px;
  }
}

.user-info {
  cursor: pointer;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 14px;
  color: var(--text-color);
}

.theme-switch {
  --el-switch-on-color: #2c3e50;
  --el-switch-off-color: #dcdfe6;
}

/* 暗黑模式适配 */
.dark .app-header {
  --bg-color: #1f1f1f;
  --text-color: #e5eaf3;
  --border-color: #4c4d4f;
}
</style>