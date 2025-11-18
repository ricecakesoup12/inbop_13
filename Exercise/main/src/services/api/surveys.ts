import { http } from './http'
import type { SurveyForm, SurveyResult } from '@/types/survey'

export async function createSurvey(userId: string, form: SurveyForm): Promise<SurveyResult> {
  const { data } = await http.post(`/users/${userId}/surveys`, form)
  return data
}

export async function getResult(userId: string): Promise<SurveyResult> {
  const { data } = await http.get(`/users/${userId}/surveys/latest`)
  return data
}

export async function getAllSurveys(userId: string): Promise<SurveyResult[]> {
  const { data } = await http.get(`/users/${userId}/surveys`)
  return data
}

