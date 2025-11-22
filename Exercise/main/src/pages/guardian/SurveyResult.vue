<template>
  <div class="SurveyResultPage">
    <h2 class="SurveyResultTitle">설문 결과</h2>

    <div v-if="loading" class="SurveyResultLoading">
      <p>불러오는 중...</p>
    </div>

    <div v-else-if="error" class="SurveyResultError">
      <p>{{ error }}</p>
      <AppButton @click="loadAllResults" class="SurveyResultRetryButton">다시 시도</AppButton>
    </div>

    <div v-else-if="allSurveys.length === 0" class="SurveyResultEmpty">
      <p>아직 완료된 설문이 없습니다.</p>
      <RouterLink :to="`/guardian/users/${userId}`">
        <AppButton class="SurveyResultBackButton">돌아가기</AppButton>
      </RouterLink>
    </div>

    <div v-else class="SurveyResultContent">
      <!-- 날짜 선택 -->
      <div class="DateSelectionCard">
        <label class="DateSelectionLabel">설문 날짜 선택</label>
        <select
          v-model="selectedSurveyId"
          @change="loadSelectedSurvey"
          class="DateSelectionSelect"
        >
          <option v-for="survey in allSurveys" :key="survey.id" :value="survey.id">
            {{ formatDate(survey.createdAt) }}
          </option>
        </select>
      </div>

      <!-- 설문 결과 표시 (UserSurveyResult.vue와 동일) -->
      <div v-if="currentSurvey" class="SurveyDetailsContainer">
        <!-- 1. 기본 정보 -->
        <AppCard>
          <div class="SectionHeader">
            <h3 class="SectionTitle">1. 기본 정보</h3>
          </div>
          <div class="SectionContent">
            <div class="DataRow">
              <span class="DataLabel">성별</span>
              <div class="DataValueWrapper">
                <span class="DataValue">{{ currentData.gender?.join(', ') || '-' }}</span>
                <span v-if="getDiff('gender')" class="DataDiff">{{ getDiff('gender') }}</span>
              </div>
            </div>
            <div class="DataRow">
              <span class="DataLabel">나이</span>
              <div class="DataValueWrapper">
                <span class="DataValue">{{ currentData.age }}세</span>
                <span v-if="getDiff('age')" class="DataDiff">{{ getDiff('age') }}</span>
              </div>
            </div>
            <div class="DataRow">
              <span class="DataLabel">키</span>
              <div class="DataValueWrapper">
                <span class="DataValue">{{ currentData.height }}cm</span>
                <span v-if="getDiff('height')" class="DataDiff">{{ getDiff('height') }}</span>
              </div>
            </div>
            <div class="DataRow">
              <span class="DataLabel">몸무게</span>
              <div class="DataValueWrapper">
                <span class="DataValue">{{ currentData.weight }}kg</span>
                <span v-if="getDiff('weight')" class="DataDiff">{{ getDiff('weight') }}kg</span>
              </div>
            </div>
            <div class="DataRow">
              <span class="DataLabel">생활 패턴</span>
              <div class="DataValueWrapper">
                <span class="DataValue">{{ currentData.lifestyle?.join(', ') || '-' }}</span>
                <span v-if="getDiff('lifestyle')" class="DataDiff">{{ getDiff('lifestyle') }}</span>
              </div>
            </div>
          </div>
        </AppCard>

        <!-- 2. 건강 상태 -->
        <AppCard>
          <div class="SectionHeader">
            <h3 class="SectionTitle">2. 건강 상태</h3>
          </div>
          <div class="SectionContent">
            <div class="DataRow">
              <span class="DataLabel">진단받은 질환</span>
              <div class="DataValueWrapper">
                <span class="DataValue">
                  {{ currentData.diseases?.join(', ') || '없음' }}
                  {{ currentData.diseaseOther ? ` (기타: ${currentData.diseaseOther})` : '' }}
                </span>
                <span v-if="getDiff('diseases')" class="DataDiff">{{ getDiff('diseases') }}</span>
              </div>
            </div>
            <div class="DataRow">
              <span class="DataLabel">최근 수술/부상</span>
              <div class="DataValueWrapper">
                <span class="DataValue">{{ currentData.surgery?.join(', ') || '-' }}</span>
                <span v-if="getDiff('surgery')" class="DataDiff">{{ getDiff('surgery') }}</span>
              </div>
            </div>
            <div class="DataRow">
              <span class="DataLabel">복용 중인 약</span>
              <div class="DataValueWrapper">
                <span class="DataValue">
                  {{ currentData.medication?.join(', ') || '-' }}
                  {{ currentData.medicationName ? ` (${currentData.medicationName})` : '' }}
                </span>
                <span v-if="getDiff('medication')" class="DataDiff">{{ getDiff('medication') }}</span>
              </div>
            </div>
            <div class="DataRow">
              <span class="DataLabel">평균 수면 시간</span>
              <div class="DataValueWrapper">
                <span class="DataValue">{{ currentData.sleep?.join(', ') || '-' }}</span>
                <span v-if="getDiff('sleep')" class="DataDiff">{{ getDiff('sleep') }}</span>
              </div>
            </div>
          </div>
        </AppCard>

        <!-- 3. 생활 습관 -->
        <AppCard>
          <div class="SectionHeader">
            <h3 class="SectionTitle">3. 생활 습관</h3>
          </div>
          <div class="SectionContent">
            <div class="DataRow">
              <span class="DataLabel">흡연 여부</span>
              <div class="DataValueWrapper">
                <span class="DataValue">{{ currentData.smoking?.join(', ') || '-' }}</span>
                <span v-if="getDiff('smoking')" class="DataDiff">{{ getDiff('smoking') }}</span>
              </div>
            </div>
            <div class="DataRow">
              <span class="DataLabel">음주 빈도</span>
              <div class="DataValueWrapper">
                <span class="DataValue">{{ currentData.alcohol?.join(', ') || '-' }}</span>
                <span v-if="getDiff('alcohol')" class="DataDiff">{{ getDiff('alcohol') }}</span>
              </div>
            </div>
            <div class="DataRow">
              <span class="DataLabel">식습관</span>
              <div class="DataValueWrapper">
                <span class="DataValue">{{ currentData.diet?.join(', ') || '-' }}</span>
                <span v-if="getDiff('diet')" class="DataDiff">{{ getDiff('diet') }}</span>
              </div>
            </div>
          </div>
        </AppCard>

        <!-- 4. 운동 경험 -->
        <AppCard>
          <div class="SectionHeader">
            <h3 class="SectionTitle">4. 운동 경험 및 체력</h3>
          </div>
          <div class="SectionContent">
            <div class="DataRow">
              <span class="DataLabel">주간 운동 횟수</span>
              <div class="DataValueWrapper">
                <span class="DataValue">{{ currentData.exerciseFrequency?.join(', ') || '-' }}</span>
                <span v-if="getDiff('exerciseFrequency')" class="DataDiff">{{ getDiff('exerciseFrequency') }}</span>
              </div>
            </div>
            <div class="DataRow">
              <span class="DataLabel">선호 운동</span>
              <div class="DataValueWrapper">
                <span class="DataValue">
                  {{ currentData.preferredExercise?.join(', ') || '-' }}
                  {{ currentData.preferredExerciseOther ? ` (기타: ${currentData.preferredExerciseOther})` : '' }}
                </span>
                <span v-if="getDiff('preferredExercise')" class="DataDiff">{{ getDiff('preferredExercise') }}</span>
              </div>
            </div>
            <div class="DataRow">
              <span class="DataLabel">운동 목표</span>
              <div class="DataValueWrapper">
                <span class="DataValue">
                  {{ currentData.exerciseGoal?.join(', ') || '-' }}
                  {{ currentData.exerciseGoalOther ? ` (기타: ${currentData.exerciseGoalOther})` : '' }}
                </span>
                <span v-if="getDiff('exerciseGoal')" class="DataDiff">{{ getDiff('exerciseGoal') }}</span>
              </div>
            </div>
            <div class="DataRow">
              <span class="DataLabel">체력 수준</span>
              <div class="DataValueWrapper">
                <span class="DataValue">{{ currentData.fitnessLevel || '-' }} / 5</span>
                <span v-if="getDiff('fitnessLevel')" class="DataDiff">{{ getDiff('fitnessLevel') }}</span>
              </div>
            </div>
          </div>
        </AppCard>

        <!-- 5. 신체 한계 -->
        <AppCard>
          <div class="SectionHeader">
            <h3 class="SectionTitle">5. 신체적 한계 및 특이사항</h3>
          </div>
          <div class="SectionContent">
            <div class="DataRow">
              <span class="DataLabel">통증 부위</span>
              <div class="DataValueWrapper">
                <span class="DataValue">
                  {{ currentData.painArea?.join(', ') || '없음' }}
                  {{ currentData.painAreaOther ? ` (기타: ${currentData.painAreaOther})` : '' }}
                </span>
                <span v-if="getDiff('painArea')" class="DataDiff">{{ getDiff('painArea') }}</span>
              </div>
            </div>
            <div class="DataRow">
              <span class="DataLabel">운동 제한 지시</span>
              <div class="DataValueWrapper">
                <span class="DataValue">{{ currentData.exerciseRestriction?.join(', ') || '-' }}</span>
                <span v-if="getDiff('exerciseRestriction')" class="DataDiff">{{ getDiff('exerciseRestriction') }}</span>
              </div>
            </div>
          </div>
        </AppCard>

        <!-- 6. 심리/동기 -->
        <AppCard>
          <div class="SectionHeader">
            <h3 class="SectionTitle">6. 심리/동기 요인</h3>
          </div>
          <div class="SectionContent">
            <div class="DataRow">
              <span class="DataLabel">운동 시작 이유</span>
              <div class="DataValueWrapper">
                <span class="DataValue">
                  {{ currentData.exerciseReason?.join(', ') || '-' }}
                  {{ currentData.exerciseReasonOther ? ` (기타: ${currentData.exerciseReasonOther})` : '' }}
                </span>
                <span v-if="getDiff('exerciseReason')" class="DataDiff">{{ getDiff('exerciseReason') }}</span>
              </div>
            </div>
            <div class="DataRow">
              <span class="DataLabel">운동 자신감</span>
              <div class="DataValueWrapper">
                <span class="DataValue">{{ currentData.exerciseConfidence?.join(', ') || '-' }}</span>
                <span v-if="getDiff('exerciseConfidence')" class="DataDiff">{{ getDiff('exerciseConfidence') }}</span>
              </div>
            </div>
            <div class="DataRow">
              <span class="DataLabel">선호 환경</span>
              <div class="DataValueWrapper">
                <span class="DataValue">{{ currentData.exerciseEnvironment?.join(', ') || '-' }}</span>
                <span v-if="getDiff('exerciseEnvironment')" class="DataDiff">{{ getDiff('exerciseEnvironment') }}</span>
              </div>
            </div>
          </div>
        </AppCard>

        <div class="BackButtonWrapper">
          <RouterLink :to="`/guardian/users/${userId}`">
            <AppButton class="BackButton">돌아가기</AppButton>
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
