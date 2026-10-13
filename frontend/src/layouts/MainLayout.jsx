import { useState, useMemo, Suspense } from 'react'
import { Outlet, useNavigate, useLocation, Link } from 'react-router-dom'
import { Layout, Menu, Tag, Button, Skeleton } from 'antd'
import {
  MonitorOutlined,
  AppstoreOutlined,
  InboxOutlined,
  SendOutlined,
  UserOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from '@ant-design/icons'
import { useAuth } from '../stores/AuthContext'
import { SPARE_PART_TYPES } from '../config/accessory'

const { Sider, Content, Header } = Layout

export default function MainLayout() {
  const [collapsed, setCollapsed] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const { user, hasPermission, hasAnyRole, logout } = useAuth()

  const activeMenu = useMemo(() => {
    const p = location.pathname
    if (/^\/spare-parts\/[^/]+\/[^/]+$/.test(p)) {
      const match = p.match(/^\/spare-parts\/([^/]+)\/[^/]+$/)
      if (match) return `/spare-parts/${match[1]}`
    }
    if (/^\/spare-parts\/[^/]+$/.test(p)) {
      return p
    }
    if (/^\/servers\/[^/]+\/(accessories|records)$/.test(p)) return '/servers'
    if (/^\/metadata\/[^/]+$/.test(p)) return p
    return p
  }, [location.pathname])

  const displayName = user?.nickname || user?.username || '未登录'
  const roleLabel = (user?.roles || []).join(', ') || '无角色'

  const canSeeAccessoryCenter = hasPermission('accessory:transfer:view', 'accessory:transfer:apply')
  const canSeeSpareParts = hasPermission('spare_part:view')
  const canSeeUserCenter = hasAnyRole('ROLE_ADMIN') || hasPermission('sys_user_view', 'sys_role_view')
  const canSeeRoleMenu = hasAnyRole('ROLE_ADMIN') || hasPermission('sys_role_view')

  const menuItems = useMemo(() => {
    const items = [
      {
        key: 'serverStats',
        icon: <AppstoreOutlined />,
        label: '服务器统计',
        children: [
          { key: '/servers', label: '服务器列表' },
          { key: '/statistics', label: '数据统计' },
          { key: '/metadata/server-types', label: '服务器类型' },
          { key: '/metadata/cpu-architectures', label: 'CPU 架构' },
          { key: '/metadata/hard-disk-types', label: '服务器硬盘类型' }
        ]
      }
    ]

    if (canSeeSpareParts) {
      items.push({
        key: 'spareParts',
        icon: <InboxOutlined />,
        label: '备件统计',
        children: [
          ...SPARE_PART_TYPES.map(t => ({ key: `/spare-parts/${t.type}`, label: t.label }))
        ]
      })
    }

    if (canSeeAccessoryCenter) {
      items.push({
        key: 'accessoryCenter',
        icon: <SendOutlined />,
        label: '配件中心',
        children: [
          { key: '/accessory-center/requests', label: '申请列表' },
          { key: '/accessory-center/transfers', label: '流转记录' }
        ]
      })
    }

    if (canSeeUserCenter) {
      const userCenterChildren = [
        { key: '/user-center/users', label: '用户管理' }
      ]
      if (canSeeRoleMenu) {
        userCenterChildren.push({ key: '/user-center/roles', label: '角色管理' })
      }
      items.push({
        key: 'userCenter',
        icon: <UserOutlined />,
        label: '用户中心',
        children: userCenterChildren
      })
    }

    return items
  }, [canSeeAccessoryCenter, canSeeUserCenter, canSeeRoleMenu])

  const handleMenuClick = ({ key }) => {
    navigate(key)
  }

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <div style={{ display: 'flex', height: '100vh' }}>
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
        width={220}
        trigger={null}
        style={{ background: '#1f2937' }}
      >
        <div style={{
          height: 60,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 10,
          color: '#fff',
          fontSize: 16,
          fontWeight: 600,
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          borderBottom: '1px solid rgba(255,255,255,0.08)'
        }}>
          <MonitorOutlined style={{ fontSize: 22 }} />
          {!collapsed && <span>IDC管理系统</span>}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[activeMenu]}
          defaultOpenKeys={['serverStats', 'spareParts', 'accessoryCenter', 'userCenter']}
          items={menuItems}
          onClick={handleMenuClick}
          style={{ background: '#1f2937', borderRight: 'none' }}
        />
      </Sider>

      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0, background: '#f5f7fa' }}>
        <Header style={{
          height: 60,
          background: '#fff',
          borderBottom: '1px solid #e4e7ed',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '0 24px'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
            {collapsed
              ? <MenuUnfoldOutlined style={{ cursor: 'pointer', fontSize: 20 }} onClick={() => setCollapsed(false)} />
              : <MenuFoldOutlined style={{ cursor: 'pointer', fontSize: 20 }} onClick={() => setCollapsed(true)} />
            }
            <h1 style={{ margin: 0, fontSize: 18, color: '#1a1a2e' }}>IDC基础设施管理系统</h1>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, color: '#606266' }}>
            <span>{displayName}</span>
            <Tag>{roleLabel}</Tag>
            <Button type="link" danger onClick={handleLogout}>退出</Button>
          </div>
        </Header>

        <Content style={{ flex: 1, overflowY: 'auto', padding: 24 }}>
          <Suspense fallback={
            <div style={{ padding: 24 }}>
              <Skeleton active paragraph={{ rows: 6 }} />
            </div>
          }>
            <div key={location.pathname} className="page-fade-in">
              <Outlet />
            </div>
          </Suspense>
        </Content>
      </div>
    </div>
  )
}
