<template>
  <el-card shadow="never">
    <el-form inline :model="query" @submit.prevent>
      <el-form-item label="订单号">
        <el-input v-model="query.orderId" placeholder="订单ID" clearable style="width: 190px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="orderId" label="订单号" width="200" />
      <el-table-column label="平台收益" width="110">
        <template #default="{ row }">￥{{ Number(row.profitPlat || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="代理收益" width="110">
        <template #default="{ row }">￥{{ Number(row.profitAgent || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="跑腿收益" width="110">
        <template #default="{ row }">￥{{ Number(row.profitRunner || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="用户收益" width="110">
        <template #default="{ row }">￥{{ Number(row.profitUser || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="165" />
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
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pageCapitalFlow } from '../../api/capital'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const dateRange = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, orderId: '' })

const load = async () => {
  loading.value = true
  try {
    const res = await pageCapitalFlow({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      orderId: query.orderId || undefined,
      beginTime: dateRange.value?.[0] || undefined,
      endTime: dateRange.value?.[1] || undefined
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
  query.orderId = ''
  dateRange.value = []
  search()
}

onMounted(load)
</script>

<style scoped>
.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
