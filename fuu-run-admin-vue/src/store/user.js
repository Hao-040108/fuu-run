import { defineStore } from 'pinia'
import { login, getInfo } from '../api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || '',
    user: null,
    permissions: [],
    config: null
  }),
  actions: {
    async doLogin(username, password) {
      const res = await login({ username, password })
      this.token = res.data.token
      this.username = username
      localStorage.setItem('token', this.token)
      localStorage.setItem('username', username)
      await this.fetchInfo()
      return res
    },
    async fetchInfo() {
      const res = await getInfo()
      this.user = res.data.user
      this.permissions = res.data.permissions || []
      this.config = res.data.config || null
      return res.data
    },
    logout() {
      this.token = ''
      this.username = ''
      this.user = null
      this.permissions = []
      this.config = null
      localStorage.removeItem('token')
      localStorage.removeItem('username')
    }
  }
})
