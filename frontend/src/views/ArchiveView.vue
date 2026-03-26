<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">내 아카이브</h2>
      <RouterLink to="/archive/new">
        <el-button class="btn-dark">+ 새 아카이브</el-button>
      </RouterLink>
    </div>

    <ShareLinkBox />

    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="3" animated />
    </div>
    <el-empty v-else-if="archives.length === 0" description="아직 아카이브가 없습니다." />
    <div v-else>
      <div class="archive-grid">
        <ArchiveCard v-for="archive in archives" :key="archive.id" :archive="archive" />
      </div>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadArchives"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getArchives } from '../api/archive.js'
import ArchiveCard from '../components/ArchiveCard.vue'
import ShareLinkBox from '../components/ShareLinkBox.vue'

const archives = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function loadArchives(page = 1) {
  loading.value = true
  try {
    const data = await getArchives(page - 1, pageSize.value)
    archives.value = data.content
    total.value = data.totalElements
    currentPage.value = page
  } catch (err) {
    ElMessage.error(err.message)
  } finally {
    loading.value = false
  }
}

onMounted(() => loadArchives())
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
}

.btn-dark {
  background-color: #1a1a1a;
  border-color: #1a1a1a;
  color: #fff;
}

.archive-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.loading-wrap {
  padding: 24px 0;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
