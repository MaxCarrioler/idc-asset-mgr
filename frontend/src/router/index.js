import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import { getAccessToken, fetchCurrentUser, hasPermission, clearAuth } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue'),
      meta: { public: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/Register.vue'),
      meta: { public: true }
    },
    {
      path: '/',
      component: MainLayout,
      redirect: '/servers',
      children: [
        { path: 'servers', name: 'servers', component: () => import('../views/ServerList.vue') },
        { path: 'statistics', name: 'statistics', component: () => import('../views/Statistics.vue') },
        { path: 'metadata', name: 'metadata', component: () => import('../views/MetadataManagement.vue') },
        { path: 'spare-parts', name: 'spareParts', component: () => import('../views/SparePartsStats.vue') },
        {
          path: 'accessory-center',
          name: 'accessoryCenter',
          component: () => import('../views/AccessoryCenter.vue'),
          meta: { permissions: ['accessory:transfer:view', 'accessory:transfer:apply'] }
        },
        {
          path: 'user-center',
          name: 'userCenter',
          component: () => import('../views/UserCenter.vue'),
          meta: { permissions: ['sys_user_view', 'sys_role_view'] }
        },
        { path: 'user-center/users', redirect: '/user-center' },
        { path: 'user-center/roles', redirect: '/user-center' },
        { path: 'server-types', redirect: '/metadata' }
      ]
    },
    {
      path: '/spare-parts/:categoryId',
      name: 'sparePartDetail',
      component: () => import('../views/SparePartDetail.vue')
    },
    {
      path: '/servers/:id/accessories',
      name: 'accessories',
      component: () => import('../views/AccessoryDetail.vue')
    },
    {
      path: '/servers/:id/records',
      name: 'records',
      component: () => import('../views/OperationRecords.vue')
    }
  ]
})

router.beforeEach(async (to) => {
  if (to.meta.public) {
    if (getAccessToken() && (to.path === '/login' || to.path === '/register')) {
      return '/servers'
    }
    return true
  }
  if (!getAccessToken()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  try {
    await fetchCurrentUser()
  } catch {
    clearAuth()
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  const required = to.meta.permissions || []
  if (required.length && !hasPermission(...required)) {
    return '/servers'
  }
  return true
})

export default router
