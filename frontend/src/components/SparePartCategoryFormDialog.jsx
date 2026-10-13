import { useState, useEffect } from 'react'
import { Modal, Form, Input, Select, Button } from 'antd'
import { request, showError, showSuccess } from '../api'
import { SPARE_PART_TYPES } from '../config/accessory'

export default function SparePartCategoryFormDialog({ open, onClose, categoryId, defaultAccessoryType, onSaved }) {
  const [form] = Form.useForm()
  const [saving, setSaving] = useState(false)

  const defaultValues = { code: '', name: '', description: '', accessoryType: defaultAccessoryType || '' }

  useEffect(() => {
    if (!open) return
    form.resetFields()
    if (categoryId) {
      request(`/api/spare-part-categories/${categoryId}`).then((cat) => {
        form.setFieldsValue({
          code: cat.code || '', name: cat.name || '', description: cat.description || '',
          accessoryType: cat.accessoryType || ''
        })
      }).catch((e) => showError(e))
    } else {
      form.setFieldsValue({ ...defaultValues, accessoryType: defaultAccessoryType || '' })
    }
  }, [open, categoryId, defaultAccessoryType])

  const handleSubmit = async (values) => {
    setSaving(true)
    try {
      const body = {
        code: values.code.trim(), name: values.name.trim(),
        description: values.description?.trim() || '',
        accessoryType: values.accessoryType || null
      }
      if (categoryId) {
        await request(`/api/spare-part-categories/${categoryId}`, { method: 'PUT', body })
        showSuccess('更新成功')
      } else {
        await request('/api/spare-part-categories', { method: 'POST', body })
        showSuccess('创建成功')
      }
      onClose()
      onSaved?.()
    } catch (e) { showError(e) }
    finally { setSaving(false) }
  }

  return (
    <Modal title={categoryId ? '编辑备件种类' : '新增备件种类'} open={open} onCancel={onClose} width={560} destroyOnClose
      footer={[<Button key="cancel" onClick={onClose}>取消</Button>,
        <Button key="save" type="primary" loading={saving} onClick={() => form.submit()}>保存</Button>]}>
      <Form form={form} layout="vertical" onFinish={handleSubmit} initialValues={defaultValues}>
        <Form.Item label="配件类型" name="accessoryType" rules={[{ required: true, message: '请选择配件类型' }]}>
          <Select placeholder="选择配件类型" options={SPARE_PART_TYPES.map(t => ({ label: t.label, value: t.type }))} />
        </Form.Item>
        <Form.Item label="种类编码" name="code" rules={[
          { required: true, message: '请输入种类编码' },
          { pattern: /^[A-Za-z0-9_]+$/, message: '编码仅支持字母、数字和下划线' }
        ]}>
          <Input disabled={!!categoryId} placeholder="如：HDD_1_8T" />
        </Form.Item>
        <Form.Item label="种类名称" name="name" rules={[{ required: true, message: '请输入种类名称' }]}>
          <Input placeholder="如：1.8T 硬盘" />
        </Form.Item>
        <Form.Item label="描述" name="description">
          <Input placeholder="种类说明" />
        </Form.Item>
      </Form>
    </Modal>
  )
}
