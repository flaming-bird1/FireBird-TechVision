<template>
  <div class="home-container">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h1>欢迎回来，{{ userName }}！</h1>
        <p v-if="learningDays > 0">今天是你坚持学习的第{{ learningDays }}天，继续加油！</p>
        <p v-else>开始你的单词学习之旅吧！</p>
        <div class="banner-actions">
          <el-button type="primary" size="large" @click="startLearning">
            <el-icon><Reading /></el-icon>
            开始学习
          </el-button>
          <el-button size="large" @click="startReview">
            <el-icon><Refresh /></el-icon>
            复习单词
          </el-button>
        </div>
      </div>
      <div class="banner-image">
        <img src="../assets/images/learning-banner.svg" alt="Learning Banner">
      </div>
    </div>

    <!-- 学习数据概览 -->
    <div class="stats-overview">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #4cc9f0;">
              <el-icon><Collection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.learned }}</div>
              <div class="stat-label">已学单词</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #4361ee;">
              <el-icon><SuccessFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.mastered }}</div>
              <div class="stat-label">已掌握</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #3a0ca3;">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.today }}</div>
              <div class="stat-label">今日学习</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #7209b7;">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.streak }}</div>
              <div class="stat-label">连续学习天数</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 学习进度 -->
    <div class="progress-section">
      <h2>你的学习进度</h2>
      <div class="progress-cards">
        <el-card class="progress-card">
          <template #header>
            <div class="card-header">
              <span>单词掌握率</span>
              <el-tag type="success" v-if="stats.learned > 0">
                {{ Math.round((stats.mastered / stats.learned) * 100) || 0 }}%
              </el-tag>
              <el-tag type="info" v-else>0%</el-tag>
            </div>
          </template>
          <el-progress
              :percentage="Math.round((stats.mastered / stats.learned) * 100) || 0"
              :color="customColors"
              :show-text="false"
              :stroke-width="12"
          />
          <div class="progress-desc">
            <span v-if="stats.learned > 0">已掌握 {{ stats.mastered }} / {{ stats.learned }} 个单词</span>
            <span v-else>尚未开始学习</span>
          </div>
        </el-card>

        <el-card class="progress-card">
          <template #header>
            <div class="card-header">
              <span>今日目标</span>
              <el-tag :type="stats.today >= dailyGoal ? 'success' : 'info'">
                {{ stats.today }} / {{ dailyGoal }}
              </el-tag>
            </div>
          </template>
          <el-progress
              :percentage="Math.round((stats.today / dailyGoal) * 100)"
              :color="customColors"
              :show-text="false"
              :stroke-width="12"
          />
          <div class="progress-desc">
            <span v-if="stats.today >= dailyGoal">恭喜！你已完成今日目标</span>
            <span v-else>还需学习 {{ dailyGoal - stats.today }} 个单词完成目标</span>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 快速开始 -->
    <div class="quick-actions">
      <h2>快速开始</h2>
      <el-row :gutter="20">
        <el-col :xs="12" :sm="8" :md="6" v-for="action in quickActions" :key="action.title">
          <el-card class="action-card" shadow="hover" @click="action.handler">
            <div class="action-icon" :style="{ backgroundColor: action.color }">
              <el-icon><component :is="action.icon" /></el-icon>
            </div>
            <h3>{{ action.title }}</h3>
            <p>{{ action.description }}</p>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 推荐学习 -->
    <div class="recommendations">
      <h2>为你推荐</h2>
      <el-card>
        <template #header>
          <div class="card-header">
            <span>根据你的学习历史推荐</span>
          </div>
        </template>
        <div class="recommendation-list">
          <div class="recommendation-item" v-for="(item, index) in recommendations" :key="index">
            <div class="recommendation-content">
              <h4>{{ item.category }}</h4>
              <p>{{ item.description }}</p>
              <div class="word-count">{{ item.count }} 个单词</div>
            </div>
            <el-button type="primary" @click="startCategory(item.category)">开始学习</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 学习日历 -->
    <div class="learning-calendar">
      <h2>学习日历</h2>
      <el-card>
        <div class="calendar-header">
          <span>最近30天学习记录</span>
          <el-tag type="success" v-if="stats.streak > 0">已连续学习 {{ stats.streak }} 天</el-tag>
          <el-tag type="info" v-else>尚未开始学习</el-tag>
        </div>
        <div class="calendar-grid">
          <div
              v-for="day in 30"
              :key="day"
              class="calendar-day"
              :class="{
              'learned': day <= stats.streak,
              'today': day === 30
            }"
          >
            <span>{{ 30 - day + 1 }}</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Reading,
  Refresh,
  Collection,
  SuccessFilled,
  Calendar,
  Clock,
  Notebook,
  Document,
  DataAnalysis
} from '@element-plus/icons-vue'

