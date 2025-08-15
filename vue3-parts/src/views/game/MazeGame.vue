<template>
  <div class="maze-game">
    <!-- 游戏开始界面 -->
    <div v-if="gameState === 'start'" class="start-screen">
      <h1 class="game-title">迷宫探险</h1>
      <div class="options">
        <div class="option-group">
          <label>初始难度:</label>
          <select v-model="initialDifficulty" class="difficulty-select">
            <option value="easy">简单</option>
            <option value="medium">中等</option>
            <option value="hard">困难</option>
            <option value="expert">专家</option>
          </select>
        </div>
        <button @click="startGame" class="start-button">开始游戏</button>
      </div>
      <div class="game-instructions">
        <p>使用方向键或WASD移动</p>
        <p>找到出口即可进入下一关！</p>
        <p>当前关卡: {{ currentLevel }}</p>
      </div>
    </div>

    <!-- 游戏界面 -->
    <div v-else-if="gameState === 'playing'" class="game-screen">
      <div class="game-info">
        <div class="level">关卡: {{ currentLevel }}</div>
        <div class="timer">时间: {{ formatTime(elapsedTime) }}</div>
        <div class="moves">步数: {{ moveCount }}</div>
        <button @click="pauseGame" class="pause-button">暂停</button>
      </div>

      <div class="maze-container" :style="{
        width: `${cellSize * mazeWidth}px`,
        height: `${cellSize * mazeHeight}px`,
        border: '2px solid #333'
      }">
        <div
            class="maze"
            :style="{
            display: 'grid',
            gridTemplateColumns: `repeat(${mazeWidth}, ${cellSize}px)`,
            gridTemplateRows: `repeat(${mazeHeight}, ${cellSize}px)`
          }"
        >
          <div
              v-for="(cell, index) in maze"
              :key="index"
              :class="getCellClass(cell, index)"
              :style="{ width: `${cellSize}px`, height: `${cellSize}px` }"
          >
            <div v-if="cell === 'player'" class="player"></div>
            <div v-if="cell === 'exit'" class="exit"></div>
          </div>
        </div>
      </div>

      <div class="mobile-controls" v-if="isMobile">
        <div class="control-row">
          <button @click="moveUp" class="control-button">↑</button>
        </div>
        <div class="control-row">
          <button @click="moveLeft" class="control-button">←</button>
          <button @click="moveDown" class="control-button">↓</button>
          <button @click="moveRight" class="control-button">→</button>
        </div>
      </div>
    </div>

    <!-- 暂停界面 -->
    <div v-else-if="gameState === 'paused'" class="pause-screen">
      <h2>游戏暂停</h2>
      <div class="pause-stats">
        <p>关卡: {{ currentLevel }}</p>
        <p>当前时间: {{ formatTime(elapsedTime) }}</p>
        <p>当前步数: {{ moveCount }}</p>
      </div>
      <div class="pause-buttons">
        <button @click="resumeGame" class="resume-button">继续</button>
        <button @click="restartGame" class="restart-button">重新开始</button>
        <button @click="quitGame" class="quit-button">退出</button>
      </div>
    </div>

    <!-- 胜利界面 -->
    <div v-else-if="gameState === 'victory'" class="victory-screen">
      <h2>恭喜你！</h2>
      <div class="victory-stats">
        <p>你成功通过了第 {{ currentLevel }} 关！</p>
        <p>用时: {{ formatTime(elapsedTime) }}</p>
        <p>步数: {{ moveCount }}</p>
        <p>总用时: {{ formatTime(totalElapsedTime) }}</p>
      </div>
      <div class="victory-buttons">
        <button @click="nextLevel" class="next-button">下一关</button>
        <button @click="restartGame" class="restart-button">重新开始</button>
        <button @click="quitGame" class="quit-button">返回主页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';

