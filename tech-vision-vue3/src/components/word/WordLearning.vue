<template>
  <div class="learning-container">
    <!-- 顶部导航 -->
    <div class="learning-header">
      <h2>单词学习</h2>
      <div class="header-controls">
        <el-button :icon="Setting" circle @click="showSettings = true" />
        <el-button :icon="Refresh" circle @click="loadNewWords" :loading="loading" />
      </div>
    </div>

    <!-- 学习进度 -->
    <div class="progress-info">
      <div class="progress-text">
        <span>今日已学: <strong>{{ learnedToday }}</strong> 个单词</span>
        <span>目标: <strong>{{ dailyGoal }}</strong> 个单词</span>
      </div>
      <el-progress
          :percentage="Math.round((learnedToday / dailyGoal) * 100)"
          :color="progressColor"
          :show-text="false"
          :stroke-width="12"
      />
    </div>

    <!-- 单词卡片区域 -->
    <div class="card-container" v-if="words.length > 0 && !loading">
      <transition name="flip" mode="out-in">
        <!-- 单词正面（仅显示单词） -->
        <div
            v-if="!showTranslation"
            class="word-card front"
            @click="showTranslation = true"
            key="front"
        >
          <div class="word-content">
            <h1 class="current-word">{{ currentWord.word }}</h1>
            <p class="hint-text">点击卡片查看释义</p>
          </div>
          <div class="card-actions">
            <el-button
                type="success"
                :icon="Check"
                size="large"
                @click.stop="handleKnowWord"
            >
              我知道
            </el-button>
          </div>
        </div>

        <!-- 单词背面（显示释义） -->
        <div v-else class="word-card back" key="back">
          <div class="word-content">
            <h1 class="current-word">{{ currentWord.word }}</h1>
            <div class="translation">
              <p>{{ currentWord.translation }}</p>
            </div>
<!--            <div class="example-sentence" v-if="currentWord.example">-->
<!--              <p>{{ currentWord.example }}</p>-->
<!--            </div>-->
          </div>
          <div class="card-actions">
            <el-button
                type="primary"
                :icon="Star"
                size="large"
                @click="addToCollection"
                v-if="!currentWord.collected"
            >
              收藏
            </el-button>
            <el-button
                type="warning"
                :icon="StarFilled"
                size="large"
                @click="removeFromCollection"
                v-else
            >
              已收藏
            </el-button>
            <el-button
                type="success"
                :icon="Check"
                size="large"
                @click="handleKnowWord"
            >
              下一个
            </el-button>
          </div>
        </div>
      </transition>

      <!-- 单词导航 -->
      <div class="word-navigation">
        <el-button
            :icon="ArrowLeft"
            :disabled="currentIndex === 0"
            @click="prevWord"
        >
          上一个
        </el-button>
        <span class="word-counter">{{ currentIndex + 1 }} / {{ words.length }}</span>
        <el-button
            :icon="ArrowRight"
            :disabled="currentIndex === words.length - 1"
            @click="nextWord"
        >
          下一个
        </el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div class="loading-state" v-if="loading">
      <el-icon class="loading-icon" :size="40"><Loading /></el-icon>
      <p>正在加载单词...</p>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else-if="words.length === 0 && !loading">
      <el-icon class="empty-icon" :size="60"><DocumentRemove /></el-icon>
      <p>暂无单词数据</p>
      <el-button type="primary" @click="loadNewWords">重新加载</el-button>
    </div>

    <!-- 设置弹窗 -->
    <el-dialog v-model="showSettings" title="学习设置" width="400px">
      <div class="settings-content">
        <div class="setting-item">
          <label>每批学习单词数量</label>
          <el-input-number v-model="wordCount" :min="5" :max="500" />
        </div>
        <div class="setting-item">
          <label>每日学习目标</label>
          <el-input-number v-model="dailyGoal" :min="10" :max="100" />
        </div>
      </div>
      <template #footer>
        <el-button @click="showSettings = false">取消</el-button>
        <el-button type="primary" @click="saveSettings">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import {
  Check,
  Star,
  StarFilled,
  ArrowLeft,
  ArrowRight,
  Refresh,
  Setting,
  Loading,
  DocumentRemove
} from '@element-plus/icons-vue'

