<template>
  <div class="guardian-dashboard-container">
    <div class="dashboard-header">
      <h1>보호자 대시보드</h1>
      <p>보호 대상자들의 건강 상태를 관리하세요</p>
    </div>
    
    <div class="dashboard-content">
      <!-- 환자 목록 카드 -->
      <div class="card">
        <div class="card-header">
          <h2>보호 대상자 목록</h2>
        </div>
        <div class="card-content">
          <div class="patient-list">
            <div v-if="patients.length === 0" class="no-patients">
              <p>등록된 보호 대상자가 없습니다.</p>
              <button @click="addPatient" class="add-patient-btn">환자 추가</button>
            </div>
            <div v-else>
              <div v-for="patient in patients" :key="patient.id" class="patient-item">
                <div class="patient-info">
                  <h3>{{ patient.name }}</h3>
                  <p>{{ patient.age }}세, {{ patient.gender }}</p>
                  <p class="last-survey">마지막 설문: {{ patient.lastSurvey || '없음' }}</p>
                </div>
                <div class="patient-actions">
                  <button @click="viewPatient(patient)" class="action-btn view-btn">상세보기</button>
                  <button @click="sendSurveyRequest(patient)" class="action-btn survey-btn">설문 보내기</button>
                  <button @click="deletePatient(patient)" class="action-btn delete-btn">삭제</button>
                </div>
              </div>
              <button @click="addPatient" class="add-patient-btn">환자 추가</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 최근 활동 카드 -->
      <div class="card">
        <div class="card-header">
          <h2>최근 활동</h2>
        </div>
        <div class="card-content">
          <div class="activity-list">
            <div v-if="recentActivities.length === 0" class="no-activity">
              <p>최근 활동이 없습니다.</p>
            </div>
            <div v-else>
              <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
                <div class="activity-icon">📋</div>
                <div class="activity-content">
                  <p class="activity-text">{{ activity.text }}</p>
                  <p class="activity-time">{{ activity.time }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 환자 목록 데이터
const patients = ref([
  // 샘플 데이터
  {
    id: 1,
    name: '김할머니',
    age: 75,
    gender: '여성',
    lastSurvey: '2024-01-15'
  }
])

// 최근 활동 데이터
const recentActivities = ref([
  {
    id: 1,
    text: '김할머니의 건강 설문이 완료되었습니다.',
    time: '2시간 전'
  }
])

const addPatient = () => {
  // 환자 추가 모달 또는 페이지로 이동
  alert('환자 추가 기능은 준비 중입니다.')
}

const viewPatient = (patient) => {
  // 환자 상세 정보 페이지로 이동
  router.push(`/guardian/profile/${patient.id}`)
}

const sendSurveyRequest = (patient) => {
  // 사용자에게 설문 요청 이벤트 전송
  const event = new CustomEvent('surveyRequest', {
    detail: {
      type: 'survey_request',
      patientId: patient.id,
      patientName: patient.name,
      timestamp: new Date().toISOString()
    }
  })
  
  // 전역 이벤트로 전송 (실제로는 서버를 통해 전송해야 함)
  window.dispatchEvent(event)
  
  // 보호자에게 알림
  alert(`${patient.name}님에게 설문 요청을 보냈습니다.`)
  
  // 최근 활동에 추가
  addRecentActivity(`${patient.name}님에게 설문 요청을 보냈습니다.`)
}

const deletePatient = (patient) => {
  // 삭제 확인 대화상자
  if (confirm(`${patient.name}님을 환자 목록에서 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`)) {
    // 환자 목록에서 제거
    const index = patients.value.findIndex(p => p.id === patient.id)
    if (index > -1) {
      patients.value.splice(index, 1)
      
      // 최근 활동에 삭제 기록 추가
      addRecentActivity(`${patient.name}님을 환자 목록에서 삭제했습니다.`)
      
      alert(`${patient.name}님이 환자 목록에서 삭제되었습니다.`)
    }
  }
}

const addRecentActivity = (text) => {
  const newActivity = {
    id: Date.now(),
    text: text,
    time: '방금 전'
  }
  recentActivities.value.unshift(newActivity)
  
  // 최대 10개까지만 유지
  if (recentActivities.value.length > 10) {
    recentActivities.value = recentActivities.value.slice(0, 10)
  }
}
</script>

<style scoped>
.guardian-dashboard-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Gowun Dodum', sans-serif;
  background-color: #f5f5dc; /* 베이지 배경색 */
  min-height: 100vh;
}

.dashboard-header {
  text-align: center;
  margin-bottom: 30px;
}

.dashboard-header h1 {
  color: #2c3e50;
  font-size: 2.5rem;
  margin-bottom: 10px;
}

.dashboard-header p {
  color: #555;
  font-size: 1.1rem;
  margin-bottom: 0;
}

.dashboard-content {
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

.patient-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.no-patients, .no-activity {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.patient-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: #f9f9f9;
}

.patient-info h3 {
  margin: 0 0 5px 0;
  color: #2c3e50;
  font-size: 1.2rem;
}

.patient-info p {
  margin: 2px 0;
  color: #666;
  font-size: 0.9rem;
}

.last-survey {
  font-style: italic;
  color: #888;
}

.patient-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
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

.survey-btn {
  background: #90EE90;
  color: #2c3e50;
}

.survey-btn:hover {
  background: #7ED321;
}

.delete-btn {
  background: #e74c3c;
  color: white;
}

.delete-btn:hover {
  background: #c0392b;
}

.add-patient-btn {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: bold;
  margin-top: 15px;
  transition: background-color 0.3s;
}

.add-patient-btn:hover {
  background: #c0392b;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px;
  border-radius: 6px;
  background: #f8f9fa;
}

.activity-icon {
  font-size: 1.5rem;
}

.activity-content {
  flex: 1;
}

.activity-text {
  margin: 0 0 5px 0;
  color: #2c3e50;
  font-size: 0.9rem;
}

.activity-time {
  margin: 0;
  color: #888;
  font-size: 0.8rem;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .guardian-dashboard-container {
    padding: 10px;
  }
  
  .dashboard-header h1 {
    font-size: 2rem;
  }
  
  .patient-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .patient-actions {
    width: 100%;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .action-btn {
    flex: 1;
    text-align: center;
    min-width: 80px;
  }
}
</style>
