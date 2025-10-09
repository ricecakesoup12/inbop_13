<template>
  <AppCard>
    <div class="p-4">
      <h3 class="font-semibold text-text-main mb-4 flex items-center gap-2 font-gowun">
        <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
        </svg>
        실시간 바이탈
      </h3>
      <div class="grid grid-cols-3 gap-4">
        <div class="text-center">
          <div class="text-2xl font-bold text-primary font-gowun">{{ vital?.hr || 0 }}</div>
          <div class="text-xs text-text-sub mt-1 font-gowun">심박수 (bpm)</div>
        </div>
        <div class="text-center">
          <div class="text-2xl font-bold text-primary font-gowun">{{ vital?.spo2 || 0 }}</div>
          <div class="text-xs text-text-sub mt-1 font-gowun">SpO₂ (%)</div>
        </div>
        <div class="text-center">
          <div class="text-2xl font-bold font-gowun" :class="vital?.active ? 'text-primary' : 'text-gray-400'">
            {{ vital?.active ? '운동중' : '휴식중' }}
          </div>
          <div class="text-xs text-text-sub mt-1 font-gowun">운동 상태</div>
        </div>
      </div>
    </div>
  </AppCard>
</template>

<script setup lang="ts">
import type { VitalNow } from '@/types/metrics'
import AppCard from '@/components/common/AppCard.vue'

defineProps<{ vital?: VitalNow | null }>()
</script>

