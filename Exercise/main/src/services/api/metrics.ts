import { http } from './http'
import type { DailyMetrics, VitalNow } from '@/types/metrics'

export async function getDaily(userId: string, days: number = 7): Promise<DailyMetrics[]> {
  const { data } = await http.get(`/users/${userId}/metrics/daily`, { params: { days } })
  return data
}

export async function getRealtime(userId: string): Promise<VitalNow> {
  const { data } = await http.get(`/users/${userId}/metrics/realtime`)
  return data
}

