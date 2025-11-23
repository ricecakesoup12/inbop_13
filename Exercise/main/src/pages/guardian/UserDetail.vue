<template>
  <div v-if="loading" class="UserDetailLoading">
    <p>불러오는 중...</p>
  </div>

  <div v-else-if="error" class="UserDetailError">
    <p>{{ error }}</p>
  </div>

  <div v-else-if="user" class="GuardianUserDetailPage">
    <!-- 카드형 사용자 정보 -->
    <AppCard class="UserProfileCard">
      <div class="UserProfileContent">
        <div class="UserProfileHeader">
          <img
            :src="user.faceUrl || defaultFace"
            alt="프로필"
            class="UserProfileImage"
          />
          <div class="UserProfileGrid">
            <div>
              <span class="UserNameLabel">이름</span>
              <p class="UserNameValue">{{ user.name }}</p>
            </div>
            <div>
              <span class="UserGenderLabel">성별</span>
              <p class="UserGenderValue">{{ user.gender }}</p>
            </div>
            <div>
              <span class="UserAgeLabel">나이</span>
              <p class="UserAgeValue">{{ user.age }}세</p>
            </div>
            <div>
              <span class="UserHeightLabel">키</span>
              <p class="UserHeightValue">{{ user.height }}cm</p>
            </div>
            <div>
              <span class="UserWeightLabel">몸무게</span>
              <p class="UserWeightValue">{{ user.weight || '-' }}kg</p>
            </div>
            <div>
              <span class="UserDiseaseLabel">지병</span>
              <p class="UserDiseaseValue">
                {{ user.chronicDiseases?.join(', ') || '없음' }}
              </p>
            </div>
            <div v-if="user.guardianPhone">
              <span class="GuardianPhoneLabel">보호자 연락처</span>
              <div class="GuardianPhoneRow">
                <p class="GuardianPhoneValue">
                  📞 {{ user.guardianPhone }}
                </p>
                <a :href="`tel:${user.guardianPhone}`">
                  <button class="EmergencyContactButton">
                    긴급 연락
                  </button>
                </a>
              </div>
            </div>
            <div>
              <span class="ExerciseStatusLabel">운동 상태</span>
              <p
                class="ExerciseStatusValue"
                :class="exerciseStatus?.isExercising ? 'ExerciseStatusActive' : 'ExerciseStatusInactive'"
              >
                {{ exerciseStatus?.isExercising ? '🏃 운동 중' : '휴식 중' }}
              </p>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="UserProfileFooter">
          <div class="UserProfileFooterLeft">
            <AppButton variant="ghost" class="EditUserButton" @click="openEditModal">
              <svg class="EditUserButtonIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              정보 수정
            </AppButton>
            <a v-if="user.guardianPhone" :href="`tel:${user.guardianPhone}`">
              <AppButton
                variant="outline"
                class="GuardianEmergencyContactButton"
              >
                <svg class="GuardianEmergencyContactIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                </svg>
                긴급 연락하기
              </AppButton>
            </a>
          </div>
          <div class="UserProfileFooterRight">
            <RouterLink :to="`/guardian/users/${id}/survey/send`">
              <AppButton variant="solid" class="SendSurveyButton">설문 보내기</AppButton>
            </RouterLink>
            <RouterLink :to="`/guardian/users/${id}/survey/result`">
              <AppButton variant="ghost" class="ViewSurveyResultButton">설문 보기</AppButton>
            </RouterLink>
          </div>
        </div>
      </template>
    </AppCard>

    <!-- 지도 -->
    <div class="UserLocationSection">
      <h3 class="UserLocationTitle">
        <span>현재 위치</span>
        <span v-if="userLocation" class="UserLocationTrackingBadge">
          <span class="UserLocationTrackingDot"></span>
          실시간 추적 중
        </span>
      </h3>
      <!-- 주소 표시 -->
      <div v-if="userLocation" class="UserLocationCoordinates">
        <div class="UserLocationCoordinatesText">
          📍 위도: {{ userLocation.lat.toFixed(4) }}, 경도: {{ userLocation.lng.toFixed(4) }}
        </div>
      </div>
      <NaverUserLocationMap :position="userLocation || user.position" :userName="user.name" />
    </div>

    <!-- 운동처방 도우미 (AI 기반 스트레칭/인터벌) -->
    <AppCard class="ExerciseRecommendationCard">
      <div class="ExerciseRecommendationContent">
        <div class="ExerciseRecommendationHeader">
          <h3 class="ExerciseRecommendationTitle">
            운동 추천 도우미
          </h3>
          <AppButton 
            @click="loadStretchRecommendation" 
            :disabled="stretchLoading"
            class="ExerciseRecommendationButton"
          >
            <svg v-if="stretchLoading" class="ExerciseRecommendationLoadingIcon" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            {{ stretchLoading ? '분석 중...' : 'AI 운동 추천' }}
          </AppButton>
        </div>

        <!-- 추천 결과 표시 -->
        <div v-if="stretchRecommendation" class="ExerciseRecommendationContent">
          <!-- 강도 ver 운동 추천 박스 (스트레칭 영상 2개) -->
          <div
            v-if="stretchRecommendation.스트레칭영상?.length > 0"
            class="StretchVideoRecommendationBox"
          >
            <h4 class="StretchVideoRecommendationTitle">
              <template v-if="stretchRecommendation.인터벌운동?.length > 0">
                {{ stretchRecommendation.인터벌운동[0].강도 === 'low' ? '낮음' : stretchRecommendation.인터벌운동[0].강도 === 'medium' ? '중간' : '높음' }} ver 운동 추천
              </template>
              <template v-else>
                운동 추천
              </template>
            </h4>
            <div class="StretchVideoRecommendationList">
              <div 
                v-for="(video, idx) in stretchRecommendation.스트레칭영상.slice(0, 2)" 
                :key="idx"
                class="StretchVideoRecommendationItem"
              >
                {{ video.제목 }}: <a :href="video.영상주소" target="_blank" class="StretchVideoLink">{{ video.영상주소 }}</a>
              </div>
            </div>
          </div>

          <!-- 인터벌 운동 (초록색) -->
          <div v-if="stretchRecommendation.인터벌운동?.length > 0">
            <div class="IntervalWorkoutRecommendationList">
              <div 
                v-for="(interval, idx) in stretchRecommendation.인터벌운동" 
                :key="idx"
                class="IntervalWorkoutRecommendationItem"
              >
                <div class="IntervalWorkoutTitle">{{ interval.루틴명 }}</div>
                <div class="IntervalWorkoutDetails">
                  <div>세트 수: {{ interval.세트수 }}</div>
                  <div>운동 시간: {{ interval.운동시간분 }}분</div>
                  <div>휴식 시간: {{ interval.휴식시간분 }}분</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 주의사항 -->
          <div
            v-if="(stretchRecommendation.주의사항 && stretchRecommendation.주의사항.length > 0) || (stretchRecommendation.인터벌운동?.length > 0 && stretchRecommendation.인터벌운동[0].설명)"
            class="ExerciseCautionBox"
          >
            <h4 class="ExerciseCautionTitle">
              주의사항
              <span v-if="stretchRecommendation.통증부위" class="ExerciseCautionPainArea">
                통증 부위: {{ stretchRecommendation.통증부위 }}
              </span>
            </h4>
            <div class="ExerciseCautionContent">
              <!-- AI가 추천한 주의사항 목록 -->
              <div v-if="stretchRecommendation.주의사항 && stretchRecommendation.주의사항.length > 0">
                <ul class="ExerciseCautionList">
                  <li v-for="(caution, idx) in stretchRecommendation.주의사항" :key="idx">
                    {{ caution }}
                  </li>
                </ul>
              </div>
              <!-- 인터벌 운동 설명 -->
              <div v-if="stretchRecommendation.인터벌운동?.length > 0 && stretchRecommendation.인터벌운동[0].설명">
                {{ stretchRecommendation.인터벌운동[0].설명 }}
              </div>
            </div>
          </div>

          <!-- 실패 메시지 -->
          <div v-if="stretchRecommendation.실패이유" class="ExerciseRecommendationError">
            <div class="ExerciseRecommendationErrorMessage">
              {{ stretchRecommendation.실패이유 }}
            </div>
          </div>

          <!-- 처방에 적용 버튼 -->
          <div v-if="stretchRecommendation.인터벌운동?.length > 0" class="ApplyRecommendationSection">
            <AppButton 
              @click="applyRecommendationToPrescription" 
              variant="solid"
              class="ApplyRecommendationButton"
            >
              <svg class="ApplyRecommendationIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
              이 추천을 운동 처방에 적용하기
            </AppButton>
          </div>
        </div>

        <!-- 초기 상태 -->
        <div v-else-if="!stretchLoading" class="ExerciseRecommendationEmpty">
          AI 버튼을 눌러 사용자의 상태에 맞는 운동을 추천받으세요
        </div>
      </div>
    </AppCard>

    <!-- 대화창과 처방 칸 (2열 레이아웃) -->
    <div class="UserCommunicationGrid">
      <!-- 왼쪽: 대화창 -->
      <AppCard class="GuardianChatCard">
        <div class="GuardianChatContent">
          <h3 class="GuardianChatTitle">
            <svg class="GuardianChatIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
            </svg>
            대화창
            <span class="GuardianChatSubtitle">({{ user.name }}님과의 대화)</span>
          </h3>
          
          <!-- 채팅 메시지 목록 -->
          <div class="GuardianChatMessageList">
            <div v-if="chatMessages.length === 0" class="GuardianChatEmpty">
              아직 대화 내용이 없습니다
            </div>
            <div 
              v-for="(chatMsg, index) in chatMessages" 
              :key="index" 
              :class="['GuardianChatMessageRow', chatMsg.sender === 'guardian' ? 'GuardianChatMessageRowRight' : 'GuardianChatMessageRowLeft']"
            >
              <div 
                :class="[
                  'GuardianChatMessageBubble',
                  chatMsg.sender === 'guardian' 
                    ? 'GuardianChatMessageBubbleGuardian' 
                    : 'GuardianChatMessageBubbleUser'
                ]"
              >
                <div class="GuardianChatMessageSender">
                  {{ chatMsg.senderName }}
                </div>
                <div class="GuardianChatMessageText">
                  {{ chatMsg.message }}
                </div>
                <div class="GuardianChatMessageTime">
                  {{ formatChatTime(chatMsg.timestamp) }}
                </div>
              </div>
            </div>
          </div>

          <!-- 메시지 입력창 -->
          <div class="GuardianChatInputRow">
            <input
              v-model="guardianChatInput"
              @keyup.enter="sendGuardianMessage"
              class="GuardianChatInput"
              placeholder="메시지를 입력하세요..."
            />
            <AppButton
              @click="sendGuardianMessage"
              variant="solid"
              class="GuardianChatSendButton"
            >
              <svg class="GuardianChatSendButtonIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
              </svg>
            </AppButton>
          </div>
        </div>
      </AppCard>

      <!-- 오른쪽: 운동 처방 칸 -->
      <AppCard class="ExercisePrescriptionCard">
        <div class="ExercisePrescriptionContent">
          <h3 class="ExercisePrescriptionTitle">
            <svg class="ExercisePrescriptionIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
            </svg>
            운동 처방
          </h3>

          <form @submit.prevent="sendPrescription" class="ExercisePrescriptionForm">
            <!-- 시작 스트레칭 -->
            <div class="StartStretchingSection">
              <label class="StartStretchingLabel">시작 스트레칭</label>
              <div class="StartStretchingOptions">
                <button
                  type="button"
                  v-for="minutes in [5, 10, 15]"
                  :key="minutes"
                  @click="prescriptionForm.startStretchingMinutes = minutes"
                  :class="[
                    'StartStretchingOptionButton',
                    prescriptionForm.startStretchingMinutes === minutes
                      ? 'StartStretchingOptionButtonActive'
                      : 'StartStretchingOptionButtonInactive'
                  ]"
                >
                  {{ minutes }}분
                </button>
              </div>
              <div class="StartStretchingUrlField">
                <label class="StartStretchingUrlLabel">URL (선택사항)</label>
                <input
                  v-model="prescriptionForm.startStretchingUrl"
                  type="url"
                  class="StartStretchingUrlInput"
                  placeholder="https://example.com/stretching"
                />
              </div>
            </div>

            <!-- 인터벌 운동 -->
            <div class="IntervalExerciseSection">
              <label class="IntervalExerciseLabel">인터벌 운동</label>
              
              <!-- 걷기 -->
              <div class="IntervalWalkingField">
                <label class="IntervalWalkingLabel">걷기 (분)</label>
                <input
                  v-model.number="prescriptionForm.walkingMinutes"
                  type="number"
                  min="1"
                  class="IntervalWalkingInput"
                  placeholder="걷기 시간 입력"
                  required
                />
              </div>

              <!-- 뛰기 -->
              <div class="IntervalRunningField">
                <label class="IntervalRunningLabel">뛰기 (분)</label>
                <input
                  v-model.number="prescriptionForm.runningMinutes"
                  type="number"
                  min="1"
                  class="IntervalRunningInput"
                  placeholder="뛰기 시간 입력"
                  required
                />
              </div>

              <!-- 세트 수 -->
              <div class="IntervalSetsField">
                <label class="IntervalSetsLabel">세트 수</label>
                <input
                  v-model.number="prescriptionForm.sets"
                  type="number"
                  min="1"
                  class="IntervalSetsInput"
                  placeholder="세트 수 입력"
                  required
                />
              </div>
            </div>

            <!-- 마무리 스트레칭 -->
            <div class="EndStretchingSection">
              <label class="EndStretchingLabel">마무리 스트레칭</label>
              <div class="EndStretchingOptions">
                <button
                  type="button"
                  v-for="minutes in [5, 10, 15]"
                  :key="minutes"
                  @click="prescriptionForm.endStretchingMinutes = minutes"
                  :class="[
                    'EndStretchingOptionButton',
                    prescriptionForm.endStretchingMinutes === minutes
                      ? 'EndStretchingOptionButtonActive'
                      : 'EndStretchingOptionButtonInactive'
                  ]"
                >
                  {{ minutes }}분
                </button>
              </div>
              <div class="EndStretchingUrlField">
                <label class="EndStretchingUrlLabel">URL (선택사항)</label>
                <input
                  v-model="prescriptionForm.endStretchingUrl"
                  type="url"
                  class="EndStretchingUrlInput"
                  placeholder="https://example.com/stretching"
                />
              </div>
            </div>

            <!-- 전송 버튼 -->
            <AppButton
              type="submit"
              variant="solid"
              class="SendPrescriptionButton"
              :disabled="prescriptionLoading"
            >
              {{ prescriptionLoading ? '전송 중...' : '처방 전송' }}
            </AppButton>
          </form>
        </div>
      </AppCard>
    </div>

    <!-- 트렌드 차트 -->
    <div class="HealthTrendSection">
      <h3 class="HealthTrendTitle">건강 트렌드</h3>
      <div class="HealthTrendGrid">
        <WeightTrendChart :data="dailyData.weight" />
        <HeartRateTrendChart :data="dailyData.hr" />
        <ActivityTrendChart :data="dailyData.activity" />
        <HeartRateAlertChart :data="weeklyAlertCounts" />
      </div>
    </div>

    <!-- 실시간 바이탈 -->
    <div class="RealtimeMonitoringSection">
      <h3 class="RealtimeMonitoringTitle">실시간 모니터링</h3>
      <UserVitalsNow :vital="vital" />
    </div>

    <!-- 정보 수정 모달 -->
    <AppModal :open="showEditModal" title="사용자 정보 수정" @close="closeEditModal">
      <form @submit.prevent="handleUpdateUser" class="EditUserForm">
        <!-- 보호자 연락처 -->
        <div class="EditGuardianPhoneField">
          <label class="EditGuardianPhoneLabel">
            보호자 연락처
            <span class="EditGuardianPhoneLabelHint">(긴급 연락용)</span>
          </label>
          <input
            v-model="editForm.guardianPhone"
            type="tel"
            placeholder="010-1234-5678"
            pattern="[0-9]{2,3}-[0-9]{3,4}-[0-9]{4}"
            class="EditGuardianPhoneInput"
          />
          <p class="EditGuardianPhoneHint">예: 010-1234-5678</p>
        </div>

        <!-- 몸무게 -->
        <div class="EditWeightField">
          <label class="EditWeightLabel">
            몸무게 (kg)
          </label>
          <input
            v-model.number="editForm.weight"
            type="number"
            placeholder="몸무게"
            class="EditWeightInput"
          />
        </div>

        <div class="EditUserActions">
          <AppButton
            type="button"
            variant="ghost"
            @click="closeEditModal"
            class="EditUserCancelButton"
          >
            취소
          </AppButton>
          <AppButton
            type="submit"
            :disabled="updateLoading"
            class="EditUserSaveButton"
          >
            {{ updateLoading ? '저장 중...' : '저장하기' }}
          </AppButton>
        </div>
      </form>
    </AppModal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed, ref, onBeforeUnmount, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { useUsersStore } from '@/stores/users.store'
