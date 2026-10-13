import { useState, useEffect, useCallback } from 'react'
import { useLocation } from 'react-router-dom'
import { Card, Table, Button, Tag, Modal, Select, Input, InputNumber, Form, Descriptions, Empty } from 'antd'
import * as XLSX from 'xlsx'
import { request, showError, showSuccess } from '../api'
import Permission from '../components/Permission'
import { useAuth } from '../stores/AuthContext'

const STATUS_MAP = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', OUTBOUND: '已出库' }
function statusLabel(s) { return STATUS_MAP[s] || s || '-' }
function statusTagType(s) { return ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'error', OUTBOUND: 'default' })[s] || '' }

function formatOutboundTime(value) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

export default function AccessoryCenter() {
  const location = useLocation()
  const { hasAnyRole } = useAuth()
  const isAdmin = hasAnyRole('ROLE_ADMIN')
  const routeTab = location.pathname.endsWith('/transfers') ? 'transfers' : 'requests'
  const [activeTab, setActiveTab] = useState(routeTab)

  const [statusFilter, setStatusFilter] = useState('')
  const [requests, setRequests] = useState([])
  const [transfers, setTransfers] = useState([])
  const [categories, setCategories] = useState([])
  const [servers, setServers] = useState([])
  const [requestsLoading, setRequestsLoading] = useState(false)
  const [transfersLoading, setTransfersLoading] = useState(false)
  const [exporting, setExporting] = useState(false)

  // Apply dialog
  const [applyVisible, setApplyVisible] = useState(false)
  const [applySubmitting, setApplySubmitting] = useState(false)
  const [applyForm] = Form.useForm()

  // Detail dialog
  const [detailVisible, setDetailVisible] = useState(false)
  const [detail, setDetail] = useState(null)

  // Approve dialog
  const [approveVisible, setApproveVisible] = useState(false)
  const [approveTarget, setApproveTarget] = useState(null)
  const [availableSpareParts, setAvailableSpareParts] = useState([])
  const [selectedSpareRows, setSelectedSpareRows] = useState([])
  const [spareLoading, setSpareLoading] = useState(false)
  const [approveSubmitting, setApproveSubmitting] = useState(false)
  const [approveRemark, setApproveRemark] = useState('')
  const [rejectReason, setRejectReason] = useState('')

  // Outbound dialog
  const [outboundVisible, setOutboundVisible] = useState(false)
  const [outboundTargetId, setOutboundTargetId] = useState(null)
  const [outboundRows, setOutboundRows] = useState([])
  const [outboundRemark, setOutboundRemark] = useState('')
  const [outboundSubmitting, setOutboundSubmitting] = useState(false)

  useEffect(() => {
    if (routeTab !== activeTab) setActiveTab(routeTab)
  }, [location.pathname])

  const loadRequests = useCallback(async () => {
    setRequestsLoading(true)
    try {
      const params = {}
      if (statusFilter) params.status = statusFilter
      setRequests((await request('/api/accessory-center/requests', { params })) || [])
    } catch (e) { showError(e) }
    finally { setRequestsLoading(false) }
  }, [statusFilter])

  const loadTransfers = useCallback(async () => {
    setTransfersLoading(true)
    try { setTransfers((await request('/api/accessory-center/transfers')) || []) }
    catch (e) { showError(e) }
    finally { setTransfersLoading(false) }
  }, [])

  const loadMeta = useCallback(async () => {
    try {
      const [cats, srv] = await Promise.all([
        request('/api/spare-part-categories'),
        request('/api/servers', { params: { page: 1, size: 1000 } })
      ])
      setCategories(cats || [])
      setServers(Array.isArray(srv) ? srv : (srv?.content || srv?.records || []))
    } catch (e) { showError(e) }
  }, [])

  useEffect(() => { loadMeta(); loadRequests() }, [])

  useEffect(() => {
    if (activeTab === 'transfers') loadTransfers()
  }, [activeTab])

  const formatSourceServer = (row) => {
    if (row.sourceServerSerial === '无') return '无'
    const name = row.sourceServerName || ''
    const serial = row.sourceServerSerial || ''
    if (name && serial) return `${name}（${serial}）`
    return name || serial || '-'
  }

  const formatActiveServer = (row) => {
    const name = row.targetServerName || ''
    const serial = row.targetServerSerial || ''
    if (name && serial) return `${name}（${serial}）`
    return name || serial || '-'
  }

  const exportTransfers = () => {
    if (!transfers.length) { showError(new Error('暂无流转记录可导出')); return }
    setExporting(true)
    try {
      const rows = transfers.map((row) => ({
        ID: row.id ?? '', 申请单: row.requestId ?? '', 种类: row.categoryName || '-',
        序列号: row.serialNumber || '-', 来源服务器: formatSourceServer(row),
        现役服务器: formatActiveServer(row), 操作人: row.operatorName || '-',
        出库时间: formatOutboundTime(row.outboundTime), 备注: row.remark || ''
      }))
      const sheet = XLSX.utils.json_to_sheet(rows)
      sheet['!cols'] = [{ wch: 8 }, { wch: 10 }, { wch: 16 }, { wch: 20 }, { wch: 28 }, { wch: 28 }, { wch: 12 }, { wch: 20 }, { wch: 20 }]
      const workbook = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(workbook, sheet, '流转记录')
      const stamp = formatOutboundTime(new Date()).replace(/[:\s]/g, '')
      XLSX.writeFile(workbook, `配件中心-流转记录-${stamp}.xlsx`)
      showSuccess('导出成功')
    } catch (e) { showError(e) }
    finally { setExporting(false) }
  }

  const submitApply = async (values) => {
    if (!values.categoryId || !values.purpose?.trim() || !values.targetServerId) {
      showError(new Error('请完整填写种类、用途与目标服务器'))
      return
    }
    setApplySubmitting(true)
    try {
      await request('/api/accessory-center/requests', {
        method: 'POST',
        body: { categoryId: values.categoryId, specParams: values.specParams || null, quantity: values.quantity, purpose: values.purpose.trim(), targetServerId: values.targetServerId }
      })
      showSuccess('申请已提交')
      setApplyVisible(false)
      applyForm.resetFields()
      await loadRequests()
    } catch (e) { showError(e) }
    finally { setApplySubmitting(false) }
  }

  const openDetail = async (row) => {
    try { setDetail(await request(`/api/accessory-center/requests/${row.id}`)); setDetailVisible(true) }
    catch (e) { showError(e) }
  }

  const openApprove = async (row) => {
    setApproveTarget(row)
    setSelectedSpareRows([])
    setApproveRemark('')
    setRejectReason('')
    setApproveVisible(true)
    setSpareLoading(true)
    try {
      setAvailableSpareParts((await request('/api/accessory-center/available-spare-parts', { params: { categoryId: row.categoryId } })) || [])
    } catch (e) { showError(e); setAvailableSpareParts([]) }
    finally { setSpareLoading(false) }
  }

  const submitApprove = async () => {
    const qty = approveTarget?.quantity || 0
    if (selectedSpareRows.length !== qty) { showError(new Error(`请恰好选择 ${qty} 个备件序列号`)); return }
    setApproveSubmitting(true)
    try {
      await request(`/api/accessory-center/requests/${approveTarget.id}/approve`, {
        method: 'POST',
        body: { sparePartIds: selectedSpareRows.map((r) => r.id), remark: approveRemark || null }
      })
      showSuccess('审批已通过')
      setApproveVisible(false)
      await loadRequests()
    } catch (e) { showError(e) }
    finally { setApproveSubmitting(false) }
  }

  const submitReject = async () => {
    if (!rejectReason?.trim()) { showError(new Error('请填写驳回原因')); return }
    setApproveSubmitting(true)
    try {
      await request(`/api/accessory-center/requests/${approveTarget.id}/reject`, {
        method: 'POST', body: { reason: rejectReason.trim() }
      })
      showSuccess('已驳回')
      setApproveVisible(false)
      await loadRequests()
    } catch (e) { showError(e) }
    finally { setApproveSubmitting(false) }
  }

  const openOutbound = async (row) => {
    try {
      const full = await request(`/api/accessory-center/requests/${row.id}`)
      setOutboundTargetId(row.id)
      setOutboundRemark('')
      setOutboundRows((full.items || []).map((item) => {
        const matched = item.sourceServerId
          ? servers.find((s) => s.id === item.sourceServerId)
          : servers.find((s) => s.serialNumber === item.sourceServerSerial)
        return {
          sparePartId: item.sparePartId, serialNumber: item.serialNumber,
          sourceServerId: item.sourceServerId || null, sourceServerSerial: item.sourceServerSerial || '',
          sourceServerName: matched?.serverName || matched?.name || ''
        }
      }))
      setOutboundVisible(true)
    } catch (e) { showError(e) }
  }

  const submitOutbound = async () => {
    setOutboundSubmitting(true)
    try {
      await request(`/api/accessory-center/requests/${outboundTargetId}/outbound`, {
        method: 'POST', body: { remark: outboundRemark || null }
      })
      showSuccess('出库成功')
      setOutboundVisible(false)
      await loadRequests()
      if (activeTab === 'transfers') await loadTransfers()
    } catch (e) { showError(e) }
    finally { setOutboundSubmitting(false) }
  }

  const revokeApproval = (row) => {
    Modal.confirm({
      title: '撤销审批',
      content: '撤销后该申请将回退为「待审批」，已预留的备件会释放回在库。确认撤销？',
      okText: '确认撤销',
      okButtonProps: { danger: true },
      cancelText: '取消',
      onOk: async () => {
        try {
          await request(`/api/accessory-center/requests/${row.id}/revoke`, { method: 'POST' })
          showSuccess('已撤销审批')
          await loadRequests()
        } catch (e) { showError(e) }
      }
    })
  }

  const handleDeleteRequest = (row) => {
    Modal.confirm({
      title: '删除申请',
      content: `确认删除申请 #${row.id}？此操作不可恢复。`,
      okText: '确认删除',
      okButtonProps: { danger: true },
      cancelText: '取消',
      onOk: async () => {
        try {
          await request(`/api/accessory-center/requests/${row.id}`, { method: 'DELETE' })
          showSuccess('删除成功')
          await loadRequests()
        } catch (e) { showError(e) }
      }
    })
  }

  // Request table columns
  const requestColumns = [
    { title: 'ID', dataIndex: 'id', width: 70 },
    { title: '配件种类', dataIndex: 'categoryName', width: 120 },
    { title: '规格参数', dataIndex: 'specParams', width: 120, ellipsis: true },
    { title: '数量', dataIndex: 'quantity', width: 70 },
    { title: '用途', dataIndex: 'purpose', width: 140, ellipsis: true },
    {
      title: '目标服务器', width: 160,
      render: (_, row) => (<div>{row.targetServerName || '-'}<div style={{ color: '#909399', fontSize: 12 }}>{row.targetServerSerial}</div></div>)
    },
    { title: '申请人', dataIndex: 'applicantName', width: 100 },
    { title: '状态', dataIndex: 'status', width: 100, render: (s) => <Tag color={statusTagType(s)}>{statusLabel(s)}</Tag> },
    { title: '申请时间', dataIndex: 'createdAt', width: 160 },
    {
      title: '操作', width: 280, fixed: 'right',
      render: (_, row) => (
        <span>
          <Button type="link" onClick={() => openDetail(row)}>详情</Button>
          {row.status === 'PENDING' && (
            <Permission require="accessory:transfer:approve">
              <Button type="link" style={{ color: '#52c41a' }} onClick={() => openApprove(row)}>审批</Button>
            </Permission>
          )}
          {row.status === 'APPROVED' && (
            <Permission require="accessory:transfer:outbound">
              <Button type="link" style={{ color: '#faad14' }} onClick={() => openOutbound(row)}>出库</Button>
            </Permission>
          )}
          {row.status === 'APPROVED' && (
            <Permission require="accessory:transfer:approve">
              <Button type="link" danger onClick={() => revokeApproval(row)}>撤销</Button>
            </Permission>
          )}
          {isAdmin && row.status !== 'OUTBOUND' && (
            <Button type="link" danger onClick={() => handleDeleteRequest(row)}>删除</Button>
          )}
        </span>
      )
    }
  ]

  const transferColumns = [
    { title: 'ID', dataIndex: 'id', width: 70 },
    { title: '申请单', dataIndex: 'requestId', width: 90 },
    { title: '种类', dataIndex: 'categoryName', width: 110 },
    { title: '序列号', dataIndex: 'serialNumber', width: 140 },
    {
      title: '来源服务器', width: 160,
      render: (_, row) => {
        const name = row.sourceServerName || (row.sourceServerSerial === '无' ? '无' : '-')
        return (
          <div>
            <div>{name}</div>
            {row.sourceServerSerial && row.sourceServerSerial !== '无' && (
              <div style={{ color: '#909399', fontSize: 12 }}>{row.sourceServerSerial}</div>
            )}
          </div>
        )
      }
    },
    {
      title: '现役服务器', width: 160,
      render: (_, row) => (<div>{row.targetServerName || '-'}<div style={{ color: '#909399', fontSize: 12 }}>{row.targetServerSerial}</div></div>)
    },
    { title: '操作人', dataIndex: 'operatorName', width: 100 },
    { title: '出库时间', dataIndex: 'outboundTime', width: 160 }
  ]

  const spareColumns = [
    { title: '序列号', dataIndex: 'serialNumber', width: 140 },
    { title: '厂商', dataIndex: 'manufacturer', width: 100 },
    { title: '规格参数', dataIndex: 'specSummary', width: 120 },
    { title: '来源服务器序列号', dataIndex: 'sourceServerSerial', width: 140 },
    { title: '状态', dataIndex: 'status', width: 90 }
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
        <h2 style={{ margin: 0, fontSize: 20, color: '#1a1a2e' }}>配件中心</h2>
        <div style={{ display: 'flex', gap: 8 }}>
          <Button onClick={() => { loadRequests(); if (activeTab === 'transfers') loadTransfers() }}>刷新</Button>
          <Permission require="accessory:transfer:apply">
            <Button type="primary" onClick={() => { applyForm.resetFields(); setApplyVisible(true) }}>新建调取申请</Button>
          </Permission>
        </div>
      </div>

      {/* Tab switcher */}
      <div style={{ marginBottom: 12, display: 'flex', gap: 8 }}>
        <Button type={activeTab === 'requests' ? 'primary' : 'default'} onClick={() => setActiveTab('requests')}>申请列表</Button>
        <Button type={activeTab === 'transfers' ? 'primary' : 'default'} onClick={() => setActiveTab('transfers')}>流转记录</Button>
      </div>

      {activeTab === 'requests' ? (
        <>
          <div style={{ marginBottom: 12 }}>
            <Select value={statusFilter || undefined} onChange={(v) => setStatusFilter(v || '')} allowClear placeholder="状态筛选" style={{ width: 160 }}
              options={[{ label: '待审批', value: 'PENDING' }, { label: '已通过', value: 'APPROVED' }, { label: '已驳回', value: 'REJECTED' }, { label: '已出库', value: 'OUTBOUND' }]}
            />
          </div>
          <Card>
            <Table dataSource={requests} columns={requestColumns} rowKey="id" loading={requestsLoading} pagination={false} scroll={{ x: 1300 }} size="middle" />
          </Card>
        </>
      ) : (
        <>
          <div style={{ marginBottom: 12 }}>
            <Button type="primary" disabled={!transfers.length} loading={exporting} onClick={exportTransfers}>导出 Excel</Button>
          </div>
          <Card>
            <Table dataSource={transfers} columns={transferColumns} rowKey="id" loading={transfersLoading} pagination={false} scroll={{ x: 1200 }} size="middle" />
          </Card>
        </>
      )}

      {/* Apply Dialog */}
      <Modal title="新建调取申请" open={applyVisible} onCancel={() => setApplyVisible(false)} width={560}
        footer={[<Button key="cancel" onClick={() => setApplyVisible(false)}>取消</Button>,
          <Button key="submit" type="primary" loading={applySubmitting} onClick={() => applyForm.submit()}>提交</Button>]}
        destroyOnClose>
        <Form form={applyForm} layout="vertical" onFinish={submitApply} initialValues={{ quantity: 1 }}>
          <Form.Item label="配件种类" name="categoryId" rules={[{ required: true }]}>
            <Select showSearch optionFilterProp="label" placeholder="选择种类"
              options={categories.map((c) => ({ label: c.name, value: c.id }))} />
          </Form.Item>
          <Form.Item label="规格参数" name="specParams">
            <Input placeholder="如容量、型号等" />
          </Form.Item>
          <Form.Item label="数量" name="quantity" rules={[{ required: true }]}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item label="用途" name="purpose" rules={[{ required: true }]}>
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item label="目标服务器" name="targetServerId" rules={[{ required: true }]}>
            <Select showSearch optionFilterProp="label" placeholder="选择服务器"
              options={servers.map((s) => ({ label: `${s.serverName} (${s.serialNumber})`, value: s.id }))} />
          </Form.Item>
        </Form>
      </Modal>

      {/* Detail Dialog */}
      <Modal title="申请详情" open={detailVisible} onCancel={() => setDetailVisible(false)} footer={null} width={720}>
        {detail && (
          <>
            <Descriptions bordered column={2}>
              <Descriptions.Item label="种类">{detail.categoryName}</Descriptions.Item>
              <Descriptions.Item label="状态"><Tag color={statusTagType(detail.status)}>{statusLabel(detail.status)}</Tag></Descriptions.Item>
              <Descriptions.Item label="数量">{detail.quantity}</Descriptions.Item>
              <Descriptions.Item label="申请人">{detail.applicantName}</Descriptions.Item>
              <Descriptions.Item label="规格参数" span={2}>{detail.specParams || '-'}</Descriptions.Item>
              <Descriptions.Item label="用途" span={2}>{detail.purpose}</Descriptions.Item>
              <Descriptions.Item label="目标服务器">{detail.targetServerName}</Descriptions.Item>
              <Descriptions.Item label="目标序列号">{detail.targetServerSerial}</Descriptions.Item>
              {detail.approveRemark && <Descriptions.Item label="审批备注" span={2}>{detail.approveRemark}</Descriptions.Item>}
              {detail.rejectReason && <Descriptions.Item label="驳回原因" span={2}>{detail.rejectReason}</Descriptions.Item>}
            </Descriptions>
            <h4 style={{ margin: '16px 0 8px' }}>已绑定备件</h4>
            {detail.items?.length ? (
              <Table dataSource={detail.items} size="small" bordered pagination={false} rowKey="id"
                columns={[
                  { title: '序列号', dataIndex: 'serialNumber' },
                  { title: '厂商', dataIndex: 'manufacturer' },
                  { title: '来源服务器序列号', dataIndex: 'sourceServerSerial' },
                  { title: '来源服务器', dataIndex: 'sourceServerName' }
                ]} />
            ) : <Empty description="尚未绑定备件" imageStyle={{ height: 64 }} />}
          </>
        )}
      </Modal>

      {/* Approve Dialog */}
      <Modal title="审批调取申请" open={approveVisible} onCancel={() => setApproveVisible(false)} width={760} destroyOnClose
        footer={[
          <Permission key="reject" require="accessory:transfer:reject">
            <Button danger loading={approveSubmitting} onClick={submitReject}>驳回</Button>
          </Permission>,
          <Permission key="approve" require="accessory:transfer:approve">
            <Button type="primary" loading={approveSubmitting} onClick={submitApprove}>通过</Button>
          </Permission>
        ]}>
        <p style={{ margin: '0 0 12px', color: '#606266', fontSize: 13 }}>
          申请数量：{approveTarget?.quantity}，请勾选对应数量的在库备件序列号后通过。
        </p>
        <Table dataSource={availableSpareParts} columns={spareColumns} rowKey="id" loading={spareLoading} pagination={false} size="small"
          rowSelection={{
            type: 'checkbox',
            onChange: (_, selectedRows) => setSelectedSpareRows(selectedRows)
          }} />
        <div style={{ marginTop: 12 }}>
          <div style={{ marginBottom: 8 }}><label style={{ marginRight: 8 }}>备注</label><Input value={approveRemark} onChange={(e) => setApproveRemark(e.target.value)} /></div>
          <div><label style={{ marginRight: 8 }}>驳回原因</label><Input value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} placeholder="驳回时填写" /></div>
        </div>
      </Modal>

      {/* Outbound Dialog */}
      <Modal title="配件出库" open={outboundVisible} onCancel={() => setOutboundVisible(false)} width={720} destroyOnClose
        footer={[<Button key="cancel" onClick={() => setOutboundVisible(false)}>取消</Button>,
          <Button key="ok" type="primary" loading={outboundSubmitting} onClick={submitOutbound}>确认出库</Button>]}>
        <p style={{ margin: '0 0 12px', color: '#606266', fontSize: 13 }}>
          以下备件的来源服务器均为新增备件时已定义的信息，确认无误后出库。
        </p>
        <Table dataSource={outboundRows} bordered size="small" pagination={false} rowKey="sparePartId"
          columns={[
            { title: '备件序列号', dataIndex: 'serialNumber', width: 140 },
            {
              title: '来源服务器', width: 280,
              render: (_, row) => {
                if (row.sourceServerSerial === '无' || row.sourceServerSerial === '采购') return row.sourceServerSerial
                if (row.sourceServerName || row.sourceServerSerial) {
                  return (
                    <div>
                      <div>{row.sourceServerName || row.sourceServerSerial}</div>
                      {row.sourceServerName && row.sourceServerSerial && (
                        <div style={{ color: '#909399', fontSize: 12 }}>{row.sourceServerSerial}</div>
                      )}
                    </div>
                  )
                }
                return '-'
              }
            }
          ]} />
        <div style={{ marginTop: 12 }}>
          <label style={{ marginRight: 8 }}>备注</label>
          <Input value={outboundRemark} onChange={(e) => setOutboundRemark(e.target.value)} />
        </div>
      </Modal>
    </div>
  )
}
