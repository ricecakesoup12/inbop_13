<template>
  <div class="space-y-4">
    <!-- 상단 절반 화이트 박스 -->
    <div class="bg-white rounded-2xl shadow-soft h-[50vh] border border-gray-200 relative">
      <div class="h-full flex items-center justify-between py-6 px-6 gap-6">
      <!-- 좌측: 아바타 이미지 -->
      <div class="flex flex-col items-center justify-center w-1/2 relative">
        <!-- 오른쪽 상단 버튼들 -->
        <div class="absolute top-0 right-0 flex flex-col gap-2 z-20">
          <!-- 새싹 버튼 -->
          <button
            class="bg-green-200 hover:bg-green-300 text-green-700 rounded-full p-3 transition-all duration-300 transform hover:scale-110 relative"
            title="보유 새싹"
            disabled
          >
            <div class="text-2xl mb-0.5">🌱</div>
            <div class="text-xs font-bold">{{ sproutCount }}</div>
          </button>
          
          <!-- 상점 버튼 -->
          <a>
            <button
              @click="showShopPopup = true"
              class="bg-pink-200 hover:bg-pink-300 text-pink-700 rounded-full p-3 transition-all duration-300 transform hover:scale-110"
              title="상점 열기"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
              </svg>
            </button>
          </a>
          
          <!-- 119 신고 버튼 -->
          <a href="tel:119">
            <button
              class="bg-red-200 hover:bg-red-300 text-red-700 rounded-full p-3 transition-all duration-300 transform hover:scale-110"
              title="119 신고"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </button>
          </a>
          
          <!-- 보호자 연락 버튼 -->
          <a v-if="currentUser?.guardianPhone" :href="`tel:${currentUser.guardianPhone}`">
            <button
              class="bg-green-200 hover:bg-green-300 text-green-700 rounded-full p-3 transition-all duration-300 transform hover:scale-110"
              title="보호자 연락"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
              </svg>
            </button>
          </a>
        </div>
        
        <!-- 이미지가 있으면 표시, 없으면 플레이스홀더 -->
        <div v-if="currentAvatarSrc" class="relative">
          <img 
            :src="currentAvatarSrc" 
            alt="User Avatar" 
            class="w-36 h-36 object-contain"
            style="image-rendering: pixelated;"
            @error="handleImageError"
          />
        </div>
        <!-- 이미지 로드 실패 시 플레이스홀더 -->
        <div v-else class="w-36 h-36 bg-gray-200 rounded-lg flex items-center justify-center">
          <span class="text-6xl">👤</span>
        </div>
        <div class="text-center text-sm text-gray-600 mt-1 font-gowun bg-white/80 px-3 py-1 rounded-lg">
          {{ avatarStatus }} (레벨 {{ avatarLevel }})
        </div>
        <div class="text-center text-xs text-gray-500 mt-1 font-gowun">
          목표: {{ dailyGoal }}분 | 진행: {{ todayProgress }}분
        </div>
      </div>
      
      <!-- 우측: 목표치 표시 -->
      <div class="w-1/2 relative flex flex-col justify-center">
        <!-- 목표치 표시 (처방 수락 후에만) -->
        <div v-if="hasActivePrescription" class="px-6 py-4">
          <div class="text-center mb-4">
            <div class="text-lg font-bold text-gray-800 mb-4 font-gowun">오늘의 운동 목표</div>
            
            <!-- 모든 운동 완료 시 -->
            <div v-if="isAllExercisesCompleted" class="space-y-4">
              <div class="text-6xl mb-4">🌱</div>
              <div class="text-2xl font-bold text-primary font-gowun">오늘의 운동 완료!</div>
            </div>

            <!-- 운동 버튼들 -->
            <div v-else-if="activePrescription" class="space-y-3 text-sm font-gowun">
              <!-- 시작 스트레칭 버튼 -->
              <button 
                v-if="!exerciseCompleted.startStretching"
                @click="completeStartStretching"
                class="w-full bg-primary hover:bg-primary-hover text-white rounded-lg py-3 px-4 transition-colors font-gowun"
              >
                시작 스트레칭 {{ activePrescription.startStretchingMinutes }}분
              </button>
              
              <!-- 인터벌 운동 버튼들 (세트 수만큼) -->
              <div class="space-y-2">
                <template v-for="setNum in activePrescription.sets">
                  <button 
                    v-if="!exerciseCompleted.intervals[setNum - 1]"
                    :key="setNum"
                    @click="completeInterval(setNum - 1)"
                    class="w-full bg-primary hover:bg-primary-hover text-white rounded-lg py-3 px-4 transition-colors font-gowun"
                  >
                    인터벌 {{ setNum }}세트: 걷기 {{ activePrescription.walkingMinutes }}분 → 뛰기 {{ activePrescription.runningMinutes }}분
                  </button>
                </template>
              </div>
              
              <!-- 마무리 스트레칭 버튼 -->
              <button 
                v-if="!exerciseCompleted.endStretching"
                @click="completeEndStretching"
                class="w-full bg-primary hover:bg-primary-hover text-white rounded-lg py-3 px-4 transition-colors font-gowun"
              >
                마무리 스트레칭 {{ activePrescription.endStretchingMinutes }}분
              </button>
            </div>
          </div>
        </div>
      </div>
      </div>
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
          <div class="space-y-2">
            <div class="text-center">
              <NaverUserLocationMap :position="position" small :userName="currentUser?.name" />
            </div>
            <!-- 현재 주소 표시 -->
            <div v-if="currentAddress" class="text-xs text-gray-600 font-gowun text-center px-2 py-1 bg-gray-50 rounded">
              📍 {{ currentAddress }}
            </div>
            <RouterLink to="/user/location">
              <AppButton variant="ghost" class="w-full">
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                실시간 위치 보기
              </AppButton>
            </RouterLink>
          </div>
        </div>
      </AppCard>

      <!-- 우측: 대화창 -->
      <AppCard>
        <div class="p-4">
          <h3 class="font-semibold text-text-main mb-4 font-gowun">대화창</h3>
          <div class="h-48 border rounded-lg p-3 overflow-auto bg-gray-50 mb-3 space-y-2">
            <div v-for="(chatMessage, index) in chatMessages" :key="index" class="text-sm">
              <span :class="chatMessage.sender === 'user' ? 'text-primary font-semibold' : 'text-green-600 font-semibold'" class="font-gowun">
                {{ chatMessage.sender === 'user' ? currentUser?.name || '나' : '운동 선생님' }}:
              </span>
              <span class="text-text-sub ml-1 font-gowun">{{ chatMessage.message }}</span>
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
          </div>
        </div>
      </AppCard>
    </div>

    <!-- 처방 알람 팝업 -->
    <AppModal :open="showPrescriptionPopup" title="새로운 운동 처방" @close="closePrescriptionPopup">
      <div class="space-y-4">
        <p class="text-text-sub mb-4 font-gowun">운동 선생님으로부터 새로운 운동 처방이 도착했습니다.</p>
        <div v-if="pendingPrescription" class="bg-green-50 rounded-lg p-4 space-y-2 font-gowun">
          <div class="text-sm"><span class="font-semibold">시작 스트레칭:</span> {{ pendingPrescription.startStretchingMinutes }}분</div>
          <div class="text-sm"><span class="font-semibold">인터벌 운동:</span> 걷기 {{ pendingPrescription.walkingMinutes }}분 → 뛰기 {{ pendingPrescription.runningMinutes }}분 ({{ pendingPrescription.sets }}세트)</div>
          <div class="text-sm"><span class="font-semibold">마무리 스트레칭:</span> {{ pendingPrescription.endStretchingMinutes }}분</div>
        </div>
        <p class="text-text-sub font-gowun">처방을 수락하시겠습니까?</p>
      </div>
      <template #footer>
        <AppButton variant="ghost" @click="declinePrescriptionHandler">거부</AppButton>
        <AppButton variant="solid" @click="acceptPrescriptionHandler">수락</AppButton>
      </template>
    </AppModal>

    <!-- 설문 팝업 -->
    <AppModal :open="showSurveyPopup" title="새로운 설문 요청" @close="closeSurveyPopup">
      <p class="text-text-sub mb-4 font-gowun">보호자로부터 새로운 설문 요청이 있습니다.</p>
      <p class="text-text-sub mb-6 font-gowun">설문을 진행하시겠습니까?</p>
      <template #footer>
        <AppButton variant="ghost" @click="declineSurvey">나중에</AppButton>
        <AppButton variant="solid" @click="acceptSurvey">설문 시작</AppButton>
      </template>
    </AppModal>

    <!-- 상점 팝업 -->
    <AppModal :open="showShopPopup" title="상점" @close="closeShopPopup">
      <div class="space-y-4">
        <!-- 선반 형식으로 물건 배치 -->
        <div class="space-y-3">
          <!-- 선반 1 -->
          <div class="bg-gradient-to-r from-pink-100 via-purple-100 to-blue-100 rounded-lg p-4 border-2 border-pink-200">
            <div class="text-xs text-pink-600 font-gowun mb-2 font-semibold">선반 1</div>
            <div class="grid grid-cols-4 gap-3">
              <div 
                v-for="item in shopItems.slice(0, 4)" 
                :key="item.id"
                @click="buyItem(item)"
                :class="[
                  'bg-white rounded-lg p-3 shadow-md hover:shadow-lg transition-shadow cursor-pointer border-2',
                  sproutCount >= item.price 
                    ? 'border-transparent hover:border-pink-300' 
                    : 'border-gray-300 opacity-50 cursor-not-allowed'
                ]"
              >
                <div class="text-3xl mb-1 text-center">{{ item.emoji }}</div>
                <div class="text-xs text-center text-gray-700 font-gowun font-semibold">{{ item.name }}</div>
                <div class="text-xs text-center text-pink-500 font-gowun mt-1">🌱 {{ item.price }}</div>
                <div v-if="sproutCount < item.price" class="text-xs text-center text-red-500 font-gowun mt-1">새싹 부족</div>
              </div>
            </div>
          </div>

          <!-- 선반 2 -->
          <div class="bg-gradient-to-r from-purple-100 via-blue-100 to-green-100 rounded-lg p-4 border-2 border-purple-200">
            <div class="text-xs text-purple-600 font-gowun mb-2 font-semibold">선반 2</div>
            <div class="grid grid-cols-4 gap-3">
              <div 
                v-for="item in shopItems.slice(4, 8)" 
                :key="item.id"
                @click="buyItem(item)"
                :class="[
                  'bg-white rounded-lg p-3 shadow-md hover:shadow-lg transition-shadow cursor-pointer border-2',
                  sproutCount >= item.price 
                    ? 'border-transparent hover:border-purple-300' 
                    : 'border-gray-300 opacity-50 cursor-not-allowed'
                ]"
              >
                <div class="text-3xl mb-1 text-center">{{ item.emoji }}</div>
                <div class="text-xs text-center text-gray-700 font-gowun font-semibold">{{ item.name }}</div>
                <div class="text-xs text-center text-purple-500 font-gowun mt-1">🌱 {{ item.price }}</div>
                <div v-if="sproutCount < item.price" class="text-xs text-center text-red-500 font-gowun mt-1">새싹 부족</div>
              </div>
            </div>
          </div>

          <!-- 선반 3 -->
          <div class="bg-gradient-to-r from-blue-100 via-green-100 to-yellow-100 rounded-lg p-4 border-2 border-blue-200">
            <div class="text-xs text-blue-600 font-gowun mb-2 font-semibold">선반 3</div>
            <div class="grid grid-cols-4 gap-3">
              <div 
                v-for="item in shopItems.slice(8, 12)" 
                :key="item.id"
                @click="buyItem(item)"
                :class="[
                  'bg-white rounded-lg p-3 shadow-md hover:shadow-lg transition-shadow cursor-pointer border-2',
                  sproutCount >= item.price 
                    ? 'border-transparent hover:border-blue-300' 
                    : 'border-gray-300 opacity-50 cursor-not-allowed'
                ]"
              >
                <div class="text-3xl mb-1 text-center">{{ item.emoji }}</div>
                <div class="text-xs text-center text-gray-700 font-gowun font-semibold">{{ item.name }}</div>
                <div class="text-xs text-center text-blue-500 font-gowun mt-1">🌱 {{ item.price }}</div>
                <div v-if="sproutCount < item.price" class="text-xs text-center text-red-500 font-gowun mt-1">새싹 부족</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </AppModal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import AppCard from '@/components/common/AppCard.vue'
