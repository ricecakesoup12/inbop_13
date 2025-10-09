import { defineStore } from 'pinia'
import type { User } from '@/types/user'
import * as usersApi from '@/services/api/users'

export const useUsersStore = defineStore('users', {
  state: () => ({
    list: [] as User[],
    detail: null as User | null,
    loading: false,
    error: null as string | null,
  }),

  actions: {
    async fetchList() {
      this.loading = true
      this.error = null
      try {
        this.list = await usersApi.getUsers()
      } catch (error) {
        this.error = '사용자 목록을 불러오는데 실패했습니다.'
        console.error(error)
      } finally {
        this.loading = false
      }
    },

    async fetchDetail(id: string) {
      this.loading = true
      this.error = null
      try {
        this.detail = await usersApi.getUser(id)
      } catch (error) {
        this.error = '사용자 정보를 불러오는데 실패했습니다.'
        console.error(error)
      } finally {
        this.loading = false
      }
    },

    async createUser(user: Omit<User, 'id'>) {
      this.loading = true
      this.error = null
      try {
        const newUser = await usersApi.createUser(user)
        this.list.push(newUser)
        this.detail = newUser
        return newUser
      } catch (error) {
        this.error = '사용자 등록에 실패했습니다.'
        console.error(error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async deleteUser(id: string) {
      this.loading = true
      this.error = null
      try {
        await usersApi.deleteUser(id)
        this.list = this.list.filter((u) => u.id !== id)
        if (this.detail?.id === id) {
          this.detail = null
        }
      } catch (error) {
        this.error = '사용자 삭제에 실패했습니다.'
        console.error(error)
      } finally {
        this.loading = false
      }
    },
  },
})

