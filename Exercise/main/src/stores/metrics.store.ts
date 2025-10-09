import { defineStore } from 'pinia'
import type { DailyMetrics, VitalNow } from '@/types/metrics'
import * as metricsApi from '@/services/api/metrics'
import { connect } from '@/services/realtime/socket'
import type { Socket } from 'socket.io-client'

export const useMetricsStore = defineStore('metrics', {
  state: () => ({
    daily: [] as DailyMetrics[],
    vitalNow: { hr: 0, spo2: 0, active: false, ts: '' } as VitalNow,
    socket: null as Socket | null,
    loading: false,
    error: null as string | null,
  }),

  actions: {
    async fetchDaily(userId: string, days: number = 7) {
      this.loading = true
      this.error = null
      try {
        this.daily = await metricsApi.getDaily(userId, days)
      } catch (error) {
        this.error = '일별 데이터를 불러오는데 실패했습니다.'
        console.error(error)
      } finally {
        this.loading = false
      }
    },

    subscribeRealtime(userId: string) {
      if (this.socket) {
        this.socket.disconnect()
      }

      this.socket = connect(userId)

      this.socket.on('vital-now', (data: VitalNow) => {
        this.vitalNow = data
      })

      this.socket.on('metrics-update', (data: Partial<VitalNow>) => {
        this.vitalNow = { ...this.vitalNow, ...data }
      })
    },

    unsubscribeRealtime() {
      if (this.socket) {
        this.socket.disconnect()
        this.socket = null
      }
    },
  },
})

