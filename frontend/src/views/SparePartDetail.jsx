import { useState, useEffect, useMemo } from 'react'
import { useParams, Link } from 'react-router-dom'
import { Card, Table, Button, Tag, Modal } from 'antd'
import { request, showError, showSuccess } from '../api'
import { SPARE_PART_TYPE_MAP } from '../config/accessory'
import SparePartFormDialog from '../components/SparePartFormDialog'
import { useAuth } from '../stores/AuthContext'

function statusLabel(status) {
  switch (status) {
    case 'OUT': return '已出库'
    case 'RESERVED': return '已预留'
    case 'IN_STOCK': default: return '在库'
  }
}

function statusTagType(status) {
  switch (status) {
    case 'OUT': return 'default'
    case 'RESERVED': return 'warning'
    case 'IN_STOCK': default: return 'success'
  }
}

export default function SparePartDetail() {
  const { type, categoryId } = useParams()
  const catId = Number(categoryId)
  const { hasAnyRole, hasPermission } = useAuth()
  const isAdmin = hasAnyRole('ROLE_ADMIN')
  const canEdit = hasPermission('spare_part:edit')
  const canDelete = hasPermission('spare_part:delete')

  const [items, setItems] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(false)
  const [dialogVisible, setDialogVisible] = useState(false)
  const [editingItemId, setEditingItemId] = useState(null)

  const typeName = useMemo(() => {
    const matched = categories.find((c) => c.id === catId)
    return matched?.name || `种类#${catId}`
  }, [categories, catId])

  const inStockCount = useMemo(() => items.filter((i) => !i.status || i.status === 'IN_STOCK').length, [items])
  const outCount = useMemo(() => items.filter((i) => i.status === 'OUT').length, [items])
  const reservedCount = useMemo(() => items.filter((i) => i.status === 'RESERVED').length, [items])

  const loadCategories = async () => {
    try { setCategories(await request('/api/spare-part-categories') || []) }
    catch (e) { showError(e); setCategories([]) }
  }

  const loadItems = async () => {
    setLoading(true)
    try { setItems(await request(`/api/spare-parts/items?categoryId=${catId}`) || []) }
    catch (e) { showError(e); setItems([]) }
    finally { setLoading(false) }
  }

  useEffect(() => { loadCategories(); loadItems() }, [catId])

  const handleDelete = (row) => {
    if (!isAdmin && (row.status === 'OUT' || row.status === 'RESERVED')) {
      showError(new Error('已出库或已预留的备件不可删除'))
      return
    }
    const isProtected = row.status === 'OUT' || row.status === 'RESERVED'
    Modal.confirm({
      title: '提示',
      content: isProtected
        ? `备件「${row.serialNumber}」当前状态为"${statusLabel(row.status)}"，确认强制删除？`
        : `确认删除备件「${row.serialNumber}」？`,
      type: isProtected ? 'warning' : undefined,
      onOk: async () => {
        try {
          await request(`/api/spare-parts/items/${row.id}`, { method: 'DELETE' })
          showSuccess('删除成功')
          loadItems()
        } catch (e) { showError(e) }
      }
    })
  }

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 70 },
    { title: '序列号', dataIndex: 'serialNumber', width: 140 },
    { title: '厂商/品牌', dataIndex: 'manufacturer', width: 110, render: (v) => v || '-' },
    { title: '规格摘要', dataIndex: 'specSummary', width: 160, render: (v) => v || '-' },
    { title: '存放位置', dataIndex: 'location', width: 110, render: (v) => v || '-' },
    {
      title: '状态', dataIndex: 'status', width: 100,
      render: (s) => <Tag color={statusTagType(s)}>{statusLabel(s)}</Tag>
    },
    {
      title: '来源服务器', width: 160,
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
    },
    {
      title: '现役服务器', width: 160,
      render: (_, row) => {
        if (row.activeServerName || row.activeServerSerial) {
          return (
            <div>
              <div>{row.activeServerName || '-'}</div>
              {row.activeServerSerial && <div style={{ color: '#909399', fontSize: 12 }}>{row.activeServerSerial}</div>}
            </div>
          )
        }
        return '-'
      }
    },
    { title: '备注', dataIndex: 'remark', width: 120, render: (v) => v || '-' },
    {
      title: '操作', width: 150, fixed: 'right',
      render: (_, row) => {
        const locked = row.status === 'OUT' || row.status === 'RESERVED'
        return (
          <span>
            {canEdit && (
              <Button type="link" disabled={locked}
                onClick={() => { setEditingItemId(row.id); setDialogVisible(true) }}>编辑</Button>
            )}
            {canDelete && (
              <Button type="link" danger disabled={!isAdmin && locked}
                onClick={() => handleDelete(row)}>删除</Button>
            )}
          </span>
        )
      }
    }
  ]

  return (
    <div>
      <header style={{ marginBottom: 20 }}>
        <Link to={`/spare-parts/${type}`} style={{ color: '#1677ff', fontSize: 14 }}>← 返回{type ? (SPARE_PART_TYPE_MAP[type] || '') : ''}备件列表</Link>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginTop: 12 }}>
          <div>
            <h1 style={{ margin: '0 0 8px', fontSize: 24 }}>{typeName} 备件明细</h1>
            <p style={{ margin: 0, color: '#909399', fontSize: 13 }}>
              共 {items.length} 条记录，在库 {inStockCount} 件，已出库 {outCount} 件
              {reservedCount > 0 && `，已预留 ${reservedCount} 件`}
            </p>
          </div>
          {canEdit && (
            <Button type="primary" onClick={() => { setEditingItemId(null); setDialogVisible(true) }}>+ 新增备件</Button>
          )}
        </div>
      </header>

      <Card>
        <Table dataSource={items} columns={columns} rowKey="id" loading={loading} pagination={false} scroll={{ x: 1400 }} size="middle" />
      </Card>

      <SparePartFormDialog
        open={dialogVisible}
        onClose={() => setDialogVisible(false)}
        itemId={editingItemId}
        initialCategoryId={catId}
        categories={categories}
        onSaved={loadItems}
      />
    </div>
  )
}
