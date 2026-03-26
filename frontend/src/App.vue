<template>
  <AppHeader />
  <main class="main-container">
    <RouterView />
  </main>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from './stores/auth.js'
import AppHeader from './components/AppHeader.vue'

const authStore = useAuthStore()

onMounted(() => {
  authStore.restoreFromStorage()
})
</script>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Pretendard', 'Apple SD Gothic Neo', sans-serif;
  background-color: #f5f5f5;
  color: #1a1a1a;
}

.main-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px;
}
</style>