import AppButton from '@/components/common/AppButton.vue'
import AppModal from '@/components/common/AppModal.vue'
import NaverUserLocationMap from '@/components/map/NaverUserLocationMap.vue'
import { useGeo } from '@/composables/useGeo'
import { useMetricsStore } from '@/stores/metrics.store'
import { getChatMessages, sendChatMessage as sendChatAPI, type ChatMessage } from '@/services/api/chatMessages'
import { useUsersStore } from '@/stores/users.store'
import { getPendingSurveyRequests } from '@/services/api/surveyRequests'
import { updateExerciseStatus } from '@/services/api/exerciseStatus'
import { getPendingPrescription, acceptPrescription, declinePrescription, getPrescriptionsByUser, completePrescription, type ExercisePrescription } from '@/services/api/exercisePrescriptions'
import type { SurveyRequest } from '@/services/api/surveyRequests'
import { getSproutCount, earnSprout, spendSprouts } from '@/services/api/sprouts'

const router = useRouter()
const { position } = useGeo()
const metricsStore = useMetricsStore()
const usersStore = useUsersStore()

// 현재 주소
const currentAddress = ref<string>('')

// 주소 변환 함수
const getAddressFromPosition = async (lat: number, lng: number) => {
  try {
    currentAddress.value = `📍 위도: ${lat.toFixed(4)}, 경도: ${lng.toFixed(4)}`
  } catch (error) {
    console.error('주소 변환 실패:', error)
  }
}

