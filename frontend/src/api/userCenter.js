import { request } from './index'

const SUPER_ADMIN_CODE = 'ROLE_ADMIN'
const BUSINESS_ROLE_CODES = ['ROLE_ACCESSORY_ADMIN', 'GENERAL_USER']

export function registerUser(payload) {
  return request('/admin/register/user', { method: 'POST', body: payload })
}

export function fetchUsers(params = {}) {
  return request('/admin/user/page', { params })
}

export function fetchUserDetail(id) {
  return request(`/admin/user/details/${id}`)
}

export function createUser(payload) {
  return request('/admin/user', { method: 'POST', body: payload })
}

export function updateUser(payload) {
  return request('/admin/user', { method: 'PUT', body: payload })
}

export function deleteUsers(ids) {
  return request('/admin/user', { method: 'DELETE', body: ids })
}

export function approveUser(id) {
  return request(`/admin/user/approve/${id}`, { method: 'PUT' })
}

export function rejectUser(id) {
  return request(`/admin/user/reject/${id}`, { method: 'DELETE' })
}

export function fetchRoles() {
  return request('/admin/role/list')
}

export function fetchRolePage(params = {}) {
  return request('/admin/role/page', { params })
}

export function createRole(payload) {
  return request('/admin/role', { method: 'POST', body: payload })
}

export function updateRole(payload) {
  return request('/admin/role', { method: 'PUT', body: payload })
}

export function deleteRoles(ids) {
  return request('/admin/role', { method: 'DELETE', body: ids })
}

export function fetchMenuTree() {
  return request('/admin/menu/tree')
}

export function fetchRoleMenuIds(roleId) {
  return request(`/admin/menu/tree/${roleId}`)
}

export function saveRoleMenus(roleId, menuIds) {
  const ids = Array.isArray(menuIds) ? menuIds.join(',') : String(menuIds || '')
  return request('/admin/role/menu', {
    method: 'PUT',
    body: { roleId, menuIds: ids }
  })
}

export function isSuperAdminRole(role) {
  return (role?.roleCode || role?.role_code) === SUPER_ADMIN_CODE
}

export function filterBusinessRoles(roles = []) {
  return roles.filter((r) => !isSuperAdminRole(r))
}

export function preferredBusinessRoles(roles = []) {
  const list = filterBusinessRoles(roles)
  const preferred = list.filter((r) => BUSINESS_ROLE_CODES.includes(r.roleCode || r.role_code))
  return preferred.length ? preferred : list
}

export { SUPER_ADMIN_CODE, BUSINESS_ROLE_CODES }
