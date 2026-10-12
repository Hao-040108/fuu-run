<template>
  <el-card shadow="never" v-loading="loading">
    <template #header>
      <div class="head">
        <span>订单详情：{{ orderId }}</span>
        <el-button size="small" @click="$router.back()">返回</el-button>
      </div>
    </template>

    <template v-if="detail.orderMain">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detail.orderMain.id }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="statusType(detail.orderMain.status)">{{ STATUS_MAP[detail.orderMain.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单来源">
          <el-tag :type="detail.orderMain.orderSource === 1 ? 'warning' : 'info'">
            {{ detail.orderMain.orderSource === 1 ? '商城代购' : '需求大厅' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="服务类型">{{ SERVICE_MAP[detail.orderMain.serviceType] }}</el-descriptions-item>
        <el-descriptions-item label="标签">{{ detail.orderMain.tag }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">￥{{ detail.orderMain.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="下单用户">
          {{ detail.orderMain.userId }}<span v-if="detail.nicknameUser">（{{ detail.nicknameUser }}）</span>
        </el-descriptions-item>
        <el-descriptions-item label="跑腿员">
          {{ detail.orderMain.runnerId || '-' }}<span v-if="detail.nicknameRunner">（{{ detail.nicknameRunner }}）</span>
        </el-descriptions-item>
        <el-descriptions-item label="起点">{{ addrText(detail.orderMain.startAddress) }}</el-descriptions-item>
        <el-descriptions-item label="终点">{{ addrText(detail.orderMain.endAddress) }}</el-descriptions-item>
        <el-descriptions-item label="订单内容" :span="2">{{ detail.orderMain.detail }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">
          {{ PAY_MAP[detail.orderPayment?.paymentStatus] || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="实付金额">
          ￥{{ detail.orderPayment?.actualPayment ?? '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">订单进度</el-divider>
      <el-steps :active="stepActive" finish-status="success" align-center>
        <el-step title="下单" :description="fmt(detail.orderMain.createTime)" />
        <el-step title="支付" :description="fmt(detail.orderPayment?.paymentTime)" />
        <el-step title="接单" :description="fmt(detail.progress?.acceptedTime)" />
        <el-step title="送达" :description="fmt(detail.progress?.deliveredTime)" />
        <el-step title="完成" :description="fmt(detail.progress?.completedTime)" />
      </el-steps>
      <el-alert
        v-if="detail.progress?.cancelTime"
        style="margin-top: 12px"
        type="warning"
        :closable="false"
        :title="'已于 ' + fmt(detail.progress.cancelTime) + ' 取消：' + (detail.progress.cancelReason || '无原因')"
      />

      <template v-if="allImages.length">
        <el-divider content-position="left">订单附件</el-divider>
        <div class="imgs">
          <el-image
            v-for="img in allImages"
            :key="img.id"
            :src="img.fileUrl"
            :preview-src-list="allImages.map((i) => i.fileUrl)"
            fit="cover"
            class="img"
          />
        </div>
      </template>

      <template v-if="appeals.length">
        <el-divider content-position="left">订单申诉</el-divider>
        <el-table :data="appeals" border size="small">
          <el-table-column prop="appealReason" label="申诉理由" min-width="200" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag>{{ APPEAL_MAP[row.appealStatus] || row.appealStatus }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remarks" label="处理备注" min-width="150" />
          <el-table-column prop="appealTime" label="申诉时间" width="165" />
        </el-table>
      </template>
    </template>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { orderDetail, getAppeal } from '../../api/order'

const STATUS_MAP = {
  0: '待支付',
  1: '待接单',
  2: '待配送',
  3: '配送中',
  4: '已送达',
  5: '已取消',
  10: '已完成',
  11: '已申诉'
}
const SERVICE_MAP = { 0: '帮取送', 1: '帮买/代购', 2: '万能帮' }
const PAY_MAP = { 0: '未支付', 1: '已支付', 2: '退款中', 3: '已退款' }
const APPEAL_MAP = { 0: '已驳回', 1: '已通过', 2: '处理中' }

const route = useRoute()
const orderId = route.params.id

const loading = ref(false)
const detail = ref({})
const appeals = ref([])

const statusType = (s) => {
  if (s === 0) return 'warning'
  if (s === 1) return 'primary'
  if (s === 10) return 'success'
  if (s === 5) return 'info'
  if (s === 11) return 'danger'
  return 'info'
}

const fmt = (t) => (t ? String(t).replace('T', ' ') : '')
const addrText = (a) => (a ? `${a.title || ''} ${a.detail || ''}（${a.name || ''} ${a.phone || ''}）` : '-')

const stepActive = computed(() => {
  const s = detail.value.orderMain?.status
  if (s === 0) return 1
  if (s === 1) return 2
  if (s === 2 || s === 3) return 3
  if (s === 4) return 4
  if (s === 10) return 5
  return 1
})

const allImages = computed(() => {
  const d = detail.value
  return [...(d.attachImages || []), ...(d.completionImages || [])]
})

onMounted(async () => {
  loading.value = true
  try {
    const res = await orderDetail(orderId)
    detail.value = res.data || {}
    try {
      const ap = await getAppeal(orderId)
      appeals.value = ap.data || []
    } catch (e) {}
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.imgs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.img {
  width: 120px;
  height: 120px;
  border-radius: 6px;
}
</style>
