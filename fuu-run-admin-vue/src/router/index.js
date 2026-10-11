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
      {
        path: 'goods/category',
        component: () => import('../views/goods/CategoryList.vue'),
        meta: { title: '商品分类' }
      },
      {
        path: 'goods/list',
        component: () => import('../views/goods/GoodsList.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'order/mall',
        component: () => import('../views/order/MallOrderList.vue'),
        meta: { title: '商城订单' }
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
