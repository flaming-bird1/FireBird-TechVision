<template>
  <div class="converter-container">
    <!-- 顶部标题栏 -->
    <div class="converter-header">
      <div class="header-content">
        <div class="logo-title">
          <img
              src="../../assets/images/logo.png"
              alt="Logo"
              class="logo-image"
          >
          <h1>GeoJSON 转换工具</h1>
        </div>
        <div class="header-actions">
          <el-button
              type="primary"
              @click="goToHome"
              plain
              size="small"
              class="home-btn"
          >
            <el-icon><HomeFilled /></el-icon>
            回到首页
          </el-button>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 - 左右结构 -->
    <div class="converter-content-wrapper">
      <div class="converter-layout">
        <!-- 左侧区域 -->
        <div class="left-panel">
          <!-- 上传区域 -->
          <el-card class="upload-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>上传GeoJSON文件</span>
                <el-tag type="info">支持.json格式</el-tag>
              </div>
            </template>

            <div class="upload-area" @dragover.prevent="dragover" @drop.prevent="drop">
              <el-upload
                  ref="uploadRef"
                  class="upload-demo"
                  drag
                  action=""
                  :auto-upload="false"
                  :on-change="handleFileChange"
                  :show-file-list="false"
                  accept=".json"
              >
                <div class="upload-inner">
                  <el-icon class="upload-icon"><UploadFilled /></el-icon>
                  <div class="upload-text">
                    <p>拖拽文件到此处或<em>点击上传</em></p>
                    <p class="upload-hint">支持单个GeoJSON文件</p>
                  </div>
                </div>
              </el-upload>

              <div v-if="file" class="file-info">
                <el-icon><Document /></el-icon>
                <span class="file-name">{{ file.name }}</span>
                <span class="file-size">({{ formatFileSize(file.size) }})</span>
                <el-button
                    type="danger"
                    text
                    circle
                    @click="removeFile"
                    class="remove-btn"
                >
                  <el-icon><Close /></el-icon>
                </el-button>
              </div>
            </div>
          </el-card>

          <!-- 预览区域 -->
          <el-card class="preview-card" shadow="hover" v-if="file">
            <template #header>
              <div class="card-header">
                <span>数据预览</span>
                <el-tag type="success" v-if="previewData">已加载</el-tag>
              </div>
            </template>

            <div class="preview-content">
              <el-tabs v-model="activeTab" class="preview-tabs">
                <el-tab-pane label="表格视图" name="table">
                  <div class="table-container">
                    <el-table
                        :data="previewData?.features.slice(0, 10)"
                        height="300"
                        border
                        style="width: 100%"
                        v-loading="loading"
                    >
                      <el-table-column
                          prop="properties.name"
                          label="名称"
                          width="180"
                      />
                      <el-table-column
                          prop="type"
                          label="类型"
                          width="120"
                      />
                      <el-table-column
                          label="坐标"
                      >
                        <template #default="{ row }">
                          {{ formatCoordinates(row.geometry) }}
                        </template>
                      </el-table-column>
                    </el-table>
                    <div class="table-hint" v-if="previewData?.features.length > 10">
                      仅显示前10条记录，共{{ previewData.features.length }}条
                    </div>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="JSON视图" name="json">
                  <div class="json-container">
                    <pre v-if="previewData">{{ formattedJson }}</pre>
                    <div v-else class="empty-preview">
                      <el-icon><Document /></el-icon>
                      <p>暂无数据预览</p>
                    </div>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
          </el-card>
        </div>

        <!-- 右侧区域 -->
        <div class="right-panel">
          <!-- 转换按钮 -->
          <el-card class="action-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>转换操作</span>
              </div>
            </template>
            <div class="action-area">
              <el-button
                  type="primary"
                  @click="convertFile"
                  :disabled="!file || loading"
                  :loading="loading"
                  class="convert-btn"
              >
                <el-icon><Refresh /></el-icon>
                转换为Shapefile
              </el-button>
            </div>
          </el-card>

          <!-- 转换结果展示 -->
          <el-card class="result-card" shadow="hover" v-if="resultPath">
            <template #header>
              <div class="card-header">
                <span>转换结果</span>
                <el-tag type="success">转换成功</el-tag>
              </div>
            </template>

            <div class="result-content">
              <div class="result-path">
                <el-icon><Folder /></el-icon>
                <span class="path-label">文件保存路径:</span>
                <div class="path-value-container">
                  <span class="path-value">{{ resultPath }}</span>
                  <el-tooltip content="复制路径" placement="top">
                    <el-button
                        text
                        circle
                        @click="copyPath"
                        class="copy-btn"
                    >
                      <el-icon><CopyDocument /></el-icon>
                    </el-button>
                  </el-tooltip>
                </div>
              </div>
              <div class="result-hint">
                <el-icon><InfoFilled /></el-icon>
                <span>您可以在上述路径中找到转换后的Shapefile文件</span>
              </div>
            </div>
          </el-card>

          <!-- 状态提示 -->
          <div class="status-area">
            <el-alert
                v-if="errorMessage"
                :title="errorMessage"
                type="error"
                show-icon
                closable
                @close="errorMessage = ''"
            />

            <el-alert
                v-if="successMessage"
                :title="successMessage"
                type="success"
                show-icon
                closable
                @close="successMessage = ''"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  HomeFilled,
  UploadFilled,
  Document,
  Close,
  Refresh,
  Folder,
  InfoFilled,
  CopyDocument
} from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 数据状态
const file = ref(null)
const previewData = ref(null)
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const activeTab = ref('table')
const uploadRef = ref(null)
const resultPath = ref('')

