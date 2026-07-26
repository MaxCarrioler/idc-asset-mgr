<template>
  <div>
    <div class="toolbar">
      <h2>用户中心</h2>
      <div class="toolbar-actions">
        <el-button @click="refreshAll">刷新</el-button>
        <el-button
          v-if="activeTab === 'users'"
          v-permission="'sys_user_add'"
          type="primary"
          @click="openCreateUser"
        >
          新建用户
        </el-button>
        <el-button
          v-if="activeTab === 'roles'"
          v-permission="'sys_role_add'"
          type="primary"
          @click="openCreateRole"
        >
          新建角色
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="用户管理" name="users">
        <div class="filter-row">
          <el-select v-model="lockFilter" clearable placeholder="状态筛选" style="width: 140px" @change="loadUsers">
            <el-option label="待审批" value="9" />
            <el-option label="正常" value="0" />
          </el-select>
          <el-input
            v-model="username"
            clearable
            placeholder="用户名"
            style="width: 180px"
            @keyup.enter="loadUsers"
          />
          <el-button @click="loadUsers">查询</el-button>
        </div>
        <el-card shadow="never">
          <el-table :data="users" v-loading="usersLoading" stripe>
            <el-table-column prop="userId" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" min-width="120" />
            <el-table-column prop="nickname" label="昵称" min-width="120" />
            <el-table-column prop="phone" label="手机号" min-width="130" />
            <el-table-column label="角色" min-width="180">
              <template #default="{ row }">
                {{ (row.roleList || []).map((r) => r.roleName).join(', ') || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.lockFlag === '9'" type="warning" size="small">待审批</el-tag>
                <el-tag v-else-if="row.lockFlag === '0'" type="success" size="small">正常</el-tag>
                <el-tag v-else type="info" size="small">{{ row.lockFlag }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" min-width="170" />
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.lockFlag === '9'"
                  v-permission="'sys_user_edit'"
                  link
                  type="success"
                  @click="handleApprove(row)"
                >通过</el-button>
                <el-button
                  v-if="row.lockFlag === '9'"
                  v-permission="'sys_user_del'"
                  link
                  type="danger"
                  @click="handleReject(row)"
                >驳回</el-button>
                <el-button v-permission="'sys_user_edit'" link type="primary" @click="openEditUser(row)">编辑</el-button>
                <el-button
                  v-if="row.lockFlag !== '9'"
                  v-permission="'sys_user_del'"
                  link
                  type="danger"
                  @click="handleDeleteUser(row)"
                >注销</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager">
            <el-pagination
              background
              layout="total, prev, pager, next"
              :total="total"
              :page-size="pageSize"
              :current-page="page"
              @current-change="onPageChange"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="角色管理" name="roles">
        <p class="tab-desc">正式业务角色为「配件管理员」与「普通用户」；超级管理员受保护不可删改。</p>
        <el-card shadow="never">
          <el-table :data="roles" v-loading="rolesLoading" stripe>
            <el-table-column prop="roleId" label="ID" width="80" />
            <el-table-column prop="roleName" label="角色名" min-width="140" />
            <el-table-column prop="roleCode" label="角色码" min-width="180" />
            <el-table-column prop="roleDesc" label="描述" min-width="200" show-overflow-tooltip />
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button v-permission="'sys_role_edit'" link type="primary" @click="openEditRole(row)">编辑</el-button>
                <el-button v-permission="'sys_role_perm'" link type="success" @click="openPerm(row)">授权</el-button>
                <el-button v-permission="'sys_role_del'" link type="danger" @click="handleDeleteRole(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      v-model="editVisible"
      :title="isCreatingUser ? '新建用户' : '编辑用户 / 分配角色'"
      width="480px"
      destroy-on-close
    >
      <el-form label-width="80px">
        <el-form-item label="用户名" :required="isCreatingUser">
          <el-input v-model="editForm.username" :disabled="!isCreatingUser" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="密码" :required="isCreatingUser">
          <el-input
            v-model="editForm.password"
            type="password"
            show-password
            autocomplete="new-password"
            :placeholder="isCreatingUser ? '为该用户设定初始密码' : '留空则不修改密码'"
          />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="业务角色">
          <el-select v-model="editForm.role" multiple placeholder="选择业务角色" style="width: 100%">
            <el-option
              v-for="role in businessRoles"
              :key="role.roleId"
              :label="role.roleName"
              :value="role.roleId"
            />
          </el-select>
          <div class="hint">
            仅可分配配件管理员 / 普通用户；超级管理员角色不可通过此入口授予{{
              isCreatingUser ? '，留空默认普通用户' : ''
            }}
          </div>
        </el-form-item>
        <el-form-item v-if="isCreatingUser" label="状态">
          <el-radio-group v-model="editForm.lockFlag">
            <el-radio value="0">正常（可直接登录）</el-radio>
            <el-radio value="9">锁定</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitUserForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="formVisible" :title="roleForm.roleId ? '编辑角色' : '新建角色'" width="480px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="角色名" required>
          <el-input v-model="roleForm.roleName" />
        </el-form-item>
        <el-form-item label="角色码" required>
          <el-input v-model="roleForm.roleCode" :disabled="!!roleForm.roleId" placeholder="如 CUSTOM_ROLE" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="roleForm.roleDesc" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveRoleForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permVisible" title="菜单权限授权" width="560px" destroy-on-close>
      <p class="tab-desc">当前角色：{{ permRole?.roleName }}（{{ permRole?.roleCode }}）</p>
      <el-tree
        ref="treeRef"
        v-loading="permLoading"
        :data="menuTree"
        show-checkbox
        node-key="id"
        default-expand-all
        :props="{ label: 'name', children: 'children' }"
      />
      <template #footer>
        <el-button @click="permVisible = false">取消</el-button>
        <el-button v-permission="'sys_role_perm'" type="primary" :loading="saving" @click="savePerm">保存授权</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import {
  approveUser,
  createRole,
  createUser,
  deleteRoles,
  deleteUsers,
  fetchMenuTree,
  fetchRoleMenuIds,
  fetchRoles,
  fetchUsers,
  filterBusinessRoles,
  isSuperAdminRole,
  preferredBusinessRoles,
  rejectUser,
  saveRoleMenus,
  updateRole,
  updateUser
} from '../api/userCenter'
import { showError, showSuccess } from '../api'

const activeTab = ref('users')
const saving = ref(false)

const usersLoading = ref(false)
const users = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 10
const username = ref('')
const lockFilter = ref('')
const businessRoles = ref([])
const editVisible = ref(false)
const isCreatingUser = ref(false)
const preservedAdminRoleIds = ref([])
const editForm = reactive({
  userId: null,
  username: '',
  password: '',
  nickname: '',
  name: '',
  phone: '',
  lockFlag: '0',
  role: []
})

const rolesLoading = ref(false)
const roles = ref([])
const formVisible = ref(false)
const roleForm = reactive({
  roleId: null,
  roleName: '',
  roleCode: '',
  roleDesc: ''
})
const permVisible = ref(false)
const permLoading = ref(false)
const permRole = ref(null)
const menuTree = ref([])
const treeRef = ref(null)

async function loadUsers() {
  usersLoading.value = true
  try {
    const data = await fetchUsers({
      current: page.value,
      size: pageSize,
      username: username.value || undefined,
      lockFlag: lockFilter.value || undefined
    })
    users.value = data?.records || []
    total.value = data?.total || 0
  } catch (error) {
    showError(error)
  } finally {
    usersLoading.value = false
  }
}

async function loadBusinessRoles() {
  const data = await fetchRoles()
  businessRoles.value = preferredBusinessRoles(data || [])
}

async function loadRoles() {
  rolesLoading.value = true
  try {
    const data = await fetchRoles()
    roles.value = filterBusinessRoles(data || [])
    businessRoles.value = preferredBusinessRoles(data || [])
  } catch (error) {
    showError(error)
  } finally {
    rolesLoading.value = false
  }
}

function onPageChange(p) {
  page.value = p
  loadUsers()
}

async function onTabChange(name) {
  if (name === 'roles' && !roles.value.length) {
    await loadRoles()
  }
}

async function refreshAll() {
  if (activeTab.value === 'users') {
    await loadUsers()
  } else {
    await loadRoles()
  }
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(`确认审批通过用户「${row.username}」？`, '审批通过')
    await approveUser(row.userId)
    showSuccess('已通过，用户可登录')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') showError(error)
  }
}

async function handleReject(row) {
  try {
    await ElMessageBox.confirm(`驳回将删除用户「${row.username}」，确认？`, '驳回注册', { type: 'warning' })
    await rejectUser(row.userId)
    showSuccess('已驳回并删除')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') showError(error)
  }
}

async function handleDeleteUser(row) {
  try {
    await ElMessageBox.confirm(`确认注销（删除）用户「${row.username}」？`, '注销用户', { type: 'warning' })
    await deleteUsers([row.userId])
    showSuccess('已注销')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') showError(error)
  }
}

function openCreateUser() {
  isCreatingUser.value = true
  preservedAdminRoleIds.value = []
  editForm.userId = null
  editForm.username = ''
  editForm.password = ''
  editForm.nickname = ''
  editForm.name = ''
  editForm.phone = ''
  editForm.lockFlag = '0'
  editForm.role = []
  editVisible.value = true
}

function openEditUser(row) {
  isCreatingUser.value = false
  editForm.userId = row.userId
  editForm.username = row.username
  editForm.password = ''
  editForm.nickname = row.nickname || ''
  editForm.name = row.name || ''
  editForm.phone = row.phone || ''
  editForm.lockFlag = row.lockFlag || '0'
  const roleList = row.roleList || []
  preservedAdminRoleIds.value = roleList.filter((r) => isSuperAdminRole(r)).map((r) => r.roleId)
  editForm.role = roleList.filter((r) => !isSuperAdminRole(r)).map((r) => r.roleId)
  editVisible.value = true
}

async function submitUserForm() {
  if (isCreatingUser.value && (!editForm.username.trim() || !editForm.password)) {
    showError(new Error('请填写用户名与密码'))
    return
  }
  if (!isCreatingUser.value && !editForm.role.length && !preservedAdminRoleIds.value.length) {
    showError(new Error('请至少选择一个业务角色'))
    return
  }
  saving.value = true
  try {
    const payload = {
      username: editForm.username.trim(),
      nickname: editForm.nickname.trim() || undefined,
      name: editForm.name.trim() || undefined,
      phone: editForm.phone.trim() || undefined,
      role: [...preservedAdminRoleIds.value, ...editForm.role]
    }
    if (isCreatingUser.value) {
      await createUser({ ...payload, password: editForm.password, lockFlag: editForm.lockFlag })
      showSuccess('用户已创建')
    } else {
      await updateUser({
        ...payload,
        userId: editForm.userId,
        password: editForm.password || undefined
      })
      showSuccess('已保存')
    }
    editVisible.value = false
    await loadUsers()
  } catch (error) {
    showError(error)
  } finally {
    saving.value = false
  }
}

function openCreateRole() {
  roleForm.roleId = null
  roleForm.roleName = ''
  roleForm.roleCode = ''
  roleForm.roleDesc = ''
  formVisible.value = true
}

function openEditRole(row) {
  roleForm.roleId = row.roleId
  roleForm.roleName = row.roleName
  roleForm.roleCode = row.roleCode
  roleForm.roleDesc = row.roleDesc || ''
  formVisible.value = true
}

async function saveRoleForm() {
  if (!roleForm.roleName.trim() || !roleForm.roleCode.trim()) {
    showError(new Error('请填写角色名与角色码'))
    return
  }
  saving.value = true
  try {
    const payload = {
      roleId: roleForm.roleId || undefined,
      roleName: roleForm.roleName.trim(),
      roleCode: roleForm.roleCode.trim(),
      roleDesc: roleForm.roleDesc
    }
    if (roleForm.roleId) {
      await updateRole(payload)
    } else {
      await createRole(payload)
    }
    showSuccess('已保存')
    formVisible.value = false
    await loadRoles()
  } catch (error) {
    showError(error)
  } finally {
    saving.value = false
  }
}

async function handleDeleteRole(row) {
  try {
    await ElMessageBox.confirm(`确认删除角色「${row.roleName}」？`, '删除角色', { type: 'warning' })
    await deleteRoles([row.roleId])
    showSuccess('已删除')
    await loadRoles()
  } catch (error) {
    if (error !== 'cancel') showError(error)
  }
}

function normalizeTree(nodes = []) {
  return nodes.map((n) => ({
    id: n.id,
    name: n.name || n.meta?.title || String(n.id),
    children: normalizeTree(n.children || [])
  }))
}

async function openPerm(row) {
  permRole.value = row
  permVisible.value = true
  permLoading.value = true
  try {
    const [tree, checked] = await Promise.all([fetchMenuTree(), fetchRoleMenuIds(row.roleId)])
    menuTree.value = normalizeTree(tree || [])
    await nextTick()
    const ids = (checked || []).map((id) => String(id))
    treeRef.value?.setCheckedKeys(ids)
  } catch (error) {
    showError(error)
  } finally {
    permLoading.value = false
  }
}

async function savePerm() {
  if (!permRole.value) return
  saving.value = true
  try {
    const checked = treeRef.value?.getCheckedKeys(false) || []
    const half = treeRef.value?.getHalfCheckedKeys?.() || []
    const menuIds = [...checked, ...half]
    await saveRoleMenus(permRole.value.roleId, menuIds)
    showSuccess('授权已保存')
    permVisible.value = false
  } catch (error) {
    showError(error)
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    await Promise.all([loadUsers(), loadBusinessRoles()])
  } catch (error) {
    showError(error)
  }
})
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.toolbar h2 {
  margin: 0;
  font-size: 20px;
  color: #1a1a2e;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.filter-row {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.tab-desc {
  margin: 0 0 12px;
  color: #909399;
  font-size: 13px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.hint {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}
</style>
