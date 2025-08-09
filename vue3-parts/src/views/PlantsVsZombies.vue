<template>
  <div class="game-container">
    <!-- 开始界面 -->
    <div v-if="gameState === 'start'" class="start-screen">
      <div class="game-title">植物大战僵尸</div>
      <button @click="startGame" class="start-button">开始游戏</button>
      <div class="game-intro">
        <p>种植植物抵御僵尸入侵</p>
        <p>点击草地草地种植选中的植物</p>
      </div>
    </div>

    <!-- 游戏界面 -->
    <div v-else-if="gameState === 'playing'" class="game-screen">
      <!-- 顶部状态栏 -->
      <div class="status-bar">
        <div class="sun-count">
          <img src="./assets/sun.png" alt="阳光" class="sun-icon">
          <span>{{ sunCount }}</span>
        </div>
        <div class="wave-info">
          第 {{ currentWave }} 波 / 共 {{ totalWaves }} 波
        </div>
        <div class="buttons">
          <button @click="collectAllSuns" class="collect-sun-button">收集所有阳光</button>
          <button @click="pauseGame" class="pause-button">暂停</button>
        </div>
      </div>

      <!-- 植物选择栏 -->
      <div class="plant-selection">
        <div
            v-for="(plant, index) in availablePlants"
            :key="plant.type"
            class="plant-card"
            :class="{
            selected: selectedPlant === plant.type,
            locked: sunCount < plant.cost
          }"
            @click="selectPlant(plant.type)"
        >
          <img
              :src="`./assets/plants/${plant.type}.png`"
              :alt="plant.name"
              class="plant-image"
          >
          <div class="plant-info">
            <div class="plant-name">{{ plant.name }}</div>
            <div class="plant-cost">{{ plant.cost }}</div>
          </div>
        </div>
      </div>

      <!-- 游戏主区域 -->
      <div class="game-field">
        <!-- 草地网格 -->
        <div class="lawn">
          <div
              v-for="(row, rowIndex) in gridRows"
              :key="rowIndex"
              class="lawn-row"
          >
            <div
                v-for="(col, colIndex) in gridCols"
                :key="colIndex"
                class="lawn-cell"
                :class="{ hasPlant: getPlantAt(rowIndex, colIndex) }"
                @click="plantOnCell(rowIndex, colIndex)"
            >
              <!-- 植物 -->
              <template v-if="getPlantAt(rowIndex, colIndex)">
                <div
                    class="plant"
                    :class="`plant-${getPlantAt(rowIndex, colIndex).type}`"
                >
                  <img
                      :src="`./assets/plants/${getPlantAt(rowIndex, colIndex).type}.png`"
                      :alt="getPlantName(getPlantAt(rowIndex, colIndex).type)"
                      class="plant-image"
                  >
                  <!-- 植物生命值 -->
                  <div class="plant-health">
                    <div
                        class="health-bar"
                        :style="{ width: `${getPlantAt(rowIndex, colIndex).health * 100}%` }"
                    ></div>
                  </div>
                </div>
              </template>

              <!-- 阳光 -->
              <div
                  v-for="(sun, sunIndex) in getSunsAt(rowIndex, colIndex)"
                  :key="sunIndex"
                  class="sun"
                  :style="{ top: `${sun.offset}px` }"
                  @click="collectSun(rowIndex, colIndex, sunIndex)"
              >
                <img src="./assets/plants/sun.png" alt="阳光" class="sun-image">
              </div>
            </div>
          </div>
        </div>

        <!-- 僵尸 -->
        <div
            v-for="(zombie, index) in zombies"
            :key="zombie.id"
            class="zombie"
            :class="`zombie-${zombie.type}`"
            :style="{
            left: `${zombie.position}%`,
            top: `${(zombie.row * (100 / gridRows)) + (100 / (gridRows * 2))}%`
          }"
        >
          <img
              :src="`./assets/zombies/${zombie.type}.png`"
              :alt="zombie.type"
              class="zombie-image"
          >
          <!-- 僵尸生命值 -->
          <div class="zombie-health">
            <div
                class="health-bar"
                :style="{ width: `${zombie.health * 100}%` }"
            ></div>
          </div>
        </div>

        <!-- 子弹/攻击 -->
        <div
            v-for="(bullet, index) in bullets"
            :key="bullet.id"
            class="bullet"
            :class="`bullet-${bullet.type}`"
            :style="{
            left: `${bullet.position}%`,
            top: `${(bullet.row * (100 / gridRows)) + (100 / (gridRows * 2))}%`
          }"
        >
          <img
              :src="`./assets/bullets/${bullet.type}.png`"
              :alt="bullet.type"
              class="bullet-image"
          >
        </div>
      </div>
    </div>

    <!-- 暂停界面 -->
    <div v-else-if="gameState === 'paused'" class="pause-screen">
      <div class="pause-title">游戏暂停</div>
      <div class="pause-buttons">
        <button @click="resumeGame" class="resume-button">继续游戏</button>
        <button @click="restartGame" class="restart-button">重新开始</button>
        <button @click="exitGame" class="exit-button">退出游戏</button>
      </div>
    </div>

    <!-- 胜利界面 -->
    <div v-else-if="gameState === 'victory'" class="victory-screen">
      <div class="victory-title">恭喜胜利！</div>
      <div class="wave-completed">你成功抵御了所有僵尸波次</div>
      <button @click="restartGame" class="restart-button">再玩一次</button>
    </div>

    <!-- 失败界面 -->
    <div v-else-if="gameState === 'defeat'" class="defeat-screen">
      <div class="defeat-title">游戏结束</div>
      <div class="defeat-message">僵尸突破了你的防线</div>
      <button @click="restartGame" class="restart-button">重新开始</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';

