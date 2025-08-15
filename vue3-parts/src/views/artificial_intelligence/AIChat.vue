<template>
  <div class="ai-chat-container">
    <!-- 顶部标题栏 - 保持原有样式 -->
    <div class="chat-header">
      <div class="header-content">
        <div class="logo-title">
          <img
              src="../../assets/images/logo.png"
              alt="Logo"
              class="logo-image"
          >
          <h1>AI 聊天助手</h1>
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
          <el-button
              type="danger"
              @click="clearHistory"
              plain
              size="small"
              class="clear-btn"
          >
            <el-icon><Delete /></el-icon>
            清空记录
          </el-button>
        </div>
      </div>
    </div>

    <!-- 聊天内容区域 - 改进设计 -->
    <div class="chat-content-wrapper" ref="chatContent">
      <div class="chat-content">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome-message">
          <div class="welcome-icon">
            <el-icon><ChatRound /></el-icon>
          </div>
          <h3>欢迎使用AI聊天助手</h3>
          <p>有什么问题可以随时向我提问，我会尽力为您解答</p>
          <div class="welcome-examples">
            <div class="example-title">你可以尝试问我：</div>
            <div class="example-list">
              <div class="example-item" @click="inputMessage = '如何提高深度学习模型的准确率？'; sendMessage()">
                如何提高深度学习模型的准确率？
              </div>
              <div class="example-item" @click="inputMessage = '请解释卷积神经网络的工作原理'; sendMessage()">
                请解释卷积神经网络的工作原理
              </div>
              <div class="example-item" @click="inputMessage = 'Python中如何处理大数据集？'; sendMessage()">
                Python中如何处理大数据集？
              </div>
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div
            v-for="(message, index) in messages"
            :key="index"
            :class="['message-item', message.role]"
        >
          <div class="message-avatar">
            <el-avatar :src="message.role === 'user' ? userAvatar : aiAvatar" />
          </div>
          <div class="message-content">
            <div class="message-role">
              {{ message.role === 'user' ? '我' : 'AI助手' }}
              <span class="message-time">{{ formatTime(message.timestamp) }}</span>
            </div>
            <div
                class="message-text"
                v-html="message.content"
            ></div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-indicator">
          <div class="typing-indicator">
            <div class="dot"></div>
            <div class="dot"></div>
            <div class="dot"></div>
          </div>
          <span>AI正在思考中...</span>
        </div>
      </div>
    </div>

    <!-- 底部输入区域 - 改进设计 -->
    <div class="input-area">
      <el-card class="input-card" shadow="hover">
        <div class="input-container">
          <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="2"
              :maxlength="1000"
              placeholder="请输入您的问题..."
              resize="none"
              @keyup.enter.exact="sendMessage"
              class="message-input"
          ></el-input>
          <div class="action-buttons">
            <el-button
                type="primary"
                @click="sendMessage"
                :disabled="!inputMessage.trim() || loading"
                :loading="loading"
                class="send-btn"
            >
              <el-icon><Promotion /></el-icon>
              发送
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import DOMPurify from 'dompurify'
import { useRouter } from 'vue-router'
import {
  Delete,
  ChatRound,
  Promotion,
  HomeFilled
} from '@element-plus/icons-vue'
import axios from 'axios'
import userAvatarImage from "@/assets/images/烈焰飞鸟.png";
import aiAvatarImage from "@/assets/images/aiAvatar.png";

const router = useRouter()

// 配置marked
marked.setOptions({
  highlight: function (code, lang) {
    const language = hljs.getLanguage(lang) ? lang : 'plaintext'
    return hljs.highlight(code, { language }).value
  },
  breaks: true,
  gfm: true
})

// 头像设置
const userAvatar = userAvatarImage
const aiAvatar = aiAvatarImage

// 消息数据
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const chatContent = ref(null)

// 返回首页
const goToHome = () => {
  router.push('/home')
}

