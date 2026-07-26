<template>
  <div class="page-container">
    <header class="page-header">
      <router-link to="/servers" class="back-link">← 返回服务器列表</router-link>
      <h1>{{ pageTitle }}</h1>
      <div v-if="server" class="server-info">
        <span>Region：<strong>{{ server.region || '-' }}</strong></span>
        <span>机架位置：<strong>{{ server.rackLocation || '-' }}</strong></span>
        <span>型号：<strong>{{ server.serverModel || '-' }}</strong></span>
        <span>CPU架构：<strong>{{ resolveCpuArchitectureLabel(server.cpuArchitecture, cpuArchitectures) }}</strong></span>
        <span>更新时间：<strong>{{ formatDateTime(server.updatedAt) }}</strong></span>
      </div>
    </header>

    <el-card shadow="never">
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column label="修改时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.modifiedAt) }}</template>
        </el-table-column>
        <el-table-column prop="changeContent" label="变更内容" min-width="300" />
        <el-table-column prop="changeReason" label="变更原因" width="220" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { request, showError } from '../api'
import { formatDateTime, resolveCpuArchitectureLabel } from '../utils/format'

const route = useRoute()
const serverId = route.params.id

const server = ref(null)
const cpuArchitectures = ref([])
const records = ref([])
const loading = ref(false)
const pageTitle = ref('操作记录')

async function loadData() {
  if (!serverId) return
  loading.value = true
  try {
    const [archList, serverData, recordData] = await Promise.all([
      request('/api/cpu-architectures'),
      request(`/api/servers/${serverId}`),
      request(`/api/servers/${serverId}/operation-records`)
    ])
    cpuArchitectures.value = archList || []
    server.value = serverData
    records.value = recordData || []
    pageTitle.value = `${serverData.serverName} - 操作记录`
  } catch (e) {
    showError(e)
    records.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 20px;
}

.back-link {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.back-link:hover {
  text-decoration: underline;
}

.page-header h1 {
  margin: 12px 0;
  font-size: 24px;
}

.server-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  color: #606266;
  font-size: 14px;
}
</style>
