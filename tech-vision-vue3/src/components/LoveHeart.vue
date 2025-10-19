<template>
  <div class="love-container" ref="container">
    <!-- 倒计时显示 -->
    <div class="countdown" v-if="showCountdown">
<!--      距离520还有 {{ daysRemaining }} 天{{ hoursRemaining > 0 ? ` ${hoursRemaining}小时` : '' }}{{ minutesRemaining > 0 ? ` ${minutesRemaining}分钟` : '' }}-->
    很高心遇见你
    </div>

    <!-- 点击后的表白文字 -->
    <transition name="fade">
      <div class="message" v-if="showMessage" @click="hideMessage">
        <h1>{{ loveMessage }}</h1>
        <p>{{ subMessage }}</p>
        <div class="hearts-falling">
          <div v-for="n in 30" :key="n" class="heart-fall" :style="heartStyle(n)">❤️</div>
        </div>
      </div>
    </transition>

    <!-- 音乐控制按钮 -->
    <button class="music-btn" @click="toggleMusic">
      {{ isMusicPlaying ? '🔊' : '🔇' }}
    </button>

    <!-- 花瓣飘落效果 -->
    <div class="petals" v-if="showPetals">
      <div v-for="n in 50" :key="n" class="petal" :style="petalStyle(n)"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls'
import { EffectComposer } from 'three/examples/jsm/postprocessing/EffectComposer'
import { RenderPass } from 'three/examples/jsm/postprocessing/RenderPass'
import { UnrealBloomPass } from 'three/examples/jsm/postprocessing/UnrealBloomPass'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader'
import gsap from 'gsap'

// 配置参数
const props = defineProps({
  loveMessage: {
    type: String,
    default: '从大一看见你的第一眼，我就心动了！'
  },
  subMessage: {
    type: String,
    default: '你是我生命中最美的遇见 ❤️'
  },
  showCountdown: {
    type: Boolean,
    default: true
  },
  showPetals: {
    type: Boolean,
    default: true
  },
  heartColor: {
    type: String,
    default: '#ff3366'
  },
  roseColor: {
    type: String,
    default: '#e91e63'
  },
  particleColor: {
    type: String,
    default: '#ff99cc'
  },
  bgColor: {
    type: String,
    default: '#000000'
  },
  musicUrl: {
    type: String,
    default: 'https://music.163.com/song/media/outer/url?id=1824045033.mp3'
  }
})

// 响应式数据
const container = ref(null)
const showMessage = ref(false)
const isMusicPlaying = ref(false)
const audioElement = ref(null)
const petals = ref([])
const hearts = ref([])

// 计算剩余时间
const now = ref(new Date())
const targetDate = computed(() => {
  const date = new Date(now.value.getFullYear(), 4, 20, 13, 14) // 5月20日13:14
  if (now.value > date) date.setFullYear(date.getFullYear() + 1)
  return date
})

const timeRemaining = computed(() => targetDate.value - now.value)
const daysRemaining = computed(() => Math.floor(timeRemaining.value / (1000 * 60 * 60 * 24)))
const hoursRemaining = computed(() => Math.floor((timeRemaining.value % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)))
const minutesRemaining = computed(() => Math.floor((timeRemaining.value % (1000 * 60 * 60)) / (1000 * 60)))

// Three.js相关变量
let scene, camera, renderer, heartMesh, roseMesh, particles, composer, bloomPass
let controls, clock, uniforms, loader
let isHeartRising = false
let heartRiseProgress = 0

// 初始化3D场景
function initThreeJS() {
  // 创建场景
  scene = new THREE.Scene()
  scene.background = new THREE.Color(props.bgColor)
  scene.fog = new THREE.FogExp2(0x000000, 0.002)

  // 创建相机
  camera = new THREE.PerspectiveCamera(
      75,
      window.innerWidth / window.innerHeight,
      0.1,
      1000
  )
  camera.position.z = 30

  // 创建渲染器
  renderer = new THREE.WebGLRenderer({
    antialias: true,
    alpha: true,
    powerPreference: "high-performance"
  })
  renderer.setSize(window.innerWidth, window.innerHeight)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  container.value.appendChild(renderer.domElement)

  // 添加轨道控制器
  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.05
  controls.minDistance = 15
  controls.maxDistance = 50

  // 创建爱心几何体
  createHeart()

  // 创建玫瑰花
  createRose()

  // 创建粒子系统
  createParticles()

  // 添加光效
  addLights()

  // 设置后期处理 (辉光效果)
  setupPostProcessing()

  // 动画时钟
  clock = new THREE.Clock()

  // 处理窗口大小变化
  window.addEventListener('resize', onWindowResize)

  // 开始动画循环
  animate()

  // 更新时间
  setInterval(() => {
    now.value = new Date()
  }, 1000)
}