// 生成唯一ID工具函数
const generateId = (() => {
  let count = 0;
  return () => {
    count++;
    return `id-${Date.now()}-${count}`;
  };
})();

// 游戏状态
const gameState = ref('start'); // start, playing, paused, victory, defeat
const sunCount = ref(100);
const currentWave = ref(1);
const totalWaves = ref(5);
const selectedPlant = ref(null);
const gameLoopId = ref(null);

// 游戏网格设置
const gridRows = ref(5);
const gridCols = ref(9);

// 计算单元格宽度百分比
const cellWidthPercent = computed(() => 100 / gridCols.value);

// 植物数据 - 增强坚果墙的防御能力和阻挡特性
const availablePlants = ref([
  { type: 'peashooter', name: '豌豆射手', cost: 100, attack: 2, attackSpeed: 2000, health: 1 },
  { type: 'sunflower', name: '向日葵', cost: 50, attack: 0, attackSpeed: 0, health: 1, sunProduction: 25, sunInterval: 8000 },
  { type: 'wallnut', name: '坚果墙', cost: 50, attack: 0, attackSpeed: 0, health: 8, defense: 0.7, blocksZombies: true }, // 高生命值、70%伤害减免且能阻挡僵尸
  { type: 'cherrybomb', name: '樱桃炸弹', cost: 150, attack: 100, attackSpeed: 0, health: 1, isExplosive: true, armTime: 3000 } // 添加3秒引爆时间
]);

// 僵尸类型
const zombieTypes = ref([
  { type: 'normal', health: 8, speed: 0.02, damage: 0.01, reward: 25 },
  { type: 'conehead', health: 9, speed: 0.015, damage: 0.01, reward: 50 },
  { type: 'buckethead', health: 12, speed: 0.01, damage: 0.01, reward: 75 }
]);

// 游戏对象
const lawn = ref(
    Array(gridRows.value).fill().map(() =>
        Array(gridCols.value).fill(null)
    )
);

const zombies = ref([]);
const bullets = ref([]);
const suns = ref(
    Array(gridRows.value).fill().map(() =>
        Array(gridCols.value).fill().map(() => [])
    )
);

// 游戏计时器
let sunSpawnTimer = 0;
let waveTimer = 0;
const waveCooldown = 20000; // 30秒一波
let lastTime = 0;
let animationFrameId = null;

// 开始游戏
function startGame() {
  resetGame();
  gameState.value = 'playing';
  lastTime = performance.now();
  gameLoopId.value = requestAnimationFrame(gameLoop);
}

// 重置游戏
function resetGame() {
  sunCount.value = 10000;
  currentWave.value = 1;
  selectedPlant.value = null;

  // 清空游戏对象
  lawn.value = Array(gridRows.value).fill().map(() =>
      Array(gridCols.value).fill(null)
  );

  zombies.value = [];
  bullets.value = [];
  suns.value = Array(gridRows.value).fill().map(() =>
      Array(gridCols.value).fill().map(() => [])
  );

  // 重置计时器
  sunSpawnTimer = 0;
  waveTimer = 0;
}

