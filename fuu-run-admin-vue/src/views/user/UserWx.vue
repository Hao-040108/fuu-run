<template>
  <el-card shadow="never">
    <el-form inline :model="query" @submit.prevent>
      <el-form-item label="UID">
        <el-input v-model="query.uid" placeholder="用户UID" clearable style="width: 120px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="query.nickname" placeholder="昵称" clearable style="width: 130px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="query.phone" placeholder="手机号" clearable style="width: 130px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="校区">
        <el-select v-model="query.schoolId" placeholder="全部" clearable style="width: 150px">
          <el-option v-for="s in schools" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="uid" label="UID" width="90" />
      <el-table-column label="头像" width="70">
        <template #default="{ row }">
          <el-avatar :size="34" :src="wx(row).avatar" />
        </template>
      </el-table-column>
      <el-table-column label="昵称" width="120">
        <template #default="{ row }">{{ wx(row).nickname }}</template>
      </el-table-column>
      <el-table-column label="手机号" width="125">
        <template #default="{ row }">{{ wx(row).phone || '-' }}</template>
      </el-table-column>
      <el-table-column label="openid" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ wx(row).openid }}</template>
      </el-table-column>
      <el-table-column label="积分" width="75">
        <template #default="{ row }">{{ wx(row).points ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="跑腿员" width="85">
        <template #default="{ row }">
          <el-tag v-if="wx(row).isRunner === 1" type="success">是</el-tag>
          <el-tag v-else type="info">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单权限" width="90">
        <template #default="{ row }">
          <el-tag :type="wx(row).canOrder === 1 ? 'success' : 'danger'">{{ wx(row).canOrder === 1 ? '可下单' : '已禁止' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="接单权限" width="90">
        <template #default="{ row }">
          <el-tag :type="wx(row).canTake === 1 ? 'success' : 'danger'">{{ wx(row).canTake === 1 ? '可接单' : '已禁止' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="校区" width="150">
        <template #default="{ row }">{{ schoolName(wx(row).schoolId) }}</template>
      </el-table-column>
      <el-table-column label="信用分" width="80">
        <template #default="{ row }">{{ wx(row).creditScore ?? '-' }}</template>
      </el-table-column>
      <el-table-column prop="loginRegion" label="登录地区" width="150" show-overflow-tooltip />
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" title="编辑小程序用户" width="520px">
      <el-form label-width="100px">
        <el-form-item label="用户">
          <span>UID {{ form.uid }} · {{ form.nickname }}</span>
        </el-form-item>
        <el-form-item label="跑腿员">
          <el-switch v-model="form.isRunner" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="下单权限">
          <el-switch v-model="form.canOrder" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="接单权限">
          <el-switch v-model="form.canTake" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="信用分">
          <el-input-number v-model="form.creditScore" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="绑定校区">
          <el-select v-model="form.schoolId" placeholder="请选择" clearable style="width: 100%">
            <el-option v-for="s in schools" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="form.realname" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">女</el-radio>
            <el-radio :value="1">男</el-radio>
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
import { editUserWx, pageUserWx } from '../../api/user'
import { listSchools } from '../../api/school'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const schools = ref([])
const dialogVisible = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, uid: '', nickname: '', phone: '', schoolId: null })
const form = reactive({
  id: null,
  uid: null,
  nickname: '',
  isRunner: 0,
  canOrder: 1,
  canTake: 0,
  creditScore: 12,
  schoolId: null,
  realname: '',
  gender: 1
})

const wx = (row) => ({ ...row, ...(row.userWx || {}) })
const schoolName = (id) => schools.value.find((s) => s.id === id)?.name || (id ?? '-')

const load = async () => {
  loading.value = true
  try {
    const res = await pageUserWx({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      uid: query.uid || undefined,
      nickname: query.nickname || undefined,
      phone: query.phone || undefined,
      schoolId: query.schoolId || undefined
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
  const merged = wx(row)
  Object.assign(form, {
    id: row.userWx?.id || null,
    uid: merged.uid,
    nickname: merged.nickname,
    isRunner: merged.isRunner ?? 0,
    canOrder: merged.canOrder ?? 1,
    canTake: merged.canTake ?? 0,
    creditScore: merged.creditScore ?? 12,
    schoolId: merged.schoolId ?? null,
    realname: merged.realname || '',
    gender: merged.gender ?? 1
  })
  dialogVisible.value = true
}

const onSave = async () => {
  saving.value = true
  try {
    const { nickname, ...payload } = form
    await editUserWx(payload)
    ElMessage.success('保存成功')
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