// 创建爱心模型
function createHeart() {
  loader = new GLTFLoader()
  loader.load(
      '',
      (gltf) => {
        heartMesh = gltf.scene.children[0]
        heartMesh.scale.set(5, 5, 5)
        heartMesh.position.y = -10
        heartMesh.position.z = 0
        heartMesh.material = new THREE.MeshPhongMaterial({
          color: new THREE.Color(props.heartColor),
          emissive: 0xff3366,
          emissiveIntensity: 0.5,
          shininess: 100,
          flatShading: true,
          transparent: true,
          opacity: 0.9
        })
        scene.add(heartMesh)

        // 添加点击事件
        renderer.domElement.addEventListener('click', onClickHeart, false)
      },
      undefined,
      (error) => {
        console.error('加载爱心模型失败:', error)
        // 如果加载失败，使用原始几何体
        createFallbackHeart()
      }
  )
}

// 创建玫瑰花模型
function createRose() {
  // 创建玫瑰花几何体 (简化版)
  const roseGeometry = new THREE.Group()

  // 创建花瓣
  for (let i = 0; i < 20; i++) {
    const petalGeometry = new THREE.CylinderGeometry(0.1, 0.3, 0.8, 8, 1, true)
    const petalMaterial = new THREE.MeshPhongMaterial({
      color: new THREE.Color(props.roseColor),
      emissive: 0xe91e63,
      emissiveIntensity: 0.3,
      shininess: 50,
      flatShading: true,
      transparent: true,
      opacity: 0.9
    })

    const petal = new THREE.Mesh(petalGeometry, petalMaterial)
    const angle = (i / 20) * Math.PI * 2
    const radius = 1.5 + Math.random() * 0.5
    const height = i * 0.2

    petal.position.set(
        Math.cos(angle) * radius,
        height,
        Math.sin(angle) * radius
    )

    petal.scale.set(
        1 + Math.random() * 0.5,
        0.5 + Math.random() * 0.3,
        0.3 + Math.random() * 0.2
    )

    petal.rotation.x = Math.random() * Math.PI
    petal.rotation.y = Math.random() * Math.PI

    roseGeometry.add(petal)
  }

  // 创建花茎
  const stemGeometry = new THREE.CylinderGeometry(0.1, 0.2, 5, 8)
  const stemMaterial = new THREE.MeshPhongMaterial({
    color: 0x4caf50,
    shininess: 30
  })
  const stem = new THREE.Mesh(stemGeometry, stemMaterial)
  stem.position.y = -2.5
  roseGeometry.add(stem)

  // 创建叶子
  const leafGeometry = new THREE.PlaneGeometry(0.8, 0.4, 4)
  const leafMaterial = new THREE.MeshPhongMaterial({
    color: 0x4caf50,
    shininess: 20,
    side: THREE.DoubleSide
  })

  for (let i = 0; i < 4; i++) {
    const leaf = new THREE.Mesh(leafGeometry, leafMaterial)
    leaf.scale.set(1, 0.3, 0.5)
    leaf.position.set(
        i % 2 === 0 ? 0.5 : -0.5,
        -1 - i * 0.5,
        0
    )
    leaf.rotation.z = i % 2 === 0 ? 0.5 : -0.5
    leaf.rotation.x = Math.PI / 2
    roseGeometry.add(leaf)
  }

  const roseMaterial = new THREE.MeshPhongMaterial({
    color: new THREE.Color(props.roseColor),
    emissive: 0xe91e63,
    emissiveIntensity: 0.3,
    shininess: 50,
    flatShading: true,
    transparent: true,
    opacity: 0.9
  })

  roseMesh = roseGeometry
  roseMesh.scale.set(3, 3, 3)
  roseMesh.position.set(10, -5, 0)
  scene.add(roseMesh)
}

