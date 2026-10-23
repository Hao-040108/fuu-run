<template>
  <el-container style="height: 100%">
    <el-aside width="220px" class="aside">
      <div class="logo">校园互助 · 管理台</div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#1f2d3d"
        text-color="#c0c4cc"
        active-text-color="#409EFF"
        style="border-right: none; height: calc(100% - 60px); overflow-y: auto"
      >
        <el-menu-item index="/dashboard">工作台</el-menu-item>
        <el-menu-item index="/stat">数据统计</el-menu-item>
        <el-sub-menu index="order">
          <template #title>订单管理</template>
          <el-menu-item index="/order/list">订单列表</el-menu-item>
          <el-menu-item v-if="!isMediator" index="/order/tag">标签管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="workorder">
          <template #title>{{ isMediator ? '纠纷调解' : '工单处理' }}</template>
          <el-menu-item v-if="!isMediator" index="/workorder/runnerApply">跑腿申请</el-menu-item>
          <el-menu-item index="/workorder/appeal">{{ isMediator ? '调解工作台' : '纠纷调解' }}</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="!isMediator" index="user">
          <template #title>用户管理</template>
          <el-menu-item index="/user/pc">PC用户</el-menu-item>
          <el-menu-item index="/user/wx">小程序用户</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="!isMediator" index="capital">
          <template #title>资金管理</template>
          <el-menu-item index="/capital/wallet">钱包管理</el-menu-item>
          <el-menu-item index="/capital/withdrawal">提现审核</el-menu-item>
          <el-menu-item index="/capital/flow">资金流动</el-menu-item>
          <el-menu-item index="/capital/mywallet">我的钱包</el-menu-item>
          <el-menu-item index="/capital/myflow">账户明细</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="!isMediator" index="school">
          <template #title>校区管理</template>
          <el-menu-item index="/school/campus">校区管理</el-menu-item>
          <el-menu-item index="/school/region">区域管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="!isMediator" index="system">
          <template #title>系统管理</template>
          <el-menu-item index="/system/config">全局配置</el-menu-item>
          <el-menu-item index="/system/permission">权限管理</el-menu-item>
          <el-menu-item index="/system/monitor">系统监控</el-menu-item>
          <el-menu-item index="/system/carousel">轮播图管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="!isMediator" index="oss">
          <template #title>存储管理</template>
          <el-menu-item index="/oss/manage">OSS文件</el-menu-item>
          <el-menu-item index="/oss/config">OSS配置</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-title">{{ $route.meta.title || '' }}</div>
        <el-dropdown @command="onCommand">
          <span class="user">{{ username }}</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const username = computed(() => userStore.username || 'admin')
const isMediator = computed(() => userStore.user?.userType === 6)

const onCommand = (cmd) => {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.aside {
  background: #1f2d3d;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-weight: 600;
  font-size: 16px;
  background: #17222e;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
}
.user {
  cursor: pointer;
  color: #409eff;
}
</style>
