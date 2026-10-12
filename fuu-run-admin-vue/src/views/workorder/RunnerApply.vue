<template>
  <el-card shadow="never">
    <el-form inline :model="query" @submit.prevent>
      <el-form-item label="校区">
        <el-select v-model="query.schoolId" placeholder="全部" clearable style="width: 160px">
          <el-option v-for="s in schools" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="申请状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
          <el-option label="待审核" :value="2" />
          <el-option label="已通过" :value="1" />
          <el-option label="已驳回" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="uid" label="申请人" width="110" />
      <el-table-column prop="realname" label="真实姓名" width="110" />
      <el-table-column label="性别" width="80">
        <template #default="{ row }">{{ row.gender === 0 ? '女' : row.gender === 1 ? '男' : '-' }}</template>
      </el-table-column>
      <el-table-column label="所属校区" width="170">
        <template #default="{ row }">{{ row.schoolName || schoolName(row.schoolId) }}</template>
      </el-table-column>
      <el-table-column label="学生证" width="110">
        <template #default="{ row }">
          <el-image
            v-if="row.studentCardUrl"
            :src="row.studentCardUrl"
            :preview-src-list="[row.studentCardUrl]"
            fit="cover"
            style="width: 60px; height: 42px; border-radius: 4px"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="申请状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ STATUS_MAP[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remarks" label="备注" min-width="140" show-overflow-tooltip />
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
import request from '../../api/request'
import { listSchools } from '../../api/school'

const STATUS_MAP = { 0: '已驳回', 1: '已通过', 2: '待审核' }
const statusType = (s) => (s === 1 ? 'success' : s === 0 ? 'danger' : 'warning')

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const schools = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, schoolId: null, status: null })

const schoolName = (id) => schools.value.find((s) => s.id === id)?.name || id

const load = async () => {
  loading.value = true
  try {
    const res = await request.get('/system/runnerApply/list', {
      params: {
        pageNum: query.pageNum,
        pageSize: query.pageSize,
        schoolId: query.schoolId || undefined,
        status: query.status === null ? undefined : query.status
      }
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
  let remarks = ''
  if (status === 0) {
    const { value } = await ElMessageBox.prompt('请输入驳回原因（必填）', '驳回申请', {
      inputValidator: (v) => (v && v.trim() ? true : '驳回原因不能为空')
    })
    remarks = value
  } else {
    await ElMessageBox.confirm(`确认通过「${row.realname || row.uid}」的跑腿申请？通过后该用户可接单。`, '提示', {
      type: 'warning'
    })
  }
  await request.put('/system/runnerApply/edit', { id: row.id, status, remarks })
  ElMessage.success('操作成功')
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
