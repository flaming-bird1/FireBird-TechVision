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
      path:'/love-heart',
      name:'love-heart',
      component: () => import('@/components/LoveHeart.vue')
    },
    {
      path: '/cnn-hand-write-number',
      name: 'cnn-hand-write-number',
      component: () => import('@/views/CnnMnist.vue')
    },
    {
      path:'/cifar10-classifier',
      name:'cifar10-classifier',
      component: () => import('@/views/Cifar10Classifier.vue')
    },
    {
      path: '/gomoku-game',
      name: 'GomokuGame',
      component: () => import('@/views/GomokuGame.vue')
    },
    {
      path: '/note/:id',
      name: 'NoteDetail',
      component: () => import('@/views/NoteDetail.vue')
    },
    {
      path:'/login',
      name:'Login',
      component: () => import('@/views/Login.vue')
    },
      {
        path:'/register',
        name:'Register',
        component: () => import('@/views/Register.vue')
      },
  ],
})

export default router
