import request from './request'

// ==================== PC 用户 ====================
export const pageUserPc = (params) => request.get('/system/userpc/list', { params })
export const addUserPc = (data) => request.post('/system/userpc/add', data)
export const editUserPc = (data) => request.put('/system/userpc/edit', data)
export const removeUserPc = (ids) => request.delete(`/system/userpc/${ids}`)
export const resetUserPwd = (uId) => request.put(`/system/userpc/resetPwd/${uId}`)

// ==================== 小程序用户 ====================
export const pageUserWx = (params) => request.get('/system/userwx/list', { params })
export const editUserWx = (data) => request.put('/system/userwx/edit', data)
