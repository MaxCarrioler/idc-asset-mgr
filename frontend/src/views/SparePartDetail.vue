<template>
  <div class="page-container">
    <header class="page-header">
      <router-link to="/spare-parts" class="back-link">← 返回备件统计</router-link>
      <div class="header-row">
        <div>
          <h1>{{ typeName }} 备件明细</h1>
          <p class="page-desc">
            共 {{ items.length }} 条记录，在库 {{ inStockCount }} 件，已出库 {{ outCount }} 件
            <span v-if="reservedCount">，已预留 {{ reservedCount }} 件</span>
          </p>
        </div>
        <el-button type="primary" @click="openCreate">+ 新增备件</el-button>
      </div>
    </header>

    <el-card shadow="never">
      <el-table :data="items" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="serialNumber" label="序列号" min-width="140" />
        <el-table-column prop="manufacturer" label="厂商/品牌" min-width="110">
          <template #default="{ row }">{{ row.manufacturer || '-' }}</template>
        </el-table-column>
        <el-table-column prop="specSummary" label="规格摘要" min-width="160">
          <template #default="{ row }">{{ row.specSummary || '-' }}</template>
        </el-table-column>
        <el-table-column prop="location" label="存放位置" min-width="110">
          <template #default="{ row }">{{ row.location || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="来源服务器" min-width="160">
          <template #default="{ row }">
            <template v-if="row.sourceServerSerial === '无' || row.sourceServerSerial === '采购'">
              {{ row.sourceServerSerial }}
            </template>
            <template v-else-if="row.sourceServerName || row.sourceServerSerial">
              <div>{{ row.sourceServerName || row.sourceServerSerial }}</div>
              <div
                v-if="row.sourceServerName && row.sourceServerSerial"
                class="sub-text"
              >{{ row.sourceServerSerial }}</div>
            </template>
            <template v-else>-</template>
          </template>
        </el-table-column>
        <el-table-column label="现役服务器" min-width="160">
          <template #default="{ row }">
            <template v-if="row.activeServerName || row.activeServerSerial">
              <div>{{ row.activeServerName || '-' }}</div>
              <div v-if="row.activeServerSerial" class="sub-text">{{ row.activeServerSerial }}</div>
            </template>
            <template v-else>-</template>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120">
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              :disabled="row.status === 'OUT' || row.status === 'RESERVED'"
              @click="openEdit(row.id)"
            >编辑</el-button>
            <el-button
              link
              type="danger"
              :disabled="row.status === 'OUT' || row.status === 'RESERVED'"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <SparePartFormDialog
      v-model="dialogVisible"
      :item-id="editingItemId"
      :category-id="categoryId"
      :categories="categories"
      @saved="loadItems"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { request, showError, showSuccess } from '../api'
import SparePartFormDialog from '../components/SparePartFormDialog.vue'

const route = useRoute()
const categoryId = computed(() => Number(route.params.categoryId))

const items = ref([])
const categories = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const editingItemId = ref(null)

const typeName = computed(() => {
  const matched = categories.value.find((c) => c.id === categoryId.value)
  return matched?.name || `种类#${categoryId.value}`
})

const inStockCount = computed(() =>
  items.value.filter((i) => !i.status || i.status === 'IN_STOCK').length
)
const outCount = computed(() => items.value.filter((i) => i.status === 'OUT').length)
const reservedCount = computed(() => items.value.filter((i) => i.status === 'RESERVED').length)

function statusLabel(status) {
  switch (status) {
    case 'OUT':
      return '已出库'
    case 'RESERVED':
      return '已预留'
    case 'IN_STOCK':
    default:
      return '在库'
  }
}

function statusTagType(status) {
  switch (status) {
    case 'OUT':
      return 'info'
    case 'RESERVED':
      return 'warning'
    case 'IN_STOCK':
    default:
      return 'success'
  }
}

async function loadCategories() {
  try {
    categories.value = await request('/api/spare-part-categories')
  } catch (e) {
    showError(e)
    categories.value = []
  }
}

async function loadItems() {
  loading.value = true
  try {
    items.value = await request(`/api/spare-parts/items?categoryId=${categoryId.value}`) || []
  } catch (e) {
    showError(e)
    items.value = []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingItemId.value = null
  dialogVisible.value = true
}

function openEdit(id) {
  editingItemId.value = id
  dialogVisible.value = true
}

async function handleDelete(row) {
  if (row.status === 'OUT' || row.status === 'RESERVED') {
    showError(new Error('已出库或已预留的备件不可删除'))
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除备件「${row.serialNumber}」？`, '提示', { type: 'warning' })
    await request(`/api/spare-parts/items/${row.id}`, { method: 'DELETE' })
    showSuccess('删除成功')
    loadItems()
  } catch (e) {
    if (e !== 'cancel') showError(e)
  }
}

onMounted(async () => {
  await loadCategories()
  await loadItems()
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

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-top: 12px;
}

.page-header h1 {
  margin: 0 0 8px;
  font-size: 24px;
}

.page-desc {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.sub-text {
  color: #909399;
  font-size: 12px;
}
</style>
