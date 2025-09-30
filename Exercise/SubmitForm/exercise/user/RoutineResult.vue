<template>
  <ion-content class="ion-padding">

    <!-- 로딩 상태 -->
    <div v-if="loading" class="loading-container">
      <ion-spinner name="crescent"></ion-spinner>
      <p>루틴을 불러오는 중...</p>
    </div>

    <!-- 루틴 결과 -->
    <div v-else-if="routine && routine.length > 0">
      <h2>추천 운동 루틴</h2>

      <!-- 운동 개별 카드 -->
      <div class="routine-cards">
        <ion-card v-for="(item, idx) in routine" :key="idx">
          <ion-card-header>
            <ion-card-title>{{ item.name }}</ion-card-title>
            <ion-card-subtitle>
              {{ item.sets }}세트 × {{ item.reps }}회
            </ion-card-subtitle>
          </ion-card-header>

          <ion-card-content>
            <!-- 운동 아이콘 -->
            <img :src="exerciseIcon" alt="운동 아이콘" class="exercise-img" />

            <!-- Vue 3 슬롯 문법 -->
            <ion-item lines="none">
              <template #start>
                <ion-icon :icon="fitnessOutline" color="primary"></ion-icon>
              </template>
              <ion-label>
                <p>강도: {{ item.intensity }}</p>
              </ion-label>
            </ion-item>
          </ion-card-content>
        </ion-card>
      </div>

      <!-- RPE 설명 카드 -->
      <ion-card>
        <ion-card-header>
          <ion-card-title>운동 강도 기준 (RPE)</ion-card-title>
        </ion-card-header>
        <ion-card-content>
          <p>{{ intensityScale["RPE(0-10)"] }}</p>
        </ion-card-content>
      </ion-card>

      <!-- 다시 설문 버튼 -->
      <div class="button-container">
        <ion-button expand="block" @click="goBackToSurvey" color="primary">
          다시 설문하기
        </ion-button>
      </div>
    </div>

    <!-- 에러 상태 -->
    <div v-else-if="error" class="error-container">
      <ion-card color="danger">
        <ion-card-content>
          <template #start>
            <ion-icon :icon="warningOutline"></ion-icon>
          </template>
          <p>{{ error }}</p>
          <ion-button @click="retryLoad" fill="clear" size="small">
            다시 시도
          </ion-button>
        </ion-card-content>
      </ion-card>
    </div>

    <!-- 빈 상태 -->
    <div v-else class="empty-state">
      <ion-icon :icon="sadOutline" size="large" color="medium"></ion-icon>
      <h2>루틴을 불러올 수 없습니다</h2>
      <p>잠시 후 다시 시도해주세요</p>
      <ion-button @click="goBackToSurvey" fill="outline">
        설문으로 돌아가기
      </ion-button>
    </div>

  </ion-content>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import {
  IonContent,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardSubtitle,
  IonCardContent,
  IonItem,
  IonLabel,
  IonIcon,
  IonButton,
  IonSpinner
} from '@ionic/vue'
import {
  fitnessOutline,
  sadOutline,
  warningOutline
} from 'ionicons/icons'

// 라우터
const route = useRoute()
const router = useRouter()

// 상태값
const routine = ref([])
const intensityScale = ref({})
const loading = ref(true)
const error = ref(null)

// 공통 아이콘 (assets 폴더에 png.png 넣어두기)
const exerciseIcon = new URL('@/assets/png.png', import.meta.url).href

// 루틴 불러오기
const loadRoutine = async () => {
  try {
    loading.value = true
    error.value = null

    const { data } = await axios.get('http://localhost:8000/api/routine', {
      params: { surveyId: route.query.surveyId } // 필요 시 수정
    })

    routine.value = data.exercises
    intensityScale.value = data.intensity_scale
  } catch (err) {
    console.error('루틴 로드 에러:', err)
    error.value = '루틴을 불러오는데 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const retryLoad = () => {
  loadRoutine()
}

const goBackToSurvey = () => {
  router.push('/survey')
}

onMounted(() => {
  loadRoutine()
})
</script>

<style scoped>
.routine-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.exercise-img {
  width: 100%;
  max-width: 200px;
  margin: 10px auto;
  display: block;
  border-radius: 12px;
}

.loading-container,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
}

.loading-container p,
.empty-state p {
  margin-top: 16px;
  color: var(--ion-color-medium);
}

.button-container {
  margin-top: 20px;
}
</style>
