<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-button type="success" @click="openDialog()">新增配置</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="ossConfigId" label="配置ID" width="90" />
      <el-table-column prop="configKey" label="配置名称" width="120" />
      <el-table-column label="AccessKey" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">{{ mask(row.accessKey) }}</template>
      </el-table-column>
      <el-table-column prop="bucketName" label="Bucket" min-width="140" show-overflow-tooltip />
      <el-table-column prop="endpoint" label="Endpoint" min-width="170" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch :model-value="String(row.status) === '1'" @change="(v) => toggleStatus(row, v)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="onRemove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.ossConfigId ? '编辑OSS配置' : '新增OSS配置'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="配置名称" prop="configKey">
          <el-input v-model="form.configKey" placeholder="如 minio / qiniu / aliyun" />
        </el-form-item>
        <el-form-item label="AccessKey">
          <el-input v-model="form.accessKey" />
        </el-form-item>
        <el-form-item label="SecretKey">
          <el-input v-model="form.secretKey" type="password" show-password />
        </el-form-item>
        <el-form-item label="Bucket">
          <el-input v-model="form.bucketName" />
        </el-form-item>
        <el-form-item label="Endpoint">
          <el-input v-model="form.endpoint" />
        </el-form-item>
        <el-form-item label="自定义域名">
          <el-input v-model="form.domain" />
        </el-form-item>
        <el-form-item label="路径前缀">
          <el-input v-model="form.prefix" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="是否HTTPS">
              <el-radio-group v-model="form.isHttps">
                <el-radio value="Y">是</el-radio>
                <el-radio value="N">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="区域">
              <el-input v-model="form.region" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="桶权限类型">
          <el-select v-model="form.accessPolicy" style="width: 100%">
            <el-option label="private" value="0" />
            <el-option label="public" value="1" />
            <el-option label="custom" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="1">启用</el-radio>
            <el-radio value="0">停用</el-radio>
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
import {
  addOssConfig,
  changeOssConfigStatus,
  editOssConfig,
  listOssConfig,
  removeOssConfig
} from '../../api/oss'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({
  ossConfigId: null,
  configKey: '',
  accessKey: '',
  secretKey: '',
  bucketName: '',
  prefix: '',
  endpoint: '',
  domain: '',
  isHttps: 'N',
  region: '',
  status: '1',
  accessPolicy: '0',
  remark: ''
})
const rules = {
  configKey: [{ required: true, message: '请输入配置名称', trigger: 'blur' }]
}

const mask = (v) => (v && v.length > 8 ? v.slice(0, 4) + '****' + v.slice(-4) : v || '-')

const load = async () => {
  loading.value = true
  try {
    const res = await listOssConfig()
    rows.value = res.rows || res.data || []
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  Object.assign(form, {
    ossConfigId: row ? row.ossConfigId : null,
    configKey: row ? row.configKey : '',
    accessKey: row ? row.accessKey : '',
    secretKey: row ? row.secretKey : '',
    bucketName: row ? row.bucketName : '',
    prefix: row ? row.prefix : '',
    endpoint: row ? row.endpoint : '',
    domain: row ? row.domain : '',
    isHttps: row ? row.isHttps : 'N',
    region: row ? row.region : '',
    status: row ? String(row.status) : '1',
    accessPolicy: row ? String(row.accessPolicy || '0') : '0',
    remark: row ? row.remark : ''
  })
  dialogVisible.value = true
}

const onSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.ossConfigId) {
      await editOssConfig({ ...form })
    } else {
      await addOssConfig({ ...form })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row, enabled) => {
  await changeOssConfigStatus({ ossConfigId: row.ossConfigId, status: enabled ? '1' : '0' })
  ElMessage.success(enabled ? '已启用' : '已停用')
  load()
}

const onRemove = async (row) => {
  await ElMessageBox.confirm(`确认删除配置「${row.configKey}」？`, '警告', { type: 'warning' })
  await removeOssConfig(row.ossConfigId)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
</style>