import { useMetricsStore } from '@/stores/metrics.store'
import { getUserWeightRecords } from '@/services/api/weightRecords'
import { getExerciseStatus } from '@/services/api/exerciseStatus'
import { getAllLocations } from '@/services/api/locations'
import { updateUser } from '@/services/api/users'
import { getChatMessages, sendChatMessage as sendChatAPI, type ChatMessage } from '@/services/api/chatMessages'
import { createPrescription, type CreateExercisePrescriptionRequest } from '@/services/api/exercisePrescriptions'
import type { WeightRecord } from '@/services/api/weightRecords'
import type { ExerciseStatus } from '@/services/api/exerciseStatus'
import type { LocationDto } from '@/services/api/locations'
import type { StretchRecommendation } from '@/services/api/stretch'
import { getStretchRecommendation } from '@/services/api/stretch'
import AppCard from '@/components/common/AppCard.vue'
import AppButton from '@/components/common/AppButton.vue'
import AppModal from '@/components/common/AppModal.vue'
import NaverUserLocationMap from '@/components/map/NaverUserLocationMap.vue'
import UserVitalsNow from '@/components/user/UserVitalsNow.vue'
import WeightTrendChart from '@/components/charts/WeightTrendChart.vue'
import HeartRateTrendChart from '@/components/charts/HeartRateTrendChart.vue'
import ActivityTrendChart from '@/components/charts/ActivityTrendChart.vue'
import HeartRateAlertChart from '@/components/charts/HeartRateAlertChart.vue'
import defaultFace from '@/assets/images/default-face.png'
import { getPrescriptionsByUser, type ExercisePrescription } from '@/services/api/exercisePrescriptions'
import { getWeeklyAlertCounts, type WeeklyAlertCount } from '@/services/api/heartRateAlerts'

