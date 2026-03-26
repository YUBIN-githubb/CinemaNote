<template>
  <div v-if="archive" class="detail-page">
    <div class="detail-top">
      <div class="poster-wrap">
        <img
          v-if="archive.posterPath"
          :src="`https://image.tmdb.org/t/p/w300${archive.posterPath}`"
          :alt="archive.title"
          class="poster-img"
        />
        <div v-else class="poster-placeholder"></div>
      </div>
      <div class="detail-info">
        <h2 class="detail-title">{{ archive.title }}</h2>
        <p class="detail-meta">{{ formatDate(archive.releaseDate) }}</p>
        <p class="detail-rating">★ {{ archive.rating }} / 10.0</p>
        <p class="detail-recorded">{{ formatDateTime(archive.createdAt) }} 기록</p>
      </div>
    </div>

    <div class="review-section">
      <p class="review-label">내 한줄평</p>
      <p class="review-text">{{ archive.review || '한줄평이 없습니다.' }}</p>
    </div>

    <div class="action-row">
      <el-button @click="router.push(`/archive/${archive.id}/edit`)">수정</el-button>
      <el-button type="danger" @click="handleDelete" :loading="deleting">삭제</el-button>
    </div>
  </div>

  <div v-else-if="loading" class="loading-wrap">
    <el-skeleton :rows="5" animated />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getArchive, deleteArchive } from '../api/archive.js'

const route = useRoute()
const router = useRouter()
const archive = ref(null)
const loading = ref(false)
const deleting = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    archive.value = await getArchive(route.params.id)
  } catch (err) {
    ElMessage.error(err.message)
    router.push('/')
  } finally {
    loading.value = false
  }
})

async function handleDelete() {
  try {
    await ElMessageBox.confirm('아카이브를 삭제하시겠습니까?', '삭제 확인', {
      confirmButtonText: '삭제',
      cancelButtonText: '취소',
      type: 'warning',
    })
  } catch {
    return
  }
  deleting.value = true
  try {
    await deleteArchive(route.params.id)
    ElMessage.success('삭제되었습니다.')
    router.push('/')
  } catch (err) {
    ElMessage.error(err.message)
  } finally {
    deleting.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.slice(0, 4) + '년'
}

function formatDateTime(dt) {
  if (!dt) return ''
  return dt.slice(0, 10).replace(/-/g, '.')
}
</script>

<style scoped>
.detail-page {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.detail-top {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
}

.poster-wrap {
  flex-shrink: 0;
}

.poster-img {
  width: 120px;
  border-radius: 8px;
  display: block;
}

.poster-placeholder {
  width: 120px;
  height: 180px;
  background: #ddd;
  border-radius: 8px;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-title {
  font-size: 20px;
  font-weight: 700;
}

.detail-meta {
  font-size: 14px;
  color: #888;
}

.detail-rating {
  font-size: 18px;
  font-weight: 600;
  color: #f5a623;
}

.detail-recorded {
  font-size: 13px;
  color: #aaa;
}

.review-section {
  border-top: 1px solid #eee;
  padding-top: 20px;
  margin-bottom: 24px;
}

.review-label {
  font-size: 13px;
  font-weight: 600;
  color: #888;
  margin-bottom: 8px;
}

.review-text {
  font-size: 15px;
  line-height: 1.7;
  color: #333;
  white-space: pre-wrap;
}

.action-row {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.loading-wrap {
  padding: 24px 0;
}
</style>