// 游戏状态
const gameState = ref('start'); // start, playing, paused, victory
const initialDifficulty = ref('medium');
const maze = ref([]);
const mazeWidth = ref(15);
const mazeHeight = ref(15);
const cellSize = ref(30);
const playerPosition = ref({ x: 1, y: 1 });
const exitPosition = ref({ x: 13, y: 13 });
const moveCount = ref(0);
const elapsedTime = ref(0);
const totalElapsedTime = ref(0);
const timerInterval = ref(null);
const isMobile = ref(false);
const currentLevel = ref(1);
const minPathLength = ref(0);
const deadEnds = ref(0);

// 检测是否为移动设备
onMounted(() => {
  checkMobileDevice();
  window.addEventListener('resize', checkMobileDevice);
  window.addEventListener('keydown', handleKeyPress);
});

onUnmounted(() => {
  window.removeEventListener('resize', checkMobileDevice);
  window.removeEventListener('keydown', handleKeyPress);
  if (timerInterval.value) {
    clearInterval(timerInterval.value);
  }
});

// 检查是否为移动设备
function checkMobileDevice() {
  isMobile.value = window.innerWidth < 768;
  // 根据屏幕大小调整单元格大小
  if (isMobile.value) {
    cellSize.value = Math.min(
        Math.floor(window.innerWidth / mazeWidth.value) - 2,
        Math.floor((window.innerHeight - 200) / mazeHeight.value) - 2
    );
    // 确保单元格不会太小
    if (cellSize.value < 15) {
      cellSize.value = 15;
    }
  } else {
    cellSize.value = 30;
  }
}

// 处理键盘输入
function handleKeyPress(event) {
  if (gameState.value !== 'playing') return;

  switch (event.key) {
    case 'ArrowUp':
    case 'w':
    case 'W':
      moveUp();
      break;
    case 'ArrowDown':
    case 's':
    case 'S':
      moveDown();
      break;
    case 'ArrowLeft':
    case 'a':
    case 'A':
      moveLeft();
      break;
    case 'ArrowRight':
    case 'd':
    case 'D':
      moveRight();
      break;
    case 'Escape':
      pauseGame();
      break;
    default:
      return;
  }

  event.preventDefault();
}

// 开始游戏
function startGame() {
  currentLevel.value = 1;
  totalElapsedTime.value = 0;
  setupLevel(currentLevel.value);
}

// 设置关卡
function setupLevel(level) {
  // 根据初始难度和当前关卡设置迷宫大小
  let baseSize;
  switch (initialDifficulty.value) {
    case 'easy':
      baseSize = 10 + Math.floor((level - 1) * 2);
      break;
    case 'medium':
      baseSize = 15 + Math.floor((level - 1) * 3);
      break;
    case 'hard':
      baseSize = 20 + Math.floor((level - 1) * 4);
      break;
    case 'expert':
      baseSize = 30 + Math.floor((level - 1) * 5);
      break;
  }

  // 确保迷宫大小为奇数，便于路径生成
  baseSize = baseSize % 2 === 0 ? baseSize + 1 : baseSize;
  // 限制最大大小，防止页面溢出
  const maxSize = isMobile.value ? 25 : 50;
  baseSize = Math.min(baseSize, maxSize);

  mazeWidth.value = baseSize;
  mazeHeight.value = baseSize;
  exitPosition.value = { x: mazeWidth.value - 2, y: mazeHeight.value - 2 };

  // 调整单元格大小
  checkMobileDevice();

  // 生成迷宫，确保有足够的错误路径
  generateMazeWithEnoughErrors();

  // 重置游戏状态
  playerPosition.value = { x: 1, y: 1 };
  moveCount.value = 0;
  elapsedTime.value = 0;

  // 开始计时
  startTimer();

  // 开始游戏
  gameState.value = 'playing';
}

