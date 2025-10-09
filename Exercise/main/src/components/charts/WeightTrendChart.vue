<template>
  <AppCard>
    <div class="p-4">
      <h3 class="font-semibold text-text-main mb-3 font-gowun">몸무게 변화</h3>
      <div class="h-48">
        <Line v-if="chartData" :data="chartData" :options="options" />
      </div>
    </div>
  </AppCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Line } from 'vue-chartjs'
import AppCard from '@/components/common/AppCard.vue'
import '@/components/charts/_LineChartBase'

const props = defineProps<{ data: { x: string; y: number }[] }>()

const chartData = computed(() => ({
  labels: props.data.map((d: { x: string; y: number }) => d.x),
  datasets: [
    {
      label: '몸무게 (kg)',
      data: props.data.map((d: { x: string; y: number }) => d.y),
      borderColor: '#8BC34A',
      backgroundColor: 'rgba(139, 195, 74, 0.1)',
      tension: 0.4,
      fill: true,
    },
  ],
}))

const options = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      padding: 12,
      titleFont: { family: 'Gowun Dodum', size: 14 },
      bodyFont: { family: 'Gowun Dodum', size: 13 },
    },
  },
  scales: {
    y: { beginAtZero: false, ticks: { font: { family: 'Gowun Dodum' } } },
    x: { ticks: { font: { family: 'Gowun Dodum' } } },
  },
}
</script>

