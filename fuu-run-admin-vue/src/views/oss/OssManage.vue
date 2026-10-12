<template>
  <el-card shadow="never">
    <el-form inline :model="query" @submit.prevent>
      <el-form-item label="文件名">
        <el-input v-model="query.originalName" placeholder="原始文件名" clearable style="width: 190px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="业务类型">
        <el-input v-model="query.service" placeholder="如 minio / qiniu" clearable style="width: 160px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-upload :show-file-list="false" :before-upload="onUpload">
          <el-button type="success">上传文件</el-button>
        </el-upload>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="ossId" label="ID" width="200" show-overflow-tooltip />
      <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
      <el-table-column prop="originalName" label="原始文件名" min-width="150" show-overflow-tooltip />
      <el-table-column prop="fileSuffix" label="后缀" width="80" />
      <el-table-column label="大小" width="100">
        <template #default="{ row }">{{ (Number(row.fileSize || 0) / 1024).toFixed(1) }} KB</template>
      </el-table-column>
      <el-table-column prop="service" label="存储服务" width="100" />
      <el-table-column prop="createTime" label="上传时间" width="165" />
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-link type="primary" :href="downloadUrl(row.ossId)" target="_blank">下载</el-link>
          <el-link type="success" style="margin-left: 10px" :href="row.url" target="_blank">访问</el-link>
          <el-button link type="danger" style="margin-left: 10px" @click="onRemove(row)">删除</el-button>
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
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageOss, removeOss, uploadOss, downloadOss } from '../../api/oss'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, originalName: '', service: '' })

const downloadUrl = (id) => downloadOss(id)

const load = async () => {
  loading.value = true
  try {
    const res = await pageOss({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      originalName: query.originalName || undefined,
      service: query.service || undefined
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

const onUpload = async (file) => {
  const fd = new FormData()
  fd.append('file', file)
  try {
    const res = await uploadOss(fd)
    ElMessage.success('上传成功')
    if (res.data) {
      ElMessage.info('文件地址：' + res.data)
    }
    load()
  } catch (e) {}
  return false
}

const onRemove = async (row) => {
  await ElMessageBox.confirm(`确认删除文件「${row.originalName || row.ossId}」？`, '警告', { type: 'warning' })
  await removeOss(row.ossId)
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
