import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 引入element-plus图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'



// 引入全局样式
import '@/assets/global.css'

const app = createApp(App)

app.use(createPinia())


app.use(router)
app.use(ElementPlus)
// 引入element-plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

app.mount('#app')
