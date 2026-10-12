import request from './request'

// ==================== 订单管理 ====================
export const pageHallOrders = (params) => request.get('/order/order/list/hall', { params })
export const pageUserOrders = (params) => request.get('/order/order/list/user', { params })
export const orderDetail = (orderId) => request.get(`/order/order/detail/${orderId}`)
export const orderPhone = (orderId) => request.get(`/order/order/phone/${orderId}`)
export const cancelOrder = (data) => request.post('/order/order/cancel', data)
export const cancelBefore = (orderId) => request.get(`/order/order/cancelbefore/${orderId}`)
export const refundOrder = (params) => request.get('/order/order/refund', { params })

// ==================== 标签管理 ====================
export const listTags = (params) => request.get('/order/tag/list', { params })
export const addTag = (data) => request.post('/order/tag/add', data)
export const editTag = (data) => request.put('/order/tag/edit', data)
export const removeTags = (ids) => request.delete(`/order/tag/${ids}`)

// ==================== 订单申诉 ====================
export const pageAppeals = (params) => request.get('/order/appeal/list', { params })
export const getAppeal = (orderId) => request.get(`/order/appeal/${orderId}`)
export const editAppeal = (data) => request.put('/order/appeal/edit', data)

// ==================== 订单聊天 ====================
export const listChat = (orderId, params) => request.get(`/order/chat/list/${orderId}`, { params })
