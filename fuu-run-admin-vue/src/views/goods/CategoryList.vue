<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-button type="primary" @click="openDialog()">新增分类</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" min-width="180" />
      <el-table-column prop="sort" label="排序" width="90" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="onRemove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="如：零食饮料" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { addCategory, editCategory, listCategory, removeCategory } from '../../api/mall'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, name: '', sort: 0, status: 1 })
const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await listCategory()
    rows.value = res.data || []
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  form.id = row ? row.id : null
  form.name = row ? row.name : ''
  form.sort = row ? row.sort : 0
  form.status = row ? row.status : 1
  dialogVisible.value = true
}

const onSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await editCategory({ ...form })
    } else {
      await addCategory({ ...form })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await load()
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

const onRemove = async (row) => {
  await ElMessageBox.confirm(`确认删除分类「${row.name}」？`, '提示', { type: 'warning' })
  await removeCategory(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
</style>
