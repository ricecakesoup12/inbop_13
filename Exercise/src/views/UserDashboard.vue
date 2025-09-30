<template>
  <div class="user-dashboard">
    <div class="dashboard-header">
      <h1>🏃‍♂️ 사용자 대시보드</h1>
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
        
        <!-- 운동 시작 버튼 (가운데, 너비의 1/3) -->
        <div class="exercise-section">
          <div class="exercise-card">
            <h3>🏃‍♂️ 운동 시작</h3>
            <div class="exercise-info">
              <div class="exercise-status" :class="exerciseStatus">
                {{ getExerciseStatusText() }}
              </div>
              <div v-if="isExercising" class="exercise-timer">
                {{ formatTime(exerciseTime) }}
              </div>
            </div>
            
            <div class="exercise-controls">
              <button 
                v-if="!isExercising"
                @click="startExercise"
                class="exercise-start-btn"
              >
                🏃‍♂️ 운동 시작하기
              </button>
              
              <div v-else class="exercise-active-controls">
                <button @click="pauseExercise" class="btn btn-warning">
                  ⏸️ 일시정지
                </button>
                <button @click="stopExercise" class="btn btn-danger">
                  ⏹️ 운동 종료
                </button>
              </div>
            </div>
            
            <div v-if="isExercising" class="exercise-progress">
              <h4>오늘의 운동 목표</h4>
              <div class="progress-bar">
                <div 
                  class="progress-fill" 
                  :style="{ width: exerciseProgress + '%' }"
                ></div>
              </div>
              <div class="progress-text">
                {{ exerciseProgress }}% 완료 ({{ completedExercises }}/{{ totalExercises }})
              </div>
            </div>
            
            <div class="quick-actions">
              <h4>빠른 액션</h4>
              <div class="action-buttons">
                <button @click="adjustIntensity('low')" class="action-btn">
                  🐌 낮은 강도
                </button>
                <button @click="adjustIntensity('medium')" class="action-btn">
                  🚶‍♂️ 보통 강도
                </button>
                <button @click="adjustIntensity('high')" class="action-btn">
                  🏃‍♂️ 높은 강도
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 챗봇 (오른쪽, 너비의 1/3) -->
        <div class="chat-section">
          <ChatBot />
        </div>
      </div>
    </div>
    
    <!-- 운동 완료 모달 -->
    <div v-if="showExerciseComplete" class="exercise-complete-modal">
      <div class="modal-content">
        <div class="complete-icon">🎉</div>
        <h3>운동 완료!</h3>
        <div class="exercise-stats">
          <div class="stat-item">
            <span class="stat-label">운동 시간</span>
            <span class="stat-value">{{ formatTime(exerciseTime) }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">칼로리 소모</span>
            <span class="stat-value">{{ caloriesBurned }} kcal</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">평균 심박수</span>
            <span class="stat-value">{{ averageHeartRate }} BPM</span>
          </div>
        </div>
        <div class="modal-actions">
          <button @click="closeExerciseComplete" class="btn btn-primary">
            확인
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Calendar from '../components/Calendar.vue'
import StatusDisplay from '../components/StatusDisplay.vue'
import ChatBot from '../components/ChatBot.vue'

export default {
  name: 'UserDashboard',
  components: {
    Calendar,
    StatusDisplay,
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
      isExercising: false,
      exerciseStatus: 'ready', // ready, exercising, paused
      exerciseTime: 0,
      exerciseInterval: null,
      showExerciseComplete: false,
      averageHeartRate: 0,
      heartRateSum: 0,
      heartRateCount: 0,
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
        }
      ]
    }
  },
  computed: {
    exerciseProgress() {
      const today = new Date().toISOString().split('T')[0]
      const todayExercises = this.exerciseData.filter(ex => ex.date === today)
      const completed = todayExercises.filter(ex => ex.status === 'completed').length
      return todayExercises.length > 0 ? Math.round((completed / todayExercises.length) * 100) : 0
    },
    completedExercises() {
      const today = new Date().toISOString().split('T')[0]
      return this.exerciseData.filter(ex => ex.date === today && ex.status === 'completed').length
    },
    totalExercises() {
      const today = new Date().toISOString().split('T')[0]
      return this.exerciseData.filter(ex => ex.date === today).length
    }
  },
  mounted() {
    this.startStatusMonitoring()
  },
  beforeUnmount() {
    if (this.exerciseInterval) {
      clearInterval(this.exerciseInterval)
    }
  },
  methods: {
    startStatusMonitoring() {
      // 실시간 상태 모니터링
      setInterval(() => {
        this.updateUserStatus()
      }, 3000)
    },
    
    updateUserStatus() {
      // 심박수 시뮬레이션
      const baseHeartRate = this.isExercising ? 90 : 70
      const variation = this.isExercising ? 30 : 10
      this.userStatus.heartRate = Math.floor(Math.random() * variation) + baseHeartRate
      
      // 산소포화도 시뮬레이션
      this.userStatus.oxygenSaturation = Math.floor(Math.random() * 5) + 95
      
      // 운동 강도에 따른 심박수 조절
      if (this.userStatus.heartRate > 100) {
        this.userStatus.exerciseIntensity = '높음'
      } else if (this.userStatus.heartRate > 80) {
        this.userStatus.exerciseIntensity = '보통'
      } else {
        this.userStatus.exerciseIntensity = '낮음'
      }
      
      // 운동 중일 때 시간과 칼로리 업데이트
      if (this.isExercising && this.exerciseStatus === 'exercising') {
        this.exerciseTime += 3
        this.userStatus.exerciseTime = this.exerciseTime
        this.userStatus.caloriesBurned = Math.floor(this.exerciseTime / 60 * 5)
        
        // 평균 심박수 계산
        this.heartRateSum += this.userStatus.heartRate
        this.heartRateCount++
        this.averageHeartRate = Math.round(this.heartRateSum / this.heartRateCount)
      }
    },
    
    startExercise() {
      this.isExercising = true
      this.exerciseStatus = 'exercising'
      this.exerciseTime = 0
      this.heartRateSum = 0
      this.heartRateCount = 0
      
      // 운동 시작 알림
      this.$toast?.success('운동을 시작합니다! 안전하게 운동하세요.')
      
      // 운동 완료 체크 (30분 후)
      setTimeout(() => {
        if (this.isExercising) {
          this.completeExercise()
        }
      }, 1800000) // 30분
    },
    
    pauseExercise() {
      this.exerciseStatus = 'paused'
      this.$toast?.info('운동이 일시정지되었습니다.')
    },
    
    stopExercise() {
      this.completeExercise()
    },
    
    completeExercise() {
      this.isExercising = false
      this.exerciseStatus = 'ready'
      this.showExerciseComplete = true
      
      // 오늘의 운동 완료로 표시
      const today = new Date().toISOString().split('T')[0]
      this.exerciseData.forEach(ex => {
        if (ex.date === today && ex.status === 'in-progress') {
          ex.status = 'completed'
          ex.statusText = '완료'
        }
      })
      
      this.$toast?.success('운동이 완료되었습니다! 수고하셨습니다.')
    },
    
    adjustIntensity(level) {
      const intensityMap = {
        low: '낮음',
        medium: '보통',
        high: '높음'
      }
      
      this.userStatus.exerciseIntensity = intensityMap[level]
      this.$toast?.info(`운동 강도가 ${intensityMap[level]}으로 조절되었습니다.`)
    },
    
    getExerciseStatusText() {
      switch (this.exerciseStatus) {
        case 'ready':
          return '운동 준비 완료'
        case 'exercising':
          return '운동 중'
        case 'paused':
          return '일시정지'
        default:
          return '대기 중'
      }
    },
    
    formatTime(seconds) {
      const hours = Math.floor(seconds / 3600)
      const minutes = Math.floor((seconds % 3600) / 60)
      const secs = seconds % 60
      
      if (hours > 0) {
        return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
      }
      return `${minutes}:${secs.toString().padStart(2, '0')}`
    },
    
    onDaySelected(day) {
      console.log('선택된 날:', day)
    },
    
    refreshData() {
      this.updateUserStatus()
      this.$toast?.success('데이터가 새로고침되었습니다.')
    },
    
    closeExerciseComplete() {
      this.showExerciseComplete = false
    }
  }
}
</script>

