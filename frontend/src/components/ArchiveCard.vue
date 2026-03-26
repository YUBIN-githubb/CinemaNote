<template>
  <div class="archive-card" @click="handleClick">
    <div class="poster-wrap">
      <img
        v-if="archive.posterPath"
        :src="`https://image.tmdb.org/t/p/w300${archive.posterPath}`"
        :alt="archive.title"
        class="poster-img"
      />
      <div v-else class="poster-placeholder"></div>
    </div>
    <div class="card-body">
      <p class="card-title">{{ archive.title }}</p>
      <p class="card-rating">★ {{ archive.rating }}</p>
      <p class="card-review">{{ archive.review }}</p>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  archive: { type: Object, required: true },
  readonly: { type: Boolean, default: false },
})

const router = useRouter()

function handleClick() {
  if (!props.readonly) {
    router.push(`/archive/${props.archive.id}`)
  }
}
</script>

<style scoped>
.archive-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
}

.archive-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.poster-wrap {
  width: 100%;
  aspect-ratio: 2/3;
  background: #ddd;
}

.poster-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.poster-placeholder {
  width: 100%;
  height: 100%;
  background: #ccc;
}

.card-body {
  padding: 12px;
}

.card-title {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-rating {
  font-size: 13px;
  color: #f5a623;
  margin-bottom: 6px;
}

.card-review {
  font-size: 13px;
  color: #555;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
