<template>
  <el-card shadow="never">
    <el-form inline :model="query" @submit.prevent>
      <el-form-item label="订单号">
        <el-input v-model="query.id" placeholder="订单ID" clearable style="width: 190px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="校区">
        <el-select v-model="query.schoolId" placeholder="全部" clearable style="width: 150px">
          <el-option v-for="s in schools" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="服务类型">
        <el-select v-model="query.serviceType" placeholder="全部" clearable style="width: 130px">
          <el-option label="帮取送" :value="0" />
          <el-option label="帮买/代购" :value="1" />
          <el-option label="万能帮" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
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
      <el-table-column label="来源" width="100">
        <template #default="{ row }">
          <el-tag :type="row.orderSource === 1 ? 'warning' : 'info'">
            {{ row.orderSource === 1 ? '商城代购' : '需求大厅' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="服务类型" width="100">
        <template #default="{ row }">
          {{ SERVICE_MAP[row.serviceType] || row.serviceType }}
        </template>
      </el-table-column>
      <el-table-column prop="tag" label="标签" width="110" show-overflow-tooltip />
      <el-table-column prop="detail" label="订单内容" min-width="220" show-overflow-tooltip />
      <el-table-column label="状态" width="95">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ STATUS_MAP[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="金额" width="90" />
      <el-table-column prop="userId" label="下单用户" width="110" />
      <el-table-column prop="runnerId" label="跑腿员" width="110" />
      <el-table-column prop="createTime" label="下单时间" width="165" />
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push('/order/detail/' + row.id)">详情</el-button>
          <el-button
            v-if="row.status === 0 || row.status === 1"
            link
            type="danger"
            @click="onCancel(row)"
          >取消</el-button>
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
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageHallOrders, cancelOrder } from '../../api/order'
import { listSchools } from '../../api/school'

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
const SERVICE_MAP = { 0: '帮取送', 1: '帮买/代购', 2: '万能帮' }

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
const schools = ref([])
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  id: '',
  schoolId: null,
  serviceType: null,
  status: null
})

const load = async () => {
  loading.value = true
  try {
    const res = await pageHallOrders({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      id: query.id || undefined,
      schoolId: query.schoolId || undefined,
      serviceType: query.serviceType === null ? undefined : query.serviceType,
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
  query.id = ''
  query.schoolId = null
  query.serviceType = null
  query.status = null
  search()
}

const onCancel = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
    inputPlaceholder: '取消原因',
    inputValidator: (v) => (v && v.trim() ? true : '取消原因不能为空')
  })
  await cancelOrder({ orderId: row.id, cancelReason: value })
  ElMessage.success('已取消')
  load()
}

onMounted(async () => {
  try {
    const res = await listSchools({ pageNum: 1, pageSize: 100 })
    schools.value = res.rows || res.data || []
  } catch (e) {}
  load()
})
</script>

<style scoped>
.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
