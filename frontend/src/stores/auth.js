import { reactive, computed } from 'vue'
import axios from 'axios'
import CryptoJS from 'crypto-js'

const TOKEN_KEY = 'idc_access_token'
const REFRESH_KEY = 'idc_refresh_token'
const USER_KEY = 'idc_user_info'
const ENCODE_KEY = 'thanks,pig4cloud'
const CLIENT_ID = 'test'
const CLIENT_SECRET = 'test'

const state = reactive({
  accessToken: localStorage.getItem(TOKEN_KEY) || '',
  refreshToken: localStorage.getItem(REFRESH_KEY) || '',
  user: JSON.parse(localStorage.getItem(USER_KEY) || 'null')
})

function encryptPassword(password) {
  const key = CryptoJS.enc.Utf8.parse(ENCODE_KEY)
  const encrypted = CryptoJS.AES.encrypt(password, key, {
    iv: key,
    mode: CryptoJS.mode.CFB,
    padding: CryptoJS.pad.NoPadding
  })
  return encrypted.toString()
}

function persist() {
  if (state.accessToken) localStorage.setItem(TOKEN_KEY, state.accessToken)
  else localStorage.removeItem(TOKEN_KEY)
  if (state.refreshToken) localStorage.setItem(REFRESH_KEY, state.refreshToken)
  else localStorage.removeItem(REFRESH_KEY)
  if (state.user) localStorage.setItem(USER_KEY, JSON.stringify(state.user))
  else localStorage.removeItem(USER_KEY)
}

function applyTokenResponse(data) {
  state.accessToken = data.access_token || ''
  state.refreshToken = data.refresh_token || state.refreshToken || ''
  persist()
}

export function getAccessToken() {
  return state.accessToken
}

export function clearAuth() {
  state.accessToken = ''
  state.refreshToken = ''
  state.user = null
  persist()
}

export function redirectToLogin() {
  if (window.location.pathname !== '/login') {
    window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`
  }
}

export async function login(username, password) {
  const basic = btoa(`${CLIENT_ID}:${CLIENT_SECRET}`)
  const form = new URLSearchParams()
  form.set('grant_type', 'password')
  form.set('scope', 'server')
  form.set('username', username)
  form.set('password', encryptPassword(password))

  const { data } = await axios.post('/auth/oauth2/token', form, {
    headers: {
      Authorization: `Basic ${basic}`,
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })

  applyTokenResponse(data)
  await fetchCurrentUser()
  return state.user
}

export async function refreshAccessToken() {
  if (!state.refreshToken) {
    throw new Error('缺少刷新令牌')
  }
  const basic = btoa(`${CLIENT_ID}:${CLIENT_SECRET}`)
  const form = new URLSearchParams()
  form.set('grant_type', 'refresh_token')
  form.set('refresh_token', state.refreshToken)
  form.set('scope', 'server')
  const { data } = await axios.post('/auth/oauth2/token', form, {
    headers: {
      Authorization: `Basic ${basic}`,
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
  applyTokenResponse(data)
  return state.accessToken
}

export async function logout() {
  try {
    if (state.accessToken) {
      await axios.delete('/auth/token/logout', {
        headers: { Authorization: `Bearer ${state.accessToken}` }
      })
    }
  } catch {
    // ignore logout errors
  } finally {
    clearAuth()
  }
}

export async function fetchCurrentUser() {
  if (!state.accessToken) {
    state.user = null
    return null
  }
  const { data } = await axios.get('/admin/user/info', {
    headers: { Authorization: `Bearer ${state.accessToken}` }
  })
  const payload = data?.data || data
  const permissions = payload?.permissions || []
  const roleList = payload?.roleList || []
  const roles = roleList.map((r) => r.roleCode || r.role_code).filter(Boolean)
  state.user = {
    userId: payload?.userId,
    username: payload?.username,
    name: payload?.name || payload?.nickname,
    nickname: payload?.nickname,
    permissions,
    roles,
    roleList
  }
  persist()
  return state.user
}

export function hasPermission(...perms) {
  const list = state.user?.permissions || []
  if (!perms.length) return false
  if (list.includes('*:*:*')) return true
  return perms.some((p) => list.includes(p))
}

export function hasAnyRole(...roleCodes) {
  const roles = state.user?.roles || []
  return roleCodes.some((code) => roles.includes(code))
}

export const isLoggedIn = computed(() => !!state.accessToken)

export const currentUser = computed(() => state.user)

export function useAuth() {
  return {
    state,
    isLoggedIn,
    currentUser,
    login,
    refreshAccessToken,
    logout,
    fetchCurrentUser,
    hasPermission,
    hasAnyRole,
    clearAuth,
    getAccessToken
  }
}
