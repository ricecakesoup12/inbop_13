<template>
  <div class="UserSurveyPage">
    <div class="UserSurveyContainer">
      <div class="UserSurveyCard">
        <div class="UserSurveyHeader">
          <h1 class="UserSurveyTitle">건강 설문</h1>
          
          <!-- 보호자 요청 메시지 강조 -->
          <div class="GuardianRequestBanner">
            <div class="GuardianRequestContent">
              <svg class="GuardianRequestIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <div class="GuardianRequestText">
                <div class="GuardianRequestLabel">보호자 요청 사항</div>
                <p class="GuardianRequestMessage">{{ requestMessage }}</p>
              </div>
            </div>
          </div>
        </div>

        <form @submit.prevent="handleSubmit" class="UserSurveyForm">
          <!-- 1. 기본 정보 -->
          <section class="SurveySection">
            <h2 class="SurveySectionTitle">1. 기본 정보</h2>
            
            <!-- 성별 -->
            <div class="SurveyField">
              <label class="SurveyFieldLabel">
                성별 <span class="SurveyRequired">*</span>
              </label>
              <div class="SurveyOptionGrid">
                <label v-for="g in ['남성', '여성', '기타/응답하지 않음']" :key="g"
                  :class="['SurveyOption', surveyData.gender.includes(g) ? 'SurveyOptionSelected' : 'SurveyOptionUnselected']">
                  <input type="checkbox" :value="g" v-model="surveyData.gender" class="SurveyOptionInput" />
                  <span>{{ g }}</span>
                </label>
              </div>
            </div>

            <!-- 나이, 키, 몸무게 -->
            <div class="SurveyFieldGrid">
              <div class="SurveyField">
                <label class="SurveyFieldLabel">나이 <span class="SurveyRequired">*</span></label>
                <input v-model.number="surveyData.age" type="number" required placeholder="세"
                  class="SurveyInput" />
              </div>
              <div class="SurveyField">
                <label class="SurveyFieldLabel">키 <span class="SurveyRequired">*</span></label>
                <input v-model.number="surveyData.height" type="number" required placeholder="cm"
                  class="SurveyInput" />
              </div>
              <div class="SurveyField">
                <label class="SurveyFieldLabel">몸무게 <span class="SurveyRequired">*</span></label>
                <input v-model.number="surveyData.weight" type="number" required placeholder="kg"
                  class="SurveyInput" />
              </div>
            </div>

            <!-- 생활 패턴 -->
            <div class="SurveyField">
              <label class="SurveyFieldLabel">평소 생활 패턴 <span class="SurveyRequired">*</span></label>
              <div class="SurveyOptionList">
                <label v-for="pattern in ['주로 앉아 있음 (사무직 등)', '가볍게 활동함 (학생, 가사 등)', '많이 움직임 (서비스직, 현장 근무 등)']" :key="pattern"
                  :class="['SurveyOption', surveyData.lifestyle.includes(pattern) ? 'SurveyOptionSelected' : 'SurveyOptionUnselected']">
                  <input type="checkbox" :value="pattern" v-model="surveyData.lifestyle" class="SurveyOptionInput" />
                  <span>{{ pattern }}</span>
                </label>
              </div>
            </div>
          </section>

          <!-- 2. 건강 상태 -->
          <section class="SurveySection">
            <h2 class="SurveySectionTitle">2. 건강 상태</h2>
            
            <!-- 진단받은 질환 -->
            <div class="SurveyField">
              <label class="SurveyFieldLabel">현재 진단받은 질환</label>
              <div class="SurveyOptionGrid">
                <label v-for="disease in ['없음', '고혈압', '당뇨', '심장질환', '호흡기질환', '관절질환']" :key="disease"
                  :class="['SurveyOption', surveyData.diseases.includes(disease) ? 'SurveyOptionSelected' : 'SurveyOptionUnselected']">
                  <input type="checkbox" :value="disease" v-model="surveyData.diseases" class="SurveyOptionInput" @change="handleDiseaseChange(disease)" />
                  <span>{{ disease }}</span>
                </label>
              </div>
              <input v-model="surveyData.diseaseOther" type="text" placeholder="기타 질환 입력"
                class="SurveyOtherInput" />
            </div>

            <!-- 최근 수술/부상 -->
            <div class="SurveyField">
              <label class="SurveyFieldLabel">최근 6개월 이내 수술/큰 부상</label>
              <div class="SurveyOptionGrid">
                <label v-for="opt in ['예', '아니오']" :key="opt"
                  :class="['SurveyOption', surveyData.surgery.includes(opt) ? 'SurveyOptionSelected' : 'SurveyOptionUnselected']">
                  <input type="checkbox" :value="opt" v-model="surveyData.surgery" class="SurveyOptionInput" />
                  <span>{{ opt }}</span>
                </label>
              </div>
            </div>

            <!-- 복용 중인 약 -->
            <div class="SurveyField">
              <label class="SurveyFieldLabel">현재 복용 중인 약</label>
              <div class="SurveyOptionGrid">
                <label v-for="opt in ['예', '아니오']" :key="opt"
                  :class="['SurveyOption', surveyData.medication.includes(opt) ? 'SurveyOptionSelected' : 'SurveyOptionUnselected']">
                  <input type="checkbox" :value="opt" v-model="surveyData.medication" class="SurveyOptionInput" />
                  <span>{{ opt }}</span>
                </label>
              </div>
              <input v-if="surveyData.medication.includes('예')" v-model="surveyData.medicationName" type="text" placeholder="약물명 입력"
                class="SurveyOtherInput" />
            </div>

            <!-- 수면 시간 -->
            <div class="SurveyField">
              <label class="SurveyFieldLabel">하루 평균 수면 시간 <span class="SurveyRequired">*</span></label>
              <div class="SurveyOptionGrid">
                <label v-for="hours in ['4시간 이하', '5–6시간', '7–8시간', '9시간 이상']" :key="hours"
                  :class="['SurveyOption', surveyData.sleep.includes(hours) ? 'SurveyOptionSelected' : 'SurveyOptionUnselected']">
                  <input type="checkbox" :value="hours" v-model="surveyData.sleep" class="SurveyOptionInput" />
                  <span>{{ hours }}</span>
                </label>
              </div>
            </div>
          </section>

          <!-- 3. 생활 습관 -->
          <section class="SurveySection">
            <h2 class="SurveySectionTitle">3. 생활 습관</h2>
            
            <!-- 흡연 -->
            <div class="SurveyField">
              <label class="SurveyFieldLabel">흡연 여부</label>
              <div class="SurveyOptionGrid">
                <label v-for="opt in ['비흡연', '현재 흡연 중', '과거 흡연자']" :key="opt"
                  :class="['SurveyOption', surveyData.smoking.includes(opt) ? 'SurveyOptionSelected' : 'SurveyOptionUnselected']">
                  <input type="checkbox" :value="opt" v-model="surveyData.smoking" class="SurveyOptionInput" />
                  <span>{{ opt }}</span>
                </label>
              </div>
            </div>

            <!-- 음주 -->
            <div class="SurveyField">
              <label class="SurveyFieldLabel">음주 빈도</label>
              <div class="SurveyOptionGrid">
                <label v-for="freq in ['거의 안 함', '주 1–2회', '주 3–4회', '거의 매일']" :key="freq"
                  :class="['SurveyOption', surveyData.alcohol.includes(freq) ? 'SurveyOptionSelected' : 'SurveyOptionUnselected']">
                  <input type="checkbox" :value="freq" v-model="surveyData.alcohol" class="SurveyOptionInput" />
                  <span>{{ freq }}</span>
                </label>
              </div>
            </div>

            <!-- 식습관 -->
            <div class="SurveyField">
              <label class="SurveyFieldLabel">평소 식습관</label>
              <div class="SurveyOptionGrid">
                <label v-for="diet in ['채소/과일 위주', '가공식품/외식 위주', '균형 잡힌 편']" :key="diet"
                  :class="['SurveyOption', surveyData.diet.includes(diet) ? 'SurveyOptionSelected' : 'SurveyOptionUnselected']">
                  <input type="checkbox" :value="diet" v-model="surveyData.diet" class="SurveyOptionInput" />
                  <span>{{ diet }}</span>
                </label>
              </div>
            </div>
          </section>

          <!-- 4. 운동 경험 및 체력 -->
          <section class="SurveySection">
            <h2 class="SurveySectionTitle">4. 운동 경험 및 체력</h2>
            
            <!-- 주간 운동 횟수 -->
            <div class="SurveyField">
              <label class="SurveyFieldLabel">주간 평균 운동 횟수</label>
              <div class="SurveyOptionGrid">
                <label v-for="freq in ['전혀 안 함', '1–2회', '3–4회', '5회 이상']" :key="freq"
                  :class="['SurveyOption', surveyData.exerciseFrequency.includes(freq) ? 'SurveyOptionSelected' : 'SurveyOptionUnselected']">
                  <input type="checkbox" :value="freq" v-model="surveyData.exerciseFrequency" class="SurveyOptionInput" />
                  <span>{{ freq }}</span>
                </label>
              </div>
            </div>

            <!-- 선호 운동 -->
            <div class="SurveyField">
              <label class="SurveyFieldLabel">선호하는 운동 (복수 선택 가능)</label>
              <div class="SurveyOptionGrid">
                <label v-for="ex in ['유산소(걷기, 자전거 등)', '근력(밴드, 아령, 체중부하)', '균형훈련(한 발 서기, Tai Chi 등)', '유연성(요가, 스트레칭 등)']" :key="ex"
                  :class="['SurveyOption', surveyData.preferredExercise.includes(ex) ? 'SurveyOptionSelected' : 'SurveyOptionUnselected']">
                  <input type="checkbox" :value="ex" v-model="surveyData.preferredExercise" class="SurveyOptionInput" />
                  <span>{{ ex }}</span>
                </label>
              </div>
              <input v-model="surveyData.preferredExerciseOther" type="text" placeholder="기타 운동 입력"
                class="SurveyOtherInput" />
            </div>

            <!-- 운동 목표 -->
            <div class="SurveyField">
              <label class="SurveyFieldLabel">운동 목표</label>
              <div class="SurveyOptionGrid">
                <label v-for="goal in ['체중 감량', '근육 증가', '건강 관리', '재활/통증 완화', '낙상 예방']" :key="goal"
                  :class="['SurveyOption', surveyData.exerciseGoal.includes(goal) ? 'SurveyOptionSelected' : 'SurveyOptionUnselected']">
                  <input type="checkbox" :value="goal" v-model="surveyData.exerciseGoal" class="SurveyOptionInput" />
                  <span>{{ goal }}</span>
                </label>
              </div>
              <input v-model="surveyData.exerciseGoalOther" type="text" placeholder="기타 목표 입력"
                class="SurveyOtherInput" />
            </div>

            <!-- 체력 수준 -->
            <div class="SurveyField">
              <label class="SurveyFieldLabel">본인 체력 수준 <span class="SurveyRequired">*</span></label>
              <div class="SurveyFitnessLevel">
                <span class="SurveyFitnessLevelLabel">매우 약함</span>
                <div class="SurveyFitnessLevelOptions">
                  <label v-for="level in [1, 2, 3, 4, 5]" :key="level"
                    :class="['SurveyFitnessLevelOption', surveyData.fitnessLevel === level ? 'SurveyFitnessLevelOptionSelected' : 'SurveyFitnessLevelOptionUnselected']">
                    <input type="radio" :value="level" v-model="surveyData.fitnessLevel" class="SurveyFitnessLevelInput" required />
                    <span>{{ level }}</span>
                  </label>
                </div>
                <span class="SurveyFitnessLevelLabel">매우 강함</span>
              </div>
            </div>
          </section>

          <!-- 5. 신체적 한계 -->
          <section class="SurveySection">
            <h2 class="SurveySectionTitle">5. 신체적 한계 및 특이사항</h2>
            
            <!-- 통증 부위 -->
            <div class="SurveyField">
              <label class="SurveyFieldLabel">운동 시 불편/통증 부위</label>
              <div class="SurveyOptionGrid">
                <label v-for="part in ['없음', '허리', '무릎', '어깨']" :key="part"
                  :class="['SurveyOption', surveyData.painArea.includes(part) ? 'SurveyOptionSelected' : 'SurveyOptionUnselected']">
                  <input type="checkbox" :value="part" v-model="surveyData.painArea" class="SurveyOptionInput" @change="handlePainChange(part)" />
                  <span>{{ part }}</span>
                </label>
              </div>
              <input v-model="surveyData.painAreaOther" type="text" placeholder="기타 부위 입력"
                class="SurveyOtherInput" />
            </div>

            <!-- 운동 제한 -->
            <div class="SurveyField">
              <label class="SurveyFieldLabel">의사로부터 운동 제한 지시 여부</label>
              <div class="SurveyOptionGrid">
                <label v-for="opt in ['예', '아니오']" :key="opt"
                  :class="['SurveyOption', surveyData.exerciseRestriction.includes(opt) ? 'SurveyOptionSelected' : 'SurveyOptionUnselected']">
                  <input type="checkbox" :value="opt" v-model="surveyData.exerciseRestriction" class="SurveyOptionInput" />
                  <span>{{ opt }}</span>
                </label>
              </div>
            </div>
          </section>

          <!-- 6. 심리/동기 요인 -->
          <section class="SurveySection">
            <h2 class="SurveySectionTitle">6. 심리/동기 요인</h2>
            
            <!-- 운동 시작 이유 -->
            <div class="SurveyField">
              <label class="SurveyFieldLabel">운동 시작 이유</label>
              <div class="SurveyOptionGrid">
                <label v-for="reason in ['건강 개선', '체형 관리', '스트레스 해소', '생활 습관 개선', '낙상 예방/균형 능력 향상']" :key="reason"
                  :class="['SurveyOption', surveyData.exerciseReason.includes(reason) ? 'SurveyOptionSelected' : 'SurveyOptionUnselected']">
                  <input type="checkbox" :value="reason" v-model="surveyData.exerciseReason" class="SurveyOptionInput" />
                  <span>{{ reason }}</span>
                </label>
              </div>
              <input v-model="surveyData.exerciseReasonOther" type="text" placeholder="기타 이유 입력"
                class="SurveyOtherInput" />
            </div>

            <!-- 운동 자신감 -->
            <div class="SurveyField">
              <label class="SurveyFieldLabel">운동 자신감</label>
              <div class="SurveyOptionGrid">
                <label v-for="conf in ['매우 자신 없음', '조금 자신 없음', '보통', '자신 있음', '매우 자신 있음']" :key="conf"
                  :class="['SurveyOption', surveyData.exerciseConfidence.includes(conf) ? 'SurveyOptionSelected' : 'SurveyOptionUnselected']">
                  <input type="checkbox" :value="conf" v-model="surveyData.exerciseConfidence" class="SurveyOptionInput" />
                  <span>{{ conf }}</span>
                </label>
              </div>
            </div>

            <!-- 선호 환경 -->
            <div class="SurveyField">
              <label class="SurveyFieldLabel">선호하는 운동 환경</label>
              <div class="SurveyOptionGrid">
                <label v-for="env in ['집', '헬스장', '야외', '그룹/수업']" :key="env"
                  :class="['SurveyOption', surveyData.exerciseEnvironment.includes(env) ? 'SurveyOptionSelected' : 'SurveyOptionUnselected']">
                  <input type="checkbox" :value="env" v-model="surveyData.exerciseEnvironment" class="SurveyOptionInput" />
                  <span>{{ env }}</span>
                </label>
              </div>
            </div>
          </section>

          <!-- 버튼 -->
          <div class="UserSurveyActions">
            <AppButton
              type="button"
              variant="ghost"
              @click="$router.back()"
              class="SurveyCancelButton"
            >
              취소
            </AppButton>
            <AppButton
              type="submit"
              :disabled="submitting"
              class="SurveySubmitButton"
            >
              {{ submitting ? '제출 중...' : '설문 제출' }}
            </AppButton>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { submitSurvey, getUserSurveyRequests } from '@/services/api/surveyRequests'
