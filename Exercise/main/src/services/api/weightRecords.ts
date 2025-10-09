import { http } from './http'

export interface WeightRecord {
  id: string
  userId: string
  weight: number
  recordDate: string
  createdAt: string
}

// 사용자의 몸무게 기록 조회
export async function getUserWeightRecords(userId: string): Promise<WeightRecord[]> {
  const { data } = await http.get(`/weight-records/user/${userId}`)
  return data
}

// 기간별 몸무게 기록 조회
export async function getUserWeightRecordsByRange(
  userId: string,
  startDate: string,
  endDate: string
): Promise<WeightRecord[]> {
  const { data } = await http.get(`/weight-records/user/${userId}/range`, {
    params: { startDate, endDate }
  })
  return data
}

