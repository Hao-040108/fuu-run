import request from './request'

// 钱包分页
export const pageWallets = (params) => request.get('/payment/wallet/page', { params })
// 我的钱包
export const myWallet = () => request.get('/payment/wallet/curr')
// 提现申请分页
export const pageWithdraw = (params) => request.get('/payment/withdraw/page', { params })
// 提现审核（通过/驳回）
export const auditWithdraw = (data) => request.put('/payment/recode/edit', data)
// 提交提现申请（我的钱包）
export const submitWithdraw = (data) => request.post('/payment/recode', data)
// 最近一条提现记录
export const lastRecode = () => request.get('/payment/recode/last')
// 资金流动分页
export const pageCapitalFlow = (params) => request.get('/payment/capital/list', { params })