// 游戏主循环 - 使用时间delta优化移动平滑度
function gameLoop(timestamp) {
  if (gameState.value !== 'playing') return;

  const deltaTime = timestamp - lastTime;
  lastTime = timestamp;

  // 限制最大deltaTime，防止浏览器标签页切换后导致的异常
  const safeDeltaTime = Math.min(deltaTime, 100);

  // 更新游戏状态
  updatePlants(safeDeltaTime);
  updateZombies(safeDeltaTime);
  updateBullets(safeDeltaTime);
  updateSuns(safeDeltaTime);
  checkCollisions();
  spawnSuns(safeDeltaTime);
  manageWaves(safeDeltaTime);

  // 检查游戏结束条件
  checkGameOver();

  // 继续循环
  gameLoopId.value = requestAnimationFrame(gameLoop);
}

// 更新植物
function updatePlants(deltaTime) {
  for (let row = 0; row < gridRows.value; row++) {
    for (let col = 0; col < gridCols.value; col++) {
      const plant = lawn.value[row][col];
      if (!plant) continue;

      // 处理樱桃炸弹倒计时 - 修复不触发问题
      if (plant.isExplosive) {
        plant.armTime -= deltaTime;
        // 确保armTime正确减少到0以下时触发爆炸
        if (plant.armTime <= 0) {
          explodePlant(row, col);
          lawn.value[row][col] = null;
          continue; // 爆炸后不再处理其他逻辑
        }
      }

      // 植物攻击冷却
      plant.attackCooldown -= deltaTime;
      if (plant.attack > 0 && !plant.isExplosive && plant.attackCooldown <= 0) {
        shootBullet(row, col, plant);
        plant.attackCooldown = plant.attackSpeed;
      }

      // 向日葵产生阳光
      if (plant.sunProduction) {
        plant.sunCooldown -= deltaTime;
        if (plant.sunCooldown <= 0) {
          spawnSun(row, col, plant.sunProduction);
          plant.sunCooldown = plant.sunInterval;
        }
      }
    }
  }
}

// 植物射击
function shootBullet(row, col, plant) {
  bullets.value.push({
    id: generateId(),
    row,
    position: (col + 1) * cellWidthPercent.value,
    type: plant.type === 'peashooter' ? 'pea' : 'generic',
    damage: plant.attack,
    speed: 0.3
  });
}

// 生成阳光
function spawnSun(row, col, amount = 25) {
  suns.value[row][col].push({
    amount,
    offset: 0,
    fallSpeed: 0.05,
    lifetime: 15000 // 15秒后消失
  });
}

// 随机生成阳光
function spawnSuns(deltaTime) {
  sunSpawnTimer += deltaTime;
  if (sunSpawnTimer >= 15000) { // 每15秒随机生成一个阳光
    sunSpawnTimer = 0;
    const row = Math.floor(Math.random() * gridRows.value);
    const col = Math.floor(Math.random() * gridCols.value);
    spawnSun(row, col);
  }
}

// 更新阳光
function updateSuns(deltaTime) {
  for (let row = 0; row < gridRows.value; row++) {
    for (let col = 0; col < gridCols.value; col++) {
      const sunList = suns.value[row][col];
      for (let i = sunList.length - 1; i >= 0; i--) {
        const sun = sunList[i];
        sun.offset += sun.fallSpeed * (deltaTime / 16); // 基于60fps的标准化移动
        sun.lifetime -= deltaTime;

        // 阳光落地后停止下落
        if (sun.offset > 50) {
          sun.fallSpeed = 0;
        }

        // 阳光过期消失
        if (sun.lifetime <= 0) {
          sunList.splice(i, 1);
        }
      }
    }
  }
}

// 收集阳光
function collectSun(row, col, index) {
  sunCount.value += suns.value[row][col][index].amount;
  suns.value[row][col].splice(index, 1);
}

// 一键收集所有阳光
function collectAllSuns() {
  let total = 0;
  for (let row = 0; row < gridRows.value; row++) {
    for (let col = 0; col < gridCols.value; col++) {
      const sunList = suns.value[row][col];
      sunList.forEach(sun => {
        total += sun.amount;
      });
      suns.value[row][col] = []; // 清空阳光
    }
  }
  sunCount.value += total;
}