// 위치 변경 감지
watch(position, (newPos) => {
  if (newPos) {
    getAddressFromPosition(newPos.lat, newPos.lng)
  }
}, { immediate: true })

const vital = computed(() => metricsStore.vitalNow)
const currentUser = computed(() => usersStore.detail)
const isExercising = ref(false)

// 디버깅용: 콘솔에서 확인할 수 있도록 전역 함수 등록
if (typeof window !== 'undefined') {
  (window as any).checkGuardianPhone = () => {
    console.log('=== 보호자 연락처 확인 ===')
    console.log('currentUser:', currentUser.value)
    console.log('보호자 연락처:', currentUser.value?.guardianPhone)
    console.log('usersStore.detail:', usersStore.detail)
    console.log('usersStore.detail?.guardianPhone:', usersStore.detail?.guardianPhone)
    return currentUser.value?.guardianPhone
  }
}
const caloriesBurned = ref(0)
const exerciseTimeInSeconds = ref(0)
const chatMessages = ref<ChatMessage[]>([])

// 처방 관련
const showPrescriptionPopup = ref(false)
const pendingPrescription = ref<ExercisePrescription | null>(null)
const activePrescription = ref<ExercisePrescription | null>(null)
const hasActivePrescription = computed(() => activePrescription.value !== null)

