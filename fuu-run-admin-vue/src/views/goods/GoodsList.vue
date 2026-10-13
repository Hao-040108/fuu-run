<template>
  <el-card shadow="never">
    <el-form inline :model="query" @submit.prevent>
      <el-form-item label="关键字">
        <el-input v-model="query.keyword" placeholder="商品名称" clearable style="width: 180px" @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="query.categoryId" placeholder="全部" clearable style="width: 160px">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
        <el-button type="success" @click="openDialog()">新增商品</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
      <el-table-column label="分类" width="120">
        <template #default="{ row }">{{ categoryName(row.categoryId) }}</template>
      </el-table-column>
      <el-table-column prop="price" label="价格(元)" width="100" />
      <el-table-column prop="purchaseFee" label="代购费(元)" width="110" />
      <el-table-column prop="stock" label="库存" width="90" />
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            @change="(v) => toggleStatus(row, v)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="onRemove(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="如：康师傅冰红茶 500ml" />
        </el-form-item>
        <el-form-item label="所属分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="价格(元)" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="0.5" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="代购费(元)">
              <el-input-number v-model="form.purchaseFee" :min="0" :precision="2" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="规格/单位">
              <el-input v-model="form.unit" placeholder="如：瓶 / 袋" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存">
              <el-input-number v-model="form.stock" :min="-1" style="width: 100%" />
              <div class="hint">-1 表示不限量</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="图片地址">
          <div style="width: 100%">
            <el-space alignment="center">
              <el-image
                v-if="form.image"
                :src="form.image"
                fit="cover"
                style="width: 64px; height: 64px; border-radius: 6px"
                :preview-src-list="[form.image]"
                preview-teleported
              />
              <el-upload :show-file-list="false" :before-upload="onUploadImage" accept="image/*">
                <el-button :loading="uploading">上传图片</el-button>
              </el-upload>
              <el-button v-if="form.image" link type="danger" @click="form.image = ''">清除</el-button>
            </el-space>
            <el-input
              v-model="form.image"
              placeholder="图片 URL（可点击左侧上传，或手动粘贴地址；留空使用占位图）"
              style="margin-top: 8px"
            />
          </div>
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
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
  addGoods,
  changeGoodsStatus,
  editGoods,
  listCategory,
  pageGoods,
  removeGoods,
  uploadGoodsImage
} from '../../api/mall'

const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const rows = ref([])
const total = ref(0)
const categories = ref([])
const dialogVisible = ref(false)
const formRef = ref()

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', categoryId: null, status: null })
const form = reactive({
  id: null,
  categoryId: null,
  name: '',
  image: '',
  price: 1,
  unit: '',
  purchaseFee: 0,
  stock: -1,
  description: '',
  status: 1,
  sort: 0
})
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const categoryName = (id) => categories.value.find((c) => c.id === id)?.name || id

const load = async () => {
  loading.value = true
  try {
    const res = await pageGoods({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      categoryId: query.categoryId || undefined,
      status: query.status === null ? undefined : query.status
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

const reset = () => {
  query.keyword = ''
  query.categoryId = null
  query.status = null
  search()
}

const openDialog = (row) => {
  Object.assign(form, {
    id: row ? row.id : null,
    categoryId: row ? row.categoryId : null,
    name: row ? row.name : '',
    image: row ? row.image : '',
    price: row ? row.price : 1,
    unit: row ? row.unit : '',
    purchaseFee: row ? row.purchaseFee : 0,
    stock: row ? row.stock : -1,
    description: row ? row.description : '',
    status: row ? row.status : 1,
    sort: row ? row.sort : 0
  })
  dialogVisible.value = true
}

// 图片上传（开发演示：本地文件存储 /mall/admin/upload/image，返回可直接访问的 URL）
const onUploadImage = async (file) => {
  const fd = new FormData()
  fd.append('file', file)
  uploading.value = true
  try {
    const res = await uploadGoodsImage(fd)
    form.image = res.data || ''
    ElMessage.success('图片上传成功')
  } finally {
    uploading.value = false
  }
  return false
}

const onSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await editGoods({ ...form })
    } else {
      await addGoods({ ...form })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const onRemove = async (row) => {
  await ElMessageBox.confirm(`确认删除商品「${row.name}」？`, '提示', { type: 'warning' })
  await removeGoods(row.id)
  ElMessage.success('删除成功')
  await load()
}

const toggleStatus = async (row, enabled) => {
  const status = enabled ? 1 : 0
  await changeGoodsStatus(row.id, status)
  row.status = status
  ElMessage.success(enabled ? '已上架' : '已下架')
}

onMounted(async () => {
  const cats = await listCategory()
  categories.value = cats.data || []
  await load()
})
</script>

<style scoped>
.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
.hint {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}
</style>
