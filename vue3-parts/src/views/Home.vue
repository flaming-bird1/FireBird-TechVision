<template>
  <el-container class="main-layout">
    <!-- 顶部导航栏 -->
    <el-header class="header-wrapper">
      <Header/>
    </el-header>

    <!-- 主体部分 -->
    <el-container>
      <!-- 左侧边栏 -->
      <el-aside class="sidebar-wrapper">
        <Sidebar/>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="dashboard-container">
        <!-- 个人介绍卡片 -->
        <el-card class="profile-card" shadow="hover">
          <div class="profile-content">
            <div class="avatar-wrapper">
              <!--              <el-avatar :size="80" src="../assets/images/烈焰飞鸟.png" />-->
              <el-avatar :size="80" :src="avatarUrl"/>
              <!--              <img src="../assets/images/logo.png" alt="Logo" class="logo-img"/>-->
              <div class="online-dot"></div>
            </div>
            <div class="profile-info">
              <h2 class="name">烈焰飞鸟</h2>
              <p class="bio">空间信息与数字技术 | 技术探索者</p>
              <div class="social-links">
                <el-tooltip content="GitCode" placement="top">
                  <el-icon :size="24" class="social-icon">
                    <Link/>
                  </el-icon>
                </el-tooltip>
                <el-tooltip content="博客" placement="top">
                  <el-icon :size="24" class="social-icon">
                    <Document/>
                  </el-icon>
                </el-tooltip>
                <el-tooltip content="邮箱" placement="top">
                  <el-icon :size="24" class="social-icon">
                    <Message/>
                  </el-icon>
                </el-tooltip>
              </div>
            </div>
          </div>
          <div class="tech-stack">
            <el-tag v-for="tech in techStack" :key="tech" type="info" effect="plain">
              {{ tech }}
            </el-tag>
          </div>
        </el-card>

        <!-- 学习展示区 -->
        <section class="projects-section">
          <h2 class="section-title">
            <el-icon>
              <Collection/>
            </el-icon>
            近期探索
          </h2>

          <div class="projects-grid">
            <el-card
                v-for="project in projects"
                :key="project.id"
                shadow="hover"
                class="project-card"
                @click="viewProject(project.id)"
            >
              <div class="card-header">
                <el-icon :size="24" :color="project.iconColor">
                  <component :is="project.icon"/>
                </el-icon>
                <h3>{{ project.title }}</h3>
              </div>
              <p class="project-desc">{{ project.description }}</p>
              <div class="project-footer">
                <div class="project-tags">
                  <el-tag
                      v-for="tag in project.tags"
                      :key="tag"
                      size="small"
                      :type="tagTypes[tag] || 'info'"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
                <div class="project-date">{{ project.date }}</div>
              </div>
            </el-card>
          </div>
        </section>

        <!-- 学习笔记 -->
        <section class="notes-section">
          <h2 class="section-title">
            <el-icon>
              <Notebook/>
            </el-icon>
            学习笔记
          </h2>

          <el-timeline>
            <el-timeline-item
                v-for="note in studyNotes"
                :key="note.id"
                :timestamp="note.date"
                placement="top"
            >
              <el-card shadow="hover">
                <h4>{{ note.title }}</h4>
                <p>{{ note.summary }}</p>
                <el-button
                    text
                    type="primary"
                    size="small"
                    @click="viewNote(note.id)"
                >
                  查看详情
                </el-button>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </section>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import {
  Link, Document, Message, Collection,
  Notebook, MagicStick, Cpu, DataAnalysis,
  VideoCamera, Monitor, Connection, Warning
} from '@element-plus/icons-vue'
import Header from '@/components/Header.vue'
import Sidebar from '@/components/LeftSidebar.vue'
import avatarImage from '@/assets/images/烈焰飞鸟.png'
import {useRouter} from 'vue-router'

const router = useRouter()

const avatarUrl = avatarImage

const techStack = [
  'Vue 3', 'JavaScript', 'Python',
  'PyTorch', 'java', 'springboot', 'MySQL'
]

