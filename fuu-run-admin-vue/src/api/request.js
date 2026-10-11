import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：统一携带 Sa-Token（Bearer 前缀与后端 token-prefix 配置一致）
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

// 响应拦截：R 结构统一处理（code==200 放行，401 跳登录）
request.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data && typeof data === 'object' && 'code' in data) {
      if (data.code === 401) {
        localStorage.removeItem('token')
        router.push('/login')
        return Promise.reject(new Error(data.msg || '登录已过期，请重新登录'))
      }
      if (data.code !== 200) {
        ElMessage.error(data.msg || '操作失败')
        return Promise.reject(new Error(data.msg || '操作失败'))
      }
    }
    return data
  },
  (error) => {
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request