// 格式化JSON显示
const formattedJson = computed(() => {
  return JSON.stringify(previewData.value, null, 2)
})

// 返回首页
const goToHome = () => {
  router.push('/home')
}

// 复制路径
const copyPath = () => {
  if (!resultPath.value) return
  navigator.clipboard.writeText(resultPath.value)
  ElMessage.success('路径已复制到剪贴板')
}

// 处理文件拖放
const dragover = (event) => {
  event.preventDefault()
  event.dataTransfer.dropEffect = 'copy'
}

const drop = (event) => {
  event.preventDefault()
  const files = event.dataTransfer.files
  if (files.length > 0 && files[0].name.endsWith('.json')) {
    handleFileChange({ raw: files[0] })
  }
}

// 处理文件选择
const handleFileChange = (uploadFile) => {
  if (!uploadFile.raw) return

  const selectedFile = uploadFile.raw
  if (!selectedFile.name.endsWith('.json')) {
    errorMessage.value = '请选择JSON格式的文件'
    return
  }

  file.value = selectedFile
  readFile(selectedFile)
}

// 读取文件内容
const readFile = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      previewData.value = JSON.parse(e.target.result)
      successMessage.value = '文件加载成功'
      errorMessage.value = ''
      resultPath.value = '' // 清除之前的结果
    } catch (err) {
      errorMessage.value = '文件解析失败，请检查是否为有效的GeoJSON'
      previewData.value = null
    }
  }
  reader.onerror = () => {
    errorMessage.value = '文件读取失败'
    previewData.value = null
  }
  reader.readAsText(file)
}

