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
        <span class="logo-text">焰翼码栈</span>
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
              <el-avatar :size="36" :src="userAvatar"/>
              <span class="user-name">{{ userName }}</span>
              <el-icon>
                <ArrowDown/>
              </el-icon>
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

        <!-- 音乐播放按钮 -->
        <el-button @click="toggleMusic" class="music-button">
          <el-icon v-if="playerReady && musicPlayerRef.isPlaying">
            <svg class="icon" viewBox="0 0 1024 1024" width="200"
                 height="200">
              <path
                  d="M512.449956 124.087882c52.394883 0 103.189923 10.298994 150.985255 30.497022 46.195489 19.498096 87.691436 47.495362 123.287961 83.191876 35.696514 35.696514 63.59379 77.192462 83.191875 123.28796 20.198028 47.795332 30.497022 98.590372 30.497022 150.985255s-10.298994 103.189923-30.497022 150.985255c-19.498096 46.195489-47.495362 87.691436-83.191875 123.287961-35.696514 35.696514-77.192462 63.59379-123.287961 83.191875-47.795332 20.198028-98.590372 30.497022-150.985255 30.497022s-103.189923-10.298994-150.985255-30.497022c-46.195489-19.498096-87.691436-47.495362-123.28796-83.191875-35.696514-35.696514-77.192462-77.192462-123.28796-123.28796-20.198028-47.795332-30.497022-98.590372-30.497022-150.985255s10.298994-103.189923 30.497022-150.985255c19.498096-46.195489 47.495362-87.691436 83.191876-123.28796 35.696514-35.696514 77.192462-63.59379 123.28796-83.191876 47.795332-20.198028 98.590372-30.497022 150.985255-30.497022m0-59.994141c-247.375842 0-447.956254 200.580412-447.956254 447.956254s200.580412 447.956254 447.956254 447.956254 447.956254-200.580412 447.956254-447.956254-200.580412-447.956254-447.956254-447.956254z"
                  fill="#FF6600" opacity=".502"></path>
              <path
                  d="M432.457768 322.068548c-16.598379 0-29.997071 13.398692-29.997071 29.997071v319.968753c0 16.598379 13.398692 29.997071 29.997071 29.99707s29.997071-13.398692 29.99707-29.99707v-319.968753c0-16.498389-13.398692-29.997071-29.99707-29.997071zM592.442144 322.068548c-16.598379 0-29.997071 13.398692-29.99707 29.997071v319.968753c0 16.598379 13.398692 29.997071 29.99707 29.99707s29.997071-13.398692 29.997071-29.99707v-319.968753c0-16.498389-13.398692-29.997071-29.997071-29.997071z"
                  fill="#FF6600"></path>
            </svg>
          </el-icon>
          <el-icon v-else-if="playerReady && !musicPlayerRef.isPlaying">
            <svg class="icon" viewBox="0 0 1024 1024" width="200"
                 height="200">
              <path
                  d="M380.677 176.279l390.182 225.272c85.024 49.089 85.024 171.81 0 220.899L380.677 847.721c-85.024 49.089-191.304-12.272-191.304-110.449V286.728c0-98.177 106.28-159.538 191.304-110.449z"
                  fill="#FF7B15"></path>
            </svg>
          </el-icon>
          <el-icon v-else>
            <Loading />
          </el-icon>
          <span>{{ playerReady ? (musicPlayerRef.isPlaying ? '暂停音乐' : '播放音乐') : '加载中...' }}</span>
        </el-button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, nextTick, inject } from 'vue';
import { useRouter } from 'vue-router';
import {
  Search,
  Sunny,
  Moon,
  ArrowDown,
  Loading
} from '@element-plus/icons-vue';
import avatarImage from '@/assets/images/烈焰飞鸟.png';
import { ElMessage } from 'element-plus';

const router = useRouter();
// 从父组件注入音乐播放器实例
const musicPlayer = inject('musicPlayer');
const musicPlayerRef = ref(null); // 存储实际的播放器实例
const playerReady = ref(false);

// 导航菜单项
const navItems = ref([
  { path: '/home', name: '首页' },
  { path: '/blogs', name: '博客', tag: { type: 'success', text: 'New' } },
  { path: '/project-view', name: '项目' },
  { path: '/notes', name: '笔记' },
]);

// 搜索查询
const searchQuery = ref('');

// 用户信息
const userName = ref('管理员');
const userAvatar = avatarImage;

// 主题模式
const darkMode = ref(false);

const goHome = () => {
  router.push('/home');
};

const toggleTheme = (val) => {
  document.documentElement.classList.toggle('dark', val);
};

const gotoLogin = () => {
  router.push('/login');
};

const toggleMusic = () => {
  if (playerReady.value && musicPlayerRef.value) {
    musicPlayerRef.value.toggleMusic();
  }
};

onMounted(() => {
  // 等待DOM更新，确保注入的播放器实例已初始化
  nextTick(() => {
    if (musicPlayer && musicPlayer.value) {
      musicPlayerRef.value = musicPlayer.value;
      playerReady.value = true;
    } else {
      console.error('无法获取音乐播放器实例，请检查App.vue是否正确提供了实例');
      ElMessage.error('音乐播放器初始化失败');
    }
  });
});
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

.music-button {
  min-width: 100px;
}

/* 暗黑模式适配 */
.dark .app-header {
  --bg-color: #1f1f1f;
  --text-color: #e5eaf3;
  --border-color: #4c4d4f;
}
</style>