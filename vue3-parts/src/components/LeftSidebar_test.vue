<template>
  <div class="sidebar-container">
    <!-- 顶部Logo区域 -->
    <div class="sidebar-logo">
      <div class="logo-wrapper">
        <img src="../assets/images/logo.png" alt="Logo" class="logo-img"/>
        <!--        <span class="logo-text">FlameWing CodeForge</span>-->
      </div>
    </div>

    <!-- 导航菜单 -->
    <div class="menu-scroll-container">
      <div class="menu-outer-container">
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


            <!-- 404页面 -->
            <el-menu-item index="/404" route="/404" class="gomoku-menu-item">
              <el-icon class="dynamic-icon">
                <svg viewBox="0 0 1024 1024" width="200" height="200">
                  <path
                      d="M931.6 585.6v79c28.6-60.2 44.8-127.4 44.8-198.4C976.4 211 769.4 4 514.2 4S52 211 52 466.2c0 3.2 0.2 6.4 0.2 9.6l166-206h96.4L171.8 485.6h46.4v-54.8l99.2-154.6V668h-99.2v-82.4H67.6c43 161 170.6 287.4 332.4 328.6-10.4 26.2-40.6 89.4-90.8 100.6-62.2 14 168.8 3.4 333.6-104.6C769.4 873.6 873.6 784.4 930.2 668h-97.6v-82.4H666.4V476l166.2-206.2h94L786.2 485.6h46.4v-59l99.2-154v313zM366.2 608c-4.8-11.2-7.2-23.2-7.2-36V357.6c0-12.8 2.4-24.8 7.2-36 4.8-11.2 11.4-21 19.6-29.2 8.2-8.2 18-14.8 29.2-19.6 11.2-4.8 23.2-7.2 36-7.2h81.6c12.8 0 24.8 2.4 36 7.2 11 4.8 20.6 11.2 28.8 19.2l-88.6 129.4v-23c0-4.8-1.6-8.8-4.8-12-3.2-3.2-7.2-4.8-12-4.8s-8.8 1.6-12 4.8c-3.2 3.2-4.8 7.2-4.8 12v72L372.6 620c-2.4-3.8-4.6-7.8-6.4-12z m258.2-36c0 12.8-2.4 24.8-7.2 36-4.8 11.2-11.4 21-19.6 29.2-8.2 8.2-18 14.8-29.2 19.6-11.2 4.8-23.2 7.2-36 7.2h-81.6c-12.8 0-24.8-2.4-36-7.2-11.2-4.8-21-11.4-29.2-19.6-3.6-3.6-7-7.8-10-12l99.2-144.6v50.6c0 4.8 1.6 8.8 4.8 12 3.2 3.2 7.2 4.8 12 4.8s8.8-1.6 12-4.8c3.2-3.2 4.8-7.2 4.8-12v-99.6L601 296.4c6.6 7.4 12 15.8 16 25.2 4.8 11.2 7.2 23.2 7.2 36V572z"
                      fill="#e73725"/>
                </svg>
              </el-icon>
              <template #title>
                <span>404测试页</span>
                <el-tag size="small" effect="dark" type="warning" class="menu-tag">测试</el-tag>
              </template>
            </el-menu-item>


            <!-- CIFAR10分类菜单项 -->
            <el-menu-item index="/cifar10-classifier" route="/cifar10-classifier" class="gomoku-menu-item">
              <el-icon class="dynamic-icon">
                <svg viewBox="0 0 1024 1024" width="200" height="200">
                  <path
                      d="M329.142857 438.857143c40.393143 0 73.142857-32.749714 73.142857-73.142857 0-40.411429-32.749714-73.142857-73.142857-73.142857-40.356571 0-73.142857 32.731429-73.142857 73.142857C256 406.107429 288.786286 438.857143 329.142857 438.857143z"
                      fill="#F69661"></path>
                  <path
                      d="M658.285714 393.142857 441.536 698.784914 329.142857 542.749257 146.285714 804.571429 877.714286 804.571429Z"
                      fill="#F69661"></path>
                  <path
                      d="M950.857143 91.428571 73.142857 91.428571c-20.214857 0-36.571429 16.374857-36.571429 36.571429l0 768c0 20.214857 16.356571 36.571429 36.571429 36.571429l877.714286 0c20.214857 0 36.571429-16.356571 36.571429-36.571429L987.428571 128C987.428571 107.803429 971.072 91.428571 950.857143 91.428571zM914.285714 859.428571 109.714286 859.428571 109.714286 164.571429l804.571429 0L914.285714 859.428571z"
                      fill="#F69661"></path>
                </svg>
              </el-icon>
              <template #title>
                <span>CIFAR10分类</span>
                <el-tag size="small" effect="dark" type="success" class="menu-tag">AI</el-tag>
              </template>
            </el-menu-item>

            <!--AI对话-->
            <el-menu-item index="/ai-chat" route="/ai-chat" class="gomoku-menu-item">
              <el-icon class="dynamic-icon">
                <svg viewBox="0 0 1024 1024"
                     width="200" height="200">
                  <path
                      d="M832 839.9H203.3c-10.8 0-19.6-8.8-19.6-19.6V547.5c0-109.8 89.3-199.2 199.2-199.2h269.4c109.8 0 199.2 89.4 199.2 199.2v272.8c0.1 10.8-8.7 19.6-19.5 19.6z m-609.1-39.2h589.5V547.5c0-88.2-71.8-160-160-160H382.9c-88.2 0-160 71.8-160 160v253.2z"
                      fill="#1E94FC"></path>
                  <path
                      d="M665.8 609.2H369.5c-33.1 0-60-26.9-60-60s26.9-60 60-60h296.4c33.1 0 60 26.9 60 60-0.1 33.1-27 60-60.1 60z m-296.3-80.8c-11.5 0-20.8 9.3-20.8 20.8S358 570 369.5 570h296.4c11.5 0 20.8-9.4 20.8-20.8 0-11.5-9.3-20.8-20.8-20.8H369.5z"
                      fill="#28E3C4"></path>
                  <path
                      d="M192.1 730.4h-44.9c-15.8 0-28.6-12.8-28.6-28.6V612c0-15.8 12.8-28.6 28.6-28.6h44.9c10.8 0 19.6 8.8 19.6 19.6s-8.8 19.6-19.6 19.6h-34.3v68.6h34.3c10.8 0 19.6 8.8 19.6 19.6s-8.8 19.6-19.6 19.6zM883.6 730.4h-44.9c-10.8 0-19.6-8.8-19.6-19.6s8.8-19.6 19.6-19.6H873v-68.6h-34.3c-10.8 0-19.6-8.8-19.6-19.6s8.8-19.6 19.6-19.6h44.9c15.8 0 28.6 12.8 28.6 28.6v89.8c0 15.8-12.8 28.6-28.6 28.6zM627.7 384.7c-4.4 0-8.9-1.5-12.6-4.6-8.3-6.9-9.4-19.3-2.4-27.6l139.2-166.1c6.9-8.3 19.3-9.4 27.6-2.4 8.3 6.9 9.4 19.3 2.4 27.6L642.7 377.7c-3.9 4.6-9.4 7-15 7zM403.1 384.7c-5.6 0-11.1-2.4-15-7L248.9 211.5c-7-8.3-5.9-20.7 2.4-27.6 8.3-7 20.6-5.9 27.6 2.4l139.2 166.1c7 8.3 5.9 20.7-2.4 27.6-3.6 3.2-8.1 4.7-12.6 4.7z"
                      fill="#1E94FC"></path>
                  <path
                      d="M780.3 218.9c-23.4 0-42.5-19-42.5-42.5s19-42.5 42.5-42.5 42.5 19 42.5 42.5-19 42.5-42.5 42.5z m0-44.9c-1.4 0-2.5 1.1-2.5 2.5s1.1 2.5 2.5 2.5 2.5-1.1 2.5-2.5-1.1-2.5-2.5-2.5zM250.5 218.9c-23.4 0-42.5-19-42.5-42.5s19-42.5 42.5-42.5 42.5 19 42.5 42.5-19.1 42.5-42.5 42.5z m0-44.9c-1.4 0-2.5 1.1-2.5 2.5s1.1 2.5 2.5 2.5 2.5-1.1 2.5-2.5-1.2-2.5-2.5-2.5z"
                      fill="#1E94FC"></path>
                </svg>
              </el-icon>
              <template #title>
                <span>AI对话</span>
                <el-tag size="small" effect="dark" type="success" class="menu-tag">AI</el-tag>
              </template>
            </el-menu-item>


            <!-- GeoJsonToShapefile-->
            <el-menu-item index="GeoJson-to-shapefile" route="/GeoJson-to-shapefile" class="gomoku-menu-item">
              <el-icon class="dynamic-icon">
                <svg t="1755235884118" class="icon" viewBox="0 0 1024 1024" version="1.1"
                     xmlns="http://www.w3.org/2000/svg" p-id="4963" width="200" height="200">
                  <path
                      d="M510.4 64c-247.4 0-448 200.6-448 448s200.6 448 448 448 448-200.6 448-448-200.6-448-448-448zM806 807.6c-38.4 38.4-83.1 68.5-132.9 89.6C621.6 919 566.8 930 510.4 930s-111.2-11-162.7-32.8c-49.8-21-94.5-51.2-132.9-89.6-38.4-38.4-68.5-83.1-89.6-132.9-21.8-51.5-32.8-106.3-32.8-162.7s11.1-111.2 32.8-162.7c21.1-49.8 51.2-94.5 89.6-132.9 38.4-38.4 83.1-68.5 132.9-89.6C399.2 105.1 454 94 510.4 94s111.2 11.1 162.7 32.8c49.8 21.1 94.5 51.2 132.9 89.6 38.4 38.4 68.5 83.1 89.6 132.9 21.8 51.5 32.8 106.3 32.8 162.7 0 56.4-11 111.2-32.8 162.7-21.1 49.8-51.2 94.5-89.6 132.9z"
                      fill="#2264F6" ></path>
                  <path
                      d="M830.1 511.7c0-176.6-143.3-319.8-320-319.8s-320 143.2-320 319.8c0 163.1 122.2 297.7 280.2 317.3 8.9 1.9 19.7 3 32.7 3 3.6 0 7.1-0.2 10.5-0.5 175.1-1.8 316.6-144.3 316.6-319.8z m-45.8 4.6c-0.9-16-3-31.6-6.2-46.7 0.3-5.4 0.4-10.6 0.2-15.3 4 18.6 6.1 37.8 6.1 57.6 0 1.4 0 2.9-0.1 4.4z m-548.5-4.6c0-19.4 2-38.3 5.8-56.5 11.8 12.7 39.8 12.4 46.6-6.6 12.2 7.3 28.6 8.6 28.6 23.1 0 47.9 1.7 99.2 45.2 100 1.2 0 24.3 8.7 35.2 37.2 3.8 9.8 18.8 0 35.2 0 8.2 0 0 13.8 0 43.7 0 29.8 64.2 75.7 64.2 75.7-0.3 19.7 0.5 35.7 2.2 48.4-14.5-0.3-26.7 1.7-36.3 4.9-128.8-22.4-226.7-134.7-226.7-269.9z m341.8 265.8c-1.4-7-7.6-10.8-19-7.8 9.1-38.6 13.5-60.2 32.4-76.6 27.4-23.7 3.3-50.1-17.6-47-16.4 2.5-6-20.3-20.7-21.6-14.6-1.2-33.8-30.4-54.8-40.4-11.2-5.3-22.1-19.5-39.4-20.2-15.3-0.6-37.6 12.9-37.6 2.5 0-33.5-3.4-57.5-4.1-67-0.6-7.7-5-2.6 15.6-2.1 11.2 0.3 5.7-22.6 16.9-23.5 10.9-0.9 36.9 10.2 43.6 5.8 6.2-4.1 45.3 102.7 45.3 17.7 0-10.1-5.2-27.6 0-37.2 20.7-37.8 40-68.5 38.3-73-1-2.5-21.1-4.6-37.3 0.8-5.4 1.8 1.7 10.3-6.1 12.2-29.3 6.8-55.2-7.9-46.1-21.7 9.3-14.1 42.9-6.2 45.8-34.5 1.7-16.2 3.1-35 4-49 39.4 6.2 35.1-51.2-23.5-57.3 118.6 1.4 219.1 78 255.9 184.2-1.9-1.7-4-2.7-6.5-3-17.7-44.3-60.7-12.2-46.1 26.8-78.2 60.1-58.2 102-32.5 126 13.5 12.6 26.4 31.6 34.8 45.2-9.1 26.6 33.6 16 54.7-29.2-26.7 93.2-101.6 166-196 189.9z m0 0"
                      fill="#82AAFC" ></path>
                </svg>
              </el-icon>
              <template #title>
                <span>GeoJson-to-shapefile</span>
                <el-tag size="small" effect="dark" type="success" class="menu-tag">工具</el-tag>
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
  Notebook,
  Document,
  Memo,
  Fold,
  Expand,
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
  /* 增加横向滚动提示 */
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.2) transparent;
}

/* 外部容器用于实现横向滚动 */
.menu-outer-container {
  width: max-content;
  min-width: 100%;
}

.sidebar-menu {
  min-width: 200px;
  width: 100%;
  border-right: none !important;
  overflow: visible;
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


.el-menu--collapse .el-sub-menu__title {
  padding-left: 20px !important;
}

/* 横向滚动提示 */
.menu-scroll-container:hover {
  overflow-x: auto;
}

/* 防止折叠模式下出现滚动 */
.el-menu--collapse + .menu-outer-container {
  overflow-x: hidden !important;
}

/* 菜单项动画 */
.gomoku-menu-item {
  position: relative;
  overflow: hidden;
  white-space: nowrap; /* 防止文字换行 */
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

/* 中国象棋图标特殊样式 */
.gomoku-menu-item:hover .dynamic-icon {
  transform: rotate(-15deg) scale(1.1);
  color: #c62828;
}
</style>