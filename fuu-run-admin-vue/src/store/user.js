import { defineStore } from 'pinia'
import { login } from '../api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || ''
  }),
  actions: {
    async doLogin(username, password) {
      const res = await login({ username, password })
      this.token = res.data.token
      this.username = username
      localStorage.setItem('token', this.token)
      localStorage.setItem('username', username)
      return res
    },
    logout() {
      this.token = ''
      this.username = ''
      localStorage.removeItem('token')
      localStorage.removeItem('username')
    }
  }
})
