<template>
	<div class="cartPage">
		<div class="item" v-for="item in items" :key="item.id">
			<image class="thumb" :src="item.image || '/static/logo.png'" mode="aspectFill"></image>
			<div class="info">
				<div class="name">{{ item.name }}</div>
				<div class="price">￥{{ item.price }}<span class="fee">（代购费 ￥{{ item.purchaseFee }}/件）</span></div>
				<div class="stepper">
					<nut-button size="mini" @click="changeQty(item, -1)">-</nut-button>
					<span class="qtyNum">{{ item.quantity }}</span>
					<nut-button size="mini" @click="changeQty(item, 1)">+</nut-button>
					<nut-button size="mini" type="danger" plain class="delBtn" @click="onRemove(item)">删除</nut-button>
				</div>
			</div>
		</div>

		<nut-empty v-if="loaded && items.length == 0" description="购物车还是空的，去商城逛逛吧"></nut-empty>

		<div class="footer" v-if="items.length > 0">
			<div class="total">
				<div>合计：<span class="totalPrice">￥{{ totalPrice }}</span></div>
				<div class="totalFee">含代购服务费 ￥{{ totalFee }}，另加配送费</div>
			</div>
			<nut-button type="primary" @click="toSettle">去结算</nut-button>
		</div>
	</div>
</template>

<script>
	import { getCartList, updateCartItem, removeCartItem } from '@/request/apis/mall.js'

	export default {
		data() {
			return {
				items: [],
				loaded: false
			}
		},
		computed: {
			totalPrice() {
				return this.items.reduce((sum, i) => sum + Number(i.subtotal), 0).toFixed(2)
			},
			totalFee() {
				return this.items.reduce((sum, i) => sum + Number(i.feeSubtotal), 0).toFixed(2)
			}
		},
		onShow() {
			this.load()
		},
		methods: {
			async load() {
				try {
					const res = await getCartList()
					this.items = res.data || []
				} catch (e) {}
				this.loaded = true
			},
			async changeQty(item, delta) {
				const quantity = item.quantity + delta
				if (quantity < 1) {
					return
				}
				try {
					await updateCartItem({ id: item.id, quantity })
					item.quantity = quantity
					item.subtotal = (Number(item.subtotal) / (item.quantity - delta) * quantity).toFixed(2)
					this.load()
				} catch (e) {}
			},
			onRemove(item) {
				uni.showModal({
					title: '提示',
					content: '确认从购物车移除「' + item.name + '」？',
					success: async (res) => {
						if (res.confirm) {
							try {
								await removeCartItem(item.id)
								this.load()
							} catch (e) {}
						}
					}
				})
			},
			toSettle() {
				const ids = this.items.map((i) => i.id).join(',')
				uni.navigateTo({ url: '/pages/API/mall/settle?cartItemIds=' + ids })
			}
		}
	}
</script>

<style scoped>
	.cartPage {
		background: #f8f8f8;
		min-height: 100vh;
		padding-bottom: 110px;
	}

	.item {
		display: flex;
		background: #fff;
		margin: 10px;
		border-radius: 10px;
		padding: 12px;
	}

	.thumb {
		width: 84px;
		height: 84px;
		border-radius: 8px;
		background: #f2f2f2;
	}

	.info {
		flex: 1;
		margin-left: 12px;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
	}

	.name {
		font-size: 14px;
		color: #222;
	}

	.price {
		color: #fa2c19;
		font-size: 15px;
		font-weight: 600;
	}

	.fee {
		color: #999;
		font-size: 11px;
		font-weight: 400;
	}

	.stepper {
		display: flex;
		align-items: center;
	}

	.qtyNum {
		padding: 0 12px;
		font-size: 14px;
	}

	.delBtn {
		margin-left: auto;
	}

	.footer {
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

	.totalPrice {
		color: #fa2c19;
		font-size: 18px;
		font-weight: 700;
	}

	.totalFee {
		color: #999;
		font-size: 11px;
		margin-top: 4px;
	}
</style>
