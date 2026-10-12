<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-button type="success" @click="openDialog()">新增校区</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="学校名称" min-width="180" show-overflow-tooltip />
      <el-table-column label="Logo" width="90">
        <template #default="{ row }">
          <el-avatar :size="40" shape="square" :src="row.logo" />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '运营中' : '已停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="收益占比(平台/代理/跑腿)" width="200">
        <template #default="{ row }">
          {{ row.profitPlat }}% / {{ row.profitAgent }}% / {{ row.profitRunner }}%
        </template>
      </el-table-column>
      <el-table-column prop="floorPrice" label="底价(元)" width="100" />
      <el-table-column prop="belongUid" label="代理人UID" width="110" />
      <el-table-column prop="createTime" label="创建时间" width="165" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pager"
      layout="total, prev, pager, next"
      :total="total"
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      @current-change="load"
    />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑校区' : '新增校区'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item label="学校名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="Logo地址">
          <el-input v-model="form.logo" placeholder="图片URL" />
        </el-form-item>
        <el-form-item label="代理人UID" prop="belongUid">
          <el-input-number v-model="form.belongUid" :min="1" style="width: 100%" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="平台占比">
              <el-input-number v-model="form.profitPlat" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="代理占比">
              <el-input-number v-model="form.profitAgent" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="跑腿占比">
              <el-input-number v-model="form.profitRunner" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="底价(元)" prop="floorPrice">
          <el-input-number v-model="form.floorPrice" :min="0" :precision="2" :step="0.5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">运营中</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
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
import { ElMessage } from 'element-plus'
import { addSchool, editSchool, listSchools } from '../../api/school'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({ pageNum: 1, pageSize: 10 })
const form = reactive({
  id: null,
  name: '',
  logo: '',
  belongUid: null,
  profitPlat: 10,
  profitAgent: 30,
  profitRunner: 60,
  floorPrice: 1,
  status: 1
})
const rules = {
  name: [{ required: true, message: '请输入学校名称', trigger: 'blur' }],
  belongUid: [{ required: true, message: '请输入代理人UID', trigger: 'blur' }],
  floorPrice: [{ required: true, message: '请输入底价', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await listSchools({ pageNum: query.pageNum, pageSize: query.pageSize })
    rows.value = res.rows || res.data || []
    total.value = res.total || (rows.value.length || 0)
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  Object.assign(form, {
    id: row ? row.id : null,
    name: row ? row.name : '',
    logo: row ? row.logo : '',
    belongUid: row ? row.belongUid : null,
    profitPlat: row ? row.profitPlat : 10,
    profitAgent: row ? row.profitAgent : 30,
    profitRunner: row ? row.profitRunner : 60,
    floorPrice: row ? Number(row.floorPrice) : 1,
    status: row ? row.status : 1
  })
  dialogVisible.value = true
}

const onSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await editSchool({ ...form })
    } else {
      await addSchool({ ...form })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
