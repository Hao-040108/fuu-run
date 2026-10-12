<template>
  <el-card shadow="never" v-loading="loading">
    <el-form inline>
      <el-form-item label="角色">
        <el-select v-model="userType" style="width: 200px" @change="loadAssigned">
          <el-option label="校区代理" :value="1" />
          <el-option label="普通管理员" :value="2" />
          <el-option label="普通用户" :value="3" />
          <el-option label="跑腿用户" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="onSave">保存该角色权限</el-button>
        <el-button type="success" @click="openAdd">新增权限项</el-button>
      </el-form-item>
    </el-form>

    <el-tree
      ref="treeRef"
      :data="permTree"
      show-checkbox
      node-key="id"
      default-expand-all
      :props="{ label: (data) => data.name + (data.perms ? '（' + data.perms + '）' : ''), children: 'children' }"
    />

    <el-dialog v-model="dialogVisible" title="新增权限项" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="权限ID" prop="id">
          <el-input-number v-model="form.id" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="form.perms" placeholder="如 order:order:add（菜单级可留空）" />
        </el-form-item>
        <el-form-item label="父级ID">
          <el-input-number v-model="form.parentId" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving2" @click="onAdd">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { addPerm, handleRolePerms, listPerms, listPermsByType } from '../../api/system'

const loading = ref(false)
const saving = ref(false)
const saving2 = ref(false)
const userType = ref(1)
const permTree = ref([])
const treeRef = ref()
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, name: '', perms: '', parentId: 0, sort: 0 })
const rules = {
  id: [{ required: true, message: '请输入权限ID', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

const loadAssigned = async () => {
  const res = await listPermsByType(userType.value)
  const ids = (res.data || []).map((p) => p.id)
  treeRef.value?.setCheckedKeys(ids)
}

const load = async () => {
  loading.value = true
  try {
    const res = await listPerms()
    permTree.value = Array.isArray(res) ? res : res.data || []
    await loadAssigned()
  } finally {
    loading.value = false
  }
}

const onSave = async () => {
  const checked = treeRef.value.getCheckedKeys()
  const half = treeRef.value.getHalfCheckedKeys()
  saving.value = true
  try {
    await handleRolePerms(userType.value, [...checked, ...half])
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

const openAdd = () => {
  Object.assign(form, { id: null, name: '', perms: '', parentId: 0, sort: 0 })
  dialogVisible.value = true
}

const onAdd = async () => {
  await formRef.value.validate()
  saving2.value = true
  try {
    await addPerm({ ...form })
    ElMessage.success('新增成功')
    dialogVisible.value = false
    load()
  } finally {
    saving2.value = false
  }
}

onMounted(load)
</script>
