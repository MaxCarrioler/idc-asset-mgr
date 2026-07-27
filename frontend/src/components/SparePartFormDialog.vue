<template>
  <el-dialog
    :model-value="modelValue"
    :title="itemId ? '编辑备件' : '新增备件'"
    width="620px"
    @update:model-value="$emit('update:modelValue', $event)"
    @open="handleOpen"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="备件种类">
        <el-input :model-value="categoryName" disabled />
      </el-form-item>
      <el-form-item label="序列号" prop="serialNumber">
        <el-input v-model="form.serialNumber" placeholder="唯一标识，一件备件一条记录" />
      </el-form-item>
      <el-form-item label="厂商/品牌">
        <el-input v-model="form.manufacturer" placeholder="如：Samsung" />
      </el-form-item>
      <el-form-item label="规格摘要">
        <el-input v-model="form.specSummary" placeholder="可选，如：1.8TB NVMe SSD" />
      </el-form-item>
      <el-form-item label="存放位置">
        <el-input v-model="form.location" placeholder="如：库房B-01" />
      </el-form-item>
      <el-form-item label="来源服务器" prop="sourceKey">
        <el-select
          v-model="form.sourceKey"
          filterable
          clearable
          placeholder="必选：无 / 采购 / 服务器"
          style="width: 100%"
        >
          <el-option label="无" value="NONE" />
          <el-option label="采购" value="PURCHASE" />
          <el-option
            v-for="s in servers"
            :key="s.id"
            :label="`${s.serialNumber || '-'}（${s.serverName || s.name || '未命名'}）`"
            :value="`ID:${s.id}`"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { request, showError, showSuccess } from '../api'

const props = defineProps({
  modelValue: Boolean,
  itemId: { type: [Number, String], default: null },
  categoryId: { type: [Number, String], default: null },
  categories: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue', 'saved'])

const formRef = ref()
const saving = ref(false)
const servers = ref([])

const categoryName = computed(() => {
  const matched = props.categories.find((c) => c.id === Number(props.categoryId))
  return matched?.name || ''
})

const defaultForm = () => ({
  serialNumber: '',
  manufacturer: '',
  specSummary: '',
  location: '',
  sourceKey: '',
  remark: ''
})

const form = reactive(defaultForm())

const rules = {
  serialNumber: [{ required: true, message: '请输入序列号', trigger: 'blur' }],
  sourceKey: [{ required: true, message: '请选择来源服务器', trigger: 'change' }]
}

function resolveSourceKey(item) {
  if (item?.sourceServerId) return `ID:${item.sourceServerId}`
  if (item?.sourceServerSerial === '无') return 'NONE'
  if (item?.sourceServerSerial === '采购') return 'PURCHASE'
  if (item?.sourceServerSerial) {
    const matched = servers.value.find((s) => s.serialNumber === item.sourceServerSerial)
    if (matched) return `ID:${matched.id}`
  }
  return ''
}

function buildSourcePayload(sourceKey) {
  if (sourceKey === 'NONE') {
    return { sourceServerId: null, sourceServerSerial: '无', sourceServerName: null }
  }
  if (sourceKey === 'PURCHASE') {
    return { sourceServerId: null, sourceServerSerial: '采购', sourceServerName: null }
  }
  if (sourceKey?.startsWith('ID:')) {
    const id = Number(sourceKey.slice(3))
    const server = servers.value.find((s) => s.id === id)
    if (!server) {
      throw new Error('所选来源服务器不存在')
    }
    return {
      sourceServerId: server.id,
      sourceServerSerial: server.serialNumber || '',
      sourceServerName: server.serverName || server.name || null
    }
  }
  throw new Error('请选择来源服务器')
}

async function loadServers() {
  try {
    const data = await request('/api/servers', { params: { page: 1, size: 1000 } })
    servers.value = data?.records || data?.content || data || []
  } catch (e) {
    servers.value = []
    showError(e)
  }
}

async function handleOpen() {
  Object.assign(form, defaultForm())
  await loadServers()
  if (props.itemId) {
    try {
      const item = await request(`/api/spare-parts/items/${props.itemId}`)
      Object.assign(form, {
        serialNumber: item.serialNumber || '',
        manufacturer: item.manufacturer || '',
        specSummary: item.specSummary || '',
        location: item.location || '',
        sourceKey: resolveSourceKey(item),
        remark: item.remark || ''
      })
    } catch (e) {
      showError(e)
    }
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const source = buildSourcePayload(form.sourceKey)
    const body = {
      categoryId: Number(props.categoryId),
      serialNumber: form.serialNumber.trim(),
      manufacturer: form.manufacturer.trim(),
      specSummary: form.specSummary.trim(),
      location: form.location.trim(),
      remark: form.remark.trim(),
      ...source
    }
    if (props.itemId) {
      await request(`/api/spare-parts/items/${props.itemId}`, { method: 'PUT', body })
      showSuccess('更新成功')
    } else {
      await request('/api/spare-parts/items', { method: 'POST', body })
      showSuccess('创建成功')
    }
    emit('update:modelValue', false)
    emit('saved')
  } catch (e) {
    showError(e)
  } finally {
    saving.value = false
  }
}
</script>
