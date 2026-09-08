import { useState, useEffect } from 'react'
import { Modal, Form, Input, Checkbox, Button, message } from 'antd'
import { request, showError, showSuccess } from '../api'

export default function ServerTypeFormDialog({ open, onClose, typeId, onSaved }) {
  const [form] = Form.useForm()
  const [saving, setSaving] = useState(false)

  const defaultValues = { code: '', name: '', description: '', trackThreads: false, includeDiskStats: false }

  useEffect(() => {
    if (!open) return
    form.resetFields()
    if (typeId) {
      request(`/api/server-types/${typeId}`).then((type) => {
        form.setFieldsValue({
          code: type.code || '', name: type.name || '', description: type.description || '',
          trackThreads: !!type.trackThreads, includeDiskStats: !!type.includeDiskStats
        })
      }).catch((e) => showError(e))
    } else {
      form.setFieldsValue(defaultValues)
    }
  }, [open, typeId])

  const handleSubmit = async (values) => {
    setSaving(true)
    try {
      const body = { code: values.code.trim(), name: values.name.trim(), description: values.description?.trim() || '', trackThreads: !!values.trackThreads, includeDiskStats: !!values.includeDiskStats }
      if (typeId) {
        await request(`/api/server-types/${typeId}`, { method: 'PUT', body })
        showSuccess('更新成功')
      } else {
        await request('/api/server-types', { method: 'POST', body })
        showSuccess('创建成功')
      }
      onClose()
      onSaved?.()
    } catch (e) { showError(e) }
    finally { setSaving(false) }
  }

  return (
    <Modal title={typeId ? '编辑服务器类型' : '新增服务器类型'} open={open} onCancel={onClose} width={560} destroyOnClose
      footer={[<Button key="cancel" onClick={onClose}>取消</Button>,
        <Button key="save" type="primary" loading={saving} onClick={() => form.submit()}>保存</Button>]}>
      <Form form={form} layout="vertical" onFinish={handleSubmit} initialValues={defaultValues}>
        <Form.Item label="类型编码" name="code" rules={[
          { required: true, message: '请输入类型编码' },
          { pattern: /^[A-Za-z0-9_]+$/, message: '编码仅支持字母、数字和下划线' }
        ]}>
          <Input disabled={!!typeId} placeholder="如：GPU_NODE" />
        </Form.Item>
        <Form.Item label="类型名称" name="name" rules={[{ required: true, message: '请输入类型名称' }]}>
          <Input placeholder="如：GPU计算节点" />
        </Form.Item>
        <Form.Item label="描述" name="description">
          <Input placeholder="类型用途说明" />
        </Form.Item>
        <Form.Item name="trackThreads" valuePropName="checkbox">
          <Checkbox>统计 CPU 线程数</Checkbox>
        </Form.Item>
        <Form.Item name="includeDiskStats" valuePropName="checkbox">
          <Checkbox>统计硬盘容量及规格</Checkbox>
        </Form.Item>
      </Form>
    </Modal>
  )
}
