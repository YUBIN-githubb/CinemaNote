<template>
  <div>
    <h2 class="page-title">새 아카이브</h2>

    <div class="search-section">
      <el-radio-group v-model="contentType" class="type-radio">
        <el-radio-button value="MOVIE">영화</el-radio-button>
        <el-radio-button value="TV">드라마</el-radio-button>
      </el-radio-group>

      <el-input
        v-model="query"
        :placeholder="contentType === 'MOVIE' ? '영화 제목을 검색하세요...' : '드라마 제목을 검색하세요...'"
        size="large"
        clearable
        @input="onQueryInput"
      />
    </div>

    <div v-if="searchResults.length > 0" class="search-results">
      <div
        v-for="item in searchResults"
        :key="item.id"
        class="result-item"
        :class="{ selected: selectedItem?.id === item.id }"
        @click="selectItem(item)"
      >
        <img
          v-if="item.posterPath"
          :src="`https://image.tmdb.org/t/p/w92${item.posterPath}`"
          :alt="item.title || item.name"
          class="result-poster"
        />
        <div v-else class="result-poster-placeholder"></div>
        <div class="result-info">
          <p class="result-title">{{ item.title || item.name }}</p>
          <p class="result-year">{{ getYear(item) }}</p>
        </div>
      </div>
    </div>

    <div v-if="selectedItem" class="form-section">
      <div class="rating-row">
        <span class="rating-label">별점</span>
        <el-slider v-model="rating" :min="0" :max="10" :step="0.5" class="rating-slider" />
        <span class="rating-value">{{ rating }}</span>
      </div>
      <el-input
        v-model="review"
        type="textarea"
        placeholder="한줄평을 남겨보세요 (최대 500자)"
        :rows="3"
        maxlength="500"
        show-word-limit
      />
      <el-button class="btn-submit" size="large" :loading="saving" @click="handleSave">
        저장하기
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { searchMovies, searchTv } from '../api/tmdb.js'
import { createArchive } from '../api/archive.js'

const router = useRouter()
const contentType = ref('MOVIE')
const query = ref('')
const searchResults = ref([])
const selectedItem = ref(null)
const rating = ref(7)
const review = ref('')
const saving = ref(false)

let debounceTimer = null

function onQueryInput() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(doSearch, 300)
}

async function doSearch() {
  if (!query.value.trim()) {
    searchResults.value = []
    return
  }
  try {
    const data =
      contentType.value === 'MOVIE'
        ? await searchMovies(query.value)
        : await searchTv(query.value)
    searchResults.value = data.results || []
  } catch (err) {
    ElMessage.error(err.message)
  }
}

watch(contentType, () => {
  query.value = ''
  searchResults.value = []
  selectedItem.value = null
})

function selectItem(item) {
  selectedItem.value = item
}

function getYear(item) {
  const date = item.releaseDate || item.firstAirDate || item.release_date || item.first_air_date
  return date ? date.slice(0, 4) : ''
}

async function handleSave() {
  if (!selectedItem.value) {
    ElMessage.error('작품을 선택해주세요.')
    return
  }
  saving.value = true
  try {
    await createArchive({
      tmdbId: selectedItem.value.id,
      contentType: contentType.value,
      rating: rating.value,
      review: review.value,
    })
    ElMessage.success('아카이브가 저장되었습니다.')
    router.push('/')
  } catch (err) {
    ElMessage.error(err.message)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 20px;
}

.search-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.type-radio {
  align-self: flex-start;
}

.search-results {
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
  max-height: 300px;
  overflow-y: auto;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.15s;
}

.result-item:last-child {
  border-bottom: none;
}

.result-item:hover {
  background: #f5f5f5;
}

.result-item.selected {
  background: #e8f4fd;
}

.result-poster {
  width: 40px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
}

.result-poster-placeholder {
  width: 40px;
  height: 60px;
  background: #ddd;
  border-radius: 4px;
  flex-shrink: 0;
}

.result-title {
  font-size: 14px;
  font-weight: 500;
}

.result-year {
  font-size: 13px;
  color: #888;
  margin-top: 2px;
}

.form-section {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.rating-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
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

.btn-submit {
  width: 100%;
  margin-top: 16px;
  background-color: #1a1a1a;
  border-color: #1a1a1a;
  color: #fff;
  font-size: 16px;
}
</style>
