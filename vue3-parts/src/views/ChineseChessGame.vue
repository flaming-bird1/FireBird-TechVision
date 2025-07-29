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
            <h1>中国象棋</h1>
            <p class="subtitle">人机对战 - 体验传统棋艺</p>
          </div>
        </div>
      </div>

      <div class="game-container">
        <el-card class="game-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>中国象棋</h3>
              <el-tag type="primary" effect="plain">红方先行</el-tag>
            </div>
          </template>

          <div class="game-content">
            <div class="game-board-container">
              <div id="game-board" ref="gameBoard" class="game-board">
                <div class="board-grid">
                  <div
                      v-for="(row, rowIndex) in board"
                      :key="rowIndex"
                      class="board-row"
                  >
                    <div
                        v-for="(piece, colIndex) in row"
                        :key="colIndex"
                        class="cell"
                        :class="{
                        'selected': selectedPiece && selectedPiece.row === rowIndex && selectedPiece.col === colIndex,
                        'move-start': moveStart && moveStart.row === rowIndex && moveStart.col === colIndex,
                        'move-end': moveEnd && moveEnd.row === rowIndex && moveEnd.col === colIndex
                      }"
                        @click="onCellClick(rowIndex, colIndex)"
                    >
                      <transition name="piece-move">
                        <div v-if="piece" class="chess-piece" :class="piece.color">
                          {{ piece.name }}
                        </div>
                      </transition>
                    </div>
                  </div>
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
    <el-dialog
        :visible.sync="dialogVisible"
        title="游戏结束"
        width="30%"
    >
      <template #content>
        <p>{{ dialogMessage }}</p>
      </template>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="initBoard">重新开始</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Footer from '@/components/Footer.vue'

// 棋子类型
const PIECES = {
  R: { name: '车', value: 9 },
  H: { name: '马', value: 4 },
  E: { name: '相', value: 2 },
  A: { name: '士', value: 2 },
  K: { name: '将', value: 0 },
  C: { name: '炮', value: 4.5 },
  P: { name: '兵', value: 1 }
}

// 初始化棋盘
const initBoard = () => {
  board.value = Array(10).fill().map(() => Array(9).fill(null))

  // 红方布局 (下方)
  board.value[9][0] = { name: PIECES.R.name, color: 'red', type: 'R' }
  board.value[9][1] = { name: PIECES.H.name, color: 'red', type: 'H' }
  board.value[9][2] = { name: PIECES.E.name, color: 'red', type: 'E' }
  board.value[9][3] = { name: PIECES.A.name, color: 'red', type: 'A' }
  board.value[9][4] = { name: PIECES.K.name, color: 'red', type: 'K' }
  board.value[9][5] = { name: PIECES.A.name, color: 'red', type: 'A' }
  board.value[9][6] = { name: PIECES.E.name, color: 'red', type: 'E' }
  board.value[9][7] = { name: PIECES.H.name, color: 'red', type: 'H' }
  board.value[9][8] = { name: PIECES.R.name, color: 'red', type: 'R' }
  board.value[7][1] = { name: PIECES.C.name, color: 'red', type: 'C' }
  board.value[7][7] = { name: PIECES.C.name, color: 'red', type: 'C' }
  board.value[6][0] = { name: PIECES.P.name, color: 'red', type: 'P' }
  board.value[6][2] = { name: PIECES.P.name, color: 'red', type: 'P' }
  board.value[6][4] = { name: PIECES.P.name, color: 'red', type: 'P' }
  board.value[6][6] = { name: PIECES.P.name, color: 'red', type: 'P' }
  board.value[6][8] = { name: PIECES.P.name, color: 'red', type: 'P' }

  // 黑方布局 (上方)
  board.value[0][0] = { name: PIECES.R.name, color: 'black', type: 'R' }
  board.value[0][1] = { name: PIECES.H.name, color: 'black', type: 'H' }
  board.value[0][2] = { name: PIECES.E.name, color: 'black', type: 'E' }
  board.value[0][3] = { name: PIECES.A.name, color: 'black', type: 'A' }
  board.value[0][4] = { name: PIECES.K.name, color: 'black', type: 'K' }
  board.value[0][5] = { name: PIECES.A.name, color: 'black', type: 'A' }
  board.value[0][6] = { name: PIECES.E.name, color: 'black', type: 'E' }
  board.value[0][7] = { name: PIECES.H.name, color: 'black', type: 'H' }
  board.value[0][8] = { name: PIECES.R.name, color: 'black', type: 'R' }
  board.value[2][1] = { name: PIECES.C.name, color: 'black', type: 'C' }
  board.value[2][7] = { name: PIECES.C.name, color: 'black', type: 'C' }
  board.value[3][0] = { name: PIECES.P.name, color: 'black', type: 'P' }
  board.value[3][2] = { name: PIECES.P.name, color: 'black', type: 'P' }
  board.value[3][4] = { name: PIECES.P.name, color: 'black', type: 'P' }
  board.value[3][6] = { name: PIECES.P.name, color: 'black', type: 'P' }
  board.value[3][8] = { name: PIECES.P.name, color: 'black', type: 'P' }

  currentPlayer.value = 'red'
  selectedPiece.value = null
  possibleMoves.value = []
  gameOver.value = false
  statusText.value = '红方回合'
  statusType.value = 'info'
  dialogVisible.value = false
  moveStart.value = null
  moveEnd.value = null
}

