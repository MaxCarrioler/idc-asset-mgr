import { useState, useEffect, useMemo } from 'react'
import { useLocation } from 'react-router-dom'
import { Card, Table, Button, Tag, Modal } from 'antd'
import { request, showError, showSuccess } from '../api'
import ServerTypeFormDialog from '../components/ServerTypeFormDialog'
import CpuArchitectureFormDialog from '../components/CpuArchitectureFormDialog'
import HardDiskTypeFormDialog from '../components/HardDiskTypeFormDialog'

export default function MetadataManagement() {
  const location = useLocation()
  const activeTab = useMemo(() => {
    if (location.pathname.endsWith('/cpu-architectures')) return 'cpuArchitectures'
    if (location.pathname.endsWith('/hard-disk-types')) return 'hardDiskTypes'
    return 'serverTypes'
  }, [location.pathname])

  const [serverTypes, setServerTypes] = useState([])
  const [cpuArchitectures, setCpuArchitectures] = useState([])
  const [hardDiskTypes, setHardDiskTypes] = useState([])
  const [typesLoading, setTypesLoading] = useState(false)
  const [archLoading, setArchLoading] = useState(false)
  const [diskTypeLoading, setDiskTypeLoading] = useState(false)
  const [typeDialogVisible, setTypeDialogVisible] = useState(false)
  const [editingTypeId, setEditingTypeId] = useState(null)
  const [archDialogVisible, setArchDialogVisible] = useState(false)
  const [editingArchId, setEditingArchId] = useState(null)
  const [diskTypeDialogVisible, setDiskTypeDialogVisible] = useState(false)
  const [editingDiskTypeId, setEditingDiskTypeId] = useState(null)

  const loadServerTypes = async () => {
    setTypesLoading(true)
    try {
      setServerTypes(await request('/api/server-types') || [])
    } catch (e) { showError(e); setServerTypes([]) }
    finally { setTypesLoading(false) }
  }

  const loadCpuArchitectures = async () => {
    setArchLoading(true)
    try {
      setCpuArchitectures(await request('/api/cpu-architectures') || [])
    } catch (e) { showError(e); setCpuArchitectures([]) }
    finally { setArchLoading(false) }
  }

  const loadHardDiskTypes = async () => {
    setDiskTypeLoading(true)
    try {
      setHardDiskTypes(await request('/api/hard-disk-types') || [])
    } catch (e) { showError(e); setHardDiskTypes([]) }
    finally { setDiskTypeLoading(false) }
  }

  useEffect(() => {
    loadServerTypes()
    loadCpuArchitectures()
    loadHardDiskTypes()
  }, [])

  const handleDeleteType = (row) => {
    Modal.confirm({
      title: '提示', content: `确认删除服务器类型「${row.name}」？`,
      onOk: async () => {
        try {
          await request(`/api/server-types/${row.id}`, { method: 'DELETE' })
          showSuccess('删除成功')
          loadServerTypes()
        } catch (e) { showError(e) }
      }
    })
  }

  const handleDeleteArch = (row) => {
    Modal.confirm({
      title: '提示', content: `确认删除 CPU 架构「${row.name}」？`,
      onOk: async () => {
        try {
          await request(`/api/cpu-architectures/${row.id}`, { method: 'DELETE' })
          showSuccess('删除成功')
          loadCpuArchitectures()
        } catch (e) { showError(e) }
      }
    })
  }

  const handleDeleteDiskType = (row) => {
    Modal.confirm({
      title: '提示', content: `确认删除硬盘类型「${row.name}」？`,
      onOk: async () => {
        try {
          await request(`/api/hard-disk-types/${row.id}`, { method: 'DELETE' })
          showSuccess('删除成功')
          loadHardDiskTypes()
        } catch (e) { showError(e) }
      }
    })
  }

  const typeColumns = [
    { title: 'ID', dataIndex: 'id', width: 70 },
    { title: '类型编码', dataIndex: 'code', render: (v) => <Tag>{v}</Tag> },
    { title: '类型名称', dataIndex: 'name' },
    { title: '描述', dataIndex: 'description', ellipsis: true },
    { title: '统计线程', dataIndex: 'trackThreads', width: 100, render: (v) => v ? '是' : '否' },
    { title: '统计硬盘', dataIndex: 'includeDiskStats', width: 100, render: (v) => v ? '是' : '否' },
    { title: '来源', dataIndex: 'builtIn', width: 90, render: (v) => v ? '内置' : '自定义' },
    {
      title: '操作', width: 150, fixed: 'right',
      render: (_, row) => (
        <span>
          <Button type="link" onClick={() => { setEditingTypeId(row.id); setTypeDialogVisible(true) }}>编辑</Button>
          {!row.builtIn && <Button type="link" danger onClick={() => handleDeleteType(row)}>删除</Button>}
        </span>
      )
    }
  ]

  const archColumns = [
    { title: 'ID', dataIndex: 'id', width: 70 },
    { title: '架构编码', dataIndex: 'code', render: (v) => <Tag color="success">{v}</Tag> },
    { title: '架构名称', dataIndex: 'name' },
    { title: '描述', dataIndex: 'description', ellipsis: true },
    { title: '来源', dataIndex: 'builtIn', width: 90, render: (v) => v ? '内置' : '自定义' },
    {
      title: '操作', width: 150, fixed: 'right',
      render: (_, row) => (
        <span>
          <Button type="link" onClick={() => { setEditingArchId(row.id); setArchDialogVisible(true) }}>编辑</Button>
          {!row.builtIn && <Button type="link" danger onClick={() => handleDeleteArch(row)}>删除</Button>}
        </span>
      )
    }
  ]

  const diskTypeColumns = [
    { title: 'ID', dataIndex: 'id', width: 70 },
    { title: '类型编码', dataIndex: 'code', render: (v) => <Tag color="processing">{v}</Tag> },
    { title: '类型名称', dataIndex: 'name' },
    { title: '描述', dataIndex: 'description', ellipsis: true },
    { title: '来源', dataIndex: 'builtIn', width: 90, render: (v) => v ? '内置' : '自定义' },
    {
      title: '操作', width: 150, fixed: 'right',
      render: (_, row) => (
        <span>
          <Button type="link" onClick={() => { setEditingDiskTypeId(row.id); setDiskTypeDialogVisible(true) }}>编辑</Button>
          {!row.builtIn && <Button type="link" danger onClick={() => handleDeleteDiskType(row)}>删除</Button>}
        </span>
      )
    }
  ]

  return (
    <div>
      <h2 style={{ margin: '0 0 16px', fontSize: 20 }}>元数据管理</h2>

      {activeTab === 'serverTypes' && (
        <>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <p style={{ margin: 0, color: '#909399', fontSize: 13 }}>定义服务器类型及统计规则，供服务器录入与数据统计使用</p>
            <Button type="primary" onClick={() => { setEditingTypeId(null); setTypeDialogVisible(true) }}>+ 新增类型</Button>
          </div>
          <Card>
            <Table dataSource={serverTypes} columns={typeColumns} rowKey="id" loading={typesLoading} pagination={false} size="middle" />
          </Card>
        </>
      )}

      {activeTab === 'cpuArchitectures' && (
        <>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <p style={{ margin: 0, color: '#909399', fontSize: 13 }}>定义 CPU 架构元数据，供服务器录入与数据统计筛选使用</p>
            <Button type="primary" onClick={() => { setEditingArchId(null); setArchDialogVisible(true) }}>+ 新增架构</Button>
          </div>
          <Card>
            <Table dataSource={cpuArchitectures} columns={archColumns} rowKey="id" loading={archLoading} pagination={false} size="middle" />
          </Card>
        </>
      )}

      {activeTab === 'hardDiskTypes' && (
        <>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <p style={{ margin: 0, color: '#909399', fontSize: 13 }}>定义硬盘类型元数据，供硬盘配件录入与备件分类使用</p>
            <Button type="primary" onClick={() => { setEditingDiskTypeId(null); setDiskTypeDialogVisible(true) }}>+ 新增类型</Button>
          </div>
          <Card>
            <Table dataSource={hardDiskTypes} columns={diskTypeColumns} rowKey="id" loading={diskTypeLoading} pagination={false} size="middle" />
          </Card>
        </>
      )}

      <ServerTypeFormDialog
        open={typeDialogVisible}
        onClose={() => setTypeDialogVisible(false)}
        typeId={editingTypeId}
        onSaved={loadServerTypes}
      />
      <CpuArchitectureFormDialog
        open={archDialogVisible}
        onClose={() => setArchDialogVisible(false)}
        archId={editingArchId}
        onSaved={loadCpuArchitectures}
      />
      <HardDiskTypeFormDialog
        open={diskTypeDialogVisible}
        onClose={() => setDiskTypeDialogVisible(false)}
        diskTypeId={editingDiskTypeId}
        onSaved={loadHardDiskTypes}
      />
    </div>
  )
}
