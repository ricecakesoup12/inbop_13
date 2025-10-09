<template>
  <div class="max-w-4xl mx-auto">
    <h2 class="text-2xl font-semibold text-text-main mb-6 font-gowun">설문 결과</h2>

    <div v-if="loading" class="text-center py-12">
      <p class="text-text-sub font-gowun">불러오는 중...</p>
    </div>

    <div v-else-if="error" class="text-center py-12">
      <p class="text-red-600 font-gowun">{{ error }}</p>
      <AppButton @click="loadAllResults" class="mt-4">다시 시도</AppButton>
    </div>

    <div v-else-if="allSurveys.length === 0" class="text-center py-12">
      <p class="text-text-sub font-gowun">아직 완료된 설문이 없습니다.</p>
      <RouterLink :to="`/guardian/users/${userId}`">
        <AppButton class="mt-4">돌아가기</AppButton>
      </RouterLink>
    </div>

    <div v-else class="space-y-6">
      <!-- 날짜 선택 -->
      <div class="bg-primary/10 rounded-xl p-4">
        <label class="block text-sm font-semibold text-text-main mb-2 font-gowun">설문 날짜 선택</label>
        <select
          v-model="selectedSurveyId"
          @change="loadSelectedSurvey"
          class="w-full px-4 py-3 border-2 border-primary rounded-xl focus:border-primary outline-none font-gowun bg-white"
        >
          <option v-for="survey in allSurveys" :key="survey.id" :value="survey.id">
            {{ formatDate(survey.createdAt) }}
          </option>
        </select>
      </div>

      <!-- 설문 결과 표시 (UserSurveyResult.vue와 동일) -->
      <div v-if="currentSurvey" class="space-y-6">
        <!-- 1. 기본 정보 -->
        <AppCard>
          <div class="bg-beige p-4 border-b border-accent">
            <h3 class="font-semibold text-text-main font-gowun">1. 기본 정보</h3>
          </div>
          <div class="p-6 space-y-3">
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">성별</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">{{ currentData.gender?.join(', ') || '-' }}</span>
                <span v-if="getDiff('gender')" class="text-sm text-red-500 font-gowun">{{ getDiff('gender') }}</span>
              </div>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">나이</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">{{ currentData.age }}세</span>
                <span v-if="getDiff('age')" class="text-sm text-red-500 font-gowun">{{ getDiff('age') }}</span>
              </div>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">키</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">{{ currentData.height }}cm</span>
                <span v-if="getDiff('height')" class="text-sm text-red-500 font-gowun">{{ getDiff('height') }}</span>
              </div>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">몸무게</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">{{ currentData.weight }}kg</span>
                <span v-if="getDiff('weight')" class="text-sm text-red-500 font-bold font-gowun">{{ getDiff('weight') }}kg</span>
              </div>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">생활 패턴</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">{{ currentData.lifestyle?.join(', ') || '-' }}</span>
                <span v-if="getDiff('lifestyle')" class="text-sm text-red-500 font-gowun">{{ getDiff('lifestyle') }}</span>
              </div>
            </div>
          </div>
        </AppCard>

        <!-- 2. 건강 상태 -->
        <AppCard>
          <div class="bg-beige p-4 border-b border-accent">
            <h3 class="font-semibold text-text-main font-gowun">2. 건강 상태</h3>
          </div>
          <div class="p-6 space-y-3">
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">진단받은 질환</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">
                  {{ currentData.diseases?.join(', ') || '없음' }}
                  {{ currentData.diseaseOther ? ` (기타: ${currentData.diseaseOther})` : '' }}
                </span>
                <span v-if="getDiff('diseases')" class="text-sm text-red-500 font-gowun">{{ getDiff('diseases') }}</span>
              </div>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">최근 수술/부상</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">{{ currentData.surgery?.join(', ') || '-' }}</span>
                <span v-if="getDiff('surgery')" class="text-sm text-red-500 font-gowun">{{ getDiff('surgery') }}</span>
              </div>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">복용 중인 약</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">
                  {{ currentData.medication?.join(', ') || '-' }}
                  {{ currentData.medicationName ? ` (${currentData.medicationName})` : '' }}
                </span>
                <span v-if="getDiff('medication')" class="text-sm text-red-500 font-gowun">{{ getDiff('medication') }}</span>
              </div>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">평균 수면 시간</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">{{ currentData.sleep?.join(', ') || '-' }}</span>
                <span v-if="getDiff('sleep')" class="text-sm text-red-500 font-gowun">{{ getDiff('sleep') }}</span>
              </div>
            </div>
          </div>
        </AppCard>

        <!-- 3. 생활 습관 -->
        <AppCard>
          <div class="bg-beige p-4 border-b border-accent">
            <h3 class="font-semibold text-text-main font-gowun">3. 생활 습관</h3>
          </div>
          <div class="p-6 space-y-3">
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">흡연 여부</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">{{ currentData.smoking?.join(', ') || '-' }}</span>
                <span v-if="getDiff('smoking')" class="text-sm text-red-500 font-gowun">{{ getDiff('smoking') }}</span>
              </div>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">음주 빈도</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">{{ currentData.alcohol?.join(', ') || '-' }}</span>
                <span v-if="getDiff('alcohol')" class="text-sm text-red-500 font-gowun">{{ getDiff('alcohol') }}</span>
              </div>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">식습관</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">{{ currentData.diet?.join(', ') || '-' }}</span>
                <span v-if="getDiff('diet')" class="text-sm text-red-500 font-gowun">{{ getDiff('diet') }}</span>
              </div>
            </div>
          </div>
        </AppCard>

        <!-- 4. 운동 경험 -->
        <AppCard>
          <div class="bg-beige p-4 border-b border-accent">
            <h3 class="font-semibold text-text-main font-gowun">4. 운동 경험 및 체력</h3>
          </div>
          <div class="p-6 space-y-3">
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">주간 운동 횟수</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">{{ currentData.exerciseFrequency?.join(', ') || '-' }}</span>
                <span v-if="getDiff('exerciseFrequency')" class="text-sm text-red-500 font-gowun">{{ getDiff('exerciseFrequency') }}</span>
              </div>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">선호 운동</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">
                  {{ currentData.preferredExercise?.join(', ') || '-' }}
                  {{ currentData.preferredExerciseOther ? ` (기타: ${currentData.preferredExerciseOther})` : '' }}
                </span>
                <span v-if="getDiff('preferredExercise')" class="text-sm text-red-500 font-gowun">{{ getDiff('preferredExercise') }}</span>
              </div>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">운동 목표</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">
                  {{ currentData.exerciseGoal?.join(', ') || '-' }}
                  {{ currentData.exerciseGoalOther ? ` (기타: ${currentData.exerciseGoalOther})` : '' }}
                </span>
                <span v-if="getDiff('exerciseGoal')" class="text-sm text-red-500 font-gowun">{{ getDiff('exerciseGoal') }}</span>
              </div>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">체력 수준</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">{{ currentData.fitnessLevel || '-' }} / 5</span>
                <span v-if="getDiff('fitnessLevel')" class="text-sm text-red-500 font-bold font-gowun">{{ getDiff('fitnessLevel') }}</span>
              </div>
            </div>
          </div>
        </AppCard>

        <!-- 5. 신체 한계 -->
        <AppCard>
          <div class="bg-beige p-4 border-b border-accent">
            <h3 class="font-semibold text-text-main font-gowun">5. 신체적 한계 및 특이사항</h3>
          </div>
          <div class="p-6 space-y-3">
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">통증 부위</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">
                  {{ currentData.painArea?.join(', ') || '없음' }}
                  {{ currentData.painAreaOther ? ` (기타: ${currentData.painAreaOther})` : '' }}
                </span>
                <span v-if="getDiff('painArea')" class="text-sm text-red-500 font-gowun">{{ getDiff('painArea') }}</span>
              </div>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">운동 제한 지시</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">{{ currentData.exerciseRestriction?.join(', ') || '-' }}</span>
                <span v-if="getDiff('exerciseRestriction')" class="text-sm text-red-500 font-gowun">{{ getDiff('exerciseRestriction') }}</span>
              </div>
            </div>
          </div>
        </AppCard>

        <!-- 6. 심리/동기 -->
        <AppCard>
          <div class="bg-beige p-4 border-b border-accent">
            <h3 class="font-semibold text-text-main font-gowun">6. 심리/동기 요인</h3>
          </div>
          <div class="p-6 space-y-3">
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">운동 시작 이유</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">
                  {{ currentData.exerciseReason?.join(', ') || '-' }}
                  {{ currentData.exerciseReasonOther ? ` (기타: ${currentData.exerciseReasonOther})` : '' }}
                </span>
                <span v-if="getDiff('exerciseReason')" class="text-sm text-red-500 font-gowun">{{ getDiff('exerciseReason') }}</span>
              </div>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">운동 자신감</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">{{ currentData.exerciseConfidence?.join(', ') || '-' }}</span>
                <span v-if="getDiff('exerciseConfidence')" class="text-sm text-red-500 font-gowun">{{ getDiff('exerciseConfidence') }}</span>
              </div>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-text-sub font-gowun">선호 환경</span>
              <div class="flex items-center gap-2">
                <span class="font-semibold text-text-main font-gowun">{{ currentData.exerciseEnvironment?.join(', ') || '-' }}</span>
                <span v-if="getDiff('exerciseEnvironment')" class="text-sm text-red-500 font-gowun">{{ getDiff('exerciseEnvironment') }}</span>
              </div>
            </div>
          </div>
        </AppCard>

        <div class="flex justify-center pt-4">
          <RouterLink :to="`/guardian/users/${userId}`">
            <AppButton>돌아가기</AppButton>
          </RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getAllSurveyResults, getSurveyById } from '@/services/api/surveyRequests'
