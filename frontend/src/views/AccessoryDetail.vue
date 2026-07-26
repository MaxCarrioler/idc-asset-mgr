<template>
  <div class="page-container">
    <header class="page-header">
      <router-link to="/servers" class="back-link">← 返回服务器列表</router-link>
      <h1>{{ pageTitle }}</h1>
      <div v-if="server" class="server-info">
        <span>Region：<strong>{{ server.region || '-' }}</strong></span>
        <span>IP：<strong>{{ server.ipAddress || '-' }}</strong></span>
        <span>机架：<strong>{{ server.rackLocation || '-' }}</strong></span>
        <span>型号：<strong>{{ server.serverModel || '-' }}</strong></span>
        <span>CPU架构：<strong>{{ resolveCpuArchitectureLabel(server.cpuArchitecture, cpuArchitectures) }}</strong></span>
        <span>厂商：<strong>{{ server.manufacturer || '-' }}</strong></span>
        <span>
          状态：
          <el-tag :type="statusTagType(server.status)" size="small">
            {{ getStatusLabel(server.status) }}
          </el-tag>
        </span>
      </div>
    </header>

    <el-tabs v-model="currentType" @tab-change="loadAccessories">
      <el-tab-pane
        v-for="type in accessoryTypes"
        :key="type"
        :label="ACCESSORY_CONFIG[type].title"
        :name="type"
      />
    </el-tabs>

    <div class="toolbar">
      <h2>{{ config.title }} 配件</h2>
      <el-button type="primary" @click="openCreate">+ 新增配件</el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="accessories" v-loading="loading" stripe>
        <el-table-column
          v-for="col in config.columns"
          :key="col.key"
          :label="col.label"
          :prop="col.key"
        >
          <template #default="{ row }">
            {{ col.render ? col.render(row[col.key]) : (row[col.key] ?? '-') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row.id)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <AccessoryFormDialog
      v-model="dialogVisible"
      :config="config"
      :server-id="serverId"
      :editing-id="editingId"
      @saved="loadAccessories"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { request, showError, showSuccess } from '../api'
import { getStatusLabel, resolveCpuArchitectureLabel } from '../utils/format'
import { ACCESSORY_CONFIG, ACCESSORY_TYPES } from '../config/accessory'
import AccessoryFormDialog from '../components/AccessoryFormDialog.vue'

const route = useRoute()
const serverId = route.params.id

const server = ref(null)
const cpuArchitectures = ref([])
const pageTitle = ref('服务器配件详情')
const currentType = ref('cpus')
const accessories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)

const accessoryTypes = ACCESSORY_TYPES
const config = computed(() => ACCESSORY_CONFIG[currentType.value])

function statusTagType(status) {
  const map = { RUNNING: 'success', STOPPED: 'info', MAINTENANCE: 'warning' }
  return map[status] || 'info'
}

async function loadServerInfo() {
  try {
    server.value = await request(`/api/servers/${serverId}`)
    pageTitle.value = `${server.value.serverName} - 配件详情`
  } catch (e) {
    showError(e)
  }
}

async function loadAccessories() {
  loading.value = true
  try {
    accessories.value = await request(`${config.value.apiPath}?serverId=${serverId}`)
  } catch (e) {
    showError(e)
    accessories.value = []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  dialogVisible.value = true
}

function openEdit(id) {
  editingId.value = id
  dialogVisible.value = true
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确认移除此配件？', '提示', { type: 'warning' })
    await request(`${config.value.apiPath}/${id}`, { method: 'DELETE' })
    showSuccess('移除成功')
    loadAccessories()
  } catch (e) {
    if (e !== 'cancel') showError(e)
  }
}

onMounted(async () => {
  try {
    cpuArchitectures.value = await request('/api/cpu-architectures')
  } catch (e) {
    showError(e)
  }
  await loadServerInfo()
  await loadAccessories()
})
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

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 16px 0;
}

.toolbar h2 {
  margin: 0;
  font-size: 18px;
}
</style>
