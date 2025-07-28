<template>
  <div class="sidebar-container">
    <!-- 顶部Logo区域 -->
    <div class="sidebar-logo">
      <div class="logo-wrapper">
        <img src="../assets/images/logo.png" alt="Logo" class="logo-img"/>
        <span class="logo-text">AI Vision Lab</span>
      </div>
    </div>

    <!-- 导航菜单 -->
    <div class="menu-scroll-container">
      <el-menu
          :default-active="activeIndex"
          class="sidebar-menu"
          :router="true"
          active-text-color="#409EFF"
          background-color="#304156"
          text-color="#b8c7ce"
          :collapse="isCollapse"
      >
        <!-- 我的项目 -->
        <el-sub-menu index="my-projects">
          <template #title>
            <el-icon>
              <Folder/>
            </el-icon>
            <span>我的项目</span>
          </template>
          <!-- 爱心页面 -->
          <el-menu-item index="/love-heart" route="/love-heart" class="gomoku-menu-item">
            <el-icon class="heart-icon">
              <StarFilled/>
            </el-icon>
            <template #title>
              <span>爱心特效</span>
              <el-tag size="small" effect="dark" type="danger" class="menu-tag">New</el-tag>
            </template>
          </el-menu-item>

          <!-- 五子棋 -->
          <el-menu-item
              index="/gomoku-game"
              route="/gomoku-game"
              class="gomoku-menu-item"
          >
            <el-icon class="five-icon">
              <svg viewBox="0 0 24 24" width="1em" height="1em">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z" fill="currentColor"/>
                <path d="M8 12c0 2.21 1.79 4 4 4s4-1.79 4-4-1.79-4-4-4-4 1.79-4 4zm4-2c1.1 0 2 .9 2 2s-.9 2-2 2-2-.9-2-2 .9-2 2-2z" fill="currentColor"/>
              </svg>
            </el-icon>
            <template #title>
              <span>五子棋</span>
              <el-tag size="small" effect="dark" type="info" class="menu-tag">策略</el-tag>
            </template>
          </el-menu-item>

          <!-- 404页面 -->
          <el-menu-item index="/404" route="/404" class="gomoku-menu-item">
            <el-icon>
              <Warning/>
            </el-icon>
            <template #title>
              <span>404测试页</span>
              <el-tag size="small" effect="dark" type="warning" class="menu-tag">测试</el-tag>
            </template>
          </el-menu-item>


          <!-- 手写数字识别 -->
          <el-menu-item index="/cnn-hand-write-number" route="/cnn-hand-write-number" class="gomoku-menu-item">
            <el-icon>
              <Cpu/>
            </el-icon>
            <template #title>
              <span>数字识别</span>
              <el-tag size="small" effect="dark" type="success" class="menu-tag">AI</el-tag>
            </template>
          </el-menu-item>

          <!-- CIFAR10分类菜单项 -->
          <el-menu-item index="/cifar10-classifier" route="/cifar10-classifier" class="gomoku-menu-item">
            <el-icon>
              <Picture/>
            </el-icon>
            <template #title>
              <span>CIFAR10分类</span>
              <el-tag size="small" effect="dark" type="success" class="menu-tag">AI</el-tag>
            </template>
          </el-menu-item>

        </el-sub-menu>

        <!-- 学习资源 -->
        <el-sub-menu index="learning-resources">
          <template #title>
            <el-icon>
              <Notebook/>
            </el-icon>
            <span>学习资源</span>
          </template>
          <el-menu-item index="/tutorials">
            <el-icon>
              <Document/>
            </el-icon>
            <span>教程文档</span>
          </el-menu-item>
          <el-menu-item index="/cheatsheets">
            <el-icon>
              <Memo/>
            </el-icon>
            <span>速查表</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </div>

    <!-- 折叠按钮 -->
    <div class="collapse-btn" @click="toggleCollapse">
      <el-icon>
        <component :is="isCollapse ? Expand : Fold"/>
      </el-icon>
      <span v-show="!isCollapse">收起菜单</span>
    </div>
  </div>
</template>

<script setup>
import {ref, computed} from 'vue'
import {useRoute} from 'vue-router'
import {
  Folder,
  Warning,
  StarFilled,
  Cpu,
  Notebook,
  Document,
  Memo,
  Fold,
  Expand,
  Picture,
} from '@element-plus/icons-vue'

const route = useRoute()
const isCollapse = ref(false)

