<template>
  <el-card shadow="never">
    <el-form inline :model="query" @submit.prevent>
      <el-form-item label="用户UID">
        <el-input v-model="query.uid" placeholder="UID" clearable style="width: 140px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
          <el-option label="审核中" :value="2" />
          <el-option label="已通过" :value="1" />
          <el-option label="已驳回" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="提现ID" width="190" />
      <el-table-column prop="uid" label="用户UID" width="110" />
      <el-table-column label="提现金额" width="110">
        <template #default="{ row }">￥{{ Number(row.cash || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="platform" label="提现平台" width="110" />
      <el-table-column prop="card" label="卡号/账号" min-width="160" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ STATUS_MAP[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="feedback" label="处理反馈" min-width="140" show-overflow-tooltip />
      <el-table-column prop="createTime" label="申请时间" width="165" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 2">
            <el-button link type="success" @click="onAudit(row, 1)">通过</el-button>
            <el-button link type="danger" @click="onAudit(row, 0)">驳回</el-button>
          </template>
          <span v-else style="color: #909399">已处理</span>
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
import { auditWithdraw, pageWithdraw } from '../../api/capital'

const STATUS_MAP = { 0: '已驳回', 1: '已通过', 2: '审核中' }
const statusType = (s) => (s === 1 ? 'success' : s === 0 ? 'danger' : 'warning')

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, uid: '', status: null })

const load = async () => {
  loading.value = true
  try {
    const res = await pageWithdraw({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      uid: query.uid || undefined,
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

const onAudit = async (row, status) => {
  let feedback = ''
  if (status === 0) {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回提现', {
      inputValidator: (v) => (v && v.trim() ? true : '驳回原因不能为空')
    })
    feedback = value
  } else {
    await ElMessageBox.confirm(`确认通过 ￥${Number(row.cash).toFixed(2)} 的提现申请？`, '提示', { type: 'warning' })
  }
  await auditWithdraw({ id: row.id, status, feedback })
  ElMessage.success('操作成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
