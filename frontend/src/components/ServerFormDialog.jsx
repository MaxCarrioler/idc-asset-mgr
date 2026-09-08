import { useState, useEffect } from 'react'
import { Modal, Form, Input, Select, Button, Row, Col, message } from 'antd'
import { request, showError, showSuccess } from '../api'

export default function ServerFormDialog({ open, onClose, serverId, onSaved }) {
  const [form] = Form.useForm()
  const [saving, setSaving] = useState(false)
  const [serverTypes, setServerTypes] = useState([])
  const [cpuArchitectures, setCpuArchitectures] = useState([])

  const defaultValues = {
    serverName: '', serverType: 'COMPUTE', ipAddress: '', rackLocation: '',
    region: '', serverModel: '', manufacturer: '', serialNumber: '',
    status: 'RUNNING', osType: '', cpuArchitecture: '', remark: '', changeReason: ''
  }

  useEffect(() => {
    if (!open) return
    form.resetFields()
    Promise.all([
      request('/api/server-types').then((d) => setServerTypes(d || [])),
      request('/api/cpu-architectures').then((d) => setCpuArchitectures(d || []))
    ]).then(async () => {
      if (serverId) {
        try {
          const server = await request(`/api/servers/${serverId}`)
          form.setFieldsValue({
            serverName: server.serverName || '', serverType: server.serverType || 'COMPUTE',
            ipAddress: server.ipAddress || '', rackLocation: server.rackLocation || '',
            region: server.region || '', serverModel: server.serverModel || '',
            manufacturer: server.manufacturer || '', serialNumber: server.serialNumber || '',
            status: server.status || 'RUNNING', osType: server.osType || '',
            cpuArchitecture: server.cpuArchitecture || '', remark: server.remark || '',
            changeReason: ''
          })
        } catch (e) { showError(e) }
      } else {
        form.setFieldsValue(defaultValues)
      }
    }).catch((e) => showError(e))
  }, [open, serverId])

  const handleSubmit = async (values) => {
    setSaving(true)
    try {
      if (serverId) {
        await request(`/api/servers/${serverId}`, { method: 'PUT', body: values })
        showSuccess('更新成功')
      } else {
        const { changeReason, ...body } = values
        await request('/api/servers', { method: 'POST', body })
        showSuccess('创建成功')
      }
      onClose()
      onSaved?.()
    } catch (e) { showError(e) }
    finally { setSaving(false) }
  }

  return (
    <Modal title={serverId ? '编辑服务器' : '新增服务器'} open={open} onCancel={onClose} width={720} destroyOnClose
      footer={[<Button key="cancel" onClick={onClose}>取消</Button>,
        <Button key="save" type="primary" loading={saving} onClick={() => form.submit()}>保存</Button>]}>
      <Form form={form} layout="vertical" onFinish={handleSubmit} initialValues={defaultValues}>
        <Row gutter={16}>
          <Col span={12}>
            <Form.Item label="服务器名称" name="serverName" rules={[{ required: true, message: '请输入服务器名称' }]}>
              <Input />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="服务器类型" name="serverType" rules={[{ required: true, message: '请选择服务器类型' }]}>
              <Select options={serverTypes.map((t) => ({ label: `${t.name} (${t.code})`, value: t.code }))} />
            </Form.Item>
          </Col>
          <Col span={12}><Form.Item label="IP地址" name="ipAddress"><Input /></Form.Item></Col>
          <Col span={12}><Form.Item label="机架位置" name="rackLocation"><Input /></Form.Item></Col>
          <Col span={12}><Form.Item label="Region名称" name="region"><Input placeholder="如：华北-北京" /></Form.Item></Col>
          <Col span={12}><Form.Item label="服务器型号" name="serverModel"><Input /></Form.Item></Col>
          <Col span={12}><Form.Item label="厂商" name="manufacturer"><Input /></Form.Item></Col>
          <Col span={12}><Form.Item label="序列号" name="serialNumber"><Input /></Form.Item></Col>
          <Col span={12}>
            <Form.Item label="状态" name="status">
              <Select options={[{ label: '运行中', value: 'RUNNING' }, { label: '已停止', value: 'STOPPED' }, { label: '维护中', value: 'MAINTENANCE' }]} />
            </Form.Item>
          </Col>
          <Col span={12}><Form.Item label="操作系统" name="osType"><Input /></Form.Item></Col>
          <Col span={12}>
            <Form.Item label="CPU架构" name="cpuArchitecture">
              <Select allowClear placeholder="请选择"
                options={cpuArchitectures.map((a) => ({ label: `${a.name} (${a.code})`, value: a.code }))} />
            </Form.Item>
          </Col>
          <Col span={24}><Form.Item label="备注" name="remark"><Input /></Form.Item></Col>
          {serverId && (
            <Col span={24}>
              <Form.Item label="变更原因" name="changeReason">
                <Input placeholder="请填写本次修改的原因" />
              </Form.Item>
            </Col>
          )}
        </Row>
      </Form>
    </Modal>
  )
}