const board = ref([])
const currentPlayer = ref('red')
const selectedPiece = ref(null)
const possibleMoves = ref([])
const gameOver = ref(false)
const statusText = ref('红方回合')
const statusType = ref('info')
const gameBoard = ref(null)
const dialogVisible = ref(false)
const dialogMessage = ref('')
const moveStart = ref(null)
const moveEnd = ref(null)

// 棋盘点击处理
const onCellClick = (row, col) => {
  if (gameOver.value) return

  // 如果是AI回合，不允许玩家操作
  if (currentPlayer.value === 'black') return

  const piece = board.value[row][col]

  // 如果已经选中了一个棋子，尝试移动
  if (selectedPiece.value) {
    // 如果点击的是自己的另一个棋子，切换选中
    if (piece && piece.color === currentPlayer.value) {
      selectPiece(row, col)
      return
    }

    // 检查是否是合法移动
    const isValidMove = possibleMoves.value.some(
        move => move.row === row && move.col === col
    )

    if (isValidMove) {
      // 记录移动起始和结束位置
      moveStart.value = { row: selectedPiece.value.row, col: selectedPiece.value.col }
      moveEnd.value = { row, col }

      // 执行移动
      movePiece(selectedPiece.value.row, selectedPiece.value.col, row, col)

      // 检查是否将军或胜利
      if (checkWin()) {
        endGame(currentPlayer.value)
        return
      }

      // 切换玩家
      currentPlayer.value = 'black'
      statusText.value = '黑方回合'
      statusType.value = 'warning'

      // AI思考
      setTimeout(aiMove, 500)
    }
  }
  // 如果点击的是自己的棋子，选中它
  else if (piece && piece.color === currentPlayer.value) {
    selectPiece(row, col)
  }
}

// 选中棋子
const selectPiece = (row, col) => {
  selectedPiece.value = { row, col }
  possibleMoves.value = getPossibleMoves(row, col)
}