// 管理僵尸波次
function manageWaves(deltaTime) {
  // 如果当前波次所有僵尸都被消灭，且不是最后一波，准备下一波
  if (zombies.value.length === 0 && currentWave.value <= totalWaves.value) {
    waveTimer += deltaTime;

    // 波次冷却时间
    if (waveTimer >= waveCooldown) {
      waveTimer = 0;
      spawnWave(currentWave.value);

      // 如果是最后一波且已完成，游戏胜利
      if (currentWave.value === totalWaves.value) {
        setTimeout(() => {
          if (zombies.value.length === 0) {
            gameState.value = 'victory';
            cancelAnimationFrame(gameLoopId.value);
          }
        }, waveCooldown);
      } else {
        currentWave.value++;
      }
    }
  }
}

// 生成僵尸波
function spawnWave(waveNumber) {
  const zombieCount = 5 + waveNumber * 10; // 每波增加2个僵尸
  const spawnInterval = 1500; // 每个僵尸生成间隔

  for (let i = 0; i < zombieCount; i++) {
    setTimeout(() => {
      // 确保游戏仍在进行中
      if (gameState.value !== 'playing') return;

      // 根据波次增加僵尸种类和强度
      let zombieType;
      const rand = Math.random();

      if (waveNumber >= 4 && rand < 0.2) {
        zombieType = zombieTypes.value[2]; // buckethead
      } else if (waveNumber >= 2 && rand < 0.3) {
        zombieType = zombieTypes.value[1]; // conehead
      } else {
        zombieType = zombieTypes.value[0]; // normal
      }

      // 随机行
      const row = Math.floor(Math.random() * gridRows.value);

      // 生成僵尸
      zombies.value.push({
        id: generateId(),
        ...zombieType,
        row,
        position: 100, // 从右侧进入
        lastAttack: 0,
        attackInterval: 1000 // 攻击间隔1秒
      });
    }, i * spawnInterval);
  }
}

// 更新僵尸 - 优化移动逻辑，实现阻挡机制
function updateZombies(deltaTime) {
  // 使用for循环而非forEach，提高性能
  for (let i = zombies.value.length - 1; i >= 0; i--) {
    const zombie = zombies.value[i];

    // 检查前方路径上是否有阻挡性植物
    const blockingPlant = getBlockingPlantInPath(zombie);
    if (blockingPlant) {
      // 有阻挡植物，尝试攻击它
      zombie.lastAttack += deltaTime;
      if (zombie.lastAttack >= zombie.attackInterval) {
        zombie.lastAttack = 0;
        attackPlant(zombie, blockingPlant.row, blockingPlant.col);
      }
    } else {
      // 检查是否有植物在正前方相邻格子，可以攻击
      const adjacentPlant = getAdjacentPlant(zombie);
      if (adjacentPlant) {
        // 攻击植物
        zombie.lastAttack += deltaTime;
        if (zombie.lastAttack >= zombie.attackInterval) {
          zombie.lastAttack = 0;
          attackPlant(zombie, adjacentPlant.row, adjacentPlant.col);
        }
      } else {
        // 没有阻挡，可以移动
        const moveDistance = zombie.speed * (deltaTime / 16); // 基于60fps的标准化移动
        zombie.position = Math.max(0, zombie.position - moveDistance);

        // 检查是否到达终点
        if (zombie.position <= 0) {
          // 僵尸突破防线，游戏结束
          gameState.value = 'defeat';
          cancelAnimationFrame(gameLoopId.value);
          return;
        }
      }
    }

    // 检查僵尸是否死亡
    if (zombie.health <= 0) {
      // 僵尸死亡，获得阳光奖励
      sunCount.value += zombie.reward;
      zombies.value.splice(i, 1);
    }
  }
}

