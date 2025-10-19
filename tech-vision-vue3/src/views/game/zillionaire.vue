<template>
  <div class="monopoly-container">
    <!-- 游戏标题和玩家信息 -->
    <div class="game-header">
      <h1 class="game-title">大富翁</h1>
      <div class="players-info">
        <div class="player-card" :class="{ 'active-player': currentPlayerIndex === 0 }">
          <el-avatar :size="48" :src="playerAvatar" />
          <div class="player-details">
            <span class="player-name">玩家</span>
            <span class="player-money">¥{{ formatMoney(players[0].money) }}</span>
          </div>
        </div>
        <div class="player-card" :class="{ 'active-player': currentPlayerIndex === 1 }">
          <el-avatar :size="48" :src="aiAvatar" />
          <div class="player-details">
            <span class="player-name">AI</span>
            <span class="player-money">¥{{ formatMoney(players[1].money) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 游戏主区域 -->
    <div class="game-main">
      <!-- 游戏棋盘 -->
      <div class="game-board">
        <div class="board-grid">
          <div
              v-for="(cell, index) in board"
              :key="index"
              class="board-cell"
              :class="getCellClasses(cell, index)"
          >
            <div class="cell-content">
              <div class="cell-name">{{ cell.name }}</div>
              <div v-if="cell.price" class="cell-price">¥{{ formatMoney(cell.price) }}</div>
              <div v-if="cell.owner !== null" class="cell-owner">
                <el-avatar :size="20" :src="cell.owner === 0 ? playerAvatar : aiAvatar" />
              </div>
              <div class="players-on-cell">
                <el-avatar
                    v-for="(player, pIndex) in players"
                    v-show="player.position === index"
                    :key="pIndex"
                    :size="24"
                    :src="pIndex === 0 ? playerAvatar : aiAvatar"
                    class="player-token"
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 游戏控制区 -->
      <div class="game-controls">
        <el-button
            type="primary"
            :disabled="!canRollDice || gameOver"
            @click="handleRollDice"
            class="dice-button"
        >
          {{ gameOver ? '游戏结束' : currentPlayerIndex === 0 ? '掷骰子' : 'AI思考中...' }}
        </el-button>

        <div class="dice-display" v-if="diceValue > 0">
          <div class="dice-value">{{ diceValue }}</div>
        </div>

        <div class="action-buttons" v-if="showActionButtons">
          <el-button
              v-if="canBuyCurrentProperty"
              type="success"
              @click="buyProperty"
          >
            购买房产 (¥{{ formatMoney(currentCell.price) }})
          </el-button>
          <el-button
              v-if="canPayRent"
              type="warning"
              @click="payRent"
          >
            支付租金 (¥{{ formatMoney(currentRent) }})
          </el-button>
          <el-button
              v-if="canEndTurn"
              type="info"
              @click="endTurn"
          >
            结束回合
          </el-button>
        </div>
      </div>
    </div>

    <!-- 游戏日志 -->
    <div class="game-log">
      <el-card shadow="hover" class="log-container">
        <template #header>
          <div class="log-header">
            <span>游戏日志</span>
            <el-button type="text" @click="clearLog">清空</el-button>
          </div>
        </template>
        <div class="log-content">
          <div v-for="(log, index) in gameLog" :key="index" class="log-entry">
            <span :class="getLogClass(log)">{{ log.message }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 事件对话框 -->
    <el-dialog
        v-model="showEventDialog"
        :title="eventTitle"
        width="85%"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
    >
      <p>{{ eventMessage }}</p>
      <template #footer>
        <el-button type="primary" @click="confirmEvent">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 游戏状态
const currentPlayerIndex = ref(0) // 0=玩家, 1=AI
const diceValue = ref(0)
const canRollDice = ref(true)
const gameOver = ref(false)
const showEventDialog = ref(false)
const eventTitle = ref('')
const eventMessage = ref('')
const pendingEventAction = ref(null)
const gameLog = ref([])

// 玩家数据
const players = ref([
  { position: 0, money: 15000, properties: [], inJail: false, jailTurns: 0 },
  { position: 0, money: 15000, properties: [], inJail: false, jailTurns: 0 }
])

// 头像
const playerAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')
const aiAvatar = ref('https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')

// 游戏板配置
const board = ref([
  { type: 'start', name: '起点', action: 'pass' },
  { type: 'property', name: '北京路', price: 1000, rent: 200, owner: null },
  { type: 'chance', name: '机会', action: 'chance' },
  { type: 'property', name: '上海街', price: 1200, rent: 240, owner: null },
  { type: 'tax', name: '所得税', amount: 500, action: 'tax' },
  { type: 'property', name: '广州大道', price: 1500, rent: 300, owner: null },
  { type: 'jail', name: '监狱', action: 'jail' },
  { type: 'property', name: '深圳湾', price: 1800, rent: 360, owner: null },
  { type: 'chance', name: '机会', action: 'chance' },
  { type: 'property', name: '杭州西湖', price: 2000, rent: 400, owner: null },
  { type: 'free', name: '免费停车', action: 'free' },
  { type: 'property', name: '成都春熙', price: 2200, rent: 440, owner: null },
  { type: 'event', name: '随机事件', action: 'event' },
  { type: 'property', name: '重庆洪崖', price: 2500, rent: 500, owner: null },
  { type: 'property', name: '西安城墙', price: 2800, rent: 560, owner: null },
  { type: 'go-to-jail', name: '进监狱', action: 'go-to-jail' },
  { type: 'property', name: '苏州园林', price: 3000, rent: 600, owner: null },
  { type: 'chance', name: '机会', action: 'chance' },
  { type: 'property', name: '南京路', price: 3500, rent: 700, owner: null },
  { type: 'property', name: '武汉江滩', price: 4000, rent: 800, owner: null }
])

// 计算属性
const currentPlayer = computed(() => players.value[currentPlayerIndex.value])
const currentCell = computed(() => board.value[currentPlayer.value.position])
const currentRent = computed(() => {
  if (currentCell.value.owner === null || currentCell.value.owner === currentPlayerIndex.value) return 0
  return currentCell.value.rent
})

const canBuyCurrentProperty = computed(() => {
  return (
      currentPlayerIndex.value === 0 &&
      !canRollDice.value &&
      currentCell.value.type === 'property' &&
      currentCell.value.owner === null &&
      currentPlayer.value.money >= currentCell.value.price
  )
})

const canPayRent = computed(() => {
  return (
      !canRollDice.value &&
      currentCell.value.owner !== null &&
      currentCell.value.owner !== currentPlayerIndex.value &&
      !currentPlayer.value.inJail
  )
})

const canEndTurn = computed(() => {
  return (
      !canRollDice.value &&
      !canBuyCurrentProperty.value &&
      !canPayRent.value &&
      !showEventDialog.value &&
      !currentPlayer.value.inJail
  )
})

const showActionButtons = computed(() => {
  return canBuyCurrentProperty.value || canPayRent.value || canEndTurn.value
})

// 游戏方法
const formatMoney = (amount) => {
  return amount.toLocaleString('zh-CN')
}

const addLog = (message, type = '') => {
  gameLog.value.push({ message, type, timestamp: new Date() })
  // 保持日志不超过100条
  if (gameLog.value.length > 100) {
    gameLog.value.shift()
  }
}

const clearLog = () => {
  gameLog.value = []
}

const getLogClass = (log) => {
  return {
    'log-text': true,
    'text-success': log.type === 'success',
    'text-warning': log.type === 'warning',
    'text-danger': log.type === 'danger',
    'text-info': log.type === 'info'
  }
}

const getCellClasses = (cell, index) => {
  const classes = [`cell-${cell.type}`]

  if (cell.owner !== null) {
    classes.push(`owned-by-${cell.owner === 0 ? 'player' : 'ai'}`)
  }

  // 标记当前位置
  players.value.forEach((player, pIndex) => {
    if (player.position === index) {
      classes.push(`player-${pIndex}-here`)
    }
  })

  return classes
}

const handleRollDice = () => {
  if (!canRollDice.value || gameOver.value) return

  canRollDice.value = false
  diceValue.value = 0

  // 模拟骰子动画
  let rolls = 0
  const rollInterval = setInterval(() => {
    diceValue.value = Math.floor(Math.random() * 6) + 1
    rolls++
    if (rolls >= 8) {
      clearInterval(rollInterval)
      movePlayer(diceValue.value)
    }
  }, 100)
}

const movePlayer = (steps) => {
  const player = currentPlayer.value

  if (player.inJail) {
    player.jailTurns++
    if (player.jailTurns >= 3 || diceValue.value === 6) {
      addLog(`${currentPlayerIndex.value === 0 ? '玩家' : 'AI'}出狱了`, 'info')
      player.inJail = false
      player.jailTurns = 0
    } else {
      addLog(`${currentPlayerIndex.value === 0 ? '玩家' : 'AI'}还在监狱中 (${player.jailTurns}/3)`, 'warning')
      endTurn()
      return
    }
  }

  const oldPosition = player.position
  player.position = (player.position + steps) % board.value.length

  addLog(`${currentPlayerIndex.value === 0 ? '玩家' : 'AI'}前进了 ${steps} 步，从 ${board.value[oldPosition].name} 到 ${board.value[player.position].name}`, 'info')

  // 检查是否经过起点
  if (oldPosition + steps >= board.value.length) {
    player.money += 2000
    addLog(`${currentPlayerIndex.value === 0 ? '玩家' : 'AI'}经过起点，获得 ¥2000`, 'success')
  }

  // 处理格子事件
  handleCellAction()
}

const handleCellAction = () => {
  const cell = currentCell.value
  const player = currentPlayer.value

  switch (cell.action) {
    case 'pass':
      // 起点，无事发生
      break
    case 'tax':
      player.money -= cell.amount
      addLog(`${currentPlayerIndex.value === 0 ? '玩家' : 'AI'}支付了 ¥${cell.amount} 所得税`, 'warning')
      checkBankruptcy()
      break
    case 'jail':
      // 只是参观监狱
      addLog(`${currentPlayerIndex.value === 0 ? '玩家' : 'AI'}参观了监狱`, 'info')
      break
    case 'go-to-jail':
      player.position = board.value.findIndex(c => c.action === 'jail')
      player.inJail = true
      player.jailTurns = 0
      addLog(`${currentPlayerIndex.value === 0 ? '玩家' : 'AI'}被关进了监狱!`, 'danger')
      break
    case 'chance':
      drawChanceCard()
      return // 不继续执行后面的逻辑
    case 'event':
      triggerRandomEvent()
      return // 不继续执行后面的逻辑
    case 'free':
      // 免费停车，无事发生
      break
  }

  // 如果是房产且有人拥有
  if (cell.type === 'property' && cell.owner !== null && cell.owner !== currentPlayerIndex.value && !player.inJail) {
    const rent = cell.rent
    player.money -= rent
    players.value[cell.owner].money += rent
    addLog(`${currentPlayerIndex.value === 0 ? '玩家' : 'AI'}支付了 ¥${rent} 租金给 ${cell.owner === 0 ? '玩家' : 'AI'}`, 'warning')
    checkBankruptcy()
  }

  // AI自动行动
  if (currentPlayerIndex.value === 1 && !player.inJail) {
    setTimeout(() => {
      performAIAction()
    }, 1000)
  }
}

const buyProperty = () => {
  if (!canBuyCurrentProperty.value) return

  const cell = currentCell.value
  const player = currentPlayer.value

  player.money -= cell.price
  cell.owner = currentPlayerIndex.value
  player.properties.push(player.position)

  addLog(`玩家购买了 ${cell.name} 花费 ¥${cell.price}`, 'success')
  checkBankruptcy()

  endTurn()
}

const payRent = () => {
  if (!canPayRent.value) return

  const cell = currentCell.value
  const player = currentPlayer.value
  const owner = players.value[cell.owner]

  player.money -= currentRent.value
  owner.money += currentRent.value

  addLog(`${currentPlayerIndex.value === 0 ? '玩家' : 'AI'}支付了 ¥${currentRent.value} 租金给 ${cell.owner === 0 ? '玩家' : 'AI'}`, 'warning')
  checkBankruptcy()

  endTurn()
}

const endTurn = () => {
  if (gameOver.value) return

  currentPlayerIndex.value = currentPlayerIndex.value === 0 ? 1 : 0
  canRollDice.value = true
  diceValue.value = 0

  addLog(`轮到 ${currentPlayerIndex.value === 0 ? '玩家' : 'AI'} 的回合`, 'info')

  // 如果是AI回合，自动掷骰子
  if (currentPlayerIndex.value === 1) {
    setTimeout(() => {
      handleRollDice()
    }, 1000)
  }
}

const performAIAction = () => {
  const cell = currentCell.value
  const ai = players.value[1]

  // AI决策逻辑
  if (cell.type === 'property' && cell.owner === null) {
    // 如果有足够钱且价格合理就购买
    if (ai.money > cell.price * 1.5) {
      ai.money -= cell.price
      cell.owner = 1
      ai.properties.push(ai.position)
      addLog(`AI购买了 ${cell.name}`, 'success')
    }
  }

  // 检查是否有足够现金
  if (ai.money < 1000) {
    // 如果现金不足，尝试出售房产
    if (ai.properties.length > 0) {
      const propIndex = ai.properties[0]
      const prop = board.value[propIndex]
      ai.money += Math.floor(prop.price * 0.8) // 以80%价格出售
      prop.owner = null
      ai.properties.shift()
      addLog(`AI出售了 ${prop.name} 获得 ¥${Math.floor(prop.price * 0.8)}`, 'warning')
    }
  }

  setTimeout(() => {
    endTurn()
  }, 1000)
}

const drawChanceCard = () => {
  const chanceCards = [
    {
      title: '中奖了',
      message: '你买彩票中了 ¥1000!',
      action: () => { currentPlayer.value.money += 1000 }
    },
    {
      title: '捐款',
      message: '你向慈善机构捐款 ¥500',
      action: () => { currentPlayer.value.money -= 500 }
    },
    {
      title: '生日礼物',
      message: '所有玩家送你 ¥200作为生日礼物',
      action: () => {
        currentPlayer.value.money += 200
        players.value[currentPlayerIndex.value === 0 ? 1 : 0].money -= 200
      }
    },
    {
      title: '股票收益',
      message: '你的股票升值了，获得 ¥800',
      action: () => { currentPlayer.value.money += 800 }
    },
    {
      title: '罚款',
      message: '你因违章停车被罚款 ¥300',
      action: () => { currentPlayer.value.money -= 300 }
    },
    {
      title: '前进3格',
      message: '前进3格',
      action: () => { movePlayer(3) }
    },
    {
      title: '后退2格',
      message: '后退2格',
      action: () => {
        currentPlayer.value.position = (currentPlayer.value.position - 2 + board.value.length) % board.value.length
        handleCellAction()
      }
    }
  ]

  const card = chanceCards[Math.floor(Math.random() * chanceCards.length)]
  showEvent(card.title, card.message, card.action)
}

const triggerRandomEvent = () => {
  const events = [
    {
      title: '捡到钱包',
      message: '你捡到一个钱包，里面有 ¥300',
      action: () => { currentPlayer.value.money += 300 }
    },
    {
      title: '生病了',
      message: '你生病了，支付 ¥400医疗费',
      action: () => { currentPlayer.value.money -= 400 }
    },
    {
      title: '继承遗产',
      message: '你继承了一笔 ¥1500的遗产',
      action: () => { currentPlayer.value.money += 1500 }
    },
    {
      title: '房屋维修',
      message: '你的所有房产需要维修，每处支付 ¥100',
      action: () => {
        const cost = currentPlayer.value.properties.length * 100
        currentPlayer.value.money -= cost
        addLog(`${currentPlayerIndex.value === 0 ? '玩家' : 'AI'}支付了 ¥${cost} 房屋维修费`, 'warning')
      }
    },
    {
      title: '彩票',
      message: '你买的彩票中了 ¥600',
      action: () => { currentPlayer.value.money += 600 }
    }
  ]

  const event = events[Math.floor(Math.random() * events.length)]
  showEvent(event.title, event.message, event.action)
}

const showEvent = (title, message, action) => {
  eventTitle.value = title
  eventMessage.value = message
  pendingEventAction.value = action
  showEventDialog.value = true
}

const confirmEvent = () => {
  if (pendingEventAction.value) {
    pendingEventAction.value()
  }
  showEventDialog.value = false
  pendingEventAction.value = null

  checkBankruptcy()

  if (canEndTurn.value) {
    endTurn()
  }
}

const checkBankruptcy = () => {
  const player = currentPlayer.value
  if (player.money < 0) {
    gameOver.value = true
    addLog(`${currentPlayerIndex.value === 0 ? '玩家' : 'AI'}破产了! 游戏结束`, 'danger')

    const winner = currentPlayerIndex.value === 0 ? 1 : 0
    setTimeout(() => {
      addLog(`${winner === 0 ? '玩家' : 'AI'}赢得了游戏!`, 'success')
    }, 1000)
  }
}

// 初始化游戏
onMounted(() => {
  addLog('游戏开始! 玩家先行动', 'info')
})
</script>

<style scoped>
.monopoly-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
  font-family: 'Helvetica Neue', Arial, sans-serif;
  overflow: hidden;
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.game-title {
  margin: 0;
  color: #409eff;
  font-size: 2rem;
  font-weight: 600;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
}

.players-info {
  display: flex;
  gap: 20px;
}

.player-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.active-player {
  background: #409eff;
  color: white;
  box-shadow: 0 0 12px rgba(64, 158, 255, 0.6);
}

.player-details {
  display: flex;
  flex-direction: column;
}

.player-name {
  font-weight: 500;
  font-size: 0.9rem;
}

.player-money {
  font-weight: bold;
  font-size: 1rem;
}

.game-main {
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: 16px;
  overflow: hidden;
}

.game-board {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: auto;
  padding: 8px;
}

.board-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(80px, 1fr));
  grid-template-rows: repeat(5, minmax(80px, 1fr));
  gap: 6px;
  width: 100%;
  max-width: 800px;
  height: 100%;
  max-height: 600px;
  background: #f0f2f5;
  border-radius: 12px;
  padding: 10px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.board-cell {
  position: relative;
  border-radius: 8px;
  background: white;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  overflow: hidden;
}

.board-cell:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 12px rgba(0, 0, 0, 0.15);
}

.cell-content {
  width: 100%;
  height: 100%;
  padding: 8px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
}

.cell-name {
  font-size: 0.8rem;
  font-weight: bold;
  text-align: center;
  word-break: break-word;
  margin-bottom: 4px;
}

.cell-price {
  font-size: 0.7rem;
  color: #666;
  font-weight: 500;
}

.cell-owner {
  margin-top: 4px;
}

.players-on-cell {
  position: absolute;
  bottom: 4px;
  display: flex;
  gap: 2px;
}

.player-token {
  border: 2px solid white;
  box-shadow: 0 0 4px rgba(0, 0, 0, 0.2);
}

/* 单元格类型样式 */
.cell-start {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.cell-property {
  background: linear-gradient(135deg, #f9fcff 0%, #dee7f0 100%);
}

.cell-chance {
  background: linear-gradient(135deg, #fff6b7 0%, #f6416c 100%);
  color: white;
}

.cell-tax {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.cell-jail {
  background: linear-gradient(135deg, #5b5656 0%, #2c3e50 100%);
  color: white;
}

.cell-event {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.cell-free {
  background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%);
}

.owned-by-player {
  border: 3px solid #67c23a;
}

.owned-by-ai {
  border: 3px solid #f56c6c;
}

.game-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.dice-button {
  font-size: 1rem;
  padding: 12px 28px;
  border-radius: 50px;
  background: linear-gradient(135deg, #409eff 0%, #64b5f6 100%);
  border: none;
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.4);
  transition: all 0.3s ease;
}

.dice-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(64, 158, 255, 0.5);
}

.dice-button:disabled {
  background: #c0c4cc;
  transform: none;
  box-shadow: none;
}

.dice-display {
  display: flex;
  justify-content: center;
  align-items: center;
}

.dice-value {
  width: 48px;
  height: 48px;
  background: white;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.5rem;
  font-weight: bold;
  color: #409eff;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.game-log {
  height: 180px;
  margin-top: 12px;
}

.log-container {
  height: 100%;
  border-radius: 12px;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
}

.log-content {
  height: calc(100% - 56px);
  overflow-y: auto;
  padding: 8px 12px;
}

.log-entry {
  margin-bottom: 6px;
  padding: 4px 0;
  border-bottom: 1px solid #eee;
}

.log-text {
  font-size: 0.85rem;
}

.text-success {
  color: #67c23a;
}

.text-warning {
  color: #e6a23c;
}

.text-danger {
  color: #f56c6c;
}

.text-info {
  color: #409eff;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .game-header {
    flex-direction: column;
    gap: 12px;
  }

  .players-info {
    width: 100%;
    justify-content: space-between;
  }

  .board-grid {
    grid-template-columns: repeat(5, minmax(60px, 1fr));
    grid-template-rows: repeat(5, minmax(60px, 1fr));
  }

  .cell-name {
    font-size: 0.7rem;
  }

  .game-controls {
    flex-direction: column;
    gap: 12px;
  }

  .action-buttons {
    flex-wrap: wrap;
    justify-content: center;
  }
}

/* 特殊位置样式 - 确保棋盘布局正确 */
.cell:nth-child(1) {
  grid-column: 1;
  grid-row: 1;
}

.cell:nth-child(2) {
  grid-column: 2;
  grid-row: 1;
}

.cell:nth-child(3) {
  grid-column: 3;
  grid-row: 1;
}

.cell:nth-child(4) {
  grid-column: 4;
  grid-row: 1;
}

.cell:nth-child(5) {
  grid-column: 5;
  grid-row: 1;
}

.cell:nth-child(6) {
  grid-column: 5;
  grid-row: 2;
}

.cell:nth-child(7) {
  grid-column: 5;
  grid-row: 3;
}

.cell:nth-child(8) {
  grid-column: 5;
  grid-row: 4;
}

.cell:nth-child(9) {
  grid-column: 5;
  grid-row: 5;
}

.cell:nth-child(10) {
  grid-column: 4;
  grid-row: 5;
}

.cell:nth-child(11) {
  grid-column: 3;
  grid-row: 5;
}

.cell:nth-child(12) {
  grid-column: 2;
  grid-row: 5;
}

.cell:nth-child(13) {
  grid-column: 1;
  grid-row: 5;
}

.cell:nth-child(14) {
  grid-column: 1;
  grid-row: 4;
}

.cell:nth-child(15) {
  grid-column: 1;
  grid-row: 3;
}

.cell:nth-child(16) {
  grid-column: 1;
  grid-row: 2;
}

.cell:nth-child(17) {
  grid-column: 2;
  grid-row: 2;
}

.cell:nth-child(18) {
  grid-column: 3;
  grid-row: 2;
}

.cell:nth-child(19) {
  grid-column: 4;
  grid-row: 2;
}

.cell:nth-child(20) {
  grid-column: 4;
  grid-row: 3;
}

.cell:nth-child(21) {
  grid-column: 3;
  grid-row: 3;
}

.cell:nth-child(22) {
  grid-column: 2;
  grid-row: 3;
}

.cell:nth-child(23) {
  grid-column: 2;
  grid-row: 4;
}

.cell:nth-child(24) {
  grid-column: 3;
  grid-row: 4;
}

.cell:nth-child(25) {
  grid-column: 4;
  grid-row: 4;
}
</style>