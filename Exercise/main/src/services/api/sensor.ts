import type { VitalNow } from '@/types/metrics'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8081'

export interface SensorData {
  hr: number
  spo2: number
  timestamp: number
  raw?: string
}

/**
 * 최신 센서 데이터 조회
 */
export async function getLatestSensorData(): Promise<SensorData> {
  const response = await fetch(`${API_BASE}/api/serial/latest`)
  return response.json()
}

/**
 * 실시간 센서 스트림 (Server-Sent Events)
 * @param onData 센서 데이터를 받을 콜백 함수
 * @returns EventSource (닫을 때 사용)
 */
export function subscribeSensorStream(
  onData: (data: SensorData) => void,
  onError?: (error: Event) => void
): EventSource {
  const eventSource = new EventSource(`${API_BASE}/api/serial/stream`)

  eventSource.onmessage = (event) => {
    try {
      const data: SensorData = JSON.parse(event.data)
      onData(data)
    } catch (error) {
      console.error('센서 데이터 파싱 실패:', error)
    }
  }

  eventSource.onerror = (error) => {
    console.error('센서 스트림 오류:', error)
    if (onError) {
      onError(error)
    }
  }

  return eventSource
}

/**
 * 센서 연결 상태 확인
 */
export interface SensorStatus {
  connected: boolean
  portName: string
  baud: number
  running: boolean
  hasData: boolean
  lastData: SensorData | null
  availablePorts: string[]
}

export async function getSensorStatus(): Promise<SensorStatus> {
  const response = await fetch(`${API_BASE}/api/serial/status`)
  return response.json()
}

/**
 * 센서 데이터를 VitalNow 형식으로 변환
 */
export function convertToVitalNow(sensorData: SensorData): VitalNow {
  return {
    hr: sensorData.hr,
    spo2: sensorData.spo2,
    active: false, // 센서에서 운동 상태는 별도로 판단 필요
    ts: new Date(sensorData.timestamp).toISOString()
  }
}

