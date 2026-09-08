import { useState, useEffect } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { Card, Table, Button, Tag, Modal, Empty } from 'antd'
import { request, showError, showSuccess } from '../api'
import SparePartCategoryFormDialog from '../components/SparePartCategoryFormDialog'

export default function SparePartsStats() {
  const location = useLocation()
  const navigate = useNavigate()
  const activeTab = location.pathname.endsWith('/categories') ? 'categories' : 'summary'

  const [summaries, setSummaries] = useState([])
  const [categories, setCategories] = useState([])
  const [summaryLoading, setSummaryLoading] = useState(false)
  const [categoryLoading, setCategoryLoading] = useState(false)
  const [categoryDialogVisible, setCategoryDialogVisible] = useState(false)
  const [editingCategoryId, setEditingCategoryId] = useState(null)

  const loadSummary = async () => {
    setSummaryLoading(true)
    try {
      setSummaries(await request('/api/spare-parts/summary') || [])
    } catch (e) { showError(e); setSummaries([]) }
    finally { setSummaryLoading(false) }
  }

  const loadCategories = async () => {
    setCategoryLoading(true)
    try {
      setCategories(await request('/api/spare-part-categories') || [])
    } catch (e) { showError(e); setCategories([]) }
    finally { setCategoryLoading(false) }
  }

  useEffect(() => {
    loadSummary()
    loadCategories()
  }, [])

  const handleDeleteCategory = (row) => {
    Modal.confirm({
      title: '提示', content: `确认删除备件种类「${row.name}」？`,
      onOk: async () => {
        try {
          await request(`/api/spare-part-categories/${row.id}`, { method: 'DELETE' })
          showSuccess('删除成功')
          loadSummary()
          loadCategories()
        } catch (e) { showError(e) }
      }
    })
  }

  const categoryColumns = [
    { title: 'ID', dataIndex: 'id', width: 70 },
    { title: '种类编码', dataIndex: 'code', render: (v) => <Tag>{v}</Tag> },
    { title: '种类名称', dataIndex: 'name' },
    { title: '描述', dataIndex: 'description', ellipsis: true },
    { title: '来源', dataIndex: 'builtIn', width: 90, render: (v) => v ? '内置' : '自定义' },
    {
      title: '操作', width: 150, fixed: 'right',
      render: (_, row) => (
        <span>
          <Button type="link" onClick={() => { setEditingCategoryId(row.id); setCategoryDialogVisible(true) }}>编辑</Button>
          {!row.builtIn && <Button type="link" danger onClick={() => handleDeleteCategory(row)}>删除</Button>}
        </span>
      )
    }
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
        <h2 style={{ margin: 0, fontSize: 20 }}>备件统计</h2>
        <Button onClick={() => { loadSummary(); loadCategories() }}>刷新数据</Button>
      </div>

      {activeTab === 'summary' ? (
        <>
          <p style={{ margin: '0 0 20px', color: '#909399', fontSize: 13 }}>
            每种规格单独统计，一件备件一条记录。卡片数字为在库数量（配件中心出库后会减少）；点击查看明细可看到「已出库」标注
          </p>
          {summaryLoading ? (
            <div style={{ textAlign: 'center', padding: 40 }}><span>Loading...</span></div>
          ) : !summaries.length ? (
            <Empty description="暂无备件种类，请先在「种类管理」中新增" />
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 16, minHeight: 120 }}>
              {summaries.map((item) => (
                <Card
                  key={item.categoryId}
                  hoverable
                  onClick={() => navigate(`/spare-parts/${item.categoryId}`)}
                  style={{ cursor: 'pointer' }}
                >
                  <div style={{ textAlign: 'center', padding: '8px 0' }}>
                    <h3 style={{ margin: '0 0 12px', fontSize: 16, color: '#303133' }}>{item.typeName}</h3>
                    <div style={{ fontSize: 36, fontWeight: 700, color: '#1677ff', lineHeight: 1.2 }}>{item.totalCount}</div>
                    <p style={{ margin: '8px 0 0', color: '#909399', fontSize: 13 }}>在库数量</p>
                    {item.outCount > 0 && (
                      <p style={{ margin: '4px 0 0', color: '#909399', fontSize: 12 }}>已出库 {item.outCount}</p>
                    )}
                  </div>
                  <div style={{ marginTop: 12, paddingTop: 12, borderTop: '1px solid #ebeef5', textAlign: 'center', color: '#1677ff', fontSize: 13 }}>
                    查看明细 →
                  </div>
                </Card>
              ))}
            </div>
          )}
        </>
      ) : (
        <>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <p style={{ margin: 0, color: '#909399', fontSize: 13 }}>每种规格/型号单独建类，如「1.8T 硬盘」「8T 硬盘」。明细中以序列号区分每一件备件</p>
            <Button type="primary" onClick={() => { setEditingCategoryId(null); setCategoryDialogVisible(true) }}>+ 新增种类</Button>
          </div>
          <Card>
            <Table dataSource={categories} columns={categoryColumns} rowKey="id" loading={categoryLoading} pagination={false} size="middle" />
          </Card>
        </>
      )}

      <SparePartCategoryFormDialog
        open={categoryDialogVisible}
        onClose={() => setCategoryDialogVisible(false)}
        categoryId={editingCategoryId}
        onSaved={() => { loadSummary(); loadCategories() }}
      />
    </div>
  )
}
