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
            path:'/project-view',
            name: 'project-view',
            component: () => import('@/views/ProjectView.vue')
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
        // AI
        {
            path: '/cnn-hand-write-number',
            name: 'cnn-hand-write-number',
            component: () => import('@/views/artificial_intelligence/CnnMnist.vue')
        },
        {
            path: '/cifar10-classifier',
            name: 'cifar10-classifier',
            component: () => import('@/views/artificial_intelligence/Cifar10Classifier.vue')
        },
        {
            path: '/ai-chat',
            name: 'ai-chat',
            component: () => import('@/views/artificial_intelligence/AIChat.vue')
        },
        // 工具
        {
            path:'/GeoJson-to-shapefile',
            name: 'GeoJson-to-shapefile',
            component: () => import('@/views/artificial_intelligence/GeoJsonToShapefile.vue')
        },
        // 游戏
        {
            path: '/gomoku-game',
            name: 'GomokuGame',
            component: () => import('@/views/game/GomokuGame.vue')
        },
        {
            path: '/chinese-chess',
            name: 'ChineseChess',
            component: () => import('@/views/game/ChineseChessGame.vue')
        },
        {
            path: '/plants-zombies',
            name: 'PlantsVsZombies',
            component: () => import('@/views/game/PlantsVsZombies.vue')
        },
        {
            path: '/maze-game',
            name: 'MazeGame',
            component: () => import('@/views/game/MazeGame.vue')
        },
        {
            path: '/number-connection-game',
            name: 'NumberConnectionGame',
            component: () => import('@/views/game/SpaceShooter.vue')
        },
        {
            path: '/zillionaire',
            name: 'Zillionaire',
            component: () => import('@/views/game/zillionaire.vue')
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
