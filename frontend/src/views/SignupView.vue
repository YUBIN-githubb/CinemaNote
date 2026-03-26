<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1 class="logo">CinemaNote</h1>
      <p class="subtitle">회원가입</p>

      <el-form @submit.prevent="handleSignup">
        <el-form-item>
          <el-input v-model="email" placeholder="이메일" type="email" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="nickname" placeholder="닉네임" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="password" placeholder="비밀번호" type="password" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-input v-model="passwordConfirm" placeholder="비밀번호 확인" type="password" size="large" show-password />
        </el-form-item>
        <el-button
          class="btn-submit"
          native-type="submit"
          size="large"
          :loading="loading"
          @click="handleSignup"
        >
          가입하기
        </el-button>
      </el-form>

      <p class="auth-link">
        이미 계정이 있나요?
        <RouterLink to="/login">로그인</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { signup } from '../api/auth.js'

const router = useRouter()
const email = ref('')
const nickname = ref('')
const password = ref('')
const passwordConfirm = ref('')
const loading = ref(false)

async function handleSignup() {
  if (password.value !== passwordConfirm.value) {
    ElMessage.error('비밀번호가 일치하지 않습니다.')
    return
  }
  if (!email.value || !nickname.value || !password.value) {
    ElMessage.error('모든 항목을 입력해주세요.')
    return
  }
  loading.value = true
  try {
    await signup({ email: email.value, nickname: nickname.value, password: password.value })
    ElMessage.success('회원가입이 완료되었습니다. 로그인해주세요.')
    router.push('/login')
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
