import { http } from './http'

export interface StretchVideo {
  제목: string
  영상주소: string
}

export interface IntervalExercise {
  루틴명: string
  세트수: number
  운동시간분: number
  휴식시간분: number
  강도: string
  설명: string
}

export interface StretchRecommendation {
  사용자코드: string
  통증부위: string | null
  스트레칭영상: StretchVideo[]
  인터벌운동: IntervalExercise[]
  주의사항: string[]
  실패이유?: string
  출처?: string
}

/**
 * 사용자 코드 기준으로 AI 스트레칭 및 인터벌 운동 추천
 */
export async function getStretchRecommendation(userCode: string): Promise<StretchRecommendation> {
  const { data } = await http.get(`/api/stretch/user-code/${userCode}`)
  return data
}