// 获取可能的移动
const getPossibleMoves = (row, col) => {
  const piece = board.value[row][col]
  if (!piece) return []

  const moves = []
  const type = piece.type
  const color = piece.color

  // 车 (Rook)
  if (type === 'R') {
    // 横向移动
    for (let c = col - 1; c >= 0; c--) {
      if (!board.value[row][c]) {
        moves.push({ row, col: c })
      } else {
        if (board.value[row][c].color !== color) {
          moves.push({ row, col: c })
        }
        break
      }
    }
    for (let c = col + 1; c < 9; c++) {
      if (!board.value[row][c]) {
        moves.push({ row, col: c })
      } else {
        if (board.value[row][c].color !== color) {
          moves.push({ row, col: c })
        }
        break
      }
    }
    // 纵向移动
    for (let r = row - 1; r >= 0; r--) {
      if (!board.value[r][col]) {
        moves.push({ row: r, col })
      } else {
        if (board.value[r][col].color !== color) {
          moves.push({ row: r, col })
        }
        break
      }
    }
    for (let r = row + 1; r < 10; r++) {
      if (!board.value[r][col]) {
        moves.push({ row: r, col })
      } else {
        if (board.value[r][col].color !== color) {
          moves.push({ row: r, col })
        }
        break
      }
    }
  }
  // 马 (Horse)
  else if (type === 'H') {
    const horseMoves = [
      { dr: -2, dc: -1, blockRow: row - 1, blockCol: col },
      { dr: -2, dc: 1, blockRow: row - 1, blockCol: col },
      { dr: -1, dc: -2, blockRow: row, blockCol: col - 1 },
      { dr: -1, dc: 2, blockRow: row, blockCol: col + 1 },
      { dr: 1, dc: -2, blockRow: row, blockCol: col - 1 },
      { dr: 1, dc: 2, blockRow: row, blockCol: col + 1 },
      { dr: 2, dc: -1, blockRow: row + 1, blockCol: col },
      { dr: 2, dc: 1, blockRow: row + 1, blockCol: col }
    ]

    for (const move of horseMoves) {
      const newRow = row + move.dr
      const newCol = col + move.dc

      if (newRow >= 0 && newRow < 10 && newCol >= 0 && newCol < 9) {
        // 检查马脚是否被挡
        if (!board.value[move.blockRow]?.[move.blockCol]) {
          const targetPiece = board.value[newRow][newCol]
          if (!targetPiece || targetPiece.color !== color) {
            moves.push({ row: newRow, col: newCol })
          }
        }
      }
    }
  }
  // 相/象 (Elephant)
  else if (type === 'E') {
    const elephantMoves = [
      { dr: -2, dc: -2, blockRow: row - 1, blockCol: col - 1 },
      { dr: -2, dc: 2, blockRow: row - 1, blockCol: col + 1 },
      { dr: 2, dc: -2, blockRow: row + 1, blockCol: col - 1 },
      { dr: 2, dc: 2, blockRow: row + 1, blockCol: col + 1 }
    ]

    for (const move of elephantMoves) {
      const newRow = row + move.dr
      const newCol = col + move.dc

      // 检查是否过河 (象不能过河)
      if (color === 'red' && newRow < 5) continue
      if (color === 'black' && newRow > 4) continue

      if (newRow >= 0 && newRow < 10 && newCol >= 0 && newCol < 9) {
        // 检查象眼是否被挡
        if (!board.value[move.blockRow]?.[move.blockCol]) {
          const targetPiece = board.value[newRow][newCol]
          if (!targetPiece || targetPiece.color !== color) {
            moves.push({ row: newRow, col: newCol })
          }
        }
      }
    }
  }
  // 士/仕 (Advisor)
  else if (type === 'A') {
    const advisorMoves = [
      { dr: -1, dc: -1 },
      { dr: -1, dc: 1 },
      { dr: 1, dc: -1 },
      { dr: 1, dc: 1 }
    ]

    for (const move of advisorMoves) {
      const newRow = row + move.dr
      const newCol = col + move.dc

      // 检查是否在九宫格内
      if (color === 'red') {
        if (newRow < 7 || newRow > 9 || newCol < 3 || newCol > 5) continue
      } else {
        if (newRow < 0 || newRow > 2 || newCol < 3 || newCol > 5) continue
      }

      const targetPiece = board.value[newRow]?.[newCol]
      if (!targetPiece || targetPiece.color !== color) {
        moves.push({ row: newRow, col: newCol })
      }
    }
  }
  // 将/帅 (King)
  else if (type === 'K') {
    const kingMoves = [
      { dr: -1, dc: 0 },
      { dr: 1, dc: 0 },
      { dr: 0, dc: -1 },
      { dr: 0, dc: 1 }
    ]

    for (const move of kingMoves) {
      const newRow = row + move.dr
      const newCol = col + move.dc

      // 检查是否在九宫格内
      if (color === 'red') {
        if (newRow < 7 || newRow > 9 || newCol < 3 || newCol > 5) continue
      } else {
        if (newRow < 0 || newRow > 2 || newCol < 3 || newCol > 5) continue
      }

      const targetPiece = board.value[newRow]?.[newCol]
      if (!targetPiece || targetPiece.color !== color) {
        moves.push({ row: newRow, col: newCol })
      }
    }

    // 将帅对面特殊规则
    if (color === 'red') {
      for (let r = row - 1; r >= 0; r--) {
        if (board.value[r][col]) {
          if (board.value[r][col].type === 'K') {
            moves.push({ row: r, col })
          }
          break
        }
      }
    } else {
      for (let r = row + 1; r < 10; r++) {
        if (board.value[r][col]) {
          if (board.value[r][col].type === 'K') {
            moves.push({ row: r, col })
          }
          break
        }
      }
    }
  }
  // 炮 (Cannon)
  else if (type === 'C') {
    // 横向移动
    let hasJumped = false
    for (let c = col - 1; c >= 0; c--) {
      if (!hasJumped) {
        if (!board.value[row][c]) {
          moves.push({ row, col: c })
        } else {
          hasJumped = true
        }
      } else {
        if (board.value[row][c]) {
          if (board.value[row][c].color !== color) {
            moves.push({ row, col: c })
          }
          break
        }
      }
    }

    hasJumped = false
    for (let c = col + 1; c < 9; c++) {
      if (!hasJumped) {
        if (!board.value[row][c]) {
          moves.push({ row, col: c })
        } else {
          hasJumped = true
        }
      } else {
        if (board.value[row][c]) {
          if (board.value[row][c].color !== color) {
            moves.push({ row, col: c })
          }
          break
        }
      }
    }

    // 纵向移动
    hasJumped = false
    for (let r = row - 1; r >= 0; r--) {
      if (!hasJumped) {
        if (!board.value[r][col]) {
          moves.push({ row: r, col })
        } else {
          hasJumped = true
        }
      } else {
        if (board.value[r][col]) {
          if (board.value[r][col].color !== color) {
            moves.push({ row: r, col })
          }
          break
        }
      }
    }

    hasJumped = false
    for (let r = row + 1; r < 10; r++) {
      if (!hasJumped) {
        if (!board.value[r][col]) {
          moves.push({ row: r, col })
        } else {
          hasJumped = true
        }
      } else {
        if (board.value[r][col]) {
          if (board.value[r][col].color !== color) {
            moves.push({ row: r, col })
          }
          break
        }
      }
    }
  }
  // 兵/卒 (Pawn)
  else if (type === 'P') {
    if (color === 'red') {
      // 红方兵向上移动
      if (row > 0) {
        if (!board.value[row - 1][col] || board.value[row - 1][col].color !== color) {
          moves.push({ row: row - 1, col })
        }
      }

      // 过河后可以左右移动
      if (row <= 4) {
        if (col > 0 && (!board.value[row][col - 1] || board.value[row][col - 1].color !== color)) {
          moves.push({ row, col: col - 1 })
        }
        if (col < 8 && (!board.value[row][col + 1] || board.value[row][col + 1].color !== color)) {
          moves.push({ row, col: col + 1 })
        }
      }
    } else {
      // 黑方卒向下移动
      if (row < 9) {
        if (!board.value[row + 1][col] || board.value[row + 1][col].color !== color) {
          moves.push({ row: row + 1, col })
        }
      }

      // 过河后可以左右移动
      if (row >= 5) {
        if (col > 0 && (!board.value[row][col - 1] || board.value[row][col - 1].color !== color)) {
          moves.push({ row, col: col - 1 })
        }
        if (col < 8 && (!board.value[row][col + 1] || board.value[row][col + 1].color !== color)) {
          moves.push({ row, col: col + 1 })
        }
      }
    }
  }

  return moves
}

