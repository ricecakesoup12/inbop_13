<template>
  <div class="calendar">
    <div class="calendar-header">
      <button @click="previousMonth" class="nav-btn">‹</button>
      <h3>{{ currentMonthYear }}</h3>
      <button @click="nextMonth" class="nav-btn">›</button>
    </div>
    
    <div class="calendar-weekdays">
      <div v-for="day in weekdays" :key="day" class="weekday">{{ day }}</div>
    </div>
    
    <div class="calendar-grid">
      <div 
        v-for="day in calendarDays" 
        :key="day.date"
        :class="[
          'calendar-day',
          { 
            'today': day.isToday,
            'has-exercise': day.hasExercise,
            'other-month': !day.isCurrentMonth
          }
        ]"
        @click="selectDay(day)"
      >
        <div class="day-number">{{ day.day }}</div>
        <div v-if="day.hasExercise" class="exercise-indicator">
          <div class="progress-bar">
            <div 
              class="progress-fill" 
              :style="{ width: day.exerciseProgress + '%' }"
            ></div>
          </div>
          <div class="progress-text">{{ day.exerciseProgress }}%</div>
        </div>
      </div>
    </div>
    
    <div v-if="selectedDay" class="selected-day-info">
      <h4>{{ selectedDay.date }} 운동 계획</h4>
      <div v-if="selectedDay.exercises && selectedDay.exercises.length > 0">
        <div v-for="exercise in selectedDay.exercises" :key="exercise.id" class="exercise-item">
          <span class="exercise-time">{{ exercise.time }}</span>
          <span class="exercise-name">{{ exercise.name }}</span>
          <span :class="['exercise-status', exercise.status]">{{ exercise.statusText }}</span>
        </div>
      </div>
      <div v-else class="no-exercise">
        <p>이 날은 운동 계획이 없습니다.</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Calendar',
  props: {
    exercises: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      currentDate: new Date(),
      selectedDay: null,
      weekdays: ['일', '월', '화', '수', '목', '금', '토']
    }
  },
  computed: {
    currentMonthYear() {
      return this.currentDate.toLocaleDateString('ko-KR', { 
        year: 'numeric', 
        month: 'long' 
      })
    },
    calendarDays() {
      const year = this.currentDate.getFullYear()
      const month = this.currentDate.getMonth()
      
      const firstDay = new Date(year, month, 1)
      const lastDay = new Date(year, month + 1, 0)
      const startDate = new Date(firstDay)
      startDate.setDate(startDate.getDate() - firstDay.getDay())
      
      const days = []
      const today = new Date()
      
      for (let i = 0; i < 42; i++) {
        const date = new Date(startDate)
        date.setDate(startDate.getDate() + i)
        
        const dayData = {
          date: date.toISOString().split('T')[0],
          day: date.getDate(),
          isCurrentMonth: date.getMonth() === month,
          isToday: date.toDateString() === today.toDateString(),
          hasExercise: this.hasExerciseForDate(date),
          exerciseProgress: this.getExerciseProgress(date),
          exercises: this.getExercisesForDate(date)
        }
        
        days.push(dayData)
      }
      
      return days
    }
  },
  methods: {
    previousMonth() {
      this.currentDate.setMonth(this.currentDate.getMonth() - 1)
    },
    nextMonth() {
      this.currentDate.setMonth(this.currentDate.getMonth() + 1)
    },
    selectDay(day) {
      this.selectedDay = day
      this.$emit('day-selected', day)
    },
    hasExerciseForDate(date) {
      const dateStr = date.toISOString().split('T')[0]
      return this.exercises.some(ex => ex.date === dateStr)
    },
    getExerciseProgress(date) {
      const dateStr = date.toISOString().split('T')[0]
      const dayExercises = this.exercises.filter(ex => ex.date === dateStr)
      if (dayExercises.length === 0) return 0
      
      const completed = dayExercises.filter(ex => ex.status === 'completed').length
      return Math.round((completed / dayExercises.length) * 100)
    },
    getExercisesForDate(date) {
      const dateStr = date.toISOString().split('T')[0]
      return this.exercises.filter(ex => ex.date === dateStr)
    }
  }
}
</script>

<style scoped>
.calendar {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.calendar-header h3 {
  font-size: 1.5rem;
  color: #333;
}

.nav-btn {
  background: #f8f9fa;
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  font-size: 1.2rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.nav-btn:hover {
  background: #e9ecef;
  transform: scale(1.1);
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 5px;
  margin-bottom: 10px;
}

.weekday {
  text-align: center;
  font-weight: bold;
  color: #666;
  padding: 10px 5px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 5px;
}

.calendar-day {
  padding: 10px 5px;
  text-align: center;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 60px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.calendar-day:hover {
  background-color: #f0f0f0;
}

.calendar-day.today {
  background-color: #4CAF50;
  color: white;
}

.calendar-day.has-exercise {
  background-color: #E3F2FD;
  border: 2px solid #2196F3;
}

.calendar-day.other-month {
  color: #ccc;
}

.day-number {
  font-weight: bold;
  font-size: 1rem;
}

.exercise-indicator {
  margin-top: 5px;
}

.progress-bar {
  width: 100%;
  height: 4px;
  background-color: rgba(0,0,0,0.1);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 2px;
}

.progress-fill {
  height: 100%;
  background-color: #4CAF50;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 0.7rem;
  font-weight: bold;
  color: #333;
}

.selected-day-info {
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.selected-day-info h4 {
  margin-bottom: 15px;
  color: #333;
}

.exercise-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.exercise-item:last-child {
  border-bottom: none;
}

.exercise-time {
  font-weight: bold;
  color: #666;
  min-width: 60px;
}

.exercise-name {
  flex: 1;
  margin: 0 10px;
}

.exercise-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: bold;
}

.exercise-status.completed {
  background-color: #d4edda;
  color: #155724;
}

.exercise-status.pending {
  background-color: #fff3cd;
  color: #856404;
}

.exercise-status.in-progress {
  background-color: #cce5ff;
  color: #004085;
}

.no-exercise {
  text-align: center;
  color: #666;
  font-style: italic;
}

@media (max-width: 768px) {
  .calendar-day {
    min-height: 50px;
    padding: 8px 3px;
  }
  
  .day-number {
    font-size: 0.9rem;
  }
  
  .progress-text {
    font-size: 0.6rem;
  }
}
</style>