// 운동 완료 상태
const exerciseCompleted = ref({
  startStretching: false,
  intervals: [] as boolean[], // 세트별 완료 상태
  endStretching: false
})

// 모든 운동 완료 여부 확인
const isAllExercisesCompleted = computed(() => {
  if (!activePrescription.value) return false
  
  // 시작 스트레칭 완료 확인
  if (!exerciseCompleted.value.startStretching) return false
  
  // 모든 인터벌 세트 완료 확인
  const allIntervalsCompleted = activePrescription.value.sets > 0 && 
    exerciseCompleted.value.intervals.length === activePrescription.value.sets &&
    exerciseCompleted.value.intervals.every(completed => completed)
  if (!allIntervalsCompleted) return false
  
  // 마무리 스트레칭 완료 확인
  if (!exerciseCompleted.value.endStretching) return false
  
  return true
})

// 아바타 시스템
const avatarLevel = ref(3) // 1=건강, 5=살찜
const dailyGoal = ref(30) // 일일 목표 (분)
const todayProgress = ref(0) // 오늘 진행 (분)

// 성별 매핑 (사용자의 Gender 타입을 아바타 이미지 타입으로 변환)
const userGender = computed(() => {
  if (!currentUser.value) return 'female'
  // '남성' -> 'male', '여성' -> 'female'
  return currentUser.value.gender === '남성' ? 'male' : 'female'
})

// 아바타 이미지 매핑 (1=건강, 5=살찜)
const avatarImages = {
  female: [
    null,
    '/images/female-1.png', // 1: 건강
    '/images/female-2.png',  // 2: 마른
    '/images/female-3.png',  // 3: 보통
    '/images/female-4.png',  // 4: 덜 찐
    '/images/female-5.png',  // 5: 살찐
  ],
  male: [
    null,
    '/images/male-1.png',  // 1: 건강
    '/images/male-2.png',  // 2: 마른
    '/images/male-3.png',  // 3: 보통
    '/images/male-4.png',  // 4: 덜 찐
    '/images/male-5.png',  // 5: 살찐
  ],
}

const currentAvatarSrc = computed(() => {
  const gender = userGender.value
  const level = avatarLevel.value
  return avatarImages[gender]?.[level] || ''
})

const handleImageError = () => {
  console.error('아바타 이미지 로드 실패')
}

const avatarStatus = computed(() => {
  const statuses = ['', '건강', '마른', '보통', '덜 찐', '살찐']
  return statuses[avatarLevel.value]
})

