import request from './request'

// OSS 文件管理
export const pageOss = (params) => request.get('/system/oss/list', { params })
export const uploadOss = (formData) =>
  request.post('/system/oss/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
export const removeOss = (ids) => request.delete(`/system/oss/${ids}`)
export const downloadOss = (ossId) => `/api/system/oss/download/${ossId}`

// OSS 配置
export const listOssConfig = () => request.get('/system/oss/config/list')
export const getOssConfig = (id) => request.get(`/system/oss/config/${id}`)
export const addOssConfig = (data) => request.post('/system/oss/config', data)
export const editOssConfig = (data) => request.put('/system/oss/config', data)
export const removeOssConfig = (ids) => request.delete(`/system/oss/config/${ids}`)
export const changeOssConfigStatus = (data) => request.put('/system/oss/config/changeStatus', data)
