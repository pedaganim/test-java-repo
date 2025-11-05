import { useEffect, useMemo, useState } from 'react'
import type { User } from './types'
import { listUsers, createUser, updateUser, deleteUser } from './api'

type Draft = Omit<User, 'id'>

export default function App() {
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [showAdd, setShowAdd] = useState(false)

  useEffect(() => {
    (async () => {
      try {
        setLoading(true)
        const data = await listUsers()
        setUsers(data)
      } catch (e: any) {
        setError(e?.message ?? 'Failed to load users')
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  const onCreate = async (draft: Draft) => {
    const created = await createUser(draft)
    setUsers(prev => [created, ...prev])
    setShowAdd(false)
  }

  const onUpdate = async (id: number, draft: Draft) => {
    const updated = await updateUser(id, draft)
    setUsers(prev => prev.map(u => (u.id === id ? updated : u)))
    setEditingId(null)
  }

  const onDelete = async (id: number) => {
    await deleteUser(id)
    setUsers(prev => prev.filter(u => u.id !== id))
  }

  return (
    <div style={styles.page}>
      <header style={styles.header}>
        <h1 style={styles.title}>Users</h1>
        <button style={styles.primaryBtn} onClick={() => setShowAdd(true)}>Add User</button>
      </header>

      {loading && <div style={styles.banner}>Loading…</div>}
      {error && <div style={{ ...styles.banner, background: '#fee2e2', color: '#991b1b' }}>{error}</div>}

      {!loading && !error && (
        <table style={styles.table}>
          <thead>
            <tr>
              <th style={styles.th}>Name</th>
              <th style={styles.th}>Email</th>
              <th style={styles.th}>Role</th>
              <th style={styles.th} aria-label="actions">Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map(u => (
              <Row key={u.id} user={u}
                   editing={editingId === u.id}
                   onEdit={() => setEditingId(u.id!)}
                   onCancel={() => setEditingId(null)}
                   onSave={(draft) => onUpdate(u.id!, draft)}
                   onDelete={() => onDelete(u.id!)} />
            ))}
            {users.length === 0 && (
              <tr><td style={styles.td} colSpan={4}>No users yet.</td></tr>
            )}
          </tbody>
        </table>
      )}

      {showAdd && (
        <Modal onClose={() => setShowAdd(false)} title="Add User">
          <UserForm initial={{ name: '', email: '', role: '' }}
                    submitLabel="Create"
                    onSubmit={onCreate}
                    onCancel={() => setShowAdd(false)} />
        </Modal>
      )}
    </div>
  )
}

function Row({ user, editing, onEdit, onCancel, onSave, onDelete }: {
  user: User
  editing: boolean
  onEdit: () => void
  onCancel: () => void
  onSave: (u: Draft) => void
  onDelete: () => void
}) {
  const [draft, setDraft] = useState<Draft>({ name: user.name, email: user.email, role: user.role })

  useEffect(() => {
    setDraft({ name: user.name, email: user.email, role: user.role })
  }, [user])

  if (!editing) {
    return (
      <tr>
        <td style={styles.td}>{user.name}</td>
        <td style={styles.td}>{user.email}</td>
        <td style={styles.td}><span style={styles.badge}>{user.role}</span></td>
        <td style={styles.tdRight}>
          <button style={styles.secondaryBtn} onClick={onEdit}>Edit</button>
          <button style={styles.dangerBtn} onClick={onDelete}>Delete</button>
        </td>
      </tr>
    )
  }

  return (
    <tr>
      <td style={styles.td}><input style={styles.input} value={draft.name} onChange={e => setDraft({ ...draft, name: e.target.value })} /></td>
      <td style={styles.td}><input style={styles.input} value={draft.email} onChange={e => setDraft({ ...draft, email: e.target.value })} /></td>
      <td style={styles.td}><input style={styles.input} value={draft.role} onChange={e => setDraft({ ...draft, role: e.target.value })} /></td>
      <td style={styles.tdRight}>
        <button style={styles.primaryBtn} onClick={() => onSave(draft)}>Save</button>
        <button style={styles.secondaryBtn} onClick={onCancel}>Cancel</button>
      </td>
    </tr>
  )
}

function UserForm({ initial, submitLabel, onSubmit, onCancel }: {
  initial: Draft
  submitLabel: string
  onSubmit: (u: Draft) => void
  onCancel: () => void
}) {
  const [draft, setDraft] = useState<Draft>(initial)
  const valid = useMemo(() => draft.name.trim() && draft.email.trim() && draft.role.trim(), [draft])
  return (
    <form onSubmit={e => { e.preventDefault(); onSubmit(draft) }}>
      <div style={styles.formRow}><label style={styles.label}>Name</label><input style={styles.input} value={draft.name} onChange={e => setDraft({ ...draft, name: e.target.value })} /></div>
      <div style={styles.formRow}><label style={styles.label}>Email</label><input style={styles.input} type="email" value={draft.email} onChange={e => setDraft({ ...draft, email: e.target.value })} /></div>
      <div style={styles.formRow}><label style={styles.label}>Role</label><input style={styles.input} value={draft.role} onChange={e => setDraft({ ...draft, role: e.target.value })} /></div>
      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 12 }}>
        <button type="button" style={styles.secondaryBtn} onClick={onCancel}>Cancel</button>
        <button type="submit" style={styles.primaryBtn} disabled={!valid}>{submitLabel}</button>
      </div>
    </form>
  )
}

function Modal({ title, children, onClose }: { title: string, children: any, onClose: () => void }) {
  return (
    <div style={styles.modalBackdrop} onClick={onClose}>
      <div style={styles.modal} onClick={e => e.stopPropagation()}>
        <div style={styles.modalHeader}>
          <h2 style={{ margin: 0 }}>{title}</h2>
          <button style={styles.iconBtn} onClick={onClose}>✕</button>
        </div>
        {children}
      </div>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  page: { maxWidth: 960, margin: '0 auto', padding: 24, fontFamily: 'Inter, system-ui, -apple-system, Segoe UI, Roboto, Ubuntu, Cantarell, Noto Sans, Helvetica Neue, Arial' },
  header: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 },
  title: { fontSize: 24, fontWeight: 700 },
  table: { width: '100%', borderCollapse: 'separate', borderSpacing: 0, background: 'white', borderRadius: 8, overflow: 'hidden', boxShadow: '0 1px 2px rgba(0,0,0,0.06)' },
  th: { textAlign: 'left', padding: '12px 16px', background: '#f8fafc', borderBottom: '1px solid #e2e8f0', color: '#334155' },
  td: { padding: '12px 16px', borderBottom: '1px solid #e2e8f0' },
  tdRight: { padding: '12px 16px', borderBottom: '1px solid #e2e8f0', textAlign: 'right', whiteSpace: 'nowrap' },
  primaryBtn: { background: '#2563eb', color: 'white', border: 'none', padding: '8px 12px', borderRadius: 6, cursor: 'pointer' },
  secondaryBtn: { background: '#e2e8f0', color: '#0f172a', border: 'none', padding: '8px 12px', borderRadius: 6, cursor: 'pointer' },
  dangerBtn: { background: '#ef4444', color: 'white', border: 'none', padding: '8px 12px', borderRadius: 6, cursor: 'pointer' },
  badge: { background: '#e0e7ff', color: '#3730a3', padding: '2px 8px', borderRadius: 999 },
  banner: { background: '#f1f5f9', color: '#0f172a', padding: 12, borderRadius: 8, marginBottom: 12 },
  modalBackdrop: { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.3)', display: 'grid', placeItems: 'center', padding: 16 },
  modal: { background: 'white', borderRadius: 12, padding: 16, width: 'min(520px, 96vw)', boxShadow: '0 10px 40px rgba(0,0,0,0.2)' },
  modalHeader: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 },
  iconBtn: { background: 'transparent', border: 'none', fontSize: 18, cursor: 'pointer' },
  formRow: { display: 'flex', alignItems: 'center', gap: 8, marginBottom: 10 },
  label: { color: '#475569', width: 120, minWidth: 120 },
  input: { width: '100%', padding: '8px 10px', border: '1px solid #cbd5e1', borderRadius: 6, flex: 1 },
}
