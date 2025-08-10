<template>
  <div class="game-container" tabindex="0"
       @keydown="handleKeyDown" @keyup="handleKeyUp"
       @touchstart="handleTouchStart" @touchmove="handleTouchMove" @touchend="handleTouchEnd">
    <!-- 开始界面 -->
    <div v-if="gameState === 'start'" class="screen start-screen">
      <h1>太空射击游戏</h1>
      <p>最高分: {{ highScore }}</p>
      <button @click="startGame">开始游戏</button>
      <div class="controls">
        <p>控制: 方向键/WASD移动，空格射击，B键使用炸弹</p>
        <p>道具: 武器升级(⚔️)、生命(❤️)、护盾(🛡️)、加分(⭐)</p>
      </div>
    </div>

    <!-- 关卡奖励界面 -->
    <div v-if="gameState === 'levelComplete'" class="screen level-complete-screen">
      <div class="level-complete-card">
        <h2>关卡 {{ previousLevel }} 完成!</h2>
        <div class="rewards">
          <h3>奖励:</h3>
          <div class="reward-item">
            <span class="reward-icon">⭐</span>
            <span class="reward-text">得分: +{{ levelReward.score }}</span>
          </div>
          <div class="reward-item">
            <span class="reward-icon">❤️</span>
            <span class="reward-text">生命: +{{ levelReward.lives }}</span>
          </div>
          <div class="reward-item">
            <span class="reward-icon">💣</span>
            <span class="reward-text">炸弹: +{{ levelReward.bombs }}</span>
          </div>
          <div v-if="levelReward.weaponUpgrade" class="reward-item">
            <span class="reward-icon">⚔️</span>
            <span class="reward-text">武器升级!</span>
          </div>
        </div>
        <p class="continue-message">
          继续游戏 {{ countdown }}...
        </p>
        <button @click="proceedToNextLevel">立即继续</button>
      </div>
    </div>

    <!-- 游戏界面 -->
    <div v-else-if="gameState === 'playing'" class="screen game-screen">
      <!-- 游戏UI -->
      <div class="game-ui">
        <div class="score">得分: {{ score }}</div>
        <div class="level">关卡: {{ level }}</div>
        <div class="lives">
          生命:
          <span v-for="i in 5" :key="i" class="life-icon" :class="{ lost: i > player.lives }">❤️</span>
        </div>
        <div class="weapon">武器: {{ weaponLevel }}级</div>
        <div class="bomb">炸弹: {{ bombCount }}个</div>
      </div>

      <!-- 游戏区域 -->
      <div class="game-area">
        <!-- 玩家飞船 -->
        <div
            class="player"
            :class="{ invincible: player.invincible }"
            :style="{
            left: player.x + 'px',
            top: player.y + 'px',
            transform: `rotate(${player.rotation}deg)`
          }"
        >
          ▲
        </div>

        <!-- 玩家子弹 -->
        <div
            v-for="bullet in playerBullets"
            :key="bullet.id"
            class="bullet player-bullet"
            :style="{
            left: bullet.x + 'px',
            top: bullet.y + 'px',
            transform: bullet.angle ? `rotate(${bullet.angle}deg)` : 'none'
          }"
        ></div>

        <!-- 敌机 -->
        <div
            v-for="enemy in enemies"
            :key="enemy.id"
            class="enemy"
            :class="enemy.type"
            :style="{
            left: enemy.x + 'px',
            top: enemy.y + 'px'
          }"
        >
          {{ enemy.type === 'boss' ? '🔴' : enemy.type === 'elite' ? '🟠' : '🔵' }}
        </div>

        <!-- 敌机子弹 -->
        <div
            v-for="bullet in enemyBullets"
            :key="bullet.id"
            class="bullet enemy-bullet"
            :style="{
            left: bullet.x + 'px',
            top: bullet.y + 'px'
          }"
        ></div>

        <!-- 道具 -->
        <div
            v-for="powerup in powerups"
            :key="powerup.id"
            class="powerup"
            :class="powerup.type"
            :style="{
            left: powerup.x + 'px',
            top: powerup.y + 'px'
          }"
        >
          {{
            powerup.type === 'weapon' ? '⚔️' : powerup.type === 'life' ? '❤️' : powerup.type === 'shield' ? '🛡️' : '⭐'
          }}
        </div>

        <!-- 爆炸效果 -->
        <div
            v-for="explosion in explosions"
            :key="explosion.id"
            class="explosion"
            :style="{
            left: explosion.x + 'px',
            top: explosion.y + 'px',
            width: explosion.size + 'px',
            height: explosion.size + 'px',
            opacity: 1 - explosion.frame / 30,
            background: explosion.color ? `radial-gradient(circle, ${explosion.color}, transparent)` : ''
          }"
        ></div>
      </div>
    </div>

    <!-- 游戏结束界面 -->
    <div v-else-if="gameState === 'gameover'" class="screen gameover-screen">
      <h2>游戏结束</h2>
      <p>最终得分: {{ score }}</p>
      <p>最高分: {{ highScore }}</p>
      <button @click="resetGame">重新开始</button>
    </div>
  </div>
