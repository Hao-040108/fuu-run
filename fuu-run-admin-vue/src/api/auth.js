import request from './request'

// 登录
export const login = (data) => request.post('/login', data)

// 获取当前用户信息（含权限与全局配置 config）
export const getInfo = () => request.get('/getInfo')
