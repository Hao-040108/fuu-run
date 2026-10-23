<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-label">今日任务</div>
          <div class="stat-value">{{ stats.todayTasks }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-label">待接单</div>
          <div class="stat-value">{{ stats.pendingAccept }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-label">待处理纠纷</div>
          <div class="stat-value">{{ stats.disputePending }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 16px">
      <template #header>快捷入口</template>
      <el-space wrap>
        <el-button type="primary" @click="$router.push('/order/list')">任务管理</el-button>
        <el-button @click="$router.push('/workorder/appeal')">纠纷调解</el-button>
        <el-button v-if="!isMediator" @click="$router.push('/workorder/runnerApply')">跑腿申请</el-button>
        <el-button @click="$router.push('/stat')">数据统计</el-button>
        <el-button link type="info" @click="openApiDoc">打开后端接口文档（Swagger）</el-button>
      </el-space>
      <el-divider />
      <div class="tip">
        本项目为校园互助任务众包平台：学生发布代拿、代购与其他服务任务，跑腿员接取，
        平台提供托管支付、凭证与纠纷调解。管理端使用 Vue3 + Element Plus 重写；
        开发环境已开启模拟支付，可在订单详情或接口 /dev/mockPay/{orderId} 完成支付闭环。
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, reactive, onMounted } from 'vue'
import { statSummary } from '../api/stat'
import { useUserStore } from '../store/user'

const stats = reactive({ todayTasks: '-', pendingAccept: '-', disputePending: '-' })
const userStore = useUserStore()
const isMediator = computed(() => userStore.user?.userType === 6)

const openApiDoc = () => window.open('http://localhost:8081/swagger-ui/index.html', '_blank')

onMounted(async () => {
  try {
    const res = await statSummary()
    Object.assign(stats, res.data || {})
  } catch (e) {
    // 接口异常时保留占位符
  }
})
</script>

<style scoped>
.stat-label {
  color: #909399;
  font-size: 13px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  margin-top: 8px;
  color: #303133;
}
.tip {
  color: #909399;
  font-size: 13px;
  line-height: 1.8;
}
</style>
