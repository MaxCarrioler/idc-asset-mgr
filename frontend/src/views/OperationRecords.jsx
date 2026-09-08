import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { Card, Table, Spin } from 'antd'
import { request, showError } from '../api'
import { formatDateTime, resolveCpuArchitectureLabel } from '../utils/format'

export default function OperationRecords() {
  const { id: serverId } = useParams()
  const [server, setServer] = useState(null)
  const [cpuArchitectures, setCpuArchitectures] = useState([])
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(false)
  const [pageTitle, setPageTitle] = useState('操作记录')

  const columns = [
    {
      title: '修改时间',
      dataIndex: 'modifiedAt',
      width: 180,
      render: (v) => formatDateTime(v)
    },
    {
      title: '变更内容',
      dataIndex: 'changeContent',
      ellipsis: true
    },
    {
      title: '变更原因',
      dataIndex: 'changeReason',
      width: 220
    }
  ]

  useEffect(() => {
    if (!serverId) return
    setLoading(true)
    Promise.all([
      request('/api/cpu-architectures'),
      request(`/api/servers/${serverId}`),
      request(`/api/servers/${serverId}/operation-records`)
    ]).then(([archList, serverData, recordData]) => {
      setCpuArchitectures(archList || [])
      setServer(serverData)
      setRecords(recordData || [])
      setPageTitle(`${serverData.serverName} - 操作记录`)
    }).catch((e) => {
      showError(e)
      setRecords([])
    }).finally(() => setLoading(false))
  }, [serverId])

  return (
    <div>
      <header style={{ marginBottom: 20 }}>
        <Link to="/servers" style={{ color: '#1677ff', fontSize: 14 }}>← 返回服务器列表</Link>
        <h1 style={{ margin: '12px 0', fontSize: 24 }}>{pageTitle}</h1>
        {server && (
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 16, color: '#606266', fontSize: 14 }}>
            <span>Region：<strong>{server.region || '-'}</strong></span>
            <span>机架位置：<strong>{server.rackLocation || '-'}</strong></span>
            <span>型号：<strong>{server.serverModel || '-'}</strong></span>
            <span>CPU架构：<strong>{resolveCpuArchitectureLabel(server.cpuArchitecture, cpuArchitectures)}</strong></span>
            <span>更新时间：<strong>{formatDateTime(server.updatedAt)}</strong></span>
          </div>
        )}
      </header>

      <Card>
        <Table
          dataSource={records}
          columns={columns}
          rowKey="id"
          loading={loading}
          pagination={false}
          size="middle"
        />
      </Card>
    </div>
  )
}
