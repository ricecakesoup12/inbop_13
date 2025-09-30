<template>
  <div class="exercise-start">
    <div class="exercise-header">
      <div class="header-left">
        <button @click="goBack" class="back-btn">
          ← 뒤로가기
        </button>
        <h1>🏃‍♂️ 운동 시작</h1>
      </div>
      <div class="header-right">
        <div class="exercise-timer" v-if="isExercising">
          {{ formatTime(exerciseTime) }}
        </div>
        <div class="exercise-status" :class="exerciseStatus">
          {{ getStatusText() }}
        </div>
      </div>
    </div>
    
    <div class="exercise-content">
      <!-- 실시간 센서 데이터 -->
      <div class="sensor-panel">
        <div class="sensor-item">
          <div class="sensor-icon">💓</div>
          <div class="sensor-data">
            <div class="sensor-value" :class="getHeartRateStatus()">
              {{ heartRate }} BPM
            </div>
            <div class="sensor-label">심박수</div>
          </div>
        </div>
        
        <div class="sensor-item">
          <div class="sensor-icon">🫁</div>
          <div class="sensor-data">
            <div class="sensor-value" :class="getOxygenStatus()">
              {{ oxygenSaturation }}%
            </div>
            <div class="sensor-label">산소포화도</div>
          </div>
        </div>
        
        <div class="sensor-item">
          <div class="sensor-icon">⚡</div>
          <div class="sensor-data">
            <div class="sensor-value">
              {{ exerciseIntensity }}
            </div>
            <div class="sensor-label">운동 강도</div>
          </div>
        </div>
      </div>
      
      <!-- 네비게이션 맵 -->
      <div class="navigation-panel">
        <div class="map-container">
          <div class="map-header">
            <h3>🗺️ 산책 경로 안내</h3>
            <div class="route-info">
              <span class="route-distance">{{ totalDistance }}km</span>
              <span class="route-time">{{ formatTime(estimatedTime) }}</span>
            </div>
          </div>
          
          <div class="map-content">
            <div class="map-placeholder">
              <div class="current-location">
                <div class="location-icon">📍</div>
                <div class="location-info">
                  <h4>현재 위치</h4>
                  <p>{{ currentLocation.address }}</p>
                </div>
              </div>
              
              <div class="route-path">
                <div class="path-point" v-for="(point, index) in routePoints" :key="index">
                  <div class="point-number">{{ index + 1 }}</div>
                  <div class="point-info">
                    <div class="point-name">{{ point.name }}</div>
                    <div class="point-distance">{{ point.distance }}m</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 운동 컨트롤 -->
      <div class="control-panel">
        <div class="exercise-controls">
          <button 
            v-if="!isExercising"
            @click="startExercise"
            class="start-btn"
          >
            🏃‍♂️ 운동 시작
          </button>
          
          <div v-else class="active-controls">
            <button @click="pauseExercise" class="control-btn pause">
              ⏸️ 일시정지
            </button>
            <button @click="stopExercise" class="control-btn stop">
              ⏹️ 종료
            </button>
          </div>
        </div>
        
        <div class="intensity-controls">
          <h4>운동 강도 조절</h4>
          <div class="intensity-buttons">
            <button 
              @click="setIntensity('low')"
              :class="['intensity-btn', { active: exerciseIntensity === '낮음' }]"
            >
              🐌 낮음
            </button>
            <button 
              @click="setIntensity('medium')"
              :class="['intensity-btn', { active: exerciseIntensity === '보통' }]"
            >
              🚶‍♂️ 보통
            </button>
            <button 
              @click="setIntensity('high')"
              :class="['intensity-btn', { active: exerciseIntensity === '높음' }]"
            >
              🏃‍♂️ 높음
            </button>
          </div>
        </div>
      </div>
      
      <!-- 긴급 도움 버튼 -->
      <button class="emergency-btn" @click="sendEmergencyAlert">
        🆘 긴급 도움
      </button>
    </div>
    
    <!-- 운동 완료 모달 -->
    <div v-if="showCompleteModal" class="complete-modal">
      <div class="modal-content">
        <div class="complete-icon">🎉</div>
        <h3>운동 완료!</h3>
        <div class="exercise-summary">
          <div class="summary-item">
            <span class="summary-label">운동 시간</span>
            <span class="summary-value">{{ formatTime(exerciseTime) }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">칼로리 소모</span>
            <span class="summary-value">{{ caloriesBurned }} kcal</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">평균 심박수</span>
            <span class="summary-value">{{ averageHeartRate }} BPM</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">총 거리</span>
            <span class="summary-value">{{ totalDistance }} km</span>
          </div>
        </div>
        <div class="modal-actions">
          <button @click="goToDashboard" class="btn btn-primary">
            대시보드로 돌아가기
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ExerciseStart',
  data() {
    return {
      isExercising: false,
      exerciseStatus: 'ready', // ready, exercising, paused
      exerciseTime: 0,
      exerciseInterval: null,
      heartRate: 75,
      oxygenSaturation: 98,
      exerciseIntensity: '보통',
      totalDistance: 0,
      estimatedTime: 0,
      caloriesBurned: 0,
      averageHeartRate: 0,
      heartRateSum: 0,
      heartRateCount: 0,
      showCompleteModal: false,
      currentLocation: {
        address: '서울특별시 중구 세종대로 110'
      },
      routePoints: [
        { name: '출발점', distance: 0 },
        { name: '공원 입구', distance: 200 },
        { name: '운동 기구 구역', distance: 500 },
        { name: '휴게 공간', distance: 800 },
        { name: '도착점', distance: 1000 }
      ]
    }
  },
  mounted() {
    this.startSensorMonitoring()
  },
  beforeUnmount() {
    if (this.exerciseInterval) {
      clearInterval(this.exerciseInterval)
    }
  },
  methods: {
    startSensorMonitoring() {
      // 센서 데이터 시뮬레이션
      setInterval(() => {
        this.updateSensorData()
      }, 2000)
    },
    
    updateSensorData() {
      // 심박수 시뮬레이션
      const baseHeartRate = this.isExercising ? 90 : 70
      const variation = this.isExercising ? 30 : 10
      this.heartRate = Math.floor(Math.random() * variation) + baseHeartRate
      
      // 산소포화도 시뮬레이션
      this.oxygenSaturation = Math.floor(Math.random() * 5) + 95
      
      // 운동 중일 때 거리 업데이트
      if (this.isExercising && this.exerciseStatus === 'exercising') {
        this.totalDistance += 0.01
        this.estimatedTime = Math.floor(this.totalDistance * 15) // 15분/km 가정
        this.caloriesBurned = Math.floor(this.exerciseTime / 60 * 5)
        
        // 평균 심박수 계산
        this.heartRateSum += this.heartRate
        this.heartRateCount++
        this.averageHeartRate = Math.round(this.heartRateSum / this.heartRateCount)
      }
    },
    
    startExercise() {
      this.isExercising = true
      this.exerciseStatus = 'exercising'
      this.exerciseTime = 0
      this.totalDistance = 0
      this.heartRateSum = 0
      this.heartRateCount = 0
      
      // 운동 완료 체크 (30분 후)
      setTimeout(() => {
        if (this.isExercising) {
          this.completeExercise()
        }
      }, 1800000) // 30분
      
      this.$toast?.success('운동을 시작합니다!')
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
      this.showCompleteModal = true
      this.$toast?.success('운동이 완료되었습니다!')
    },
    
    setIntensity(level) {
      const intensityMap = {
        low: '낮음',
        medium: '보통',
        high: '높음'
      }
      
      this.exerciseIntensity = intensityMap[level]
      this.$toast?.info(`운동 강도가 ${intensityMap[level]}으로 설정되었습니다.`)
    },
    
    getStatusText() {
      switch (this.exerciseStatus) {
        case 'ready':
          return '준비 완료'
        case 'exercising':
          return '운동 중'
        case 'paused':
          return '일시정지'
        default:
          return '대기 중'
      }
    },
    
    getHeartRateStatus() {
      if (this.heartRate > 120 || this.heartRate < 50) {
        return 'danger'
      } else if (this.heartRate > 100 || this.heartRate < 60) {
        return 'warning'
      }
      return 'normal'
    },
    
    getOxygenStatus() {
      if (this.oxygenSaturation < 90) {
        return 'danger'
      } else if (this.oxygenSaturation < 95) {
        return 'warning'
      }
      return 'normal'
    },
    
    sendEmergencyAlert() {
      this.$toast?.error('긴급 도움 요청이 전송되었습니다!')
      // 실제 구현에서는 보호자에게 알림 전송
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
    
    goBack() {
      this.$router.go(-1)
    },
    
    goToDashboard() {
      this.$router.push('/user')
    }
  }
}
</script>

<style scoped>
.exercise-start {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.exercise-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background: white;
  padding: 20px 30px;
  border-radius: 15px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  background: #f8f9fa;
  border: none;
  padding: 10px 15px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: #e9ecef;
  transform: translateX(-2px);
}

.exercise-header h1 {
  margin: 0;
  color: #333;
  font-size: 2rem;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.exercise-timer {
  font-size: 1.5rem;
  font-weight: bold;
  color: #4CAF50;
  background: #e8f5e8;
  padding: 10px 20px;
  border-radius: 8px;
}

.exercise-status {
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: bold;
}

.exercise-status.ready {
  background: #e8f5e8;
  color: #4CAF50;
}

.exercise-status.exercising {
  background: #fff3e0;
  color: #FF9800;
  animation: pulse 2s infinite;
}

.exercise-status.paused {
  background: #e3f2fd;
  color: #2196F3;
}

.exercise-content {
  display: grid;
  grid-template-columns: 1fr 2fr 1fr;
  gap: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.sensor-panel, .control-panel {
  background: white;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  height: fit-content;
}

.sensor-panel h3, .control-panel h3 {
  margin: 0 0 20px 0;
  color: #333;
  text-align: center;
}

.sensor-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  margin-bottom: 15px;
  background: #f8f9fa;
  border-radius: 10px;
}

.sensor-icon {
  font-size: 2rem;
}

.sensor-data {
  flex: 1;
}

.sensor-value {
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 5px;
}

.sensor-value.normal {
  color: #4CAF50;
}

.sensor-value.warning {
  color: #FF9800;
}

.sensor-value.danger {
  color: #f44336;
  animation: pulse 1s infinite;
}

.sensor-label {
  font-size: 0.9rem;
  color: #666;
}

.navigation-panel {
  background: white;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.map-container {
  height: 100%;
}

.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.map-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.3rem;
}

.route-info {
  display: flex;
  gap: 15px;
  font-size: 0.9rem;
  color: #666;
}

.map-content {
  height: 400px;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 10px;
  padding: 20px;
  overflow-y: auto;
}

.current-location {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
  padding: 15px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.location-icon {
  font-size: 2rem;
}

.location-info h4 {
  margin: 0 0 5px 0;
  color: #333;
}

.location-info p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}

.route-path {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.path-point {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
}

.path-point:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.point-number {
  width: 30px;
  height: 30px;
  background: #4CAF50;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.9rem;
}

.point-info {
  flex: 1;
}

.point-name {
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.point-distance {
  font-size: 0.9rem;
  color: #666;
}

.exercise-controls {
  margin-bottom: 30px;
}

.start-btn {
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

.start-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.3);
}

.active-controls {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.control-btn {
  padding: 15px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: bold;
  transition: all 0.3s ease;
}

.control-btn.pause {
  background: #FF9800;
  color: white;
}

.control-btn.stop {
  background: #f44336;
  color: white;
}

.control-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.2);
}

.intensity-controls h4 {
  margin: 0 0 15px 0;
  color: #333;
  text-align: center;
}

.intensity-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.intensity-btn {
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.intensity-btn:hover {
  background: #f8f9fa;
  border-color: #4CAF50;
  color: #4CAF50;
}

.intensity-btn.active {
  background: #4CAF50;
  color: white;
  border-color: #4CAF50;
}

.emergency-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 80px;
  height: 80px;
  background: linear-gradient(45deg, #f44336, #d32f2f);
  color: white;
  border: none;
  border-radius: 50%;
  font-size: 1.5rem;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.4);
  transition: all 0.3s ease;
  animation: pulse 2s infinite;
}

.emergency-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(244, 67, 54, 0.6);
}

.complete-modal {
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

.exercise-summary {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.summary-item {
  text-align: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.summary-label {
  display: block;
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 5px;
}

.summary-value {
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
  .exercise-content {
    grid-template-columns: 1fr 1fr;
  }
  
  .navigation-panel {
    grid-column: 1 / -1;
  }
}

@media (max-width: 768px) {
  .exercise-header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .header-left, .header-right {
    flex-direction: column;
    gap: 10px;
  }
  
  .exercise-content {
    grid-template-columns: 1fr;
  }
  
  .map-content {
    height: 300px;
  }
  
  .exercise-summary {
    grid-template-columns: 1fr;
  }
  
  .emergency-btn {
    bottom: 20px;
    right: 20px;
    width: 60px;
    height: 60px;
    font-size: 1.2rem;
  }
}
</style>
