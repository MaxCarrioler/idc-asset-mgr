<template>
  <el-dialog
    :model-value="modelValue"
    :title="serverId ? '编辑服务器' : '新增服务器'"
    width="720px"
    @update:model-value="$emit('update:modelValue', $event)"
    @open="handleOpen"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="服务器名称" prop="serverName">
            <el-input v-model="form.serverName" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="服务器类型" prop="serverType">
            <el-select v-model="form.serverType" style="width: 100%">
              <el-option
                v-for="type in serverTypes"
                :key="type.code"
                :label="`${type.name} (${type.code})`"
                :value="type.code"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="IP地址">
            <el-input v-model="form.ipAddress" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="机架位置">
            <el-input v-model="form.rackLocation" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Region名称">
            <el-input v-model="form.region" placeholder="如：华北-北京" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="服务器型号">
            <el-input v-model="form.serverModel" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="厂商">
            <el-input v-model="form.manufacturer" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="序列号">
            <el-input v-model="form.serialNumber" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="运行中" value="RUNNING" />
              <el-option label="已停止" value="STOPPED" />
              <el-option label="维护中" value="MAINTENANCE" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="操作系统">
            <el-input v-model="form.osType" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="CPU架构">
            <el-select v-model="form.cpuArchitecture" clearable placeholder="请选择" style="width: 100%">
              <el-option
                v-for="arch in cpuArchitectures"
                :key="arch.code"
                :label="`${arch.name} (${arch.code})`"
                :value="arch.code"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注">
            <el-input v-model="form.remark" />
          </el-form-item>
        </el-col>
        <el-col v-if="serverId" :span="24">
          <el-form-item label="变更原因">
            <el-input v-model="form.changeReason" placeholder="请填写本次修改的原因" />
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
  serverId: { type: [Number, String], default: null }
})

const emit = defineEmits(['update:modelValue', 'saved'])

const formRef = ref()
const saving = ref(false)
const serverTypes = ref([])
const cpuArchitectures = ref([])

const defaultForm = () => ({
  serverName: '',
  serverType: 'COMPUTE',
  ipAddress: '',
  rackLocation: '',
  region: '',
  serverModel: '',
  manufacturer: '',
  serialNumber: '',
  status: 'RUNNING',
  osType: '',
  cpuArchitecture: '',
  remark: '',
  changeReason: ''
})

const form = reactive(defaultForm())

const rules = {
  serverName: [{ required: true, message: '请输入服务器名称', trigger: 'blur' }],
  serverType: [{ required: true, message: '请选择服务器类型', trigger: 'change' }]
}

async function loadServerTypes() {
  serverTypes.value = await request('/api/server-types')
}

async function loadCpuArchitectures() {
  cpuArchitectures.value = await request('/api/cpu-architectures')
}

async function handleOpen() {
  Object.assign(form, defaultForm())
  try {
    await Promise.all([loadServerTypes(), loadCpuArchitectures()])
    if (props.serverId) {
      const server = await request(`/api/servers/${props.serverId}`)
      Object.assign(form, {
        serverName: server.serverName || '',
        serverType: server.serverType || 'COMPUTE',
        ipAddress: server.ipAddress || '',
        rackLocation: server.rackLocation || '',
        region: server.region || '',
        serverModel: server.serverModel || '',
        manufacturer: server.manufacturer || '',
        serialNumber: server.serialNumber || '',
        status: server.status || 'RUNNING',
        osType: server.osType || '',
        cpuArchitecture: server.cpuArchitecture || '',
        remark: server.remark || '',
        changeReason: ''
      })
    }
  } catch (e) {
    showError(e)
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const body = { ...form }
    if (props.serverId) {
      await request(`/api/servers/${props.serverId}`, { method: 'PUT', body })
      showSuccess('更新成功')
    } else {
      delete body.changeReason
      await request('/api/servers', { method: 'POST', body })
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
