<template>
  <div class="space-y-4">
    <!-- 상단 절반 화이트 박스 -->
    <div class="bg-white rounded-2xl shadow-soft h-[50vh] flex items-center justify-center border border-gray-200">
      <p class="text-text-sub font-gowun">컨텐츠 영역</p>
    </div>

    <!-- 하단 3분할 -->
    <div class="grid md:grid-cols-3 gap-4">
      <!-- 좌측: 실시간 바이탈 + 설문 결과 보기 -->
      <AppCard>
        <div class="p-4">
          <h3 class="font-semibold text-text-main mb-4 flex items-center gap-2 font-gowun">
            <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
            </svg>
            실시간 바이탈
          </h3>
          <div class="space-y-3 mb-4">
            <div class="flex justify-between items-center">
              <span class="text-sm text-text-sub font-gowun">심박수</span>
              <span class="text-2xl font-bold text-primary font-gowun">{{ vital.hr }}</span>
              <span class="text-xs text-text-sub font-gowun">bpm</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-sm text-text-sub font-gowun">SpO₂</span>
              <span class="text-2xl font-bold text-primary font-gowun">{{ vital.spo2 }}</span>
              <span class="text-xs text-text-sub font-gowun">%</span>
            </div>
          </div>
          <div class="space-y-2">
            <RouterLink to="/user/survey/result">
              <AppButton variant="ghost" class="w-full">설문 결과 보기</AppButton>
            </RouterLink>
            
            <!-- 새로운 설문 요청 알림 -->
            <div v-if="pendingSurveyRequests.length > 0" class="space-y-2">
              <div
                v-for="surveyRequest in pendingSurveyRequests"
                :key="surveyRequest.id"
                class="relative"
              >
                <AppButton
                  @click="goToSurvey(surveyRequest.id)"
                  class="w-full bg-red-500 hover:bg-red-600 text-white animate-pulse"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                  </svg>
                  새로운 설문 요청
                </AppButton>
                <span class="absolute -top-1 -right-1 w-3 h-3 bg-red-500 rounded-full animate-ping"></span>
              </div>
            </div>
          </div>
        </div>
      </AppCard>

      <!-- 가운데: 운동 시작 + 칼로리/시간 + 위치 -->
      <AppCard>
        <div class="p-4">
          <h3 class="font-semibold text-text-main mb-4 font-gowun">운동</h3>
          <AppButton
            @click="toggleWorkout"
            :variant="isExercising ? 'outline' : 'solid'"
            class="w-full mb-4"
          >
            {{ isExercising ? '운동 중지' : '운동 시작' }}
          </AppButton>
          <div class="grid grid-cols-2 gap-3 mb-4 text-sm">
            <div class="text-center p-3 bg-beige rounded-lg">
              <div class="text-text-sub font-gowun">칼로리</div>
              <div class="text-xl font-bold text-text-main font-gowun">{{ caloriesBurned }}</div>
              <div class="text-xs text-text-sub font-gowun">kcal</div>
            </div>
            <div class="text-center p-3 bg-beige rounded-lg">
              <div class="text-text-sub font-gowun">시간</div>
              <div class="text-xl font-bold text-text-main font-gowun">{{ exerciseTimeFormatted }}</div>
            </div>
          </div>
          <div class="text-center">
            <UserLocationMap :position="position" small />
          </div>
        </div>
      </AppCard>

      <!-- 우측: 챗봇 + 음성 인식 -->
      <AppCard>
        <div class="p-4">
          <h3 class="font-semibold text-text-main mb-4 font-gowun">코칭 챗봇</h3>
          <div class="h-48 border rounded-lg p-3 overflow-auto bg-gray-50 mb-3 space-y-2">
            <div v-for="(chatMessage, index) in chatMessages" :key="index" class="text-sm">
              <span :class="chatMessage.role === 'user' ? 'text-primary font-semibold' : 'text-text-main font-semibold'" class="font-gowun">
                {{ chatMessage.role === 'user' ? '나' : '봇' }}:
              </span>
              <span class="text-text-sub ml-1 font-gowun">{{ chatMessage.text }}</span>
            </div>
          </div>
          <div class="flex gap-2">
            <input
              v-model="chatInput"
              @keyup.enter="sendChatMessage"
              class="flex-1 border-2 border-gray-200 rounded-lg px-3 py-2 text-sm focus:border-primary outline-none font-gowun"
              placeholder="메시지 입력"
            />
            <AppButton @click="sendChatMessage" variant="solid" class="px-4">전송</AppButton>
            <button
              @click="toggleVoiceRecognition"
              :class="listening ? 'bg-red-500 text-white' : 'bg-gray-100 text-text-main'"
              class="px-3 py-2 rounded-lg transition-colors"
              :title="listening ? '음성 인식 중...' : '음성 인식 시작'"
            >
              🎤
            </button>
          </div>
        </div>
      </AppCard>
    </div>

    <!-- 설문 팝업 -->
    <AppModal :open="showSurveyPopup" title="새로운 설문 요청" @close="closeSurveyPopup">
      <p class="text-text-sub mb-4 font-gowun">보호자로부터 새로운 설문 요청이 있습니다.</p>
      <p class="text-text-sub mb-6 font-gowun">설문을 진행하시겠습니까?</p>
      <template #footer>
        <AppButton variant="ghost" @click="declineSurvey">나중에</AppButton>
        <AppButton variant="solid" @click="acceptSurvey">설문 시작</AppButton>
      </template>
    </AppModal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppCard from '@/components/common/AppCard.vue'
