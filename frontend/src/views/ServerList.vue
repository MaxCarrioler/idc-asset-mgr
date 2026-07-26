<template>
  <div>
    <div class="toolbar">
      <div class="search-box">
        <el-input
          v-model="keyword"
          placeholder="搜索服务器名称、Region、序列号、型号、CPU架构、机架位置..."
          clearable
          style="width: 400px"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      <el-button type="primary" @click="openCreate">+ 新增服务器</el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="servers" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="服务器名称" min-width="140">
          <template #default="{ row }">
            <router-link :to="`/servers/${row.id}/accessories`" class="link">
              {{ row.serverName }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column prop="region" label="Region名称" min-width="110" />
        <el-table-column prop="serialNumber" label="序列号" min-width="120" />
        <el-table-column prop="rackLocation" label="机架位置" min-width="100" />
        <el-table-column prop="serverModel" label="型号" min-width="100" />
        <el-table-column label="CPU架构" min-width="100">
          <template #default="{ row }">
            {{ resolveCpuArchitectureLabel(row.cpuArchitecture, cpuArchitectures) }}
          </template>
        </el-table-column>
        <el-table-column prop="manufacturer" label="厂商" min-width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="osType" label="操作系统" min-width="100" />
        <el-table-column label="更新时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作记录" width="100">
          <template #default="{ row }">
            <router-link :to="`/servers/${row.id}/records`" class="link">查看记录</router-link>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row.id)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next, total"
          @current-change="loadServers"
        />
      </div>
    </el-card>

    <ServerFormDialog
      v-model="dialogVisible"
      :server-id="editingId"
      @saved="handleSaved"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { request, showError, showSuccess } from '../api'
import { getStatusLabel, formatDateTime, resolveCpuArchitectureLabel } from '../utils/format'
import ServerFormDialog from '../components/ServerFormDialog.vue'

const keyword = ref('')
const servers = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = 10
const total = ref(0)
const dialogVisible = ref(false)
const editingId = ref(null)
const cpuArchitectures = ref([])

function statusTagType(status) {
  const map = { RUNNING: 'success', STOPPED: 'info', MAINTENANCE: 'warning' }
  return map[status] || 'info'
}

async function loadServers(page = currentPage.value) {
  loading.value = true
  currentPage.value = page
  try {
    const params = { page, size: pageSize }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    const data = await request('/api/servers', { params })
    servers.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    showError(e)
    servers.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadServers(1)
}

function handleReset() {
  keyword.value = ''
  loadServers(1)
}

function openCreate() {
  editingId.value = null
  dialogVisible.value = true
}

function openEdit(id) {
  editingId.value = id
  dialogVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除服务器「${row.serverName}」？`, '提示', { type: 'warning' })
    await request(`/api/servers/${row.id}`, { method: 'DELETE' })
    showSuccess('删除成功')
    loadServers(currentPage.value)
  } catch (e) {
    if (e !== 'cancel') showError(e)
  }
}

function handleSaved() {
  loadServers(currentPage.value)
}

onMounted(async () => {
  try {
    cpuArchitectures.value = await request('/api/cpu-architectures')
  } catch (e) {
    showError(e)
  }
  loadServers(1)
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.search-box {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.link {
  color: #409eff;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}
</style>
