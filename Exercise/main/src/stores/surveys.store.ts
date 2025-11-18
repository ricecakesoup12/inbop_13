import { defineStore } from 'pinia'
import type { SurveyForm, SurveyResult } from '@/types/survey'
import * as surveysApi from '@/services/api/surveys'

export const useSurveysStore = defineStore('surveys', {
  state: () => ({
    result: null as SurveyResult | null,
    loading: false,
    error: null as string | null,
  }),

  actions: {
    async createSurvey(userId: string, form: SurveyForm) {
      this.loading = true
      this.error = null
      try {
        await surveysApi.createSurvey(userId, form)
      } catch (error) {
        this.error = '설문 전송에 실패했습니다.'
        console.error(error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchResult(userId: string) {
      this.loading = true
      this.error = null
      try {
        this.result = await surveysApi.getResult(userId)
      } catch (error) {
        this.error = '설문 결과를 불러오는데 실패했습니다.'
        console.error(error)
      } finally {
        this.loading = false
      }
    },
  },
})

