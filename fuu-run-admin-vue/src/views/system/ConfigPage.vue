<template>
  <el-card shadow="never" v-loading="loading">
    <template #header>全局配置（保存后立即对小程序端与后端生效）</template>
    <el-form ref="formRef" :model="form" label-width="150px" style="max-width: 640px">
      <el-divider content-position="left">基本设置</el-divider>
      <el-form-item label="项目名称">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="版本号">
        <el-input v-model="form.version" />
      </el-form-item>
      <el-form-item label="版权年份">
        <el-input v-model="form.copyrightYear" />
      </el-form-item>

      <el-divider content-position="left">订单与超时设置</el-divider>
      <el-form-item label="未支付取消(分钟)">
        <el-input-number v-model="form.payCancelTtl" :min="1" />
      </el-form-item>
      <el-form-item label="未完成自动完成(小时)">
        <el-input-number v-model="form.autoCompleteTtl" :min="1" />
      </el-form-item>
      <el-form-item label="完成凭证上限(张)">
        <el-input-number v-model="form.completionImagesLimit" :min="1" :max="20" />
      </el-form-item>

      <el-divider content-position="left">用户与信用设置</el-divider>
      <el-form-item label="信用分上限">
        <el-input-number v-model="form.creditUpperLimit" :min="0" />
      </el-form-item>
      <el-form-item label="信用分下限">
        <el-input-number v-model="form.creditLowerLimit" :min="0" />
      </el-form-item>
      <el-form-item label="信用分每次扣除">
        <el-input-number v-model="form.creditDeduction" :min="1" />
      </el-form-item>
      <el-form-item label="用户地址上限">
        <el-input-number v-model="form.maxAddress" :min="1" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="saving" @click="onSave">保存配置</el-button>
        <el-button @click="reset">恢复当前值</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { editConfig } from '../../api/system'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const form = reactive({})

const KEYS = [
  'name',
  'version',
  'copyrightYear',
  'payCancelTtl',
  'autoCompleteTtl',
  'completionImagesLimit',
  'creditUpperLimit',
  'creditLowerLimit',
  'creditDeduction',
  'maxAddress'
]

const reset = () => {
  const cfg = userStore.config || {}
  KEYS.forEach((k) => {
    form[k] = cfg[k]
  })
}

const onSave = async () => {
  saving.value = true
  try {
    const payload = {}
    KEYS.forEach((k) => {
      payload[k] = form[k]
    })
    await editConfig(payload)
    ElMessage.success('保存成功')
    await userStore.fetchInfo()
    reset()
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    if (!userStore.config) {
      await userStore.fetchInfo()
    }
    reset()
  } finally {
    loading.value = false
  }
})
</script>
