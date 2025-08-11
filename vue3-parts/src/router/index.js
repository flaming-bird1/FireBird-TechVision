import {createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        // 重定向 / 到 /home
        {
            path: '/',
            redirect: '/login'
        },
        {
            path: '/home',
            name: 'home',
            component: () => import('@/views/Home.vue')
        },
        {
            path: '/404',
            name: '404',
            component: () => import('@/components/NotFoundView.vue')
        },
        {
            path: '/love-heart',
            name: 'love-heart',
            component: () => import('@/components/LoveHeart.vue')
        },
        {
            path: '/cnn-hand-write-number',
            name: 'cnn-hand-write-number',
            component: () => import('@/views/CnnMnist.vue')
        },
        {
            path: '/cifar10-classifier',
            name: 'cifar10-classifier',
            component: () => import('@/views/Cifar10Classifier.vue')
        },
        {
            path: '/ai-chat',
            name: 'ai-chat',
            component: () => import('@/views/AIChat.vue')
        },
        // 游戏
        {
            path: '/gomoku-game',
            name: 'GomokuGame',
            component: () => import('@/views/GomokuGame.vue')
        },
        {
            path: '/chinese-chess',
            name: 'ChineseChess',
            component: () => import('@/views/ChineseChessGame.vue')
        },
        {
            path: '/plants-zombies',
            name: 'PlantsVsZombies',
            component: () => import('@/views/PlantsVsZombies.vue')
        },
        {
            path: '/maze-game',
            name: 'MazeGame',
            component: () => import('@/views/MazeGame.vue')
        },
        {
            path: '/number-connection-game',
            name: 'NumberConnectionGame',
            component: () => import('@/views/SpaceShooter.vue')
        },
        {
            path: '/zillionaire',
            name: 'Zillionaire',
            component: () => import('@/views/Zillionaire.vue')
        },
        // 笔记
        {
            path: '/note/:id',
            name: 'NoteDetail',
            component: () => import('@/views/NoteDetail.vue')
        },
        {
            path: '/login',
            name: 'Login',
            component: () => import('@/views/Login.vue')
        },
        {
            path: '/register',
            name: 'Register',
            component: () => import('@/views/Register.vue')
        },
    ],
})

export default router
