<template>
  <el-card shadow="never">
    <el-alert
      type="warning"
      :closable="false"
      style="margin-bottom: 14px"
      title="轮播图保存在服务端内存中，重启后仅保留默认图片；上传的图片将以 base64 形式提交。"
    />
    <div class="toolbar">
      <el-upload :show-file-list="false" accept="image/*" :before-upload="onUpload">
        <el-button type="success">新增轮播图</el-button>
      </el-upload>
    </div>

    <el-row :gutter="16">
      <el-col v-for="(img, index) in images" :key="index" :span="8">
        <el-card shadow="hover" class="imgCard">
          <el-image :src="img" fit="cover" class="img" :preview-src-list="images" :initial-index="index" />
          <div class="imgFoot">
            <span>第 {{ index + 1 }} 张</span>
            <span>
              <el-upload :show-file-list="false" accept="image/*" :before-upload="(file) => onReplace(file, index)">
                <el-button link type="primary">替换</el-button>
              </el-upload>
              <el-button link type="danger" @click="onDelete(index)">删除</el-button>
            </span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && images.length === 0" description="暂无轮播图" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addCarousel, deleteCarousel, getCarousel, updateCarousel } from '../../api/system'

const loading = ref(false)
const images = ref([])

const readBase64 = (file) =>
  new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const result = String(reader.result || '')
      resolve(result.split(',')[1] || '')
    }
    reader.onerror = reject
    reader.readAsDataURL(file)
  })

const load = async () => {
  loading.value = true
  try {
    const res = await getCarousel()
    images.value = res.data || []
  } finally {
    loading.value = false
  }
}

const onUpload = async (file) => {
  const base64Content = await readBase64(file)
  await addCarousel({ base64Content })
  ElMessage.success('已新增')
  load()
  return false
}

const onReplace = async (file, index) => {
  const base64Content = await readBase64(file)
  await updateCarousel(index, { base64Content })
  ElMessage.success('已替换')
  load()
  return false
}

const onDelete = async (index) => {
  await ElMessageBox.confirm(`确认删除第 ${index + 1} 张轮播图？`, '提示', { type: 'warning' })
  await deleteCarousel(index)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 14px;
}
.imgCard {
  margin-bottom: 16px;
}
.img {
  width: 100%;
  height: 150px;
  border-radius: 6px;
}
.imgFoot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}
</style>
