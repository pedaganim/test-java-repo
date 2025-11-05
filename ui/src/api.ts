import type { User } from './types'

const headers = {
  'Content-Type': 'application/json'
}

export async function listUsers(): Promise<User[]> {
  const res = await fetch('/api/users')
  if (!res.ok) throw new Error('Failed to load users')
  return res.json()
}

export async function createUser(user: Omit<User, 'id'>): Promise<User> {
  const res = await fetch('/api/users', { method: 'POST', headers, body: JSON.stringify(user) })
  if (!res.ok) throw new Error('Failed to create user')
  return res.json()
}

export async function updateUser(id: number, user: Omit<User, 'id'>): Promise<User> {
  const res = await fetch(`/api/users/${id}`, { method: 'PUT', headers, body: JSON.stringify(user) })
  if (!res.ok) throw new Error('Failed to update user')
  return res.json()
}

export async function deleteUser(id: number): Promise<void> {
  const res = await fetch(`/api/users/${id}`, { method: 'DELETE' })
  if (!res.ok) throw new Error('Failed to delete user')
}
