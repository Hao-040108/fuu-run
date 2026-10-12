# fuu-run-admin-vue · 校园代购平台管理后台（Vue3 版）

> fuu-run 二创项目的管理端，使用 **Vue 3 + Vite + Element Plus + Pinia** 重写。原 React/Umi 版 `fuu-run-admin` 的功能**已全量迁移至本项目并移除**（见 06 文档）。

## 功能范围（全量，覆盖原 React 版 21 个功能页）

| 模块 | 功能 |
|---|---|
| 登录 | 账号密码登录（Sa-Token JWT），token 持久化 |
| 工作台 | 商品/分类/订单数据概览 + 快捷入口 |
| 代购商城 | 商品分类、商品管理、商城订单（二创新增模块） |
| 订单管理 | 订单列表（来源/状态筛选、取消）、订单详情（进度时间轴、附件、申诉） |
| 标签管理 | 标签增删改查（按校区/服务类型） |
| 工单处理 | 跑腿申请审核、订单申诉处理 |
| 用户管理 | PC 用户（增删改、重置密码）、小程序用户（权限开关、信用分、绑定校区） |
| 资金管理 | 钱包列表、提现审核、资金流动、我的钱包（提现申请）、账户明细 |
| 校区管理 | 校区增改、区域/楼栋两级管理 |
| 系统管理 | 全局配置、权限管理（角色-权限树）、系统监控（CPU/内存/JVM/磁盘）、轮播图管理 |
| 存储管理 | OSS 文件（上传/下载/删除）、OSS 配置（增删改、启停） |
| 个人中心 | 资料与头像、修改密码、绑定邮箱 |

## 启动

```powershell
cd C:\Users\12487\Desktop\bishe\fuu-run\fuu-run-admin-vue
npm install        # 首次执行（已配置 npmmirror 镜像）
npm run dev        # http://localhost:5173
```

前置条件：
1. 后端已启动（`http://localhost:8081`，见项目根目录 `start-dev-env.ps1`）
2. 已执行数据库脚本 `sql/v1_campus_proxy.sql` 与 `sql/v1_dev_fixtures.sql`

登录账号：`admin / admin123`（超级管理员）

## 目录结构

```
src/
├── api/          接口封装（request.js 统一拦截 token 与错误）
├── router/       路由与登录守卫
├── store/        Pinia 状态（用户 token）
├── layout/       侧边栏 + 顶栏布局
└── views/
    ├── Login.vue            登录页
    ├── Dashboard.vue        工作台
    ├── goods/               商品分类 / 商品管理
    └── order/               商城订单
```

## 与后端接口对照

所有接口经 Vite 代理 `/api` → `http://localhost:8081`（见 `vite.config.js`）：

| 前端调用 | 后端接口 | 说明 |
|---|---|---|
| POST /api/login | POST /login | 登录，返回 token |
| GET /api/mall/admin/category/list | 分类列表（含停用） |
| POST /api/mall/admin/category/{add,edit,remove} | 分类增删改 |
| GET /api/mall/admin/goods/list | 商品分页（管理端） |
| POST /api/mall/admin/goods/{add,edit,remove,status} | 商品增删改/上下架 |
| GET /api/mall/admin/order/list | 商城订单分页 |
| GET /api/mall/admin/order/detail/{orderId} | 订单 + 商品明细 |
