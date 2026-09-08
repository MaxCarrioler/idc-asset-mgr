import { useState, useEffect, useRef, useCallback } from 'react'
import { useLocation } from 'react-router-dom'
import { Card, Table, Button, Tag, Modal, Select, Input, Form, Radio, Tree, Pagination } from 'antd'
import {
  approveUser, createRole, createUser, deleteRoles, deleteUsers,
  fetchMenuTree, fetchRoleMenuIds, fetchRoles, fetchUsers,
  filterBusinessRoles, isSuperAdminRole, preferredBusinessRoles,
  rejectUser, saveRoleMenus, updateRole, updateUser
} from '../api/userCenter'
import { showError, showSuccess } from '../api'
import Permission from '../components/Permission'

export default function UserCenter() {
  const location = useLocation()
  const routeTab = location.pathname.endsWith('/roles') ? 'roles' : 'users'
  const [activeTab, setActiveTab] = useState(routeTab)
  const [saving, setSaving] = useState(false)

  useEffect(() => { if (routeTab !== activeTab) setActiveTab(routeTab) }, [location.pathname])

  // Users state
  const [usersLoading, setUsersLoading] = useState(false)
  const [users, setUsers] = useState([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const pageSize = 10
  const [username, setUsername] = useState('')
  const [lockFilter, setLockFilter] = useState('')
  const [businessRoles, setBusinessRoles] = useState([])

  // User edit dialog
  const [editVisible, setEditVisible] = useState(false)
  const [isCreatingUser, setIsCreatingUser] = useState(false)
  const [preservedAdminRoleIds, setPreservedAdminRoleIds] = useState([])
  const [userForm] = Form.useForm()

  // Roles state
  const [rolesLoading, setRolesLoading] = useState(false)
  const [roles, setRoles] = useState([])

  // Role form dialog
  const [roleFormVisible, setRoleFormVisible] = useState(false)
  const [roleForm] = Form.useForm()

  // Permission dialog
  const [permVisible, setPermVisible] = useState(false)
  const [permLoading, setPermLoading] = useState(false)
  const [permRole, setPermRole] = useState(null)
  const [menuTree, setMenuTree] = useState([])
  const treeRef = useRef(null)

  const loadUsers = useCallback(async (p = page) => {
    setUsersLoading(true)
    try {
      const data = await fetchUsers({ current: p, size: pageSize, username: username || undefined, lockFlag: lockFilter || undefined })
      setUsers(data?.records || [])
      setTotal(data?.total || 0)
    } catch (e) { showError(e) }
    finally { setUsersLoading(false) }
  }, [page, username, lockFilter])

  const loadBusinessRoles = async () => {
    try {
      const data = await fetchRoles()
      setBusinessRoles(preferredBusinessRoles(data || []))
    } catch (e) { showError(e) }
  }

  const loadRoles = async () => {
    setRolesLoading(true)
    try {
      const data = await fetchRoles()
      setRoles(filterBusinessRoles(data || []))
      setBusinessRoles(preferredBusinessRoles(data || []))
    } catch (e) { showError(e) }
    finally { setRolesLoading(false) }
  }

  useEffect(() => { loadUsers(1); loadBusinessRoles() }, [])

  useEffect(() => {
    if (activeTab === 'roles' && !roles.length) loadRoles()
  }, [activeTab])

  const handleApprove = (row) => {
    Modal.confirm({
      title: '审批通过', content: `确认审批通过用户「${row.username}」？`,
      onOk: async () => {
        try { await approveUser(row.userId); showSuccess('已通过，用户可登录'); await loadUsers() }
        catch (e) { showError(e) }
      }
    })
  }

  const handleReject = (row) => {
    Modal.confirm({
      title: '驳回注册', content: `驳回将删除用户「${row.username}」，确认？`, type: 'warning',
      onOk: async () => {
        try { await rejectUser(row.userId); showSuccess('已驳回并删除'); await loadUsers() }
        catch (e) { showError(e) }
      }
    })
  }

  const handleDeleteUser = (row) => {
    Modal.confirm({
      title: '注销用户', content: `确认注销（删除）用户「${row.username}」？`, type: 'warning',
      onOk: async () => {
        try { await deleteUsers([row.userId]); showSuccess('已注销'); await loadUsers() }
        catch (e) { showError(e) }
      }
    })
  }

  const openCreateUser = () => {
    setIsCreatingUser(true)
    setPreservedAdminRoleIds([])
    userForm.resetFields()
    userForm.setFieldsValue({ lockFlag: '0', role: [] })
    setEditVisible(true)
  }

  const openEditUser = (row) => {
    setIsCreatingUser(false)
    const roleList = row.roleList || []
    setPreservedAdminRoleIds(roleList.filter((r) => isSuperAdminRole(r)).map((r) => r.roleId))
    userForm.setFieldsValue({
      username: row.username, password: '', nickname: row.nickname || '',
      name: row.name || '', phone: row.phone || '', lockFlag: row.lockFlag || '0',
      role: roleList.filter((r) => !isSuperAdminRole(r)).map((r) => r.roleId)
    })
    setEditVisible(true)
  }

  const submitUserForm = async (values) => {
    if (isCreatingUser && (!values.username?.trim() || !values.password)) {
      showError(new Error('请填写用户名与密码')); return
    }
    if (!isCreatingUser && !values.role?.length && !preservedAdminRoleIds.length) {
      showError(new Error('请至少选择一个业务角色')); return
    }
    setSaving(true)
    try {
      const payload = {
        username: values.username?.trim(),
        nickname: values.nickname?.trim() || undefined,
        name: values.name?.trim() || undefined,
        phone: values.phone?.trim() || undefined,
        role: [...preservedAdminRoleIds, ...(values.role || [])]
      }
      if (isCreatingUser) {
        await createUser({ ...payload, password: values.password, lockFlag: values.lockFlag })
        showSuccess('用户已创建')
      } else {
        await updateUser({ ...payload, userId: userForm.getFieldValue('_userId'), password: values.password || undefined })
        showSuccess('已保存')
      }
      setEditVisible(false)
      await loadUsers()
    } catch (e) { showError(e) }
    finally { setSaving(false) }
  }

  const openCreateRole = () => {
    roleForm.resetFields()
    setRoleFormVisible(true)
  }

  const openEditRole = (row) => {
    roleForm.setFieldsValue({ roleId: row.roleId, roleName: row.roleName, roleCode: row.roleCode, roleDesc: row.roleDesc || '' })
    setRoleFormVisible(true)
  }

  const saveRoleForm = async (values) => {
    if (!values.roleName?.trim() || !values.roleCode?.trim()) {
      showError(new Error('请填写角色名与角色码')); return
    }
    setSaving(true)
    try {
      const payload = { roleId: values.roleId || undefined, roleName: values.roleName.trim(), roleCode: values.roleCode.trim(), roleDesc: values.roleDesc }
      if (values.roleId) await updateRole(payload)
      else await createRole(payload)
      showSuccess('已保存')
      setRoleFormVisible(false)
      await loadRoles()
    } catch (e) { showError(e) }
    finally { setSaving(false) }
  }

  const handleDeleteRole = (row) => {
    Modal.confirm({
      title: '删除角色', content: `确认删除角色「${row.roleName}」？`, type: 'warning',
      onOk: async () => {
        try { await deleteRoles([row.roleId]); showSuccess('已删除'); await loadRoles() }
        catch (e) { showError(e) }
      }
    })
  }

  const normalizeTree = (nodes = []) =>
    nodes.map((n) => ({ key: String(n.id), title: n.name || n.meta?.title || String(n.id), children: normalizeTree(n.children || []) }))

  const openPerm = async (row) => {
    setPermRole(row)
    setPermVisible(true)
    setPermLoading(true)
    try {
      const [tree, checked] = await Promise.all([fetchMenuTree(), fetchRoleMenuIds(row.roleId)])
      setMenuTree(normalizeTree(tree || []))
      setTimeout(() => {
        const ids = (checked || []).map((id) => String(id))
        treeRef.current?.setCheckedKeys(ids)
      }, 100)
    } catch (e) { showError(e) }
    finally { setPermLoading(false) }
  }

  const savePerm = async () => {
    if (!permRole) return
    setSaving(true)
    try {
      const checked = treeRef.current?.checkedKeys || []
      const half = treeRef.current?.halfCheckedKeys || []
      const menuIds = [...checked, ...half]
      await saveRoleMenus(permRole.roleId, menuIds)
      showSuccess('授权已保存')
      setPermVisible(false)
    } catch (e) { showError(e) }
    finally { setSaving(false) }
  }

  // User table columns
  const userColumns = [
    { title: 'ID', dataIndex: 'userId', width: 80 },
    { title: '用户名', dataIndex: 'username', width: 120 },
    { title: '昵称', dataIndex: 'nickname', width: 120 },
    { title: '手机号', dataIndex: 'phone', width: 130 },
    { title: '角色', width: 180, render: (_, row) => (row.roleList || []).map((r) => r.roleName).join(', ') || '-' },
    {
      title: '状态', width: 100,
      render: (_, row) => {
        if (row.lockFlag === '9') return <Tag color="warning">待审批</Tag>
        if (row.lockFlag === '0') return <Tag color="success">正常</Tag>
        return <Tag>{row.lockFlag}</Tag>
      }
    },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
    {
      title: '操作', width: 280, fixed: 'right',
      render: (_, row) => (
        <span>
          {row.lockFlag === '9' && (
            <Permission require="sys_user_edit">
              <Button type="link" style={{ color: '#52c41a' }} onClick={() => handleApprove(row)}>通过</Button>
            </Permission>
          )}
          {row.lockFlag === '9' && (
            <Permission require="sys_user_del">
              <Button type="link" danger onClick={() => handleReject(row)}>驳回</Button>
            </Permission>
          )}
          <Permission require="sys_user_edit">
            <Button type="link" onClick={() => openEditUser(row)}>编辑</Button>
          </Permission>
          {row.lockFlag !== '9' && (
            <Permission require="sys_user_del">
              <Button type="link" danger onClick={() => handleDeleteUser(row)}>注销</Button>
            </Permission>
          )}
        </span>
      )
    }
  ]

  const roleColumns = [
    { title: 'ID', dataIndex: 'roleId', width: 80 },
    { title: '角色名', dataIndex: 'roleName', width: 140 },
    { title: '角色码', dataIndex: 'roleCode', width: 180 },
    { title: '描述', dataIndex: 'roleDesc', width: 200, ellipsis: true },
    {
      title: '操作', width: 220, fixed: 'right',
      render: (_, row) => (
        <span>
          <Permission require="sys_role_edit">
            <Button type="link" onClick={() => openEditRole(row)}>编辑</Button>
          </Permission>
          <Permission require="sys_role_perm">
            <Button type="link" style={{ color: '#52c41a' }} onClick={() => openPerm(row)}>授权</Button>
          </Permission>
          <Permission require="sys_role_del">
            <Button type="link" danger onClick={() => handleDeleteRole(row)}>删除</Button>
          </Permission>
        </span>
      )
    }
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
        <h2 style={{ margin: 0, fontSize: 20, color: '#1a1a2e' }}>用户中心</h2>
        <div style={{ display: 'flex', gap: 8 }}>
          <Button onClick={() => activeTab === 'users' ? loadUsers() : loadRoles()}>刷新</Button>
          {activeTab === 'users' && (
            <Permission require="sys_user_add">
              <Button type="primary" onClick={openCreateUser}>新建用户</Button>
            </Permission>
          )}
          {activeTab === 'roles' && (
            <Permission require="sys_role_add">
              <Button type="primary" onClick={openCreateRole}>新建角色</Button>
            </Permission>
          )}
        </div>
      </div>

      {/* Tab switcher */}
      <div style={{ marginBottom: 12, display: 'flex', gap: 8 }}>
        <Button type={activeTab === 'users' ? 'primary' : 'default'} onClick={() => setActiveTab('users')}>用户管理</Button>
        <Button type={activeTab === 'roles' ? 'primary' : 'default'} onClick={() => setActiveTab('roles')}>角色管理</Button>
      </div>

      {activeTab === 'users' ? (
        <>
          <div style={{ marginBottom: 12, display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
            <Select value={lockFilter || undefined} onChange={(v) => setLockFilter(v || '')} allowClear placeholder="状态筛选" style={{ width: 140 }}
              options={[{ label: '待审批', value: '9' }, { label: '正常', value: '0' }]} />
            <Input value={username} onChange={(e) => setUsername(e.target.value)} allowClear placeholder="用户名" style={{ width: 180 }} onPressEnter={() => { setPage(1); loadUsers(1) }} />
            <Button onClick={() => { setPage(1); loadUsers(1) }}>查询</Button>
          </div>
          <Card>
            <Table dataSource={users} columns={userColumns} rowKey="userId" loading={usersLoading} pagination={false} scroll={{ x: 1200 }} size="middle" />
            <div style={{ marginTop: 16, display: 'flex', justifyContent: 'flex-end' }}>
              <Pagination current={page} pageSize={pageSize} total={total}
                onChange={(p) => { setPage(p); loadUsers(p) }}
                showTotal={(t) => `共 ${t} 条`} />
            </div>
          </Card>
        </>
      ) : (
        <>
          <p style={{ margin: '0 0 12px', color: '#909399', fontSize: 13 }}>正式业务角色为「配件管理员」与「普通用户」；超级管理员受保护不可删改。</p>
          <Card>
            <Table dataSource={roles} columns={roleColumns} rowKey="roleId" loading={rolesLoading} pagination={false} scroll={{ x: 900 }} size="middle" />
          </Card>
        </>
      )}

      {/* User Edit Dialog */}
      <Modal title={isCreatingUser ? '新建用户' : '编辑用户 / 分配角色'} open={editVisible}
        onCancel={() => setEditVisible(false)} width={480} destroyOnClose
        footer={[<Button key="cancel" onClick={() => setEditVisible(false)}>取消</Button>,
          <Button key="save" type="primary" loading={saving} onClick={() => userForm.submit()}>保存</Button>]}>
        <Form form={userForm} layout="vertical" onFinish={(v) => submitUserForm(v)}
          initialValues={{ lockFlag: '0', role: [] }}>
          <Form.Item label="用户名" name="username">
            <Input disabled={!isCreatingUser} placeholder="登录用户名" />
          </Form.Item>
          <Form.Item label="密码" name="password">
            <Input.Password autoComplete="new-password" placeholder={isCreatingUser ? '为该用户设定初始密码' : '留空则不修改密码'} />
          </Form.Item>
          <Form.Item label="昵称" name="nickname"><Input /></Form.Item>
          <Form.Item label="姓名" name="name"><Input /></Form.Item>
          <Form.Item label="手机号" name="phone"><Input /></Form.Item>
          <Form.Item label="业务角色" name="role">
            <Select mode="multiple" placeholder="选择业务角色"
              options={businessRoles.map((r) => ({ label: r.roleName, value: r.roleId }))} />
            <div style={{ marginTop: 6, fontSize: 12, color: '#909399', lineHeight: 1.4 }}>
              仅可分配配件管理员 / 普通用户；超级管理员角色不可通过此入口授予{isCreatingUser ? '，留空默认普通用户' : ''}
            </div>
          </Form.Item>
          {isCreatingUser && (
            <Form.Item label="状态" name="lockFlag">
              <Radio.Group>
                <Radio value="0">正常（可直接登录）</Radio>
                <Radio value="9">锁定</Radio>
              </Radio.Group>
            </Form.Item>
          )}
        </Form>
      </Modal>

      {/* Role Form Dialog */}
      <Modal title={roleForm.getFieldValue('roleId') ? '编辑角色' : '新建角色'} open={roleFormVisible}
        onCancel={() => setRoleFormVisible(false)} width={480} destroyOnClose
        footer={[<Button key="cancel" onClick={() => setRoleFormVisible(false)}>取消</Button>,
          <Button key="save" type="primary" loading={saving} onClick={() => roleForm.submit()}>保存</Button>]}>
        <Form form={roleForm} layout="vertical" onFinish={saveRoleForm}>
          <Form.Item label="角色名" name="roleName" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item label="角色码" name="roleCode" rules={[{ required: true }]}>
            <Input placeholder="如 CUSTOM_ROLE" />
          </Form.Item>
          <Form.Item label="描述" name="roleDesc"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>

      {/* Permission Dialog */}
      <Modal title="菜单权限授权" open={permVisible} onCancel={() => setPermVisible(false)} width={560} destroyOnClose
        footer={[<Button key="cancel" onClick={() => setPermVisible(false)}>取消</Button>,
          <Permission key="save" require="sys_role_perm">
            <Button type="primary" loading={saving} onClick={savePerm}>保存授权</Button>
          </Permission>]}>
        <p style={{ margin: '0 0 12px', color: '#909399', fontSize: 13 }}>
          当前角色：{permRole?.roleName}（{permRole?.roleCode}）
        </p>
        <Tree ref={treeRef} treeData={menuTree} checkable defaultExpandAll
          style={{ maxHeight: 400, overflow: 'auto' }} />
      </Modal>
    </div>
  )
}
