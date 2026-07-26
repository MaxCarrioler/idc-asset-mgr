export function getStatusLabel(status) {
  const map = {
    RUNNING: '运行中',
    STOPPED: '已停止',
    MAINTENANCE: '维护中',
    NORMAL: '正常'
  }
  return map[status] || status
}

export function getCpuArchitectureLabel(value) {
  const map = {
    X86_64: 'x86_64',
    ARM64: 'ARM64',
    IA64: 'IA64',
    OTHER: '其他'
  }
  return map[value] || value || '-'
}

export function resolveCpuArchitectureLabel(code, options = []) {
  if (!code) return '-'
  const item = options.find((option) => option.code === code)
  return item?.name || getCpuArchitectureLabel(code)
}

export function formatDateTime(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { hour12: false })
}

export function formatMemory(gb) {
  if (gb == null) return '-'
  if (gb >= 1024) {
    return (Math.round((gb / 1024) * 10) / 10) + ' TB'
  }
  return gb + ' GB'
}

export function formatTb(value) {
  if (value == null) return '-'
  return value + ' TB'
}
