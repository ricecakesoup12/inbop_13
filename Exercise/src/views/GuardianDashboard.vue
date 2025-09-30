<template>
  <div class="guardian-dashboard">
    <div class="dashboard-header">
      <h1>👨‍👩‍👧‍👦 보호자 대시보드</h1>
      <div class="header-actions">
        <button @click="refreshData" class="btn btn-primary">
          🔄 새로고침
        </button>
        <router-link to="/" class="btn btn-secondary">
          🏠 홈으로
        </router-link>
      </div>
    </div>
    
    <div class="dashboard-content">
      <!-- 달력 섹션 (상단, 화면 높이의 3/5) -->
      <div class="calendar-section">
        <Calendar 
          :exercises="exerciseData"
          @day-selected="onDaySelected"
        />
      </div>
      
      <!-- 하단 섹션들 -->
      <div class="bottom-section">
        <!-- 사용자 상태 (왼쪽, 너비의 1/3) -->
        <div class="status-section">
          <StatusDisplay
            :heart-rate="userStatus.heartRate"
            :oxygen-saturation="userStatus.oxygenSaturation"
            :exercise-intensity="userStatus.exerciseIntensity"
            :exercise-time="userStatus.exerciseTime"
            :calories-burned="userStatus.caloriesBurned"
          />
        </div>
        
        <!-- 실시간 위치 (가운데, 너비의 1/3) -->
        <div class="map-section">
          <MapDisplay
            @location-updated="onLocationUpdated"
            @emergency-alert="onEmergencyAlert"
            @check-in="onCheckIn"
          />
        </div>
        
        <!-- 챗봇 (오른쪽, 너비의 1/3) -->
        <div class="chat-section">
          <ChatBot />
        </div>
      </div>
    </div>
    
    <!-- 알림 모달 -->
    <div v-if="showAlert" class="alert-modal">
      <div class="alert-content">
        <div class="alert-icon">⚠️</div>
        <h3>긴급 알림</h3>
        <p>{{ alertMessage }}</p>
        <div class="alert-actions">
          <button @click="dismissAlert" class="btn btn-primary">확인</button>
          <button @click="callEmergency" class="btn btn-danger">응급 연락</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Calendar from '../components/Calendar.vue'
import StatusDisplay from '../components/StatusDisplay.vue'
import MapDisplay from '../components/MapDisplay.vue'
import ChatBot from '../components/ChatBot.vue'

