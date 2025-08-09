import axios from 'axios'

const service = axios.create({
    baseURL: '/api', // 根据实际情况配置
    timeout: 10000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        // 从本地存储获取token
        const token = localStorage.getItem('token')
        if (token) {
            // 如果token存在，添加到请求头
            config.headers['Authorization'] = 'Bearer ' + token
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        // 对响应数据做处理
        return response.data
    },
    error => {
        // 对响应错误做处理
        return Promise.reject(error)
    }
)

export default service


// 其它组件如何使用
// import request from '@/utils/request'
//
// // 获取用户信息
// const getUserInfo = async () => {
//     try {
//         const data = await request.get('/user/info')
//         console.log(data)
//     } catch (error) {
//         console.error(error)
//     }
// }