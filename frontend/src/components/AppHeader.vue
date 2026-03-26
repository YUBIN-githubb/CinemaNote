<template>
  <header class="app-header">
    <div class="header-inner">
      <RouterLink to="/" class="logo">CinemaNote</RouterLink>
      <nav v-if="!isSharedView && authStore.isLoggedIn" class="header-nav">
        <RouterLink to="/archive/new">
          <el-button type="primary" class="btn-dark">+ 새 아카이브</el-button>
        </RouterLink>
        <el-button text @click="handleLogout">로그아웃</el-button>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isSharedView = computed(() => route.path.startsWith('/share/'))

async function handleLogout() {
  try {
    await authStore.logout()
    router.push('/login')
  } catch {
    ElMessage.error('로그아웃에 실패했습니다.')
  }
}
</script>

<style scoped>
.app-header {
  background-color: #fff;
  border-bottom: 1px solid #e5e5e5;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 16px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  text-decoration: none;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-dark {
  background-color: #1a1a1a;
  border-color: #1a1a1a;
  color: #fff;
}

.btn-dark:hover {
  background-color: #333;
  border-color: #333;
}
</style>
