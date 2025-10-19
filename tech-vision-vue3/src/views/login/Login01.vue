<template>
  <div class="login-page" @mousemove="handleMouseMove">
    <div class="login-container">
      <div class="floating-icons">
        <span
            v-for="(icon, index) in icons"
            :key="index"
            :style="{
            left: icon.left + 'px',
            top: icon.top + 'px',
            width: icon.size + 'px',
            height: icon.size + 'px',
            animationDuration: (10 + Math.random() * 20) + 's',
            animationDelay: (Math.random() * 5) + 's'
          }"
        ></span>
      </div>
      <h2>欢迎登录</h2>
      <form @submit.prevent="handleSubmit">
        <div class="input-group">
          <input type="text" required v-model="username">
          <label>用户名</label>
        </div>
        <div class="input-group">
          <input type="password" required v-model="password">
          <label>密码</label>
        </div>
        <button type="submit" class="btn">登 录</button>
        <div class="links">
          <a href="#" @mouseenter="changeBgColor('#a1c4fd')">忘记密码?</a>
          <a href="#" @mouseenter="changeBgColor('#fbc2eb')">注册账号</a>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const username = ref('');
const password = ref('');
const icons = ref([]);

const createIcons = () => {
  const iconsCount = 15;
  const newIcons = [];
  for (let i = 0; i < iconsCount; i++) {
    newIcons.push({
      left: Math.random() * 300,
      top: Math.random() * 400,
      size: 20 + Math.random() * 30
    });
  }
  icons.value = newIcons;
};

const handleSubmit = () => {
  if (username.value && password.value) {
    alert(`欢迎, ${username.value}!`);
  }
};

const changeBgColor = (color) => {
  document.querySelector('.login-page').style.background = `linear-gradient(45deg, ${color}, ${color.replace(')', '')}80)`;
};

const handleMouseMove = (e) => {
  const x = e.clientX / window.innerWidth;
  const y = e.clientY / window.innerHeight;
  document.querySelector('.login-page').style.background = `linear-gradient(${x * 180}deg, #ff9a9e, #fad0c4)`;
  document.querySelector('.login-container').style.transform = `translateY(${y * -10}px)`;
};

onMounted(() => {
  createIcons();
});
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(45deg, #ff9a9e, #fad0c4);
  transition: background 0.5s ease;
}

.login-container {
  position: relative;
  width: 380px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 25px 45px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
  transition: all 0.3s ease;
}

.login-container:hover {
  transform: translateY(-5px);
  box-shadow: 0 30px 50px rgba(0, 0, 0, 0.15);
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: 0.5s;
}

.login-container:hover::before {
  left: 100%;
}

h2 {
  color: #fff;
  text-align: center;
  margin-bottom: 30px;
  font-size: 2em;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.input-group {
  position: relative;
  margin-bottom: 30px;
}

.input-group input {
  width: 100%;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  outline: none;
  border-radius: 35px;
  color: #fff;
  font-size: 16px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.input-group input:focus,
.input-group input:hover {
  background: rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.input-group input:valid {
  background: rgba(255, 255, 255, 0.25);
}

.input-group label {
  position: absolute;
  top: 15px;
  left: 20px;
  color: rgba(255, 255, 255, 0.8);
  pointer-events: none;
  transition: all 0.3s ease;
}

.input-group input:focus + label,
.input-group input:valid + label {
  top: -10px;
  left: 15px;
  font-size: 12px;
  background: rgba(255, 255, 255, 0.2);
  padding: 0 10px;
  border-radius: 10px;
}

.btn {
  position: relative;
  width: 100%;
  padding: 15px;
  background: linear-gradient(45deg, #ff9a9e, #fad0c4);
  border: none;
  outline: none;
  border-radius: 35px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
}

.btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: 0.5s;
}

.btn:hover::before {
  left: 100%;
}

.links {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}

.links a {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s ease;
}

.links a:hover {
  color: #fff;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

.floating-icons {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -1;
}

.floating-icons span {
  position: absolute;
  display: block;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  animation: float 15s linear infinite;
  opacity: 0;
  transition: opacity 0.5s ease;
}

.login-container:hover .floating-icons span {
  opacity: 1;
}

@keyframes float {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translateY(-1000px) rotate(720deg);
    opacity: 0;
  }
}
</style>