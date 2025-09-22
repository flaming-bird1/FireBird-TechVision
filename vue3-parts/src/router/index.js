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
            path: '/project-view',
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
            path: '/GeoJson-to-shapefile',
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
        // 地理信息系统
        {
            path: '/geo-information-system01',
            name: 'GeoInformationSystem01',
            component: () => import('@/components/geoserver/GeoServerWMSDemo.vue')
        },
        {
            path: '/geo-information-system02',
            name: 'GeoInformationSystem02',
            component: () => import('@/components/geoserver/GeoServer02.vue')
        },
        // // 地理信息系统 - 父路由
        // {
        //     path: '/geoserver',
        //     name: 'GeoServer',
        //     component: () => import('@/components/layout/GeoServerLayout.vue'), // 创建布局组件
        //     redirect: '/geoserver/demo', // 默认重定向到第一个子路由
        //     children: [
        //         {
        //             path: 'demo',
        //             name: 'GeoServerDemo',
        //             component: () => import('@/components/geoserver/GeoServerWMSDemo.vue')
        //         },
        //
        //     ]
        // },
        // 单词学习系统
        {
            path: '/word-study-system',
            name: 'WordStudySystem',
            component: () => import('@/views/WordLayout.vue'), // 布局组件
            redirect: '/word-study-system/home',
            children: [
                {
                    path: 'home',
                    name: 'WordStudySystemHome',
                    component: () => import('@/components/word/WordLearningHome.vue'),
                    meta: {
                        title: '单词学习系统'
                    }
                },
                {
                    path: 'learn',
                    name: 'WordStudySystemLearning',
                    component: () => import('@/components/word/WordLearning.vue'),
                    meta: {
                        title: '单词学习系统'
                    }
                },
            ]
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
        // 通配符路由，确保放在最后
        {
            path: '/:pathMatch(.*)*',
            redirect: '/404'
        }
    ],
})

export default router