const route = useRoute()
const id = route.params.id as string

const usersStore = useUsersStore()
const metricsStore = useMetricsStore()

const user = computed(() => usersStore.detail)
const loading = computed(() => usersStore.loading)
const error = computed(() => usersStore.error)
const vital = computed(() => metricsStore.vitalNow)

const weightRecords = ref<WeightRecord[]>([])
const weeklyAlertCounts = ref<WeeklyAlertCount[]>([])
const exerciseStatus = ref<ExerciseStatus | null>(null)
const userLocation = ref<{ lat: number; lng: number } | null>(null)
let locationUpdateInterval: number | null = null

// 채팅 관련
const chatMessages = ref<ChatMessage[]>([])
const guardianChatInput = ref('')
let chatUpdateInterval: number | null = null

// 처방 관련
const prescriptionForm = reactive({
  startStretchingMinutes: 5,
  startStretchingUrl: '',
  walkingMinutes: 0,
  runningMinutes: 0,
  sets: 0,
  endStretchingMinutes: 5,
  endStretchingUrl: '',
  intensity: '' as string  // 난이도: 'low', 'medium', 'high'
})
const prescriptionLoading = ref(false)

// 운동처방 도우미 (AI 스트레칭/인터벌)
const stretchRecommendation = ref<StretchRecommendation | null>(null)
const stretchLoading = ref(false)

