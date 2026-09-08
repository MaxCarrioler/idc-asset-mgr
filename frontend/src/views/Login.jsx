import { useState } from 'react'
import { useNavigate, useSearchParams, Link } from 'react-router-dom'
import { Card, Form, Input, Button, message } from 'antd'
import { useAuth } from '../stores/AuthContext'

export default function Login() {
  const [loading, setLoading] = useState(false)
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { login } = useAuth()

  const handleLogin = async (values) => {
    if (!values.username || !values.password) {
      message.warning('请输入用户名和密码')
      return
    }
    setLoading(true)
    try {
      await login(values.username.trim(), values.password)
      message.success('登录成功')
      const redirect = searchParams.get('redirect') || '/servers'
      navigate(redirect, { replace: true })
    } catch (error) {
      const msg = error.response?.data?.error_description
        || error.response?.data?.msg
        || error.message
        || '登录失败'
      message.error(msg)
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
        <h2 style={{ margin: '0 0 24px', textAlign: 'center', color: '#1a1a2e' }}>
          IDC 基础设施管理系统
        </h2>
        <Form
          initialValues={{ username: 'admin', password: '123456' }}
          onFinish={handleLogin}
        >
          <Form.Item label="用户名" name="username">
            <Input placeholder="admin / accessory_admin / user" autoComplete="username" />
          </Form.Item>
          <Form.Item label="密码" name="password">
            <Input.Password placeholder="默认 123456" autoComplete="current-password" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              登录
            </Button>
          </Form.Item>
        </Form>
        <div style={{ marginTop: 14, textAlign: 'center', fontSize: 14, color: '#606266' }}>
          没有账号？<Link to="/register">去注册</Link>
        </div>
        <div style={{ marginTop: 20, paddingTop: 16, borderTop: '1px solid #ebeef5', fontSize: 13, color: '#606266' }}>
          <p>演示账号（密码均为 123456）：</p>
          <ul style={{ margin: '8px 0 0', paddingLeft: 18 }}>
            <li><b>admin</b> — 全局管理员（用户审批 / 角色管理）</li>
            <li><b>accessory_admin</b> — 配件管理员</li>
            <li><b>user</b> — 普通用户</li>
          </ul>
        </div>
      </Card>
    </div>
  )
}