const projects = [
  {
    id: 1,
    title: '手写数字识别系统',
    description: '基于CNN的MNIST手写数字识别，实现99%以上的准确率',
    icon: MagicStick,
    iconColor: '#409EFF',
    tags: ['深度学习', 'CNN', 'Pytorch'],
    date: '2025-7-20'
  },
  {
    id: 2,
    title: '有趣的404页面',
    description: '使用vue3+element-plus开发了一个具有404页面效果的网站',
    icon: Warning,
    iconColor: '#67C23A',
    tags: ['前端', 'vue3', 'element-plus'],
    date: '2025-7-15'
  },
  {
    id: 3,
    title: '朴素贝叶斯实战',
    description: '机器学习：用朴素贝叶斯算法对新闻进行分类',
    icon: Cpu,
    iconColor: '#E6A23C',
    tags: ['机器学习', '朴素贝叶斯', '分类'],
    date: '2025-7-22'
  },
]

const studyNotes = [
  {
    id: 1,
    title: 'pytorch学习笔记',
    summary: '记录了使用PyTorch进行深度学习模型的搭建、训练和部署的详细步骤...',
    date: '2025-6-30'
  },
  {
    id: 2,
    title: '机器学习笔记',
    summary: '记录了机器学习的一些基础概念和算法，以及如何使用Python实现这些算法...',
    date: '2025-7-5'
  },
]

const tagTypes = {
  '深度学习': 'danger',
  '机器学习': 'danger',
  '计算机视觉': 'danger',
  'Python': '',
  '人工智能': 'success',
  'PyTorch': '',
  '前端': 'warning',
  'JavaScript': '',
  '算法': '',
  '可视化': 'info',
}

const viewProject = (id) => {
  console.log('查看项目:', id)
}

const viewNote = (id) => {
  router.push(`/note/${id}`)
}
</script>

<style scoped>
/* 布局样式 */
.main-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.header-wrapper {
  height: 60px;
  padding: 0 !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  z-index: 1000;
  background: white;
}

.sidebar-wrapper {
  width: 220px !important;
  border-right: 1px solid #e6e6e6;
  background: white;
}

.dashboard-container {
  padding: 20px;
  height: calc(100vh - 60px);
  overflow: auto;
  background-color: #f5f7fa;
}

/* 个人资料卡片 */
.profile-card {
  margin-bottom: 24px;
  border-radius: 12px;
  border: none;
}

.profile-content {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.avatar-wrapper {
  position: relative;
  margin-right: 20px;
}

.online-dot {
  position: absolute;
  bottom: 5px;
  right: 5px;
  width: 12px;
  height: 12px;
  background-color: #67C23A;
  border-radius: 50%;
  border: 2px solid white;
}

.profile-info {
  flex: 1;

  .name {
    margin: 0 0 4px 0;
    font-size: 1.5rem;
    color: #303133;
  }

  .bio {
    margin: 0 0 8px 0;
    color: #909399;
    font-size: 0.9rem;
  }
}

.social-links {
  display: flex;
  gap: 12px;

  .social-icon {
    cursor: pointer;
    color: #606266;
    transition: color 0.3s;

    &:hover {
      color: #409EFF;
    }
  }
}

.tech-stack {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px dashed #e6e6e6;
}

/* 项目展示区 */
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
  margin: 24px 0 16px 0;

  .el-icon {
    font-size: 1.2em;
  }
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.project-card {
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  padding-bottom: 16px;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1) !important;
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;

    h3 {
      margin: 0;
      font-size: 1.1rem;
    }
  }

  .project-desc {
    color: #606266;
    font-size: 0.9rem;
    margin: 8px 0 12px 0;
    line-height: 1.5;
  }

  .project-footer {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-top: 12px;

    .project-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
    }

    .project-date {
      font-size: 0.8rem;
      color: #909399;
      text-align: right;
      margin-top: 4px;
    }
  }
}

/* 学习笔记区 */
.notes-section {
  margin-top: 32px;

  .el-timeline {
    padding-left: 8px;
  }

  .el-card {
    border-radius: 8px;

    h4 {
      margin: 0 0 8px 0;
      font-size: 1rem;
    }

    p {
      margin: 0 0 12px 0;
      color: #606266;
      font-size: 0.9rem;
      line-height: 1.5;
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .projects-grid {
    grid-template-columns: 1fr;
  }

  .profile-content {
    flex-direction: column;
    text-align: center;

    .avatar-wrapper {
      margin-right: 0;
      margin-bottom: 16px;
    }
  }

  .social-links {
    justify-content: center;
  }

  .tech-stack {
    justify-content: center;
  }
}
</style>