// 수정 모달
const showEditModal = ref(false)
const updateLoading = ref(false)
const editForm = reactive({
  guardianPhone: '',
  weight: null as number | null,
})

// 완료된 처방 조회
const completedPrescriptions = ref<ExercisePrescription[]>([])

// 완료된 처방 로드
const loadCompletedPrescriptions = async () => {
  try {
    const allPrescriptions = await getPrescriptionsByUser(id)
    completedPrescriptions.value = allPrescriptions.filter(p => 
      p.status === 'COMPLETED' && p.completedAt
    )
    console.log('✅ 완료된 처방 로드:', completedPrescriptions.value.length, '개')
  } catch (error) {
    console.error('❌ 완료된 처방 로드 실패:', error)
    completedPrescriptions.value = []
  }
}

// 난이도 문자열을 숫자로 변환 (low=1, medium=2, high=3)
const intensityToNumber = (intensity?: string): number => {
  if (!intensity) return 0
  switch (intensity.toLowerCase()) {
    case 'low': return 1
    case 'medium': return 2
    case 'high': return 3
    default: return 0
  }
}

const dailyData = computed(() => {
  const daily = metricsStore.daily
  
  // 실제 몸무게 기록 데이터 사용
  const weightData = weightRecords.value.map((record) => ({
    x: record.recordDate,
    y: record.weight
  }))
  
  // 완료된 처방을 날짜별로 매핑 (completedAt 날짜 기준)
  const prescriptionByDate = new Map<string, ExercisePrescription>()
  completedPrescriptions.value.forEach(prescription => {
    if (prescription.completedAt) {
      // completedAt 날짜를 YYYY-MM-DD 형식으로 변환
      const date = new Date(prescription.completedAt).toISOString().split('T')[0]
      prescriptionByDate.set(date, prescription)
    }
  })
  
  // 운동량 데이터 생성 (날짜별로)
  // 완료된 처방이 있는 날은 난이도 점수(1,2,3), 없는 날은 0
  const activityData = daily.map((d) => {
    const prescription = prescriptionByDate.get(d.date)
    
    if (prescription && prescription.intensity) {
      // 처방 완료된 날짜에 난이도 점수 부여
      return { 
        x: d.date, 
        y: intensityToNumber(prescription.intensity)
      }
    }
    
    // 운동 안 한 날 또는 난이도 정보가 없는 날은 0
    return { x: d.date, y: 0 }
  })
  
  return {
    weight: weightData.length > 0 ? weightData : daily.map((d) => ({ x: d.date, y: d.weight || 0 })),
    hr: daily.map((d) => ({ x: d.date, y: d.avgHr || 0 })),
    activity: activityData,
  }
})

