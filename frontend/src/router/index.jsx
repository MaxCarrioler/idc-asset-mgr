import { lazy, useEffect, useState } from 'react'
import { createBrowserRouter, Navigate } from 'react-router-dom'
import { Spin } from 'antd'
import MainLayout from '../layouts/MainLayout'
import {
  getAccessTokenStandalone,
  clearAuthStandalone,
  redirectToLogin,
  useAuth
} from '../stores/AuthContext'

// Lazy-loaded page components
const Login = lazy(() => import('../views/Login'))
const Register = lazy(() => import('../views/Register'))
const ServerList = lazy(() => import('../views/ServerList'))
const Statistics = lazy(() => import('../views/Statistics'))
const MetadataManagement = lazy(() => import('../views/MetadataManagement'))
const SparePartTypeList = lazy(() => import('../views/SparePartTypeList'))
const SparePartCategoryList = lazy(() => import('../views/SparePartCategoryList'))
const SparePartDetail = lazy(() => import('../views/SparePartDetail'))
const AccessoryCenter = lazy(() => import('../views/AccessoryCenter'))
const AccessoryDetail = lazy(() => import('../views/AccessoryDetail'))
const OperationRecords = lazy(() => import('../views/OperationRecords'))
const UserCenter = lazy(() => import('../views/UserCenter'))

/**
 * Protected route wrapper — only checks auth ONCE on mount.
 * After initial verification, route changes are instant (no re-fetch / no spinner).
 */
function ProtectedRoute() {
  const { getAccessToken, fetchCurrentUser } = useAuth()
  const [status, setStatus] = useState('loading')

  useEffect(() => {
    let cancelled = false
    const token = getAccessToken()
    if (!token) {
      redirectToLogin()
      return
    }
    fetchCurrentUser()
      .then(() => { if (!cancelled) setStatus('ready') })
      .catch(() => {
        clearAuthStandalone()
        redirectToLogin()
      })
    return () => { cancelled = true }
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  if (status === 'loading') {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" />
      </div>
    )
  }

  return <MainLayout />
}

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <Login />
  },
  {
    path: '/register',
    element: <Register />
  },
  {
    path: '/',
    element: <ProtectedRoute />,
    children: [
      { index: true, element: <Navigate to="/servers" replace /> },
      { path: 'servers', element: <ServerList /> },
      { path: 'statistics', element: <Statistics /> },
      {
        path: 'metadata',
        children: [
          { index: true, element: <Navigate to="/metadata/server-types" replace /> },
          { path: 'server-types', element: <MetadataManagement /> },
          { path: 'cpu-architectures', element: <MetadataManagement /> },
          { path: 'hard-disk-types', element: <MetadataManagement /> }
        ]
      },
      {
        path: 'spare-parts',
        children: [
          { index: true, element: <Navigate to="/spare-parts/cpu" replace /> },
          { path: ':type', element: <SparePartTypeList /> },
          { path: ':type/categories', element: <SparePartCategoryList /> },
          { path: ':type/:categoryId', element: <SparePartDetail /> }
        ]
      },
      {
        path: 'accessory-center',
        children: [
          { index: true, element: <Navigate to="/accessory-center/requests" replace /> },
          { path: 'requests', element: <AccessoryCenter /> },
          { path: 'transfers', element: <AccessoryCenter /> }
        ]
      },
      {
        path: 'user-center',
        children: [
          { index: true, element: <Navigate to="/user-center/users" replace /> },
          { path: 'users', element: <UserCenter /> },
          { path: 'roles', element: <UserCenter /> }
        ]
      },
      { path: 'servers/:id/accessories', element: <AccessoryDetail /> },
      { path: 'servers/:id/records', element: <OperationRecords /> },
      { path: 'server-types', element: <Navigate to="/metadata/server-types" replace /> }
    ]
  }
])
