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
