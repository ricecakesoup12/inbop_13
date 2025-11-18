import { http } from './http'

export interface ExerciseStatus {
  id: string
  userId: string
  isExercising: boolean
  startedAt?: string
  updatedAt: string
}

// 운동 상태 업데이트
export async function updateExerciseStatus(userId: string, isExercising: boolean): Promise<ExerciseStatus> {
  const { data } = await http.post(`/exercise-status/user/${userId}`, { isExercising })
  return data
}

// 운동 상태 조회
export async function getExerciseStatus(userId: string): Promise<ExerciseStatus> {
  const { data } = await http.get(`/exercise-status/user/${userId}`)
  return data
}