export default {
  name: 'HomeView',
  components: {
    Reading,
    Refresh
  },
  setup() {
    const router = useRouter()

    const userInfo = ref({})
    const userName = ref('学习者')
    const learningDays = ref(0)
    const dailyGoal = ref(20)

    const stats = ref({
      learned: 0,
      mastered: 0,
      today: 0,
      streak: 0
    })

    const customColors = ref([
      { color: '#f56c6c', percentage: 20 },
      { color: '#e6a23c', percentage: 40 },
      { color: '#5cb87a', percentage: 60 },
      { color: '#1989fa', percentage: 80 },
      { color: '#6f7ad3', percentage: 100 }
    ])

    const quickActions = ref([
      {
        title: '学习新词',
        description: '学习新的单词',
        icon: Reading,
        color: '#4cc9f0',
        handler: () => router.push('/word-study-system/learn')
      },
      {
        title: '复习单词',
        description: '巩固已学内容',
        icon: Refresh,
        color: '#4361ee',
        handler: () => router.push('/word-study-system/review')
      },
      {
        title: '单词本',
        description: '查看收藏单词',
        icon: Notebook,
        color: '#7209b7',
        handler: () => router.push('/word-study-system/collection')
      },
      {
        title: 'PK对战',
        description: '与其他用户比拼',
        icon: DataAnalysis,
        color: '#f72585',
        handler: () => router.push('/word-study-system/statistics')
      }
    ])

    const recommendations = ref([
      {
        category: '商务英语',
        description: '适用于工作场景的专业词汇',
        count: 42
      },
      {
        category: '旅行词汇',
        description: '旅行中常用的实用单词和短语',
        count: 36
      },
      {
        category: '学术词汇',
        description: '学术写作和阅读中常见的高级词汇',
        count: 28
      }
    ])

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
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        ElMessage.error('用户信息加载失败')
      }
    }

    // 获取用户学习统计数据
    const getUserStats = async () => {
      try {
        // 这里可以调用API获取用户的实际学习数据
        // 暂时使用模拟数据
        stats.value = {
          learned: 1258,
          mastered: 856,
          today: 15,
          streak: 28
        }
        learningDays.value = 28
      } catch (error) {
        console.error('获取学习数据失败:', error)
        ElMessage.error('学习数据加载失败')
      }
    }

    const startLearning = () => {
      router.push('/word-study-system/learn')
    }

    const startReview = () => {
      router.push('/word-study-system/review')
    }

    const startCategory = (category) => {
      console.log('开始学习分类:', category)
      router.push('/word-study-system/learn')
    }

    // 组件挂载时获取数据
    onMounted(() => {
      getUserInfo()
      getUserStats()
    })

    return {
      userName,
      learningDays,
      dailyGoal,
      stats,
      customColors,
      quickActions,
      recommendations,
      startLearning,
      startReview,
      startCategory
    }
  }
}
</script>

<style scoped>
.home-container {
  padding: 0;
  max-width: 1200px;
  margin: 0 auto;
}

