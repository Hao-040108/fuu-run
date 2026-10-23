import request from './request'

// 任务统计（统一任务域：代拿 / 代购 / 其他服务）
export const statSummary = () => request.get('/order/admin/stat/summary')
export const statCharts = (params) => request.get('/order/admin/stat/charts', { params })
export const generateStat = (params) => request.post('/order/admin/stat/generate', null, { params })
