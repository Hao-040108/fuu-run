<template>
  <div v-loading="loading">
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>CPU</template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="核心数">{{ server.cpu?.cpuNum ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="总使用率">{{ cpuUsage }}%</el-descriptions-item>
            <el-descriptions-item label="用户使用率">{{ server.cpu?.used ?? '-' }}%</el-descriptions-item>
            <el-descriptions-item label="系统使用率">{{ server.cpu?.sys ?? '-' }}%</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>内存</template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="总内存">{{ server.mem?.total ?? '-' }} M</el-descriptions-item>
            <el-descriptions-item label="已用">{{ server.mem?.used ?? '-' }} M</el-descriptions-item>
            <el-descriptions-item label="剩余">{{ server.mem?.free ?? '-' }} M</el-descriptions-item>
            <el-descriptions-item label="使用率">{{ server.mem?.usage ?? '-' }}%</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>JVM</template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="版本">{{ server.jvm?.version ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="运行时长">{{ server.jvm?.runTime ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="总内存">{{ server.jvm?.total ?? '-' }} M</el-descriptions-item>
            <el-descriptions-item label="最大可用">{{ server.jvm?.max ?? '-' }} M</el-descriptions-item>
            <el-descriptions-item label="已用">{{ server.jvm?.used ?? '-' }} M</el-descriptions-item>
            <el-descriptions-item label="使用率">{{ server.jvm?.usage ?? '-' }}%</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>服务器信息</template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="计算机名称">{{ server.sys?.computerName ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="IP">{{ server.sys?.computerIp ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="操作系统">{{ server.sys?.osName ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="系统架构">{{ server.sys?.osArch ?? '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 16px">
      <template #header>磁盘状态</template>
      <el-table :data="server.sysFiles || []" border>
        <el-table-column prop="dirName" label="磁盘名称" min-width="120" />
        <el-table-column prop="sysTypeName" label="文件系统" width="110" />
        <el-table-column prop="typeName" label="类型" width="90" />
        <el-table-column label="总容量" width="110">
          <template #default="{ row }">{{ row.total }}</template>
        </el-table-column>
        <el-table-column label="已用" width="110">
          <template #default="{ row }">{{ row.used }}</template>
        </el-table-column>
        <el-table-column label="剩余" width="110">
          <template #default="{ row }">{{ row.free }}</template>
        </el-table-column>
        <el-table-column label="使用率" min-width="180">
          <template #default="{ row }">
            <el-progress :percentage="Number(row.usage || 0)" :stroke-width="12" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <div class="refreshBar">
      <el-button type="primary" @click="load">刷新</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMonitor } from '../../api/system'

const loading = ref(false)
const server = ref({})

const cpuUsage = computed(() => {
  const used = Number(server.value.cpu?.used || 0)
  const sys = Number(server.value.cpu?.sys || 0)
  return (used + sys).toFixed(1)
})

const load = async () => {
  loading.value = true
  try {
    const res = await getMonitor()
    server.value = res.data || {}
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.refreshBar {
  margin-top: 16px;
  text-align: right;
}
</style>
