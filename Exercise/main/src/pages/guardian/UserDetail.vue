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
            <div class="col-span-2">
              <span class="text-sm text-text-sub font-gowun">운동 상태</span>
              <p class="text-lg font-semibold font-gowun" :class="exerciseStatus?.isExercising ? 'text-green-600' : 'text-gray-400'">
                {{ exerciseStatus?.isExercising ? '🏃 운동 중' : '휴식 중' }}
              </p>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <RouterLink :to="`/guardian/users/${id}/survey/send`">
            <AppButton variant="solid">설문 보내기</AppButton>
          </RouterLink>
          <RouterLink :to="`/guardian/users/${id}/survey/result`">
            <AppButton variant="ghost">설문 보기</AppButton>
          </RouterLink>
        </div>
      </template>
    </AppCard>

    <!-- 지도 -->
    <div>
      <h3 class="text-lg font-semibold text-text-main mb-3 font-gowun">현재 위치</h3>
      <UserLocationMap :position="user.position" />
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
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useUsersStore } from '@/stores/users.store'
import { useMetricsStore } from '@/stores/metrics.store'
import { getUserWeightRecords } from '@/services/api/weightRecords'
import { getExerciseStatus } from '@/services/api/exerciseStatus'
import type { WeightRecord } from '@/services/api/weightRecords'
import type { ExerciseStatus } from '@/services/api/exerciseStatus'
import AppCard from '@/components/common/AppCard.vue'
import AppButton from '@/components/common/AppButton.vue'
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
})
</script>

