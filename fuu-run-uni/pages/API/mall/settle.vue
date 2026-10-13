<template>
	<div class="settle">
		<div class="block" @click="showAddrPopup = true">
			<div class="blockTitle">收货地址</div>
			<div v-if="address" class="addrInfo">
				<div class="addrMain">{{ address.title }} {{ address.detail }}</div>
				<div class="addrSub">{{ address.name }} {{ address.phone }}</div>
			</div>
			<div v-else class="addrEmpty">请选择收货地址（点击展开地址簿）</div>
		</div>

		<div class="block">
			<div class="blockTitle">订单内容</div>
			<div class="line" v-for="g in orderItems" :key="g.key">
				<span class="lineName">{{ g.name }} ×{{ g.quantity }}</span>
				<span>￥{{ g.subtotal }}</span>
			</div>
		</div>

		<div class="block">
			<div class="feeLine"><span>商品金额</span><span>￥{{ goodsAmount }}</span></div>
			<div class="feeLine"><span>代购服务费</span><span>￥{{ serviceFee }}</span></div>
			<div class="feeLine"><span>配送费（学校底价）</span><span>￥{{ deliveryFee }}</span></div>
			<div class="feeLine totalLine"><span>合计</span><span class="totalPrice">￥{{ totalAmount }}</span></div>
		</div>

		<div class="block">
			<div class="blockTitle">备注</div>
			<textarea class="remark" v-model="remark" placeholder="选填：楼层、送达时间、口味偏好等" maxlength="50" />
		</div>

		<div class="bar">
			<div class="barTotal">￥{{ totalAmount }}</div>
			<nut-button type="primary" :loading="submitting" @click="onSubmit">提交订单</nut-button>
		</div>

		<nut-popup v-model:visible="showAddrPopup" position="bottom" round>
			<div class="popupTitle">选择收货地址</div>
			<div class="addrItem" v-for="a in addresses" :key="a.id" @click="chooseAddr(a)">
				<div class="addrMain">{{ a.title }} {{ a.detail }}</div>
				<div class="addrSub">{{ a.name }} {{ a.phone }}</div>
			</div>
			<nut-empty v-if="addresses.length == 0" description="暂无收货地址，请先到我的-地址中添加"></nut-empty>
		</nut-popup>
	</div>
</template>