onMounted(async () => {
  await usersStore.fetchDetail(id)
  
  // 디버깅: 사용자 데이터 확인
  console.log('=== 사용자 데이터 ===')
  console.log('사용자 정보:', user.value)
  console.log('보호자 연락처:', user.value?.guardianPhone)
  console.log('전체 데이터:', JSON.stringify(user.value, null, 2))
  
  // 디버깅용: 콘솔에서 확인할 수 있도록 전역 함수 등록
  if (typeof window !== 'undefined') {
    (window as any).checkGuardianPhone = () => {
      console.log('=== 보호자 연락처 확인 ===')
      console.log('user:', user.value)
      console.log('보호자 연락처:', user.value?.guardianPhone)
      return user.value?.guardianPhone
    }
    console.log('💡 콘솔에서 "checkGuardianPhone()"을 입력하면 보호자 연락처를 확인할 수 있습니다!')
  }
  
  await metricsStore.fetchDaily(id)
  metricsStore.subscribeRealtime(id)
  
  // 몸무게 기록 로드
  try {
    weightRecords.value = await getUserWeightRecords(id)
  } catch (error) {
    console.error('몸무게 기록 로드 실패:', error)
  }

  // 일주일 경고 횟수 로드
  try {
    weeklyAlertCounts.value = await getWeeklyAlertCounts(id)
  } catch (error) {
    console.error('일주일 경고 횟수 로드 실패:', error)
    weeklyAlertCounts.value = []
  }
  
  // 운동 상태 로드
  const updateExerciseStatus = async () => {
    try {
      const status = await getExerciseStatus(id)
      exerciseStatus.value = status
      console.log('✅ 운동 상태 업데이트:', status)
      console.log('🏃 운동 중 여부:', status?.isExercising)
    } catch (error) {
      console.error('❌ 운동 상태 로드 실패:', error)
      // 에러가 발생해도 null로 설정하여 UI가 깨지지 않도록
      exerciseStatus.value = null
    }
  }
  
  // 초기 로드
  await updateExerciseStatus()
  
  // 주기적으로 운동 상태 업데이트 (5초마다 - 더 빠른 업데이트)
  setInterval(updateExerciseStatus, 5000)
  
  // 실시간 위치 업데이트 (5초마다)
  const updateUserLocation = async () => {
    try {
      const locations = await getAllLocations()
      
      // 사용자 ID 결정: user.value.id가 있으면 우선 사용, 없으면 route param 사용
      const targetUserId = user.value?.id ? String(user.value.id) : String(id)
      
      // 디버깅: 위치 데이터 확인
      console.log('=== 위치 조회 디버깅 ===')
      console.log('Route param ID:', id, '(타입:', typeof id, ')')
      console.log('사용자 정보 ID:', user.value?.id, '(타입:', typeof user.value?.id, ')')
      console.log('최종 사용할 ID:', targetUserId)
      console.log('전체 위치 데이터:', locations)
      console.log('사용자 정보:', user.value)
      
      // userId를 문자열로 변환하여 비교 (타입 일치 보장)
      const location = locations.find((loc: LocationDto) => {
        const locUserId = String(loc.userId)
        const matches = locUserId === targetUserId
        console.log('비교:', locUserId, '===', targetUserId, '→', matches)
        return matches
      })
      
      if (location) {
        console.log('✅ 위치 찾음:', location)
        userLocation.value = {
          lat: location.latitude,
          lng: location.longitude
        }
      } else {
        console.warn('⚠️ 위치를 찾을 수 없음. 사용자 ID:', id, '전체 위치:', locations.map(l => ({ userId: l.userId, userIdType: typeof l.userId })))
        // 위치가 없으면 null로 설정 (기존 user.position 사용)
        userLocation.value = null
      }
    } catch (error) {
      console.error('위치 업데이트 실패:', error)
    }
  }
  
  // 초기 위치 로드
  await updateUserLocation()
  
  // 주기적 위치 업데이트
  locationUpdateInterval = window.setInterval(updateUserLocation, 5000)

  // 채팅 메시지 초기 로드 (에러 발생 시에도 계속 진행)
  try {
    await loadChatMessages()
    // 주기적 채팅 메시지 업데이트
    startChatUpdate()
  } catch (error) {
    console.warn('⚠️ 채팅 메시지 로드 실패 (무시하고 계속):', error)
  }
  
  // 완료된 처방 로드
  await loadCompletedPrescriptions()
})

