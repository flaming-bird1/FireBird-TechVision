<template>
  <div class="app-container">
    <!-- 主内容区 -->
    <main class="main-content">
      <div class="hero-section">
        <div class="logo-title">
          <img
              src="../assets/images/logo.png"
              alt="Logo"
              class="logo-image"
          >
          <div>
            <h1>五子棋对战</h1>
            <p class="subtitle">智能 AI 对战 - 挑战你的策略思维</p>
          </div>
        </div>
      </div>

      <div class="game-container">
        <el-card class="game-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>五子棋游戏</h3>
              <el-tag type="primary" effect="plain">黑棋先行</el-tag>
            </div>
          </template>

          <div class="game-content">
            <div class="game-board-container">
              <div id="game-board" ref="gameBoard" class="game-board">
                <div
                    v-for="(row, rowIndex) in boardSize"
                    :key="rowIndex"
                    class="board-row"
                >
                  <div
                      v-for="(col, colIndex) in boardSize"
                      :key="colIndex"
                      class="cell"
                      :class="{
                        'black': board[rowIndex][colIndex] === 'black',
                        'white': board[rowIndex][colIndex] === 'white',
                        'last-move': lastMove && lastMove.row === rowIndex && lastMove.col === colIndex
                      }"
                      @click="onCellClick(rowIndex, colIndex)"
                  ></div>
                </div>
              </div>
            </div>

            <div class="game-controls">
              <div class="game-status">
                <el-alert
                    :title="statusText"
                    :type="statusType"
                    :closable="false"
                    show-icon
                />
              </div>
              <el-button
                  type="primary"
                  size="large"
                  @click="initBoard"
                  class="restart-btn"
              >
                <el-icon class="mr-1"><Refresh /></el-icon>
                重新开始
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </main>

    <!-- 页脚组件 -->
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Footer from '@/components/Footer.vue'

const boardSize = 15
const board = ref(Array(boardSize).fill().map(() => Array(boardSize).fill(null)))
const currentPlayer = ref('black')
const gameOver = ref(false)
const statusText = ref('黑棋回合')
const statusType = ref('info')
const gameBoard = ref(null)
const lastMove = ref(null) // 新增：记录最后一步棋的位置

const AI_DEPTH = 4
const WIN_SCORE = 10000000
const patternScores = {
  'five': WIN_SCORE,
  'double_open4': WIN_SCORE / 2,
  'open4': 800000,
  'half4': 150000,
  'double_open3': 120000,
  'open3': 60000,
  'half3': 5000,
  'open2': 800
}

// 初始化棋盘
const initBoard = () => {
  board.value = Array(boardSize).fill().map(() => Array(boardSize).fill(null))
  currentPlayer.value = 'black'
  gameOver.value = false
  statusText.value = '黑棋回合'
  statusType.value = 'info'
  lastMove.value = null // 重置最后一步棋
}

// 放置棋子
const placePiece = (row, col, player) => {
  board.value[row][col] = player
  lastMove.value = { row, col, player } // 记录最后一步棋
  playSoundOnline()
}

// 棋盘点击处理
const onCellClick = (row, col) => {
  if (gameOver.value || currentPlayer.value !== 'black') return
  if (board.value[row][col] !== null) return

  placePiece(row, col, 'black')

  if (checkWin(row, col, 'black')) {
    endGame('black')
    return
  }

  if (isBoardFull()) {
    endGame('draw')
    return
  }

  currentPlayer.value = 'white'
  statusText.value = '白棋回合'
  statusType.value = 'warning'

  setTimeout(aiMove, 100)
}

// AI移动逻辑
const aiMove = () => {
  if (gameOver.value || currentPlayer.value !== 'white') return

  const winMove = findWinningMove('white')
  if (winMove) return finalizeMove(winMove)

  const blockMove = findWinningMove('black')
  if (blockMove) return finalizeMove(blockMove)

  const bestMove = getBestMove()
  if (bestMove) finalizeMove(bestMove)
}

// 获取最佳移动
const getBestMove = () => {
  let bestScore = -Infinity
  let bestMoves = []
  const moves = getPriorityMoves('white', 2)

  for (const move of moves) {
    board.value[move.row][move.col] = 'white'
    let score = alphaBeta(AI_DEPTH, -Infinity, Infinity, false)
    board.value[move.row][move.col] = null

    if (score > bestScore) {
      bestScore = score
      bestMoves = [move]
    } else if (score === bestScore) {
      bestMoves.push(move)
    }
  }

  return bestMoves[Math.floor(Math.random() * bestMoves.length)]
}