// 移动棋子
const movePiece = (fromRow, fromCol, toRow, toCol) => {
  const piece = board.value[fromRow][fromCol]
  board.value[fromRow][fromCol] = null
  board.value[toRow][toCol] = piece

  selectedPiece.value = null
  possibleMoves.value = []
}

// 评估函数
const evaluateBoard = (player) => {
  let score = 0
  for (let row = 0; row < 10; row++) {
    for (let col = 0; col < 9; col++) {
      const piece = board.value[row][col]
      if (piece) {
        const pieceValue = PIECES[piece.type].value
        if (piece.color === player) {
          score += pieceValue
        } else {
          score -= pieceValue
        }
      }
    }
  }
  return score
}

// 极小极大搜索算法
const minimax = (depth, maximizingPlayer) => {
  if (depth === 0 || checkWin()) {
    return evaluateBoard('black')
  }

  if (maximizingPlayer) {
    let maxEval = -Infinity
    const allPieces = []
    for (let row = 0; row < 10; row++) {
      for (let col = 0; col < 9; col++) {
        const piece = board.value[row][col]
        if (piece && piece.color === 'black') {
          allPieces.push({ row, col })
        }
      }
    }

    for (const piece of allPieces) {
      const moves = getPossibleMoves(piece.row, piece.col)
      for (const move of moves) {
        const tempPiece = board.value[move.row][move.col]
        movePiece(piece.row, piece.col, move.row, move.col)
        const evalScore = minimax(depth - 1, false)
        movePiece(move.row, move.col, piece.row, piece.col)
        board.value[move.row][move.col] = tempPiece
        maxEval = Math.max(maxEval, evalScore)
      }
    }
    return maxEval
  } else {
    let minEval = Infinity
    const allPieces = []
    for (let row = 0; row < 10; row++) {
      for (let col = 0; col < 9; col++) {
        const piece = board.value[row][col]
        if (piece && piece.color === 'red') {
          allPieces.push({ row, col })
        }
      }
    }

    for (const piece of allPieces) {
      const moves = getPossibleMoves(piece.row, piece.col)
      for (const move of moves) {
        const tempPiece = board.value[move.row][move.col]
        movePiece(piece.row, piece.col, move.row, move.col)
        const evalScore = minimax(depth - 1, true)
        movePiece(move.row, move.col, piece.row, piece.col)
        board.value[move.row][move.col] = tempPiece
        minEval = Math.min(minEval, evalScore)
      }
    }
    return minEval
  }
}

