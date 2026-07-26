export default {
  mounted(el, binding) {
    const value = binding.value
    if (!value) return
    const required = Array.isArray(value) ? value : [value]
    const raw = localStorage.getItem('idc_user_info')
    const user = raw ? JSON.parse(raw) : null
    const permissions = user?.permissions || []
    const ok = required.some((p) => permissions.includes(p) || permissions.includes('*:*:*'))
    if (!ok) {
      el.style.display = 'none'
      el.setAttribute('disabled', 'disabled')
    }
  }
}
