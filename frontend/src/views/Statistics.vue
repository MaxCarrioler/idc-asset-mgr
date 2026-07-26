<template>
  <div>
    <div class="toolbar">
      <h2>硬件资源汇总</h2>
      <el-button @click="loadStatistics">刷新数据</el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <div class="filter-item">
          <label>Region 区域</label>
          <el-select
            v-model="selectedRegions"
            multiple
            clearable
            collapse-tags
            collapse-tags-tooltip
            filterable
            placeholder="全部区域"
            style="width: 100%"
            @change="loadStatistics"
          >
            <el-option
              v-for="region in regionOptions"
              :key="region"
              :label="region"
              :value="region"
            />
          </el-select>
        </div>
        <div class="filter-item">
          <label>服务器类型</label>
          <el-select
            v-model="selectedTypeIds"
            multiple
            clearable
            collapse-tags
            collapse-tags-tooltip
            filterable
            placeholder="全部类型"
            style="width: 100%"
            @change="loadStatistics"
          >
            <el-option
              v-for="type in serverTypeOptions"
              :key="type.id"
              :label="`${type.name} (${type.code})`"
              :value="type.id"
            />
          </el-select>
        </div>
        <div class="filter-item">
          <label>CPU 架构</label>
          <el-select
            v-model="selectedCpuArchitectures"
            multiple
            clearable
            collapse-tags
            collapse-tags-tooltip
            filterable
            placeholder="全部架构"
            style="width: 100%"
            @change="loadStatistics"
          >
            <el-option
              v-for="arch in cpuArchitectureOptions"
              :key="arch.code"
              :label="`${arch.name} (${arch.code})`"
              :value="arch.code"
            />
          </el-select>
        </div>
      </div>
      <p class="filter-desc">默认无筛选，展示全量汇总；勾选任意条件后将实时重新聚合统计</p>
    </el-card>

    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="serverCount" label="服务器数量" width="110" align="center" fixed />
        <el-table-column prop="totalCores" label="CPU核心总数" width="120" align="center" />
        <el-table-column prop="totalThreads" label="线程总数" width="110" align="center" />
        <el-table-column label="内存总容量" width="130" align="center">
          <template #default="{ row }">{{ formatMemory(row.totalMemoryGb) }}</template>
        </el-table-column>
        <el-table-column
          v-for="spec in DISK_SPEC_COLUMNS"
          :key="spec"
          :label="`${spec} 物理总容量`"
          min-width="140"
          align="center"
        >
          <template #default="{ row }">
            {{ formatSpecCapacity(row, spec) }}
          </template>
        </el-table-column>
        <el-table-column label="硬盘物理总容量" width="150" align="center" fixed="right">
          <template #default="{ row }">{{ formatTb(row.totalDiskCapacityTb) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { request, showError } from '../api'
import { formatMemory, formatTb } from '../utils/format'

const regionOptions = ref([])
const serverTypeOptions = ref([])
const cpuArchitectureOptions = ref([])

const selectedRegions = ref([])
const selectedTypeIds = ref([])
const selectedCpuArchitectures = ref([])

const DISK_SPEC_COLUMNS = ['1.8T', '2.4T', '3.84T', '4T', '8T']

const statistics = ref(null)
const loading = ref(false)

const tableData = computed(() => {
  if (!statistics.value) return []
  return [statistics.value]
})

async function loadFilterOptions() {
  const [regions, serverTypes, cpuArchitectures] = await Promise.all([
    request('/api/servers/regions'),
    request('/api/server-types'),
    request('/api/cpu-architectures')
  ])
  regionOptions.value = regions || []
  serverTypeOptions.value = serverTypes || []
  cpuArchitectureOptions.value = cpuArchitectures || []
}

function buildParams() {
  const params = {}
  if (selectedRegions.value.length) {
    params.regions = selectedRegions.value.join(',')
  }
  if (selectedTypeIds.value.length) {
    params.serverTypeIds = selectedTypeIds.value.join(',')
  }
  if (selectedCpuArchitectures.value.length) {
    params.cpuArchitectures = selectedCpuArchitectures.value.join(',')
  }
  return params
}

async function loadStatistics() {
  loading.value = true
  try {
    statistics.value = await request('/api/statistics/hardware', { params: buildParams() })
  } catch (e) {
    showError(e)
    statistics.value = null
  } finally {
    loading.value = false
  }
}

function formatSpecCapacity(row, spec) {
  const item = row.diskSpecSummaries?.find((d) => d.spec === spec)
  if (!item || item.totalCapacityTb == null) return '0 TB'
  return formatTb(item.totalCapacityTb)
}

async function init() {
  try {
    await loadFilterOptions()
    await loadStatistics()
  } catch (e) {
    showError(e)
  }
}

onMounted(init)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar h2 {
  margin: 0;
  font-size: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.filter-item label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.filter-desc {
  margin: 12px 0 0;
  color: #909399;
  font-size: 13px;
}

@media (max-width: 960px) {
  .filter-row {
    grid-template-columns: 1fr;
  }
}
</style>