// 生成有足够错误路径的迷宫
function generateMazeWithEnoughErrors() {
  let validMaze = false;
  let attempts = 0;

  // 最多尝试10次生成符合要求的迷宫
  while (!validMaze && attempts < 10) {
    generateMaze();

    // 检查迷宫是否有足够的错误路径和合理的最短路径
    if (deadEnds.value >= Math.floor((mazeWidth.value * mazeHeight.value) / 15) &&
        minPathLength.value >= mazeWidth.value * 0.7) {
      validMaze = true;
    }
    attempts++;
  }

  // 如果多次尝试仍未生成有效迷宫，使用最后一次生成的
  if (!validMaze) {
    console.warn('未能生成理想迷宫，使用最后一次尝试的结果');
  }
}

// 生成迷宫 - 改进的递归回溯算法，增加更多错误路径
function generateMaze() {
  // 初始化迷宫，全部为墙
  const newMaze = Array(mazeHeight.value).fill().map(() =>
      Array(mazeWidth.value).fill('wall')
  );

  deadEnds.value = 0;
  const pathCells = [];

  // 递归回溯算法生成路径，增加更多分支
  function carve(x, y) {
    // 定义四个方向
    const directions = [
      { dx: 0, dy: -2, nx: 0, ny: -1 }, // 上
      { dx: 0, dy: 2, nx: 0, ny: 1 },   // 下
      { dx: -2, dy: 0, nx: -1, ny: 0 }, // 左
      { dx: 2, dy: 0, nx: 1, ny: 0 }    // 右
    ];

    // 随机打乱方向
    directions.sort(() => Math.random() - 0.5);

    // 标记当前单元格为路径
    newMaze[y][x] = 'path';
    pathCells.push({x, y});

    let accessibleDirections = 0;

    // 尝试每个方向
    for (const dir of directions) {
      const newX = x + dir.dx;
      const newY = y + dir.dy;

      // 检查新位置是否在迷宫内且未访问过
      if (
          newX > 0 && newX < mazeWidth.value - 1 &&
          newY > 0 && newY < mazeHeight.value - 1 &&
          newMaze[newY][newX] === 'wall'
      ) {
        accessibleDirections++;
        // 增加一定概率创建分支（错误路径）
        if (Math.random() < 0.85) { // 85%概率打通路径，增加分支
          // 打通当前单元格与新单元格之间的墙
          newMaze[y + dir.ny][x + dir.nx] = 'path';
          pathCells.push({x: x + dir.nx, y: y + dir.ny});
          // 递归处理新单元格
          carve(newX, newY);
        }
      }
    }

    // 统计死胡同数量
    if (accessibleDirections === 0) {
      deadEnds.value++;
    }
  }

  // 从左上角开始生成 (1,1)
  carve(1, 1);

  // 确保出口是路径的一部分，如果不是则打通一条路
  if (newMaze[exitPosition.value.y][exitPosition.value.x] === 'wall') {
    createPathToExit(newMaze);
  }

  // 设置出口
  newMaze[exitPosition.value.y][exitPosition.value.x] = 'exit';

  // 计算最短路径长度
  calculateMinPathLength(newMaze);

  // 更新迷宫
  maze.value = newMaze.flat();
}

// 创建通往出口的路径（如果出口不在生成的路径上）
function createPathToExit(mazeGrid) {
  let x = exitPosition.value.x;
  let y = exitPosition.value.y;

  // 向起点方向移动，打通墙壁
  while (x > 1 || y > 1) {
    mazeGrid[y][x] = 'path';

    if (x > 1 && Math.random() > 0.3) {
      x--;
    } else if (y > 1) {
      y--;
    } else {
      x--;
    }
  }
}

