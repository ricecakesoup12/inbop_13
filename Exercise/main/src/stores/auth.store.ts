import { defineStore } from 'pinia'

export type UserRole = 'guardian' | 'user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    role: null as UserRole | null,
    token: null as string | null,
    userId: null as string | null,
  }),

  actions: {
    setRole(role: UserRole) {
      this.role = role
      localStorage.setItem('user_role', role)
    },

    setToken(token: string) {
      this.token = token
      localStorage.setItem('auth_token', token)
    },

    setUserId(userId: string) {
      this.userId = userId
      localStorage.setItem('user_id', userId)
    },

    logout() {
      this.role = null
      this.token = null
      this.userId = null
      localStorage.removeItem('user_role')
      localStorage.removeItem('auth_token')
      localStorage.removeItem('user_id')
    },

    loadFromStorage() {
      this.role = localStorage.getItem('user_role') as UserRole | null
      this.token = localStorage.getItem('auth_token')
      this.userId = localStorage.getItem('user_id')
    },
  },
})

