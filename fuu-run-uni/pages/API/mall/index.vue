<template>
	<div class="mallHome">
		<nut-searchbar v-model="keyword" placeholder="搜索代购商品" @search="onSearch"></nut-searchbar>

		<scroll-view scroll-x class="catBar">
			<div class="catItem" :class="{ active: categoryId == null }" @click="selectCategory(null)">全部</div>
			<div class="catItem" v-for="c in categories" :key="c.id" :class="{ active: categoryId == c.id }"
				@click="selectCategory(c.id)">{{ c.name }}</div>
		</scroll-view>

		<div class="goodsWrap">
			<div class="goodsCard" v-for="g in goods" :key="g.id" @click="toDetail(g.id)">
				<image class="goodsImg" :src="g.image || '/static/logo.png'" mode="aspectFill"></image>
				<div class="goodsName">{{ g.name }}</div>
				<div class="goodsMeta">
					<span class="price">￥{{ g.price }}</span>
					<span class="fee">代购费 ￥{{ g.purchaseFee }}</span>
				</div>
				<div class="goodsSales">已售 {{ g.sales || 0 }}{{ g.unit ? ' · ' + g.unit : '' }}</div>
			</div>
		</div>

		<div v-if="loaded && goods.length == 0" class="empty">
			<nut-empty description="暂无商品"></nut-empty>
		</div>
		<div v-if="finished && goods.length > 0" class="finished">— 已经到底啦 —</div>

		<div class="cartBar">
			<nut-button type="primary" size="small" @click="toCart">购物车（{{ cartCount }}）</nut-button>
		</div>
	</div>
</template>

<script>
	import { getMallCategories, getMallGoods, getCartList } from '@/request/apis/mall.js'

	export default {
		data() {
			return {
				keyword: '',
				categoryId: null,
				categories: [],
				goods: [],
				pageNum: 1,
				pageSize: 20,
				finished: false,
				loaded: false,
				cartCount: 0
			}
		},
		onLoad() {
			this.init()
		},
		onShow() {
			this.loadCartCount()
		},
		onReachBottom() {
			if (!this.finished) {
				this.pageNum++
				this.loadGoods()
			}
		},
		methods: {
			async init() {
				try {
					const cats = await getMallCategories()
					this.categories = cats.data || []
					await this.loadGoods()
				} catch (e) {}
			},
			async loadGoods() {
				try {
					const res = await getMallGoods({
						pageNum: this.pageNum,
						pageSize: this.pageSize,
						categoryId: this.categoryId || undefined,
						keyword: this.keyword || undefined
					})
					const rows = res.rows || []
					this.goods = this.pageNum == 1 ? rows : this.goods.concat(rows)
					this.finished = this.goods.length >= res.total
					this.loaded = true
				} catch (e) {}
			},
			selectCategory(id) {
				this.categoryId = id
				this.pageNum = 1
				this.loadGoods()
			},
			onSearch() {
				this.pageNum = 1
				this.loadGoods()
			},
			toDetail(id) {
				uni.navigateTo({ url: '/pages/API/mall/detail?id=' + id })
			},
			toCart() {
				uni.navigateTo({ url: '/pages/API/mall/cart' })
			},
			async loadCartCount() {
				try {
					const res = await getCartList()
					this.cartCount = (res.data || []).reduce((sum, item) => sum + item.quantity, 0)
				} catch (e) {}
			}
		}
	}
</script>

<style scoped>
	.mallHome {
		padding-bottom: 90px;
		background: #f8f8f8;
		min-height: 100vh;
	}

	.catBar {
		white-space: nowrap;
		background: #fff;
		padding: 12px 8px;
	}

	.catItem {
		display: inline-block;
		padding: 6px 16px;
		margin-right: 10px;
		border-radius: 16px;
		background: #f2f2f2;
		color: #666;
		font-size: 13px;
	}

	.catItem.active {
		background: #b300fa;
		color: #fff;
	}

	.goodsWrap {
		display: flex;
		flex-wrap: wrap;
		padding: 8px;
	}

	.goodsCard {
		width: calc(50% - 8px);
		margin: 4px;
		background: #fff;
		border-radius: 10px;
		overflow: hidden;
		padding-bottom: 10px;
	}

	.goodsImg {
		width: 100%;
		height: 150px;
		background: #f2f2f2;
	}

	.goodsName {
		font-size: 14px;
		color: #222;
		padding: 8px 10px 4px;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.goodsMeta {
		padding: 0 10px;
		display: flex;
		align-items: baseline;
		justify-content: space-between;
	}

	.price {
		color: #fa2c19;
		font-size: 16px;
		font-weight: 600;
	}

	.fee {
		color: #999;
		font-size: 11px;
	}

	.goodsSales {
		padding: 4px 10px 0;
		color: #bbb;
		font-size: 11px;
	}

	.finished {
		text-align: center;
		color: #bbb;
		font-size: 12px;
		padding: 10px 0;
	}

	.cartBar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 60px;
		display: flex;
		justify-content: center;
		pointer-events: none;
	}

	.cartBar .nut-button {
		pointer-events: auto;
		box-shadow: 0 4px 12px rgba(0, 0, 0, 0.18);
	}
</style>
