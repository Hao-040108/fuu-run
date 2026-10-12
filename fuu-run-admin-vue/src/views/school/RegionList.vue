<template>
  <el-card shadow="never">
    <el-form inline @submit.prevent>
      <el-form-item label="校区">
        <el-select v-model="schoolId" placeholder="请选择校区" style="width: 220px" @change="load">
          <el-option v-for="s in schools" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="success" :disabled="!schoolId" @click="openDialog(null, 0)">新增区域</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="areas" border stripe row-key="id">
      <el-table-column type="expand">
        <template #default="{ row }">
          <div class="expandWrap">
            <div class="expandHead">
              <span>楼栋列表</span>
              <el-button size="small" type="primary" @click="openDialog(null, 1, row.id)">添加楼栋</el-button>
            </div>
            <el-table :data="buildings(row.id)" size="small" border>
              <el-table-column prop="name" label="楼栋名称" min-width="160" />
              <el-table-column prop="remark" label="备注" min-width="160" />
              <el-table-column prop="createTime" label="创建时间" width="165" />
              <el-table-column label="操作" width="140">
                <template #default="{ row: b }">
                  <el-button link type="primary" @click="openDialog(b, 1, row.id)">编辑</el-button>
                  <el-button link type="danger" @click="onRemove(b)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="区域名称" min-width="160" />
      <el-table-column label="楼栋数量" width="100" align="center">
        <template #default="{ row }">{{ buildings(row.id).length }}</template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="160" />
      <el-table-column prop="createTime" label="创建时间" width="165" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row, 0)">编辑</el-button>
          <el-button link type="success" @click="openDialog(null, 1, row.id)">添加楼栋</el-button>
          <el-button link type="danger" @click="onRemove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item :label="form.type === 0 ? '区域名称' : '楼栋名称'" prop="name">
          <el-input v-model="form.name" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addRegion, editRegion, listSchools, queryRegions, removeRegions } from '../../api/school'

const loading = ref(false)
const saving = ref(false)
const schools = ref([])
const schoolId = ref(null)
const items = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, schoolId: null, type: 0, parentId: 0, name: '', remark: '' })
const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

const dialogTitle = computed(() => {
  const kind = form.type === 0 ? '区域' : '楼栋'
  return (form.id ? '编辑' : '新增') + kind
})

const areas = computed(() => items.value.filter((i) => i.type === 0))
const buildings = (areaId) => items.value.filter((i) => i.type === 1 && i.parentId === areaId)

const load = async () => {
  if (!schoolId.value) return
  loading.value = true
  try {
    const res = await queryRegions({ schoolId: schoolId.value })
    items.value = res.data || []
  } finally {
    loading.value = false
  }
}

const openDialog = (row, type, parentId) => {
  Object.assign(form, {
    id: row ? row.id : null,
    schoolId: schoolId.value,
    type: row ? row.type : type,
    parentId: row ? row.parentId : parentId || 0,
    name: row ? row.name : '',
    remark: row ? row.remark : ''
  })
  dialogVisible.value = true
}

const onSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await editRegion({ ...form })
    } else {
      await addRegion({ ...form })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

const onRemove = async (row) => {
  const tip = row.type === 0 ? '删除区域会同时删除其下所有楼栋，' : ''
  await ElMessageBox.confirm(`${tip}确认删除「${row.name}」？`, '警告', { type: 'warning' })
  await removeRegions(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  try {
    const res = await listSchools({ pageNum: 1, pageSize: 100 })
    schools.value = res.rows || res.data || []
    if (schools.value.length) {
      schoolId.value = schools.value[0].id
      load()
    }
  } catch (e) {}
})
</script>

<style scoped>
.expandWrap {
  padding: 10px 20px 16px;
  background: #fafafa;
}
.expandHead {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  color: #606266;
  font-size: 13px;
}
</style>