// Alpha-Beta剪枝算法
const alphaBeta = (depth, alpha, beta, maximizingPlayer) => {
  if (depth === 0) return evaluate('white') - evaluate('black') * 1.3

  const player = maximizingPlayer ? 'white' : 'black'
  const moves = getPriorityMoves(player, 1)

  if (maximizingPlayer) {
    let maxScore = -Infinity
    for (const move of moves) {
      board.value[move.row][move.col] = 'white'
      const score = alphaBeta(depth - 1, alpha, beta, false)
      board.value[move.row][move.col] = null
      maxScore = Math.max(maxScore, score)
      alpha = Math.max(alpha, score)
      if (beta <= alpha) break
    }
    return maxScore
  } else {
    let minScore = Infinity
    for (const move of moves) {
      board.value[move.row][move.col] = 'black'
      const score = alphaBeta(depth - 1, alpha, beta, true)
      board.value[move.row][move.col] = null
      minScore = Math.min(minScore, score)
      beta = Math.min(beta, score)
      if (beta <= alpha) break
    }
    return minScore
  }
}

// 评估函数
const evaluate = (player) => {
  let score = 0
  const directions = [[1, 0], [0, 1], [1, 1], [1, -1]]

  for (let i = 0; i < boardSize; i++) {
    for (let j = 0; j < boardSize; j++) {
      if (board.value[i][j] !== player) continue

      for (const [dx, dy] of directions) {
        const pattern = scanPattern(i, j, dx, dy, player)
        score += evaluatePattern(pattern)

        if (pattern.open3 >= 2) {
          score += patternScores.double_open3
        }
      }
    }
  }
  return score
}

// 扫描模式
const scanPattern = (x, y, dx, dy, player) => {
  let pattern = {
    length: 1,
    openStart: false,
    openEnd: false,
    open3: 0
  }

  // 正向扫描
  let i = 1
  while (x + dx * i >= 0 && x + dx * i < boardSize &&
  y + dy * i >= 0 && y + dy * i < boardSize) {
    if (board.value[x + dx * i][y + dy * i] === player) {
      pattern.length++
    } else {
      pattern.openEnd = (board.value[x + dx * i][y + dy * i] === null)
      break
    }
    i++
  }

  // 反向扫描
  i = 1
  while (x - dx * i >= 0 && x - dx * i < boardSize &&
  y - dy * i >= 0 && y - dy * i < boardSize) {
    if (board.value[x - dx * i][y - dy * i] === player) {
      pattern.length++
    } else {
      pattern.openStart = (board.value[x - dx * i][y - dy * i] === null)
      break
    }
    i++
  }

  return pattern
}

// 评估模式
const evaluatePattern = (pattern) => {
  if (pattern.length >= 5) return patternScores.five

  const openEnds = (pattern.openStart ? 1 : 0) + (pattern.openEnd ? 1 : 0)

  switch (pattern.length) {
    case 4:
      return openEnds === 2 ? patternScores.open4 :
          openEnds === 1 ? patternScores.half4 : 0
    case 3:
      if (openEnds === 2) return patternScores.open3
      if (openEnds === 1) return patternScores.half3
      return 0
    case 2:
      return openEnds === 2 ? patternScores.open2 : 0
    default:
      return 0
  }
}

// 获取优先移动
const getPriorityMoves = (player, distance = 2) => {
  const moves = new Map()

  for (let i = 0; i < boardSize; i++) {
    for (let j = 0; j < boardSize; j++) {
      if (board.value[i][j] !== null || !hasNeighbor(i, j, distance)) continue

      let score = 0

      // 进攻评分
      board.value[i][j] = player
      score += evaluatePattern(scanPattern(i, j, 1, 0, player))
      score += evaluatePattern(scanPattern(i, j, 0, 1, player))
      score += evaluatePattern(scanPattern(i, j, 1, 1, player))
      score += evaluatePattern(scanPattern(i, j, 1, -1, player))
      board.value[i][j] = null

      // 防御评分
      const opponent = player === 'white' ? 'black' : 'white'
      board.value[i][j] = opponent
      score += 0.8 * evaluatePattern(scanPattern(i, j, 1, 0, opponent))
      score += 0.8 * evaluatePattern(scanPattern(i, j, 0, 1, opponent))
      score += 0.8 * evaluatePattern(scanPattern(i, j, 1, 1, opponent))
      score += 0.8 * evaluatePattern(scanPattern(i, j, 1, -1, opponent))
      board.value[i][j] = null

      if (score > 0) {
        moves.set(`${i},${j}`, score)
      }
    }
  }

  return Array.from(moves)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 10)
      .map(([key]) => ({
        row: parseInt(key.split(',')[0]),
        col: parseInt(key.split(',')[1])
      }))
}

// 查找获胜移动
const findWinningMove = (player) => {
  for (let i = 0; i < boardSize; i++) {
    for (let j = 0; j < boardSize; j++) {
      if (board.value[i][j] !== null) continue
      board.value[i][j] = player
      if (checkWin(i, j, player)) {
        board.value[i][j] = null
        return { row: i, col: j }
      }
      board.value[i][j] = null
    }
  }
  return null
}

// 完成移动
const finalizeMove = (move) => {
  placePiece(move.row, move.col, 'white')

  if (checkWin(move.row, move.col, 'white')) {
    endGame('white')
    return
  }

  if (isBoardFull()) endGame('draw')
  currentPlayer.value = 'black'
  statusText.value = '黑棋回合'
  statusType.value = 'info'
}

