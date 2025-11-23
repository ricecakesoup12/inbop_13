<template>
  <AppCard>
    <div class="HeartRateAlertChartContent">
      <h3 class="HeartRateAlertChartTitle">심박수 경고 횟수</h3>
      <div class="HeartRateAlertChartContainer">
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

interface WeeklyAlertCount {
  date: string
  count: number
}

const props = defineProps<{ data: WeeklyAlertCount[] }>()

const chartData = computed(() => {
  // WeeklyAlertCount[]를 { x: string, y: number }[] 형식으로 변환
  const formattedData = props.data.map((d: WeeklyAlertCount) => ({
    x: d.date,
    y: d.count
  }))
  
  return {
    labels: formattedData.map((d: { x: string; y: number }) => d.x),
    datasets: [
      {
        label: '경고 횟수',
        data: formattedData.map((d: { x: string; y: number }) => d.y),
        borderColor: '#F44336',
        backgroundColor: 'rgba(244, 67, 54, 0.1)',
        tension: 0.4,
        fill: true,
      },
    ],
  }
})

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
    y: { 
      beginAtZero: true, 
      ticks: { 
        font: { family: 'Gowun Dodum' },
        stepSize: 1,
      } 
    },
    x: { ticks: { font: { family: 'Gowun Dodum' } } },
  },
}
</script>

