<template>
  <el-container style="height: 100%">
    <el-aside width="220px" class="aside">
      <div class="logo">校园代购 · 管理台</div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#1f2d3d"
        text-color="#c0c4cc"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">工作台</el-menu-item>
        <el-sub-menu index="goods">
          <template #title>代购商城</template>
          <el-menu-item index="/goods/category">商品分类</el-menu-item>
          <el-menu-item index="/goods/list">商品管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/order/mall">商城订单</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-title">{{ $route.meta.title || '' }}</div>
        <el-dropdown @command="onCommand">
          <span class="user">{{ username }}</span>
          <template #dropdown>
            <el-dropdown-menu>
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

const onCommand = (cmd) => {
  if (cmd === 'logout') {
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
