<template>
  <el-dialog
    :model-value="modelValue"
    :title="editingId ? `编辑 ${config.title}` : `新增 ${config.title}`"
    width="720px"
    @update:model-value="$emit('update:modelValue', $event)"
    @open="handleOpen"
  >
    <el-form ref="formRef" :model="form" label-width="140px">
      <el-row :gutter="16">
        <el-col v-for="field in config.fields" :key="field.key" :span="12">
          <el-form-item
            :label="field.label"
            :prop="field.key"
            :rules="field.required ? [{ required: true, message: `请填写${field.label}`, trigger: 'blur' }] : []"
          >
            <el-select
              v-if="field.type === 'boolean'"
              v-model="form[field.key]"
              style="width: 100%"
            >
              <el-option label="是" :value="true" />
              <el-option label="否" :value="false" />
            </el-select>
            <el-input-number
              v-else-if="field.type === 'number'"
              v-model="form[field.key]"
              :step="field.step || 1"
              style="width: 100%"
            />
            <el-input v-else v-model="form[field.key]" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { request, showError, showSuccess } from '../api'

const props = defineProps({
  modelValue: Boolean,
  config: { type: Object, required: true },
  serverId: { type: [Number, String], required: true },
  editingId: { type: [Number, String], default: null }
})

const emit = defineEmits(['update:modelValue', 'saved'])

const formRef = ref()
const saving = ref(false)
const form = reactive({})

function buildDefaultForm(data = {}) {
  props.config.fields.forEach((field) => {
    let val = data[field.key]
    if (field.type === 'boolean') {
      form[field.key] = val === true
    } else if (field.type === 'number') {
      form[field.key] = val ?? undefined
    } else {
      form[field.key] = val ?? ''
    }
  })
}

async function handleOpen() {
  if (props.editingId) {
    try {
      const item = await request(`${props.config.apiPath}/${props.editingId}`)
      buildDefaultForm(item)
    } catch (e) {
      showError(e)
    }
  } else {
    buildDefaultForm()
  }
}

function collectBody() {
  const body = { serverId: Number(props.serverId) }
  props.config.fields.forEach((field) => {
    let val = form[field.key]
    if (field.type === 'number' && (val === undefined || val === '')) {
      val = null
    }
    body[field.key] = val
  })
  return body
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const body = collectBody()
    if (props.editingId) {
      await request(`${props.config.apiPath}/${props.editingId}`, { method: 'PUT', body })
      showSuccess('更新成功')
    } else {
      await request(props.config.apiPath, { method: 'POST', body })
      showSuccess('新增成功')
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
