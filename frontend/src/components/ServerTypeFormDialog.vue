<template>
  <el-dialog
    :model-value="modelValue"
    :title="typeId ? '编辑服务器类型' : '新增服务器类型'"
    width="560px"
    @update:model-value="$emit('update:modelValue', $event)"
    @open="handleOpen"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="类型编码" prop="code">
        <el-input
          v-model="form.code"
          :disabled="!!typeId"
          placeholder="如：GPU_NODE"
        />
      </el-form-item>
      <el-form-item label="类型名称" prop="name">
        <el-input v-model="form.name" placeholder="如：GPU计算节点" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" placeholder="类型用途说明" />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="form.trackThreads">统计 CPU 线程数</el-checkbox>
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="form.includeDiskStats">统计硬盘容量及规格</el-checkbox>
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
  typeId: { type: [Number, String], default: null }
})

const emit = defineEmits(['update:modelValue', 'saved'])

const formRef = ref()
const saving = ref(false)

const defaultForm = () => ({
  code: '',
  name: '',
  description: '',
  trackThreads: false,
  includeDiskStats: false
})

const form = reactive(defaultForm())

const rules = {
  code: [
    { required: true, message: '请输入类型编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]+$/, message: '编码仅支持字母、数字和下划线', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入类型名称', trigger: 'blur' }]
}

async function handleOpen() {
  Object.assign(form, defaultForm())
  if (props.typeId) {
    try {
      const type = await request(`/api/server-types/${props.typeId}`)
      Object.assign(form, {
        code: type.code || '',
        name: type.name || '',
        description: type.description || '',
        trackThreads: !!type.trackThreads,
        includeDiskStats: !!type.includeDiskStats
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
      description: form.description.trim(),
      trackThreads: form.trackThreads,
      includeDiskStats: form.includeDiskStats
    }
    if (props.typeId) {
      await request(`/api/server-types/${props.typeId}`, { method: 'PUT', body })
      showSuccess('更新成功')
    } else {
      await request('/api/server-types', { method: 'POST', body })
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