// 创建备用爱心模型
function createFallbackHeart() {
  const heartShape = new THREE.Shape()
  const x = 0, y = 0
  const scale = 5

  heartShape.moveTo(x + 0.5 * scale, y + 0.5 * scale)
  heartShape.bezierCurveTo(
      x + 0.5 * scale, y + 0.8 * scale,
      x + 0.2 * scale, y + 1.0 * scale,
      x, y + 0.8 * scale
  )
  heartShape.bezierCurveTo(
      x - 0.6 * scale, y + 0.4 * scale,
      x - 0.3 * scale, y - 0.4 * scale,
      x + 0.5 * scale, y - 0.8 * scale
  )
  heartShape.bezierCurveTo(
      x + 1.3 * scale, y - 0.4 * scale,
      x + 1.6 * scale, y + 0.4 * scale,
      x, y + 0.8 * scale
  )

  const extrudeSettings = {
    steps: 2,
    depth: 2,
    bevelEnabled: true,
    bevelThickness: 0.5,
    bevelSize: 0.5,
    bevelOffset: 0,
    bevelSegments: 8
  }

  const geometry = new THREE.ExtrudeGeometry(heartShape, extrudeSettings)
  const material = new THREE.MeshPhongMaterial({
    color: new THREE.Color(props.heartColor),
    emissive: 0xff3366,
    emissiveIntensity: 0.2,
    shininess: 100,
    flatShading: true,
    transparent: true,
    opacity: 0.9
  })

  heartMesh = new THREE.Mesh(geometry, material)
  heartMesh.rotation.x = Math.PI
  heartMesh.position.y = -10
  scene.add(heartMesh)

  // 添加点击事件
  renderer.domElement.addEventListener('click', onClickHeart, false)
}

// 创建粒子系统
function createParticles() {
  const particleCount = 2000
  const positions = new Float32Array(particleCount * 3)
  const colors = new Float32Array(particleCount * 3)
  const color = new THREE.Color(props.particleColor)

  for (let i = 0; i < particleCount; i++) {
    // 随机位置在爱心周围
    const radius = 10 + Math.random() * 10
    const theta = Math.random() * Math.PI * 2
    const phi = Math.random() * Math.PI

    positions[i * 3] = radius * Math.sin(phi) * Math.cos(theta)
    positions[i * 3 + 1] = radius * Math.sin(phi) * Math.sin(theta)
    positions[i * 3 + 2] = radius * Math.cos(phi)

    // 随机颜色变化
    colors[i * 3] = color.r * (0.5 + Math.random() * 0.5)
    colors[i * 3 + 1] = color.g * (0.5 + Math.random() * 0.5)
    colors[i * 3 + 2] = color.b * (0.5 + Math.random() * 0.5)
  }

  const geometry = new THREE.BufferGeometry()
  geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3))

  const material = new THREE.PointsMaterial({
    size: 0.2,
    vertexColors: true,
    transparent: true,
    opacity: 0.8,
    blending: THREE.AdditiveBlending
  })

  particles = new THREE.Points(geometry, material)
  scene.add(particles)
}

// 添加光源
function addLights() {
  const ambientLight = new THREE.AmbientLight(0x404040)
  scene.add(ambientLight)

  const directionalLight = new THREE.DirectionalLight(0xffffff, 1)
  directionalLight.position.set(1, 1, 1)
  scene.add(directionalLight)

  const pointLight = new THREE.PointLight(0xff3366, 2, 50)
  pointLight.position.set(0, 0, 10)
  scene.add(pointLight)

  // 添加环境光
  const hemiLight = new THREE.HemisphereLight(0xffffbb, 0x080820, 0.5)
  scene.add(hemiLight)

  // 添加聚光灯照亮玫瑰花
  const spotLight = new THREE.SpotLight(0xff69b4, 2, 30, Math.PI / 6, 0.5)
  spotLight.position.set(10, 10, 10)
  spotLight.target.position.set(10, -5, 0)
  scene.add(spotLight)
  scene.add(spotLight.target)
}

// 设置后期处理
function setupPostProcessing() {
  composer = new EffectComposer(renderer)
  composer.addPass(new RenderPass(scene, camera))

  bloomPass = new UnrealBloomPass(
      new THREE.Vector2(window.innerWidth, window.innerHeight),
      1.5, 0.4, 0.85
  )
  bloomPass.threshold = 0
  bloomPass.strength = 1.5
  bloomPass.radius = 0.5
  composer.addPass(bloomPass)
}

// 窗口大小变化处理
function onWindowResize() {
  camera.aspect = window.innerWidth / window.innerHeight
  camera.updateProjectionMatrix()
  renderer.setSize(window.innerWidth, window.innerHeight)
  composer.setSize(window.innerWidth, window.innerHeight)
}

