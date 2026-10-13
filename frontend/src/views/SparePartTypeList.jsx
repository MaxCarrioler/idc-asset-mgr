import { useState, useEffect, useMemo } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { Card, Table, Button, Tag, Modal, Empty, message } from 'antd'
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

export default function SparePartTypeList() {
  const { type } = useParams()
  const navigate = useNavigate()
  const { hasAnyRole, hasPermission } = useAuth()
  const isAdmin = hasAnyRole('ROLE_ADMIN')
  const canEdit = hasPermission('spare_part:edit')
  const canDelete = hasPermission('spare_part:delete')
  const typeLabel = SPARE_PART_TYPE_MAP[type] || type

  const [items, setItems] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(false)
  const [itemDialogVisible, setItemDialogVisible] = useState(false)
  const [editingItemId, setEditingItemId] = useState(null)
  const [categoryFilter, setCategoryFilter] = useState(null)

  const loadData = async () => {
    setLoading(true)
    try {
      const [itemsData, catsData] = await Promise.all([
        request(`/api/spare-parts/items/by-type/${type}`) || [],
        request(`/api/spare-parts/by-type/${type}`) || []
      ])
      setItems(itemsData)
      setCategories(catsData)
    } catch (e) { showError(e); setItems([]); setCategories([]) }
    finally { setLoading(false) }
  }

  useEffect(() => { loadData() }, [type])

  const filteredItems = useMemo(() => {
    if (!categoryFilter) return items
    return items.filter(i => i.categoryId === categoryFilter)
  }, [items, categoryFilter])

  const inStockCount = useMemo(() => filteredItems.filter(i => !i.status || i.status === 'IN_STOCK').length, [filteredItems])
  const outCount = useMemo(() => filteredItems.filter(i => i.status === 'OUT').length, [filteredItems])

  const handleDeleteItem = (row) => {
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
          loadData()
        } catch (e) { showError(e) }
      }
    })
  }

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 70 },
    { title: '种类', dataIndex: 'categoryName', width: 130, render: (v) => v || '-' },
    { title: '序列号', dataIndex: 'serialNumber', width: 140 },
    { title: '厂商/品牌', dataIndex: 'manufacturer', width: 110, render: (v) => v || '-' },
    { title: '规格摘要', dataIndex: 'specSummary', width: 150, render: (v) => v || '-' },
    { title: '存放位置', dataIndex: 'location', width: 100, render: (v) => v || '-' },
    {
      title: '状态', dataIndex: 'status', width: 90,
      render: (s) => <Tag color={statusTagType(s)}>{statusLabel(s)}</Tag>
    },
    {
      title: '来源服务器', width: 150,
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
      title: '现役服务器', width: 150,
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
    { title: '备注', dataIndex: 'remark', width: 110, render: (v) => v || '-' },
    {
      title: '操作', width: 140, fixed: 'right',
      render: (_, row) => {
        const locked = row.status === 'OUT' || row.status === 'RESERVED'
        return (
          <span>
            {canEdit && (
              <Button type="link" disabled={locked}
                onClick={() => { setEditingItemId(row.id); setItemDialogVisible(true) }}>编辑</Button>
            )}
            {canDelete && (
              <Button type="link" danger disabled={!isAdmin && locked}
                onClick={() => handleDeleteItem(row)}>删除</Button>
            )}
          </span>
        )
      }
    }
  ]

  return (
    <div>
      <header style={{ marginBottom: 20 }}>
        <Link to="/spare-parts/cpu" style={{ color: '#1677ff', fontSize: 14 }}>← 返回备件统计</Link>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginTop: 12 }}>
          <div>
            <h1 style={{ margin: '0 0 8px', fontSize: 24 }}>{typeLabel} 备件明细</h1>
            <p style={{ margin: 0, color: '#909399', fontSize: 13 }}>
              共 {filteredItems.length} 条记录，在库 {inStockCount} 件，已出库 {outCount} 件
              {categoryFilter && (
                <Button type="link" size="small" onClick={() => setCategoryFilter(null)}>取消筛选</Button>
              )}
            </p>
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            {hasPermission('spare_part:category_manage') && (
              <Button onClick={() => navigate(`/spare-parts/${type}/categories`)}>种类管理</Button>
            )}
            {canEdit && (
              <Button type="primary" onClick={() => {
                if (categories.length === 0) {
                  message.warning('请先在"种类管理"中创建至少一个种类')
                  return
                }
                setEditingItemId(null)
                setItemDialogVisible(true)
              }}>+ 新增备件</Button>
            )}
          </div>
        </div>
      </header>

      {categories.length > 1 && (
        <Card size="small" style={{ marginBottom: 16 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
            <span style={{ fontSize: 13, color: '#606266' }}>种类：</span>
            <Tag
              color={categoryFilter ? 'default' : 'blue'}
              style={{ cursor: 'pointer' }}
              onClick={() => setCategoryFilter(null)}
            >全部</Tag>
            {categories.map(cat => (
              <Tag
                key={cat.categoryId}
                color={categoryFilter === cat.categoryId ? 'blue' : 'default'}
                style={{ cursor: 'pointer' }}
                onClick={() => setCategoryFilter(cat.categoryId)}
              >{cat.typeName} ({cat.totalCount})</Tag>
            ))}
          </div>
        </Card>
      )}

      <Card>
        <Table
          dataSource={filteredItems}
          columns={columns}
          rowKey="id"
          loading={loading}
          pagination={false}
          scroll={{ x: 1500 }}
          size="middle"
        />
      </Card>

      <SparePartFormDialog
        open={itemDialogVisible}
        onClose={() => setItemDialogVisible(false)}
        itemId={editingItemId}
        categories={categories.map(c => ({ id: c.categoryId, name: c.typeName }))}
        onSaved={loadData}
      />
    </div>
  )
}