// 发送消息
const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message || loading.value) return

  // 添加用户消息
  const userMsg = {
    role: 'user',
    content: DOMPurify.sanitize(marked.parse(message)),
    timestamp: new Date()
  }
  messages.value.push(userMsg)

  inputMessage.value = ''
  loading.value = true
  scrollToBottom()

  try {
    // 调用后端API
    const response = await axios.get('/spring/AI-chat', {
      params: { param: message }
    })

    // 添加AI回复
    if (response.data && response.data.data) {
      messages.value.push({
        role: 'assistant',
        content: DOMPurify.sanitize(marked.parse(response.data.data)),
        timestamp: new Date()
      })
    } else {
      messages.value.push({
        role: 'assistant',
        content: DOMPurify.sanitize('抱歉，未获取到有效回复。'),
        timestamp: new Date()
      })
    }
  } catch (error) {
    console.error('API调用失败:', error)
    messages.value.push({
      role: 'assistant',
      content: DOMPurify.sanitize('抱歉，我遇到了一个问题，请稍后再试。'),
      timestamp: new Date()
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

// 清空聊天记录
const clearHistory = () => {
  if (messages.value.length > 0) {
    messages.value = []
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContent.value) {
      chatContent.value.scrollTop = chatContent.value.scrollHeight
    }
  })
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

// 监听消息变化，自动滚动到底部
watch(messages, () => {
  scrollToBottom()
})

// 初始化
onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
/* 全局样式 */
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f8fafc;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  position: relative;
  overflow: hidden;
}

/* 顶部标题栏 - 保持原有样式 */
.chat-header {
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

.clear-btn {
  padding: 5px 10px;
  font-size: 12px;
}

.home-btn:hover, .clear-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

/* 聊天内容区域 - 改进设计 */
.chat-content-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 16px 24px;
  background-color: #ffffff;
  scroll-behavior: smooth;
  background-image: linear-gradient(#f5f7fa 1px, transparent 1px),
  linear-gradient(90deg, #f5f7fa 1px, transparent 1px);
  background-size: 20px 20px;
}

.chat-content {
  max-width: 900px;
  margin: 0 auto;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  padding-bottom: 20px;
}

/* 欢迎消息 */
.welcome-message {
  text-align: center;
  padding: 60px 20px;
  color: #7f8c8d;
  margin: auto;
}

.welcome-icon {
  font-size: 48px;
  margin-bottom: 15px;
  color: #bdc3c7;
}

.welcome-message h3 {
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-size: 1.5rem;
  font-weight: normal;
}

.welcome-message p {
  margin: 0;
  font-size: 1rem;
}

.welcome-examples {
  margin-top: 32px;
  text-align: left;
}

.example-title {
  font-size: 14px;
  color: #7f8c8d;
  margin-bottom: 12px;
  text-align: center;
}

.example-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.example-item {
  padding: 12px 16px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  color: #2c3e50;
  border: 1px solid #e2e8f0;
}

.example-item:hover {
  background-color: #f8f9fa;
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

/* 消息项 */
.message-item {
  display: flex;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease-out;
  max-width: 80%;
}

.message-item.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-item.assistant {
  align-self: flex-start;
}

.message-avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  margin-top: 4px;
}

.message-item.user .message-avatar {
  margin-left: 12px;
}

.message-item.assistant .message-avatar {
  margin-right: 12px;
}

.message-content {
  max-width: calc(100% - 48px);
}

.message-role {
  font-size: 12px;
  margin-bottom: 6px;
  color: #7f8c8d;
  display: flex;
  align-items: center;
}

.message-item.user .message-role {
  justify-content: flex-end;
}

.message-time {
  margin-left: 8px;
  font-size: 11px;
  color: #bdc3c7;
}

.message-text {
  line-height: 1.6;
  word-break: break-word;
  padding: 12px 16px;
  border-radius: 12px;
  position: relative;
  font-size: 15px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.message-item.user .message-text {
  background-color: #95ec69;
  color: #000;
  border-top-right-radius: 0;
}

.message-item.assistant .message-text {
  background-color: #ffffff;
  color: #2c3e50;
  border-top-left-radius: 0;
  border: 1px solid #e2e8f0;
}

/* Markdown 样式优化 */
.message-text :deep(p) {
  margin: 0 0 10px 0;
}

.message-text :deep(p:last-child) {
  margin-bottom: 0;
}

.message-text :deep(pre) {
  background-color: rgba(0, 0, 0, 0.05);
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 10px 0;
}

.message-item.user .message-text :deep(pre) {
  background-color: rgba(0, 0, 0, 0.1);
}

.message-text :deep(code) {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 0.9em;
}

.message-text :deep(blockquote) {
  border-left: 3px solid #e2e8f0;
  padding-left: 12px;
  margin: 10px 0;
  color: #666;
}

.message-item.user .message-text :deep(blockquote) {
  border-left-color: rgba(0, 0, 0, 0.2);
}

/* 加载状态 */
.loading-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  margin: 10px auto;
  max-width: 300px;
}

.typing-indicator {
  display: flex;
  margin-bottom: 8px;
}

.typing-indicator .dot {
  width: 8px;
  height: 8px;
  background-color: #409EFF;
  border-radius: 50%;
  margin: 0 4px;
  animation: bounce 1.4s infinite ease-in-out;
}

.typing-indicator .dot:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator .dot:nth-child(2) {
  animation-delay: -0.16s;
}

/* 底部输入区域 - 改进设计 */
.input-area {
  padding: 16px 24px;
  background-color: #f8fafc;
  border-top: 1px solid #e4e7ed;
}

.input-card {
  border-radius: 12px;
  border: none;
  max-width: 900px;
  margin: 0 auto;
  transition: transform 0.3s, box-shadow 0.3s;
}

.input-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1) !important;
}

.input-container {
  padding: 8px;
}

.message-input {
  border: none;
  padding: 8px;
  font-size: 15px;
}

.message-input:deep(.el-textarea__inner) {
  box-shadow: none !important;
  border: none !important;
  background-color: transparent;
  font-family: inherit;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.send-btn {
  padding: 8px 20px;
  border-radius: 4px;
  font-size: 14px;
  width: 120px;
  transition: all 0.2s;
}

.send-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

/* 动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-6px);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-header {
    padding: 12px 16px;
  }

  .logo-image {
    width: 32px;
    height: 32px;
  }

  h1 {
    font-size: 1.25rem;
  }

  .chat-content-wrapper {
    padding: 12px 16px;
  }

  .input-area {
    padding: 12px 16px;
  }

  .message-item {
    max-width: 90%;
  }

  .message-text {
    font-size: 14px;
    padding: 10px 14px;
  }

  .welcome-message {
    padding: 40px 16px;
  }

  .header-actions {
    flex-direction: column;
    gap: 4px;
  }

  .home-btn, .clear-btn {
    width: 100%;
    margin: 2px 0;
  }
}
</style>