// AI移动
const aiMove = () => {
  if (gameOver.value || currentPlayer.value !== 'black') return

  let bestMove = null
  let bestScore = -Infinity
  const allPieces = []
  for (let row = 0; row < 10; row++) {
    for (let col = 0; col < 9; col++) {
      const piece = board.value[row][col]
      if (piece && piece.color === 'black') {
        allPieces.push({ row, col })
      }
    }
  }

  for (const piece of allPieces) {
    const moves = getPossibleMoves(piece.row, piece.col)
    for (const move of moves) {
      const tempPiece = board.value[move.row][move.col]
      movePiece(piece.row, piece.col, move.row, move.col)
      const score = minimax(2, false)
      movePiece(move.row, move.col, piece.row, piece.col)
      board.value[move.row][move.col] = tempPiece

      if (score > bestScore) {
        bestScore = score
        bestMove = { from: { row: piece.row, col: piece.col }, to: move }
      }
    }
  }

  if (bestMove) {
    // 记录移动起始和结束位置
    moveStart.value = { row: bestMove.from.row, col: bestMove.from.col }
    moveEnd.value = { row: bestMove.to.row, col: bestMove.to.col }

    movePiece(bestMove.from.row, bestMove.from.col, bestMove.to.row, bestMove.to.col)

    // 检查是否将军或胜利
    if (checkWin()) {
      endGame('black')
      return
    }

    currentPlayer.value = 'red'
    statusText.value = '红方回合'
    statusType.value = 'info'
  } else {
    // 如果没有合法移动，游戏结束
    endGame('red')
  }
}

