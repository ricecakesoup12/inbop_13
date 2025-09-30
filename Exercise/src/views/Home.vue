<template>
  <div class="home-container">
    <div class="welcome-section">
      <h1 class="app-title">🏃‍♂️ 운동 관리 앱</h1>
      <p class="app-subtitle">아두이노 센서와 함께하는 스마트 운동 관리</p>
    </div>
    
    <div class="user-selection">
      <h2>사용자 유형을 선택해주세요</h2>
      <div class="selection-buttons">
        <router-link to="/guardian" class="btn btn-primary selection-btn">
          <div class="btn-icon">👨‍👩‍👧‍👦</div>
          <div class="btn-text">
            <h3>보호자</h3>
            <p>운동 현황 모니터링<br>실시간 위치 확인<br>챗봇 상담</p>
          </div>
        </router-link>
        
        <router-link to="/user" class="btn btn-secondary selection-btn">
          <div class="btn-icon">🏃‍♂️</div>
          <div class="btn-text">
            <h3>운동자</h3>
            <p>운동 루틴 관리<br>실시간 센서 모니터링<br>운동 시작</p>
          </div>
        </router-link>
      </div>
    </div>
    
    <div class="features-section">
      <h3>주요 기능</h3>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">💓</div>
          <h4>실시간 생체 신호</h4>
          <p>심박수와 산소포화도 모니터링</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">🗓️</div>
          <h4>운동 루틴 관리</h4>
          <p>개인 맞춤 운동 계획</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">🗺️</div>
          <h4>실시간 위치 추적</h4>
          <p>안전한 운동 경로 안내</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">🤖</div>
          <h4>AI 운동 챗봇</h4>
          <p>음성 인식 기반 운동 상담</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  mounted() {
    // 페이지 로드 시 애니메이션 효과
    this.animateElements()
  },
  methods: {
    animateElements() {
      const elements = document.querySelectorAll('.selection-btn, .feature-card')
      elements.forEach((el, index) => {
        setTimeout(() => {
          el.style.opacity = '1'
          el.style.transform = 'translateY(0)'
        }, index * 200)
      })
    }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.welcome-section {
  text-align: center;
  margin-bottom: 60px;
  color: white;
}

.app-title {
  font-size: 3.5rem;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
  animation: fadeInDown 1s ease-out;
}

.app-subtitle {
  font-size: 1.3rem;
  opacity: 0.9;
  animation: fadeInUp 1s ease-out 0.3s both;
}

.user-selection {
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
  margin-bottom: 60px;
  text-align: center;
  max-width: 800px;
  width: 100%;
  animation: fadeInUp 1s ease-out 0.6s both;
}

.user-selection h2 {
  margin-bottom: 40px;
  color: #333;
  font-size: 2rem;
}

.selection-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  max-width: 600px;
  margin: 0 auto;
}

.selection-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px;
  text-decoration: none;
  border-radius: 15px;
  transition: all 0.3s ease;
  opacity: 0;
  transform: translateY(30px);
  min-height: 200px;
}

.selection-btn:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 0 15px 30px rgba(0,0,0,0.2);
}

.btn-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.btn-text h3 {
  font-size: 1.5rem;
  margin-bottom: 15px;
  color: white;
}

.btn-text p {
  font-size: 1rem;
  line-height: 1.6;
  color: rgba(255,255,255,0.9);
}

.features-section {
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
  max-width: 1000px;
  width: 100%;
  text-align: center;
  animation: fadeInUp 1s ease-out 0.9s both;
}

.features-section h3 {
  margin-bottom: 40px;
  color: #333;
  font-size: 2rem;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 30px;
}

.feature-card {
  padding: 30px 20px;
  border-radius: 15px;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  transition: all 0.3s ease;
  opacity: 0;
  transform: translateY(30px);
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 20px;
}

.feature-card h4 {
  margin-bottom: 15px;
  color: #333;
  font-size: 1.2rem;
}

.feature-card p {
  color: #666;
  line-height: 1.5;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .selection-buttons {
    grid-template-columns: 1fr;
  }
  
  .app-title {
    font-size: 2.5rem;
  }
  
  .user-selection, .features-section {
    padding: 20px;
  }
}
</style>
