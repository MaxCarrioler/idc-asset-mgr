<template>
  <div class="login-page">
    <el-card class="login-card" shadow="hover">
      <h2>IDC 基础设施管理系统</h2>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="admin / accessory_admin / user" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="默认 123456"
            autocomplete="current-password"
          />
        </el-form-item>
        <el-button type="primary" style="width: 100%" :loading="loading" native-type="submit">
          登录
        </el-button>
      </el-form>
      <div class="footer-link">
        没有账号？
        <router-link to="/register">去注册</router-link>
      </div>
      <div class="tips">
        <p>演示账号（密码均为 123456）：</p>
        <ul>
          <li><b>admin</b> — 全局管理员（用户审批 / 角色管理）</li>
          <li><b>accessory_admin</b> — 配件管理员</li>
          <li><b>user</b> — 普通用户</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: '123456'
})

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await login(form.username.trim(), form.password)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/servers'
    router.replace(String(redirect))
  } catch (error) {
    const msg = error.response?.data?.error_description
      || error.response?.data?.msg
      || error.message
      || '登录失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 24px;
}

.login-card {
  width: 420px;
  border-radius: 12px;
}

.login-card h2 {
  margin: 0 0 24px;
  text-align: center;
  color: #1a1a2e;
}

.tips {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  font-size: 13px;
  color: #606266;
}

.footer-link {
  margin-top: 14px;
  text-align: center;
  font-size: 14px;
  color: #606266;
}

.tips ul {
  margin: 8px 0 0;
  padding-left: 18px;
}
</style>
