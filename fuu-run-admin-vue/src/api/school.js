import request from './request'

// ==================== 校区 ====================
export const listSchools = (params) => request.get('/address/school/list', { params })
export const addSchool = (data) => request.post('/address/school/add', data)
export const editSchool = (data) => request.put('/address/school/edit', data)
export const getSchool = (id) => request.get(`/address/school/${id}`)

// ==================== 区域 / 楼栋 ====================
export const listRegions = (schoolId) => request.get(`/address/region/list/${schoolId}`)
export const queryRegions = (params) => request.get('/address/region/list/user', { params })
export const addRegion = (data) => request.post('/address/region/add', data)
export const editRegion = (data) => request.put('/address/region/edit', data)
export const removeRegions = (ids) => request.delete(`/address/region/${ids}`)
