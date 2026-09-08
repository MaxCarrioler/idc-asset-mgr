import { useState, useEffect } from 'react'
import { Modal, Form, Input, InputNumber, Select, Button, Row, Col } from 'antd'
import { request, showError, showSuccess } from '../api'

export default function AccessoryFormDialog({ open, onClose, config, serverId, editingId, onSaved }) {
  const [form] = Form.useForm()
  const [saving, setSaving] = useState(false)

  const buildDefaultForm = (data = {}) => {
    const values = {}
    config.fields.forEach((field) => {
      let val = data[field.key]
      if (field.type === 'boolean') values[field.key] = val === true
      else if (field.type === 'number') values[field.key] = val ?? undefined
      else values[field.key] = val ?? ''
    })
    return values
  }

  useEffect(() => {
    if (!open) return
    form.resetFields()
    if (editingId) {
      request(`${config.apiPath}/${editingId}`).then((item) => {
        form.setFieldsValue(buildDefaultForm(item))
      }).catch((e) => showError(e))
    } else {
      form.setFieldsValue(buildDefaultForm())
    }
  }, [open, editingId, config])

  const collectBody = (values) => {
    const body = { serverId: Number(serverId) }
    config.fields.forEach((field) => {
      let val = values[field.key]
      if (field.type === 'number' && (val === undefined || val === '')) val = null
      body[field.key] = val
    })
    return body
  }

  const handleSubmit = async (values) => {
    setSaving(true)
    try {
      const body = collectBody(values)
      if (editingId) {
        await request(`${config.apiPath}/${editingId}`, { method: 'PUT', body })
        showSuccess('更新成功')
      } else {
        await request(config.apiPath, { method: 'POST', body })
        showSuccess('新增成功')
      }
      onClose()
      onSaved?.()
    } catch (e) { showError(e) }
    finally { setSaving(false) }
  }

  return (
    <Modal title={editingId ? `编辑 ${config.title}` : `新增 ${config.title}`} open={open} onCancel={onClose} width={720} destroyOnClose
      footer={[<Button key="cancel" onClick={onClose}>取消</Button>,
        <Button key="save" type="primary" loading={saving} onClick={() => form.submit()}>保存</Button>]}>
      <Form form={form} layout="vertical" onFinish={handleSubmit}>
        <Row gutter={16}>
          {config.fields.map((field) => (
            <Col span={12} key={field.key}>
              <Form.Item
                label={field.label}
                name={field.key}
                rules={field.required ? [{ required: true, message: `请填写${field.label}` }] : []}
              >
                {field.type === 'boolean' ? (
                  <Select options={[{ label: '是', value: true }, { label: '否', value: false }]} />
                ) : field.type === 'number' ? (
                  <InputNumber step={field.step || 1} style={{ width: '100%' }} />
                ) : (
                  <Input />
                )}
              </Form.Item>
            </Col>
          ))}
        </Row>
      </Form>
    </Modal>
  )
}
