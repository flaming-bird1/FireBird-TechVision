<template>
  <div id="app">
    <!-- 使用 keep-alive 确保音乐播放器在路由切换时不被销毁 -->
    <keep-alive>
      <MusicPlayer ref="musicPlayer" />
    </keep-alive>
    <router-view />
  </div>
</template>

<script setup>
import { ref, provide } from 'vue';
import MusicPlayer from './components/MusicPlayer.vue';

const musicPlayer = ref(null);

// 提供音乐播放器实例，供子组件注入使用
provide('musicPlayer', musicPlayer);
</script>

<style scoped>
/* 保持原有的样式或添加全局样式 */
</style>