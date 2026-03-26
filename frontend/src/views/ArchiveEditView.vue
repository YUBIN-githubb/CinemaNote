<template>
  <div v-if="archive" class="edit-page">
    <h2 class="page-title">아카이브 수정</h2>

    <div class="info-card">
      <p class="info-title">{{ archive.title }}</p>
      <p class="info-meta">{{ formatDate(archive.releaseDate) }}</p>
    </div>

    <div class="form-section">
      <div class="rating-row">
        <span class="rating-label">별점</span>
        <el-slider v-model="rating" :min="0" :max="10" :step="0.5" class="rating-slider" />
        <span class="rating-value">{{ rating }}</span>
      </div>
      <el-input
        v-model="review"
        type="textarea"
        placeholder="한줄평을 남겨보세요 (최대 500자)"
        :rows="4"
        maxlength="500"
        show-word-limit
      />
      <div class="button-row">
        <el-button @click="router.push(`/archive/${route.params.id}`)">취소</el-button>
        <el-button class="btn-save" :loading="saving" @click="handleSave">저장</el-button>
      </div>
    </div>
  </div>

  <div v-else-if="loading" class="loading-wrap">
    <el-skeleton :rows="4" animated />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getArchive, updateArchive } from '../api/archive.js'

const route = useRoute()
const router = useRouter()
const archive = ref(null)
const rating = ref(7)
const review = ref('')
const loading = ref(false)
const saving = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    archive.value = await getArchive(route.params.id)
    rating.value = archive.value.rating
    review.value = archive.value.review || ''
  } catch (err) {
    ElMessage.error(err.message)
    router.push('/')
  } finally {
    loading.value = false
  }
})

async function handleSave() {
  saving.value = true
  try {
    await updateArchive(route.params.id, { rating: rating.value, review: review.value })
    ElMessage.success('수정되었습니다.')
    router.push(`/archive/${route.params.id}`)
  } catch (err) {
    ElMessage.error(err.message)
  } finally {
    saving.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.slice(0, 4) + '년'
}
</script>

<style scoped>
.edit-page {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 20px;
}

.info-card {
  background: #f8f8f8;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 24px;
}

.info-title {
  font-size: 16px;
  font-weight: 600;
}

.info-meta {
  font-size: 13px;
  color: #888;
  margin-top: 4px;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rating-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rating-label {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

.rating-slider {
  flex: 1;
}

.rating-value {
  font-size: 16px;
  font-weight: 600;
  width: 32px;
  text-align: right;
}

.button-row {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-save {
  background-color: #1a1a1a;
  border-color: #1a1a1a;
  color: #fff;
}

.loading-wrap {
  padding: 24px 0;
}
</style>
