import { http } from './http'

export interface SurveyRequest {
  id: string
  userId: string
  message: string
  status: 'PENDING' | 'COMPLETED' | 'DECLINED'
  createdAt: string
  completedAt?: string
}

export interface SurveyData {
  [key: string]: any
}

// 설문 요청 보내기
export async function createSurveyRequest(userId: string, message: string): Promise<SurveyRequest> {
  const { data } = await http.post('/survey-requests', { userId, message })
  return data
}

// 사용자의 대기 중인 설문 요청 조회
export async function getPendingSurveyRequests(userId: string): Promise<SurveyRequest[]> {
  const { data } = await http.get(`/survey-requests/user/${userId}/pending`)
  return data
}

// 사용자의 모든 설문 요청 조회
export async function getUserSurveyRequests(userId: string): Promise<SurveyRequest[]> {
  const { data } = await http.get(`/survey-requests/user/${userId}`)
  return data
}

// 설문 제출
export async function submitSurvey(requestId: string, surveyData: SurveyData): Promise<any> {
  const { data } = await http.post(`/survey-requests/${requestId}/submit`, surveyData)
  return data
}

// 최신 설문 결과 조회
export async function getLatestSurveyResult(userId: string): Promise<any> {
  const { data } = await http.get(`/survey-requests/user/${userId}/latest-result`)
  return data
}

// 특정 설문 요청의 결과 조회
export async function getSurveyResult(requestId: string): Promise<any> {
  const { data } = await http.get(`/survey-requests/${requestId}/result`)
  return data
}

// 모든 설문 결과 조회
export async function getAllSurveyResults(userId: string): Promise<any[]> {
  const { data } = await http.get(`/survey-requests/user/${userId}/all-results`)
  return data
}

// 특정 설문 조회 (ID로)
export async function getSurveyById(surveyId: string): Promise<any> {
  const { data } = await http.get(`/survey-requests/result/${surveyId}`)
  return data
}