// 爱心升起动画
function startHeartRising() {
  isHeartRising = true
  heartRiseProgress = 0

  // 玫瑰花动画
  gsap.to(roseMesh.position, {
    x: 0,
    y: 0,
    duration: 3,
    ease: "power2.inOut"
  })

  gsap.to(roseMesh.rotation, {
    y: Math.PI * 2,
    duration: 6,
    repeat: -1,
    ease: "none"
  })
}

// 动画循环
function animate() {
  requestAnimationFrame(animate)

  const delta = clock.getDelta()
  const time = clock.getElapsedTime()

  // 爱心跳动动画
  if (heartMesh) {
    heartMesh.rotation.z = time * 0.1
    heartMesh.scale.x = 1 + Math.sin(time * 2) * 0.05
    heartMesh.scale.y = 1 + Math.sin(time * 2) * 0.05

    // 爱心升起动画
    if (isHeartRising && heartRiseProgress < 1) {
      heartRiseProgress += 0.005
      heartMesh.position.y = -10 + heartRiseProgress * 15
      heartMesh.position.z = heartRiseProgress * 5
    }
  }

  // 玫瑰花动画
  if (roseMesh) {
    roseMesh.rotation.y = time * 0.1
  }

  // 粒子动画
  if (particles) {
    particles.rotation.x = time * 0.1
    particles.rotation.y = time * 0.1

    const positions = particles.geometry.attributes.position.array
    for (let i = 0; i < positions.length; i += 3) {
      // 让粒子围绕爱心运动
      positions[i] += Math.sin(time + i) * 0.01
      positions[i + 1] += Math.cos(time + i) * 0.01
      positions[i + 2] += Math.sin(time + i * 0.1) * 0.01
    }
    particles.geometry.attributes.position.needsUpdate = true
  }

  // 更新控制器
  controls.update()

  // 渲染场景
  composer.render()
}

// 点击爱心事件
function onClickHeart(event) {
  // 检查是否点击了爱心
  const mouse = new THREE.Vector2(
      (event.clientX / window.innerWidth) * 2 - 1,
      -(event.clientY / window.innerHeight) * 2 + 1
  )

  const raycaster = new THREE.Raycaster()
  raycaster.setFromCamera(mouse, camera)

  const intersects = raycaster.intersectObjects([heartMesh, roseMesh])
  if (intersects.length > 0) {
    showMessage.value = true
    playSound()
    startHeartRising()

    // 爱心点击动画
    gsap.to(heartMesh.scale, {
      x: 1.5,
      y: 1.5,
      z: 1.5,
      duration: 0.5,
      yoyo: true,
      repeat: 1,
      ease: "power2.inOut"
    })

    // 玫瑰花点击动画
    gsap.to(roseMesh.scale, {
      x: 3.5,
      y: 3.5,
      z: 3.5,
      duration: 0.5,
      yoyo: true,
      repeat: 1,
      ease: "power2.inOut"
    })

    // 粒子爆发效果
    if (particles) {
      const positions = particles.geometry.attributes.position.array
      for (let i = 0; i < positions.length; i += 3) {
        positions[i] *= 1.5
        positions[i + 1] *= 1.5
        positions[i + 2] *= 1.5
      }
      particles.geometry.attributes.position.needsUpdate = true
    }
  }
}

// 隐藏消息
function hideMessage() {
  showMessage.value = false
}

// 播放音效
function playSound() {
  const audio = new Audio('https://assets.mixkit.co/sfx/preview/mixkit-positive-interface-beep-221.mp3')
  audio.volume = 0.3
  audio.play()
}

// 切换背景音乐
function toggleMusic() {
  if (!audioElement.value) {
    audioElement.value = new Audio(props.musicUrl)
    audioElement.value.loop = true
    audioElement.value.volume = 0.5
  }

  if (isMusicPlaying.value) {
    audioElement.value.pause()
  } else {
    audioElement.value.play().catch(e => {
      console.error('自动播放被阻止:', e)
      // 显示点击播放提示
      alert('请点击页面任意位置后，再点击音乐按钮播放音乐')
    })
  }
  isMusicPlaying.value = !isMusicPlaying.value
}

