# fuu-run-admin-vue · 校园互助任务众包平台管理后台

> 基于 **Vue 3 + Vite + Element Plus + Pinia** 的管理端。原 React/Umi 版 `fuu-run-admin` 的功能已全量迁移到本项目并移除。
> 业务已从"代购商城 + 需求大厅"重构为统一任务域：**代拿 / 代购 / 其他服务**。

## 功能范围

| 模块 | 功能 |
|---|---|
| 登录 | 账号密码登录（Sa-Token JWT），token 持久化 |
| 工作台 | 今日任务、待接单、待处理纠纷概览 + 快捷入口 |
| 任务管理 | 任务列表（任务类型/状态筛选、取消）、任务详情（进度、附件、申诉） |
| 纠纷调解 | 调解工作台：查看纠纷、订单证据、裁决退款/驳回（调解员可见） |
| 跑腿申请 | 跑腿员资格审核 |
| 用户管理 | PC 用户与小程序用户管理（权限开关、信用分、绑定校区） |
| 资金管理 | 钱包、提现审核、资金流动、我的钱包与账户明细 |
| 校区管理 | 校区、区域/楼栋管理 |
| 系统管理 | 全局配置、权限管理、系统监控、轮播图 |
| 存储管理 | OSS 文件与配置 |
| 数据统计 | 任务量/交易额趋势、任务类型分布、跑腿员接单榜、统计补算 |
| 个人中心 | 资料、头像、修改密码、绑定邮箱 |

## 启动

```powershell
cd C:\Users\12487\Desktop\bishe\fuu-run\fuu-run-admin-vue
npm install
npm run dev        # http://localhost:5173
```

前置条件：
1. 后端已启动（`http://localhost:8081`）
2. 数据库脚本按顺序执行：`sql/v1_campus_proxy.sql` → `sql/v2_task_platform.sql` → `sql/v1_dev_fixtures.sql`

测试账号：

| 角色 | 账号 | 密码 |
|---|---|---|
| 平台管理员 | `admin` | `admin123` |
| 纠纷调解员 | `mediator` | `admin123` |
| 跑腿员（PC） | `runner` | `admin123` |

> 调解员登录后仅显示工作台、数据统计、任务列表与纠纷调解菜单。

## 目录结构

```
src/
├── api/          接口封装（request.js 统一拦截 token 与错误）
├── router/       路由与登录守卫
├── store/        Pinia 状态（用户 token / 权限）
├── layout/       侧边栏 + 顶栏布局（按角色显示菜单）
└── views/
    ├── Login.vue            登录页
    ├── Dashboard.vue        工作台（任务概览）
    ├── stat/                数据统计（ECharts）
    ├── order/               任务列表 / 任务详情
    ├── workorder/           跑腿申请 / 纠纷调解
    ├── user/ capital/ school/ system/ oss/ profile/
```

## 与后端接口对照

所有接口经 Vite 代理 `/api` → `http://localhost:8081`：

| 前端调用 | 后端接口 | 说明 |
|---|---|---|
| POST /api/login | POST /login | 登录，返回 token |
| GET /api/order/admin/stat/summary | 任务概览 |
| GET /api/order/admin/stat/charts?days=30 | 趋势 / 类型分布 / 跑腿榜 |
| POST /api/order/admin/stat/generate?days=30 | 补算 statistics_daily |
| GET /api/order/order/list/hall | 任务列表 |
| GET /api/order/order/detail/{orderId} | 任务详情 |
| GET /api/order/appeal/list | 纠纷列表 |
| PUT /api/order/appeal/edit | 提交调解结果 |
