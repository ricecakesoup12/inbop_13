<template>
  <div class="min-h-screen bg-gradient-to-br from-beige to-white p-6">
    <div class="max-w-4xl mx-auto">
      <div class="bg-white rounded-2xl shadow-soft border border-gray-200 p-8">
        <div class="mb-8">
          <h1 class="text-3xl font-bold text-text-main font-gowun mb-4">건강 설문</h1>
          
          <!-- 보호자 요청 메시지 강조 -->
          <div class="bg-primary/10 border-l-4 border-primary rounded-lg p-4 mb-6">
            <div class="flex items-start gap-3">
              <svg class="w-6 h-6 text-primary flex-shrink-0 mt-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <div>
                <div class="text-sm text-text-sub font-gowun mb-1">보호자 요청 사항</div>
                <p class="text-lg font-semibold text-text-main font-gowun">{{ requestMessage }}</p>
              </div>
            </div>
          </div>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-8">
          <!-- 1. 기본 정보 -->
          <section class="border-b border-gray-200 pb-6">
            <h2 class="text-xl font-bold text-text-main font-gowun mb-4">1. 기본 정보</h2>
            
            <!-- 성별 -->
            <div class="mb-4">
              <label class="block font-semibold mb-3 font-gowun">
                성별 <span class="text-red-500">*</span>
              </label>
              <div class="grid grid-cols-3 gap-3">
                <label v-for="g in ['남성', '여성', '기타/응답하지 않음']" :key="g"
                  :class="['flex items-center justify-center p-3 border-2 rounded-xl cursor-pointer transition-all font-gowun',
                    surveyData.gender.includes(g) ? 'bg-primary/10 border-primary text-primary font-semibold' : 'border-gray-200 hover:border-gray-300']">
                  <input type="checkbox" :value="g" v-model="surveyData.gender" class="hidden" />
                  <span>{{ g }}</span>
                </label>
              </div>
            </div>

            <!-- 나이, 키, 몸무게 -->
            <div class="grid grid-cols-3 gap-4 mb-4">
              <div>
                <label class="block font-semibold mb-2 font-gowun">나이 <span class="text-red-500">*</span></label>
                <input v-model.number="surveyData.age" type="number" required placeholder="세"
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:border-primary outline-none font-gowun" />
              </div>
              <div>
                <label class="block font-semibold mb-2 font-gowun">키 <span class="text-red-500">*</span></label>
                <input v-model.number="surveyData.height" type="number" required placeholder="cm"
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:border-primary outline-none font-gowun" />
              </div>
              <div>
                <label class="block font-semibold mb-2 font-gowun">몸무게 <span class="text-red-500">*</span></label>
                <input v-model.number="surveyData.weight" type="number" required placeholder="kg"
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:border-primary outline-none font-gowun" />
              </div>
            </div>

            <!-- 생활 패턴 -->
            <div>
              <label class="block font-semibold mb-3 font-gowun">평소 생활 패턴 <span class="text-red-500">*</span></label>
              <div class="space-y-2">
                <label v-for="pattern in ['주로 앉아 있음 (사무직 등)', '가볍게 활동함 (학생, 가사 등)', '많이 움직임 (서비스직, 현장 근무 등)']" :key="pattern"
                  :class="['flex items-center p-3 border-2 rounded-xl cursor-pointer transition-all font-gowun',
                    surveyData.lifestyle.includes(pattern) ? 'bg-primary/10 border-primary text-primary font-semibold' : 'border-gray-200 hover:border-gray-300']">
                  <input type="checkbox" :value="pattern" v-model="surveyData.lifestyle" class="hidden" />
                  <span>{{ pattern }}</span>
                </label>
              </div>
            </div>
          </section>

          <!-- 2. 건강 상태 -->
          <section class="border-b border-gray-200 pb-6">
            <h2 class="text-xl font-bold text-text-main font-gowun mb-4">2. 건강 상태</h2>
            
            <!-- 진단받은 질환 -->
            <div class="mb-4">
              <label class="block font-semibold mb-3 font-gowun">현재 진단받은 질환</label>
              <div class="grid grid-cols-3 gap-2">
                <label v-for="disease in ['없음', '고혈압', '당뇨', '심장질환', '호흡기질환', '관절질환']" :key="disease"
                  :class="['flex items-center justify-center p-3 border-2 rounded-lg cursor-pointer transition-all font-gowun text-sm',
                    surveyData.diseases.includes(disease) ? 'bg-primary/10 border-primary text-primary font-semibold' : 'border-gray-200 hover:border-gray-300']">
                  <input type="checkbox" :value="disease" v-model="surveyData.diseases" class="hidden" @change="handleDiseaseChange(disease)" />
                  <span>{{ disease }}</span>
                </label>
              </div>
              <input v-model="surveyData.diseaseOther" type="text" placeholder="기타 질환 입력"
                class="w-full mt-2 px-4 py-2 border-2 border-gray-200 rounded-xl focus:border-primary outline-none font-gowun text-sm" />
            </div>

            <!-- 최근 수술/부상 -->
            <div class="mb-4">
              <label class="block font-semibold mb-3 font-gowun">최근 6개월 이내 수술/큰 부상</label>
              <div class="grid grid-cols-2 gap-3">
                <label v-for="opt in ['예', '아니오']" :key="opt"
                  :class="['flex items-center justify-center p-3 border-2 rounded-xl cursor-pointer transition-all font-gowun',
                    surveyData.surgery.includes(opt) ? 'bg-primary/10 border-primary text-primary font-semibold' : 'border-gray-200 hover:border-gray-300']">
                  <input type="checkbox" :value="opt" v-model="surveyData.surgery" class="hidden" />
                  <span>{{ opt }}</span>
                </label>
              </div>
            </div>

            <!-- 복용 중인 약 -->
            <div class="mb-4">
              <label class="block font-semibold mb-3 font-gowun">현재 복용 중인 약</label>
              <div class="grid grid-cols-2 gap-3 mb-2">
                <label v-for="opt in ['예', '아니오']" :key="opt"
                  :class="['flex items-center justify-center p-3 border-2 rounded-xl cursor-pointer transition-all font-gowun',
                    surveyData.medication.includes(opt) ? 'bg-primary/10 border-primary text-primary font-semibold' : 'border-gray-200 hover:border-gray-300']">
                  <input type="checkbox" :value="opt" v-model="surveyData.medication" class="hidden" />
                  <span>{{ opt }}</span>
                </label>
              </div>
              <input v-if="surveyData.medication.includes('예')" v-model="surveyData.medicationName" type="text" placeholder="약물명 입력"
                class="w-full px-4 py-2 border-2 border-gray-200 rounded-xl focus:border-primary outline-none font-gowun text-sm" />
            </div>

            <!-- 수면 시간 -->
            <div>
              <label class="block font-semibold mb-3 font-gowun">하루 평균 수면 시간 <span class="text-red-500">*</span></label>
              <div class="grid grid-cols-4 gap-3">
                <label v-for="hours in ['4시간 이하', '5–6시간', '7–8시간', '9시간 이상']" :key="hours"
                  :class="['flex items-center justify-center p-3 border-2 rounded-xl cursor-pointer transition-all font-gowun text-sm',
                    surveyData.sleep.includes(hours) ? 'bg-primary/10 border-primary text-primary font-semibold' : 'border-gray-200 hover:border-gray-300']">
                  <input type="checkbox" :value="hours" v-model="surveyData.sleep" class="hidden" />
                  <span>{{ hours }}</span>
                </label>
              </div>
            </div>
          </section>

          <!-- 3. 생활 습관 -->
          <section class="border-b border-gray-200 pb-6">
            <h2 class="text-xl font-bold text-text-main font-gowun mb-4">3. 생활 습관</h2>
            
            <!-- 흡연 -->
            <div class="mb-4">
              <label class="block font-semibold mb-3 font-gowun">흡연 여부</label>
              <div class="grid grid-cols-3 gap-3">
                <label v-for="opt in ['비흡연', '현재 흡연 중', '과거 흡연자']" :key="opt"
                  :class="['flex items-center justify-center p-3 border-2 rounded-xl cursor-pointer transition-all font-gowun',
                    surveyData.smoking.includes(opt) ? 'bg-primary/10 border-primary text-primary font-semibold' : 'border-gray-200 hover:border-gray-300']">
                  <input type="checkbox" :value="opt" v-model="surveyData.smoking" class="hidden" />
                  <span>{{ opt }}</span>
                </label>
              </div>
            </div>

            <!-- 음주 -->
            <div class="mb-4">
              <label class="block font-semibold mb-3 font-gowun">음주 빈도</label>
              <div class="grid grid-cols-4 gap-3">
                <label v-for="freq in ['거의 안 함', '주 1–2회', '주 3–4회', '거의 매일']" :key="freq"
                  :class="['flex items-center justify-center p-3 border-2 rounded-xl cursor-pointer transition-all font-gowun text-sm',
                    surveyData.alcohol.includes(freq) ? 'bg-primary/10 border-primary text-primary font-semibold' : 'border-gray-200 hover:border-gray-300']">
                  <input type="checkbox" :value="freq" v-model="surveyData.alcohol" class="hidden" />
                  <span>{{ freq }}</span>
                </label>
              </div>
            </div>

            <!-- 식습관 -->
            <div>
              <label class="block font-semibold mb-3 font-gowun">평소 식습관</label>
              <div class="grid grid-cols-3 gap-3">
                <label v-for="diet in ['채소/과일 위주', '가공식품/외식 위주', '균형 잡힌 편']" :key="diet"
                  :class="['flex items-center justify-center p-3 border-2 rounded-xl cursor-pointer transition-all font-gowun text-sm',
                    surveyData.diet.includes(diet) ? 'bg-primary/10 border-primary text-primary font-semibold' : 'border-gray-200 hover:border-gray-300']">
                  <input type="checkbox" :value="diet" v-model="surveyData.diet" class="hidden" />
                  <span>{{ diet }}</span>
                </label>
              </div>
            </div>
          </section>

          <!-- 4. 운동 경험 및 체력 -->
          <section class="border-b border-gray-200 pb-6">
            <h2 class="text-xl font-bold text-text-main font-gowun mb-4">4. 운동 경험 및 체력</h2>
            
            <!-- 주간 운동 횟수 -->
            <div class="mb-4">
              <label class="block font-semibold mb-3 font-gowun">주간 평균 운동 횟수</label>
              <div class="grid grid-cols-4 gap-3">
                <label v-for="freq in ['전혀 안 함', '1–2회', '3–4회', '5회 이상']" :key="freq"
                  :class="['flex items-center justify-center p-3 border-2 rounded-xl cursor-pointer transition-all font-gowun text-sm',
                    surveyData.exerciseFrequency.includes(freq) ? 'bg-primary/10 border-primary text-primary font-semibold' : 'border-gray-200 hover:border-gray-300']">
                  <input type="checkbox" :value="freq" v-model="surveyData.exerciseFrequency" class="hidden" />
                  <span>{{ freq }}</span>
                </label>
              </div>
            </div>

            <!-- 선호 운동 -->
            <div class="mb-4">
              <label class="block font-semibold mb-3 font-gowun">선호하는 운동 (복수 선택 가능)</label>
              <div class="grid grid-cols-2 gap-2">
                <label v-for="ex in ['유산소(걷기, 자전거 등)', '근력(밴드, 아령, 체중부하)', '균형훈련(한 발 서기, Tai Chi 등)', '유연성(요가, 스트레칭 등)']" :key="ex"
                  :class="['flex items-center justify-center p-3 border-2 rounded-lg cursor-pointer transition-all font-gowun text-sm',
                    surveyData.preferredExercise.includes(ex) ? 'bg-primary/10 border-primary text-primary font-semibold' : 'border-gray-200 hover:border-gray-300']">
                  <input type="checkbox" :value="ex" v-model="surveyData.preferredExercise" class="hidden" />
                  <span>{{ ex }}</span>
                </label>
              </div>
              <input v-model="surveyData.preferredExerciseOther" type="text" placeholder="기타 운동 입력"
                class="w-full mt-2 px-4 py-2 border-2 border-gray-200 rounded-xl focus:border-primary outline-none font-gowun text-sm" />
            </div>

            <!-- 운동 목표 -->
            <div class="mb-4">
              <label class="block font-semibold mb-3 font-gowun">운동 목표</label>
              <div class="grid grid-cols-3 gap-2">
                <label v-for="goal in ['체중 감량', '근육 증가', '건강 관리', '재활/통증 완화', '낙상 예방']" :key="goal"
                  :class="['flex items-center justify-center p-3 border-2 rounded-lg cursor-pointer transition-all font-gowun text-sm',
                    surveyData.exerciseGoal.includes(goal) ? 'bg-primary/10 border-primary text-primary font-semibold' : 'border-gray-200 hover:border-gray-300']">
                  <input type="checkbox" :value="goal" v-model="surveyData.exerciseGoal" class="hidden" />
                  <span>{{ goal }}</span>
                </label>
              </div>
              <input v-model="surveyData.exerciseGoalOther" type="text" placeholder="기타 목표 입력"
                class="w-full mt-2 px-4 py-2 border-2 border-gray-200 rounded-xl focus:border-primary outline-none font-gowun text-sm" />
            </div>

            <!-- 체력 수준 -->
            <div>
              <label class="block font-semibold mb-3 font-gowun">본인 체력 수준 <span class="text-red-500">*</span></label>
              <div class="flex items-center gap-4">
                <span class="text-sm text-text-sub font-gowun">매우 약함</span>
                <div class="flex gap-2 flex-1 justify-center">
                  <label v-for="level in [1, 2, 3, 4, 5]" :key="level"
                    :class="['w-12 h-12 flex items-center justify-center border-2 rounded-full cursor-pointer transition-all font-gowun font-bold',
                      surveyData.fitnessLevel === level ? 'bg-primary border-primary text-white scale-110' : 'border-gray-300 hover:border-primary']">
                    <input type="radio" :value="level" v-model="surveyData.fitnessLevel" class="hidden" required />
                    <span>{{ level }}</span>
                  </label>
                </div>
                <span class="text-sm text-text-sub font-gowun">매우 강함</span>
              </div>
            </div>
          </section>

          <!-- 5. 신체적 한계 -->
          <section class="border-b border-gray-200 pb-6">
            <h2 class="text-xl font-bold text-text-main font-gowun mb-4">5. 신체적 한계 및 특이사항</h2>
            
            <!-- 통증 부위 -->
            <div class="mb-4">
              <label class="block font-semibold mb-3 font-gowun">운동 시 불편/통증 부위</label>
              <div class="grid grid-cols-3 gap-2">
                <label v-for="part in ['없음', '허리', '무릎', '어깨']" :key="part"
                  :class="['flex items-center justify-center p-3 border-2 rounded-lg cursor-pointer transition-all font-gowun',
                    surveyData.painArea.includes(part) ? 'bg-primary/10 border-primary text-primary font-semibold' : 'border-gray-200 hover:border-gray-300']">
                  <input type="checkbox" :value="part" v-model="surveyData.painArea" class="hidden" @change="handlePainChange(part)" />
                  <span>{{ part }}</span>
                </label>
              </div>
              <input v-model="surveyData.painAreaOther" type="text" placeholder="기타 부위 입력"
                class="w-full mt-2 px-4 py-2 border-2 border-gray-200 rounded-xl focus:border-primary outline-none font-gowun text-sm" />
            </div>

            <!-- 운동 제한 -->
            <div>
              <label class="block font-semibold mb-3 font-gowun">의사로부터 운동 제한 지시 여부</label>
              <div class="grid grid-cols-2 gap-3">
                <label v-for="opt in ['예', '아니오']" :key="opt"
                  :class="['flex items-center justify-center p-3 border-2 rounded-xl cursor-pointer transition-all font-gowun',
                    surveyData.exerciseRestriction.includes(opt) ? 'bg-primary/10 border-primary text-primary font-semibold' : 'border-gray-200 hover:border-gray-300']">
                  <input type="checkbox" :value="opt" v-model="surveyData.exerciseRestriction" class="hidden" />
                  <span>{{ opt }}</span>
                </label>
              </div>
            </div>
          </section>

          <!-- 6. 심리/동기 요인 -->
          <section class="pb-6">
            <h2 class="text-xl font-bold text-text-main font-gowun mb-4">6. 심리/동기 요인</h2>
            
            <!-- 운동 시작 이유 -->
            <div class="mb-4">
              <label class="block font-semibold mb-3 font-gowun">운동 시작 이유</label>
              <div class="grid grid-cols-3 gap-2">
                <label v-for="reason in ['건강 개선', '체형 관리', '스트레스 해소', '생활 습관 개선', '낙상 예방/균형 능력 향상']" :key="reason"
                  :class="['flex items-center justify-center p-3 border-2 rounded-lg cursor-pointer transition-all font-gowun text-sm',
                    surveyData.exerciseReason.includes(reason) ? 'bg-primary/10 border-primary text-primary font-semibold' : 'border-gray-200 hover:border-gray-300']">
                  <input type="checkbox" :value="reason" v-model="surveyData.exerciseReason" class="hidden" />
                  <span>{{ reason }}</span>
                </label>
              </div>
              <input v-model="surveyData.exerciseReasonOther" type="text" placeholder="기타 이유 입력"
                class="w-full mt-2 px-4 py-2 border-2 border-gray-200 rounded-xl focus:border-primary outline-none font-gowun text-sm" />
            </div>

            <!-- 운동 자신감 -->
            <div class="mb-4">
              <label class="block font-semibold mb-3 font-gowun">운동 자신감</label>
              <div class="grid grid-cols-5 gap-2">
                <label v-for="conf in ['매우 자신 없음', '조금 자신 없음', '보통', '자신 있음', '매우 자신 있음']" :key="conf"
                  :class="['flex items-center justify-center p-3 border-2 rounded-lg cursor-pointer transition-all font-gowun text-xs',
                    surveyData.exerciseConfidence.includes(conf) ? 'bg-primary/10 border-primary text-primary font-semibold' : 'border-gray-200 hover:border-gray-300']">
                  <input type="checkbox" :value="conf" v-model="surveyData.exerciseConfidence" class="hidden" />
                  <span>{{ conf }}</span>
                </label>
              </div>
            </div>

            <!-- 선호 환경 -->
            <div>
              <label class="block font-semibold mb-3 font-gowun">선호하는 운동 환경</label>
              <div class="grid grid-cols-4 gap-3">
                <label v-for="env in ['집', '헬스장', '야외', '그룹/수업']" :key="env"
                  :class="['flex items-center justify-center p-3 border-2 rounded-xl cursor-pointer transition-all font-gowun',
                    surveyData.exerciseEnvironment.includes(env) ? 'bg-primary/10 border-primary text-primary font-semibold' : 'border-gray-200 hover:border-gray-300']">
                  <input type="checkbox" :value="env" v-model="surveyData.exerciseEnvironment" class="hidden" />
                  <span>{{ env }}</span>
                </label>
              </div>
            </div>
          </section>

          <!-- 버튼 -->
          <div class="flex gap-4 pt-4">
            <AppButton type="button" variant="ghost" @click="$router.back()" class="flex-1">
              취소
            </AppButton>
            <AppButton type="submit" :disabled="submitting" class="flex-1">
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
