<template>
  <el-card shadow="never">
    <el-form inline :model="query" @submit.prevent>
      <el-form-item label="校区">
        <el-select v-model="query.schoolId" placeholder="全部" clearable style="width: 160px">
          <el-option v-for="s in schools" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="服务类型">
        <el-select v-model="query.serviceType" placeholder="全部" clearable style="width: 130px">
          <el-option label="帮取送" :value="0" />
          <el-option label="代购" :value="1" />
          <el-option label="万能帮" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button type="success" @click="openDialog()">新增标签</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="name" label="标签名称" min-width="140" />
      <el-table-column label="所属校区" width="170">
        <template #default="{ row }">{{ schoolName(row.schoolId) }}</template>
      </el-table-column>
      <el-table-column label="服务类型" width="120">
        <template #default="{ row }">
          <el-tag>{{ SERVICE_MAP[row.serviceType] || row.serviceType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="165" />
      <el-table-column prop="updateTime" label="修改时间" width="165" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="onRemove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑标签' : '新增标签'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="form.name" placeholder="如：奶茶、快递" />
        </el-form-item>
        <el-form-item label="所属校区" prop="schoolId">
          <el-select v-model="form.schoolId" placeholder="请选择" style="width: 100%">
            <el-option v-for="s in schools" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务类型" prop="serviceType">
          <el-radio-group v-model="form.serviceType">
            <el-radio :value="0">帮取送</el-radio>
            <el-radio :value="1">代购</el-radio>
            <el-radio :value="2">万能帮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addTag, editTag, listTags, removeTags } from '../../api/order'
import { listSchools } from '../../api/school'

const SERVICE_MAP = { 0: '代拿', 1: '代购', 2: '其他服务' }

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const schools = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({ schoolId: null, serviceType: null })
const form = reactive({ id: null, name: '', schoolId: null, serviceType: 0, remark: '' })
const rules = {
  name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
  schoolId: [{ required: true, message: '请选择校区', trigger: 'change' }],
  serviceType: [{ required: true, message: '请选择服务类型', trigger: 'change' }]
}

const schoolName = (id) => schools.value.find((s) => s.id === id)?.name || id

const load = async () => {
  loading.value = true
  try {
    const res = await listTags({
      schoolId: query.schoolId || undefined,
      serviceType: query.serviceType === null ? undefined : query.serviceType
    })
    rows.value = res.rows || res.data || []
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  Object.assign(form, {
    id: row ? row.id : null,
    name: row ? row.name : '',
    schoolId: row ? row.schoolId : null,
    serviceType: row ? row.serviceType : 0,
    remark: row ? row.remark : ''
  })
  dialogVisible.value = true
}

const onSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await editTag({ ...form })
    } else {
      await addTag({ ...form })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

const onRemove = async (row) => {
  await ElMessageBox.confirm(`确认删除标签「${row.name}」？`, '提示', { type: 'warning' })
  await removeTags(row.id)
  ElMessage.success('删除成功')
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
