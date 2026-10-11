<template>
	<div class="detail">
		<image class="hero" :src="goods.image || '/static/logo.png'" mode="aspectFill"></image>

		<div class="block">
			<div class="name">{{ goods.name }}</div>
			<div class="row">
				<span class="price">￥{{ goods.price }}</span>
				<span class="unit" v-if="goods.unit">/{{ goods.unit }}</span>
			</div>
			<div class="fee">代购服务费：￥{{ goods.purchaseFee }}/件</div>
		</div>

		<div class="block">
			<div class="descTitle">商品描述</div>
			<div class="desc">{{ goods.description || '暂无描述' }}</div>
		</div>

		<div class="block qtyRow">
			<span>购买数量</span>
			<div class="stepper">
				<nut-button size="mini" @click="minus">-</nut-button>
				<span class="qtyNum">{{ quantity }}</span>
				<nut-button size="mini" @click="plus">+</nut-button>
			</div>
		</div>

		<div class="bar">
			<nut-button type="warning" @click="onAddCart">加入购物车</nut-button>
			<nut-button type="primary" @click="onBuyNow">立即购买</nut-button>
		</div>
	</div>
</template>

<script>
	import { getMallGoodsDetail, addCart } from '@/request/apis/mall.js'

	export default {
		data() {
			return {
				id: null,
				goods: {},
				quantity: 1
			}
		},
		onLoad(options) {
			this.id = options.id
			this.load()
		},
		methods: {
			async load() {
				try {
					const res = await getMallGoodsDetail(this.id)
					this.goods = res.data || {}
				} catch (e) {}
			},
			minus() {
				if (this.quantity > 1) {
					this.quantity--
				}
			},
			plus() {
				if (this.quantity < 99) {
					this.quantity++
				}
			},
			async onAddCart() {
				try {
					await addCart({ goodsId: this.id, quantity: this.quantity })
					uni.showToast({ title: '已加入购物车', icon: 'success' })
				} catch (e) {}
			},
			onBuyNow() {
				uni.navigateTo({
					url: '/pages/API/mall/settle?goodsId=' + this.id + '&quantity=' + this.quantity
				})
			}
		}
	}
</script>

<style scoped>
	.detail {
		background: #f8f8f8;
		min-height: 100vh;
		padding-bottom: 90px;
	}

	.hero {
		width: 100%;
		height: 280px;
		background: #f2f2f2;
	}

	.block {
		background: #fff;
		margin: 10px;
		border-radius: 10px;
		padding: 14px;
	}

	.name {
		font-size: 17px;
		font-weight: 600;
		color: #222;
	}

	.row {
		margin-top: 8px;
	}

	.price {
		color: #fa2c19;
		font-size: 22px;
		font-weight: 700;
	}

	.unit {
		color: #999;
		font-size: 12px;
	}

	.fee {
		margin-top: 6px;
		color: #e07b00;
		font-size: 13px;
	}

	.descTitle {
		font-size: 14px;
		font-weight: 600;
		margin-bottom: 8px;
	}

	.desc {
		color: #666;
		font-size: 13px;
		line-height: 1.7;
	}

	.qtyRow {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.stepper {
		display: flex;
		align-items: center;
	}

	.qtyNum {
		padding: 0 14px;
		font-size: 15px;
	}

	.bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		display: flex;
		background: #fff;
		padding: 10px 14px;
		box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.06);
	}

	.bar .nut-button {
		flex: 1;
		margin: 0 6px;
	}
</style>
