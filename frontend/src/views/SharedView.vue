<template>
  <!-- 오류 상태 [8] -->
  <div v-if="error" class="error-page">
    <div class="error-card">
      <div class="icon-wrap">
        <span class="icon-text">!</span>
      </div>
      <p class="error-title">공유 링크를 찾을 수 없습니다</p>
      <p class="error-desc">만료되었거나 취소된 링크입니다. 공유한 분에게 다시 요청해보세요.</p>
      <RouterLink to="/"><el-button class="btn-home">홈으로 이동</el-button></RouterLink>
    </div>
  </div>

  <!-- 정상 상태 [7] -->
  <div v-else class="shared-page">
    <header class="shared-header">
      <span class="logo">CinemaNote</span>
      <h2 class="shared-title">{{ pageTitle }}</h2>
    </header>

    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="3" animated />
    </div>
    <div v-else-if="archives.length === 0">
      <el-empty description="아카이브가 없습니다." />
    </div>
    <div v-else>
      <div class="archive-grid">
        <ArchiveCard
          v-for="archive in archives"
          :key="archive.title + archive.rating"
          :archive="archive"
          :readonly="true"
        />
      </div>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadPage"
        />
      </div>
    </div>

    <div class="shared-footer">
      <RouterLink to="/signup">나도 CinemaNote 시작하기 →</RouterLink>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSharedArchives } from '../api/share.js'
import ArchiveCard from '../components/ArchiveCard.vue'

const route = useRoute()
const token = route.params.token

const archives = ref([])
const loading = ref(false)
const error = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const nickname = ref('')

const pageTitle = computed(() => nickname.value ? `${nickname.value}의 아카이브` : '아카이브')

async function loadPage(page = 1) {
  loading.value = true
  try {
    const data = await getSharedArchives(token, page - 1)
    archives.value = data.content
    total.value = data.totalElements
    nickname.value = data.nickname
    currentPage.value = page
  } catch (err) {
    error.value = true
    ElMessage.error(err.message)
  } finally {
    loading.value = false
  }
}

onMounted(() => loadPage())
</script>

<style scoped>
.shared-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px;
}

.shared-header {
  text-align: center;
  margin-bottom: 28px;
}

.logo {
  font-size: 18px;
  font-weight: 700;
  display: block;
  margin-bottom: 4px;
  color: #888;
}

.shared-title {
  font-size: 22px;
  font-weight: 700;
}

.archive-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.shared-footer {
  text-align: center;
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #eee;
}

.shared-footer a {
  font-size: 15px;
  color: #1a1a1a;
  font-weight: 600;
  text-decoration: none;
}

.shared-footer a:hover {
  text-decoration: underline;
}

/* 오류 페이지 */
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.error-card {
  text-align: center;
  max-width: 360px;
}

.icon-wrap {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background-color: #fce4ec;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.icon-text {
  font-size: 28px;
  font-weight: 700;
  color: #e91e63;
}

.error-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 10px;
}

.error-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 24px;
}

.btn-home {
  background-color: #1a1a1a;
  border-color: #1a1a1a;
  color: #fff;
}

.loading-wrap {
  padding: 24px 0;
}
</style>
