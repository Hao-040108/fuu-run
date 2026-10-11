<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-label">商品总数</div>
          <div class="stat-value">{{ stats.goodsTotal }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-label">商品分类</div>
          <div class="stat-value">{{ stats.categoryTotal }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-label">商城订单</div>
          <div class="stat-value">{{ stats.orderTotal }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 16px">
      <template #header>快捷入口</template>
      <el-space wrap>
        <el-button type="primary" @click="$router.push('/goods/list')">商品管理</el-button>
        <el-button @click="$router.push('/goods/category')">商品分类</el-button>
        <el-button @click="$router.push('/order/mall')">商城订单</el-button>
        <el-button link type="info" @click="openApiDoc">打开后端接口文档（Swagger）</el-button>
      </el-space>
      <el-divider />
      <div class="tip">
        提示：本项目为"校园代购平台"二创版，管理端使用 Vue3 + Element Plus 重写；
        小程序端（fuu-run-uni）提供商城下单与"需求大厅"发布服务。
        开发环境已开启模拟支付：下单后在订单详情/接口 /dev/mockPay/{orderId} 可完成支付闭环。
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { listCategory, pageGoods, pageMallOrders } from '../api/mall'

const stats = reactive({ goodsTotal: '-', categoryTotal: '-', orderTotal: '-' })

const openApiDoc = () => window.open('http://localhost:8081/swagger-ui/index.html', '_blank')

onMounted(async () => {
  try {
    const goods = await pageGoods({ pageNum: 1, pageSize: 1 })
    stats.goodsTotal = goods.total
    const cats = await listCategory()
    stats.categoryTotal = (cats.data || []).length
    const orders = await pageMallOrders({ pageNum: 1, pageSize: 1 })
    stats.orderTotal = orders.total
  } catch (e) {
    // 忽略：接口异常时保留占位符
  }
})
</script>

<style scoped>
.stat-label {
  color: #909399;
  font-size: 13px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  margin-top: 8px;
  color: #303133;
}
.tip {
  color: #909399;
  font-size: 13px;
  line-height: 1.8;
}
</style>