// 운동량 업데이트
const updateExerciseProgress = () => {
  todayProgress.value = Math.floor(exerciseTimeInSeconds.value / 60)
}

// 아바타 레벨 체크 및 업데이트
const checkDailyGoal = () => {
  const today = new Date().toISOString().slice(0, 10)
  const lastChecked = localStorage.getItem('lastAvatarCheck')
  
  if (lastChecked === today) return // 오늘 이미 체크함
  
  const achieved = todayProgress.value >= dailyGoal.value
  
  if (achieved) {
    // 목표 달성: 1쪽으로 이동 (더 건강)
    avatarLevel.value = Math.max(1, avatarLevel.value - 1)
  } else {
    // 목표 미달: 5쪽으로 이동 (더 살찜)
    avatarLevel.value = Math.min(5, avatarLevel.value + 1)
  }
  
  // 아바타 상태 저장
  localStorage.setItem('avatarLevel', avatarLevel.value.toString())
  localStorage.setItem('lastAvatarCheck', today)
}

// 아바타 상태 로드
const loadAvatarState = () => {
  const savedLevel = localStorage.getItem('avatarLevel')
  if (savedLevel) {
    avatarLevel.value = parseInt(savedLevel)
  }
}

// 운동 시간 추적을 위한 watch
watch(exerciseTimeInSeconds, () => {
  updateExerciseProgress()
})
const chatInput = ref('')
const showSurveyPopup = ref(false)
const showShopPopup = ref(false)
const pendingSurveyRequests = ref<SurveyRequest[]>([])

// 새싹 관련
const sproutCount = ref(0)
const isProcessingSproutEarn = ref(false) // 새싹 획득 중복 방지

// 상점 아이템 데이터
const shopItems = ref([
  { id: 1, name: '운동화', emoji: '👟', price: 5000 },
  { id: 2, name: '물병', emoji: '💧', price: 1000 },
  { id: 3, name: '헤드폰', emoji: '🎧', price: 3000 },
  { id: 4, name: '손목밴드', emoji: '⌚', price: 2000 },
  { id: 5, name: '수건', emoji: '🧺', price: 800 },
  { id: 6, name: '요가매트', emoji: '🧘', price: 4000 },
  { id: 7, name: '덤벨', emoji: '🏋️', price: 6000 },
  { id: 8, name: '밴드', emoji: '🏃', price: 2500 },
  { id: 9, name: '프로틴', emoji: '🥤', price: 3500 },
  { id: 10, name: '반바지', emoji: '🩳', price: 2800 },
  { id: 11, name: '티셔츠', emoji: '👕', price: 2200 },
  { id: 12, name: '모자', emoji: '🧢', price: 1500 },
])
const currentChatPage = ref(0)
const messagesPerPage = 4
const chatPages = computed(() => Math.max(1, Math.ceil(chatMessages.value.length / messagesPerPage)))

const displayedMessages = computed(() => {
  const start = currentChatPage.value * messagesPerPage
  const end = start + messagesPerPage
  return chatMessages.value.slice(start, end)
})

// 컨텐츠 영역 대화창에는 운동 선생님 메시지만 표시
const botMessages = computed(() => {
  return chatMessages.value.filter(msg => msg.sender === 'guardian')
})

const currentBotChatPage = ref(0)
const botMessagesPerPage = 3
const botChatPages = computed(() => Math.max(1, Math.ceil(botMessages.value.length / botMessagesPerPage)))

const displayedBotMessages = computed(() => {
  const start = currentBotChatPage.value * botMessagesPerPage
  const end = start + botMessagesPerPage
  return botMessages.value.slice(start, end)
})

const nextBotChatPage = () => {
  if (currentBotChatPage.value < botChatPages.value - 1) {
    currentBotChatPage.value++
  }
}

const previousBotChatPage = () => {
  if (currentBotChatPage.value > 0) {
    currentBotChatPage.value--
  }
}

// 채팅 메시지 로드
const loadChatMessages = async (userId: string) => {
  try {
    chatMessages.value = await getChatMessages(userId)
    console.log('✅ 채팅 메시지 로드 완료:', chatMessages.value.length, '개')
  } catch (error) {
    console.error('❌ 채팅 메시지 로드 실패:', error)
  }
}

const nextChatPage = () => {
  if (currentChatPage.value < chatPages.value - 1) {
    currentChatPage.value++
  }
}

