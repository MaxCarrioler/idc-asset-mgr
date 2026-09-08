import axios from 'axios'
import { message } from 'antd'
import {
  getAccessTokenStandalone,
  refreshAccessTokenStandalone,
  clearAuthStandalone,
  redirectToLogin
} from '../stores/AuthContext'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use((config) => {
  const token = getAccessTokenStandalone()
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
    if (status === 401 && original && !original._retried && getAccessTokenStandalone()) {
      original._retried = true
      try {
        refreshPromise ||= refreshAccessTokenStandalone()
        const token = await refreshPromise
        original.headers = original.headers || {}
        original.headers.Authorization = `Bearer ${token}`
        return api(original)
      } catch {
        clearAuthStandalone()
        redirectToLogin()
      } finally {
        refreshPromise = null
      }
    } else if (status === 401) {
      clearAuthStandalone()
      redirectToLogin()
    }
    const data = error.response?.data
    const msg = data?.message || data?.msg || error.message || '网络错误'
    return Promise.reject(new Error(msg))
  }
)

export async function request(url, options = {}) {
  const { method = 'GET', body, params } = options
  const config = { url, method, params }
  if (body) config.data = body
  return api(config)
}

export function showError(error) {
  message.error(error.message || '操作失败')
}

export function showSuccess(text) {
  message.success(text)
}

export default api
