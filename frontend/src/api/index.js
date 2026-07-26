import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getAccessToken, refreshAccessToken, clearAuth, redirectToLogin } from '../stores/auth'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let refreshPromise = null

api.interceptors.response.use(
  (response) => {
    const result = response.data
    const code = result?.code
    if (code !== undefined && code !== 200 && code !== 0) {
      return Promise.reject(new Error(result.message || result.msg || '请求失败'))
    }
    if (code === undefined) {
      return result
    }
    return result.data
  },
  async (error) => {
    const status = error.response?.status
    const original = error.config
    if (status === 401 && original && !original._retried && getAccessToken()) {
      original._retried = true
      try {
        refreshPromise ||= refreshAccessToken()
        const token = await refreshPromise
        original.headers = original.headers || {}
        original.headers.Authorization = `Bearer ${token}`
        return api(original)
      } catch {
        clearAuth()
        redirectToLogin()
      } finally {
        refreshPromise = null
      }
    } else if (status === 401) {
      clearAuth()
      redirectToLogin()
    }
    const data = error.response?.data
    const message = data?.message || data?.msg || error.message || '网络错误'
    return Promise.reject(new Error(message))
  }
)

export async function request(url, options = {}) {
  const { method = 'GET', body, params } = options
  const config = { url, method, params }
  if (body) config.data = body
  return api(config)
}

export function showError(error) {
  ElMessage.error(error.message || '操作失败')
}

export function showSuccess(message) {
  ElMessage.success(message)
}

export default api
