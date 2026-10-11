<template>
  <el-card shadow="never">
    <el-form inline :model="query" @submit.prevent>
      <el-form-item label="订单状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 150px">
          <el-option v-for="(label, code) in STATUS_MAP" :key="code" :label="label" :value="Number(code)" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column label="订单号" width="200">
        <template #default="{ row }">{{ row.id }}</template>
      </el-table-column>
      <el-table-column prop="detail" label="订单内容" min-width="260" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ STATUS_MAP[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="goodsAmount" label="商品金额" width="100" />
      <el-table-column prop="totalAmount" label="订单总额" width="100" />
      <el-table-column prop="userId" label="下单用户" width="110" />
      <el-table-column prop="runnerId" label="跑腿员" width="110" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pager"
      layout="total, sizes, prev, pager, next"
      :total="total"
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :page-sizes="[10, 20, 50]"
      @current-change="load"
      @size-change="search"
    />

    <el-drawer v-model="drawerVisible" title="商城订单详情" size="620px">
      <template v-if="detail.order">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ detail.order.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detail.order.status)">
              {{ STATUS_MAP[detail.order.status] || detail.order.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单内容">{{ detail.order.detail }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">
            {{ detail.order.endAddress?.title }} {{ detail.order.endAddress?.detail }}
            （{{ detail.order.endAddress?.name }} {{ detail.order.endAddress?.phone }}）
          </el-descriptions-item>
          <el-descriptions-item label="商品金额">￥{{ detail.order.goodsAmount }}</el-descriptions-item>
          <el-descriptions-item label="订单总额">￥{{ detail.order.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="下单用户">{{ detail.order.userId }}</el-descriptions-item>
          <el-descriptions-item label="跑腿员">{{ detail.order.runnerId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.order.createTime }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">商品明细</el-divider>
        <el-table :data="detail.items" border size="small">
          <el-table-column prop="goodsName" label="商品" min-width="160" />
          <el-table-column prop="price" label="单价" width="90" />
          <el-table-column prop="quantity" label="数量" width="70" />
          <el-table-column prop="purchaseFee" label="代购费/件" width="100" />
        </el-table>
      </template>
    </el-drawer>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { mallOrderDetail, pageMallOrders } from '../../api/mall'

const STATUS_MAP = {
  0: '待支付',
  1: '待接单',
  2: '待配送',
  3: '配送中',
  4: '已送达',
  5: '已取消',
  10: '已完成',
  11: '已申诉'
}

const statusType = (s) => {
  if (s === 0) return 'warning'
  if (s === 1) return 'primary'
  if (s === 10) return 'success'
  if (s === 5) return 'info'
  if (s === 11) return 'danger'
  return 'info'
}

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, status: null })

const drawerVisible = ref(false)
const detail = reactive({ order: null, items: [] })

const load = async () => {
  loading.value = true
  try {
    const res = await pageMallOrders({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      status: query.status === null ? undefined : query.status
    })
    rows.value = res.rows || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const search = () => {
  query.pageNum = 1
  load()
}

const reset = () => {
  query.status = null
  search()
}

const openDetail = async (row) => {
  const res = await mallOrderDetail(row.id)
  detail.order = res.data.order
  detail.items = res.data.items || []
  drawerVisible.value = true
}

onMounted(load)
</script>

<style scoped>
.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
