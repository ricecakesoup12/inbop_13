<template>
  <div class="guardian-profile-container">
      <div class="profile-header">
        <button @click="goBack" class="back-btn">← 뒤로가기</button>
        <h1>환자 상세 정보</h1>
        <button @click="deletePatient" class="delete-patient-btn">환자 삭제</button>
      </div>
    
    <div class="profile-content">
      <!-- 환자 기본 정보 -->
      <div class="card">
        <div class="card-header">
          <h2>기본 정보</h2>
        </div>
        <div class="card-content">
          <div class="patient-basic-info">
            <div class="info-item">
              <label>이름:</label>
              <span>{{ patient.name }}</span>
            </div>
            <div class="info-item">
              <label>나이:</label>
              <span>{{ patient.age }}세</span>
            </div>
            <div class="info-item">
              <label>성별:</label>
              <span>{{ patient.gender }}</span>
            </div>
            <div class="info-item">
              <label>등록일:</label>
              <span>{{ patient.registrationDate }}</span>
            </div>
            <div class="info-item">
              <label>마지막 설문:</label>
              <span>{{ patient.lastSurvey || '없음' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 설문 이력 -->
      <div class="card">
        <div class="card-header">
          <h2>설문 이력</h2>
        </div>
        <div class="card-content">
          <div class="survey-history">
            <div v-if="surveyHistory.length === 0" class="no-history">
              <p>설문 이력이 없습니다.</p>
              <button @click="sendSurveyRequest" class="start-survey-btn">설문 보내기</button>
            </div>
            <div v-else>
              <div v-for="survey in surveyHistory" :key="survey.id" class="survey-item">
                <div class="survey-info">
                  <h3>{{ survey.date }}</h3>
                  <p>상태: <span :class="survey.status">{{ survey.statusText }}</span></p>
                  <p>점수: {{ survey.score || 'N/A' }}</p>
                </div>
                <div class="survey-actions">
                  <button @click="viewSurvey(survey)" class="action-btn view-btn">보기</button>
                  <button @click="downloadSurvey(survey)" class="action-btn download-btn">다운로드</button>
                </div>
              </div>
              <button @click="sendSurveyRequest" class="start-survey-btn">설문 보내기</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 건강 상태 요약 -->
      <div class="card">
        <div class="card-header">
          <h2>건강 상태 요약</h2>
        </div>
        <div class="card-content">
          <div class="health-summary">
            <div class="summary-item">
              <div class="summary-label">전체 건강 점수</div>
              <div class="summary-value">{{ healthSummary.overallScore }}/100</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">운동 빈도</div>
              <div class="summary-value">{{ healthSummary.exerciseFrequency }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">수면 시간</div>
              <div class="summary-value">{{ healthSummary.sleepHours }}시간</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">주요 관심사항</div>
              <div class="summary-value">{{ healthSummary.concerns.join(', ') || '없음' }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 환자 정보 (실제로는 API에서 가져와야 함)
const patient = ref({
  id: 1,
  name: '김할머니',
  age: 75,
  gender: '여성',
  registrationDate: '2024-01-01',
  lastSurvey: '2024-01-15'
})

// 설문 이력
const surveyHistory = ref([
  {
    id: 1,
    date: '2024-01-15',
    status: 'completed',
    statusText: '완료',
    score: 85
  },
  {
    id: 2,
    date: '2024-01-01',
    status: 'completed',
    statusText: '완료',
    score: 78
  }
])

// 건강 상태 요약
const healthSummary = ref({
  overallScore: 85,
  exerciseFrequency: '주 3-4회',
  sleepHours: 7,
  concerns: ['관절 통증', '수면 부족']
})

onMounted(() => {
  // URL에서 환자 ID를 가져와서 해당 환자 정보 로드
  const patientId = route.params.id
  if (patientId) {
    loadPatientData(patientId)
  }
})

const loadPatientData = (patientId) => {
  // 실제로는 API 호출로 환자 데이터를 가져옴
  console.log('Loading patient data for ID:', patientId)
}

const goBack = () => {
  router.push('/guardian/dashboard')
}

const sendSurveyRequest = () => {
  // 사용자에게 설문 요청 이벤트 전송
  const event = new CustomEvent('surveyRequest', {
    detail: {
      type: 'survey_request',
      patientId: patient.value.id,
      patientName: patient.value.name,
      timestamp: new Date().toISOString()
    }
  })
  
  // 전역 이벤트로 전송 (실제로는 서버를 통해 전송해야 함)
  window.dispatchEvent(event)
  
  // 보호자에게 알림
  alert(`${patient.value.name}님에게 설문 요청을 보냈습니다.`)
}

const viewSurvey = (survey) => {
  // 설문 상세 보기
  router.push(`/guardian/survey-result/${survey.id}`)
}

const downloadSurvey = (survey) => {
  // 설문 결과 다운로드
  alert('다운로드 기능은 준비 중입니다.')
}

const deletePatient = () => {
  // 삭제 확인 대화상자
  if (confirm(`${patient.value.name}님을 환자 목록에서 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`)) {
    // 환자 삭제 후 대시보드로 이동
    alert(`${patient.value.name}님이 환자 목록에서 삭제되었습니다.`)
    router.push('/guardian/dashboard')
  }
}
</script>

<style scoped>
.guardian-profile-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Gowun Dodum', sans-serif;
  background-color: #f5f5dc; /* 베이지 배경색 */
  min-height: 100vh;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.back-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.3s;
}

.back-btn:hover {
  background: #5a6268;
}

.delete-patient-btn {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.3s;
}

.delete-patient-btn:hover {
  background: #c0392b;
}

.profile-header h1 {
  color: #2c3e50;
  font-size: 2.5rem;
  margin: 0;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-header {
  background: #90fd97; /* 연두색 헤더 */
  color: white;
  padding: 15px 20px;
}

.card-header h2 {
  margin: 0;
  font-size: 1.3rem;
}

.card-content {
  padding: 20px;
}

.patient-basic-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.info-item label {
  font-weight: bold;
  color: #2c3e50;
  font-size: 0.9rem;
}

.info-item span {
  color: #555;
  font-size: 1rem;
}

.survey-history {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.no-history {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.survey-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: #f9f9f9;
}

.survey-info h3 {
  margin: 0 0 5px 0;
  color: #2c3e50;
  font-size: 1.1rem;
}

.survey-info p {
  margin: 2px 0;
  color: #666;
  font-size: 0.9rem;
}

.survey-info .status {
  font-weight: bold;
}

.survey-info .completed {
  color: #27ae60;
}

.survey-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  font-weight: bold;
  transition: all 0.2s;
}

.view-btn {
  background: #3498db;
  color: white;
}

.view-btn:hover {
  background: #2980b9;
}

.download-btn {
  background: #17a2b8;
  color: white;
}

.download-btn:hover {
  background: #138496;
}

.start-survey-btn {
  background: #90EE90;
  color: #2c3e50;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: bold;
  margin-top: 15px;
  transition: background-color 0.3s;
}

.start-survey-btn:hover {
  background: #7ED321;
}

.health-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.summary-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.summary-label {
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 0.9rem;
}

.summary-value {
  font-size: 1.5rem;
  color: #27ae60;
  font-weight: bold;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .guardian-profile-container {
    padding: 10px;
  }
  
  .profile-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .profile-header h1 {
    font-size: 2rem;
  }
  
  .delete-patient-btn {
    width: 100%;
    margin-top: 10px;
  }
  
  .patient-basic-info {
    grid-template-columns: 1fr;
  }
  
  .survey-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .survey-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .action-btn {
    flex: 1;
    text-align: center;
  }
  
  .health-summary {
    grid-template-columns: 1fr;
  }
}
</style>
