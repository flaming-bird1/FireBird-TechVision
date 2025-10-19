<template>
  <div class="note-detail">
    <el-page-header @back="goBack" title="返回">
      <template #content>
        <span class="text-large font-600 mr-3">{{ noteTitle }}</span>
      </template>
    </el-page-header>

    <MarkdownViewer
        v-if="noteFile"
        :file-path="`/notes/${noteFile}`"
    />
    <div v-else class="not-found">
      <el-empty description="未找到该笔记" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MarkdownViewer from '@/components/MarkdownViewer.vue'
import { ElPageHeader, ElEmpty } from 'element-plus'

const route = useRoute()
const router = useRouter()

// 笔记映射关系
const noteMap = {
  '1': {
    file: 'pytorchStudy.md',
    title: 'PyTorch学习笔记'
  }
}

const noteFile = computed(() => noteMap[route.params.id]?.file || '')
const noteTitle = computed(() => noteMap[route.params.id]?.title || '未知笔记')

const goBack = () => {
  router.push('/')
}
</script>

<style scoped>
.note-detail {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.not-found {
  margin-top: 50px;
  text-align: center;
}
</style>