// 检查僵尸正前方相邻格子是否有植物（用于攻击判断）
function getAdjacentPlant(zombie) {
  // 计算僵尸当前所在的格子列（从左到右0-8）
  const zombieCol = Math.floor((100 - zombie.position) / cellWidthPercent.value);

  // 正前方相邻格子（左边第一个格子）
  const targetCol = zombieCol - 1;

  // 扩大攻击范围判断，允许稍微远一点的植物也能被攻击
  if (targetCol >= 0 && targetCol < gridCols.value) {
    const plant = lawn.value[zombie.row][targetCol];
    if (plant) {
      return { row: zombie.row, col: targetCol, plant };
    }
  }

  // 检查更近的范围，修复距离过远就攻击的问题
  const nearCol = Math.floor((100 - (zombie.position - cellWidthPercent.value * 0.3)) / cellWidthPercent.value);
  if (nearCol >= 0 && nearCol !== zombieCol) {
    const plant = lawn.value[zombie.row][nearCol];
    if (plant) {
      return { row: zombie.row, col: nearCol, plant };
    }
  }

  return null;
}

// 检查僵尸前进路径上是否有阻挡性植物（如坚果墙）
function getBlockingPlantInPath(zombie) {
  // 计算僵尸当前所在的格子列和前方1.5个格子的位置
  const zombieCol = Math.floor((100 - zombie.position) / cellWidthPercent.value);
  const lookAheadCol = Math.floor((100 - (zombie.position - cellWidthPercent.value * 1.5)) / cellWidthPercent.value);

  // 检查从当前格子左侧开始的所有格子，扩大检查范围
  for (let col = Math.min(zombieCol - 1, lookAheadCol); col >= 0; col--) {
    const plant = lawn.value[zombie.row][col];
    // 如果是能阻挡僵尸的植物
    if (plant && plant.blocksZombies) {
      return { row: zombie.row, col, plant };
    }
  }

  return null;
}

// 僵尸攻击植物 - 考虑坚果墙的防御效果
function attackPlant(zombie, row, col) {
  const plant = lawn.value[row][col];
  if (!plant) return;

  // 计算实际伤害，考虑植物的防御属性（坚果墙有防御加成）
  let actualDamage = zombie.damage;
  if (plant.defense) {
    actualDamage *= (1 - plant.defense); // 应用防御减免
  }

  plant.health -= actualDamage;

  // 检查植物是否死亡
  if (plant.health <= 0) {
    // 处理樱桃炸弹等爆炸性植物
    if (plant.isExplosive) {
      explodePlant(row, col);
    }
    lawn.value[row][col] = null;
  }
}

// 爆炸植物效果（如樱桃炸弹）- 增强效果范围和视觉反馈
function explodePlant(row, col) {
  // 清除周围更大范围内的僵尸，确保效果可靠
  const explosionRange = 3; // 2格范围
  for (let i = zombies.value.length - 1; i >= 0; i--) {
    const zombie = zombies.value[i];
    const zombieCol = Math.floor((100 - zombie.position) / cellWidthPercent.value);

    // 检查是否在爆炸范围内（行和列都在范围内）
    if (
        Math.abs(zombie.row - row) <= explosionRange &&
        Math.abs(zombieCol - col) <= explosionRange
    ) {
      // 直接消灭爆炸范围内的僵尸
      sunCount.value += zombie.reward;
      zombies.value.splice(i, 1);
    }
  }

  console.log('樱桃炸弹爆炸了！');
}

// 更新子弹
function updateBullets(deltaTime) {
  for (let i = bullets.value.length - 1; i >= 0; i--) {
    const bullet = bullets.value[i];
    // 标准化子弹移动速度
    bullet.position += bullet.speed * (deltaTime / 16);

    // 子弹超出屏幕范围
    if (bullet.position > 100) {
      bullets.value.splice(i, 1);
    }
  }
}

// 检查碰撞
function checkCollisions() {
  // 子弹与僵尸碰撞
  for (let i = bullets.value.length - 1; i >= 0; i--) {
    const bullet = bullets.value[i];
    let hitZombie = false;

    for (let j = zombies.value.length - 1; j >= 0; j--) {
      const zombie = zombies.value[j];

      // 检查是否在同一行
      if (zombie.row !== bullet.row) continue;

      // 检查位置是否重叠
      if (zombie.position <= bullet.position + 2 && zombie.position >= bullet.position - 2) {
        // 僵尸受伤
        zombie.health -= bullet.damage;
        hitZombie = true;
        break;
      }
    }

    if (hitZombie) {
      // 移除子弹
      bullets.value.splice(i, 1);
    }
  }
}

// 检查游戏结束
function checkGameOver() {
  // 检查是否所有僵尸都被消灭且已完成所有波次
  if (currentWave.value > totalWaves.value && zombies.value.length === 0) {
    gameState.value = 'victory';
    cancelAnimationFrame(gameLoopId.value);
  }
}

