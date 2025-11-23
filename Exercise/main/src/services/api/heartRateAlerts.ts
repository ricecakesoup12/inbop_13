import { http } from './http'

/**
 * 심박수 경고 타입
 */
export interface HeartRateAlert {
  id?: number
  userId: number
  heartRate: number
  alertType: 'warning'
  alertedAt?: string
}

/**
 * 일주일별 경고 횟수
 */
export interface WeeklyAlertCount {
  date: string  // "YYYY-MM-DD"
  count: number
}

/**
 * 활성 경고 (보호자용)
 */
export interface ActiveAlert {
  userId: number
  userName: string
  heartRate: number
  alertedAt: string
}

/**
 * 심박수 경고 기록 생성 요청
 */
export interface CreateHeartRateAlertRequest {
  userId: number
  heartRate: number
  alertType: 'warning'
}

/**
 * 심박수 경고 기록 생성
 */
export async function createHeartRateAlert(
  userId: number,
  heartRate: number
): Promise<HeartRateAlert> {
  try {
    const response = await http.post<HeartRateAlert>('/api/heart-rate-alerts', {
      userId,
      heartRate,
      alertType: 'warning' as const
    })
    return response.data
  } catch (error) {
    console.error('심박수 경고 기록 생성 실패:', error)
    throw error
  }
}

/**
 * 사용자별 일주일 경고 횟수 조회
 */
export async function getWeeklyAlertCounts(
  userId: string
): Promise<WeeklyAlertCount[]> {
  try {
    const response = await http.get<WeeklyAlertCount[]>(
      `/api/users/${userId}/heart-rate-alerts/weekly`
    )
    return response.data
  } catch (error) {
    console.error('일주일 경고 횟수 조회 실패:', error)
    throw error
  }
}

/**
 * 모든 사용자의 현재 경고 상태 조회 (보호자용)
 */
export async function getActiveAlerts(): Promise<ActiveAlert[]> {
  try {
    const response = await http.get<ActiveAlert[]>('/api/guardian/heart-rate-alerts/active')
    return response.data
  } catch (error) {
    console.error('활성 경고 조회 실패:', error)
    throw error
  }
}