// 检查胜利
const checkWin = (row, col, player) => {
  const directions = [[1, 0], [0, 1], [1, 1], [1, -1]]
  for (const [dx, dy] of directions) {
    let count = 1
    for (let i = 1; i < 5; i++) {
      const x = row + dx * i
      const y = col + dy * i
      if (x < 0 || x >= boardSize || y < 0 || y >= boardSize) break
      if (board.value[x][y] === player) count++; else break
    }
    for (let i = 1; i < 5; i++) {
      const x = row - dx * i
      const y = col - dy * i
      if (x < 0 || x >= boardSize || y < 0 || y >= boardSize) break
      if (board.value[x][y] === player) count++; else break
    }
    if (count >= 5) return true
  }
  return false
}

// 检查邻居
const hasNeighbor = (x, y, distance = 2) => {
  for (let i = -distance; i <= distance; i++) {
    for (let j = -distance; j <= distance; j++) {
      const nx = x + i
      const ny = y + j
      if (nx >= 0 && nx < boardSize && ny >= 0 && ny < boardSize) {
        if (board.value[nx][ny] !== null) return true
      }
    }
  }
  return false
}

// 检查棋盘是否已满
const isBoardFull = () => {
  return board.value.flat().every(cell => cell !== null)
}

// 结束游戏
const endGame = (winner) => {
  gameOver.value = true
  if (winner === 'draw') {
    statusText.value = '平局！'
    statusType.value = 'info'
    ElMessage('平局！')
  } else {
    statusText.value = `${winner === 'black' ? '黑棋' : '白棋'} 胜利！`
    statusType.value = winner === 'black' ? 'success' : 'error'
    ElMessage(winner === 'black' ? '恭喜你，你赢啦！' : '很遗憾，你输了！')
  }
  playWinSoundOnline()
}

// 音效
const playSoundOnline = () => {
  const audio = new Audio('https://assets.mixkit.co/active_storage/sfx/2927/2927-preview.mp3')
  audio.volume = 0.3
  audio.play().catch(() => { })
}

const playWinSoundOnline = () => {
  const audio = new Audio('https://assets.mixkit.co/active_storage/sfx/1023/1023-preview.mp3')
  audio.volume = 1
  audio.play().catch(() => { })
}

// 初始化
onMounted(() => {
  initBoard()
})
</script>

<style scoped>
/* 全局样式 */
:root {
  --primary-color: #409EFF;
  --bg-color: #ffffff;
  --text-color: #303133;
  --border-color: #e4e7ed;
  --footer-height: 80px;
}

/* 布局容器 */
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f8fafc;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* 主内容区 */
.main-content {
  flex: 1;
  padding: 40px 24px;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
}

.hero-section {
  text-align: center;
  margin-bottom: 48px;
}

.logo-title {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.logo-image {
  width: 80px;
  height: 80px;
}

h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 16px;
  background: linear-gradient(90deg, #409EFF, #67C23A);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  font-size: 1.2rem;
  color: #7f8c8d;
  max-width: 600px;
  margin: 0 auto;
}

.game-container {
  display: flex;
  justify-content: center;
}

.game-card {
  border-radius: 12px;
  border: none;
  transition: transform 0.3s, box-shadow 0.3s;
  max-width: 800px;
  width: 100%;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1) !important;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  h3 {
    margin: 0;
    font-size: 1.25rem;
    color: var(--text-color);
  }
}

.game-content {
  padding: 16px;
}

.game-board-container {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.game-board {
  display: grid;
  grid-template-columns: repeat(15, 1fr);
  gap: 1px;
  background-color: #d2b48c;
  border: 2px solid #8b4513;
  aspect-ratio: 1/1;
  width: 100%;
  max-width: 600px;
}

.board-row {
  display: contents;
}

.cell {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  background-color: #f0e68c;
  cursor: pointer;
}

.cell::after {
  content: '';
  position: absolute;
  top: 10%;
  left: 10%;
  width: 80%;
  height: 80%;
  border-radius: 50%;
  background-color: transparent;
  transition: all 0.2s;
}

.cell.black::after {
  background-color: #333;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.3);
}

.cell.white::after {
  background-color: #fff;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.3);
}

/* 新增：最后一步棋的高亮样式 */
.cell.last-move::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 30%;
  height: 30%;
  border-radius: 50%;
  background-color: rgba(255, 255, 0, 0.7);
  transform: translate(-50%, -50%);
  z-index: 1;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.7;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.3);
    opacity: 0.4;
  }
  100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.7;
  }
}

.game-controls {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.game-status {
  width: 100%;
  max-width: 400px;
}

.restart-btn {
  width: 160px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-section h1 {
    font-size: 2rem;
  }

  .logo-image {
    width: 60px;
    height: 60px;
  }

  .game-board {
    max-width: 100%;
  }
}

.mr-1 {
  margin-right: 4px;
}
</style>