const previousChatPage = () => {
  if (currentChatPage.value > 0) {
    currentChatPage.value--
  }
}

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

  // 아바타 상태 로드
  loadAvatarState()

  // 사용자 정보 로드
  await usersStore.fetchDetail(userId)
  
  // 디버깅: 사용자 데이터 확인
  console.log('=== 사용자 대시보드 - 사용자 정보 ===')
  console.log('currentUser:', currentUser.value)
  console.log('보호자 연락처:', currentUser.value?.guardianPhone)

  // 실시간 바이탈 구독
  metricsStore.subscribeRealtime(userId)

  // 채팅 메시지 로드 (에러 발생 시에도 계속 진행)
  try {
    await loadChatMessages(userId)
  } catch (error) {
    console.warn('⚠️ 채팅 메시지 로드 실패 (무시하고 계속):', error)
  }

  // 새싹 개수 로드
  try {
    await loadSproutCount(userId)
  } catch (error) {
    console.warn('⚠️ 새싹 개수 로드 실패 (무시하고 계속):', error)
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

  // 처방 확인 및 로드
  await checkPendingPrescription(userId)
  
  // 주기적으로 처방 확인 (30초마다)
  setInterval(() => {
    checkPendingPrescription(userId)
  }, 30000)
})

// 처방 확인
const checkPendingPrescription = async (userId: string) => {
  try {
    console.log('🔍 대기 중인 처방 확인 중... userId:', userId)
    const prescription = await getPendingPrescription(userId)
    if (prescription) {
      pendingPrescription.value = prescription
      showPrescriptionPopup.value = true
      console.log('✅ 새로운 처방 발견:', prescription)
    } else {
      console.log('⚠️ 대기 중인 처방 없음')
    }
  } catch (error: any) {
    console.error('❌ 처방 확인 실패:', error)
    console.error('❌ 에러 상세:', {
      message: error?.message,
      status: error?.response?.status,
      data: error?.response?.data
    })
  }
  
  // 활성화된 처방 확인 (ACCEPTED 상태)
  try {
    console.log('🔍 활성 처방 확인 중... userId:', userId)
    const prescriptions = await getPrescriptionsByUser(userId)
    console.log('📋 모든 처방:', prescriptions)
    
    const accepted = prescriptions.find(p => p.status === 'ACCEPTED' && !p.completedAt)
    if (accepted) {
      // 새로운 처방이거나 이전 처방과 다른 경우 완료 상태 초기화
      if (!activePrescription.value || activePrescription.value.id !== accepted.id) {
        activePrescription.value = accepted
        resetExerciseCompleted()
      } else {
        activePrescription.value = accepted
      }
      hasActivePrescription.value = true
      console.log('✅ 활성 처방:', accepted)
      console.log('🔗 시작 스트레칭 URL:', accepted.startStretchingUrl)
      console.log('🔗 마무리 스트레칭 URL:', accepted.endStretchingUrl)
    } else {
      hasActivePrescription.value = false
      console.log('⚠️ 활성 처방 없음')
    }
  } catch (error: any) {
    console.error('❌ 활성 처방 확인 실패:', error)
    console.error('❌ 에러 상세:', {
      message: error?.message,
      status: error?.response?.status,
      data: error?.response?.data
    })
  }
}

// 처방 수락
const acceptPrescriptionHandler = async () => {
  if (!pendingPrescription.value?.id) {
    console.error('❌ pendingPrescription.value.id가 없습니다')
    return
  }
  
  try {
    console.log('🔍 처방 수락 시도:', pendingPrescription.value.id)
    const accepted = await acceptPrescription(pendingPrescription.value.id)
    console.log('✅ 처방 수락 API 응답:', accepted)
    
    activePrescription.value = accepted
    hasActivePrescription.value = true
    showPrescriptionPopup.value = false
    pendingPrescription.value = null
    
    // 새로운 처방 수락 시 운동 완료 상태 초기화
    resetExerciseCompleted()
    
    alert('처방을 수락했습니다!')
    console.log('✅ 처방 수락 완료')
  } catch (error: any) {
    console.error('❌ 처방 수락 실패:', error)
    console.error('❌ 에러 상세:', {
      message: error?.message,
      status: error?.response?.status,
      data: error?.response?.data
    })
    alert('처방 수락에 실패했습니다.')
  }
}

// 운동 완료 상태 초기화
const resetExerciseCompleted = () => {
  exerciseCompleted.value = {
    startStretching: false,
    intervals: [],
    endStretching: false
  }
  
  // 인터벌 배열 초기화 (세트 수만큼)
  if (activePrescription.value) {
    exerciseCompleted.value.intervals = new Array(activePrescription.value.sets).fill(false)
  }
}

