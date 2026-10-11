// 代购商城接口
import { toRaw } from 'vue';
import { request } from '@/request/request.js'

// 商品分类列表
export const getMallCategories = () => {
	return request({
		url: '/mall/category/list',
		method: 'get',
	})
}

// 商品分页
export const getMallGoods = (params) => {
	return request({
		url: '/mall/goods/list',
		method: 'get',
		params: toRaw(params),
	})
}

// 商品详情
export const getMallGoodsDetail = (id) => {
	return request({
		url: '/mall/goods/detail/' + id,
		method: 'get',
	})
}

// 购物车列表
export const getCartList = () => {
	return request({
		url: '/mall/cart/list',
		method: 'get',
	})
}

// 加入购物车
export const addCart = (params) => {
	return request({
		url: '/mall/cart/add',
		method: 'post',
		data: params,
	})
}

// 修改购物车数量
export const updateCartItem = (params) => {
	return request({
		url: '/mall/cart/update',
		method: 'post',
		data: params,
	})
}

// 删除购物车条目
export const removeCartItem = (id) => {
	return request({
		url: '/mall/cart/remove/' + id,
		method: 'post',
	})
}

// 提交商城订单（购物车结算 / 立即购买）
export const submitMallOrder = (params) => {
	return request({
		url: '/mall/order/submit',
		method: 'post',
		data: params,
	})
}
