import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { Card, Table, Button, Tag, Modal, Empty } from 'antd'
import { request, showError, showSuccess } from '../api'
import { SPARE_PART_TYPE_MAP } from '../config/accessory'
import SparePartCategoryFormDialog from '../components/SparePartCategoryFormDialog'
import { useAuth } from '../stores/AuthContext'

export default function SparePartCategoryList() {
  const { type } = useParams()
  const { hasPermission } = useAuth()
  const canManage = hasPermission('spare_part:category_manage')
  const typeLabel = SPARE_PART_TYPE_MAP[type] || type

  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(false)
  const [dialogVisible, setDialogVisible] = useState(false)
  const [editingCategoryId, setEditingCategoryId] = useState(null)

  const loadCategories = async () => {
    setLoading(true)
    try {
      setCategories(await request(`/api/spare-parts/by-type/${type}`) || [])
    } catch (e) { showError(e); setCategories([]) }
    finally { setLoading(false) }
  }

  useEffect(() => { loadCategories() }, [type])

  const handleDelete = (row) => {
    Modal.confirm({
      title: '提示',
      content: `确认删除备件种类「${row.typeName}」？${row.totalCount > 0 ? `该种类下还有 ${row.totalCount} 件备件，需先清空才能删除。` : ''}`,
      okButtonProps: { danger: true },
      onOk: async () => {
        try {
          await request(`/api/spare-part-categories/${row.categoryId}`, { method: 'DELETE' })
          showSuccess('删除成功')
          loadCategories()
        } catch (e) { showError(e) }
      }
    })
  }

  const columns = [
    { title: 'ID', dataIndex: 'categoryId', width: 70 },
    { title: '种类编码', dataIndex: 'categoryCode', width: 180, render: (v) => <Tag>{v}</Tag> },
    { title: '种类名称', dataIndex: 'typeName', width: 200 },
    {
      title: '在库数量', dataIndex: 'totalCount', width: 100, align: 'center',
      render: (v) => <span style={{ fontWeight: 600, color: '#1677ff' }}>{v}</span>
    },
    {
      title: '已出库', dataIndex: 'outCount', width: 90, align: 'center',
      render: (v) => v > 0 ? <span style={{ color: '#909399' }}>{v}</span> : '-'
    },
    {
      title: '记录总数', dataIndex: 'recordCount', width: 90, align: 'center'
    },
    {
      title: '操作', width: 150, fixed: 'right',
      render: (_, row) => canManage ? (
        <span>
          <Button type="link" onClick={() => { setEditingCategoryId(row.categoryId); setDialogVisible(true) }}>编辑</Button>
          <Button type="link" danger onClick={() => handleDelete(row)}>删除</Button>
        </span>
      ) : null
    }
  ]

  const totalInStock = categories.reduce((sum, c) => sum + c.totalCount, 0)
  const totalOut = categories.reduce((sum, c) => sum + c.outCount, 0)

  return (
    <div>
      <header style={{ marginBottom: 20 }}>
        <Link to={`/spare-parts/${type}`} style={{ color: '#1677ff', fontSize: 14 }}>← 返回{typeLabel}备件明细</Link>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginTop: 12 }}>
          <div>
            <h1 style={{ margin: '0 0 8px', fontSize: 24 }}>{typeLabel} 种类管理</h1>
            <p style={{ margin: 0, color: '#909399', fontSize: 13 }}>
              共 {categories.length} 个种类，在库 {totalInStock} 件，已出库 {totalOut} 件
            </p>
          </div>
          {canManage && (
            <Button type="primary" onClick={() => { setEditingCategoryId(null); setDialogVisible(true) }}>
              + 新增种类
            </Button>
          )}
        </div>
      </header>

      {loading ? (
        <div style={{ textAlign: 'center', padding: 40 }}><span>Loading...</span></div>
      ) : !categories.length ? (
        <Card>
          <Empty description={`暂无${typeLabel}备件种类，请点击「新增种类」添加`} />
        </Card>
      ) : (
        <Card>
          <Table
            dataSource={categories}
            columns={columns}
            rowKey="categoryId"
            pagination={false}
            scroll={{ x: 900 }}
            size="middle"
          />
        </Card>
      )}

      <SparePartCategoryFormDialog
        open={dialogVisible}
        onClose={() => setDialogVisible(false)}
        categoryId={editingCategoryId}
        defaultAccessoryType={type}
        onSaved={loadCategories}
      />
    </div>
  )
}