// 채팅 메시지 로드
const loadChatMessages = async () => {
  try {
    chatMessages.value = await getChatMessages(id)
    console.log('✅ 채팅 메시지 로드 완료:', chatMessages.value.length, '개')
  } catch (error) {
    console.error('❌ 채팅 메시지 로드 실패:', error)
  }
}

// 보호자 메시지 전송
const sendGuardianMessage = async () => {
  if (!guardianChatInput.value.trim()) return

  const messageText = guardianChatInput.value
  guardianChatInput.value = ''

  try {
    const newMessage = await sendChatAPI({
      userId: id,
      sender: 'guardian',
      senderName: '운동 선생님',
      message: messageText
    })

    chatMessages.value.push(newMessage)
    console.log('✅ 보호자 메시지 전송 완료:', newMessage)
  } catch (error) {
    console.error('❌ 메시지 전송 실패:', error)
    alert('메시지 전송에 실패했습니다.')
    guardianChatInput.value = messageText // 실패 시 입력 복원
  }
}

// 시간 포맷팅 (타임스탬프를 HH:MM 형식으로)
const formatChatTime = (timestamp: number) => {
  const date = new Date(timestamp)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

// 처방 전송
const sendPrescription = async () => {
  console.log('🔍 처방 전송 시작:', prescriptionForm)
  
  if (!prescriptionForm.walkingMinutes || !prescriptionForm.runningMinutes || !prescriptionForm.sets) {
    alert('인터벌 운동 정보를 모두 입력해주세요.')
    return
  }

  if (prescriptionForm.walkingMinutes <= 0 || prescriptionForm.runningMinutes <= 0 || prescriptionForm.sets <= 0) {
    alert('인터벌 운동 정보는 1 이상의 값이어야 합니다.')
    return
  }

  prescriptionLoading.value = true

  try {
    const request: CreateExercisePrescriptionRequest = {
      userId: id,
      startStretchingMinutes: prescriptionForm.startStretchingMinutes,
      startStretchingUrl: prescriptionForm.startStretchingUrl?.trim() || undefined,
      walkingMinutes: prescriptionForm.walkingMinutes,
      runningMinutes: prescriptionForm.runningMinutes,
      sets: prescriptionForm.sets,
      endStretchingMinutes: prescriptionForm.endStretchingMinutes,
      endStretchingUrl: prescriptionForm.endStretchingUrl?.trim() || undefined,
      intensity: prescriptionForm.intensity || undefined
    }

    console.log('📤 처방 전송 요청:', request)
    console.log('📤 사용자 ID:', id)
    
    const result = await createPrescription(request)
    console.log('✅ 처방 전송 성공:', result)
    
    alert('처방이 전송되었습니다!')
    
    // 폼 초기화
    prescriptionForm.startStretchingMinutes = 5
    prescriptionForm.startStretchingUrl = ''
    prescriptionForm.walkingMinutes = 0
    prescriptionForm.runningMinutes = 0
    prescriptionForm.sets = 0
    prescriptionForm.endStretchingMinutes = 5
    prescriptionForm.endStretchingUrl = ''
    prescriptionForm.intensity = ''
  } catch (error: any) {
    console.error('❌ 처방 전송 실패:', error)
    console.error('❌ 에러 상세:', {
      message: error?.message,
      response: error?.response,
      status: error?.response?.status,
      data: error?.response?.data
    })
    
    let errorMessage = '처방 전송에 실패했습니다.'
    if (error?.response?.status === 404) {
      errorMessage = '서버를 찾을 수 없습니다. 백엔드 서버가 실행 중인지 확인해주세요.'
    } else if (error?.response?.status === 500) {
      errorMessage = '서버 오류가 발생했습니다. 백엔드 로그를 확인해주세요.'
    } else if (error?.message) {
      errorMessage = `오류: ${error.message}`
    }
    
    alert(errorMessage)
  } finally {
    prescriptionLoading.value = false
  }
}

// AI 스트레칭/인터벌 운동 추천 로드
const loadStretchRecommendation = async () => {
  if (!user.value?.userCode) {
    alert('사용자 코드가 없습니다.')
    return
  }

  console.log('🔍 AI 추천 요청 시작 - userCode:', user.value.userCode)
  stretchLoading.value = true
  stretchRecommendation.value = null

  try {
    const result = await getStretchRecommendation(user.value.userCode)
    stretchRecommendation.value = result
    console.log('✅ AI 운동 추천 완료:', result)
    console.log('📹 스트레칭영상 개수:', result.스트레칭영상?.length || 0)
    console.log('📹 스트레칭영상 상세:', JSON.stringify(result.스트레칭영상, null, 2))
    console.log('🏃 인터벌운동 개수:', result.인터벌운동?.length || 0)
    console.log('🏃 인터벌운동 상세:', JSON.stringify(result.인터벌운동, null, 2))
    console.log('🎯 통증부위:', result.통증부위)
    console.log('⚠️ 주의사항:', result.주의사항)
    
    if (!result.스트레칭영상 || result.스트레칭영상.length === 0) {
      console.warn('⚠️ 스트레칭 영상이 비어있습니다!')
      console.warn('⚠️ 전체 응답 데이터:', JSON.stringify(result, null, 2))
      if (result.실패이유) {
        console.error('❌ 실패 이유:', result.실패이유)
      }
    } else {
      console.log('✅ 스트레칭 영상 정상적으로 받아왔습니다!')
      result.스트레칭영상.forEach((video, idx) => {
        console.log(`  ${idx + 1}. ${video.제목}: ${video.영상주소}`)
      })
    }
  } catch (error: any) {
    console.error('❌ AI 운동 추천 실패:', error)
    console.error('❌ 에러 상세 정보:', {
      message: error?.message,
      response: error?.response,
      status: error?.response?.status,
      statusText: error?.response?.statusText,
      data: error?.response?.data,
      url: error?.config?.url,
      method: error?.config?.method
    })
    
    let errorMessage = 'AI 운동 추천을 불러오는 중 오류가 발생했습니다.'
    
    if (error?.response) {
      // 백엔드에서 응답이 온 경우
      const status = error.response.status
      const data = error.response.data
      
      if (status === 404) {
        errorMessage = '사용자를 찾을 수 없습니다. 사용자 코드를 확인해주세요.'
      } else if (status === 500) {
        errorMessage = `서버 오류가 발생했습니다.\n${data?.message || '잠시 후 다시 시도해주세요.'}`
      } else if (status === 400) {
        errorMessage = `잘못된 요청입니다.\n${data?.message || '요청 파라미터를 확인해주세요.'}`
      } else {
        errorMessage = `오류가 발생했습니다. (상태 코드: ${status})\n${data?.message || ''}`
      }
    } else if (error?.code === 'ECONNABORTED' || error?.message?.includes('timeout')) {
      errorMessage = '요청 시간이 초과되었습니다. 네트워크 연결을 확인해주세요.'
    } else if (error?.message?.includes('Network Error') || error?.code === 'ERR_NETWORK') {
      errorMessage = '네트워크 오류가 발생했습니다.\n백엔드 서버가 실행 중인지 확인해주세요.\n(기본 주소: http://localhost:8081)'
    } else if (error?.message) {
      errorMessage = `오류: ${error.message}`
    }
    
    alert(errorMessage)
  } finally {
    stretchLoading.value = false
  }
}

// AI 추천 결과를 처방 폼에 자동으로 적용
const applyRecommendationToPrescription = () => {
  if (!stretchRecommendation.value) {
    alert('적용할 추천 데이터가 없습니다.')
    return
  }

  const videos = stretchRecommendation.value.스트레칭영상 || []
  const intervals = stretchRecommendation.value.인터벌운동 || []

  // 스트레칭 영상 URL 적용
  if (videos.length > 0) {
    prescriptionForm.startStretchingUrl = videos[0].영상주소 || ''
  }
  if (videos.length > 1) {
    prescriptionForm.endStretchingUrl = videos[1].영상주소 || ''
  }

  // 인터벌 운동 정보 적용
  if (intervals.length > 0) {
    const interval = intervals[0]
    
    // 운동시간분 = 뛰기, 휴식시간분 = 걷기
    prescriptionForm.runningMinutes = interval.운동시간분 || 0
    prescriptionForm.walkingMinutes = interval.휴식시간분 || 0
    prescriptionForm.sets = interval.세트수 || 0
    
    // 난이도 적용 (강도 필드에서 가져오기)
    prescriptionForm.intensity = interval.강도 || ''
  }

  console.log('✅ AI 추천을 처방 폼에 적용했습니다:', prescriptionForm)
  
  // 처방 폼이 있는 곳으로 스크롤
  setTimeout(() => {
    const prescriptionSection = document.querySelector('form')
    if (prescriptionSection) {
      prescriptionSection.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  }, 100)
  
  alert('✅ AI 추천이 운동 처방 폼에 적용되었습니다!')
}

// 주기적으로 채팅 메시지 업데이트 (5초마다)
const startChatUpdate = () => {
  chatUpdateInterval = window.setInterval(loadChatMessages, 5000)
}

onBeforeUnmount(() => {
  if (locationUpdateInterval) {
    clearInterval(locationUpdateInterval)
  }
  if (chatUpdateInterval) {
    clearInterval(chatUpdateInterval)
  }
})

const openEditModal = () => {
  // 현재 사용자 정보로 폼 초기화
  if (user.value) {
    editForm.guardianPhone = user.value.guardianPhone || ''
    editForm.weight = user.value.weight || null
  }
  showEditModal.value = true
}

const closeEditModal = () => {
  showEditModal.value = false
  editForm.guardianPhone = ''
  editForm.weight = null
}

const handleUpdateUser = async () => {
  if (!user.value) return

  updateLoading.value = true

  try {
    const updateData: any = {}
    
    if (editForm.guardianPhone && editForm.guardianPhone.trim()) {
      updateData.guardianPhone = editForm.guardianPhone.trim()
    }
    if (editForm.weight && editForm.weight > 0) {
      updateData.weight = editForm.weight
    }

    if (Object.keys(updateData).length === 0) {
      alert('수정할 내용이 없습니다.')
      updateLoading.value = false
      return
    }

    await updateUser(id, updateData)
    
    // 사용자 정보 새로고침
    await usersStore.fetchDetail(id)
    
    // 디버깅: 수정 후 데이터 확인
    console.log('=== 수정 후 사용자 데이터 ===')
    console.log('사용자 정보:', user.value)
    console.log('보호자 연락처:', user.value?.guardianPhone)
    
    alert('정보가 성공적으로 수정되었습니다.')
    closeEditModal()
  } catch (error) {
    console.error('수정 실패:', error)
    alert('정보 수정에 실패했습니다. 다시 시도해주세요.')
  } finally {
    updateLoading.value = false
  }
}
</script>

