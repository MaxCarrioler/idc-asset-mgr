import { useState, useEffect, useMemo } from 'react'
import { useParams, Link } from 'react-router-dom'
import { Card, Table, Button, Tag, Modal, Tabs } from 'antd'
import { request, showError, showSuccess } from '../api'
import { getStatusLabel, resolveCpuArchitectureLabel } from '../utils/format'
import { ACCESSORY_CONFIG, ACCESSORY_TYPES } from '../config/accessory'
import AccessoryFormDialog from '../components/AccessoryFormDialog'

export default function AccessoryDetail() {
  const { id: serverId } = useParams()
  const [server, setServer] = useState(null)
  const [cpuArchitectures, setCpuArchitectures] = useState([])
  const [pageTitle, setPageTitle] = useState('服务器配件详情')
  const [currentType, setCurrentType] = useState('cpus')
  const [accessories, setAccessories] = useState([])
  const [loading, setLoading] = useState(false)
  const [dialogVisible, setDialogVisible] = useState(false)
  const [editingId, setEditingId] = useState(null)

  const config = useMemo(() => ACCESSORY_CONFIG[currentType], [currentType])

  const statusTagType = (status) => {
    const map = { RUNNING: 'success', STOPPED: 'default', MAINTENANCE: 'warning' }
    return map[status] || 'default'
  }

  const loadServerInfo = async () => {
    try {
      const data = await request(`/api/servers/${serverId}`)
      setServer(data)
      setPageTitle(`${data.serverName} - 配件详情`)
    } catch (e) { showError(e) }
  }

  const loadAccessories = async () => {
    setLoading(true)
    try { setAccessories(await request(`${config.apiPath}?serverId=${serverId}`) || []) }
    catch (e) { showError(e); setAccessories([]) }
    finally { setLoading(false) }
  }

  useEffect(() => {
    request('/api/cpu-architectures')
      .then((d) => setCpuArchitectures(d || []))
      .catch((e) => showError(e))
    loadServerInfo()
  }, [serverId])

  useEffect(() => { loadAccessories() }, [currentType])

  const handleDelete = (id) => {
    Modal.confirm({
      title: '提示', content: '确认移除此配件？',
      onOk: async () => {
        try {
          await request(`${config.apiPath}/${id}`, { method: 'DELETE' })
          showSuccess('移除成功')
          loadAccessories()
        } catch (e) { showError(e) }
      }
    })
  }

  const tableColumns = config.columns.map((col) => ({
    title: col.label,
    dataIndex: col.key,
    render: (v) => {
      const field = config.fields.find((f) => f.key === col.key)
      if (col.render) return col.render(v)
      return v ?? '-'
    }
  }))

  tableColumns.push({
    title: '操作', width: 150, fixed: 'right',
    render: (_, row) => (
      <span>
        <Button type="link" onClick={() => { setEditingId(row.id); setDialogVisible(true) }}>编辑</Button>
        <Button type="link" danger onClick={() => handleDelete(row.id)}>移除</Button>
      </span>
    )
  })

  const tabItems = ACCESSORY_TYPES.map((type) => ({
    key: type,
    label: ACCESSORY_CONFIG[type].title
  }))

  return (
    <div>
      <header style={{ marginBottom: 20 }}>
        <Link to="/servers" style={{ color: '#1677ff', fontSize: 14 }}>← 返回服务器列表</Link>
        <h1 style={{ margin: '12px 0', fontSize: 24 }}>{pageTitle}</h1>
        {server && (
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 16, color: '#606266', fontSize: 14 }}>
            <span>Region：<strong>{server.region || '-'}</strong></span>
            <span>IP：<strong>{server.ipAddress || '-'}</strong></span>
            <span>机架：<strong>{server.rackLocation || '-'}</strong></span>
            <span>型号：<strong>{server.serverModel || '-'}</strong></span>
            <span>CPU架构：<strong>{resolveCpuArchitectureLabel(server.cpuArchitecture, cpuArchitectures)}</strong></span>
            <span>厂商：<strong>{server.manufacturer || '-'}</strong></span>
            <span>
              状态：<Tag color={statusTagType(server.status)}>{getStatusLabel(server.status)}</Tag>
            </span>
          </div>
        )}
      </header>

      <Tabs activeKey={currentType} onChange={(key) => setCurrentType(key)} items={tabItems} />

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', margin: '16px 0' }}>
        <h2 style={{ margin: 0, fontSize: 18 }}>{config.title} 配件</h2>
        <Button type="primary" onClick={() => { setEditingId(null); setDialogVisible(true) }}>+ 新增配件</Button>
      </div>

      <Card>
        <Table dataSource={accessories} columns={tableColumns} rowKey="id" loading={loading} pagination={false} scroll={{ x: 1000 }} size="middle" />
      </Card>

      <AccessoryFormDialog
        open={dialogVisible}
        onClose={() => setDialogVisible(false)}
        config={config}
        serverId={serverId}
        editingId={editingId}
        onSaved={loadAccessories}
      />
    </div>
  )
}
