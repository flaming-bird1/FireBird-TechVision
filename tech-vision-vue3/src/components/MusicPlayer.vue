<template>
  <!-- 音频元素 -->
  <audio ref="audioRef" :src="audioSource" @ended="playNext" @error="handleAudioError"></audio>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

// 音频元素引用
const audioRef = ref(null);
// 音乐播放状态
const isPlaying = ref(false);
// 音频源数组
const audioSources = ref([
  '/src/assets/images/慢慢-鹿晗.mp3',
  '/src/assets/images/吹吹风-鹿晗.mp3',
]);
// 当前音频索引
const currentAudioIndex = ref(0);
// 当前音频源路径
const audioSource = ref(audioSources.value[currentAudioIndex.value]);
// 错误信息
const errorMessage = ref('');

const toggleMusic = async () => {
  try {
    if (isPlaying.value) {
      audioRef.value.pause();
    } else {
      await audioRef.value.play();
    }
    isPlaying.value = !isPlaying.value;
  } catch (error) {
    console.error('播放音乐时出错:', error);
    errorMessage.value = '播放音乐失败，请检查音频文件路径是否正确。';
    ElMessage.error(errorMessage.value);
  }
};

const playNext = async () => {
  try {
    currentAudioIndex.value = (currentAudioIndex.value + 1) % audioSources.value.length;
    audioSource.value = audioSources.value[currentAudioIndex.value];
    audioRef.value.src = audioSource.value;

    // 等待音频加载完成
    await new Promise((resolve) => {
      audioRef.value.addEventListener('canplaythrough', resolve, { once: true });
      audioRef.value.load();
    });

    await audioRef.value.play();
  } catch (error) {
    console.error('播放下一首音乐时出错:', error);
    errorMessage.value = '播放下一首音乐失败，请检查音频文件路径是否正确。';
    ElMessage.error(errorMessage.value);
  }
};

const handleAudioError = () => {
  const error = audioRef.value.error;
  if (error) {
    switch (error.code) {
      case error.MEDIA_ERR_ABORTED:
        errorMessage.value = '音频加载被中断';
        break;
      case error.MEDIA_ERR_NETWORK:
        errorMessage.value = '网络错误导致音频加载失败';
        break;
      case error.MEDIA_ERR_DECODE:
        errorMessage.value = '音频解码失败';
        break;
      case error.MEDIA_ERR_SRC_NOT_SUPPORTED:
        errorMessage.value = '不支持的音频格式或文件路径错误';
        break;
      default:
        errorMessage.value = '未知错误，音频加载失败';
    }
    console.error('音频错误:', errorMessage.value);
    ElMessage.error(errorMessage.value);
  }
};

onMounted(() => {
  if (!audioRef.value.canPlayType('audio/mpeg')) {
    errorMessage.value = '当前浏览器不支持MP3格式音频播放';
    console.error(errorMessage.value);
    ElMessage.error(errorMessage.value);
  }
});

// 导出 toggleMusic 和 isPlaying 供外部使用
defineExpose({
  toggleMusic,
  isPlaying
});
</script>

<style scoped>
/* 可以为空，因为不需要样式 */
</style>