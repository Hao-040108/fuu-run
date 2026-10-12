<template>
  <el-row :gutter="16">
    <el-col :span="12">
      <el-card shadow="never">
        <template #header>账号信息</template>
        <div class="avatarRow">
          <el-avatar :size="64" :src="userPc.avatar" />
          <el-upload :show-file-list="false" accept="image/*" :before-upload="onUploadAvatar">
            <el-button size="small">更换头像</el-button>
          </el-upload>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="Uid">{{ user.uid }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ userPc.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ userPc.name }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ userPc.phone }}</el-descriptions-item>
          <el-descriptions-item label="用户类型">
            {{ USER_TYPE_MAP[user.userType] || user.userType }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ userPc.email || '未绑定' }}
            <el-tag v-if="userPc.emailEnable === 1" type="success" size="small">已开启通知</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="最后登录">{{ user.loginTime }}</el-descriptions-item>
          <el-descriptions-item label="登录IP">{{ user.loginIp }}</el-descriptions-item>
          <el-descriptions-item label="登录地区">{{ user.loginRegion }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </el-col>

    <el-col :span="12">
      <el-card shadow="never">
        <template #header>修改密码</template>
        <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwdForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="savingPwd" @click="onUpdatePwd">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card shadow="never" style="margin-top: 16px">
        <template #header>绑定邮箱（用于接收工单通知）</template>
        <el-form ref="mailRef" :model="mailForm" :rules="mailRules" label-width="100px">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="mailForm.email" placeholder="邮箱地址" />
          </el-form-item>
          <el-form-item label="验证码" prop="code">
            <div class="codeRow">
              <el-input v-model="mailForm.code" placeholder="邮箱验证码" />
              <el-button :disabled="countdown > 0" @click="onSendCode">
                {{ countdown > 0 ? countdown + 's' : '发送验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="savingMail" @click="onBindEmail">绑定邮箱</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { bindEmail, sendEmailCode, updatePwd, updateProfile } from '../../api/profile'
import { uploadOss } from '../../api/oss'
import { useUserStore } from '../../store/user'

const USER_TYPE_MAP = { 0: '超级管理员', 1: '校区代理', 2: '普通管理员', 3: '普通用户', 4: '跑腿用户' }

const userStore = useUserStore()
const user = computed(() => userStore.user || {})
const userPc = computed(() => userStore.user?.userPc || {})

const pwdRef = ref()
const savingPwd = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const mailRef = ref()
const savingMail = ref(false)
const countdown = ref(0)
const mailForm = reactive({ email: '', code: '' })
const mailRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const onUpdatePwd = async () => {
  await pwdRef.value.validate()
  savingPwd.value = true
  try {
    await updatePwd({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
  } finally {
    savingPwd.value = false
  }
}

const onSendCode = async () => {
  await mailRef.value.validateField('email')
  await sendEmailCode(mailForm.email)
  ElMessage.success('验证码已发送，请查收邮箱')
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

const onBindEmail = async () => {
  await mailRef.value.validate()
  savingMail.value = true
  try {
    await bindEmail({ email: mailForm.email, code: mailForm.code })
    ElMessage.success('邮箱绑定成功')
    await userStore.fetchInfo()
  } finally {
    savingMail.value = false
  }
}

const onUploadAvatar = async (file) => {
  const fd = new FormData()
  fd.append('file', file)
  try {
    const res = await uploadOss(fd)
    const url = res.data
    if (url) {
      await updateProfile({ avatar: url, emailEnable: userPc.value.emailEnable ?? 0 })
      ElMessage.success('头像已更新')
      await userStore.fetchInfo()
    }
  } catch (e) {}
  return false
}

onMounted(async () => {
  if (!userStore.user) {
    await userStore.fetchInfo()
  }
  mailForm.email = userStore.user?.userPc?.email || ''
})
</script>

<style scoped>
.avatarRow {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
}
.codeRow {
  display: flex;
  gap: 10px;
  width: 100%;
}
</style>
