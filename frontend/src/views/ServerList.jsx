import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { Card, Table, Input, Button, Tag, Modal, Pagination } from 'antd'
import { request, showError, showSuccess } from '../api'
import { getStatusLabel, formatDateTime, resolveCpuArchitectureLabel } from '../utils/format'
import ServerFormDialog from '../components/ServerFormDialog'

export default function ServerList() {
  const [keyword, setKeyword] = useState('')
  const [servers, setServers] = useState([])
  const [loading, setLoading] = useState(false)
  const [currentPage, setCurrentPage] = useState(1)
  const [total, setTotal] = useState(0)
  const [dialogVisible, setDialogVisible] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [cpuArchitectures, setCpuArchitectures] = useState([])

  const pageSize = 10

  const loadServers = useCallback(async (page = currentPage) => {
    setLoading(true)
    try {
      const params = { page, size: pageSize }
      if (keyword.trim()) params.keyword = keyword.trim()
      const data = await request('/api/servers', { params })
      setServers(data.records || [])
      setTotal(data.total || 0)
    } catch (e) {
      showError(e)
      setServers([])
    } finally {
      setLoading(false)
    }
  }, [keyword, currentPage])

  useEffect(() => {
    request('/api/cpu-architectures')
      .then((data) => setCpuArchitectures(data || []))
      .catch((e) => showError(e))
    loadServers(1)
  }, [])

  const handleSearch = () => {
    setCurrentPage(1)
    loadServers(1)
  }

  const handleReset = () => {
    setKeyword('')
    setCurrentPage(1)
    loadServers(1)
  }

  const handleDelete = (row) => {
    Modal.confirm({
      title: '提示',
      content: `确认删除服务器「${row.serverName}」？`,
      onOk: async () => {
        try {
          await request(`/api/servers/${row.id}`, { method: 'DELETE' })
          showSuccess('删除成功')
          loadServers(currentPage)
        } catch (e) {
          showError(e)
        }
      }
    })
  }

  const statusTagType = (status) => {
    const map = { RUNNING: 'success', STOPPED: 'default', MAINTENANCE: 'warning' }
    return map[status] || 'default'
  }

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 70 },
    {
      title: '服务器名称',
      dataIndex: 'serverName',
      minWidth: 140,
      render: (text, row) => (
        <Link to={`/servers/${row.id}/accessories`} style={{ color: '#1677ff' }}>{text}</Link>
      )
    },
    { title: 'Region名称', dataIndex: 'region', width: 110 },
    { title: '序列号', dataIndex: 'serialNumber', width: 120 },
    { title: '机架位置', dataIndex: 'rackLocation', width: 100 },
    { title: '型号', dataIndex: 'serverModel', width: 100 },
    {
      title: 'CPU架构',
      dataIndex: 'cpuArchitecture',
      width: 100,
      render: (val) => resolveCpuArchitectureLabel(val, cpuArchitectures)
    },
    { title: '厂商', dataIndex: 'manufacturer', width: 90 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (status) => <Tag color={statusTagType(status)}>{getStatusLabel(status)}</Tag>
    },
    { title: '操作系统', dataIndex: 'osType', width: 100 },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      width: 170,
      render: (v) => formatDateTime(v)
    },
    {
      title: '操作记录',
      width: 100,
      render: (_, row) => (
        <Link to={`/servers/${row.id}/records`} style={{ color: '#1677ff' }}>查看记录</Link>
      )
    },
    {
      title: '操作',
      width: 150,
      fixed: 'right',
      render: (_, row) => (
        <span>
          <Button type="link" onClick={() => { setEditingId(row.id); setDialogVisible(true) }}>编辑</Button>
          <Button type="link" danger onClick={() => handleDelete(row)}>删除</Button>
        </span>
      )
    }
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <Input
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="搜索服务器名称、Region、序列号、型号、CPU架构、机架位置..."
            allowClear
            style={{ width: 400 }}
            onPressEnter={handleSearch}
          />
          <Button type="primary" onClick={handleSearch}>查询</Button>
          <Button onClick={handleReset}>重置</Button>
        </div>
        <Button type="primary" onClick={() => { setEditingId(null); setDialogVisible(true) }}>
          + 新增服务器
        </Button>
      </div>

      <Card>
        <Table
          dataSource={servers}
          columns={columns}
          rowKey="id"
          loading={loading}
          pagination={false}
          scroll={{ x: 1400 }}
          size="middle"
        />
        <div style={{ marginTop: 16, display: 'flex', justifyContent: 'flex-end' }}>
          <Pagination
            current={currentPage}
            pageSize={pageSize}
            total={total}
            onChange={(page) => { setCurrentPage(page); loadServers(page) }}
            showTotal={(t) => `共 ${t} 条`}
          />
        </div>
      </Card>

      <ServerFormDialog
        open={dialogVisible}
        onClose={() => setDialogVisible(false)}
        serverId={editingId}
        onSaved={() => loadServers(currentPage)}
      />
    </div>
  )
}