// 花瓣样式
const petalStyle = (n) => {
  const size = Math.random() * 15 + 5
  const left = Math.random() * 100
  const animationDuration = Math.random() * 10 + 10
  const animationDelay = Math.random() * 5
  const rotation = Math.random() * 360

  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${left}%`,
    animationDuration: `${animationDuration}s`,
    animationDelay: `${animationDelay}s`,
    transform: `rotate(${rotation}deg)`,
    background: `hsl(${Math.random() * 30 + 330}, 100%, 70%)`,
    borderRadius: `${Math.random() * 50 + 50}% 0 ${Math.random() * 50 + 50}% 0`
  }
}

// 下落的心形样式
const heartStyle = (n) => {
  const left = Math.random() * 100
  const animationDuration = Math.random() * 3 + 2
  const animationDelay = Math.random() * 2
  const size = Math.random() * 20 + 10

  return {
    left: `${left}%`,
    animationDuration: `${animationDuration}s`,
    animationDelay: `${animationDelay}s`,
    fontSize: `${size}px`,
    color: `hsl(${Math.random() * 30 + 330}, 100%, 70%)`,
    textShadow: `0 0 ${Math.random() * 10}px rgba(255, 51, 102, 0.5)`
  }
}

onMounted(() => {
  initThreeJS()
  // 自动播放音乐（如果允许）
  if (audioElement.value) {
    const playPromise = audioElement.value.play()
    if (playPromise !== undefined) {
      playPromise.then(_ => {
        isMusicPlaying.value = true
      }).catch(error => {
        console.log('自动播放被阻止:', error)
      })
    }
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', onWindowResize)
  if (renderer) {
    renderer.dispose()
  }
  if (audioElement.value) {
    audioElement.value.pause()
    audioElement.value = null
  }
})
</script>

<style scoped>
.love-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: radial-gradient(ellipse at bottom, #1B2735 0%, #090A0F 100%);
  touch-action: none;
}

.countdown {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  color: white;
  font-size: 1.2rem;
  background: rgba(255, 51, 102, 0.3);
  padding: 8px 16px;
  border-radius: 20px;
  z-index: 100;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 10px rgba(255, 51, 102, 0.5);
  font-family: 'Arial', sans-serif;
  font-weight: bold;
}

.message {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: white;
  background: rgba(0, 0, 0, 0.7);
  padding: 30px;
  border-radius: 15px;
  max-width: 80%;
  z-index: 100;
  cursor: pointer;
  animation: pulse 2s infinite;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 51, 102, 0.3);
  font-family: 'Arial', sans-serif;
}

.message h1 {
  font-size: 2.5rem;
  margin-bottom: 15px;
  background: linear-gradient(to right, #ff3366, #ffcc33);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 10px rgba(255, 51, 102, 0.5);
  font-weight: bold;
}

.message p {
  font-size: 1.5rem;
  opacity: 0.8;
  margin-bottom: 20px;
  color: #fff;
}

.music-btn {
  position: absolute;
  bottom: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  font-size: 1.2rem;
  cursor: pointer;
  z-index: 100;
  transition: all 0.3s;
  backdrop-filter: blur(5px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.music-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

/* 花瓣飘落效果 */
.petals {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.petal {
  position: absolute;
  background: pink;
  opacity: 0.8;
  animation: falling linear infinite;
}

@keyframes falling {
  0% {
    top: -10%;
    transform: rotate(0deg);
  }
  100% {
    top: 110%;
    transform: rotate(360deg);
  }
}

/* 下落的心形 */
.hearts-falling {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.heart-fall {
  position: absolute;
  animation: heart-fall linear infinite;
  opacity: 0.7;
  user-select: none;
}

@keyframes heart-fall {
  0% {
    top: -10%;
    transform: translateX(0) rotate(0deg);
    opacity: 0;
  }
  20% {
    opacity: 0.7;
  }
  100% {
    top: 110%;
    transform: translateX(50px) rotate(360deg);
    opacity: 0;
  }
}

/* 淡入淡出效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@keyframes pulse {
  0% {
    transform: translate(-50%, -50%) scale(1);
    box-shadow: 0 0 0 0 rgba(255, 51, 102, 0.4);
  }
  70% {
    transform: translate(-50%, -50%) scale(1.05);
    box-shadow: 0 0 0 15px rgba(255, 51, 102, 0);
  }
  100% {
    transform: translate(-50%, -50%) scale(1);
    box-shadow: 0 0 0 0 rgba(255, 51, 102, 0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message h1 {
    font-size: 1.8rem;
  }

  .message p {
    font-size: 1.2rem;
  }

  .countdown {
    font-size: 1rem;
    top: 10px;
  }

  .petal {
    animation-duration: 15s !important;
  }

  .heart-fall {
    font-size: 12px !important;
  }
}
</style>