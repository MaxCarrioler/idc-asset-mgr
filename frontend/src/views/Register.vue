<template>
  <div class="register-page">
    <el-card class="register-card" shadow="hover">
      <h2>账号注册</h2>
      <p class="sub">提交后需超级管理员审批通过方可登录</p>
      <el-form :model="form" label-position="top" @submit.prevent="handleSubmit">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" autocomplete="username" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="显示名称（可选）" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="可选；本地演示可不填" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="form.password" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <el-form-item label="确认密码" required>
          <el-input v-model="form.confirmPassword" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <el-button type="primary" style="width: 100%" :loading="loading" native-type="submit">
          提交注册
        </el-button>
      </el-form>
      <div class="footer-link">
        已有账号？
        <router-link to="/login">去登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registerUser } from '../api/userCenter'
import { showError } from '../api'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

async function handleSubmit() {
  if (!form.username.trim() || !form.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  loading.value = true
  try {
    await registerUser({
      username: form.username.trim(),
      password: form.password,
      nickname: form.nickname.trim() || form.username.trim(),
      name: form.nickname.trim() || form.username.trim(),
      phone: form.phone.trim() || undefined
    })
    ElMessage.success('注册成功，请等待超级管理员审批后再登录')
    router.replace('/login')
  } catch (error) {
    showError(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 24px;
}

.register-card {
  width: 420px;
  border-radius: 12px;
}

.register-card h2 {
  margin: 0 0 4px;
  text-align: center;
}

.sub {
  text-align: center;
  color: #909399;
  margin: 0 0 20px;
}

.footer-link {
  margin-top: 16px;
  text-align: center;
  font-size: 14px;
  color: #606266;
}
</style>
