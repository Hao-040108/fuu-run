<template>
  <el-card shadow="never">
    <el-form inline :model="query" @submit.prevent>
      <el-form-item label="校区">
        <el-select v-model="query.schoolId" placeholder="全部" clearable style="width: 160px">
          <el-option v-for="s in schools" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="申诉状态">
        <el-select v-model="query.appealStatus" placeholder="全部" clearable style="width: 130px">
          <el-option label="处理中" :value="2" />
          <el-option label="已通过" :value="1" />
          <el-option label="已驳回" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="orderId" label="订单号" width="200" />
      <el-table-column label="所属校区" width="160">
        <template #default="{ row }">{{ schoolName(row.schoolId) }}</template>
      </el-table-column>
      <el-table-column prop="appealReason" label="申诉理由" min-width="220" show-overflow-tooltip />
      <el-table-column label="申诉状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.appealStatus)">{{ STATUS_MAP[row.appealStatus] || row.appealStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remarks" label="处理备注" min-width="140" show-overflow-tooltip />
      <el-table-column prop="appealTime" label="申诉时间" width="165" />
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openHandle(row)">
            {{ row.appealStatus === 2 ? '处理' : '查看' }}
          </el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.appealStatus === 2 ? '处理申诉' : '申诉详情'" width="520px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="订单号">{{ form.orderId }}</el-descriptions-item>
        <el-descriptions-item label="申诉理由">{{ form.appealReason }}</el-descriptions-item>
        <el-descriptions-item label="申诉时间">{{ form.appealTime }}</el-descriptions-item>
        <el-descriptions-item v-if="form.remarks" label="已有备注">{{ form.remarks }}</el-descriptions-item>
      </el-descriptions>
      <el-form
        v-if="form.appealStatus === 2"
        ref="formRef"
        :model="handleForm"
        :rules="rules"
        label-width="90px"
        style="margin-top: 16px"
      >
        <el-form-item label="处理结果" prop="appealStatus">
          <el-radio-group v-model="handleForm.appealStatus">
            <el-radio :value="1">通过（发起退款）</el-radio>
            <el-radio :value="0">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="handleForm.remarks" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button v-if="form.appealStatus === 2" type="primary" :loading="saving" @click="onHandle">提交处理</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { editAppeal, pageAppeals } from '../../api/order'
import { listSchools } from '../../api/school'

const STATUS_MAP = { 0: '已驳回', 1: '已通过', 2: '处理中' }
const statusType = (s) => (s === 1 ? 'success' : s === 0 ? 'danger' : 'warning')

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const schools = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, schoolId: null, appealStatus: null })
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, orderId: '', appealReason: '', appealStatus: 2, appealTime: '', remarks: '' })
const handleForm = reactive({ appealStatus: 1, remarks: '' })
const rules = {
  appealStatus: [{ required: true, message: '请选择处理结果', trigger: 'change' }]
}

const schoolName = (id) => schools.value.find((s) => s.id === id)?.name || id

const load = async () => {
  loading.value = true
  try {
    const res = await pageAppeals({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      schoolId: query.schoolId || undefined,
      appealStatus: query.appealStatus === null ? undefined : query.appealStatus
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

const openHandle = (row) => {
  Object.assign(form, {
    id: row.id,
    orderId: row.orderId,
    appealReason: row.appealReason,
    appealStatus: row.appealStatus,
    appealTime: row.appealTime,
    remarks: row.remarks
  })
  handleForm.appealStatus = 1
  handleForm.remarks = ''
  dialogVisible.value = true
}

const onHandle = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    await editAppeal({ id: form.id, appealStatus: handleForm.appealStatus, remarks: handleForm.remarks })
    ElMessage.success('处理成功（通过将自动发起退款，开发环境无商户号时退款可能失败）')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
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
