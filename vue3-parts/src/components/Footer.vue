<!-- src/components/Footer.vue -->
<template>
  <footer class="app-footer">
    <div class="footer-content">
      <div class="footer-logo">
        <img src="../assets/images/logo.png" alt="Logo" width="40">
        <span>烈焰飞鸟·梦工厂</span>
      </div>
      <div class="footer-links">
        <a href="#">关于我们</a>
        <a href="#">技术博客</a>
        <a href="https://gitcode.com/lieyanfeiniao_">GitCode</a>
        <a href="#">联系方式</a>
      </div>
      <div class="copyright">
        © 2025 烈焰飞鸟·梦工厂 版权所有
      </div>
    </div>
  </footer>
</template>

<script setup>
// 这里可以添加组件逻辑，如果需要的话
</script>

<style scoped>
.app-footer {
  background-color: #2c3e50;
  color: #ffffff;
  padding: 24px 0;
}

.footer-content {
  max-width: 1200px;
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

.footer-links {
  display: flex;
  gap: 24px;
}

.footer-links a {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #ffffff;
}

.copyright {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
}

@media (max-width: 768px) {
  .footer-links {
    flex-wrap: wrap;
    justify-content: center;
    gap: 12px;
  }
}
</style>