<script>
	import { getCartList, getMallGoodsDetail, submitMallOrder } from '@/request/apis/mall.js'
	import { getListAddress } from '@/request/apis/address.js'

	export default {
		data() {
			return {
				mode: 'cart',
				cartItemIds: [],
				goodsId: null,
				quantity: 1,
				school: {},
				orderItems: [],
				address: null,
				addresses: [],
				showAddrPopup: false,
				remark: '',
				submitting: false
			}
		},
		computed: {
			goodsAmount() {
				return this.orderItems.reduce((sum, i) => sum + Number(i.price) * i.quantity, 0).toFixed(2)
			},
			serviceFee() {
				return this.orderItems.reduce((sum, i) => sum + Number(i.purchaseFee || 0) * i.quantity, 0).toFixed(2)
			},
			deliveryFee() {
				return Number(this.school.floorPrice || 0).toFixed(2)
			},
			totalAmount() {
				return (Number(this.goodsAmount) + Number(this.serviceFee) + Number(this.deliveryFee)).toFixed(2)
			}
		},
		onLoad(options) {
			this.school = uni.getStorageSync('currentSchool') || {}
			if (options.cartItemIds) {
				this.mode = 'cart'
				this.cartItemIds = String(options.cartItemIds).split(',').map((v) => Number(v))
			} else if (options.goodsId) {
				this.mode = 'direct'
				this.goodsId = Number(options.goodsId)
				this.quantity = Number(options.quantity || 1)
			}
			this.init()
		},
		methods: {
			async init() {
				await Promise.all([this.loadItems(), this.loadAddresses()])
			},
			async loadItems() {
				try {
					if (this.mode == 'cart') {
						const res = await getCartList()
						const all = res.data || []
						this.orderItems = all
							.filter((i) => this.cartItemIds.indexOf(i.id) >= 0)
							.map((i) => ({
								key: 'c' + i.id,
								name: i.name,
								price: i.price,
								purchaseFee: i.purchaseFee,
								quantity: i.quantity,
								subtotal: i.subtotal
							}))
					} else {
						const res = await getMallGoodsDetail(this.goodsId)
						const g = res.data || {}
						this.orderItems = [{
							key: 'g' + g.id,
							name: g.name,
							price: g.price,
							purchaseFee: g.purchaseFee,
							quantity: this.quantity,
							subtotal: (Number(g.price) * this.quantity).toFixed(2)
						}]
					}
				} catch (e) {}
			},
			async loadAddresses() {
				try {
					const res = await getListAddress({ pageNum: 1, pageSize: 50 })
					this.addresses = res.rows || res.data || []
					const def = this.addresses.find((a) => a.isDefault == 1) || this.addresses[0]
					if (def) {
						this.chooseAddr(def)
					}
				} catch (e) {}
			},
			chooseAddr(a) {
				this.address = a
				this.showAddrPopup = false
			},
			async onSubmit() {
				if (!this.school.id) {
					uni.showToast({ title: '请先选择校区', icon: 'none' })
					return
				}
				if (!this.address) {
					uni.showToast({ title: '请选择收货地址', icon: 'none' })
					return
				}
				if (this.orderItems.length == 0) {
					uni.showToast({ title: '没有可下单的商品', icon: 'none' })
					return
				}
				const payload = {
					schoolId: this.school.id,
					endAddress: {
						title: this.address.title,
						detail: this.address.detail,
						lat: this.address.lat || '',
						lon: this.address.lon || '',
						name: this.address.name,
						phone: this.address.phone
					},
					remark: this.remark,
					autoCancelTtl: 900
				}
				if (this.mode == 'cart') {
					payload.cartItemIds = this.cartItemIds
				} else {
					payload.goodsId = this.goodsId
					payload.quantity = this.quantity
				}
				this.submitting = true
				try {
					const res = await submitMallOrder(payload)
					const data = res.data || {}
					uni.showModal({
						title: '下单成功',
						content: '订单号：' + data.orderId + '\n合计 ￥' + data.totalAmount +
							'\n开发环境已开启模拟支付，可在订单详情中点击「模拟支付」完成支付闭环。',
						showCancel: false,
						success: () => {
							uni.switchTab({ url: '/pages/tabBar/order/order' })
						}
					})
				} catch (e) {
					// 错误信息由请求层提示
				} finally {
					this.submitting = false
				}
			}
		}
	}
</script>

<style scoped>
	.settle {
		background: #f8f8f8;
		min-height: 100vh;
		padding-bottom: 110px;
	}

	.block {
		background: #fff;
		margin: 10px;
		border-radius: 10px;
		padding: 14px;
	}

	.blockTitle {
		font-size: 13px;
		color: #999;
		margin-bottom: 8px;
	}

	.addrMain {
		font-size: 15px;
		color: #222;
	}

	.addrSub {
		font-size: 12px;
		color: #999;
		margin-top: 4px;
	}

	.addrEmpty {
		color: #b300fa;
		font-size: 14px;
	}

	.line {
		display: flex;
		justify-content: space-between;
		font-size: 14px;
		padding: 4px 0;
	}

	.lineName {
		color: #333;
		max-width: 70%;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.feeLine {
		display: flex;
		justify-content: space-between;
		font-size: 13px;
		color: #666;
		padding: 4px 0;
	}

	.totalLine {
		border-top: 1px dashed #eee;
		margin-top: 6px;
		padding-top: 10px;
		color: #222;
		font-size: 14px;
	}

	.totalPrice {
		color: #fa2c19;
		font-size: 18px;
		font-weight: 700;
	}

	.remark {
		width: 100%;
		height: 70px;
		font-size: 14px;
		background: #f8f8f8;
		border-radius: 8px;
		padding: 10px;
		box-sizing: border-box;
	}

	.bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		background: #fff;
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 12px 14px;
		box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.06);
	}

	.barTotal {
		color: #fa2c19;
		font-size: 20px;
		font-weight: 700;
	}

	.popupTitle {
		text-align: center;
		font-size: 15px;
		font-weight: 600;
		padding: 14px 0 6px;
	}

	.addrItem {
		padding: 12px 16px;
		border-bottom: 1px solid #f2f2f2;
	}
</style>
