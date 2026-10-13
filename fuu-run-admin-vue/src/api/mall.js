import request from './request'

// ==================== 商品分类 ====================
export const listCategory = () => request.get('/mall/admin/category/list')
export const addCategory = (data) => request.post('/mall/admin/category/add', data)
export const editCategory = (data) => request.post('/mall/admin/category/edit', data)
export const removeCategory = (id) => request.post(`/mall/admin/category/remove/${id}`)

// ==================== 商品 ====================
export const pageGoods = (params) => request.get('/mall/admin/goods/list', { params })
export const addGoods = (data) => request.post('/mall/admin/goods/add', data)
export const editGoods = (data) => request.post('/mall/admin/goods/edit', data)
export const removeGoods = (id) => request.post(`/mall/admin/goods/remove/${id}`)
export const changeGoodsStatus = (id, status) =>
  request.post('/mall/admin/goods/status', null, { params: { id, status } })

// ==================== 商城订单 ====================
export const pageMallOrders = (params) => request.get('/mall/admin/order/list', { params })
export const mallOrderDetail = (orderId) => request.get(`/mall/admin/order/detail/${orderId}`)

// ==================== 数据统计 ====================
export const statSummary = () => request.get('/mall/admin/stat/summary')
export const statCharts = (params) => request.get('/mall/admin/stat/charts', { params })
export const generateStat = (params) => request.post('/mall/admin/stat/generate', null, { params })

// ==================== 本地图片上传（开发演示环境） ====================
export const uploadGoodsImage = (formData) =>
  request.post('/mall/admin/upload/image', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
