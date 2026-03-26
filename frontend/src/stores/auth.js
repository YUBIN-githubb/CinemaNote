import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { signout as apiSignout } from '../api/auth.js'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const isLoggedIn = computed(() => user.value !== null)

  function login(userData) {
    user.value = userData
    localStorage.setItem('user', JSON.stringify(userData))
  }

  async function logout() {
    await apiSignout()
    user.value = null
    localStorage.removeItem('user')
  }

  function restoreFromStorage() {
    const stored = localStorage.getItem('user')
    if (stored) {
      user.value = JSON.parse(stored)
    }
  }

  return { user, isLoggedIn, login, logout, restoreFromStorage }
})