import AppButton from '@/components/common/AppButton.vue'

const route = useRoute()
const router = useRouter()

const requestId = route.params.requestId as string
const requestMessage = ref('건강 설문을 작성해주세요.')
const submitting = ref(false)

const surveyData = reactive({
  // 1. 기본 정보
  gender: [] as string[],
  age: null as number | null,
  height: null as number | null,
  weight: null as number | null,
  lifestyle: [] as string[],
  
  // 2. 건강 상태
  diseases: [] as string[],
  diseaseOther: '',
  surgery: [] as string[],
  medication: [] as string[],
  medicationName: '',
  sleep: [] as string[],
  
  // 3. 생활 습관
  smoking: [] as string[],
  alcohol: [] as string[],
  diet: [] as string[],
  
  // 4. 운동 경험
  exerciseFrequency: [] as string[],
  preferredExercise: [] as string[],
  preferredExerciseOther: '',
  exerciseGoal: [] as string[],
  exerciseGoalOther: '',
  fitnessLevel: 3,
  
  // 5. 신체 한계
  painArea: [] as string[],
  painAreaOther: '',
  exerciseRestriction: [] as string[],
  
  // 6. 심리/동기
  exerciseReason: [] as string[],
  exerciseReasonOther: '',
  exerciseConfidence: [] as string[],
  exerciseEnvironment: [] as string[],
})

