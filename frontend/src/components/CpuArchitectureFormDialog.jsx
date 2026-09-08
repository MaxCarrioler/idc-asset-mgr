import { useState, useEffect } from 'react'
import { Modal, Form, Input, Button } from 'antd'
import { request, showError, showSuccess } from '../api'

export default function CpuArchitectureFormDialog({ open, onClose, archId, onSaved }) {
  const [form] = Form.useForm()
  const [saving, setSaving] = useState(false)

  const defaultValues = { code: '', name: '', description: '' }

  useEffect(() => {
    if (!open) return
    form.resetFields()
    if (archId) {
      request(`/api/cpu-architectures/${archId}`).then((arch) => {
        form.setFieldsValue({ code: arch.code || '', name: arch.name || '', description: arch.description || '' })
      }).catch((e) => showError(e))
    } else {
      form.setFieldsValue(defaultValues)
    }
  }, [open, archId])

  const handleSubmit = async (values) => {
    setSaving(true)
    try {
      const body = { code: values.code.trim(), name: values.name.trim(), description: values.description?.trim() || '' }
      if (archId) {
        await request(`/api/cpu-architectures/${archId}`, { method: 'PUT', body })
        showSuccess('更新成功')
      } else {
        await request('/api/cpu-architectures', { method: 'POST', body })
        showSuccess('创建成功')
      }
      onClose()
      onSaved?.()
    } catch (e) { showError(e) }
    finally { setSaving(false) }
  }

  return (
    <Modal title={archId ? '编辑 CPU 架构' : '新增 CPU 架构'} open={open} onCancel={onClose} width={560} destroyOnClose
      footer={[<Button key="cancel" onClick={onClose}>取消</Button>,
        <Button key="save" type="primary" loading={saving} onClick={() => form.submit()}>保存</Button>]}>
      <Form form={form} layout="vertical" onFinish={handleSubmit} initialValues={defaultValues}>
        <Form.Item label="架构编码" name="code" rules={[
          { required: true, message: '请输入架构编码' },
          { pattern: /^[A-Za-z0-9_]+$/, message: '编码仅支持字母、数字和下划线' }
        ]}>
          <Input disabled={!!archId} placeholder="如：RISC_V" />
        </Form.Item>
        <Form.Item label="架构名称" name="name" rules={[{ required: true, message: '请输入架构名称' }]}>
          <Input placeholder="如：RISC-V 64位" />
        </Form.Item>
        <Form.Item label="描述" name="description">
          <Input placeholder="架构说明" />
        </Form.Item>
      </Form>
    </Modal>
  )
}
