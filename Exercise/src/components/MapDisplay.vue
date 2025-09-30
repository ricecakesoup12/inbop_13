<template>
  <div class="map-container">
    <div class="map-header">
      <h3>📍 실시간 위치</h3>
      <div class="location-status" :class="{ online: isOnline }">
        <span class="status-dot"></span>
        {{ isOnline ? '연결됨' : '연결 끊김' }}
      </div>
    </div>
    
    <div class="map-content">
      <div v-if="isOnline" class="map-placeholder">
        <div class="map-info">
          <div class="current-location">
            <h4>현재 위치</h4>
            <p>{{ currentLocation.address }}</p>
            <div class="coordinates">
              위도: {{ currentLocation.lat.toFixed(6) }}<br>
              경도: {{ currentLocation.lng.toFixed(6) }}
            </div>
          </div>
          
          <div class="exercise-route">
            <h4>운동 경로</h4>
            <div class="route-stats">
              <div class="stat-item">
                <span class="stat-label">총 거리</span>
                <span class="stat-value">{{ totalDistance }}km</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">소요 시간</span>
                <span class="stat-value">{{ formatTime(exerciseTime) }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">평균 속도</span>
                <span class="stat-value">{{ averageSpeed }}km/h</span>
              </div>
            </div>
          </div>
          
          <div class="safety-features">
            <h4>안전 기능</h4>
            <div class="safety-buttons">
              <button class="safety-btn emergency" @click="sendEmergencyAlert">
                🆘 긴급 호출
              </button>
              <button class="safety-btn check-in" @click="sendCheckIn">
                ✅ 안전 확인
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="map-placeholder offline">
        <div class="offline-message">
          <div class="offline-icon">📡</div>
          <h4>위치 서비스 연결 끊김</h4>
          <p>GPS 신호를 확인하고 다시 시도해주세요.</p>
          <button @click="reconnectLocation" class="reconnect-btn">
            다시 연결
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MapDisplay',
  data() {
    return {
      isOnline: true,
      currentLocation: {
        lat: 37.5665,
        lng: 126.9780,
        address: '서울특별시 중구 세종대로 110'
      },
      totalDistance: 2.3,
      exerciseTime: 1800, // 30분
      averageSpeed: 4.6,
      locationUpdateInterval: null
    }
  },
  mounted() {
    this.startLocationTracking()
  },
  beforeUnmount() {
    if (this.locationUpdateInterval) {
      clearInterval(this.locationUpdateInterval)
    }
  },
  methods: {
    startLocationTracking() {
      // 실제 구현에서는 GPS API를 사용
      this.locationUpdateInterval = setInterval(() => {
        this.updateLocation()
      }, 5000) // 5초마다 위치 업데이트
    },
    
    updateLocation() {
      // 시뮬레이션된 위치 업데이트
      const randomLat = 0.001
      const randomLng = 0.001
      
      this.currentLocation.lat += (Math.random() - 0.5) * randomLat
      this.currentLocation.lng += (Math.random() - 0.5) * randomLng
      
      // 거리 계산 (간단한 시뮬레이션)
      this.totalDistance += 0.01
      this.exerciseTime += 5
      this.averageSpeed = this.totalDistance / (this.exerciseTime / 3600)
      
      this.$emit('location-updated', this.currentLocation)
    },
    
    sendEmergencyAlert() {
      this.$emit('emergency-alert', {
        type: 'emergency',
        location: this.currentLocation,
        timestamp: new Date()
      })
      
      alert('긴급 호출이 전송되었습니다! 보호자에게 알림이 전송됩니다.')
    },
    
    sendCheckIn() {
      this.$emit('check-in', {
        type: 'check-in',
        location: this.currentLocation,
        timestamp: new Date()
      })
      
      alert('안전 확인 메시지가 전송되었습니다.')
    },
    
    reconnectLocation() {
      this.isOnline = false
      
      // 연결 시뮬레이션
      setTimeout(() => {
        this.isOnline = true
        this.startLocationTracking()
        alert('위치 서비스가 다시 연결되었습니다.')
      }, 2000)
    },
    
    formatTime(seconds) {
      const hours = Math.floor(seconds / 3600)
      const minutes = Math.floor((seconds % 3600) / 60)
      const secs = seconds % 60
      
      if (hours > 0) {
        return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
      }
      return `${minutes}:${secs.toString().padStart(2, '0')}`
    }
  }
}
</script>

<style scoped>
.map-container {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  height: 400px;
  display: flex;
  flex-direction: column;
}

.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.map-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.2rem;
}

.location-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  color: #666;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #ccc;
  transition: background-color 0.3s ease;
}

.location-status.online .status-dot {
  background-color: #4CAF50;
  animation: pulse 2s infinite;
}

.map-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.map-placeholder {
  flex: 1;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.map-info {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.current-location, .exercise-route, .safety-features {
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.current-location h4, .exercise-route h4, .safety-features h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 1rem;
}

.current-location p {
  margin: 5px 0;
  color: #666;
  font-size: 0.9rem;
}

.coordinates {
  font-size: 0.8rem;
  color: #999;
  margin-top: 10px;
}

.route-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.stat-item {
  text-align: center;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.stat-label {
  display: block;
  font-size: 0.8rem;
  color: #666;
  margin-bottom: 5px;
}

.stat-value {
  display: block;
  font-size: 1.1rem;
  font-weight: bold;
  color: #333;
}

.safety-buttons {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.safety-btn {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: bold;
  transition: all 0.3s ease;
}

.safety-btn.emergency {
  background-color: #f44336;
  color: white;
}

.safety-btn.emergency:hover {
  background-color: #d32f2f;
  transform: translateY(-1px);
}

.safety-btn.check-in {
  background-color: #4CAF50;
  color: white;
}

.safety-btn.check-in:hover {
  background-color: #45a049;
  transform: translateY(-1px);
}

.offline-message {
  text-align: center;
  color: #666;
}

.offline-icon {
  font-size: 3rem;
  margin-bottom: 15px;
}

.offline-message h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.offline-message p {
  margin: 0 0 20px 0;
  font-size: 0.9rem;
}

.reconnect-btn {
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.3s ease;
}

.reconnect-btn:hover {
  background-color: #0056b3;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.7;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .map-container {
    height: 350px;
  }
  
  .route-stats {
    grid-template-columns: 1fr;
  }
  
  .safety-buttons {
    flex-direction: column;
  }
  
  .map-info {
    gap: 15px;
  }
  
  .current-location, .exercise-route, .safety-features {
    padding: 12px;
  }
}
</style>
