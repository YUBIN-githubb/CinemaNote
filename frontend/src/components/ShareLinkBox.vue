<template>
  <div class="share-link-box">
    <!-- 3-A: 링크 없음 -->
    <div v-if="!shareToken" class="state-inactive">
      <el-button class="btn-create-share" @click="handleCreate" :loading="creating">
        공유 링크 생성
      </el-button>
    </div>

    <!-- 3-B: 링크 활성화 -->
    <div v-else class="state-active">
      <p class="share-title">공유 링크 활성화됨</p>
      <div class="share-url-row">
        <el-input :model-value="shareUrl" readonly class="share-url-input" />
        <el-button @click="copyUrl">복사</el-button>
      </div>
      <button class="stop-link" @click="showModal = true">공유 중지</button>
    </div>

    <StopShareModal
      :visible="showModal"
      :loading="stopping"
      @cancel="showModal = false"
      @confirm="handleStop"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { createShareLink, deleteShareLink } from '../api/share.js'
import StopShareModal from './StopShareModal.vue'

const STORAGE_KEY = 'shareToken'

const shareToken = ref(null)
const creating = ref(false)
const stopping = ref(false)
const showModal = ref(false)

const shareUrl = computed(() =>
  shareToken.value ? `${window.location.origin}/share/${shareToken.value}` : ''
)

onMounted(() => {
  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored) shareToken.value = stored
})

async function handleCreate() {
  creating.value = true
  try {
    const data = await createShareLink()
    shareToken.value = data.shareToken
    localStorage.setItem(STORAGE_KEY, data.shareToken)
  } catch (err) {
    ElMessage.error(err.message)
  } finally {
    creating.value = false
  }
}

async function handleStop() {
  stopping.value = true
  try {
    await deleteShareLink()
    shareToken.value = null
    localStorage.removeItem(STORAGE_KEY)
    showModal.value = false
    ElMessage.success('공유가 중지되었습니다.')
  } catch (err) {
    ElMessage.error(err.message)
  } finally {
    stopping.value = false
  }
}

function copyUrl() {
  navigator.clipboard.writeText(shareUrl.value).then(() => {
    ElMessage.success('링크가 복사되었습니다.')
  })
}
</script>

<style scoped>
.share-link-box {
  margin-bottom: 24px;
}

.btn-create-share {
  width: 100%;
  border: 1.5px dashed #aaa;
  color: #555;
  background: transparent;
  padding: 14px;
  font-size: 15px;
  height: auto;
}

.state-active {
  background: #e3f2fd;
  border-radius: 10px;
  padding: 16px;
}

.share-title {
  font-weight: 600;
  font-size: 14px;
  color: #1565c0;
  margin-bottom: 10px;
}

.share-url-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 10px;
}

.share-url-input {
  flex: 1;
}

.stop-link {
  background: none;
  border: none;
  color: #e53935;
  font-size: 13px;
  cursor: pointer;
  text-decoration: underline;
  padding: 0;
}
</style>