// 计算起点到出口的最短路径长度（BFS算法）
function calculateMinPathLength(mazeGrid) {
  const visited = Array(mazeHeight.value).fill().map(() =>
      Array(mazeWidth.value).fill(false)
  );

  const queue = [];
  queue.push({x: 1, y: 1, distance: 0});
  visited[1][1] = true;

  const directions = [
    {dx: 0, dy: -1}, {dx: 0, dy: 1},
    {dx: -1, dy: 0}, {dx: 1, dy: 0}
  ];

  while (queue.length > 0) {
    const current = queue.shift();

    // 到达出口
    if (current.x === exitPosition.value.x && current.y === exitPosition.value.y) {
      minPathLength.value = current.distance;
      return;
    }

    // 探索四个方向
    for (const dir of directions) {
      const newX = current.x + dir.dx;
      const newY = current.y + dir.dy;

      // 检查新位置是否有效且未访问过
      if (
          newX >= 0 && newX < mazeWidth.value &&
          newY >= 0 && newY < mazeHeight.value &&
          !visited[newY][newX] &&
          mazeGrid[newY][newX] !== 'wall'
      ) {
        visited[newY][newX] = true;
        queue.push({x: newX, y: newY, distance: current.distance + 1});
      }
    }
  }

  // 如果找不到路径，设置一个默认值
  minPathLength.value = mazeWidth.value * mazeHeight.value;
}

// 获取单元格类名
function getCellClass(cell, index) {
  const y = Math.floor(index / mazeWidth.value);
  const x = index % mazeWidth.value;

  // 检查是否是玩家位置
  if (x === playerPosition.value.x && y === playerPosition.value.y) {
    return 'cell player-cell';
  }

  // 基础类名
  const baseClass = 'cell';

  // 根据单元格类型添加类名
  switch (cell) {
    case 'wall':
      return `${baseClass} wall-cell`;
    case 'path':
      return `${baseClass} path-cell`;
    case 'exit':
      return `${baseClass} exit-cell`;
    default:
      return baseClass;
  }
}

// 移动函数
function move(dx, dy) {
  const newX = playerPosition.value.x + dx;
  const newY = playerPosition.value.y + dy;

  // 检查新位置是否在迷宫内
  if (newX < 0 || newX >= mazeWidth.value || newY < 0 || newY >= mazeHeight.value) {
    return false;
  }

  // 计算新位置在一维数组中的索引
  const index = newY * mazeWidth.value + newX;

  // 检查新位置是否可以移动（不是墙）
  if (maze.value[index] === 'wall') {
    return false;
  }

  // 移动玩家
  playerPosition.value = { x: newX, y: newY };
  moveCount.value++;

  // 检查是否到达出口
  if (newX === exitPosition.value.x && newY === exitPosition.value.y) {
    endLevel();
  }

  return true;
}

// 方向移动函数
function moveUp() {
  move(0, -1);
}

function moveDown() {
  move(0, 1);
}

function moveLeft() {
  move(-1, 0);
}

function moveRight() {
  move(1, 0);
}

// 计时器控制
function startTimer() {
  if (timerInterval.value) {
    clearInterval(timerInterval.value);
  }

  const startTime = Date.now() - elapsedTime.value * 1000;
  timerInterval.value = setInterval(() => {
    elapsedTime.value = Math.floor((Date.now() - startTime) / 1000);
  }, 1000);
}

function stopTimer() {
  if (timerInterval.value) {
    clearInterval(timerInterval.value);
    timerInterval.value = null;
  }
}

// 格式化时间显示
function formatTime(seconds) {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
}

// 游戏控制函数
function pauseGame() {
  if (gameState.value === 'playing') {
    gameState.value = 'paused';
    stopTimer();
  }
}

function resumeGame() {
  if (gameState.value === 'paused') {
    gameState.value = 'playing';
    startTimer();
  }
}

function restartGame() {
  stopTimer();
  startGame();
}

function quitGame() {
  stopTimer();
  gameState.value = 'start';
}

// 结束当前关卡
function endLevel() {
  stopTimer();
  totalElapsedTime.value += elapsedTime.value;
  gameState.value = 'victory';
}

// 进入下一关
function nextLevel() {
  currentLevel.value++;
  setupLevel(currentLevel.value);
}

