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
              <span>订单量 / 交易额趋势（statistics_daily）</span>
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
          <template #header>订单来源分布（实时）</template>
          <div ref="sourceRef" class="chart" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>商品分类销量</template>
          <div ref="categoryRef" class="chart" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>热销商品 Top8（按累计销量）</template>
          <div ref="topRef" class="chart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { statSummary, statCharts, generateStat } from '../../api/mall'

const days = ref(14)
const generating = ref(false)

const summary = reactive({
  todayOrders: '-',
  todayAmount: '-',
  totalOrders: '-',
  totalAmount: '-',
  mallOrders: 0,
  hallOrders: 0,
  completedOrders: 0,
  completionRate: '-'
})
const charts = reactive({ daily: [], sourceSplit: [], categorySales: [], topGoods: [] })

const cards = computed(() => {
  const total = Number(summary.totalOrders) || 0
  const mallRate = total > 0 ? ((summary.mallOrders * 100) / total).toFixed(1) + '%' : '-'
  return [
    { label: '今日订单量', value: summary.todayOrders, sub: '单' },
    { label: '今日交易额', value: '￥' + summary.todayAmount, sub: '按订单金额统计' },
    { label: '累计订单量', value: summary.totalOrders, sub: `已完成 ${summary.completedOrders} 单` },
    { label: '累计交易额', value: '￥' + summary.totalAmount, sub: '全部订单合计' },
    { label: '商城单占比', value: mallRate, sub: `商城 ${summary.mallOrders} / 大厅 ${summary.hallOrders}` },
    { label: '完单率', value: (summary.completionRate ?? '-') + '%', sub: '已完成 / 全部订单' }
  ]
})

const trendRef = ref(null)
const sourceRef = ref(null)
const categoryRef = ref(null)
const topRef = ref(null)
let trendChart = null
let sourceChart = null
let categoryChart = null
let topChart = null

const renderTrend = () => {
  if (!charts.daily || !charts.daily.length || !trendRef.value) return
  trendChart = trendChart || echarts.init(trendRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 4, data: ['订单量', '完成订单', '交易额'] },
    grid: { left: 45, right: 55, top: 52, bottom: 34 },
    xAxis: { type: 'category', data: charts.daily.map((i) => i.date) },
    yAxis: [
      { type: 'value', name: '单', minInterval: 1 },
      { type: 'value', name: '元' }
    ],
    series: [
      {
        name: '订单量',
        type: 'bar',
        barWidth: 14,
        itemStyle: { color: '#409EFF' },
        data: charts.daily.map((i) => i.totalOrders)
      },
      {
        name: '完成订单',
        type: 'bar',
        barWidth: 14,
        itemStyle: { color: '#67C23A' },
        data: charts.daily.map((i) => i.completedOrders)
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

const renderSource = () => {
  if (!sourceRef.value) return
  sourceChart = sourceChart || echarts.init(sourceRef.value)
  sourceChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 单（{d}%）' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '45%'],
        label: { show: true, position: 'inside', formatter: '{d}%', color: '#fff', fontSize: 12 },
        labelLine: { show: false },
        data: charts.sourceSplit
      }
    ]
  })
  sourceChart.resize()
}

const renderCategory = () => {
  if (!categoryRef.value) return
  categoryChart = categoryChart || echarts.init(categoryRef.value)
  categoryChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 45, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: charts.categorySales.map((i) => i.name) },
    yAxis: { type: 'value', name: '销量' },
    series: [
      {
        type: 'bar',
        barWidth: 30,
        itemStyle: { color: '#409EFF', borderRadius: [4, 4, 0, 0] },
        label: { show: true, position: 'top' },
        data: charts.categorySales.map((i) => i.value)
      }
    ]
  })
  categoryChart.resize()
}

const renderTop = () => {
  if (!topRef.value) return
  topChart = topChart || echarts.init(topRef.value)
  const list = [...(charts.topGoods || [])].reverse()
  topChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 140, right: 45, top: 20, bottom: 20 },
    xAxis: { type: 'value', name: '销量' },
    yAxis: { type: 'category', data: list.map((i) => i.name) },
    series: [
      {
        type: 'bar',
        barWidth: 12,
        itemStyle: { color: '#67C23A', borderRadius: [0, 4, 4, 0] },
        label: { show: true, position: 'right' },
        data: list.map((i) => i.sales)
      }
    ]
  })
  topChart.resize()
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
  renderSource()
  renderCategory()
  renderTop()
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
  ;[trendChart, sourceChart, categoryChart, topChart].forEach((c) => c && c.resize())
}

onMounted(() => {
  loadAll()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  ;[trendChart, sourceChart, categoryChart, topChart].forEach((c) => c && c.dispose())
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
