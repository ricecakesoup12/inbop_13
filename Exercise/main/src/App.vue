<template>
  <div id="app" class="AppRoot">
    <RouterView />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth.store'

const authStore = useAuthStore()

onMounted(() => {
  // 로컬 스토리지에서 인증 정보 로드
  authStore.loadFromStorage()
})
</script>

<style>
@import './assets/styles/tailwind.css';
@import './assets/styles/base.css';
</style>