// 监听迷宫大小变化，重新生成迷宫
watch([mazeWidth, mazeHeight], () => {
  if (gameState.value === 'playing') {
    generateMazeWithEnoughErrors();
  }
});
</script>

<style scoped>
.maze-game {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: #f0f0f0;
  font-family: 'Arial', sans-serif;
  padding: 20px;
  box-sizing: border-box;
}

/* 开始界面 */
.start-screen {
  text-align: center;
  background-color: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
}

.game-title {
  color: #333;
  margin-bottom: 30px;
  font-size: 2.5rem;
}

.options {
  margin-bottom: 30px;
}

.option-group {
  margin-bottom: 20px;
}

.option-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #555;
}

.difficulty-select {
  padding: 8px 12px;
  border-radius: 5px;
  border: 1px solid #ddd;
  font-size: 1rem;
  min-width: 200px;
}

.start-button {
  padding: 10px 20px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.start-button:hover {
  background-color: #45a049;
}

.game-instructions {
  color: #666;
  margin-top: 20px;
}

.game-instructions p {
  margin: 5px 0;
}

/* 游戏界面 */
.game-screen {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.game-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  max-width: calc(30px * 50);
  margin-bottom: 15px;
  padding: 10px;
  background-color: white;
  border-radius: 5px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.level, .timer, .moves {
  font-size: 1.1rem;
  font-weight: bold;
  color: #333;
  min-width: 80px;
  text-align: center;
}

.pause-button {
  padding: 5px 15px;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.pause-button:hover {
  background-color: #d32f2f;
}

.maze-container {
  background-color: #222;
  border-radius: 5px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.maze {
  background-color: #222;
}

.cell {
  position: relative;
  box-sizing: border-box;
}

.wall-cell {
  background-color: #2c3e50;
  border: 1px solid #34495e;
}

.path-cell {
  background-color: #ecf0f1;
  border: 1px solid #bdc3c7;
}

.exit-cell {
  background-color: #2ecc71;
  border: 1px solid #27ae60;
}

.player-cell::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 70%;
  height: 70%;
  background-color: #3498db;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 10px rgba(52, 152, 219, 0.7);
}

.exit-cell::after {
  content: 'EXIT';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-weight: bold;
  font-size: 10px;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

/* 移动设备控制按钮 */
.mobile-controls {
  margin-top: 20px;
  display: none;
}

:deep(.mobile-controls) {
  display: grid;
  gap: 5px;
  justify-items: center;
}

.control-row {
  display: flex;
  gap: 5px;
}

.control-button {
  width: 60px;
  height: 60px;
  font-size: 1.5rem;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 暂停界面和胜利界面 */
.pause-screen, .victory-screen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  z-index: 100;
}

.pause-screen h2, .victory-screen h2 {
  font-size: 2rem;
  margin-bottom: 20px;
}

.pause-stats, .victory-stats {
  margin-bottom: 30px;
  text-align: center;
}

.pause-stats p, .victory-stats p {
  font-size: 1.2rem;
  margin: 5px 0;
}

.pause-buttons, .victory-buttons {
  display: flex;
  gap: 15px;
}

.resume-button, .restart-button, .quit-button, .next-button {
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.resume-button {
  background-color: #4CAF50;
  color: white;
}

.resume-button:hover {
  background-color: #45a049;
}

.next-button {
  background-color: #2196F3;
  color: white;
}

.next-button:hover {
  background-color: #0b7dda;
}

.restart-button {
  background-color: #FFC107;
  color: black;
}

.restart-button:hover {
  background-color: #ffb300;
}

.quit-button {
  background-color: #f44336;
  color: white;
}

.quit-button:hover {
  background-color: #d32f2f;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .game-title {
    font-size: 2rem;
  }

  .mobile-controls {
    display: block;
  }

  .game-info {
    flex-wrap: wrap;
    gap: 10px;
    justify-content: center;
  }
}
</style>