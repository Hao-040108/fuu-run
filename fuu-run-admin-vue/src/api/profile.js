import request from './request'

// 个人资料
export const updateProfile = (data) => request.put('/system/profile', data)
// 修改密码
export const updatePwd = (params) => request.put('/system/profile/updatePwd', null, { params })
// 发送邮箱验证码
export const sendEmailCode = (email) => request.get('/system/profile/sendEmailCode', { params: { email } })
// 绑定邮箱
export const bindEmail = (data) => request.post('/system/profile/bindEmail', data)
