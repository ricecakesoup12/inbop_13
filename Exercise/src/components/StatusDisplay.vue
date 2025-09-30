<template>
  <div class="status-display">
    <h3>실시간 생체 신호</h3>
    
    <div class="status-item">
      <div class="status-label">
        <span class="status-icon">💓</span>
        <span>심박수</span>
      </div>
      <div :class="['status-value', getHeartRateStatus()]">
        {{ heartRate }} BPM
      </div>
    </div>
    
    <div class="status-item">
      <div class="status-label">
        <span class="status-icon">🫁</span>
        <span>산소포화도</span>
      </div>
      <div :class="['status-value', getOxygenStatus()]">
        {{ oxygenSaturation }}%
      </div>
    </div>
    
    <div class="status-item">
      <div class="status-label">
        <span class="status-icon">🏃‍♂️</span>
        <span>운동 강도</span>
      </div>
      <div :class="['status-value', getIntensityStatus()]">
        {{ exerciseIntensity }}
      </div>
    </div>
    
    <div class="status-item">
      <div class="status-label">
        <span class="status-icon">⏱️</span>
        <span>운동 시간</span>
      </div>
      <div class="status-value">
        {{ formatTime(exerciseTime) }}
      </div>
    </div>
    
    <div class="status-item">
      <div class="status-label">
        <span class="status-icon">📊</span>
        <span>칼로리 소모</span>
      </div>
      <div class="status-value">
        {{ caloriesBurned }} kcal
      </div>
    </div>
    
    <div v-if="showAlert" class="alert-message">
      <div class="alert-icon">⚠️</div>
      <div class="alert-text">
        <strong>주의!</strong> 생체 신호가 비정상적입니다. 운동을 중단하고 휴식을 취하세요.
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StatusDisplay',
  props: {
    heartRate: {
      type: Number,
      default: 75
    },
    oxygenSaturation: {
      type: Number,
      default: 98
    },
    exerciseIntensity: {
      type: String,
      default: '보통'
    },
    exerciseTime: {
      type: Number,
      default: 0
    },
    caloriesBurned: {
      type: Number,
      default: 0
    }
  },
  computed: {
    showAlert() {
      return this.heartRate > 120 || this.heartRate < 50 || this.oxygenSaturation < 90
    }
  },
  methods: {
    getHeartRateStatus() {
      if (this.heartRate > 120 || this.heartRate < 50) {
        return 'status-danger'
      } else if (this.heartRate > 100 || this.heartRate < 60) {
        return 'status-warning'
      }
      return 'status-good'
    },
    getOxygenStatus() {
      if (this.oxygenSaturation < 90) {
        return 'status-danger'
      } else if (this.oxygenSaturation < 95) {
        return 'status-warning'
      }
      return 'status-good'
    },
    getIntensityStatus() {
      switch (this.exerciseIntensity) {
        case '낮음':
          return 'status-good'
        case '보통':
          return 'status-warning'
        case '높음':
          return 'status-danger'
        default:
          return 'status-good'
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
    }
  }
}
</script>

<style scoped>
.status-display {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.status-display h3 {
  margin-bottom: 20px;
  color: #333;
  font-size: 1.3rem;
  text-align: center;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.status-item:last-child {
  border-bottom: none;
}

.status-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 500;
  color: #666;
}

.status-icon {
  font-size: 1.2rem;
}

.status-value {
  font-size: 1.2rem;
  font-weight: bold;
  padding: 5px 10px;
  border-radius: 6px;
  min-width: 80px;
  text-align: center;
}

.status-good {
  color: #4CAF50;
  background-color: #e8f5e8;
}

.status-warning {
  color: #FF9800;
  background-color: #fff3e0;
}

.status-danger {
  color: #f44336;
  background-color: #ffebee;
}

.alert-message {
  margin-top: 20px;
  padding: 15px;
  background-color: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  animation: pulse 2s infinite;
}

.alert-icon {
  font-size: 1.5rem;
}

.alert-text {
  color: #856404;
  font-size: 0.9rem;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.02);
  }
  100% {
    transform: scale(1);
  }
}

@media (max-width: 768px) {
  .status-item {
    padding: 12px 0;
  }
  
  .status-value {
    font-size: 1rem;
    min-width: 70px;
  }
  
  .status-label {
    font-size: 0.9rem;
  }
}
</style>