export default {
  name: 'GuardianDashboard',
  components: {
    Calendar,
    StatusDisplay,
    MapDisplay,
    ChatBot
  },
  data() {
    return {
      userStatus: {
        heartRate: 75,
        oxygenSaturation: 98,
        exerciseIntensity: '보통',
        exerciseTime: 0,
        caloriesBurned: 0
      },
      exerciseData: [
        {
          id: 1,
          date: '2024-01-15',
          time: '09:00',
          name: '아침 산책',
          status: 'completed',
          statusText: '완료'
        },
        {
          id: 2,
          date: '2024-01-15',
          time: '14:00',
          name: '가벼운 스트레칭',
          status: 'in-progress',
          statusText: '진행중'
        },
        {
          id: 3,
          date: '2024-01-15',
          time: '19:00',
          name: '저녁 산책',
          status: 'pending',
          statusText: '대기'
        },
        {
          id: 4,
          date: '2024-01-16',
          time: '09:00',
          name: '아침 산책',
          status: 'pending',
          statusText: '대기'
        },
        {
          id: 5,
          date: '2024-01-16',
          time: '15:00',
          name: '재활 운동',
          status: 'pending',
          statusText: '대기'
        }
      ],
      showAlert: false,
      alertMessage: '',
      locationUpdateInterval: null
    }
  },
  mounted() {
    this.startStatusMonitoring()
  },
  beforeUnmount() {
    if (this.locationUpdateInterval) {
      clearInterval(this.locationUpdateInterval)
    }
  },
  methods: {
    startStatusMonitoring() {
      // 실시간 상태 모니터링 시뮬레이션
      this.locationUpdateInterval = setInterval(() => {
        this.updateUserStatus()
      }, 3000) // 3초마다 업데이트
    },
    
    updateUserStatus() {
      // 심박수 시뮬레이션 (60-120 범위)
      this.userStatus.heartRate = Math.floor(Math.random() * 60) + 60
      
      // 산소포화도 시뮬레이션 (90-100 범위)
      this.userStatus.oxygenSaturation = Math.floor(Math.random() * 10) + 90
      
      // 운동 강도 자동 조절
      if (this.userStatus.heartRate > 100) {
        this.userStatus.exerciseIntensity = '높음'
      } else if (this.userStatus.heartRate > 80) {
        this.userStatus.exerciseIntensity = '보통'
      } else {
        this.userStatus.exerciseIntensity = '낮음'
      }
      
      // 운동 시간 증가 (운동 중인 경우)
      if (this.userStatus.exerciseIntensity !== '낮음') {
        this.userStatus.exerciseTime += 3
        this.userStatus.caloriesBurned = Math.floor(this.userStatus.exerciseTime / 60 * 5)
      }
      
      // 위험 상황 감지
      this.checkForAlerts()
    },
    
    checkForAlerts() {
      if (this.userStatus.heartRate > 120 || this.userStatus.oxygenSaturation < 90) {
        this.showEmergencyAlert()
      }
    },
    
    showEmergencyAlert() {
      this.alertMessage = `사용자의 생체 신호가 비정상적입니다. 심박수: ${this.userStatus.heartRate}BPM, 산소포화도: ${this.userStatus.oxygenSaturation}%`
      this.showAlert = true
      
      // 실제 구현에서는 보호자에게 푸시 알림 전송
      console.log('긴급 알림 전송:', this.alertMessage)
    },
    
    onDaySelected(day) {
      console.log('선택된 날:', day)
    },
    
    onLocationUpdated(location) {
      console.log('위치 업데이트:', location)
    },
    
    onEmergencyAlert(alert) {
      this.alertMessage = `긴급 호출이 접수되었습니다. 위치: ${alert.location.address}`
      this.showAlert = true
    },
    
    onCheckIn(checkIn) {
      console.log('안전 확인:', checkIn)
      // 안전 확인 알림 표시
      this.$toast?.success('사용자가 안전 확인을 전송했습니다.')
    },
    
    refreshData() {
      this.updateUserStatus()
      this.$toast?.success('데이터가 새로고침되었습니다.')
    },
    
    dismissAlert() {
      this.showAlert = false
      this.alertMessage = ''
    },
    
    callEmergency() {
      // 실제 구현에서는 응급 연락처로 전화
      alert('응급 연락처로 전화를 겁니다: 119')
      this.dismissAlert()
    }
  }
}
</script>

<style scoped>
.guardian-dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background: white;
  padding: 20px 30px;
  border-radius: 15px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.dashboard-header h1 {
  margin: 0;
  color: #333;
  font-size: 2rem;
}

.header-actions {
  display: flex;
  gap: 15px;
}

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.calendar-section {
  background: white;
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  min-height: 60vh; /* 화면 높이의 3/5 */
}

.bottom-section {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 20px;
  height: 400px;
}

.status-section, .map-section, .chat-section {
  background: white;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  overflow: hidden;
}

.alert-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.alert-content {
  background: white;
  padding: 40px;
  border-radius: 15px;
  text-align: center;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 10px 30px rgba(0,0,0,0.3);
  animation: slideIn 0.3s ease-out;
}

.alert-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.alert-content h3 {
  margin: 0 0 15px 0;
  color: #f44336;
  font-size: 1.5rem;
}

.alert-content p {
  margin: 0 0 30px 0;
  color: #666;
  line-height: 1.6;
}

.alert-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1200px) {
  .bottom-section {
    grid-template-columns: 1fr 1fr;
    height: auto;
  }
  
  .chat-section {
    grid-column: 1 / -1;
    height: 300px;
  }
}

@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .header-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .bottom-section {
    grid-template-columns: 1fr;
    height: auto;
  }
  
  .status-section, .map-section, .chat-section {
    height: 300px;
  }
  
  .calendar-section {
    min-height: 50vh;
  }
  
  .alert-content {
    padding: 20px;
  }
  
  .alert-actions {
    flex-direction: column;
  }
}
</style>
