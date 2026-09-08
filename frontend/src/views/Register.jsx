import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { Card, Form, Input, Button, message } from 'antd'
import { registerUser } from '../api/userCenter'
import { showError } from '../api'

export default function Register() {
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleSubmit = async (values) => {
    if (!values.username?.trim() || !values.password) {
      message.warning('请填写用户名和密码')
      return
    }
    if (values.password !== values.confirmPassword) {
      message.warning('两次密码不一致')
      return
    }
    setLoading(true)
    try {
      await registerUser({
        username: values.username.trim(),
        password: values.password,
        nickname: values.nickname?.trim() || values.username.trim(),
        name: values.nickname?.trim() || values.username.trim(),
        phone: values.phone?.trim() || undefined
      })
      message.success('注册成功，请等待超级管理员审批后再登录')
      navigate('/login', { replace: true })
    } catch (error) {
      showError(error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)',
      padding: 24
    }}>
      <Card style={{ width: 420, borderRadius: 12 }} hoverable>
        <h2 style={{ margin: '0 0 4px', textAlign: 'center' }}>账号注册</h2>
        <p style={{ textAlign: 'center', color: '#909399', margin: '0 0 20px' }}>
          提交后需超级管理员审批通过方可登录
        </p>
        <Form layout="vertical" onFinish={handleSubmit}>
          <Form.Item label="用户名" name="username" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input autoComplete="username" placeholder="登录用户名" />
          </Form.Item>
          <Form.Item label="昵称" name="nickname">
            <Input placeholder="显示名称（可选）" />
          </Form.Item>
          <Form.Item label="手机号" name="phone">
            <Input placeholder="可选；本地演示可不填" />
          </Form.Item>
          <Form.Item label="密码" name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password autoComplete="new-password" />
          </Form.Item>
          <Form.Item label="确认密码" name="confirmPassword" rules={[{ required: true, message: '请确认密码' }]}>
            <Input.Password autoComplete="new-password" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              提交注册
            </Button>
          </Form.Item>
        </Form>
        <div style={{ marginTop: 16, textAlign: 'center', fontSize: 14, color: '#606266' }}>
          已有账号？<Link to="/login">去登录</Link>
        </div>
      </Card>
    </div>
  )
}
