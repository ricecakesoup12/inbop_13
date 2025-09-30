<template>
  <div class="role-select-container">
    <div class="role-header">
      <h1>역할 선택</h1>
      <p>설문을 시작하기 전에 본인의 역할을 선택해주세요.</p>
    </div>
    
    <div class="role-content">
      <div class="role-card">
        <div class="role-card-header">
          <h2>역할 선택</h2>
        </div>
        <div class="role-card-content">
          <div class="role-buttons">
            <button @click="selectRole('user')" class="role-btn user-btn">
              <div class="role-icon">👤</div>
              <div class="role-title">사용자</div>
              <div class="role-description">본인이 직접 설문에 응답합니다</div>
            </button>
            
            <button @click="selectRole('guardian')" class="role-btn guardian-btn">
              <div class="role-icon">👥</div>
              <div class="role-title">보호자</div>
              <div class="role-description">보호 대상자를 대신하여 설문에 응답합니다</div>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const selectRole = (role) => {
  if (role === 'user') {
    // 사용자 선택 시 설문지 페이지로 이동
    router.push('/user/survey')
  } else if (role === 'guardian') {
    // 보호자 선택 시 guardian 대시보드로 이동
    router.push('/guardian/dashboard')
  }
}
</script>

<style scoped>
.role-select-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Gowun Dodum', sans-serif;
  background-color: #f5f5dc; /* 베이지 배경색 */
  min-height: 100vh;
}

.role-header {
  text-align: center;
  margin-bottom: 30px;
}

.role-header h1 {
  color: #2c3e50;
  font-size: 2.5rem;
  margin-bottom: 10px;
}

.role-header p {
  color: #555;
  font-size: 1.1rem;
  margin-bottom: 0;
}

.role-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.role-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.role-card-header {
  background: #90fd97;
  color: white;
  padding: 15px 20px;
}

.role-card-header h2 {
  margin: 0;
  font-size: 1.3rem;
}

.role-card-content {
  padding: 30px;
}

.role-buttons {
  display: flex;
  gap: 30px;
  justify-content: center;
  flex-wrap: wrap;
}

.role-btn {
  background: #90EE90; /* 연두색 */
  color: #2c3e50;
  border: none;
  padding: 30px 20px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: bold;
  min-width: 250px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.role-btn:hover {
  background: #7ED321; /* 더 진한 연두색 */
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.role-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.role-icon {
  font-size: 3rem;
  margin-bottom: 15px;
}

.role-title {
  font-size: 1.4rem;
  margin-bottom: 10px;
  font-weight: bold;
}

.role-description {
  font-size: 0.9rem;
  color: #555;
  line-height: 1.4;
}

.guardian-btn {
  background: #D3D3D3; /* 회색 (비활성화 상태) */
  color: #666;
}

.guardian-btn:hover {
  background: #C0C0C0;
  transform: none;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .role-select-container {
    padding: 10px;
  }
  
  .role-header h1 {
    font-size: 2rem;
  }
  
  .role-card-content {
    padding: 20px;
  }
  
  .role-buttons {
    flex-direction: column;
    align-items: center;
    gap: 20px;
  }
  
  .role-btn {
    min-width: 200px;
    padding: 25px 15px;
  }
  
  .role-icon {
    font-size: 2.5rem;
  }
  
  .role-title {
    font-size: 1.2rem;
  }
  
  .role-description {
    font-size: 0.8rem;
  }
}
</style>
