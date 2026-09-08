import { Suspense } from 'react'
import { RouterProvider } from 'react-router-dom'
import { Spin } from 'antd'
import { router } from './router'

const FullPageLoader = () => (
  <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
    <Spin size="large" />
  </div>
)

export default function App() {
  return (
    <Suspense fallback={<FullPageLoader />}>
      <RouterProvider router={router} />
    </Suspense>
  )
}
