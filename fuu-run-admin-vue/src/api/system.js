import request from './request'

// 全局配置（读：getInfo 返回的 config；写：）
export const editConfig = (data) => request.post('/system/system/config/edit', data)

// 系统监控
export const getMonitor = () => request.get('/system/system/monitor')

// 权限管理
export const listPerms = () => request.get('/system/system/perms/list')
export const listPermsByType = (userType) => request.get(`/system/system/perms/${userType}`)
export const handleRolePerms = (userType, perms) =>
  request.post('/system/system/roleperms/handle', perms, { params: { userType } })
export const addPerm = (data) => request.post('/system/system/perms/add', data)
export const delPerms = (perms) => request.delete('/system/system/perms/del', { data: perms })

// 轮播图
export const getCarousel = () => request.get('/carousel')
export const addCarousel = (data) => request.post('/carousel', data)
export const updateCarousel = (index, data) => request.put(`/carousel/${index}`, data)
export const deleteCarousel = (index) => request.delete(`/carousel/${index}`)