export default {
  name: 'WordLearning',
  setup() {
    const router = useRouter()

    // 响应式数据
    const words = ref([])
    const currentIndex = ref(0)
    const showTranslation = ref(false)
    const learnedToday = ref(15)
    const dailyGoal = ref(20)
    const wordCount = ref(10)
    const showSettings = ref(false)
    const loading = ref(false)

    // 计算当前单词
    const currentWord = computed(() => {
      return words.value[currentIndex.value] || {}
    })

    // 进度条颜色
    const progressColor = computed(() => {
      const percentage = (learnedToday.value / dailyGoal.value) * 100
      if (percentage < 50) return '#e6a23c'
      if (percentage < 80) return '#1989fa'
      return '#67c23a'
    })

    // 获取认证token
    const getAuthToken = () => {
      return localStorage.getItem('token') || ''
    }

    // 检查登录状态
    const checkLoginStatus = () => {
      const token = getAuthToken()
      if (!token) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return false
      }
      return true
    }

    // 加载单词
    const loadWords = async () => {
      if (!checkLoginStatus()) return

      loading.value = true
      try {
        const token = getAuthToken()
        const response = await axios.get(`/spring/word/random?num=${wordCount.value}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })

        if (response.data.code === 200) {
          words.value = response.data.data.map(word => ({
            ...word,
            collected: false,
            // example: generateExampleSentence(word.word)
          }))
          currentIndex.value = 0
          showTranslation.value = false
          ElMessage.success('单词加载成功')
        } else {
          ElMessage.error('获取单词失败: ' + response.data.msg)
          words.value = []
        }
      } catch (error) {
        console.error('获取单词错误:', error)
        if (error.response) {
          // 处理HTTP错误响应
          if (error.response.status === 401) {
            ElMessage.error('登录已过期，请重新登录')
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            router.push('/login')
          } else {
            const errorMsg = error.response.data?.msg || '获取单词失败'
            ElMessage.error(errorMsg)
          }
        } else if (error.request) {
          ElMessage.error('网络错误，请检查网络连接')
        } else {
          ElMessage.error('请求失败: ' + error.message)
        }
        words.value = []
      } finally {
        loading.value = false
      }
    }

    // // 生成示例句子（模拟功能）
    // const generateExampleSentence = (word) => {
    //   const examples = {
    //     pedigree: "The dog's pedigree shows it comes from a long line of champions.",
    //     tan: "She got a beautiful tan during her vacation in Greece.",
    //     shoot: "The photographer asked the model to shoot several poses.",
    //     packet: "The network device sends data in small packets for efficiency.",
    //     freelance: "Many designers work as freelance professionals.",
    //     doodle: "He would often doodle in the margins of his notebook during meetings.",
    //     congenial: "They found themselves in a congenial atmosphere at the party.",
    //     statement: "The company issued a statement regarding the recent events.",
    //     crow: "We heard a crow cawing in the distance early in the morning.",
    //     prism: "Light passing through a prism separates into different colors."
    //   }
    //
    //   return examples[word] || `"${word}" can be used in various contexts.`
    // }

    // 处理"我知道"按钮点击
    const handleKnowWord = () => {
      learnedToday.value++
      showTranslation.value = false

      if (currentIndex.value < words.value.length - 1) {
        currentIndex.value++
      } else {
        // 如果是最后一个单词，加载新的一组
        ElMessage.success('已完成当前组单词学习！')
        loadWords()
      }
    }

    // 添加到收藏
    const addToCollection = async () => {
      try {
        const token = getAuthToken()
        const response = await axios.post('/spring/word/collection/add', {
          wordId: currentWord.value.id
        }, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })

        if (response.data.code === 200) {
          words.value[currentIndex.value].collected = true
          ElMessage.success('已添加到单词本')
        } else {
          ElMessage.error('收藏失败: ' + response.data.msg)
        }
      } catch (error) {
        console.error('收藏单词错误:', error)
        if (error.response?.status === 401) {
          ElMessage.error('登录已过期，请重新登录')
          router.push('/login')
        } else {
          ElMessage.error('收藏失败，请重试')
        }
      }
    }

    // 从收藏移除
    const removeFromCollection = async () => {
      try {
        const token = getAuthToken()
        const response = await axios.post('/spring/word/collection/remove', {
          wordId: currentWord.value.id
        }, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })

        if (response.data.code === 200) {
          words.value[currentIndex.value].collected = false
          ElMessage.info('已从单词本移除')
        } else {
          ElMessage.error('取消收藏失败: ' + response.data.msg)
        }
      } catch (error) {
        console.error('取消收藏错误:', error)
        ElMessage.error('取消收藏失败，请重试')
      }
    }

    // 上一个单词
    const prevWord = () => {
      if (currentIndex.value > 0) {
        currentIndex.value--
        showTranslation.value = false
      }
    }

    // 下一个单词
    const nextWord = () => {
      if (currentIndex.value < words.value.length - 1) {
        currentIndex.value++
        showTranslation.value = false
      }
    }

    // 加载新单词
    const loadNewWords = () => {
      loadWords()
    }

    // 保存设置
    const saveSettings = () => {
      showSettings.value = false
      ElMessage.success('设置已保存')
      loadWords() // 重新加载单词
    }

    // 组件挂载时加载单词
    onMounted(() => {
      loadWords()
    })

    return {
      words,
      currentIndex,
      showTranslation,
      learnedToday,
      dailyGoal,
      wordCount,
      showSettings,
      loading,
      currentWord,
      progressColor,
      Check,
      Star,
      StarFilled,
      ArrowLeft,
      ArrowRight,
      Refresh,
      Setting,
      Loading,
      DocumentRemove,
      handleKnowWord,
      addToCollection,
      removeFromCollection,
      prevWord,
      nextWord,
      loadNewWords,
      saveSettings
    }
  }
}
</script>

<style scoped>
.learning-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 1rem;
}

.learning-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.learning-header h2 {
  color: #2b2d42;
  font-size: 1.8rem;
  margin: 0;
}

.progress-info {
  background-color: white;
  border-radius: 12px;
  padding: 1.2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 2rem;
}

.progress-text {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.8rem;
  font-size: 0.9rem;
  color: #6c757d;
}

.card-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.word-card {
  width: 100%;
  min-height: 350px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  padding: 2rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  margin-bottom: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.word-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

.word-content {
  text-align: center;
}

.current-word {
  font-size: 2.5rem;
  color: #2b2d42;
  margin: 1.5rem 0;
  font-weight: 700;
}

.hint-text {
  color: #6c757d;
  font-style: italic;
  margin-top: 2rem;
}

.translation {
  margin: 1.5rem 0;
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 12px;
  font-size: 1.2rem;
  color: #4361ee;
  font-weight: 500;
}

.example-sentence {
  margin: 1rem 0;
  padding: 1rem;
  background-color: #e9ecef;
  border-radius: 8px;
  font-style: italic;
  color: #6c757d;
}

.card-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 1.5rem;
}

.word-navigation {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1.5rem;
  margin-top: 1rem;
}

.word-counter {
  font-weight: 600;
  color: #2b2d42;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: #6c757d;
}

.loading-icon,
.empty-icon {
  margin-bottom: 1rem;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.settings-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 卡片翻转动画 */
.flip-enter-active,
.flip-leave-active {
  transition: all 0.5s ease;
}

.flip-enter-from,
.flip-leave-to {
  opacity: 0;
  transform: rotateY(90deg);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .learning-container {
    padding: 0.5rem;
  }

  .word-card {
    min-height: 300px;
    padding: 1.5rem;
  }

  .current-word {
    font-size: 2rem;
  }

  .card-actions {
    flex-direction: column;
    gap: 0.8rem;
  }

  .card-actions .el-button {
    width: 100%;
  }

  .word-navigation {
    flex-direction: column;
    gap: 1rem;
  }
}
</style>