// 选择植物
function selectPlant(plantType) {
  const plant = availablePlants.value.find(p => p.type === plantType);

  // 检查是否有足够的阳光
  if (sunCount.value >= plant.cost) {
    selectedPlant.value = plantType;
  }
}

// 在格子上种植植物
function plantOnCell(row, col) {
  // 检查是否已选择植物且该位置没有植物
  if (selectedPlant.value && !lawn.value[row][col]) {
    const plantData = availablePlants.value.find(p => p.type === selectedPlant.value);

    // 检查阳光是否足够
    if (sunCount.value >= plantData.cost) {
      // 种植植物
      lawn.value[row][col] = {
        type: selectedPlant.value,
        health: plantData.health,
        attack: plantData.attack,
        attackSpeed: plantData.attackSpeed,
        attackCooldown: 0,
        sunProduction: plantData.sunProduction || 0,
        sunInterval: plantData.sunInterval || 0,
        sunCooldown: plantData.sunProduction ? plantData.sunInterval / 2 : 0, // 首次产生阳光时间减半
        isExplosive: plantData.isExplosive || false,
        defense: plantData.defense || 0, // 防御属性
        armTime: plantData.armTime || 0, // 樱桃炸弹的引爆时间
        blocksZombies: plantData.blocksZombies || false // 阻挡僵尸属性
      };

      // 扣除阳光
      sunCount.value -= plantData.cost;

      // 取消选择
      selectedPlant.value = null;
    }
  }
}

// 获取指定位置的植物
function getPlantAt(row, col) {
  return lawn.value[row]?.[col] || null;
}

// 获取指定位置的阳光
function getSunsAt(row, col) {
  return suns.value[row]?.[col] || [];
}

// 获取植物名称
function getPlantName(plantType) {
  const plant = availablePlants.value.find(p => p.type === plantType);
  return plant ? plant.name : '';
}

// 暂停游戏
function pauseGame() {
  cancelAnimationFrame(gameLoopId.value);
  gameState.value = 'paused';
}

// 恢复游戏
function resumeGame() {
  if (gameState.value === 'paused') {
    gameState.value = 'playing';
    lastTime = performance.now();
    gameLoopId.value = requestAnimationFrame(gameLoop);
  }
}

// 重新开始
function restartGame() {
  cancelAnimationFrame(gameLoopId.value);
  startGame();
}

// 退出游戏
function exitGame() {
  cancelAnimationFrame(gameLoopId.value);
  gameState.value = 'start';
}

// 生命周期钩子
onMounted(() => {
  // 初始化游戏网格
  lawn.value = Array(gridRows.value).fill().map(() =>
      Array(gridCols.value).fill(null)
  );

  suns.value = Array(gridRows.value).fill().map(() =>
      Array(gridCols.value).fill().map(() => [])
  );
});

onUnmounted(() => {
  cancelAnimationFrame(gameLoopId.value);
});
</script>

<style scoped>
.game-container {
  width: 100vw;
  height: 100vh;
  background-color: #f0f0f0;
  overflow: hidden;
  position: relative;
  font-family: Arial, sans-serif;
}

/* 开始界面 */
.start-screen {
  width: 100%;
  height: 100%;
  background: url('./assets/backgrounds/start-screen.jpg') center/cover no-repeat;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.game-title {
  font-size: 3rem;
  color: #fff;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.8);
  margin-bottom: 2rem;
}

.start-button {
  padding: 1rem 2rem;
  font-size: 1.2rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: transform 0.2s;
}

.start-button:hover {
  transform: scale(1.05);
}

.game-intro {
  margin-top: 2rem;
  color: white;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.8);
  text-align: center;
}

/* 游戏界面 */
.game-screen {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #8FBC8F;
}

/* 状态栏 */
.status-bar {
  height: 60px;
  background-color: #2E8B57;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  z-index: 10;
}

.sun-count {
  display: flex;
  align-items: center;
  font-size: 1.2rem;
}

.sun-icon {
  width: 30px;
  height: 30px;
  margin-right: 10px;
}

.wave-info {
  font-size: 1.1rem;
}

.buttons {
  display: flex;
  gap: 10px;
}

.buttons .pause-button {
  padding: 5px 15px;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 3px;
  cursor: pointer;
}