// 检查胜利条件
const checkWin = () => {
  // 检查是否将/帅被吃
  let redKingFound = false
  let blackKingFound = false

  for (let row = 0; row < 10; row++) {
    for (let col = 0; col < 9; col++) {
      const piece = board.value[row][col]
      if (piece && piece.type === 'K') {
        if (piece.color === 'red') {
          redKingFound = true
        } else {
          blackKingFound = true
        }
      }
    }
  }

  if (!redKingFound) return 'black'
  if (!blackKingFound) return 'red'

  return null
}

// 结束游戏
const endGame = (winner) => {
  gameOver.value = true
  if (winner === 'red') {
    statusText.value = '红方胜利！'
    statusType.value = 'success'
    ElMessage('恭喜你，你赢啦！')
    dialogMessage.value = '恭喜你，你赢啦！'
  } else {
    statusText.value = '黑方胜利！'
    statusType.value = 'error'
    ElMessage('很遗憾，你输了！')
    dialogMessage.value = '很遗憾，你输了！'
  }
  dialogVisible.value = true
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
  background: linear-gradient(90deg, #F56C6C, #67C23A);
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
  position: relative;
  width: 100%;
  max-width: 600px;
  aspect-ratio: 9/10;
  background-color: #f0d9b5;
  border: 2px solid #8b4513;
}

.board-grid {
  display: grid;
  grid-template-columns: repeat(9, 1fr);
  grid-template-rows: repeat(10, 1fr);
  width: 100%;
  height: 100%;
  position: relative;
}

.board-row {
  display: contents;
}

.cell {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px solid rgba(0, 0, 0, 0.1);

  &.selected {
    background-color: rgba(64, 158, 255, 0.3);
    box-shadow: 0 0 10px rgba(64, 158, 255, 0.8);
  }

  &.move-start {
    background-color: rgba(255, 255, 0, 0.3);
    box-shadow: 0 0 10px rgba(255, 255, 0, 0.8);
  }

  &.move-end {
    background-color: rgba(255, 165, 0, 0.3);
    box-shadow: 0 0 10px rgba(255, 165, 0, 0.8);
  }

  &:before {
    content: '';
    position: absolute;
    width: 10px;
    height: 10px;
    background-color: #000;
    border-radius: 50%;
    transform: translate(-50%, -50%);
    top: 50%;
    left: 50%;
    opacity: 0.2;
  }
}

.chess-piece {
  width: 80%;
  height: 80%;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.3);
  transition: all 0.2s;

  &.red {
    background-color: #f56c6c;
    color: #fff;
  }

  &.black {
    background-color: #303133;
    color: #fff;
  }

  &:hover {
    transform: scale(1.05);
  }
}

