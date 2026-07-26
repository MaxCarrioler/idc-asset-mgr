<template>
  <div class="page-container">
    <header class="page-header">
      <h1>IDC基础设施管理系统</h1>
      <div class="user-box">
        <span>{{ displayName }}</span>
        <el-tag size="small" type="info">{{ roleLabel }}</el-tag>
        <el-button link type="danger" @click="handleLogout">退出</el-button>
      </div>
    </header>

    <el-menu
      :default-active="activeMenu"
      mode="horizontal"
      router
      class="main-nav"
    >
      <el-menu-item index="/servers">服务器列表</el-menu-item>
      <el-menu-item index="/statistics">数据统计</el-menu-item>
      <el-menu-item index="/metadata">元数据管理</el-menu-item>
      <el-menu-item index="/spare-parts">备件统计</el-menu-item>
      <el-menu-item v-if="canSeeAccessoryCenter" index="/accessory-center">配件中心</el-menu-item>
      <el-menu-item v-if="canSeeUserCenter" index="/user-center">用户中心</el-menu-item>
    </el-menu>

    <main class="page-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { currentUser, hasPermission, hasAnyRole, logout } from '../stores/auth'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => {
  if (route.path.startsWith('/spare-parts')) return '/spare-parts'
  if (route.path.startsWith('/accessory-center')) return '/accessory-center'
  if (route.path.startsWith('/user-center')) return '/user-center'
  return route.path
})

const displayName = computed(() => currentUser.value?.nickname || currentUser.value?.username || '未登录')
const roleLabel = computed(() => (currentUser.value?.roles || []).join(', ') || '无角色')
const canSeeAccessoryCenter = computed(() => hasPermission('accessory:transfer:view', 'accessory:transfer:apply'))
const canSeeUserCenter = computed(() =>
  hasAnyRole('ROLE_ADMIN') || hasPermission('sys_user_view', 'sys_role_view')
)

async function handleLogout() {
  await logout()
  router.replace('/login')
}
</script>

<style scoped>
.page-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-header h1 {
  margin: 0;
  font-size: 28px;
  color: #1a1a2e;
}

.user-box {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #606266;
}

.main-nav {
  margin-bottom: 20px;
  border-radius: 8px;
}

.page-main {
  min-height: 400px;
}
</style>