onMounted(async () => {
  const userId = localStorage.getItem('userId')
  if (userId) {
    try {
      const requests = await getUserSurveyRequests(userId)
      const request = requests.find(r => r.id === requestId)
      if (request) {
        requestMessage.value = request.message
      }
    } catch (error) {
      console.error('설문 요청 정보 로드 실패:', error)
    }
  }
})

const handleDiseaseChange = (disease: string) => {
  if (disease === '없음' && surveyData.diseases.includes('없음')) {
    surveyData.diseases = ['없음']
  } else if (surveyData.diseases.includes('없음')) {
    surveyData.diseases = surveyData.diseases.filter(d => d !== '없음')
  }
}

const handlePainChange = (part: string) => {
  if (part === '없음' && surveyData.painArea.includes('없음')) {
    surveyData.painArea = ['없음']
  } else if (surveyData.painArea.includes('없음')) {
    surveyData.painArea = surveyData.painArea.filter(p => p !== '없음')
  }
}

const handleSubmit = async () => {
  if (!surveyData.age || !surveyData.height || !surveyData.weight || !surveyData.fitnessLevel) {
    alert('필수 항목을 모두 입력해주세요.')
    return
  }

  submitting.value = true

  try {
    // 설문 제출 (JSON 형태로 전송)
    await submitSurvey(requestId, surveyData)

    alert('설문이 제출되었습니다!')
    router.push('/user')
  } catch (error) {
    console.error('설문 제출 실패:', error)
    alert('설문 제출에 실패했습니다. 다시 시도해주세요.')
  } finally {
    submitting.value = false
  }
}
</script>
