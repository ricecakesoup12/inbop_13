import http from './http'

/**
 * 새싹 개수 조회
 */
export async function getSproutCount(userId: string): Promise<number> {
  try {
    const response = await http.get<number>(`/users/${userId}/sprouts`)
    return response.data
  } catch (error) {
    console.error('새싹 개수 조회 실패:', error)
    return 0
  }
}

/**
 * 새싹 획득 (운동 목표 완료 시)
 */
export async function earnSprout(userId: string): Promise<{ sproutCount: number }> {
  try {
    const response = await http.post<{ id: number; sproutCount: number }>(`/users/${userId}/sprouts/earn`)
    return { sproutCount: response.data.sproutCount }
  } catch (error: any) {
    console.error('새싹 획득 실패:', error)
    if (error.response?.status === 400) {
      throw new Error(error.response.data || '오늘은 이미 새싹을 받았습니다.')
    }
    throw error
  }
}

/**
 * 새싹 차감 (상점 구매 시)
 */
export async function spendSprouts(userId: string, amount: number): Promise<{ sproutCount: number }> {
  try {
    const response = await http.post<{ id: number; sproutCount: number }>(`/users/${userId}/sprouts/spend`, amount)
    return { sproutCount: response.data.sproutCount }
  } catch (error: any) {
    console.error('새싹 차감 실패:', error)
    if (error.response?.status === 400) {
      throw new Error(error.response.data || '보유 새싹이 부족합니다.')
    }
    throw error
  }
}