/* 欢迎横幅 */
.welcome-banner {
  display: flex;
  background: linear-gradient(135deg, #4361ee 0%, #3a0ca3 100%);
  border-radius: 16px;
  padding: 2.5rem;
  color: white;
  margin-bottom: 2rem;
  box-shadow: 0 10px 30px rgba(67, 97, 238, 0.3);
}

.banner-content {
  flex: 1;
}

.banner-content h1 {
  font-size: 2.2rem;
  margin: 0 0 0.5rem 0;
  font-weight: 700;
}

.banner-content p {
  font-size: 1.1rem;
  margin: 0 0 1.5rem 0;
  opacity: 0.9;
}

.banner-actions {
  display: flex;
  gap: 1rem;
}

.banner-image {
  flex: 0 0 40%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-image img {
  max-width: 100%;
  height: auto;
}

/* 数据概览 */
.stats-overview {
  margin-bottom: 2rem;
}

.stat-card {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 12px;
  margin-right: 1rem;
  color: white;
  font-size: 1.5rem;
}

.stat-number {
  font-size: 1.8rem;
  font-weight: 700;
  color: #2b2d42;
  line-height: 1;
}

.stat-label {
  color: #6c757d;
  font-size: 0.9rem;
  margin-top: 0.3rem;
}

/* 学习进度 */
.progress-section {
  margin-bottom: 2rem;
}

.progress-section h2 {
  font-size: 1.5rem;
  color: #2b2d42;
  margin-bottom: 1rem;
}

.progress-cads {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.progress-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-desc {
  margin-top: 1rem;
  font-size: 0.9rem;
  color: #6c757d;
}

/* 快速开始 */
.quick-actions {
  margin-bottom: 2rem;
}

.quick-actions h2 {
  font-size: 1.5rem;
  color: #2b2d42;
  margin-bottom: 1rem;
}

.action-card {
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 1.5rem;
  border: none;
  text-align: center;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
}

.action-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 16px;
  margin-bottom: 1rem;
  color: white;
  font-size: 1.8rem;
}

.action-card h3 {
  margin: 0 0 0.5rem 0;
  color: #2b2d42;
  font-size: 1.1rem;
}

.action-card p {
  margin: 0;
  color: #6c757d;
  font-size: 0.9rem;
}

/* 推荐学习 */
.recommendations {
  margin-bottom: 2rem;
}

.recommendations h2 {
  font-size: 1.5rem;
  color: #2b2d42;
  margin-bottom: 1rem;
}

.recommendation-list {
  display: grid;
  gap: 1rem;
}

.recommendation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.2rem;
  border-radius: 10px;
  background-color: #f8f9fa;
  transition: background-color 0.3s;
}

.recommendation-item:hover {
  background-color: #e9ecef;
}

.recommendation-content h4 {
  margin: 0 0 0.3rem 0;
  color: #2b2d42;
}

.recommendation-content p {
  margin: 0 0 0.5rem 0;
  color: #6c757d;
  font-size: 0.9rem;
}

.word-count {
  font-size: 0.8rem;
  color: #4361ee;
  font-weight: 500;
}

/* 学习日历 */
.learning-calendar h2 {
  font-size: 1.5rem;
  color: #2b2d42;
  margin-bottom: 1rem;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  gap: 0.5rem;
}

.calendar-day {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 30px;
  border-radius: 6px;
  background-color: #e9ecef;
  font-size: 0.8rem;
  font-weight: 500;
}

.calendar-day.learned {
  background-color: #4361ee;
  color: white;
}

.calendar-day.today {
  background-color: #f72585;
  color: white;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .welcome-banner {
    flex-direction: column;
    text-align: center;
    padding: 1.5rem;
  }

  .banner-image {
    margin-top: 1.5rem;
  }

  .banner-actions {
    justify-content: center;
  }

  .progress-cards {
    grid-template-columns: 1fr;
  }

  .calendar-grid {
    grid-template-columns: repeat(5, 1fr);
  }

  .recommendation-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .recommendation-item .el-button {
    align-self: flex-end;
  }
}

@media (max-width: 480px) {
  .banner-content h1 {
    font-size: 1.8rem;
  }

  .banner-actions {
    flex-direction: column;
  }

  .stat-card {
    flex-direction: column;
    text-align: center;
  }

  .stat-icon {
    margin-right: 0;
    margin-bottom: 1rem;
  }
}
</style>