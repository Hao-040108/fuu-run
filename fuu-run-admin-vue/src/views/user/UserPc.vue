<template>
  <el-card shadow="never">
    <el-form inline :model="query" @submit.prevent>
      <el-form-item label="UID">
        <el-input v-model="query.uid" placeholder="用户UID" clearable style="width: 130px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="用户名">
        <el-input v-model="query.username" placeholder="用户名" clearable style="width: 140px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="query.phone" placeholder="手机号" clearable style="width: 140px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="用户类型">
        <el-select v-model="query.userType" placeholder="全部" clearable style="width: 130px">
          <el-option v-for="(label, code) in USER_TYPE_MAP" :key="code" :label="label" :value="Number(code)" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button type="success" @click="openDialog()">新增账号</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="uid" label="UID" width="90" />
      <el-table-column label="用户名" width="130">
        <template #default="{ row }">{{ row.userPc?.username }}</template>
      </el-table-column>
      <el-table-column label="手机号" width="130">
        <template #default="{ row }">{{ row.userPc?.phone }}</template>
      </el-table-column>
      <el-table-column label="真实姓名" width="110">
        <template #default="{ row }">{{ row.userPc?.name }}</template>
      </el-table-column>
      <el-table-column label="头像" width="80">
        <template #default="{ row }">
          <el-avatar :size="36" :src="row.userPc?.avatar" />
        </template>
      </el-table-column>
      <el-table-column label="性别" width="70">
        <template #default="{ row }">{{ row.userPc?.sex === 1 ? '男' : '女' }}</template>
      </el-table-column>
      <el-table-column label="账号状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.userPc?.status === 1 ? 'success' : 'danger'">
            {{ row.userPc?.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="用户类型" width="110">
        <template #default="{ row }">
          <el-tag type="info">{{ USER_TYPE_MAP[row.userType] || row.userType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column prop="loginTime" label="最后登录" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="warning" @click="onResetPwd(row)">重置密码</el-button>
          <el-button link type="danger" @click="onRemove(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑账号' : '新增账号'" width="540px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="初始密码" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.sex">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">正常</el-radio>
                <el-radio :value="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="头像地址">
          <el-input v-model="form.avatar" placeholder="图片URL（可留空）" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
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
import { addUserPc, editUserPc, pageUserPc, removeUserPc, resetUserPwd } from '../../api/user'

const USER_TYPE_MAP = {
  0: '超级管理员',
  1: '校区代理',
  2: '普通管理员',
  3: '普通用户',
  4: '跑腿用户'
}

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({ pageNum: 1, pageSize: 10, uid: '', username: '', phone: '', userType: null })
const form = reactive({
  id: null,
  uid: null,
  username: '',
  password: '',
  phone: '',
  name: '',
  sex: 1,
  status: 1,
  avatar: '',
  email: ''
})
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await pageUserPc({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      uid: query.uid || undefined,
      username: query.username || undefined,
      phone: query.phone || undefined,
      userType: query.userType === null ? undefined : query.userType
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

const openDialog = (row) => {
  const pc = row?.userPc || {}
  Object.assign(form, {
    id: pc.id || null,
    uid: row?.uid || null,
    username: pc.username || '',
    password: '',
    phone: pc.phone || '',
    name: pc.name || '',
    sex: pc.sex ?? 1,
    status: pc.status ?? 1,
    avatar: pc.avatar || '',
    email: pc.email || ''
  })
  dialogVisible.value = true
}

const onSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      const { password, ...rest } = form
      await editUserPc(rest)
    } else {
      const { id, uid, ...rest } = form
      await addUserPc(rest)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

const onResetPwd = async (row) => {
  await ElMessageBox.confirm(`确认重置「${row.userPc?.username}」的密码？`, '提示', { type: 'warning' })
  await resetUserPwd(row.uid)
  ElMessage.success('已重置（默认密码以系统设定为准）')
}

const onRemove = async (row) => {
  await ElMessageBox.confirm(`确认删除账号「${row.userPc?.username}」？该操作不可恢复。`, '警告', { type: 'warning' })
  await removeUserPc(row.uid)
  ElMessage.success('删除成功')
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
