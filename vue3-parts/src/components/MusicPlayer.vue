<template>
  <div style="display: none;">
    <audio ref="audioRef" :src="audioSource" @ended="playNext" @error="handleAudioError"></audio>
  </div>
</template>

<script setup>
import { ref, onMounted, provide } from 'vue';
import { ElMessage } from 'element-plus';

// 音频源数组（保持原始路径格式）
const audioSources = ref([
  '/src/assets/images/慢慢-鹿晗.mp3',
  '/src/assets/images/吹吹风-鹿晗.mp3',
]);

// 音频元素引用
const audioRef = ref(null);
// 音乐播放状态
const isPlaying = ref(false);
// 当前音频索引
const currentAudioIndex = ref(0);
// 当前音频源路径
const audioSource = ref(audioSources.value[currentAudioIndex.value]);
// 错误信息
const errorMessage = ref('');

// 播放/暂停音乐
const toggleMusic = async () => {
  console.log('MusicPlayer: toggleMusic 方法被调用');
  console.log('audioRef:', audioRef.value);
  try {

    if (isPlaying.value) {
      audioRef.value.pause();
    } else {
      // 确保音频加载完成
      if (audioRef.value.readyState < 2) {
        await new Promise((resolve) => {
          audioRef.value.addEventListener('canplaythrough', resolve, { once: true });
          audioRef.value.load();
        });
      }
      await audioRef.value.play();
    }
    isPlaying.value = !isPlaying.value;
  } catch (error) {
    console.error('播放音乐时出错:', error);
    errorMessage.value = '播放音乐失败，请检查音频文件路径是否正确。';
    ElMessage.error(errorMessage.value);
  }
};

// 播放下一首
const playNext = async () => {
  try {
    currentAudioIndex.value = (currentAudioIndex.value + 1) % audioSources.value.length;
    audioSource.value = audioSources.value[currentAudioIndex.value];
    audioRef.value.src = audioSource.value;

    // 重新加载并播放
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

// 处理音频错误
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
  if (!audioRef.value || !audioRef.value.canPlayType('audio/mpeg')) {
    errorMessage.value = '当前浏览器不支持MP3格式音频播放';
    console.error(errorMessage.value);
    ElMessage.error(errorMessage.value);
  }
});

// 提供音乐播放器实例供其他组件使用
const musicPlayer = {
  toggleMusic,
  isPlaying,
  audioSource
};

provide('musicPlayer', musicPlayer);
</script>

<style scoped>
/* 组件不显示任何UI，仅作为功能提供者 */
</style>