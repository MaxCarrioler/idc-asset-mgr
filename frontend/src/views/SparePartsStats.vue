<template>
  <div>
    <div class="toolbar">
      <h2>备件统计</h2>
      <el-button @click="refreshAll">刷新数据</el-button>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="统计概览" name="summary">
        <p class="page-desc">每种规格单独统计，一件备件一条记录。卡片数字为在库数量（配件中心出库后会减少）；点击查看明细可看到「已出库」标注</p>
        <div v-loading="summaryLoading" class="stats-grid">
          <el-empty v-if="!summaryLoading && !summaries.length" description="暂无备件种类，请先在「种类管理」中新增" />
          <el-card
            v-for="item in summaries"
            :key="item.categoryId"
            shadow="hover"
            class="stats-card clickable"
            @click="goDetail(item.categoryId)"
          >
            <div class="card-body">
              <h3>{{ item.typeName }}</h3>
              <div class="count">{{ item.totalCount }}</div>
              <p class="sub">在库数量</p>
              <p v-if="item.outCount > 0" class="out-sub">已出库 {{ item.outCount }}</p>
            </div>
            <div class="card-footer">查看明细 →</div>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="种类管理" name="categories">
        <div class="panel-toolbar">
          <p class="panel-desc">每种规格/型号单独建一类，如「1.8T 硬盘」「8T 硬盘」。明细中以序列号区分每一件备件</p>
          <el-button type="primary" @click="openCreateCategory">+ 新增种类</el-button>
        </div>
        <el-card shadow="never">
          <el-table :data="categories" v-loading="categoryLoading" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column label="种类编码" min-width="120">
              <template #default="{ row }">
                <el-tag type="info">{{ row.code }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="种类名称" min-width="120" />
            <el-table-column prop="description" label="描述" min-width="180" />
            <el-table-column label="来源" width="90">
              <template #default="{ row }">{{ row.builtIn ? '内置' : '自定义' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openEditCategory(row.id)">编辑</el-button>
                <el-button
                  v-if="!row.builtIn"
                  link
                  type="danger"
                  @click="handleDeleteCategory(row)"
                >删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <SparePartCategoryFormDialog
      v-model="categoryDialogVisible"
      :category-id="editingCategoryId"
      @saved="onCategorySaved"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { request, showError, showSuccess } from '../api'
import SparePartCategoryFormDialog from '../components/SparePartCategoryFormDialog.vue'

const router = useRouter()
const activeTab = ref('summary')

const summaries = ref([])
const categories = ref([])
const summaryLoading = ref(false)
const categoryLoading = ref(false)

const categoryDialogVisible = ref(false)
const editingCategoryId = ref(null)

async function loadSummary() {
  summaryLoading.value = true
  try {
    summaries.value = await request('/api/spare-parts/summary')
  } catch (e) {
    showError(e)
    summaries.value = []
  } finally {
    summaryLoading.value = false
  }
}

async function loadCategories() {
  categoryLoading.value = true
  try {
    categories.value = await request('/api/spare-part-categories')
  } catch (e) {
    showError(e)
    categories.value = []
  } finally {
    categoryLoading.value = false
  }
}

function refreshAll() {
  loadSummary()
  loadCategories()
}

function goDetail(categoryId) {
  router.push(`/spare-parts/${categoryId}`)
}

function openCreateCategory() {
  editingCategoryId.value = null
  categoryDialogVisible.value = true
}

function openEditCategory(id) {
  editingCategoryId.value = id
  categoryDialogVisible.value = true
}

async function handleDeleteCategory(row) {
  try {
    await ElMessageBox.confirm(`确认删除备件种类「${row.name}」？`, '提示', { type: 'warning' })
    await request(`/api/spare-part-categories/${row.id}`, { method: 'DELETE' })
    showSuccess('删除成功')
    refreshAll()
  } catch (e) {
    if (e !== 'cancel') showError(e)
  }
}

function onCategorySaved() {
  refreshAll()
}

onMounted(refreshAll)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.toolbar h2 {
  margin: 0;
  font-size: 20px;
}

.page-desc,
.panel-desc {
  margin: 0 0 20px;
  color: #909399;
  font-size: 13px;
}

.panel-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.panel-toolbar .panel-desc {
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  min-height: 120px;
}

.stats-card {
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
}

.stats-card:hover {
  transform: translateY(-2px);
}

.card-body {
  text-align: center;
  padding: 8px 0;
}

.card-body h3 {
  margin: 0 0 12px;
  font-size: 16px;
  color: #303133;
}

.count {
  font-size: 36px;
  font-weight: 700;
  color: #409eff;
  line-height: 1.2;
}

.sub {
  margin: 8px 0 0;
  color: #909399;
  font-size: 13px;
}

.out-sub {
  margin: 4px 0 0;
  color: #909399;
  font-size: 12px;
}

.card-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  text-align: center;
  color: #409eff;
  font-size: 13px;
}
</style>
