<template>
  <div v-if="loading" class="text-center py-12">
    <p class="text-text-sub font-gowun">불러오는 중...</p>
  </div>

  <div v-else-if="error" class="text-center py-12">
    <p class="text-red-600 font-gowun">{{ error }}</p>
  </div>

  <div v-else-if="user" class="space-y-6">
    <!-- 카드형 사용자 정보 -->
    <AppCard>
      <div class="p-6">
        <div class="flex items-center gap-6 mb-6">
          <img
            :src="user.faceUrl || defaultFace"
            alt="프로필"
            class="w-24 h-24 rounded-full object-cover border-4 border-accent"
          />
          <div class="flex-1 grid grid-cols-2 gap-4">
            <div>
              <span class="text-sm text-text-sub font-gowun">이름</span>
              <p class="text-lg font-semibold text-text-main font-gowun">{{ user.name }}</p>
            </div>
            <div>
              <span class="text-sm text-text-sub font-gowun">성별</span>
              <p class="text-lg font-semibold text-text-main font-gowun">{{ user.gender }}</p>
            </div>
            <div>
              <span class="text-sm text-text-sub font-gowun">나이</span>
              <p class="text-lg font-semibold text-text-main font-gowun">{{ user.age }}세</p>
            </div>
            <div>
              <span class="text-sm text-text-sub font-gowun">키</span>
              <p class="text-lg font-semibold text-text-main font-gowun">{{ user.height }}cm</p>
            </div>
            <div>
              <span class="text-sm text-text-sub font-gowun">몸무게</span>
              <p class="text-lg font-semibold text-text-main font-gowun">{{ user.weight || '-' }}kg</p>
            </div>
            <div>
              <span class="text-sm text-text-sub font-gowun">지병</span>
              <p class="text-lg font-semibold text-text-main font-gowun">
                {{ user.chronicDiseases?.join(', ') || '없음' }}
              </p>
            </div>
            <div v-if="user.guardianPhone">
              <span class="text-sm text-text-sub font-gowun">보호자 연락처</span>
              <div class="flex items-center gap-2 mt-1">
                <p class="text-lg font-semibold text-text-main font-gowun flex items-center gap-1">
                  📞 {{ user.guardianPhone }}
                </p>
                <a :href="`tel:${user.guardianPhone}`">
                  <button class="px-3 py-1 bg-red-500 hover:bg-red-600 text-white text-xs rounded-lg font-gowun transition-colors">
                    긴급 연락
                  </button>
                </a>
              </div>
            </div>
            <div>
              <span class="text-sm text-text-sub font-gowun">운동 상태</span>
              <p class="text-lg font-semibold font-gowun" :class="exerciseStatus?.isExercising ? 'text-green-600' : 'text-gray-400'">
                {{ exerciseStatus?.isExercising ? '🏃 운동 중' : '휴식 중' }}
              </p>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-between items-center gap-3">
          <div class="flex gap-2">
            <AppButton variant="ghost" @click="openEditModal">
              <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              정보 수정
            </AppButton>
            <a v-if="user.guardianPhone" :href="`tel:${user.guardianPhone}`">
              <AppButton variant="outline" class="bg-red-50 hover:bg-red-100 text-red-600 border-red-300">
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                </svg>
                긴급 연락하기
              </AppButton>
            </a>
          </div>
          <div class="flex gap-3">
            <RouterLink :to="`/guardian/users/${id}/survey/send`">
              <AppButton variant="solid">설문 보내기</AppButton>
            </RouterLink>
            <RouterLink :to="`/guardian/users/${id}/survey/result`">
              <AppButton variant="ghost">설문 보기</AppButton>
            </RouterLink>
          </div>
        </div>
      </template>
    </AppCard>

    <!-- 지도 -->
    <div>
      <h3 class="text-lg font-semibold text-text-main mb-3 font-gowun flex items-center gap-2">
        <span>현재 위치</span>
        <span v-if="userLocation" class="text-xs text-green-500 font-normal flex items-center gap-1">
          <span class="w-2 h-2 bg-green-500 rounded-full animate-pulse"></span>
          실시간 추적 중
        </span>
      </h3>
      <!-- 주소 표시 -->
      <div v-if="userLocation" class="mb-3 p-3 bg-blue-50 rounded-lg">
        <div class="text-sm text-gray-700 font-gowun">
          📍 위도: {{ userLocation.lat.toFixed(4) }}, 경도: {{ userLocation.lng.toFixed(4) }}
        </div>
      </div>
      <UserLocationMap :position="userLocation || user.position" />
    </div>

    <!-- 트렌드 차트 -->
    <div>
      <h3 class="text-lg font-semibold text-text-main mb-3 font-gowun">건강 트렌드</h3>
      <div class="grid md:grid-cols-2 gap-4">
        <WeightTrendChart :data="dailyData.weight" />
        <HeartRateTrendChart :data="dailyData.hr" />
        <SpO2TrendChart :data="dailyData.spo2" />
        <ActivityTrendChart :data="dailyData.activity" />
      </div>
    </div>

    <!-- 실시간 바이탈 -->
    <div>
      <h3 class="text-lg font-semibold text-text-main mb-3 font-gowun">실시간 모니터링</h3>
      <UserVitalsNow :vital="vital" />
    </div>

    <!-- 정보 수정 모달 -->
    <AppModal :open="showEditModal" title="사용자 정보 수정" @close="closeEditModal">
      <form @submit.prevent="handleUpdateUser" class="space-y-4">
        <!-- 보호자 연락처 -->
        <div>
          <label class="block font-semibold mb-2 font-gowun">
            보호자 연락처
            <span class="text-xs text-text-sub ml-2">(긴급 연락용)</span>
          </label>
          <input
            v-model="editForm.guardianPhone"
            type="tel"
            placeholder="010-1234-5678"
            pattern="[0-9]{2,3}-[0-9]{3,4}-[0-9]{4}"
            class="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:border-primary outline-none font-gowun"
          />
          <p class="text-xs text-gray-500 mt-1 font-gowun">예: 010-1234-5678</p>
        </div>

        <!-- 몸무게 -->
        <div>
          <label class="block font-semibold mb-2 font-gowun">
            몸무게 (kg)
          </label>
          <input
            v-model.number="editForm.weight"
            type="number"
            placeholder="몸무게"
            class="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:border-primary outline-none font-gowun"
          />
        </div>

        <div class="flex gap-3 pt-4">
          <AppButton type="button" variant="ghost" @click="closeEditModal" class="flex-1">
            취소
          </AppButton>
          <AppButton type="submit" :disabled="updateLoading" class="flex-1">
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
import type { WeightRecord } from '@/services/api/weightRecords'
import type { ExerciseStatus } from '@/services/api/exerciseStatus'
import type { LocationDto } from '@/services/api/locations'
import AppCard from '@/components/common/AppCard.vue'
import AppButton from '@/components/common/AppButton.vue'
import AppModal from '@/components/common/AppModal.vue'
import UserLocationMap from '@/components/map/UserLocationMap.vue'
import UserVitalsNow from '@/components/user/UserVitalsNow.vue'
import WeightTrendChart from '@/components/charts/WeightTrendChart.vue'
import HeartRateTrendChart from '@/components/charts/HeartRateTrendChart.vue'
import SpO2TrendChart from '@/components/charts/SpO2TrendChart.vue'
import ActivityTrendChart from '@/components/charts/ActivityTrendChart.vue'
import defaultFace from '@/assets/images/default-face.png'

