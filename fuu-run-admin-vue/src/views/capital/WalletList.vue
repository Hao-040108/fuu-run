<template>
  <el-card shadow="never">
    <el-form inline :model="query" @submit.prevent>
      <el-form-item label="用户UID">
        <el-input v-model="query.uid" placeholder="UID" clearable style="width: 150px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="uid" label="用户UID" width="110" />
      <el-table-column label="当前余额" width="130">
        <template #default="{ row }">
          <span class="green">￥{{ Number(row.balance || 0).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="已提现" width="130">
        <template #default="{ row }">
          <span class="blue">￥{{ Number(row.withdrawn || 0).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="总收入" width="130">
        <template #default="{ row }">
          ￥{{ (Number(row.balance || 0) + Number(row.withdrawn || 0)).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column prop="updateTime" label="更新时间" width="170" />
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
import { pageWallets } from '../../api/capital'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, uid: '' })

const load = async () => {
  loading.value = true
  try {
    const res = await pageWallets({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      uid: query.uid || undefined
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

onMounted(load)
</script>

<style scoped>
.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
.green {
  color: #52c41a;
  font-weight: 600;
}
.blue {
  color: #1890ff;
  font-weight: 600;
}
</style>
