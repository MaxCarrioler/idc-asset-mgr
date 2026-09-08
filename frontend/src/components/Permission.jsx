import { useAuth } from '../stores/AuthContext'

/**
 * Permission component — renders children only if the current user has
 * at least one of the required permissions.
 *
 * Usage:
 *   <Permission require="accessory:transfer:apply">
 *     <Button>新建申请</Button>
 *   </Permission>
 *
 *   <Permission require={['perm:a', 'perm:b']}>
 *     <Button>操作</Button>
 *   </Permission>
 */
export default function Permission({ require, children, fallback = null }) {
  const { hasPermission } = useAuth()

  if (!require) return children

  const perms = Array.isArray(require) ? require : [require]
  if (!perms.length) return children

  if (!hasPermission(...perms)) return fallback

  return children
}