</template>

<script>
import {ref, reactive, onMounted, onUnmounted, computed} from 'vue'

export default {
  name: 'SpaceShooter',
  setup() {
    // 游戏状态
    const gameState = ref('start') // start, playing, gameover, levelComplete
    const score = ref(0)
    const highScore = ref(localStorage.getItem('spaceShooterHighScore') || 0)
    const level = ref(1)
    const previousLevel = ref(0)
    const countdown = ref(3)
    const levelReward = reactive({
      score: 0,
      lives: 0,
      bombs: 0,
      weaponUpgrade: false
    })

    // 游戏区域尺寸
    const gameArea = reactive({
      width: 800,
      height: 600
    })

    // 玩家数据
    const player = reactive({
      x: 400,
      y: 500,
      width: 30,
      height: 30,
      speed: 5,
      lives: 3,
      invincible: false,
      invincibleTime: 0,
      rotation: 0
    })

    // 武器系统
    const weaponLevel = ref(1)
    const fireCooldown = ref(0)
    const bombCount = ref(3)
    const bombCooldown = ref(0)

    // 游戏对象数组
    const playerBullets = ref([])
    const enemies = ref([])
    const enemyBullets = ref([])
    const powerups = ref([])
    const explosions = ref([])

    // 输入控制
    const keys = reactive({
      up: false,
      down: false,
      left: false,
      right: false,
      space: false,
      bomb: false,
      spacePressed: false  // 追踪空格键是否已处理
    })

    // 触摸控制
    const touch = reactive({
      active: false,
      startX: 0,
      startY: 0,
      offsetX: 0,
      offsetY: 0,
      shoot: false  // 触摸射击标记
    })

    // 对象ID计数器
    const idCounter = ref(0)

    // 游戏循环相关
    let gameLoopId = null
    let enemySpawnTimer = 0
    let bossSpawned = false
    let lastTime = 0
    let lastShotTime = 0  // 用于控制射击频率
    let countdownTimer = null

    // 计算属性
    const canFire = computed(() => fireCooldown.value <= 0)
    const canUseBomb = computed(() => bombCount.value > 0 && bombCooldown.value <= 0)

    // 开始游戏
    const startGame = () => {
      gameState.value = 'playing'
      resetGameObjects()
      lastTime = performance.now()
      lastShotTime = 0
      gameLoopId = requestAnimationFrame(gameLoop)
    }

    // 重置游戏对象
    const resetGameObjects = () => {
      // 重置玩家
      player.x = gameArea.width / 2 - player.width / 2
      player.y = gameArea.height - 100
      player.lives = 3
      player.invincible = false
      player.invincibleTime = 0
      player.rotation = 0

      // 重置武器
      weaponLevel.value = 1
      bombCount.value = 3
      fireCooldown.value = 0
      bombCooldown.value = 0

      // 清空游戏对象
      playerBullets.value = []
      enemies.value = []
      enemyBullets.value = []
      powerups.value = []
      explosions.value = []

      // 重置游戏状态
      score.value = 0
      level.value = 1
      enemySpawnTimer = 0
      bossSpawned = false

      // 重置输入状态
      Object.keys(keys).forEach(key => {
        keys[key] = false
      })
    }

    // 重置游戏
    const resetGame = () => {
      if (score.value > highScore.value) {
        highScore.value = score.value
        localStorage.setItem('spaceShooterHighScore', highScore.value)
      }
      if (countdownTimer) clearInterval(countdownTimer)
      cancelAnimationFrame(gameLoopId)
      gameState.value = 'start'
    }

    // 处理关卡完成
    const handleLevelComplete = () => {
      // 记录当前关卡
      previousLevel.value = level.value

      // 生成关卡奖励，随关卡提升奖励增加
      levelReward.score = 500 + (level.value - 1) * 200
      levelReward.lives = Math.floor(Math.random() * 2) + 1  // 1-2条生命
      levelReward.bombs = Math.floor(Math.random() * 2) + 1   // 1-2个炸弹
      levelReward.weaponUpgrade = Math.random() > 0.3 && weaponLevel.value < 3  // 70%概率升级武器

      // 应用奖励
      score.value += levelReward.score
      player.lives = Math.min(5, player.lives + levelReward.lives)
      bombCount.value = Math.min(5, bombCount.value + levelReward.bombs)
      if (levelReward.weaponUpgrade) {
        weaponLevel.value = Math.min(3, weaponLevel.value + 1)
      }

      // 进入关卡完成界面
      gameState.value = 'levelComplete'
      countdown.value = 3

      // 清除游戏循环
      cancelAnimationFrame(gameLoopId)

      // 启动倒计时
      if (countdownTimer) clearInterval(countdownTimer)
      countdownTimer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          proceedToNextLevel()
        }
      }, 1000)
    }

    // 进入下一关
    const proceedToNextLevel = () => {
      if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }

      // 提升关卡
      level.value++

      // 清空当前关卡的游戏对象
      playerBullets.value = []
      enemies.value = []
      enemyBullets.value = []
      powerups.value = []

      // 重置玩家位置和无敌状态
      player.x = gameArea.width / 2 - player.width / 2
      player.y = gameArea.height - 100
      player.invincible = true
      player.invincibleTime = 120  // 2秒无敌时间作为关卡切换保护

      // 重置BOSS状态
      bossSpawned = false
      enemySpawnTimer = 0

      // 回到游戏
      gameState.value = 'playing'
      lastTime = performance.now()
      gameLoopId = requestAnimationFrame(gameLoop)
    }

    // 键盘事件处理
    const handleKeyDown = (e) => {
      if (gameState.value !== 'playing') return

      switch (e.key.toLowerCase()) {  // 不区分大小写处理
        case 'arrowup':
        case 'w':
          keys.up = true
          e.preventDefault()
          break
        case 'arrowdown':
        case 's':
          keys.down = true
          e.preventDefault()
          break
        case 'arrowleft':
        case 'a':
          keys.left = true
          e.preventDefault()
          break
        case 'arrowright':
        case 'd':
          keys.right = true
          e.preventDefault()
          break
        case ' ':
          keys.space = true
          e.preventDefault()
          break
        case 'b':
          keys.bomb = true
          e.preventDefault()
          break
      }
    }

    const handleKeyUp = (e) => {
      switch (e.key.toLowerCase()) {  // 不区分大小写处理
        case 'arrowup':
        case 'w':
          keys.up = false
          break
        case 'arrowdown':
        case 's':
          keys.down = false
          break
        case 'arrowleft':
        case 'a':
          keys.left = false
          break
        case 'arrowright':
        case 'd':
          keys.right = false
          break
        case ' ':
          keys.space = false
          keys.spacePressed = false  // 重置射击状态
          break
        case 'b':
          keys.bomb = false
          break
      }
    }

    // 触摸事件处理
    const handleTouchStart = (e) => {
      if (gameState.value !== 'playing') return
      e.preventDefault()
      const touchPoint = e.touches[0]
      const rect = e.target.getBoundingClientRect()

      touch.active = true
      touch.startX = touchPoint.clientX - rect.left
      touch.startY = touchPoint.clientY - rect.top
      touch.offsetX = touch.startX - player.x
      touch.offsetY = touch.startY - player.y
      touch.shoot = false  // 重置触摸射击状态
    }

    const handleTouchMove = (e) => {
      if (gameState.value !== 'playing' || !touch.active) return
      e.preventDefault()
      const touchPoint = e.touches[0]
      const rect = e.target.getBoundingClientRect()

      // 计算新位置
      const newX = touchPoint.clientX - rect.left - touch.offsetX
      const newY = touchPoint.clientY - rect.top - touch.offsetY

      // 限制在游戏区域内
      player.x = Math.max(0, Math.min(gameArea.width - player.width, newX))
      player.y = Math.max(0, Math.min(gameArea.height - player.height, newY))

      // 移动时不射击
      touch.shoot = false
    }

    const handleTouchEnd = (e) => {
      e.preventDefault()
      touch.active = false
      // 触摸结束时射击
      if (gameState.value === 'playing') {
        touch.shoot = true
      }
    }

    // 玩家移动
    const movePlayer = (deltaTime) => {
      const moveAmount = player.speed * (deltaTime / 16) // 基于时间的移动计算

      if (keys.up) player.y = Math.max(0, player.y - moveAmount)
      if (keys.down) player.y = Math.min(gameArea.height - player.height, player.y + moveAmount)
      if (keys.left) {
        player.x = Math.max(0, player.x - moveAmount)
        player.rotation = -15
      } else if (keys.right) {
        player.x = Math.min(gameArea.width - player.width, player.x + moveAmount)
        player.rotation = 15
      } else {
        player.rotation = 0
      }
    }

    // 玩家射击
    const playerShoot = () => {
      if (!canFire.value) return

      const bulletSpeed = 15
      // 根据武器等级设置不同冷却时间
      fireCooldown.value = weaponLevel.value === 1 ? 15 : weaponLevel.value === 2 ? 10 : 7
      lastShotTime = performance.now()

      // 根据武器等级创建不同数量的子弹
      switch (weaponLevel.value) {
        case 1:
          // 单发
          playerBullets.value.push({
            id: idCounter.value++,
            x: player.x + player.width / 2 - 2.5,
            y: player.y,
            width: 5,
            height: 10,
            speed: bulletSpeed
          })
          break
        case 2:
          // 双发
          playerBullets.value.push({
            id: idCounter.value++,
            x: player.x + player.width / 4,
            y: player.y,
            width: 5,
            height: 10,
            speed: bulletSpeed
          }, {
            id: idCounter.value++,
            x: player.x + player.width * 3 / 4 - 5,
            y: player.y,
            width: 5,
            height: 10,
            speed: bulletSpeed
          })
          break
        case 3:
          // 三发
          playerBullets.value.push({
            id: idCounter.value++,
            x: player.x + player.width / 2 - 2.5,
            y: player.y,
            width: 5,
            height: 10,
            speed: bulletSpeed
          }, {
            id: idCounter.value++,
            x: player.x,
            y: player.y + 10,
            width: 5,
            height: 10,
            speed: bulletSpeed,
            angle: -15
          }, {
            id: idCounter.value++,
            x: player.x + player.width - 5,
            y: player.y + 10,
            width: 5,
            height: 10,
            speed: bulletSpeed,
            angle: 15
          })
          break
      }
    }

    // 使用炸弹
    const useBomb = () => {
      if (!canUseBomb.value) return

      bombCount.value--
      bombCooldown.value = 180 // 3秒冷却

      // 清除所有敌机和敌机子弹
      enemies.value.forEach(enemy => {
        createExplosion(enemy.x + enemy.width / 2, enemy.y + enemy.height / 2, 50)
        score.value += enemy.type === 'boss' ? 500 : enemy.type === 'elite' ? 100 : 50
      })

      enemies.value = []
      enemyBullets.value = []

      // 炸弹效果
      explosions.value.push({
        id: idCounter.value++,
        x: gameArea.width / 2,
        y: gameArea.height / 2,
        size: 300,
        frame: 0
      })
    }

    // 创建敌机
    const spawnEnemy = () => {
      // 随着关卡提升，精英敌机概率增加
      const enemyTypeProbability = Math.random();
      let type = 'normal';
      if (enemyTypeProbability < 0.1 + (level.value * 0.05)) {
        type = 'elite';
      }

      const enemy = {
        id: idCounter.value++,
        type: type,
        x: Math.random() * (gameArea.width - (type === 'elite' ? 40 : 30)),
        y: -40,
        width: type === 'elite' ? 40 : 30,
        height: type === 'elite' ? 40 : 30,
        speed: type === 'elite' ? 2 + level.value * 0.2 : 3 + level.value * 0.2,
        health: type === 'elite' ? 3 + Math.floor(level.value / 2) : 1 + Math.floor(level.value / 3),
        shootCooldown: 0,
        movePattern: Math.floor(Math.random() * 3) // 0:直线, 1:波浪, 2:追踪
      }

      enemies.value.push(enemy)
    }

    // 创建BOSS
    const spawnBoss = () => {
      if (bossSpawned) return

      bossSpawned = true

      const boss = {
        id: idCounter.value++,
        type: 'boss',
        x: gameArea.width / 2 - 50,
        y: -100,
        width: 100,
        height: 100,
        speed: 1 + level.value * 0.1,
        health: 20 + level.value * 5,
        maxHealth: 20 + level.value * 5,
        shootCooldown: 0,
        movePattern: 0,
        phase: 1
      }

      enemies.value.push(boss)
    }

    // 敌机移动
    const moveEnemies = (deltaTime) => {
      const moveFactor = deltaTime / 16;

      enemies.value.forEach(enemy => {
        // 根据移动模式移动
        switch (enemy.movePattern) {
          case 0: // 直线
            enemy.y += enemy.speed * moveFactor;
            break;
          case 1: // 波浪
            enemy.y += enemy.speed * moveFactor;
            enemy.x += Math.sin(enemy.y / 30) * 2;
            break;
          case 2: // 追踪
            enemy.y += enemy.speed * 0.5 * moveFactor;
            if (player.x > enemy.x + enemy.width / 2) {
              enemy.x += enemy.speed * 0.3 * moveFactor;
            } else if (player.x + player.width / 2 < enemy.x) {
              enemy.x -= enemy.speed * 0.3 * moveFactor;
            }
            break;
        }

        // BOSS特殊行为
        if (enemy.type === 'boss') {
          // 移动到屏幕顶部
          if (enemy.y < 50) {
            enemy.y += enemy.speed * 0.3 * moveFactor;
          } else {
            // 左右移动
            if (enemy.x <= 0 || enemy.x >= gameArea.width - enemy.width) {
              enemy.speed *= -1;
            }
            enemy.x += enemy.speed * moveFactor;

            // 根据血量改变阶段
            const healthPercent = enemy.health / enemy.maxHealth;
            if (healthPercent < 0.5 && enemy.phase === 1) {
              enemy.phase = 2;
              enemy.speed *= 1.5;
            } else if (healthPercent < 0.25 && enemy.phase === 2) {
              enemy.phase = 3;
              enemy.speed *= 1.3;
            }
          }

          // BOSS射击
          if (enemy.shootCooldown <= 0) {
            // 根据阶段发射不同数量的子弹
            const bulletCount = enemy.phase === 1 ? 3 : enemy.phase === 2 ? 5 : 7;
            for (let i = 0; i < bulletCount; i++) {
              const angle = (i / bulletCount) * Math.PI * 2;
              enemyBullets.value.push({
                id: idCounter.value++,
                x: enemy.x + enemy.width / 2,
                y: enemy.y + enemy.height,
                width: 5,
                height: 10,
                speedX: Math.sin(angle) * 3,
                speedY: Math.cos(angle) * 3
              })
            }
            enemy.shootCooldown = enemy.phase === 1 ? 120 : enemy.phase === 2 ? 90 : 60;
          } else {
            enemy.shootCooldown--;
          }
        }
        // 普通敌机射击
        else if (enemy.type === 'elite') {
          if (enemy.shootCooldown <= 0) {
            enemyBullets.value.push({
              id: idCounter.value++,
              x: enemy.x + enemy.width / 2 - 2.5,
              y: enemy.y + enemy.height,
              width: 5,
              height: 10,
              speedX: (player.x - enemy.x) / 100,
              speedY: 3
            })
            enemy.shootCooldown = 120;
          } else {
            enemy.shootCooldown--;
          }
        }
      })

      // 移除离开屏幕的敌机
      enemies.value = enemies.value.filter(enemy => enemy.y < gameArea.height + 100);
    }

    // 移动子弹
    const moveBullets = (deltaTime) => {
      const moveFactor = deltaTime / 16;

      // 玩家子弹
      playerBullets.value.forEach(bullet => {
        bullet.y -= bullet.speed * moveFactor;
        // 带角度的子弹额外处理
        if (bullet.angle) {
          const rad = bullet.angle * Math.PI / 180;
          bullet.x += Math.sin(rad) * bullet.speed * 0.3 * moveFactor;
        }
      })

      // 敌机子弹
      enemyBullets.value.forEach(bullet => {
        if (bullet.speedX !== undefined) {
          bullet.x += bullet.speedX * moveFactor;
        }
        bullet.y += bullet.speedY * moveFactor;
      })

      // 移除离开屏幕的子弹
      playerBullets.value = playerBullets.value.filter(bullet => bullet.y > -10);
      enemyBullets.value = enemyBullets.value.filter(bullet =>
          bullet.y < gameArea.height + 10 && bullet.y > -10 &&
          bullet.x > -10 && bullet.x < gameArea.width + 10
      );
    }

    // 移动道具
    const movePowerups = (deltaTime) => {
      const moveFactor = deltaTime / 16;

      powerups.value.forEach(powerup => {
        powerup.y += powerup.speed * moveFactor;
      })

      // 移除离开屏幕的道具
      powerups.value = powerups.value.filter(powerup => powerup.y < gameArea.height + 10);
    }

    // 碰撞检测
    const checkCollisions = () => {
      // 玩家子弹与敌机碰撞
      playerBullets.value.forEach(bullet => {
        enemies.value.forEach(enemy => {
          if (
              bullet.x < enemy.x + enemy.width &&
              bullet.x + bullet.width > enemy.x &&
              bullet.y < enemy.y + enemy.height &&
              bullet.y + bullet.height > enemy.y
          ) {
            // 击中敌机
            enemy.health--;

            // 受伤效果
            createExplosion(bullet.x + bullet.width / 2, bullet.y + bullet.height / 2, 15);

            // 移除子弹
            bullet.y = -100; // 标记为移除

            if (enemy.health <= 0) {
              // 敌机被摧毁
              createExplosion(enemy.x + enemy.width / 2, enemy.y + enemy.height / 2, enemy.type === 'boss' ? 100 : 30);

              // 增加分数
              score.value += enemy.type === 'boss' ? 500 : enemy.type === 'elite' ? 100 : 50;

              // 掉落道具
              const dropRate = enemy.type === 'boss' ? 0.8 : enemy.type === 'elite' ? 0.6 : 0.5;
              if (Math.random() < dropRate) {
                const powerupTypes = ['weapon', 'weapon', 'weapon', 'life', 'shield', 'score'];
                const type = powerupTypes[Math.floor(Math.random() * powerupTypes.length)];

                powerups.value.push({
                  id: idCounter.value++,
                  type: type,
                  x: enemy.x + enemy.width / 2 - 10,
                  y: enemy.y + enemy.height / 2 - 10,
                  width: 20,
                  height: 20,
                  speed: 2
                });
              }

              // 移除敌机
              enemy.y = -100; // 标记为移除

              // 如果是BOSS被击败，进入下一关
              if (enemy.type === 'boss') {
                handleLevelComplete();
              }
            }
          }
        })
      })

      // 移除已标记的子弹和敌机
      playerBullets.value = playerBullets.value.filter(bullet => bullet.y !== -100);
      enemies.value = enemies.value.filter(enemy => enemy.y !== -100);

      // 玩家与敌机碰撞
      if (!player.invincible) {
        enemies.value.forEach(enemy => {
          if (
              player.x < enemy.x + enemy.width &&
              player.x + player.width > enemy.x &&
              player.y < enemy.y + enemy.height &&
              player.y + player.height > enemy.y
          ) {
            // 玩家被击中
            playerHit();
            createExplosion(enemy.x, enemy.y, 30);
            enemy.y = -100; // 移除敌机
          }
        })
      }

      // 玩家与敌机子弹碰撞
      if (!player.invincible) {
        enemyBullets.value.forEach(bullet => {
          if (
              player.x < bullet.x + bullet.width &&
              player.x + player.width > bullet.x &&
              player.y < bullet.y + bullet.height &&
              player.y + player.height > bullet.y
          ) {
            // 玩家被击中
            playerHit();
            createExplosion(bullet.x, bullet.y, 15);
            bullet.y = -100; // 移除子弹
          }
        })

        // 移除已标记的子弹
        enemyBullets.value = enemyBullets.value.filter(bullet => bullet.y !== -100);
      }

      // 玩家与道具碰撞
      powerups.value.forEach(powerup => {
        if (
            player.x < powerup.x + powerup.width &&
            player.x + player.width > powerup.x &&
            player.y < powerup.y + powerup.height &&
            player.y + player.height > powerup.y
        ) {
          // 收集道具
          collectPowerup(powerup.type);
          powerup.y = -100; // 标除道具
        }
      })

      // 移除已标记的道具
      powerups.value = powerups.value.filter(powerup => powerup.y !== -100);
    }

    // 玩家被击中
    const playerHit = () => {
      player.lives--;
      player.invincible = true;
      player.invincibleTime = 120; // 2秒无敌时间

      if (player.lives <= 0) {
        gameOver();
      }
    }

    // 收集道具
    const collectPowerup = (type) => {
      switch (type) {
        case 'weapon':
          if (weaponLevel.value < 3) {
            weaponLevel.value++;
            createExplosion(player.x, player.y, 30, '#00ffff');
          } else {
            score.value += 200;
          }
          break;
        case 'life':
          if (player.lives < 5) {
            player.lives++;
            createExplosion(player.x, player.y, 30, '#00ff00');
          } else {
            score.value += 500;
          }
          break;
        case 'shield':
          player.invincible = true;
          player.invincibleTime = 300; // 5秒无敌
          createExplosion(player.x, player.y, 40, '#9900ff');
          break;
        case 'score':
          score.value += 300;
          createExplosion(player.x, player.y, 30, '#ffff00');
          break;
      }
    }

    // 创建爆炸效果
    const createExplosion = (x, y, size = 40, color = '#ff5555') => {
      explosions.value.push({
        id: idCounter.value++,
        x: x,
        y: y,
        size: size,
        color: color,
        frame: 0
      });
    }

    // 更新爆炸效果
    const updateExplosions = () => {
      explosions.value.forEach(explosion => {
        explosion.frame++;
      })

      // 移除播放完毕的爆炸效果
      explosions.value = explosions.value.filter(explosion => explosion.frame < 30);
    }

    // 游戏结束
    const gameOver = () => {
      createExplosion(player.x + player.width / 2, player.y + player.height / 2, 100);
      gameState.value = 'gameover';
      if (score.value > highScore.value) {
        highScore.value = score.value;
        localStorage.setItem('spaceShooterHighScore', highScore.value);
      }
      cancelAnimationFrame(gameLoopId);
    }

    // 游戏循环
    const gameLoop = (timestamp) => {
      if (gameState.value !== 'playing') return;

      // 计算时间差
      const deltaTime = timestamp - lastTime;
      lastTime = timestamp;

      // 更新冷却时间
      if (fireCooldown.value > 0) fireCooldown.value--;
      if (bombCooldown.value > 0) bombCooldown.value--;
      if (player.invincibleTime > 0) {
        player.invincibleTime--;
        if (player.invincibleTime <= 0) {
          player.invincible = false;
        }
      }

      // 处理输入
      movePlayer(deltaTime);

      // 处理射击 - 支持持续按住空格射击
      if (keys.space && (!keys.spacePressed || timestamp - lastShotTime > 100)) {
        playerShoot();
        keys.spacePressed = true;
      }

      // 处理触摸射击
      if (touch.shoot) {
        playerShoot();
        touch.shoot = false;
      }

      // 处理炸弹使用
      if (keys.bomb) {
        useBomb();
        keys.bomb = false; // 防止连续使用
      }

      // 生成敌机
      enemySpawnTimer++;
      const spawnRate = Math.max(30, 120 - (level.value - 1) * 10); // 随关卡提升增加难度
      if (enemySpawnTimer >= spawnRate && !bossSpawned) {
        spawnEnemy();
        enemySpawnTimer = 0;
      }

      // 每隔一段时间生成BOSS
      if (score.value >= level.value * 1000 && !bossSpawned) {
        spawnBoss();
      }

      // 更新游戏对象
      moveEnemies(deltaTime);
      moveBullets(deltaTime);
      movePowerups(deltaTime);
      checkCollisions();
      updateExplosions();

      // 继续游戏循环
      gameLoopId = requestAnimationFrame(gameLoop);
    }

    // 组件挂载时
    onMounted(() => {
      // 设置游戏区域尺寸
      const container = document.querySelector('.game-container');
      if (container) {
        gameArea.width = container.clientWidth;
        gameArea.height = container.clientHeight;
        player.x = gameArea.width / 2 - player.width / 2;
        player.y = gameArea.height - 100;
      }

      // 添加窗口大小变化监听
      window.addEventListener('resize', () => {
        const container = document.querySelector('.game-container');
        if (container) {
          gameArea.width = container.clientWidth;
          gameArea.height = container.clientHeight;
          // 确保玩家在窗口内
          player.x = Math.max(0, Math.min(gameArea.width - player.width, player.x));
          player.y = Math.max(0, Math.min(gameArea.height - player.height, player.y));
        }
      });
    })

    // 组件卸载时
    onUnmounted(() => {
      if (gameLoopId) {
        cancelAnimationFrame(gameLoopId);
      }
      if (countdownTimer) {
        clearInterval(countdownTimer);
      }
      window.removeEventListener('resize', () => {
      });
    })

    return {
      gameState,
      score,
      highScore,
      level,
      previousLevel,
      countdown,
      levelReward,
      player,
      weaponLevel,
      bombCount,
      playerBullets,
      enemies,
      enemyBullets,
      powerups,
      explosions,
      canFire,
      canUseBomb,
      startGame,
      resetGame,
      proceedToNextLevel,
      handleKeyDown,
      handleKeyUp,
      handleTouchStart,
      handleTouchMove,
      handleTouchEnd,
      useBomb
    }
  }
}
</script>

