<template>
  <el-dialog
    :model-value="modelValue"
    :title="categoryId ? '编辑备件种类' : '新增备件种类'"
    width="560px"
    @update:model-value="$emit('update:modelValue', $event)"
    @open="handleOpen"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="种类编码" prop="code">
        <el-input
          v-model="form.code"
          :disabled="!!categoryId"
          placeholder="如：HDD_1_8T"
        />
      </el-form-item>
      <el-form-item label="种类名称" prop="name">
        <el-input v-model="form.name" placeholder="如：1.8T 硬盘" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" placeholder="种类说明" />
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
  categoryId: { type: [Number, String], default: null }
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
    { required: true, message: '请输入种类编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]+$/, message: '编码仅支持字母、数字和下划线', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入种类名称', trigger: 'blur' }]
}

async function handleOpen() {
  Object.assign(form, defaultForm())
  if (props.categoryId) {
    try {
      const category = await request(`/api/spare-part-categories/${props.categoryId}`)
      Object.assign(form, {
        code: category.code || '',
        name: category.name || '',
        description: category.description || ''
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
    if (props.categoryId) {
      await request(`/api/spare-part-categories/${props.categoryId}`, { method: 'PUT', body })
      showSuccess('更新成功')
    } else {
      await request('/api/spare-part-categories', { method: 'POST', body })
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
