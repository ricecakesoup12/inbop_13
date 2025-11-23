import http from './http'

/**
 * 운동 처방 타입
 */
export interface ExercisePrescription {
  id?: number
  userId: string
  startStretchingMinutes: number  // 5, 10, 15
  startStretchingUrl?: string  // 시작 스트레칭 URL (선택사항)
  walkingMinutes: number
  runningMinutes: number
  sets: number
  endStretchingMinutes: number  // 5, 10, 15
  endStretchingUrl?: string  // 마무리 스트레칭 URL (선택사항)
  intensity?: string  // 난이도: 'low' | 'medium' | 'high'
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'COMPLETED'
  createdAt?: string
  acceptedAt?: string
  completedAt?: string
}

/**
 * 처방 생성 요청
 */
export interface CreateExercisePrescriptionRequest {
  userId: string
  startStretchingMinutes: number
  startStretchingUrl?: string
  walkingMinutes: number
  runningMinutes: number
  sets: number
  endStretchingMinutes: number
  endStretchingUrl?: string
  intensity?: string  // 난이도: 'low' | 'medium' | 'high'
}

/**
 * 특정 사용자의 처방 조회
 */
export async function getPrescriptionsByUser(userId: string): Promise<ExercisePrescription[]> {
  try {
    const response = await http.get<ExercisePrescription[]>(`/api/exercise-prescriptions/user/${userId}`)
    return response.data
  } catch (error) {
    console.error('처방 조회 실패:', error)
    throw error
  }
}

/**
 * 특정 사용자의 대기 중인 처방 조회
 */
export async function getPendingPrescription(userId: string): Promise<ExercisePrescription | null> {
  try {
    const response = await http.get<ExercisePrescription>(`/api/exercise-prescriptions/user/${userId}/pending`)
    return response.data
  } catch (error: any) {
    if (error.response?.status === 404) {
      return null
    }
    console.error('대기 중인 처방 조회 실패:', error)
    throw error
  }
}

/**
 * 처방 전송
 */
export async function createPrescription(request: CreateExercisePrescriptionRequest): Promise<ExercisePrescription> {
  try {
    console.log('📤 API 요청 시작:', '/api/exercise-prescriptions')
    console.log('📤 요청 데이터:', JSON.stringify(request, null, 2))
    
    const response = await http.post<ExercisePrescription>('/api/exercise-prescriptions', request)
    
    console.log('✅ API 응답 성공:', response.data)
    return response.data
  } catch (error: any) {
    console.error('❌ 처방 전송 실패:', error)
    console.error('❌ 에러 응답:', error?.response?.data)
    console.error('❌ 에러 상태:', error?.response?.status)
    throw error
  }
}

/**
 * 처방 수락
 */
export async function acceptPrescription(prescriptionId: number): Promise<ExercisePrescription> {
  try {
    const response = await http.put<ExercisePrescription>(`/api/exercise-prescriptions/${prescriptionId}/accept`)
    return response.data
  } catch (error) {
    console.error('처방 수락 실패:', error)
    throw error
  }
}

/**
 * 처방 거부
 */
export async function declinePrescription(prescriptionId: number): Promise<ExercisePrescription> {
  try {
    const response = await http.put<ExercisePrescription>(`/api/exercise-prescriptions/${prescriptionId}/decline`)
    return response.data
  } catch (error) {
    console.error('처방 거부 실패:', error)
    throw error
  }
}

/**
 * 처방 완료
 */
export async function completePrescription(prescriptionId: number): Promise<ExercisePrescription> {
  try {
    const response = await http.put<ExercisePrescription>(`/api/exercise-prescriptions/${prescriptionId}/complete`)
    return response.data
  } catch (error) {
    console.error('처방 완료 실패:', error)
    throw error
  }
}

