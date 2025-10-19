<template>
  <div class="markdown-container">
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">加载失败: {{ error }}</div>
    <div v-else class="markdown-content" v-html="renderedMarkdown"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import DOMPurify from 'dompurify'

const props = defineProps({
  filePath: String
})

const renderedMarkdown = ref('')
const loading = ref(true)
const error = ref(null)

const renderMarkdown = async (path) => {
  try {
    loading.value = true
    error.value = null

    // 从public/notes目录加载Markdown文件
    const response = await fetch(path)
    if (!response.ok) throw new Error('笔记加载失败')

    const markdownText = await response.text()

    // 配置marked
    marked.setOptions({
      highlight: function(code, lang) {
        return hljs.highlightAuto(code, [lang]).value
      }
    })

    // 渲染并净化Markdown
    renderedMarkdown.value = DOMPurify.sanitize(marked(markdownText))
  } catch (err) {
    error.value = err.message
    console.error('加载Markdown失败:', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => renderMarkdown(props.filePath))
watch(() => props.filePath, (newPath) => renderMarkdown(newPath))
</script>

<style scoped>
.markdown-container {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
  min-height: 80vh;
}

.markdown-content {
  line-height: 1.6;
}

.markdown-content >>> pre {
  background-color: #f6f8fa;
  padding: 16px;
  border-radius: 6px;
  overflow: auto;
}

.markdown-content >>> code {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  background-color: #f6f8fa;
  padding: 2px 4px;
  border-radius: 4px;
}

.markdown-content >>> img {
  max-width: 100%;
  border-radius: 4px;
}

.loading, .error {
  text-align: center;
  padding: 40px;
  font-size: 16px;
}

.error {
  color: #f56c6c;
}
</style>