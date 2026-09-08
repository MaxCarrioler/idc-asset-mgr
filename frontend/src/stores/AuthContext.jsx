import { createContext, useContext, useState, useCallback, useMemo } from 'react'
import axios from 'axios'
import CryptoJS from 'crypto-js'

const TOKEN_KEY = 'idc_access_token'
const REFRESH_KEY = 'idc_refresh_token'
const USER_KEY = 'idc_user_info'
const ENCODE_KEY = 'thanks,pig4cloud'
const CLIENT_ID = 'test'
const CLIENT_SECRET = 'test'

const AuthContext = createContext(null)

function encryptPassword(password) {
  const key = CryptoJS.enc.Utf8.parse(ENCODE_KEY)
  const encrypted = CryptoJS.AES.encrypt(password, key, {
    iv: key,
    mode: CryptoJS.mode.CFB,
    padding: CryptoJS.pad.NoPadding
  })
  return encrypted.toString()
}

function persistState(accessToken, refreshToken, user) {
  if (accessToken) localStorage.setItem(TOKEN_KEY, accessToken)
  else localStorage.removeItem(TOKEN_KEY)
  if (refreshToken) localStorage.setItem(REFRESH_KEY, refreshToken)
  else localStorage.removeItem(REFRESH_KEY)
  if (user) localStorage.setItem(USER_KEY, JSON.stringify(user))
  else localStorage.removeItem(USER_KEY)
}

export function AuthProvider({ children }) {
  const [accessToken, setAccessToken] = useState(() => localStorage.getItem(TOKEN_KEY) || '')
  const [refreshToken, setRefreshToken] = useState(() => localStorage.getItem(REFRESH_KEY) || '')
  const [user, setUser] = useState(() => JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

  const isLoggedIn = useMemo(() => !!accessToken, [accessToken])

  const hasPermission = useCallback((...perms) => {
    const list = user?.permissions || []
    if (!perms.length) return false
    if (list.includes('*:*:*')) return true
    return perms.some((p) => list.includes(p))
  }, [user])

  const hasAnyRole = useCallback((...roleCodes) => {
    const roles = user?.roles || []
    return roleCodes.some((code) => roles.includes(code))
  }, [user])

  const login = useCallback(async (username, password) => {
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

    const newAccess = data.access_token || ''
    const newRefresh = data.refresh_token || ''
    setAccessToken(newAccess)
    setRefreshToken(newRefresh)
    persistState(newAccess, newRefresh, user)

    const currentUser = await fetchUser(newAccess)
    return currentUser
  }, [user])

  const refreshAccessToken = useCallback(async () => {
    if (!refreshToken) throw new Error('缺少刷新令牌')
    const basic = btoa(`${CLIENT_ID}:${CLIENT_SECRET}`)
    const form = new URLSearchParams()
    form.set('grant_type', 'refresh_token')
    form.set('refresh_token', refreshToken)
    form.set('scope', 'server')

    const { data } = await axios.post('/auth/oauth2/token', form, {
      headers: {
        Authorization: `Basic ${basic}`,
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })

    const newAccess = data.access_token || ''
    const newRefresh = data.refresh_token || refreshToken
    setAccessToken(newAccess)
    setRefreshToken(newRefresh)
    persistState(newAccess, newRefresh, user)
    return newAccess
  }, [refreshToken, user])

  const fetchUser = useCallback(async (token) => {
    const t = token || accessToken
    if (!t) {
      setUser(null)
      return null
    }
    const { data } = await axios.get('/admin/user/info', {
      headers: { Authorization: `Bearer ${t}` }
    })
    const payload = data?.data || data
    const permissions = payload?.permissions || []
    const roleList = payload?.roleList || []
    const roles = roleList.map((r) => r.roleCode || r.role_code).filter(Boolean)
    const newUser = {
      userId: payload?.userId,
      username: payload?.username,
      name: payload?.name || payload?.nickname,
      nickname: payload?.nickname,
      permissions,
      roles,
      roleList
    }
    setUser(newUser)
    persistState(accessToken, refreshToken, newUser)
    return newUser
  }, [accessToken, refreshToken])

  const logout = useCallback(async () => {
    try {
      if (accessToken) {
        await axios.delete('/auth/token/logout', {
          headers: { Authorization: `Bearer ${accessToken}` }
        })
      }
    } catch {
      // ignore logout errors
    } finally {
      setAccessToken('')
      setRefreshToken('')
      setUser(null)
      persistState('', '', null)
    }
  }, [accessToken])

  const clearAuth = useCallback(() => {
    setAccessToken('')
    setRefreshToken('')
    setUser(null)
    persistState('', '', null)
  }, [])

  const getAccessToken = useCallback(() => accessToken, [accessToken])

  const value = useMemo(() => ({
    accessToken,
    refreshToken,
    user,
    isLoggedIn,
    login,
    refreshAccessToken,
    fetchCurrentUser: fetchUser,
    logout,
    clearAuth,
    getAccessToken,
    hasPermission,
    hasAnyRole
  }), [accessToken, refreshToken, user, isLoggedIn, login, refreshAccessToken, fetchUser, logout, clearAuth, getAccessToken, hasPermission, hasAnyRole])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}

// Standalone helpers used by api/index.js interceptor (outside React tree)
export function getAccessTokenStandalone() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function clearAuthStandalone() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_KEY)
  localStorage.removeItem(USER_KEY)
}

export function redirectToLogin() {
  if (window.location.pathname !== '/login') {
    window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`
  }
}

export async function refreshAccessTokenStandalone() {
  const rt = localStorage.getItem(REFRESH_KEY)
  if (!rt) throw new Error('缺少刷新令牌')
  const basic = btoa(`${CLIENT_ID}:${CLIENT_SECRET}`)
  const form = new URLSearchParams()
  form.set('grant_type', 'refresh_token')
  form.set('refresh_token', rt)
  form.set('scope', 'server')
  const { data } = await axios.post('/auth/oauth2/token', form, {
    headers: {
      Authorization: `Basic ${basic}`,
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })
  const newAccess = data.access_token || ''
  if (newAccess) localStorage.setItem(TOKEN_KEY, newAccess)
  if (data.refresh_token) localStorage.setItem(REFRESH_KEY, data.refresh_token)
  return newAccess
}
