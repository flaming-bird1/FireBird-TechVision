<template>
  <div class="app-container">
    <!-- 主内容区 -->
    <main class="main-content">
      <div class="hero-section">
        <div class="logo-title">
          <img
              src="../assets/images/logo.png"
              alt="Logo"
              class="logo-image"
          >
          <div>
            <h1>手写数字识别系统</h1>
            <p class="subtitle">基于深度学习的高精度识别模型，准确率达99.24%</p>
          </div>
        </div>
      </div>

      <div class="demo-container">
        <!-- 左侧上传区域 -->
        <div class="upload-section">
          <el-card class="upload-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <h3>上传手写数字</h3>
                <el-tag type="primary" effect="plain">支持PNG/JPG</el-tag>
              </div>
            </template>

            <div class="upload-wrapper">
              <el-upload
                  class="upload-area"
                  drag
                  action=""
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="handleImageChange"
                  accept=".png,.jpg,.jpeg"
              >
                <div class="upload-content">
                  <el-icon class="upload-icon"><upload-filled /></el-icon>
                  <h4>拖拽图片到此处</h4>
                  <p class="upload-hint">或点击选择文件</p>
                </div>
              </el-upload>

              <div v-if="imagePreview" class="preview-wrapper">
                <div class="preview-header">
                  <span>图片预览</span>
                  <el-button
                      type="primary"
                      size="large"
                      @click="predict"
                      :loading="loading"
                      class="predict-btn"
                  >
                    <el-icon class="mr-1"><magic-stick /></el-icon>
                    开始识别
                  </el-button>
                </div>
                <div class="image-container">
                  <img :src="imagePreview" class="preview-image" />
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 右侧结果区域 -->
        <div class="result-section" v-if="result">
          <el-card class="result-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <h3>识别结果</h3>
                <el-tag type="success" size="large">
                  置信度 {{ (result.probabilities[result.prediction] * 100).toFixed(1) }}%
                </el-tag>
              </div>
            </template>

            <div class="result-content">
              <div class="prediction-display">
                <div class="prediction-number">
                  <span class="label">识别数字</span>
                  <span class="number">{{ result.prediction }}</span>
                </div>

                <div class="probability-chart">
                  <div
                      v-for="(prob, idx) in result.probabilities"
                      :key="idx"
                      class="probability-bar"
                  >
                    <div class="bar-label">{{ idx }}</div>
                    <el-progress
                        :percentage="prob * 100"
                        :show-text="false"
                        :color="getProgressColor(idx)"
                    />
                    <div class="bar-value">{{ (prob * 100).toFixed(1) }}%</div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </main>

    <!-- 页脚组件 -->
    <Footer />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { UploadFilled, MagicStick } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import Footer from '@/components/Footer.vue' // 引入页脚组件

const imagePreview = ref(null)
const result = ref(null)
const loading = ref(false)

const handleImageChange = (file) => {
  if (!file.raw.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件')
    return
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    imagePreview.value = e.target.result
    result.value = null
  }
  reader.readAsDataURL(file.raw)
}

const predict = async () => {
  if (!imagePreview.value) return

  loading.value = true
  try {
    const formData = new FormData()
    formData.append('image', dataURLtoFile(imagePreview.value))

    const response = await axios.post('/api/mnist/predict', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (response.data.code === 200) {
      result.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '识别失败')
    }
  } catch (error) {
    ElMessage.error('请求出错: ' + error.message)
  } finally {
    loading.value = false
  }
}

function dataURLtoFile(dataurl) {
  const arr = dataurl.split(',')
  const mime = arr[0].match(/:(.*?);/)[1]
  const bstr = atob(arr[1])
  let n = bstr.length
  const u8arr = new Uint8Array(n)
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n)
  }
  return new File([u8arr], 'upload.png', { type: mime })
}

function getProgressColor(idx) {
  if (idx === result.value?.prediction) {
    return 'var(--el-color-primary)'
  }
  return '#f0f0f0'
}
</script>

<style scoped>
/* 全局样式 */
:root {
  --primary-color: #409EFF;
  --bg-color: #ffffff;
  --text-color: #303133;
  --border-color: #e4e7ed;
  --footer-height: 80px;
}

/* 布局容器 */
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f8fafc;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* 主内容区 */
.main-content {
  flex: 1;
  padding: 40px 24px;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
}

.hero-section {
  text-align: center;
  margin-bottom: 48px;
}

.logo-title {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.logo-image {
  width: 80px;
  height: 80px;
}

h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 16px;
  background: linear-gradient(90deg, #409EFF, #67C23A);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  font-size: 1.2rem;
  color: #7f8c8d;
  max-width: 600px;
  margin: 0 auto;
}

.demo-container {
  display: flex;
  gap: 24px;
}

.upload-section {
  flex: 1;
}

.result-section {
  flex: 1;
}

/* 上传卡片样式 */
.upload-card, .result-card {
  border-radius: 12px;
  border: none;
  height: 100%;
  transition: transform 0.3s, box-shadow 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1) !important;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  h3 {
    margin: 0;
    font-size: 1.25rem;
    color: var(--text-color);
  }
}

.upload-wrapper {
  padding: 16px;
}

.upload-area {
  width: 100%;
}

.upload-content {
  padding: 32px;
  text-align: center;

  .upload-icon {
    font-size: 48px;
    color: var(--primary-color);
    margin-bottom: 16px;
  }

  h4 {
    font-size: 1.25rem;
    margin-bottom: 8px;
    color: #2c3e50;
  }

  .upload-hint {
    color: #7f8c8d;
    font-size: 0.9rem;
  }
}

.preview-wrapper {
  margin-top: 24px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  span {
    font-weight: 500;
    color: #2c3e50;
  }
}

.image-container {
  display: flex;
  justify-content: center;
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
}

.preview-image {
  max-width: 100%;
  max-height: 280px;
  border-radius: 4px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.predict-btn {
  width: 160px;
}

/* 结果卡片样式 */
.result-content {
  padding: 16px;
}

.prediction-display {
  margin-bottom: 24px;
}

.prediction-number {
  text-align: center;
  margin-bottom: 24px;

  .label {
    display: block;
    font-size: 1rem;
    color: #7f8c8d;
    margin-bottom: 8px;
  }

  .number {
    font-size: 72px;
    font-weight: 700;
    color: var(--primary-color);
    line-height: 1;
    text-shadow: 0 4px 10px rgba(64, 158, 255, 0.2);
  }
}

.probability-chart {
  margin-top: 24px;
}

.probability-bar {
  margin-bottom: 12px;
  display: flex;
  align-items: center;

  .bar-label {
    width: 24px;
    font-weight: 500;
    text-align: center;
  }

  .el-progress {
    flex: 1;
    margin: 0 12px;
  }

  .bar-value {
    width: 60px;
    font-size: 0.875rem;
    color: #7f8c8d;
    text-align: right;
  }
}

/* 响应式设计 */
@media (max-width: 992px) {
  .demo-container {
    flex-direction: column;
  }

  .prediction-number .number {
    font-size: 60px;
  }
}

@media (max-width: 768px) {
  .hero-section h1 {
    font-size: 2rem;
  }

  .logo-image {
    width: 60px;
    height: 60px;
  }

  .prediction-number .number {
    font-size: 48px;
  }
}

.mr-1 {
  margin-right: 4px;
}
</style>