// 移除文件
const removeFile = () => {
  file.value = null
  previewData.value = null
  resultPath.value = ''
  uploadRef.value.clearFiles()
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 格式化坐标显示
const formatCoordinates = (geometry) => {
  if (!geometry || !geometry.coordinates) return 'N/A'

  try {
    const coords = geometry.coordinates
    const type = geometry.type

    // 处理不同类型的几何体
    switch (type) {
      case 'Point':
        return `[${coords[0].toFixed(4)}, ${coords[1].toFixed(4)}]`
      case 'LineString':
      case 'MultiPoint':
        return `[${coords[0][0].toFixed(4)}, ${coords[0][1].toFixed(4)}]...`
      case 'Polygon':
      case 'MultiLineString':
        return `[${coords[0][0][0].toFixed(4)}, ${coords[0][0][1].toFixed(4)}]...`
      case 'MultiPolygon':
        return `[${coords[0][0][0][0].toFixed(4)}, ${coords[0][0][0][1].toFixed(4)}]...`
      default:
        return '复杂几何体'
    }
  } catch (err) {
    console.error('格式化坐标出错:', err)
    return '坐标解析错误'
  }
}

// 转换文件
const convertFile = async () => {
  if (!file.value || !previewData.value) return

  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''
  resultPath.value = ''

  try {
    const formData = new FormData()
    formData.append('file', file.value)

    const response = await axios.post('/api/convert/json-to-shp', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      responseType: 'json'
    })

    if (response.data.code === 200) {
      resultPath.value = response.data.data.path
      successMessage.value = '转换成功，文件已保存'
    } else {
      errorMessage.value = response.data.message || '转换失败'
    }
  } catch (error) {
    console.error('转换出错:', error)
    errorMessage.value = error.response?.data?.message || '服务器错误，请稍后再试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 全局样式 */
.converter-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f8fafc;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  position: relative;
  overflow: hidden;
}

/* 顶部标题栏 */
.converter-header {
  background-color: #ffffff;
  color: #2c3e50;
  padding: 16px 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
}

.logo-title {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo-image {
  width: 40px;
  height: 40px;
}

h1 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(90deg, #409EFF, #67C23A);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.home-btn {
  padding: 5px 10px;
  font-size: 12px;
}

.home-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

/* 主要内容区域 */
.converter-content-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background-color: #ffffff;
  scroll-behavior: smooth;
  background-image: linear-gradient(#f5f7fa 1px, transparent 1px),
  linear-gradient(90deg, #f5f7fa 1px, transparent 1px);
  background-size: 20px 20px;
}

.converter-layout {
  display: flex;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
  min-height: 100%;
}

.left-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.right-panel {
  width: 400px;
  min-width: 400px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 卡片样式 */
.upload-card,
.preview-card,
.action-card,
.result-card {
  border-radius: 12px;
  border: none;
  transition: transform 0.3s, box-shadow 0.3s;
}

.upload-card:hover,
.preview-card:hover,
.action-card:hover,
.result-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1) !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 4px;
}

/* 上传区域 */
.upload-area {
  padding: 16px;
}

.upload-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
}

.upload-icon {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 16px;
}

.upload-text {
  text-align: center;
}

.upload-text p {
  margin: 4px 0;
  color: #606266;
}

.upload-text em {
  color: #409EFF;
  font-style: normal;
  font-weight: 500;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
}

.file-info {
  display: flex;
  align-items: center;
  margin-top: 16px;
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 6px;
  font-size: 14px;
}

.file-info .el-icon {
  margin-right: 8px;
  color: #409EFF;
}

.file-name {
  font-weight: 500;
  margin-right: 8px;
}

.file-size {
  color: #909399;
  margin-right: auto;
}

.remove-btn {
  padding: 4px;
}

/* 预览区域 */
.preview-content {
  padding: 0 8px;
}

.preview-tabs {
  height: 100%;
}

:deep(.preview-tabs .el-tabs__header) {
  margin: 0;
}

:deep(.preview-tabs .el-tabs__content) {
  padding: 0;
}

.table-container {
  position: relative;
}

.table-hint {
  font-size: 12px;
  color: #909399;
  text-align: right;
  padding: 8px 0;
}

.json-container {
  height: 300px;
  overflow-y: auto;
  background-color: #f9fafc;
  border-radius: 4px;
  padding: 12px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #333;
}

.empty-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
}

.empty-preview .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-preview p {
  margin: 0;
}

/* 操作区域 */
.action-area {
  display: flex;
  justify-content: center;
  padding: 16px;
}

.convert-btn {
  padding: 12px 24px;
  font-size: 14px;
  transition: all 0.2s;
  width: 100%;
}

.convert-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 结果区域 */
.result-content {
  padding: 16px;
}

.result-path {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 6px;
  margin-bottom: 12px;
  gap: 8px;
}

.result-path .el-icon {
  margin-top: 2px;
  color: #409EFF;
  font-size: 20px;
  flex-shrink: 0;
}

.path-label {
  font-weight: 500;
  flex-shrink: 0;
}

.path-value-container {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-grow: 1;
  min-width: 0;
}

.path-value {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  color: #409EFF;
  word-break: break-all;
  flex-grow: 1;
}

.copy-btn {
  flex-shrink: 0;
  padding: 4px;
}

.result-hint {
  display: flex;
  align-items: center;
  color: #606266;
  font-size: 14px;
  gap: 8px;
}

.result-hint .el-icon {
  color: #67C23A;
  flex-shrink: 0;
}

/* 状态区域 */
.status-area {
  margin-top: auto;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .converter-layout {
    flex-direction: column;
  }

  .right-panel {
    width: 100%;
    min-width: auto;
  }
}

@media (max-width: 768px) {
  .converter-header {
    padding: 12px 16px;
  }

  .logo-image {
    width: 32px;
    height: 32px;
  }

  h1 {
    font-size: 1.25rem;
  }

  .converter-content-wrapper {
    padding: 16px;
  }

  .upload-inner {
    padding: 24px 0;
  }

  .result-path {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .path-value-container {
    width: 100%;
  }
}
</style>