const activeIndex = computed(() => {
  return route.path
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}
</script>

<style scoped>
.sidebar-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 60px);
  background-color: #304156;
  position: fixed;
  left: 0;
  top: 60px;
  bottom: 0;
  z-index: 10;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
  width: 220px;
  overflow: hidden;
}

.sidebar-logo {
  flex-shrink: 0;
  height: 60px;
  min-height: 60px;
  line-height: 60px;
  padding: 0 20px;
  background-color: #2b2f3a;
  overflow: hidden;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  height: 100%;
}

.logo-img {
  width: 32px;
  height: 32px;
  margin-right: 12px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
  white-space: nowrap;
}

.menu-scroll-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: auto;
  width: 100%;
}

.sidebar-menu {
  min-width: 200px;
  width: 100%;
  border-right: none !important;
  overflow: visible;
}

.el-menu {
  border-right: none !important;
}

.el-menu-item {
  height: 48px;
  line-height: 48px;
  font-size: 14px;
  color: #b8c7ce !important;
  margin: 4px 8px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  min-width: calc(100% - 16px);
  white-space: nowrap;
}

.el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.1) !important;
  color: #ffffff !important;
}

.el-menu-item.is-active {
  background-color: rgba(64, 158, 255, 0.15) !important;
  color: #409EFF !important;
  border-right: 3px solid #409EFF;
}

.el-menu-item [class^="el-icon"] {
  margin-right: 12px;
  font-size: 18px;
  vertical-align: middle;
}

.menu-tag {
  margin-left: 8px;
  transform: translateY(-1px);
  transition: all 0.3s;
}

.el-menu-item:hover .menu-tag {
  transform: translateY(-2px);
}

.heart-icon {
  color: #f56c6c;
  transition: all 0.3s;
}

.el-menu-item:hover .heart-icon {
  transform: scale(1.2) rotate(15deg);
}

.collapse-btn {
  flex-shrink: 0;
  height: 48px;
  min-height: 48px;
  padding: 0 24px;
  cursor: pointer;
  color: #b8c7ce;
  display: flex;
  align-items: center;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  width: 100%;
}

.collapse-btn:hover {
  color: #ffffff;
  background-color: rgba(255, 255, 255, 0.1);
}

.collapse-btn span {
  margin-left: 10px;
  font-size: 14px;
}

.collapse-btn .el-icon {
  font-size: 18px;
}

.el-menu--collapse {
  width: 64px !important;
}

.el-menu--collapse .el-menu-item {
  width: auto;
  padding: 0 16px !important;
}

.el-menu--collapse .el-menu-item [class^="el-icon"] {
  margin-right: 0;
}

.el-menu--collapse .menu-tag,
.el-menu--collapse .collapse-btn span,
.el-menu--collapse .el-sub-menu__title span {
  display: none;
}

.el-menu--collapse .sidebar-logo .logo-text {
  display: none;
}

.el-menu--collapse .sidebar-logo {
  padding: 0 10px;
}

.menu-scroll-container::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.menu-scroll-container::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.menu-scroll-container::-webkit-scrollbar-track {
  background-color: transparent;
}

/* 子菜单样式 */
.el-sub-menu__title {
  height: 48px !important;
  line-height: 48px !important;
}

.el-sub-menu.is-active .el-sub-menu__title {
  color: #409EFF !important;
}

.el-menu-item [class^="el-icon"],
.el-sub-menu__title [class^="el-icon"] {
  transition: transform 0.2s;
}

.el-menu-item:hover [class^="el-icon"],
.el-sub-menu__title:hover [class^="el-icon"] {
  transform: scale(1.1);
}

.el-menu-item {
  padding-left: 50px !important;
}

.el-menu--collapse .el-sub-menu__title {
  padding-left: 20px !important;
}

/* 横向滚动提示 */
.menu-scroll-container {
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.2) transparent;
}

.menu-scroll-container:hover {
  overflow-x: auto;
}

/* 防止折叠模式下出现滚动 */
.el-menu--collapse + .menu-scroll-container {
  overflow-x: hidden !important;
}

/* 五子棋图标特殊样式 */
.five-icon {
  color: #9c27b0; /* 紫色主题 */
  transition: all 0.3s;
}

.gomoku-menu-item:hover .five-icon {
  transform: rotate(15deg) scale(1.1);
  color: #7b1fa2;
}

/* 五子棋菜单项动画 */
.gomoku-menu-item {
  position: relative;
  overflow: hidden;
}
.gomoku-menu-item::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #9c27b0, #2196f3);
  transition: all 0.3s;
}
.gomoku-menu-item:hover::after {
  left: 0;
  width: 100%;
}
</style>