import AppButton from '@/components/common/AppButton.vue'
import AppModal from '@/components/common/AppModal.vue'
import UserLocationMap from '@/components/map/UserLocationMap.vue'
import { useGeo } from '@/composables/useGeo'
import { useSpeech } from '@/composables/useSpeech'
import { useMetricsStore } from '@/stores/metrics.store'
import { useUsersStore } from '@/stores/users.store'
import { getPendingSurveyRequests } from '@/services/api/surveyRequests'
import { updateExerciseStatus } from '@/services/api/exerciseStatus'
import type { SurveyRequest } from '@/services/api/surveyRequests'

const router = useRouter()
const { position } = useGeo()
const { listening, transcript, start, stop } = useSpeech()
const metricsStore = useMetricsStore()
const usersStore = useUsersStore()

const vital = computed(() => metricsStore.vitalNow)
const currentUser = computed(() => usersStore.detail)
const isExercising = ref(false)
const caloriesBurned = ref(0)
const exerciseTimeInSeconds = ref(0)
const chatMessages = ref<{ role: 'user' | 'bot'; text: string }[]>([
  { role: 'bot', text: '안녕하세요! 운동을 도와드릴게요.' },
])
const chatInput = ref('')
const showSurveyPopup = ref(false)
const pendingSurveyRequests = ref<SurveyRequest[]>([])

let exerciseTimer: number | null = null

onMounted(async () => {
  // localStorage에서 사용자 ID 확인
  const userId = localStorage.getItem('userId')
  const userCode = localStorage.getItem('userCode')

  if (!userId || !userCode) {
    // 로그인 정보가 없으면 등록 페이지로 이동
    alert('로그인이 필요합니다.')
    router.push('/user/register')
    return
  }

  // 사용자 정보 로드
  await usersStore.fetchDetail(userId)

  // 실시간 바이탈 구독
  metricsStore.subscribeRealtime(userId)

  // 환영 메시지
  if (currentUser.value) {
    chatMessages.value.unshift({
      role: 'bot',
      text: `${currentUser.value.name}님, 환영합니다! 오늘도 힘차게 운동해봐요!`,
    })
  }

  // 대기 중인 설문 요청 조회
  await loadPendingSurveyRequests(userId)

  // 주기적으로 설문 요청 확인 (30초마다)
  setInterval(() => {
    loadPendingSurveyRequests(userId)
  }, 30000)

  // 설문 요청 이벤트 리스너
  window.addEventListener('surveyRequest', () => {
    showSurveyPopup.value = true
  })
})

const loadPendingSurveyRequests = async (userId: string) => {
  try {
    pendingSurveyRequests.value = await getPendingSurveyRequests(userId)
  } catch (error) {
    console.error('설문 요청 조회 실패:', error)
  }
}

const goToSurvey = (requestId: string) => {
  router.push(`/user/survey/${requestId}`)
}

const toggleWorkout = async () => {
  isExercising.value = !isExercising.value
  
  // 백엔드에 운동 상태 전송
  const userId = localStorage.getItem('userId')
  if (userId) {
    try {
      await updateExerciseStatus(userId, isExercising.value)
    } catch (error) {
      console.error('운동 상태 업데이트 실패:', error)
    }
  }

  if (isExercising.value) {
    // 운동 시작
    exerciseTimer = window.setInterval(() => {
      exerciseTimeInSeconds.value += 1
      // 간단한 칼로리 계산 (1분당 5kcal 가정)
      caloriesBurned.value = Math.floor((exerciseTimeInSeconds.value / 60) * 5)
    }, 1000)
  } else {
    // 운동 중지
    if (exerciseTimer) {
      clearInterval(exerciseTimer)
      exerciseTimer = null
    }
  }
}

const exerciseTimeFormatted = computed(() => {
  const minutes = Math.floor(exerciseTimeInSeconds.value / 60)
  const seconds = exerciseTimeInSeconds.value % 60
  return `${minutes}분 ${seconds}초`
})

const sendChatMessage = () => {
  if (!chatInput.value.trim()) return

  chatMessages.value.push({ role: 'user', text: chatInput.value })

  // 간단한 봇 응답 (실제로는 API 연동)
  setTimeout(() => {
    chatMessages.value.push({ role: 'bot', text: '좋아요! 계속 힘내세요!' })
  }, 500)

  chatInput.value = ''
}

const toggleVoiceRecognition = () => {
  if (listening.value) {
    stop()
  } else {
    start()
    // 음성 인식 결과를 input에 반영
    setTimeout(() => {
      if (transcript.value) {
        chatInput.value = transcript.value
      }
    }, 100)
  }
}

const closeSurveyPopup = () => {
  showSurveyPopup.value = false
}

const acceptSurvey = () => {
  showSurveyPopup.value = false
  router.push('/user/survey')
}

const declineSurvey = () => {
  showSurveyPopup.value = false
  console.log('사용자가 설문을 거부했습니다.')
}
</script>

