import { useState, useEffect } from 'react'
import { Modal, Form, Input, Button } from 'antd'
import { request, showError, showSuccess } from '../api'

export default function HardDiskTypeFormDialog({ open, onClose, diskTypeId, onSaved }) {
  const [form] = Form.useForm()
  const [saving, setSaving] = useState(false)

  const defaultValues = { code: '', name: '', description: '' }

  useEffect(() => {
    if (!open) return
    form.resetFields()
    if (diskTypeId) {
      request(`/api/hard-disk-types/${diskTypeId}`).then((diskType) => {
        form.setFieldsValue({ code: diskType.code || '', name: diskType.name || '', description: diskType.description || '' })
      }).catch((e) => showError(e))
    } else {
      form.setFieldsValue(defaultValues)
    }
  }, [open, diskTypeId])

  const handleSubmit = async (values) => {
    setSaving(true)
    try {
      const body = { code: values.code.trim(), name: values.name.trim(), description: values.description?.trim() || '' }
      if (diskTypeId) {
        await request(`/api/hard-disk-types/${diskTypeId}`, { method: 'PUT', body })
        showSuccess('更新成功')
      } else {
        await request('/api/hard-disk-types', { method: 'POST', body })
        showSuccess('创建成功')
      }
      onClose()
      onSaved?.()
    } catch (e) { showError(e) }
    finally { setSaving(false) }
  }

  return (
    <Modal title={diskTypeId ? '编辑硬盘类型' : '新增硬盘类型'} open={open} onCancel={onClose} width={560} destroyOnClose
      footer={[<Button key="cancel" onClick={onClose}>取消</Button>,
        <Button key="save" type="primary" loading={saving} onClick={() => form.submit()}>保存</Button>]}>
      <Form form={form} layout="vertical" onFinish={handleSubmit} initialValues={defaultValues}>
        <Form.Item label="类型编码" name="code" rules={[
          { required: true, message: '请输入类型编码' },
          { pattern: /^[A-Za-z0-9_]+$/, message: '编码仅支持字母、数字和下划线' }
        ]}>
          <Input disabled={!!diskTypeId} placeholder="如：NVME" />
        </Form.Item>
        <Form.Item label="类型名称" name="name" rules={[{ required: true, message: '请输入类型名称' }]}>
          <Input placeholder="如：NVMe 固态硬盘" />
        </Form.Item>
        <Form.Item label="描述" name="description">
          <Input placeholder="硬盘类型说明" />
        </Form.Item>
      </Form>
    </Modal>
  )
}