/* 绘制棋盘线条 */
.game-board:before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    /* 横线 */
      linear-gradient(to bottom,
      transparent calc(10% - 1px),
      #000 calc(10% - 0.5px),
      #000 calc(10% + 0.5px),
      transparent calc(10% + 1px)),
      linear-gradient(to bottom,
      transparent calc(20% - 1px),
      #000 calc(20% - 0.5px),
      #000 calc(20% + 0.5px),
      transparent calc(20% + 1px)),
      linear-gradient(to bottom,
      transparent calc(30% - 1px),
      #000 calc(30% - 0.5px),
      #000 calc(30% + 0.5px),
      transparent calc(30% + 1px)),
      linear-gradient(to bottom,
      transparent calc(40% - 1px),
      #000 calc(40% - 0.5px),
      #000 calc(40% + 0.5px),
      transparent calc(40% + 1px)),
      linear-gradient(to bottom,
      transparent calc(50% - 1px),
      #000 calc(50% - 0.5px),
      #000 calc(50% + 0.5px),
      transparent calc(50% + 1px)),
      linear-gradient(to bottom,
      transparent calc(60% - 1px),
      #000 calc(60% - 0.5px),
      #000 calc(60% + 0.5px),
      transparent calc(60% + 1px)),
      linear-gradient(to bottom,
      transparent calc(70% - 1px),
      #000 calc(70% - 0.5px),
      #000 calc(70% + 0.5px),
      transparent calc(70% + 1px)),
      linear-gradient(to bottom,
      transparent calc(80% - 1px),
      #000 calc(80% - 0.5px),
      #000 calc(80% + 0.5px),
      transparent calc(80% + 1px)),
      linear-gradient(to bottom,
      transparent calc(90% - 1px),
      #000 calc(90% - 0.5px),
      #000 calc(90% + 0.5px),
      transparent calc(90% + 1px)),
        /* 竖线 */
      linear-gradient(to right,
      transparent calc(11.11% - 1px),
      #000 calc(11.11% - 0.5px),
      #000 calc(11.11% + 0.5px),
      transparent calc(11.11% + 1px)),
      linear-gradient(to right,
      transparent calc(22.22% - 1px),
      #000 calc(22.22% - 0.5px),
      #000 calc(22.22% + 0.5px),
      transparent calc(22.22% + 1px)),
      linear-gradient(to right,
      transparent calc(33.33% - 1px),
      #000 calc(33.33% - 0.5px),
      #000 calc(33.33% + 0.5px),
      transparent calc(33.33% + 1px)),
      linear-gradient(to right,
      transparent calc(44.44% - 1px),
      #000 calc(44.44% - 0.5px),
      #000 calc(44.44% + 0.5px),
      transparent calc(44.44% + 1px)),
      linear-gradient(to right,
      transparent calc(55.55% - 1px),
      #000 calc(55.55% - 0.5px),
      #000 calc(55.55% + 0.5px),
      transparent calc(55.55% + 1px)),
      linear-gradient(to right,
      transparent calc(66.66% - 1px),
      #000 calc(66.66% - 0.5px),
      #000 calc(66.66% + 0.5px),
      transparent calc(66.66% + 1px)),
      linear-gradient(to right,
      transparent calc(77.77% - 1px),
      #000 calc(77.77% - 0.5px),
      #000 calc(77.77% + 0.5px),
      transparent calc(77.77% + 1px)),
      linear-gradient(to right,
      transparent calc(88.88% - 1px),
      #000 calc(88.88% - 0.5px),
      #000 calc(88.88% + 0.5px),
      transparent calc(88.88% + 1px));
}

/* 绘制楚河汉界 */
.game-board:after {
  content: '楚河　　　　　　汉界';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  transform: translateY(-50%);
  text-align: center;
  font-size: 1.5rem;
  font-weight: bold;
  color: rgba(0, 0, 0, 0.3);
  pointer-events: none;
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

  .game-board:after {
    font-size: 1rem;
  }
}

.mr-1 {
  margin-right: 4px;
}

/* 棋子移动动画 */
.piece-move-enter-active,
.piece-move-leave-active {
  transition: all 0.3s;
}

.piece-move-enter,
.piece-move-leave-to {
  opacity: 0;
  transform: scale(0.8);
}
</style>