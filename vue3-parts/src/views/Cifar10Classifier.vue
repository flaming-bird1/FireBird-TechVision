<template>
  <div class="cifar-container">
    <!-- 标题区 -->
    <div class="header-section">
      <div class="logo-title">
        <img
            src="../assets/images/logo.png"
            alt="Logo"
            class="logo-image"
        >
        <div>
          <h1>CIFAR-10图像分类器</h1>
          <p class="subtitle">基于深度学习的10类图像分类系统</p>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 上传卡片 -->
      <el-card class="upload-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <h3>上传图像</h3>
            <el-tag type="primary" effect="plain">支持JPG/PNG</el-tag>
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
                开始分类
              </el-button>
            </div>
            <div class="image-container">
              <img :src="imagePreview" class="preview-image" />
            </div>
          </div>
        </div>
      </el-card>

      <!-- 结果卡片 -->
      <el-card v-if="result" class="result-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <h3>分类结果</h3>
            <el-tag type="success" size="large">
              置信度 {{ (result.confidence * 100).toFixed(1) }}%
            </el-tag>
          </div>
        </template>

        <div class="result-content">
          <div class="prediction-display">
            <div class="prediction-class">
              <span class="label">预测类别</span>
              <span class="class-name">{{ result.prediction }}</span>
            </div>

            <div class="class-description">
              <p>{{ getClassDescription(result.prediction) }}</p>
            </div>
          </div>

          <div class="probability-chart" v-if="result">
            <h4 class="chart-title">各类别概率分布</h4>
            <div class="probability-bars">
              <div
                  v-for="(prob, cls) in result.probabilities"
                  :key="cls"
                  class="probability-item"
              >
                <span class="class-label">{{ cls }}</span>
                <el-progress
                    :percentage="prob * 100"
                    :format="() => ''"
                    :stroke-width="16"
                    :color="prob === Math.max(...Object.values(result.probabilities)) ? '#409EFF' : '#e6e6e6'"
                />
                <span class="prob-value">{{ (prob * 100).toFixed(1) }}%</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 页脚 -->
    <Footer />

  </div>
</template>

<script setup>
import { ref } from 'vue'
import { UploadFilled, MagicStick } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import Footer from "@/components/Footer.vue";

const imagePreview = ref(null)
const result = ref(null)
const loading = ref(false)
const fileRaw = ref(null)

// CIFAR-10类别描述
const classDescriptions = {
  '飞机': '一种固定翼飞行器，通常用于运输乘客或货物',
  '汽车': '四轮机动车辆，主要用于道路运输',
  '鸟': '温血脊椎动物，特征是有羽毛、喙和翅膀',
  '猫': '小型肉食性哺乳动物，家猫常作为宠物饲养',
  '鹿': '草食性哺乳动物，雄性通常有分叉的角',
  '狗': '驯养的肉食性哺乳动物，常作为宠物或工作动物',
  '青蛙': '无尾两栖动物，生活在水中和陆地',
  '马': '大型草食性哺乳动物，用于骑乘或拉车',
  '船': '用于水上运输的交通工具',
  '卡车': '大型机动车辆，用于运输货物'
}

const handleImageChange = (file) => {
  if (!file.raw.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件')
    return
  }

  fileRaw.value = file.raw

  const reader = new FileReader()
  reader.onload = (e) => {
    imagePreview.value = e.target.result
    result.value = null
  }
  reader.readAsDataURL(file.raw)
}

const predict = async () => {
  if (!fileRaw.value) return

  loading.value = true
  try {
    const formData = new FormData()
    formData.append('file', fileRaw.value)

    const response = await axios.post('/api/cifar10/predict', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (response.data.code === 200) {
      result.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '分类失败')
    }
  } catch (error) {
    ElMessage.error('请求出错: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

function getClassDescription(className) {
  return classDescriptions[className] || '未知类别'
}
</script>

<style scoped>
.cifar-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f8fafc;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* 头部样式 */
.header-section {
  text-align: center;
  padding: 40px 20px 20px;
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
  font-size: 2.2rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 12px;
}

.subtitle {
  font-size: 1.1rem;
  color: #7f8c8d;
  max-width: 600px;
  margin: 0 auto;
}

/* 主内容区 */
.main-content {
  flex: 1;
  padding: 0 24px 40px;
  max-width: 800px;
  width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 上传卡片 */
.upload-card {
  border-radius: 12px;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 1.25rem;
  color: #2c3e50;
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
}

.upload-icon {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 16px;
}

.upload-content h4 {
  font-size: 1.25rem;
  margin-bottom: 8px;
  color: #2c3e50;
}

.upload-hint {
  color: #7f8c8d;
  font-size: 0.9rem;
}

/* 预览区 */
.preview-wrapper {
  margin-top: 24px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.preview-header span {
  font-weight: 500;
  color: #2c3e50;
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

/* 结果卡片 */
.result-card {
  border-radius: 12px;
  border: none;
}

.result-content {
  padding: 16px;
}

.prediction-display {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.prediction-class {
  text-align: center;
}

.prediction-class .label {
  display: block;
  font-size: 1rem;
  color: #7f8c8d;
  margin-bottom: 8px;
}

.prediction-class .class-name {
  font-size: 2rem;
  font-weight: 700;
  color: #409EFF;
  line-height: 1;
}

.class-description {
  text-align: center;
  font-size: 1.1rem;
  color: #5a5a5a;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

/* 概率图表 */
.probability-chart {
  margin-top: 24px;
}

.chart-title {
  font-size: 1.1rem;
  color: #2c3e50;
  margin-bottom: 16px;
  text-align: center;
}

.probability-bars {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.probability-item {
  display: grid;
  grid-template-columns: 80px 1fr 60px;
  align-items: center;
  gap: 12px;
}

.class-label {
  font-size: 0.9rem;
  color: #5a5a5a;
  text-align: right;
}

.prob-value {
  font-size: 0.9rem;
  color: #409EFF;
  font-weight: 500;
  text-align: right;
}

/* 页脚 */
.app-footer {
  background-color: #2c3e50;
  color: #ffffff;
  padding: 24px 0;
  margin-top: 40px;
}

.footer-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 600;
  font-size: 1.1rem;
}

.copyright {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
}

/* 响应式设计 */
@media (max-width: 768px) {
  h1 {
    font-size: 1.8rem;
  }

  .logo-image {
    width: 60px;
    height: 60px;
  }

  .prediction-class .class-name {
    font-size: 1.5rem;
  }

  .probability-item {
    grid-template-columns: 60px 1fr 50px;
    gap: 8px;
  }
}

.mr-1 {
  margin-right: 4px;
}
</style>