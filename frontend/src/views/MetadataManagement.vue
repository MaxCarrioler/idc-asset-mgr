<template>
  <div>
    <div class="toolbar">
      <h2>元数据管理</h2>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="服务器类型" name="serverTypes">
        <div class="panel-toolbar">
          <p class="panel-desc">定义服务器类型及统计规则，供服务器录入与数据统计使用</p>
          <el-button type="primary" @click="openCreateType">+ 新增类型</el-button>
        </div>
        <el-card shadow="never">
          <el-table :data="serverTypes" v-loading="typesLoading" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column label="类型编码" min-width="120">
              <template #default="{ row }">
                <el-tag type="info">{{ row.code }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="类型名称" min-width="120" />
            <el-table-column prop="description" label="描述" min-width="160" />
            <el-table-column label="统计线程" width="100">
              <template #default="{ row }">{{ row.trackThreads ? '是' : '否' }}</template>
            </el-table-column>
            <el-table-column label="统计硬盘" width="100">
              <template #default="{ row }">{{ row.includeDiskStats ? '是' : '否' }}</template>
            </el-table-column>
            <el-table-column label="来源" width="90">
              <template #default="{ row }">{{ row.builtIn ? '内置' : '自定义' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openEditType(row.id)">编辑</el-button>
                <el-button
                  v-if="!row.builtIn"
                  link
                  type="danger"
                  @click="handleDeleteType(row)"
                >删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="CPU 架构" name="cpuArchitectures">
        <div class="panel-toolbar">
          <p class="panel-desc">定义 CPU 架构元数据，供服务器录入与数据统计筛选使用</p>
          <el-button type="primary" @click="openCreateArch">+ 新增架构</el-button>
        </div>
        <el-card shadow="never">
          <el-table :data="cpuArchitectures" v-loading="archLoading" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column label="架构编码" min-width="120">
              <template #default="{ row }">
                <el-tag type="success">{{ row.code }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="架构名称" min-width="120" />
            <el-table-column prop="description" label="描述" min-width="180" />
            <el-table-column label="来源" width="90">
              <template #default="{ row }">{{ row.builtIn ? '内置' : '自定义' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openEditArch(row.id)">编辑</el-button>
                <el-button
                  v-if="!row.builtIn"
                  link
                  type="danger"
                  @click="handleDeleteArch(row)"
                >删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <ServerTypeFormDialog
      v-model="typeDialogVisible"
      :type-id="editingTypeId"
      @saved="loadServerTypes"
    />
    <CpuArchitectureFormDialog
      v-model="archDialogVisible"
      :arch-id="editingArchId"
      @saved="loadCpuArchitectures"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { request, showError, showSuccess } from '../api'
import ServerTypeFormDialog from '../components/ServerTypeFormDialog.vue'
import CpuArchitectureFormDialog from '../components/CpuArchitectureFormDialog.vue'

const activeTab = ref('serverTypes')

const serverTypes = ref([])
const cpuArchitectures = ref([])
const typesLoading = ref(false)
const archLoading = ref(false)

const typeDialogVisible = ref(false)
const editingTypeId = ref(null)
const archDialogVisible = ref(false)
const editingArchId = ref(null)

async function loadServerTypes() {
  typesLoading.value = true
  try {
    serverTypes.value = await request('/api/server-types')
  } catch (e) {
    showError(e)
    serverTypes.value = []
  } finally {
    typesLoading.value = false
  }
}

async function loadCpuArchitectures() {
  archLoading.value = true
  try {
    cpuArchitectures.value = await request('/api/cpu-architectures')
  } catch (e) {
    showError(e)
    cpuArchitectures.value = []
  } finally {
    archLoading.value = false
  }
}

function openCreateType() {
  editingTypeId.value = null
  typeDialogVisible.value = true
}

function openEditType(id) {
  editingTypeId.value = id
  typeDialogVisible.value = true
}

async function handleDeleteType(row) {
  try {
    await ElMessageBox.confirm(`确认删除服务器类型「${row.name}」？`, '提示', { type: 'warning' })
    await request(`/api/server-types/${row.id}`, { method: 'DELETE' })
    showSuccess('删除成功')
    loadServerTypes()
  } catch (e) {
    if (e !== 'cancel') showError(e)
  }
}

function openCreateArch() {
  editingArchId.value = null
  archDialogVisible.value = true
}

function openEditArch(id) {
  editingArchId.value = id
  archDialogVisible.value = true
}

async function handleDeleteArch(row) {
  try {
    await ElMessageBox.confirm(`确认删除 CPU 架构「${row.name}」？`, '提示', { type: 'warning' })
    await request(`/api/cpu-architectures/${row.id}`, { method: 'DELETE' })
    showSuccess('删除成功')
    loadCpuArchitectures()
  } catch (e) {
    if (e !== 'cancel') showError(e)
  }
}

onMounted(() => {
  loadServerTypes()
  loadCpuArchitectures()
})
</script>

<style scoped>
.toolbar h2 {
  margin: 0 0 16px;
  font-size: 20px;
}

.panel-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.panel-desc {
  margin: 0;
  color: #909399;
  font-size: 13px;
}
</style>
