<template>
  <el-dialog :model-value="visible" width="360px" :show-close="false" align-center>
    <div class="modal-body">
      <div class="icon-wrap">
        <span class="icon-text">!</span>
      </div>
      <p class="modal-title">공유를 중지할까요?</p>
      <p class="modal-desc">공유 링크가 비활성화되어 더 이상 접근할 수 없게 됩니다.</p>
    </div>
    <template #footer>
      <div class="modal-footer">
        <el-button @click="emit('cancel')">취소</el-button>
        <el-button type="danger" @click="emit('confirm')" :loading="loading">중지</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
defineProps({
  visible: { type: Boolean, required: true },
  loading: { type: Boolean, default: false },
})

const emit = defineEmits(['cancel', 'confirm'])
</script>

<style scoped>
.modal-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 16px 0 0;
}

.icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background-color: #fce4ec;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.icon-text {
  font-size: 24px;
  font-weight: 700;
  color: #e91e63;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.modal-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.modal-footer {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
