<template>
  <div>
    <div class="toolbar">
      <h2>配件中心</h2>
      <div class="toolbar-actions">
        <el-button @click="refreshAll">刷新</el-button>
        <el-button
          v-permission="'accessory:transfer:apply'"
          type="primary"
          @click="openApplyDialog"
        >
          新建调取申请
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="申请列表" name="requests">
        <div class="filter-row">
          <el-select v-model="statusFilter" clearable placeholder="状态筛选" style="width: 160px" @change="loadRequests">
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
            <el-option label="已出库" value="OUTBOUND" />
          </el-select>
        </div>
        <el-card shadow="never">
          <el-table :data="requests" v-loading="requestsLoading" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="categoryName" label="配件种类" min-width="120" />
            <el-table-column prop="specParams" label="规格参数" min-width="120" show-overflow-tooltip />
            <el-table-column prop="quantity" label="数量" width="70" />
            <el-table-column prop="purpose" label="用途" min-width="140" show-overflow-tooltip />
            <el-table-column label="目标服务器" min-width="160">
              <template #default="{ row }">
                <div>{{ row.targetServerName || '-' }}</div>
                <div class="sub-text">{{ row.targetServerSerial }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="applicantName" label="申请人" width="100" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="申请时间" min-width="160" />
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openDetail(row)">详情</el-button>
                <el-button
                  v-if="row.status === 'PENDING'"
                  v-permission="'accessory:transfer:approve'"
                  link
                  type="success"
                  @click="openApprove(row)"
                >审批</el-button>
                <el-button
                  v-if="row.status === 'APPROVED'"
                  v-permission="'accessory:transfer:outbound'"
                  link
                  type="warning"
                  @click="openOutbound(row)"
                >出库</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="流转记录" name="transfers">
        <div class="filter-row">
          <el-button type="primary" :disabled="!transfers.length" :loading="exporting" @click="exportTransfers">
            导出 Excel
          </el-button>
        </div>
        <el-card shadow="never">
          <el-table :data="transfers" v-loading="transfersLoading" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="requestId" label="申请单" width="90" />
            <el-table-column prop="categoryName" label="种类" min-width="110" />
            <el-table-column prop="serialNumber" label="序列号" min-width="140" />
            <el-table-column label="来源服务器" min-width="160">
              <template #default="{ row }">
                <div>{{ row.sourceServerName || (row.sourceServerSerial === '无' ? '无' : '-') }}</div>
                <div v-if="row.sourceServerSerial && row.sourceServerSerial !== '无'" class="sub-text">{{ row.sourceServerSerial }}</div>
              </template>
            </el-table-column>
            <el-table-column label="现役服务器" min-width="160">
              <template #default="{ row }">
                <div>{{ row.targetServerName || '-' }}</div>
                <div class="sub-text">{{ row.targetServerSerial }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="operatorName" label="操作人" width="100" />
            <el-table-column prop="outboundTime" label="出库时间" min-width="160" />
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 新建申请 -->
    <el-dialog v-model="applyVisible" title="新建调取申请" width="560px" destroy-on-close>
      <el-form :model="applyForm" label-width="110px">
        <el-form-item label="配件种类" required>
          <el-select v-model="applyForm.categoryId" filterable placeholder="选择种类" style="width: 100%">
            <el-option
              v-for="c in categories"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="规格参数">
          <el-input v-model="applyForm.specParams" placeholder="如容量、型号等" />
        </el-form-item>
        <el-form-item label="数量" required>
          <el-input-number v-model="applyForm.quantity" :min="1" />
        </el-form-item>
        <el-form-item label="用途" required>
          <el-input v-model="applyForm.purpose" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="目标服务器" required>
          <el-select v-model="applyForm.targetServerId" filterable placeholder="选择服务器" style="width: 100%">
            <el-option
              v-for="s in servers"
              :key="s.id"
              :label="`${s.serverName} (${s.serialNumber})`"
              :value="s.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="applySubmitting" @click="submitApply">提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="申请详情" width="720px">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="种类">{{ detail.categoryName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detail.status)" size="small">{{ statusLabel(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="数量">{{ detail.quantity }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detail.applicantName }}</el-descriptions-item>
          <el-descriptions-item label="规格参数" :span="2">{{ detail.specParams || '-' }}</el-descriptions-item>
          <el-descriptions-item label="用途" :span="2">{{ detail.purpose }}</el-descriptions-item>
          <el-descriptions-item label="目标服务器">{{ detail.targetServerName }}</el-descriptions-item>
          <el-descriptions-item label="目标序列号">{{ detail.targetServerSerial }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.rejectReason" label="驳回原因" :span="2">{{ detail.rejectReason }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin: 16px 0 8px">已绑定备件</h4>
        <el-table v-if="detail.items?.length" :data="detail.items" size="small" border>
          <el-table-column prop="serialNumber" label="序列号" />
          <el-table-column prop="manufacturer" label="厂商" />
          <el-table-column prop="sourceServerSerial" label="来源服务器序列号" />
          <el-table-column prop="sourceServerName" label="来源服务器" />
        </el-table>
        <el-empty v-else description="尚未绑定备件" :image-size="64" />
      </template>
    </el-dialog>

    <!-- 审批 -->
    <el-dialog v-model="approveVisible" title="审批调取申请" width="760px" destroy-on-close>
      <p class="dialog-hint">
        申请数量：{{ approveTarget?.quantity }}，请勾选对应数量的在库备件序列号后通过。
      </p>
      <el-table
        ref="spareTableRef"
        :data="availableSpareParts"
        v-loading="spareLoading"
        stripe
        @selection-change="onSpareSelectionChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column prop="serialNumber" label="序列号" min-width="140" />
        <el-table-column prop="manufacturer" label="厂商" min-width="100" />
        <el-table-column prop="specSummary" label="规格参数" min-width="120" />
        <el-table-column prop="sourceServerSerial" label="来源服务器序列号" min-width="140" />
        <el-table-column prop="status" label="状态" width="90" />
      </el-table>
      <el-form style="margin-top: 12px" label-width="80px">
        <el-form-item label="备注">
          <el-input v-model="approveRemark" />
        </el-form-item>
        <el-form-item label="驳回原因">
          <el-input v-model="rejectReason" placeholder="驳回时填写" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button
          v-permission="'accessory:transfer:reject'"
          type="danger"
          :loading="approveSubmitting"
          @click="submitReject"
        >驳回</el-button>
        <el-button
          v-permission="'accessory:transfer:approve'"
          type="primary"
          :loading="approveSubmitting"
          @click="submitApprove"
        >通过</el-button>
      </template>
    </el-dialog>

    <!-- 出库 -->
    <el-dialog v-model="outboundVisible" title="配件出库" width="720px" destroy-on-close>
      <p class="dialog-hint">
        请为每个备件选择来源服务器：可选择真实服务器序列号，或选择「无」（无具体来源服务器）。
      </p>
      <el-table :data="outboundRows" border size="small">
        <el-table-column prop="serialNumber" label="备件序列号" min-width="140" />
        <el-table-column label="来源服务器" min-width="280">
          <template #default="{ row }">
            <el-select
              v-model="row.sourceKey"
              filterable
              placeholder="必选：序列号或无"
              style="width: 100%"
              @change="(val) => onOutboundSourceChange(row, val)"
            >
              <el-option label="无" value="NONE" />
              <el-option
                v-for="s in servers"
                :key="s.id"
                :label="`${s.serialNumber || '-'}（${s.serverName || s.name || '未命名'}）`"
                :value="`ID:${s.id}`"
              />
            </el-select>
          </template>
        </el-table-column>
      </el-table>
      <el-form style="margin-top: 12px" label-width="60px">
        <el-form-item label="备注">
          <el-input v-model="outboundRemark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="outboundVisible = false">取消</el-button>
        <el-button type="primary" :loading="outboundSubmitting" @click="submitOutbound">确认出库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import * as XLSX from 'xlsx'
import { request, showError, showSuccess } from '../api'

const activeTab = ref('requests')
const statusFilter = ref('')
const requests = ref([])
const transfers = ref([])
const categories = ref([])
const servers = ref([])
const requestsLoading = ref(false)
const transfersLoading = ref(false)
const exporting = ref(false)

const applyVisible = ref(false)
const applySubmitting = ref(false)
const applyForm = reactive({
  categoryId: null,
  specParams: '',
  quantity: 1,
  purpose: '',
  targetServerId: null
})

const detailVisible = ref(false)
const detail = ref(null)

const approveVisible = ref(false)
const approveTarget = ref(null)
const availableSpareParts = ref([])
const selectedSpareIds = ref([])
const spareLoading = ref(false)
const approveSubmitting = ref(false)
const approveRemark = ref('')
const rejectReason = ref('')

const outboundVisible = ref(false)
const outboundTargetId = ref(null)
const outboundRows = ref([])
const outboundRemark = ref('')
const outboundSubmitting = ref(false)

const STATUS_MAP = {
  PENDING: '待审批',
  APPROVED: '已通过',
  REJECTED: '已驳回',
  OUTBOUND: '已出库'
}

function statusLabel(s) {
  return STATUS_MAP[s] || s || '-'
}

function statusTagType(s) {
  return ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', OUTBOUND: 'info' })[s] || ''
}

async function loadRequests() {
  requestsLoading.value = true
  try {
    const params = {}
    if (statusFilter.value) params.status = statusFilter.value
    requests.value = (await request('/api/accessory-center/requests', { params })) || []
  } catch (e) {
    showError(e)
  } finally {
    requestsLoading.value = false
  }
}

async function loadTransfers() {
  transfersLoading.value = true
  try {
    transfers.value = (await request('/api/accessory-center/transfers')) || []
  } catch (e) {
    showError(e)
  } finally {
    transfersLoading.value = false
  }
}

function formatSourceServer(row) {
  if (row.sourceServerSerial === '无') return '无'
  const name = row.sourceServerName || ''
  const serial = row.sourceServerSerial || ''
  if (name && serial) return `${name}（${serial}）`
  return name || serial || '-'
}

function formatActiveServer(row) {
  const name = row.targetServerName || ''
  const serial = row.targetServerSerial || ''
  if (name && serial) return `${name}（${serial}）`
  return name || serial || '-'
}

function formatOutboundTime(value) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function exportTransfers() {
  if (!transfers.value.length) {
    showError(new Error('暂无流转记录可导出'))
    return
  }
  exporting.value = true
  try {
    const rows = transfers.value.map((row) => ({
      ID: row.id ?? '',
      申请单: row.requestId ?? '',
      种类: row.categoryName || '-',
      序列号: row.serialNumber || '-',
      来源服务器: formatSourceServer(row),
      现役服务器: formatActiveServer(row),
      操作人: row.operatorName || '-',
      出库时间: formatOutboundTime(row.outboundTime),
      备注: row.remark || ''
    }))
    const sheet = XLSX.utils.json_to_sheet(rows)
    sheet['!cols'] = [
      { wch: 8 },
      { wch: 10 },
      { wch: 16 },
      { wch: 20 },
      { wch: 28 },
      { wch: 28 },
      { wch: 12 },
      { wch: 20 },
      { wch: 20 }
    ]
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, sheet, '流转记录')
    const stamp = formatOutboundTime(new Date()).replace(/[:\s]/g, '')
    XLSX.writeFile(workbook, `配件中心-流转记录-${stamp}.xlsx`)
    showSuccess('导出成功')
  } catch (e) {
    showError(e)
  } finally {
    exporting.value = false
  }
}

async function loadMeta() {
  try {
    const [cats, srv] = await Promise.all([
      request('/api/spare-part-categories'),
      request('/api/servers', { params: { page: 1, size: 1000 } })
    ])
    categories.value = cats || []
    servers.value = Array.isArray(srv) ? srv : (srv?.content || srv?.records || [])
  } catch (e) {
    showError(e)
  }
}

function refreshAll() {
  loadRequests()
  if (activeTab.value === 'transfers') loadTransfers()
}

function onTabChange(name) {
  if (name === 'transfers') loadTransfers()
}

function openApplyDialog() {
  applyForm.categoryId = null
  applyForm.specParams = ''
  applyForm.quantity = 1
  applyForm.purpose = ''
  applyForm.targetServerId = null
  applyVisible.value = true
}

async function submitApply() {
  if (!applyForm.categoryId || !applyForm.purpose?.trim() || !applyForm.targetServerId) {
    showError(new Error('请完整填写种类、用途与目标服务器'))
    return
  }
  applySubmitting.value = true
  try {
    await request('/api/accessory-center/requests', {
      method: 'POST',
      body: {
        categoryId: applyForm.categoryId,
        specParams: applyForm.specParams || null,
        quantity: applyForm.quantity,
        purpose: applyForm.purpose.trim(),
        targetServerId: applyForm.targetServerId
      }
    })
    showSuccess('申请已提交')
    applyVisible.value = false
    await loadRequests()
  } catch (e) {
    showError(e)
  } finally {
    applySubmitting.value = false
  }
}

async function openDetail(row) {
  try {
    detail.value = await request(`/api/accessory-center/requests/${row.id}`)
    detailVisible.value = true
  } catch (e) {
    showError(e)
  }
}

async function openApprove(row) {
  approveTarget.value = row
  selectedSpareIds.value = []
  approveRemark.value = ''
  rejectReason.value = ''
  approveVisible.value = true
  spareLoading.value = true
  try {
    availableSpareParts.value = (await request('/api/accessory-center/available-spare-parts', {
      params: { categoryId: row.categoryId }
    })) || []
  } catch (e) {
    showError(e)
    availableSpareParts.value = []
  } finally {
    spareLoading.value = false
  }
}

function onSpareSelectionChange(rows) {
  selectedSpareIds.value = rows.map((r) => r.id)
}

async function submitApprove() {
  const qty = approveTarget.value?.quantity || 0
  if (selectedSpareIds.value.length !== qty) {
    showError(new Error(`请恰好选择 ${qty} 个备件序列号`))
    return
  }
  approveSubmitting.value = true
  try {
    await request(`/api/accessory-center/requests/${approveTarget.value.id}/approve`, {
      method: 'POST',
      body: {
        sparePartIds: selectedSpareIds.value,
        remark: approveRemark.value || null
      }
    })
    showSuccess('审批已通过')
    approveVisible.value = false
    await loadRequests()
  } catch (e) {
    showError(e)
  } finally {
    approveSubmitting.value = false
  }
}

async function submitReject() {
  if (!rejectReason.value?.trim()) {
    showError(new Error('请填写驳回原因'))
    return
  }
  approveSubmitting.value = true
  try {
    await request(`/api/accessory-center/requests/${approveTarget.value.id}/reject`, {
      method: 'POST',
      body: { reason: rejectReason.value.trim() }
    })
    showSuccess('已驳回')
    approveVisible.value = false
    await loadRequests()
  } catch (e) {
    showError(e)
  } finally {
    approveSubmitting.value = false
  }
}

async function openOutbound(row) {
  try {
    const full = await request(`/api/accessory-center/requests/${row.id}`)
    outboundTargetId.value = row.id
    outboundRemark.value = ''
    outboundRows.value = (full.items || []).map((item) => {
      const matched = item.sourceServerId
        ? servers.value.find((s) => s.id === item.sourceServerId)
        : servers.value.find((s) => s.serialNumber === item.sourceServerSerial)
      let sourceKey = ''
      if (item.sourceServerSerial === '无') {
        sourceKey = 'NONE'
      } else if (matched) {
        sourceKey = `ID:${matched.id}`
      }
      return {
        sparePartId: item.sparePartId,
        serialNumber: item.serialNumber,
        sourceKey,
        sourceServerId: matched?.id || item.sourceServerId || null,
        sourceServerSerial: item.sourceServerSerial || '',
        sourceServerName: matched?.serverName || matched?.name || item.sourceServerName || ''
      }
    })
    outboundVisible.value = true
  } catch (e) {
    showError(e)
  }
}

function onOutboundSourceChange(row, val) {
  if (val === 'NONE') {
    row.sourceServerId = null
    row.sourceServerSerial = '无'
    row.sourceServerName = null
    return
  }
  const id = Number(String(val).replace(/^ID:/, ''))
  const server = servers.value.find((s) => s.id === id)
  if (!server) {
    row.sourceServerId = null
    row.sourceServerSerial = ''
    row.sourceServerName = null
    return
  }
  row.sourceServerId = server.id
  row.sourceServerSerial = server.serialNumber || ''
  row.sourceServerName = server.serverName || server.name || ''
}

async function submitOutbound() {
  const missing = outboundRows.value.find((r) => !r.sourceKey || !r.sourceServerSerial?.trim())
  if (missing) {
    showError(new Error(`备件 ${missing.serialNumber} 请选择来源服务器（序列号或「无」）`))
    return
  }
  outboundSubmitting.value = true
  try {
    await request(`/api/accessory-center/requests/${outboundTargetId.value}/outbound`, {
      method: 'POST',
      body: {
        remark: outboundRemark.value || null,
        sources: outboundRows.value.map((r) => ({
          sparePartId: r.sparePartId,
          sourceServerId: r.sourceServerId || null,
          sourceServerSerial: r.sourceServerSerial.trim(),
          sourceServerName: r.sourceServerName || null
        }))
      }
    })
    showSuccess('出库成功')
    outboundVisible.value = false
    await loadRequests()
    if (activeTab.value === 'transfers') await loadTransfers()
  } catch (e) {
    showError(e)
  } finally {
    outboundSubmitting.value = false
  }
}

onMounted(async () => {
  await loadMeta()
  await loadRequests()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.toolbar h2 {
  margin: 0;
  font-size: 20px;
  color: #1a1a2e;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.filter-row {
  margin-bottom: 12px;
}

.sub-text {
  color: #909399;
  font-size: 12px;
}

.dialog-hint {
  margin: 0 0 12px;
  color: #606266;
  font-size: 13px;
}
</style>
