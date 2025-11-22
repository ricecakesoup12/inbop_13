<template>
  <AppCard>
    <div class="UserVitalsNowContent">
      <h3 class="UserVitalsNowTitle">
        <svg class="UserVitalsNowIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
        </svg>
        실시간 바이탈
      </h3>
      <div class="UserVitalsNowValueWrapper">
        <div class="UserVitalsNowValue" :class="(vital?.hr && vital.hr > 0) ? 'UserVitalsNowValueActive' : 'UserVitalsNowValueInactive'">
          {{ (vital?.hr && vital.hr > 0) ? vital.hr : '-' }}
        </div>
        <div class="UserVitalsNowLabel">심박수 (bpm)</div>
      </div>
      <div v-if="!vital?.hr || vital.hr === 0" class="UserVitalsNowWaiting">
        <p>센서 연결 대기 중...</p>
      </div>
    </div>
  </AppCard>
</template>

<script setup lang="ts">
import type { VitalNow } from '@/types/metrics'
import AppCard from '@/components/common/AppCard.vue'

defineProps<{ vital?: VitalNow | null }>()
</script>