// 모든 운동 완료 처리 (공통 함수)
const handleAllExercisesCompleted = async () => {
  // 중복 실행 방지
  if (isProcessingSproutEarn.value) {
    console.log('⚠️ 새싹 획득 처리 중입니다. 대기...')
    return
  }
  
  if (!activePrescription.value) {
    console.warn('⚠️ 활성 처방이 없습니다')
    return
  }
  
  const allCompleted = 
    exerciseCompleted.value.startStretching &&
    exerciseCompleted.value.intervals.length === activePrescription.value.sets &&
    exerciseCompleted.value.intervals.every(completed => completed) &&
    exerciseCompleted.value.endStretching
  
  if (!allCompleted) {
    console.log('📊 운동 완료 상태:', {
      startStretching: exerciseCompleted.value.startStretching,
      intervals: exerciseCompleted.value.intervals,
      endStretching: exerciseCompleted.value.endStretching,
      requiredSets: activePrescription.value.sets,
      intervalsLength: exerciseCompleted.value.intervals.length
    })
    return
  }
  
  // 중복 실행 방지 플래그 설정
  isProcessingSproutEarn.value = true
  
  try {
    console.log('🎉 오늘의 운동 완료!')
    
    const userId = localStorage.getItem('userId')
    if (!userId) {
      console.error('❌ userId가 없습니다')
      return
    }
    
    // 1. 처방 상태를 COMPLETED로 변경 (DB에 저장)
    if (activePrescription.value?.id) {
      try {
        await completePrescription(activePrescription.value.id)
        console.log('✅ 처방 완료 상태로 변경 완료 (DB 저장됨)')
      } catch (error) {
        console.error('❌ 처방 완료 상태 변경 실패:', error)
      }
    }
    
    // 2. 새싹 획득 (하루 1회 제한은 백엔드에서 처리)
    try {
      console.log('🌱 새싹 획득 시도... userId:', userId)
      const result = await earnSprout(userId)
      sproutCount.value = result.sproutCount
      console.log('✅ 새싹 획득 완료! 현재 새싹:', sproutCount.value)
      alert(`🎉 오늘의 운동 완료!\n🌱 새싹 +1 획득! (보유: ${sproutCount.value}개)`)
    } catch (error: any) {
      console.error('❌ 새싹 획득 실패:', error)
      console.error('❌ 에러 상세:', {
        message: error?.message,
        response: error?.response?.data,
        status: error?.response?.status,
        fullError: error
      })
      // 오늘 이미 받았으면 에러 메시지만 표시
      if (error.message?.includes('이미')) {
        alert('오늘은 이미 새싹을 받았습니다.')
      } else {
        alert('새싹 획득에 실패했습니다. 다시 시도해주세요.')
      }
    }
  } finally {
    // 플래그 해제
    isProcessingSproutEarn.value = false
  }
}

// 시작 스트레칭 완료
const completeStartStretching = async () => {
  console.log('🔍 시작 스트레칭 버튼 클릭')
  console.log('📋 activePrescription:', activePrescription.value)
  console.log('🔗 startStretchingUrl:', activePrescription.value?.startStretchingUrl)
  
  // URL이 있으면 새 창으로 열기
  if (activePrescription.value?.startStretchingUrl) {
    const url = activePrescription.value.startStretchingUrl
    console.log('✅ URL이 있음, 새 창으로 열기:', url)
    const newWindow = window.open(url, '_blank')
    if (!newWindow) {
      console.warn('⚠️ 팝업이 차단되었을 수 있습니다')
      alert('팝업 차단을 해제해주세요')
    } else {
      console.log('✅ 새 창 열림')
    }
  } else {
    console.log('ℹ️ URL이 없어서 새 창 열지 않음')
  }
  
  exerciseCompleted.value.startStretching = true
  console.log('✅ 시작 스트레칭 완료')
  await handleAllExercisesCompleted()
}

// 인터벌 세트 완료
const completeInterval = async (setIndex: number) => {
  exerciseCompleted.value.intervals[setIndex] = true
  console.log(`✅ 인터벌 ${setIndex + 1}세트 완료`)
  await handleAllExercisesCompleted()
}