import AppCard from '@/components/common/AppCard.vue'
import AppButton from '@/components/common/AppButton.vue'

const route = useRoute()
const userId = route.params.id as string

const loading = ref(true)
const error = ref<string | null>(null)
const allSurveys = ref<any[]>([])
const currentSurvey = ref<any>(null)
const previousSurvey = ref<any>(null)
const selectedSurveyId = ref<string>('')

const currentData = computed(() => {
  if (!currentSurvey.value?.surveyData) return {}
  try {
    if (typeof currentSurvey.value.surveyData === 'string') {
      return JSON.parse(currentSurvey.value.surveyData)
    }
    return currentSurvey.value.surveyData
  } catch (error) {
    return {}
  }
})

const previousData = computed(() => {
  if (!previousSurvey.value?.surveyData) return {}
  try {
    if (typeof previousSurvey.value.surveyData === 'string') {
      return JSON.parse(previousSurvey.value.surveyData)
    }
    return previousSurvey.value.surveyData
  } catch (error) {
    return {}
  }
})

const getDiff = (field: string): string => {
  if (!previousData.value || !currentData.value) return ''
  
  const current = currentData.value[field]
  const previous = previousData.value[field]
  
  if (current === undefined || previous === undefined) return ''
  
  // 숫자 비교
  if (typeof current === 'number' && typeof previous === 'number') {
    const diff = current - previous
    if (diff === 0) return ''
    return diff > 0 ? `+${diff}` : `${diff}`
  }
  
  // 배열 비교
  if (Array.isArray(current) && Array.isArray(previous)) {
    if (JSON.stringify(current) === JSON.stringify(previous)) return ''
    return '변경됨'
  }
  
  // 문자열 비교
  if (current !== previous) {
    return '변경됨'
  }
  
  return ''
}

const loadSelectedSurvey = async () => {
  if (!selectedSurveyId.value) return
  
  loading.value = true
  try {
    currentSurvey.value = await getSurveyById(selectedSurveyId.value)
    
    // 이전 설문 찾기
    const currentIndex = allSurveys.value.findIndex(s => s.id === selectedSurveyId.value)
    if (currentIndex < allSurveys.value.length - 1) {
      previousSurvey.value = await getSurveyById(allSurveys.value[currentIndex + 1].id)
    } else {
      previousSurvey.value = null
    }
  } catch (error) {
    console.error('설문 조회 실패:', error)
  } finally {
    loading.value = false
  }
}

const loadAllResults = async () => {
  loading.value = true
  error.value = null
  
  try {
    allSurveys.value = await getAllSurveyResults(userId)
    
    if (allSurveys.value.length > 0) {
      selectedSurveyId.value = allSurveys.value[0].id
      await loadSelectedSurvey()
    }
  } catch (err) {
    console.error('설문 결과 조회 실패:', err)
    error.value = '설문 결과를 불러오는데 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(() => {
  loadAllResults()
})
</script>
