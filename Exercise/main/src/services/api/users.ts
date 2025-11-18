import { http } from './http'
import type { User } from '@/types/user'

export async function getUsers(): Promise<User[]> {
  const { data } = await http.get('/users')
  return data
}

export async function getUser(id: string): Promise<User> {
  const { data } = await http.get(`/users/${id}`)
  return data
}

export async function createUser(user: Omit<User, 'id'>): Promise<User> {
  const { data } = await http.post('/users', user)
  return data
}

export async function deleteUser(id: string): Promise<void> {
  await http.delete(`/users/${id}`)
}

export async function updateUser(id: string, user: Partial<User>): Promise<User> {
  const { data } = await http.put(`/users/${id}`, user)
  return data
}

export async function getUserByCode(userCode: string): Promise<User> {
  const { data } = await http.get(`/users/code/${userCode}`)
  return data
}

export async function checkUserCode(userCode: string): Promise<boolean> {
  const { data } = await http.get(`/users/code-check/${userCode}`)
  return data
}

