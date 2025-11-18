import { defineStore } from 'pinia'
import type { DailyMetrics, VitalNow } from '@/types/metrics'
import * as metricsApi from '@/services/api/metrics'
import * as sensorApi from '@/services/api/sensor'
import { connect } from '@/services/realtime/socket'
import type { Socket } from 'socket.io-client'

export const useMetricsStore = defineStore('metrics', {
  state: () => ({
    daily: [] as DailyMetrics[],
    vitalNow: { hr: 0, spo2: 0, active: false, ts: '' } as VitalNow,
    socket: null as Socket | null,
    sensorEventSource: null as EventSource | null,
    sensorConnected: false, // 센서 연결 상태
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

      // 센서 스트림 구독 (실시간 센서 데이터)
      this.subscribeSensorStream()
    },

    subscribeSensorStream() {
      // 기존 연결이 있으면 닫기
      if (this.sensorEventSource) {
        this.sensorEventSource.close()
      }

      // 센서 연결 상태 초기화
      this.sensorConnected = false

      // 센서 스트림 구독
      this.sensorEventSource = sensorApi.subscribeSensorStream(
        (data) => {
          // 센서 데이터를 VitalNow 형식으로 변환
          const vitalData = sensorApi.convertToVitalNow(data)
          this.vitalNow = vitalData
          this.sensorConnected = true // 데이터를 받으면 연결됨
          console.log('✅ 센서 데이터 업데이트:', vitalData)
        },
        (error) => {
          console.error('❌ 센서 스트림 오류:', error)
          this.sensorConnected = false
        }
      )

      // 연결 확인 (5초 후에도 데이터가 없으면 연결 안됨으로 표시)
      setTimeout(() => {
        if (!this.sensorConnected && this.vitalNow.hr === 0 && this.vitalNow.spo2 === 0) {
          console.warn('⚠️ 센서 연결되지 않음')
        }
      }, 5000)
    },

    unsubscribeRealtime() {
      if (this.socket) {
        this.socket.disconnect()
        this.socket = null
      }
      
      if (this.sensorEventSource) {
        this.sensorEventSource.close()
        this.sensorEventSource = null
      }
    },
  },
})

