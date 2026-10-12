import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../layout/Index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '工作台' }
      },
      // 代购商城
      {
        path: 'mall/category',
        component: () => import('../views/goods/CategoryList.vue'),
        meta: { title: '商品分类' }
      },
      {
        path: 'mall/goods',
        component: () => import('../views/goods/GoodsList.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'mall/orders',
        component: () => import('../views/order/MallOrderList.vue'),
        meta: { title: '商城订单' }
      },
      // 订单管理
      {
        path: 'order/list',
        component: () => import('../views/order/OrderList.vue'),
        meta: { title: '订单列表' }
      },
      {
        path: 'order/detail/:id',
        component: () => import('../views/order/OrderDetail.vue'),
        meta: { title: '订单详情' }
      },
      {
        path: 'order/tag',
        component: () => import('../views/school/TagList.vue'),
        meta: { title: '标签管理' }
      },
      // 工单处理
      {
        path: 'workorder/runnerApply',
        component: () => import('../views/workorder/RunnerApply.vue'),
        meta: { title: '跑腿申请' }
      },
      {
        path: 'workorder/appeal',
        component: () => import('../views/workorder/OrderAppeal.vue'),
        meta: { title: '订单申诉' }
      },
      // 用户管理
      {
        path: 'user/pc',
        component: () => import('../views/user/UserPc.vue'),
        meta: { title: 'PC用户' }
      },
      {
        path: 'user/wx',
        component: () => import('../views/user/UserWx.vue'),
        meta: { title: '小程序用户' }
      },
      // 资金管理
      {
        path: 'capital/wallet',
        component: () => import('../views/capital/WalletList.vue'),
        meta: { title: '钱包管理' }
      },
      {
        path: 'capital/withdrawal',
        component: () => import('../views/capital/WithdrawalList.vue'),
        meta: { title: '提现审核' }
      },
      {
        path: 'capital/flow',
        component: () => import('../views/capital/CapitalFlow.vue'),
        meta: { title: '资金流动' }
      },
      {
        path: 'capital/mywallet',
        component: () => import('../views/capital/MyWallet.vue'),
        meta: { title: '我的钱包' }
      },
      {
        path: 'capital/myflow',
        component: () => import('../views/capital/MyFlow.vue'),
        meta: { title: '账户明细' }
      },
      // 校区管理
      {
        path: 'school/campus',
        component: () => import('../views/school/CampusList.vue'),
        meta: { title: '校区管理' }
      },
      {
        path: 'school/region',
        component: () => import('../views/school/RegionList.vue'),
        meta: { title: '区域管理' }
      },
      // 系统管理
      {
        path: 'system/config',
        component: () => import('../views/system/ConfigPage.vue'),
        meta: { title: '全局配置' }
      },
      {
        path: 'system/permission',
        component: () => import('../views/system/PermissionPage.vue'),
        meta: { title: '权限管理' }
      },
      {
        path: 'system/monitor',
        component: () => import('../views/system/MonitorPage.vue'),
        meta: { title: '系统监控' }
      },
      {
        path: 'system/carousel',
        component: () => import('../views/system/CarouselPage.vue'),
        meta: { title: '轮播图管理' }
      },
      // 存储管理
      {
        path: 'oss/manage',
        component: () => import('../views/oss/OssManage.vue'),
        meta: { title: 'OSS文件' }
      },
      {
        path: 'oss/config',
        component: () => import('../views/oss/OssConfigPage.vue'),
        meta: { title: 'OSS配置' }
      },
      // 个人中心
      {
        path: 'profile',
        component: () => import('../views/profile/ProfilePage.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 登录守卫
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    return '/login'
  }
  if (to.path === '/login' && token) {
    return '/dashboard'
  }
  return true
})

export default router