<style scoped>
.game-container {
  width: 100%;
  height: 100vh;
  background: linear-gradient(to bottom, #000428, #004e92);
  overflow: hidden;
  position: relative;
  outline: none;
  margin: 0;
  padding: 0;
  touch-action: none; /* 防止触摸操作的默认行为 */
}

.screen {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
}

.start-screen h1 {
  font-size: 3rem;
  margin-bottom: 1rem;
  text-shadow: 0 0 10px #00eeff;
}

.start-screen p {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
}

.controls {
  margin-top: 2rem;
  font-size: 1rem;
  text-align: center;
}

button {
  padding: 12px 24px;
  font-size: 1.2rem;
  background: #00c3ff;
  color: white;
  border: none;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 1rem;
}

button:hover {
  background: #0088cc;
  transform: scale(1.05);
}

.game-ui {
  position: absolute;
  top: 20px;
  left: 0;
  width: 100%;
  display: flex;
  justify-content: space-around;
  z-index: 10;
  font-size: 1.2rem;
  font-weight: bold;
  text-shadow: 0 0 5px black;
  color: white;
  flex-wrap: wrap;
  gap: 10px;
}

.game-area {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.player {
  position: absolute;
  width: 30px;
  height: 30px;
  font-size: 30px;
  text-align: center;
  line-height: 30px;
  transition: transform 0.1s;
  z-index: 5;
  color: #00ff00;
}

.player.invincible {
  animation: blink 0.2s infinite;
}

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.bullet {
  position: absolute;
  width: 5px;
  height: 10px;
  border-radius: 50%;
}

.player-bullet {
  background: #00eeff;
  box-shadow: 0 0 5px #00eeff;
}

.enemy-bullet {
  background: #ff5555;
  box-shadow: 0 0 5px #ff5555;
}

.enemy {
  position: absolute;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 30px;
}

.enemy.elite {
  font-size: 40px;
}

.enemy.boss {
  font-size: 60px;
}

.powerup {
  position: absolute;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  animation: float 2s infinite;
  z-index: 4;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.explosion {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, #ff9900, #ff3300, transparent);
  animation: explode 0.5s forwards;
  z-index: 6;
}

@keyframes explode {
  0% {
    transform: scale(0.5);
  }
  100% {
    transform: scale(1);
  }
}

.life-icon {
  margin: 0 2px;
}

.life-icon.lost {
  opacity: 0.3;
}

.gameover-screen h2 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  color: #ff5555;
}

.gameover-screen p {
  font-size: 1.5rem;
  margin-bottom: 1rem;
}

/* 关卡完成界面样式 */
.level-complete-screen {
  background-color: rgba(0, 0, 30, 0.8);
  z-index: 100;
}

.level-complete-card {
  background: linear-gradient(135deg, #001f3f, #0074d9);
  border-radius: 15px;
  padding: 2rem 3rem;
  text-align: center;
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.5);
  max-width: 500px;
  width: 90%;
  animation: popIn 0.5s forwards;
}

@keyframes popIn {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  70% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.level-complete-card h2 {
  font-size: 2rem;
  margin-bottom: 1.5rem;
  color: #00ffcc;
  text-shadow: 0 0 10px rgba(0, 255, 204, 0.7);
}

.rewards {
  margin-bottom: 1.5rem;
  text-align: left;
}

.rewards h3 {
  font-size: 1.3rem;
  margin-bottom: 1rem;
  color: #ffdd00;
}

.reward-item {
  display: flex;
  align-items: center;
  margin-bottom: 0.8rem;
  font-size: 1.1rem;
  animation: slideIn 0.5s forwards;
  opacity: 0;
}

.reward-item:nth-child(2) {
  animation-delay: 0.2s;
}

.reward-item:nth-child(3) {
  animation-delay: 0.4s;
}

.reward-item:nth-child(4) {
  animation-delay: 0.6s;
}

@keyframes slideIn {
  from {
    transform: translateX(-20px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.reward-icon {
  margin-right: 10px;
  font-size: 1.3rem;
}

.continue-message {
  margin: 1.5rem 0;
  font-size: 1.1rem;
  color: #ffffff;
}

/* 适配移动设备 */
@media (max-width: 600px) {
  .game-ui {
    font-size: 1rem;
  }

  .start-screen h1 {
    font-size: 2rem;
  }

  .start-screen p {
    font-size: 1rem;
  }

  .level-complete-card {
    padding: 1.5rem;
  }

  .level-complete-card h2 {
    font-size: 1.5rem;
  }
}
</style>
