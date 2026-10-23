<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="4" v-for="card in cards" :key="card.label">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-sub">{{ card.sub }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>任务量 / 交易额趋势（statistics_daily）</span>
              <el-space>
                <el-radio-group v-model="days" size="small" @change="loadCharts">
                  <el-radio-button :value="7">近7天</el-radio-button>
                  <el-radio-button :value="14">近14天</el-radio-button>
                  <el-radio-button :value="30">近30天</el-radio-button>
                </el-radio-group>
                <el-button type="primary" size="small" :loading="generating" @click="onGenerate">
                  生成/补算统计
                </el-button>
                <el-button size="small" @click="loadAll">刷新</el-button>
              </el-space>
            </div>
          </template>
          <div v-if="charts.daily && charts.daily.length" ref="trendRef" class="chart" style="height: 320px"></div>
          <el-empty
            v-else
            description="暂无日统计数据：点击右上角「生成/补算统计」从业务表补算最近 30 天"
            :image-size="90"
          />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>任务类型分布（实时）</template>
          <div ref="typeRef" class="chart" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 16px">
      <template #header>跑腿员接单榜 Top8（已完成任务数）</template>
      <div ref="runnerRef" class="chart" style="height: 260px"></div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { statSummary, statCharts, generateStat } from '../../api/stat'

const days = ref(14)
const generating = ref(false)

const summary = reactive({
  todayTasks: '-',
  todayAmount: '-',
  totalTasks: '-',
  totalAmount: '-',
  completedTasks: 0,
  completionRate: '-',
  pendingAccept: 0,
  disputePending: 0,
  runnerCount: 0
})
const charts = reactive({ daily: [], typeSplit: [], topRunners: [] })

const cards = computed(() => [
  { label: '今日任务量', value: summary.todayTasks, sub: '单' },
  { label: '今日交易额', value: '￥' + summary.todayAmount, sub: '按任务金额统计' },
  { label: '累计任务量', value: summary.totalTasks, sub: `已完成 ${summary.completedTasks} 单` },
  { label: '累计交易额', value: '￥' + summary.totalAmount, sub: '全部任务合计' },
  { label: '完成率', value: (summary.completionRate ?? '-') + '%', sub: `待接单 ${summary.pendingAccept}` },
  { label: '待处理纠纷', value: summary.disputePending, sub: `跑腿员 ${summary.runnerCount} 人` }
])

const trendRef = ref(null)
const typeRef = ref(null)
const runnerRef = ref(null)
let trendChart = null
let typeChart = null
let runnerChart = null

const renderTrend = () => {
  if (!charts.daily || !charts.daily.length || !trendRef.value) return
  trendChart = trendChart || echarts.init(trendRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 4, data: ['任务量', '完成任务', '交易额'] },
    grid: { left: 45, right: 55, top: 52, bottom: 34 },
    xAxis: { type: 'category', data: charts.daily.map((i) => i.date) },
    yAxis: [
      { type: 'value', name: '单', minInterval: 1 },
      { type: 'value', name: '元' }
    ],
    series: [
      {
        name: '任务量',
        type: 'bar',
        barWidth: 14,
        itemStyle: { color: '#409EFF' },
        data: charts.daily.map((i) => i.totalTasks)
      },
      {
        name: '完成任务',
        type: 'bar',
        barWidth: 14,
        itemStyle: { color: '#67C23A' },
        data: charts.daily.map((i) => i.completedTasks)
      },
      {
        name: '交易额',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        itemStyle: { color: '#E6A23C' },
        data: charts.daily.map((i) => i.totalPayment)
      }
    ]
  })
  trendChart.resize()
}

const renderType = () => {
  if (!typeRef.value) return
  typeChart = typeChart || echarts.init(typeRef.value)
  typeChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 单（{d}%）' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '45%'],
        label: { show: true, position: 'inside', formatter: '{d}%', color: '#fff', fontSize: 12 },
        labelLine: { show: false },
        data: charts.typeSplit
      }
    ]
  })
  typeChart.resize()
}

const renderRunners = () => {
  if (!runnerRef.value) return
  runnerChart = runnerChart || echarts.init(runnerRef.value)
  const list = [...(charts.topRunners || [])].reverse()
  runnerChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 140, right: 45, top: 20, bottom: 20 },
    xAxis: { type: 'value', name: '完成单数' },
    yAxis: { type: 'category', data: list.map((i) => i.name) },
    series: [
      {
        type: 'bar',
        barWidth: 12,
        itemStyle: { color: '#67C23A', borderRadius: [0, 4, 4, 0] },
        label: { show: true, position: 'right' },
        data: list.map((i) => i.value)
      }
    ]
  })
  runnerChart.resize()
}

const loadSummary = async () => {
  const res = await statSummary()
  Object.assign(summary, res.data || {})
}

const loadCharts = async () => {
  const res = await statCharts({ days: days.value })
  Object.assign(charts, res.data || {})
  await nextTick()
  renderTrend()
  renderType()
  renderRunners()
}

const loadAll = () => {
  loadSummary().catch(() => {})
  loadCharts().catch(() => {})
}

const onGenerate = async () => {
  generating.value = true
  try {
    const res = await generateStat({ days: 30 })
    ElMessage.success(res.msg || res.data || '统计生成完成')
    if (days.value < 30) {
      days.value = 30
    }
    loadAll()
  } finally {
    generating.value = false
  }
}

const onResize = () => {
  ;[trendChart, typeChart, runnerChart].forEach((c) => c && c.resize())
}

onMounted(() => {
  loadAll()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  ;[trendChart, typeChart, runnerChart].forEach((c) => c && c.dispose())
})
</script>

<style scoped>
.stat-card {
  text-align: left;
}
.stat-label {
  color: #909399;
  font-size: 13px;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  margin-top: 6px;
  color: #303133;
}
.stat-sub {
  color: #c0c4cc;
  font-size: 12px;
  margin-top: 4px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
