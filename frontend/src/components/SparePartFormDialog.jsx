import { useState, useEffect, useMemo } from 'react'
import { Modal, Form, Input, Select, Button } from 'antd'
import { request, showError, showSuccess } from '../api'

export default function SparePartFormDialog({ open, onClose, itemId, categoryId, categories, onSaved }) {
  const [form] = Form.useForm()
  const [saving, setSaving] = useState(false)
  const [servers, setServers] = useState([])

  const categoryName = useMemo(() => {
    const matched = categories.find((c) => c.id === Number(categoryId))
    return matched?.name || ''
  }, [categories, categoryId])

  const defaultValues = { serialNumber: '', manufacturer: '', specSummary: '', location: '', sourceKey: '', remark: '' }

  const resolveSourceKey = (item) => {
    if (item?.sourceServerId) return `ID:${item.sourceServerId}`
    if (item?.sourceServerSerial === '无') return 'NONE'
    if (item?.sourceServerSerial === '采购') return 'PURCHASE'
    if (item?.sourceServerSerial) {
      const matched = servers.find((s) => s.serialNumber === item.sourceServerSerial)
      if (matched) return `ID:${matched.id}`
    }
    return ''
  }

  const buildSourcePayload = (sourceKey) => {
    if (sourceKey === 'NONE') return { sourceServerId: null, sourceServerSerial: '无', sourceServerName: null }
    if (sourceKey === 'PURCHASE') return { sourceServerId: null, sourceServerSerial: '采购', sourceServerName: null }
    if (sourceKey?.startsWith('ID:')) {
      const id = Number(sourceKey.slice(3))
      const server = servers.find((s) => s.id === id)
      if (!server) throw new Error('所选来源服务器不存在')
      return { sourceServerId: server.id, sourceServerSerial: server.serialNumber || '', sourceServerName: server.serverName || server.name || null }
    }
    throw new Error('请选择来源服务器')
  }

  useEffect(() => {
    if (!open) return
    form.resetFields()
    const loadServers = async () => {
      try {
        const data = await request('/api/servers', { params: { page: 1, size: 1000 } })
        setServers(data?.records || data?.content || data || [])
      } catch (e) { setServers([]); showError(e) }
    }
    loadServers().then(async () => {
      if (itemId) {
        try {
          const item = await request(`/api/spare-parts/items/${itemId}`)
          form.setFieldsValue({
            serialNumber: item.serialNumber || '', manufacturer: item.manufacturer || '',
            specSummary: item.specSummary || '', location: item.location || '',
            sourceKey: resolveSourceKey(item), remark: item.remark || ''
          })
        } catch (e) { showError(e) }
      } else {
        form.setFieldsValue(defaultValues)
      }
    })
  }, [open, itemId])

  const handleSubmit = async (values) => {
    setSaving(true)
    try {
      const source = buildSourcePayload(values.sourceKey)
      const body = {
        categoryId: Number(categoryId), serialNumber: values.serialNumber.trim(),
        manufacturer: values.manufacturer?.trim(), specSummary: values.specSummary?.trim(),
        location: values.location?.trim(), remark: values.remark?.trim(), ...source
      }
      if (itemId) {
        await request(`/api/spare-parts/items/${itemId}`, { method: 'PUT', body })
        showSuccess('更新成功')
      } else {
        await request('/api/spare-parts/items', { method: 'POST', body })
        showSuccess('创建成功')
      }
      onClose()
      onSaved?.()
    } catch (e) { showError(e) }
    finally { setSaving(false) }
  }

  return (
    <Modal title={itemId ? '编辑备件' : '新增备件'} open={open} onCancel={onClose} width={620} destroyOnClose
      footer={[<Button key="cancel" onClick={onClose}>取消</Button>,
        <Button key="save" type="primary" loading={saving} onClick={() => form.submit()}>保存</Button>]}>
      <Form form={form} layout="vertical" onFinish={handleSubmit} initialValues={defaultValues}>
        <Form.Item label="备件种类"><Input value={categoryName} disabled /></Form.Item>
        <Form.Item label="序列号" name="serialNumber" rules={[{ required: true, message: '请输入序列号' }]}>
          <Input placeholder="唯一标识，一件备件一条记录" />
        </Form.Item>
        <Form.Item label="厂商/品牌" name="manufacturer"><Input placeholder="如：Samsung" /></Form.Item>
        <Form.Item label="规格摘要" name="specSummary"><Input placeholder="可选，如：1.8TB NVMe SSD" /></Form.Item>
        <Form.Item label="存放位置" name="location"><Input placeholder="如：库房B-01" /></Form.Item>
        <Form.Item label="来源服务器" name="sourceKey" rules={[{ required: true, message: '请选择来源服务器' }]}>
          <Select showSearch optionFilterProp="label" allowClear placeholder="必选：无 / 采购 / 服务器"
            options={[
              { label: '无', value: 'NONE' },
              { label: '采购', value: 'PURCHASE' },
              ...servers.map((s) => ({
                label: `${s.serialNumber || '-'}（${s.serverName || s.name || '未命名'}）`,
                value: `ID:${s.id}`
              }))
            ]} />
        </Form.Item>
        <Form.Item label="备注" name="remark"><Input.TextArea rows={2} placeholder="可选" /></Form.Item>
      </Form>
    </Modal>
  )
}