.buttons .collect-sun-button {
  padding: 5px 15px;
  background-color: #FFD700;
  color: #333;
  border: none;
  border-radius: 3px;
  cursor: pointer;
}

/* 植物选择栏 */
.plant-selection {
  height: 100px;
  background-color: #9ACD32;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 15px;
  border-bottom: 2px solid #2E8B57;
  overflow-x: auto;
}

.plant-card {
  width: 80px;
  height: 80px;
  background-color: #fff;
  border: 2px solid #2E8B57;
  border-radius: 5px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.plant-card.selected {
  border-color: #FFD700;
  box-shadow: 0 0 10px #FFD700;
  transform: scale(1.05);
}

.plant-card.locked {
  opacity: 0.7;
  cursor: not-allowed;
  filter: grayscale(70%);
}

.plant-image {
  width: 40px;
  height: 40px;
  object-fit: contain;
}

.plant-info {
  text-align: center;
  font-size: 0.8rem;
  margin-top: 5px;
}

.plant-cost {
  color: #FFD700;
  font-weight: bold;
}

/* 游戏主区域 */
.game-field {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: url('./assets/backgrounds/lawn.jpg') center/cover no-repeat;
}

/* 草地网格 */
.lawn {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.lawn-row {
  flex: 1;
  display: flex;
}

.lawn-cell {
  flex: 1;
  border: 1px solid rgba(0, 0, 0, 0.1);
  position: relative;
  cursor: pointer;
}

.lawn-cell:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.lawn-cell.hasPlant:hover {
  background-color: transparent;
}

/* 植物样式 */
.plant {
  position: absolute;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.plant .plant-image {
  width: 60px;
  height: 60px;
  z-index: 2;
}

.plant-health {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 5px;
  background-color: rgba(255, 0, 0, 0.3);
  z-index: 3;
}

.health-bar {
  height: 100%;
  background-color: #4CAF50;
  transition: width 0.3s ease;
}

/* 阳光样式 */
.sun {
  position: absolute;
  width: 30px;
  height: 30px;
  left: 50%;
  transform: translateX(-50%);
  cursor: pointer;
  z-index: 4;
  transition: transform 0.2s;
}

.sun:hover {
  transform: translateX(-50%) scale(1.2);
}

.sun-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* 僵尸样式 */
.zombie {
  position: absolute;
  width: 80px;
  height: 100px;
  transform: translateX(-50%) translateY(-50%);
  z-index: 1;
  transition: left 0.1s linear; /* 平滑移动过渡 */
}

.zombie-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.zombie-health {
  position: absolute;
  bottom: -5px;
  left: 10%;
  width: 80%;
  height: 5px;
  background-color: rgba(255, 0, 0, 0.3);
}

/* 子弹样式 */
.bullet {
  position: absolute;
  width: 20px;
  height: 20px;
  transform: translateX(-50%) translateY(-50%);
  z-index: 2;
}

.bullet-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* 暂停界面 */
.pause-screen {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 100;
}

.pause-title {
  font-size: 2rem;
  color: white;
  margin-bottom: 2rem;
}

.pause-buttons {
  display: flex;
  gap: 1rem;
}

.pause-buttons button {
  padding: 0.8rem 1.5rem;
  font-size: 1rem;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: transform 0.2s;
}

.pause-buttons button:hover {
  transform: scale(1.05);
}

.resume-button {
  background-color: #4CAF50;
  color: white;
}

.restart-button {
  background-color: #FFC107;
  color: black;
}

.exit-button {
  background-color: #f44336;
  color: white;
}

/* 胜利界面 */
.victory-screen {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 100;
  color: white;
}

.victory-title {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  color: #FFD700;
}

.wave-completed {
  font-size: 1.2rem;
  margin-bottom: 2rem;
}

/* 失败界面 */
.defeat-screen {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 100;
  color: white;
}

.defeat-title {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  color: #f44336;
}

.defeat-message {
  font-size: 1.2rem;
  margin-bottom: 2rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .game-title {
    font-size: 2rem;
  }

  .status-bar {
    height: 50px;
    font-size: 0.9rem;
    padding: 0 10px;
  }

  .plant-selection {
    height: 80px;
  }

  .plant-card {
    width: 60px;
    height: 60px;
  }

  .plant-image {
    width: 30px;
    height: 30px;
  }

  .zombie {
    width: 60px;
    height: 80px;
  }
}
</style>