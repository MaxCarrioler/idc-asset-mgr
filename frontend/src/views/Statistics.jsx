import { useState, useEffect, useCallback } from 'react'
import { Card, Table, Select, Button, Spin } from 'antd'
import { request, showError } from '../api'
import { formatMemory, formatTb } from '../utils/format'

const DISK_SPEC_COLUMNS = ['1.8T', '2.4T', '3.84T', '4T', '8T']

export default function Statistics() {
  const [regionOptions, setRegionOptions] = useState([])
  const [serverTypeOptions, setServerTypeOptions] = useState([])
  const [cpuArchitectureOptions, setCpuArchitectureOptions] = useState([])
  const [hardDiskTypeOptions, setHardDiskTypeOptions] = useState([])
  const [selectedRegions, setSelectedRegions] = useState([])
  const [selectedTypeIds, setSelectedTypeIds] = useState([])
  const [selectedCpuArchitectures, setSelectedCpuArchitectures] = useState([])
  const [selectedHardDiskTypes, setSelectedHardDiskTypes] = useState([])
  const [statistics, setStatistics] = useState(null)
  const [loading, setLoading] = useState(false)

  const buildParams = useCallback(() => {
    const params = {}
    if (selectedRegions.length) params.regions = selectedRegions.join(',')
    if (selectedTypeIds.length) params.serverTypeIds = selectedTypeIds.join(',')
    if (selectedCpuArchitectures.length) params.cpuArchitectures = selectedCpuArchitectures.join(',')
    if (selectedHardDiskTypes.length) params.hardDiskTypes = selectedHardDiskTypes.join(',')
    return params
  }, [selectedRegions, selectedTypeIds, selectedCpuArchitectures, selectedHardDiskTypes])

  const loadStatistics = useCallback(async () => {
    setLoading(true)
    try {
      const data = await request('/api/statistics/hardware', { params: buildParams() })
      setStatistics(data)
    } catch (e) {
      showError(e)
      setStatistics(null)
    } finally {
      setLoading(false)
    }
  }, [buildParams])

  useEffect(() => {
    Promise.all([
      request('/api/servers/regions'),
      request('/api/server-types'),
      request('/api/cpu-architectures'),
      request('/api/hard-disk-types')
    ]).then(([regions, serverTypes, cpuArchs, diskTypes]) => {
      setRegionOptions(regions || [])
      setServerTypeOptions(serverTypes || [])
      setCpuArchitectureOptions(cpuArchs || [])
      setHardDiskTypeOptions(diskTypes || [])
    }).catch((e) => showError(e))
  }, [])

  // 筛选条件变化时用最新闭包重新聚合，避免 setTimeout 捕获旧状态导致结果滞后一拍
  useEffect(() => {
    loadStatistics()
  }, [loadStatistics])

  const formatSpecCapacity = (row, spec) => {
    const item = row.diskSpecSummaries?.find((d) => d.spec === spec)
    if (!item || item.totalCapacityTb == null) return '0 TB'
    return formatTb(item.totalCapacityTb)
  }

  const baseColumns = [
    { title: '服务器数量', dataIndex: 'serverCount', width: 110, align: 'center', fixed: 'left' },
    { title: 'CPU核心总数', dataIndex: 'totalCores', width: 120, align: 'center' },
    { title: '线程总数', dataIndex: 'totalThreads', width: 110, align: 'center' },
    {
      title: '内存总容量',
      dataIndex: 'totalMemoryGb',
      width: 130,
      align: 'center',
      render: (v) => formatMemory(v)
    }
  ]

  const diskColumns = DISK_SPEC_COLUMNS.map((spec) => ({
    title: `${spec} 物理总容量`,
    key: spec,
    width: 140,
    align: 'center',
    render: (_, row) => formatSpecCapacity(row, spec)
  }))

  const tailColumns = [
    {
      title: '硬盘物理总容量',
      dataIndex: 'totalDiskCapacityTb',
      width: 150,
      align: 'center',
      fixed: 'right',
      render: (v) => formatTb(v)
    }
  ]

  const columns = [...baseColumns, ...diskColumns, ...tailColumns]
  const tableData = statistics ? [statistics] : []

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ margin: 0, fontSize: 20 }}>硬件资源汇总</h2>
        <Button onClick={loadStatistics}>刷新数据</Button>
      </div>

      <Card style={{ marginBottom: 20 }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 16 }}>
          <div>
            <label style={{ display: 'block', marginBottom: 8, fontSize: 14, color: '#606266', fontWeight: 500 }}>Region 区域</label>
            <Select
              mode="multiple"
              allowClear
              value={selectedRegions}
              onChange={(v) => setSelectedRegions(v)}
              placeholder="全部区域"
              style={{ width: '100%' }}
              options={regionOptions.map((r) => ({ label: r, value: r }))}
              maxTagCount="responsive"
            />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: 8, fontSize: 14, color: '#606266', fontWeight: 500 }}>服务器类型</label>
            <Select
              mode="multiple"
              allowClear
              value={selectedTypeIds}
              onChange={(v) => setSelectedTypeIds(v)}
              placeholder="全部类型"
              style={{ width: '100%' }}
              options={serverTypeOptions.map((t) => ({ label: `${t.name} (${t.code})`, value: t.id }))}
              maxTagCount="responsive"
            />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: 8, fontSize: 14, color: '#606266', fontWeight: 500 }}>CPU 架构</label>
            <Select
              mode="multiple"
              allowClear
              value={selectedCpuArchitectures}
              onChange={(v) => setSelectedCpuArchitectures(v)}
              placeholder="全部架构"
              style={{ width: '100%' }}
              options={cpuArchitectureOptions.map((a) => ({ label: `${a.name} (${a.code})`, value: a.code }))}
              maxTagCount="responsive"
            />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: 8, fontSize: 14, color: '#606266', fontWeight: 500 }}>硬盘类型</label>
            <Select
              mode="multiple"
              allowClear
              value={selectedHardDiskTypes}
              onChange={(v) => setSelectedHardDiskTypes(v)}
              placeholder="全部类型"
              style={{ width: '100%' }}
              options={hardDiskTypeOptions.map((d) => ({ label: `${d.name} (${d.code})`, value: d.code }))}
              maxTagCount="responsive"
            />
          </div>
        </div>
        <p style={{ margin: '12px 0 0', color: '#909399', fontSize: 13 }}>
          默认无筛选，展示全量汇总；勾选任意条件后将实时重新聚合统计。硬盘类型同时匹配硬盘的介质类型与接口类型（如 SSD / SAS / SATA），服务器下存在任一匹配硬盘即命中
        </p>
      </Card>

      <Card>
        <Table
          dataSource={tableData}
          columns={columns}
          rowKey={(_, i) => i}
          loading={loading}
          pagination={false}
          scroll={{ x: 1600 }}
          bordered
          size="middle"
        />
      </Card>
    </div>
  )
}
