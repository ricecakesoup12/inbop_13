import { onUnmounted } from 'vue'
import { useMetricsStore } from '@/stores/metrics.store'

export function useRealtime(userId: string) {
  const metricsStore = useMetricsStore()

  // 실시간 연결 시작
  metricsStore.subscribeRealtime(userId)

  // 컴포넌트 언마운트 시 연결 해제
  onUnmounted(() => {
    metricsStore.unsubscribeRealtime()
  })

  return {
    vitalNow: metricsStore.vitalNow,
  }
}

