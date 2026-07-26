<template>
  <el-dialog
    :model-value="modelValue"
    :title="archId ? '编辑 CPU 架构' : '新增 CPU 架构'"
    width="560px"
    @update:model-value="$emit('update:modelValue', $event)"
    @open="handleOpen"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="架构编码" prop="code">
        <el-input
          v-model="form.code"
          :disabled="!!archId"
          placeholder="如：RISC_V"
        />
      </el-form-item>
      <el-form-item label="架构名称" prop="name">
        <el-input v-model="form.name" placeholder="如：RISC-V 64位" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" placeholder="架构说明" />
      </el-form-item>
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
  archId: { type: [Number, String], default: null }
})

const emit = defineEmits(['update:modelValue', 'saved'])

const formRef = ref()
const saving = ref(false)

const defaultForm = () => ({
  code: '',
  name: '',
  description: ''
})

const form = reactive(defaultForm())

const rules = {
  code: [
    { required: true, message: '请输入架构编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]+$/, message: '编码仅支持字母、数字和下划线', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入架构名称', trigger: 'blur' }]
}

async function handleOpen() {
  Object.assign(form, defaultForm())
  if (props.archId) {
    try {
      const arch = await request(`/api/cpu-architectures/${props.archId}`)
      Object.assign(form, {
        code: arch.code || '',
        name: arch.name || '',
        description: arch.description || ''
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
    const body = {
      code: form.code.trim(),
      name: form.name.trim(),
      description: form.description.trim()
    }
    if (props.archId) {
      await request(`/api/cpu-architectures/${props.archId}`, { method: 'PUT', body })
      showSuccess('更新成功')
    } else {
      await request('/api/cpu-architectures', { method: 'POST', body })
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