const route = useRoute()
const id = route.params.id as string

const usersStore = useUsersStore()
const metricsStore = useMetricsStore()

const user = computed(() => usersStore.detail)
const loading = computed(() => usersStore.loading)
const error = computed(() => usersStore.error)
const vital = computed(() => metricsStore.vitalNow)

const weightRecords = ref<WeightRecord[]>([])
const exerciseStatus = ref<ExerciseStatus | null>(null)
const userLocation = ref<{ lat: number; lng: number } | null>(null)
let locationUpdateInterval: number | null = null

// 수정 모달
const showEditModal = ref(false)
const updateLoading = ref(false)
const editForm = reactive({
  guardianPhone: '',
  weight: null as number | null,
})

const dailyData = computed(() => {
  const daily = metricsStore.daily
  
  // 실제 몸무게 기록 데이터 사용
  const weightData = weightRecords.value.map((record) => ({
    x: record.recordDate,
    y: record.weight
  }))
  
  return {
    weight: weightData.length > 0 ? weightData : daily.map((d) => ({ x: d.date, y: d.weight || 0 })),
    hr: daily.map((d) => ({ x: d.date, y: d.avgHr || 0 })),
    spo2: daily.map((d) => ({ x: d.date, y: d.avgSpO2 || 0 })),
    activity: daily.map((d) => ({ x: d.date, y: d.activity || 0 })),
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
  
  // 운동 상태 로드
  try {
    exerciseStatus.value = await getExerciseStatus(id)
  } catch (error) {
    console.error('운동 상태 로드 실패:', error)
  }
  
  // 주기적으로 운동 상태 업데이트 (10초마다)
  setInterval(async () => {
    try {
      exerciseStatus.value = await getExerciseStatus(id)
    } catch (error) {
      console.error('운동 상태 업데이트 실패:', error)
    }
  }, 10000)
  
  // 실시간 위치 업데이트 (5초마다)
  const updateUserLocation = async () => {
    try {
      const locations = await getAllLocations()
      const location = locations.find((loc: LocationDto) => loc.userId === id)
      if (location) {
        userLocation.value = {
          lat: location.latitude,
          lng: location.longitude
        }
      }
    } catch (error) {
      console.error('위치 업데이트 실패:', error)
    }
  }
  
  // 초기 위치 로드
  await updateUserLocation()
  
  // 주기적 위치 업데이트
  locationUpdateInterval = window.setInterval(updateUserLocation, 5000)
})

onBeforeUnmount(() => {
  if (locationUpdateInterval) {
    clearInterval(locationUpdateInterval)
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