<style scoped>
.user-dashboard {
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
  min-height: 60vh;
}

.bottom-section {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 20px;
  height: 400px;
}

.status-section, .exercise-section, .chat-section {
  background: white;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  overflow: hidden;
}

.exercise-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.exercise-card h3 {
  margin: 0 0 20px 0;
  color: #333;
  text-align: center;
  font-size: 1.3rem;
}

.exercise-info {
  text-align: center;
  margin-bottom: 20px;
}

.exercise-status {
  font-size: 1.1rem;
  font-weight: bold;
  padding: 10px;
  border-radius: 8px;
  margin-bottom: 10px;
}

.exercise-status.ready {
  background-color: #e8f5e8;
  color: #4CAF50;
}

.exercise-status.exercising {
  background-color: #fff3e0;
  color: #FF9800;
  animation: pulse 2s infinite;
}

.exercise-status.paused {
  background-color: #e3f2fd;
  color: #2196F3;
}

.exercise-timer {
  font-size: 2rem;
  font-weight: bold;
  color: #333;
  margin-top: 10px;
}

.exercise-controls {
  margin-bottom: 20px;
}

.exercise-start-btn {
  width: 100%;
  background: linear-gradient(45deg, #4CAF50, #45a049);
  color: white;
  padding: 20px;
  font-size: 1.2rem;
  font-weight: bold;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 8px rgba(0,0,0,0.2);
}

.exercise-start-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.3);
}

.exercise-active-controls {
  display: flex;
  gap: 10px;
}

.exercise-active-controls .btn {
  flex: 1;
  padding: 15px;
  font-size: 1rem;
}

.exercise-progress {
  margin-bottom: 20px;
}

.exercise-progress h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 1rem;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background-color: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 5px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(45deg, #4CAF50, #45a049);
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 0.9rem;
  color: #666;
  text-align: center;
}

.quick-actions h4 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 1rem;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-btn {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.action-btn:hover {
  background-color: #f8f9fa;
  border-color: #4CAF50;
  color: #4CAF50;
}

.exercise-complete-modal {
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

.modal-content {
  background: white;
  padding: 40px;
  border-radius: 15px;
  text-align: center;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 10px 30px rgba(0,0,0,0.3);
  animation: slideIn 0.3s ease-out;
}

.complete-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.modal-content h3 {
  margin: 0 0 30px 0;
  color: #4CAF50;
  font-size: 1.8rem;
}

.exercise-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 5px;
}

.stat-value {
  display: block;
  font-size: 1.3rem;
  font-weight: bold;
  color: #333;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
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
  
  .status-section, .exercise-section, .chat-section {
    height: 350px;
  }
  
  .calendar-section {
    min-height: 50vh;
  }
  
  .exercise-stats {
    grid-template-columns: 1fr;
  }
  
  .exercise-active-controls {
    flex-direction: column;
  }
}
</style>
