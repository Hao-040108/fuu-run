<template>
  <el-row :gutter="16">
    <el-col :span="12">
      <el-card shadow="never">
        <template #header>我的钱包</template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="当前余额">
            <span class="green">￥{{ Number(wallet.balance || 0).toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="已提现">￥{{ Number(wallet.withdrawn || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="钱包UID">{{ wallet.uid }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" style="margin-top: 16px">
        <template #header>最近提现记录</template>
        <el-empty v-if="!last" description="暂无提现记录" :image-size="80" />
        <el-descriptions v-else :column="1" border>
          <el-descriptions-item label="金额">￥{{ Number(last.cash || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(last.status)">{{ STATUS_MAP[last.status] || last.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="平台">{{ last.platform }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ last.createTime }}</el-descriptions-item>
          <el-descriptions-item v-if="last.feedback" label="反馈">{{ last.feedback }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </el-col>

    <el-col :span="12">
      <el-card shadow="never">
        <template #header>发起提现</template>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="提现金额" prop="cash">
            <el-input-number v-model="form.cash" :min="0.01" :precision="2" :step="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="提现平台" prop="platform">
            <el-select v-model="form.platform" placeholder="请选择" style="width: 100%">
              <el-option label="微信零钱" value="微信" />
              <el-option label="支付宝" value="支付宝" />
              <el-option label="银行卡" value="银行卡" />
            </el-select>
          </el-form-item>
          <el-form-item label="卡号/账号" prop="card">
            <el-input v-model="form.card" placeholder="收款账号" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="onSubmit">提交申请</el-button>
          </el-form-item>
        </el-form>
        <el-alert
          type="info"
          :closable="false"
          title="提现申请提交后由管理员在「提现审核」中处理；开发环境未接入真实商户，实际打款不会执行。"
        />
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { lastRecode, myWallet, submitWithdraw } from '../../api/capital'

const STATUS_MAP = { 0: '已驳回', 1: '已通过', 2: '审核中' }
const statusType = (s) => (s === 1 ? 'success' : s === 0 ? 'danger' : 'warning')

const wallet = ref({})
const last = ref(null)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ cash: 1, platform: '微信', card: '' })
const rules = {
  cash: [{ required: true, message: '请输入提现金额', trigger: 'blur' }],
  platform: [{ required: true, message: '请选择提现平台', trigger: 'change' }],
  card: [{ required: true, message: '请输入收款账号', trigger: 'blur' }]
}

const load = async () => {
  try {
    const res = await myWallet()
    wallet.value = res.data || {}
  } catch (e) {}
  try {
    const res = await lastRecode()
    last.value = res.data || null
  } catch (e) {}
}

const onSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await submitWithdraw({ ...form })
    ElMessage.success('提现申请已提交')
    load()
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.green {
  color: #52c41a;
  font-weight: 600;
  font-size: 18px;
}
</style>
