<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1 class="logo">CinemaNote</h1>
      <p class="subtitle">로그인</p>

      <el-form @submit.prevent="handleLogin">
        <el-form-item>
          <el-input v-model="email" placeholder="이메일" type="email" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="password" placeholder="비밀번호" type="password" size="large" show-password />
        </el-form-item>
        <el-button
          class="btn-submit"
          size="large"
          :loading="loading"
          @click="handleLogin"
        >
          로그인
        </el-button>
      </el-form>

      <p class="auth-link">
        계정이 없나요?
        <RouterLink to="/signup">회원가입</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { signin } from '../api/auth.js'
import { useAuthStore } from '../stores/auth.js'

const router = useRouter()
const authStore = useAuthStore()
const email = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  if (!email.value || !password.value) {
    ElMessage.error('이메일과 비밀번호를 입력해주세요.')
    return
  }
  loading.value = true
  try {
    const userData = await signin({ email: email.value, password: password.value })
    authStore.login(userData)
    router.push('/')
  } catch (err) {
    ElMessage.error(err.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - 60px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
}

.auth-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px 36px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 28px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 6px;
}

.subtitle {
  text-align: center;
  color: #888;
  margin-bottom: 28px;
  font-size: 15px;
}

.btn-submit {
  width: 100%;
  background-color: #1a1a1a;
  border-color: #1a1a1a;
  color: #fff;
  margin-top: 4px;
  font-size: 16px;
}

.btn-submit:hover {
  background-color: #333;
  border-color: #333;
}

.auth-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.auth-link a {
  color: #1a1a1a;
  font-weight: 600;
  margin-left: 4px;
}
</style>