// 마무리 스트레칭 완료
const completeEndStretching = async () => {
  console.log('🔍 마무리 스트레칭 버튼 클릭')
  console.log('📋 activePrescription:', activePrescription.value)
  console.log('🔗 endStretchingUrl:', activePrescription.value?.endStretchingUrl)
  
  // URL이 있으면 새 창으로 열기
  if (activePrescription.value?.endStretchingUrl) {
    const url = activePrescription.value.endStretchingUrl
    console.log('✅ URL이 있음, 새 창으로 열기:', url)
    const newWindow = window.open(url, '_blank')
    if (!newWindow) {
      console.warn('⚠️ 팝업이 차단되었을 수 있습니다')
      alert('팝업 차단을 해제해주세요')
    } else {
      console.log('✅ 새 창 열림')
    }
  } else {
    console.log('ℹ️ URL이 없어서 새 창 열지 않음')
  }
  
  exerciseCompleted.value.endStretching = true
  console.log('✅ 마무리 스트레칭 완료')
  await handleAllExercisesCompleted()
}

// 처방 거부
const declinePrescriptionHandler = async () => {
  if (!pendingPrescription.value?.id) {
    console.error('❌ pendingPrescription.value.id가 없습니다')
    return
  }
  
  try {
    console.log('🔍 처방 거부 시도:', pendingPrescription.value.id)
    await declinePrescription(pendingPrescription.value.id)
    showPrescriptionPopup.value = false
    pendingPrescription.value = null
    console.log('✅ 처방 거부 완료')
  } catch (error: any) {
    console.error('❌ 처방 거부 실패:', error)
    console.error('❌ 에러 상세:', {
      message: error?.message,
      status: error?.response?.status,
      data: error?.response?.data
    })
    alert('처방 거부에 실패했습니다.')
  }
}

// 처방 팝업 닫기
const closePrescriptionPopup = () => {
  showPrescriptionPopup.value = false
}

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
  const wasExercising = isExercising.value
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
    
    // 운동 종료 시 일일 목표 체크
    if (wasExercising) {
      checkDailyGoal()
    }
  }
}

const exerciseTimeFormatted = computed(() => {
  const minutes = Math.floor(exerciseTimeInSeconds.value / 60)
  const seconds = exerciseTimeInSeconds.value % 60
  return `${minutes}분 ${seconds}초`
})

const sendChatMessage = async () => {
  if (!chatInput.value.trim()) return

  const userId = localStorage.getItem('userId')
  if (!userId || !currentUser.value) {
    alert('사용자 정보를 확인할 수 없습니다.')
    return
  }

  const messageText = chatInput.value
  chatInput.value = ''

  try {
    // API로 메시지 전송
    const newMessage = await sendChatAPI({
      userId: userId,
      sender: 'user',
      senderName: currentUser.value.name,
      message: messageText
    })

    // 로컬 채팅에 추가
    chatMessages.value.push(newMessage)
    console.log('✅ 메시지 전송 완료:', newMessage)

    // 새 메시지가 추가되면 마지막 페이지로 이동
    currentChatPage.value = chatPages.value - 1
  } catch (error) {
    console.error('❌ 메시지 전송 실패:', error)
    alert('메시지 전송에 실패했습니다.')
    chatInput.value = messageText // 실패 시 입력 복원
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

// 새싹 개수 로드
const loadSproutCount = async (userId: string) => {
  try {
    sproutCount.value = await getSproutCount(userId)
    console.log('✅ 새싹 개수 로드 완료:', sproutCount.value)
  } catch (error) {
    console.error('❌ 새싹 개수 로드 실패:', error)
    sproutCount.value = 0
  }
}

// 상점 아이템 구매
const buyItem = async (item: { id: number; name: string; price: number }) => {
  if (sproutCount.value < item.price) {
    alert(`보유 새싹이 부족합니다.\n보유: ${sproutCount.value}개\n필요: ${item.price}개`)
    return
  }

  const userId = localStorage.getItem('userId')
  if (!userId) {
    alert('사용자 정보를 확인할 수 없습니다.')
    return
  }

  if (!confirm(`${item.name}을(를) 구매하시겠습니까?\n🌱 ${item.price}개 차감`)) {
    return
  }

  try {
    const result = await spendSprouts(userId, item.price)
    sproutCount.value = result.sproutCount
    alert(`✅ ${item.name} 구매 완료!\n남은 새싹: ${sproutCount.value}개`)
    console.log('✅ 아이템 구매 완료:', item.name, '남은 새싹:', sproutCount.value)
  } catch (error: any) {
    console.error('❌ 아이템 구매 실패:', error)
    alert(error.message || '구매에 실패했습니다.')
  }
}

const closeShopPopup = () => {
  showShopPopup.value = false
}
</script>

