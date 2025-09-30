<template>
  <div class="survey-container">
    <div class="survey-header">
      <h1>건강 상태 체크 설문</h1>
      <div v-if="isSurveyCompleted" class="completion-status">
        <p>✅ 설문이 완료되었습니다.</p>
        <p>보호자로부터 새로운 설문 요청이 있을 때까지 기다려주세요.</p>
      </div>
    </div>
    
    <!-- 설문 요청 팝업 -->
    <div v-if="showSurveyPopup" class="survey-popup-overlay" @click="closeSurveyPopup">
      <div class="survey-popup" @click.stop>
        <div class="popup-header">
          <h2>새로운 설문 요청</h2>
          <button @click="closeSurveyPopup" class="close-btn">×</button>
        </div>
        <div class="popup-content">
          <p>보호자로부터 새로운 설문 요청이 있습니다.</p>
          <p>설문을 진행하시겠습니까?</p>
        </div>
        <div class="popup-actions">
          <button @click="acceptSurvey" class="accept-btn">설문 시작</button>
          <button @click="declineSurvey" class="decline-btn">나중에</button>
        </div>
      </div>
    </div>

    <div v-if="!isSurveyCompleted" class="survey-content">
      <!-- 1. 기본 정보 -->
      <div class="card">
        <div class="card-header">
          <h2>1. 기본 정보</h2>
        </div>
        <div class="card-content">
          <div class="form-group">
            <label>이름: <input type="text" v-model="form.name" placeholder="이름을 입력하세요" class="inline-input name-input"></label>
          </div>

          <div class="form-group">
            <label>성별:</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.gender" value="남성">
                <span class="checkbox-label">남성</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.gender" value="여성">
                <span class="checkbox-label">여성</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.gender" value="기타">
                <span class="checkbox-label">기타/응답하지 않음</span>
              </label>
            </div>
          </div>

          <div class="form-group">
            <label>나이: <input type="number" v-model="form.age" placeholder="____" class="inline-input"> 세</label>
          </div>

          <div class="form-group">
            <label>키: <input type="number" v-model="form.height" placeholder="____" class="inline-input"> cm / 몸무게: <input type="number" v-model="form.weight" placeholder="____" class="inline-input"> kg</label>
          </div>

          <div class="form-group">
            <label>평소 생활 패턴:</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.lifestyle" value="sitting">
                <span class="checkbox-label">주로 앉아 있음 (사무직 등)</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.lifestyle" value="light">
                <span class="checkbox-label">가볍게 활동함 (학생, 가사 등)</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.lifestyle" value="active">
                <span class="checkbox-label">많이 움직임 (서비스직, 현장 근무 등)</span>
              </label>
            </div>
          </div>
        </div>
      </div>

      <!-- 2. 건강 상태 -->
      <div class="card">
        <div class="card-header">
          <h2>2. 건강 상태</h2>
        </div>
        <div class="card-content">
          <div class="form-group">
            <label>현재 진단받은 질환:</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.diseases" value="없음">
                <span class="checkbox-label"> 없음</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.diseases" value="고혈압">
                <span class="checkbox-label"> 고혈압</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.diseases" value="당뇨">
                <span class="checkbox-label"> 당뇨</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.diseases" value="심장질환">
                <span class="checkbox-label"> 심장질환</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.diseases" value="호흡기질환">
                <span class="checkbox-label"> 호흡기질환</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.diseases" value="관절질환">
                <span class="checkbox-label"> 관절질환(무릎/허리/어깨 등)</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.diseases" value="기타">
                <span class="checkbox-label"> 기타: <input type="text" v-model="form.otherDisease" placeholder="______" class="inline-input"></span>
              </label>
            </div>
          </div>

          <div class="form-group">
            <label>최근 6개월 이내 수술/큰 부상:</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.surgery" value="예">
                <span class="checkbox-label"> 예</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.surgery" value="아니오">
                <span class="checkbox-label"> 아니오</span>
              </label>
            </div>
          </div>

          <div class="form-group">
            <label>현재 복용 중인 약:</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.medication" value="예">
                <span class="checkbox-label"> 예 (약물명: <input type="text" v-model="form.medicationName" placeholder="______" class="inline-input">)</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.medication" value="아니오">
                <span class="checkbox-label"> 아니오</span>
              </label>
            </div>
          </div>

          <div class="form-group">
            <label>하루 평균 수면 시간:</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.sleep" value="4시간이하">
                <span class="checkbox-label"> 4시간 이하</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.sleep" value="5-6시간">
                <span class="checkbox-label"> 5–6시간</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.sleep" value="7-8시간">
                <span class="checkbox-label"> 7–8시간</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.sleep" value="9시간이상">
                <span class="checkbox-label"> 9시간 이상</span>
              </label>
            </div>
          </div>
        </div>
      </div>

      <!-- 3. 생활 습관 -->
      <div class="card">
        <div class="card-header">
          <h2>3. 생활 습관</h2>
        </div>
        <div class="card-content">
          <div class="form-group">
            <label>흡연 여부:</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.smoking" value="비흡연">
                <span class="checkbox-label"> 비흡연</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.smoking" value="현재흡연">
                <span class="checkbox-label"> 현재 흡연 중</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.smoking" value="과거흡연">
                <span class="checkbox-label"> 과거 흡연자</span>
              </label>
            </div>
          </div>

          <div class="form-group">
            <label>음주 빈도:</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.alcohol" value="거의안함">
                <span class="checkbox-label"> 거의 안 함</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.alcohol" value="주1-2회">
                <span class="checkbox-label"> 주 1–2회</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.alcohol" value="주3-4회">
                <span class="checkbox-label"> 주 3–4회</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.alcohol" value="거의매일">
                <span class="checkbox-label"> 거의 매일</span>
              </label>
            </div>
          </div>

          <div class="form-group">
            <label>평소 식습관:</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.diet" value="채소과일">
                <span class="checkbox-label"> 채소/과일 위주</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.diet" value="가공외식">
                <span class="checkbox-label"> 가공식품/외식 위주</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.diet" value="균형">
                <span class="checkbox-label"> 균형 잡힌 편</span>
              </label>
            </div>
          </div>
        </div>
      </div>

      <!-- 4. 운동 경험 및 체력 -->
      <div class="card">
        <div class="card-header">
          <h2>4. 운동 경험 및 체력 (※ NIA 4대 영역 반영)</h2>
        </div>
        <div class="card-content">
          <div class="form-group">
            <label>주간 평균 운동 횟수:</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseFrequency" value="전혀안함">
                <span class="checkbox-label"> 전혀 안 함</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseFrequency" value="1-2회">
                <span class="checkbox-label"> 1–2회</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseFrequency" value="3-4회">
                <span class="checkbox-label"> 3–4회</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseFrequency" value="5회이상">
                <span class="checkbox-label"> 5회 이상</span>
              </label>
            </div>
          </div>

          <div class="form-group">
            <label>선호하는 운동 (복수 선택 가능):</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.preferredExercise" value="유산소">
                <span class="checkbox-label"> 유산소(걷기, 자전거 등)</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.preferredExercise" value="근력">
                <span class="checkbox-label"> 근력(밴드, 아령, 체중부하)</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.preferredExercise" value="균형훈련">
                <span class="checkbox-label"> 균형훈련(한 발 서기, Tai Chi 등)</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.preferredExercise" value="유연성">
                <span class="checkbox-label"> 유연성(요가, 스트레칭 등)</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.preferredExercise" value="기타">
                <span class="checkbox-label"> 기타: <input type="text" v-model="form.otherExercise" placeholder="______" class="inline-input"></span>
              </label>
            </div>
          </div>

          <div class="form-group">
            <label>운동 목표:</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseGoal" value="체중감량">
                <span class="checkbox-label"> 체중 감량</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseGoal" value="근육증가">
                <span class="checkbox-label"> 근육 증가</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseGoal" value="건강관리">
                <span class="checkbox-label"> 건강 관리</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseGoal" value="재활통증">
                <span class="checkbox-label"> 재활/통증 완화</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseGoal" value="낙상예방">
                <span class="checkbox-label"> 낙상 예방</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseGoal" value="기타">
                <span class="checkbox-label"> 기타: <input type="text" v-model="form.otherGoal" placeholder="______" class="inline-input"></span>
              </label>
            </div>
          </div>

          <div class="form-group">
            <label>본인 체력 수준: (1=매우 약함, 5=매우 강함)</label>
            <div class="radio-group">
              <label class="radio-item">
                <input type="radio" v-model="form.fitnessLevel" value="1">
                <span class="radio-label">1</span>
              </label>
              <label class="radio-item">
                <input type="radio" v-model="form.fitnessLevel" value="2">
                <span class="radio-label">2</span>
              </label>
              <label class="radio-item">
                <input type="radio" v-model="form.fitnessLevel" value="3">
                <span class="radio-label">3</span>
              </label>
              <label class="radio-item">
                <input type="radio" v-model="form.fitnessLevel" value="4">
                <span class="radio-label">4</span>
              </label>
              <label class="radio-item">
                <input type="radio" v-model="form.fitnessLevel" value="5">
                <span class="radio-label">5</span>
              </label>
            </div>
          </div>
        </div>
      </div>

      <!-- 5. 신체적 한계 및 특이사항 -->
      <div class="card">
        <div class="card-header">
          <h2>5. 신체적 한계 및 특이사항</h2>
        </div>
        <div class="card-content">
          <div class="form-group">
            <label>운동 시 불편/통증 부위:</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.painArea" value="없음">
                <span class="checkbox-label"> 없음</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.painArea" value="허리">
                <span class="checkbox-label"> 허리</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.painArea" value="무릎">
                <span class="checkbox-label"> 무릎</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.painArea" value="어깨">
                <span class="checkbox-label"> 어깨</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.painArea" value="기타">
                <span class="checkbox-label"> 기타: <input type="text" v-model="form.otherPainArea" placeholder="______" class="inline-input"></span>
              </label>
            </div>
          </div>

          <div class="form-group">
            <label>의사로부터 운동 제한 지시 여부:</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseRestriction" value="예">
                <span class="checkbox-label"> 예</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseRestriction" value="아니오">
                <span class="checkbox-label"> 아니오</span>
              </label>
            </div>
          </div>
        </div>
      </div>

      <!-- 6. 심리/동기 요인 -->
      <div class="card">
        <div class="card-header">
          <h2>6. 심리/동기 요인</h2>
        </div>
        <div class="card-content">
          <div class="form-group">
            <label>운동 시작 이유:</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseReason" value="건강개선">
                <span class="checkbox-label"> 건강 개선</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseReason" value="체형관리">
                <span class="checkbox-label"> 체형 관리</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseReason" value="스트레스해소">
                <span class="checkbox-label"> 스트레스 해소</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseReason" value="생활습관개선">
                <span class="checkbox-label"> 생활 습관 개선</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseReason" value="낙상예방">
                <span class="checkbox-label"> 낙상 예방/균형 능력 향상</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseReason" value="기타">
                <span class="checkbox-label"> 기타: <input type="text" v-model="form.otherReason" placeholder="______" class="inline-input"></span>
              </label>
            </div>
          </div>

          <div class="form-group">
            <label>운동 자신감:</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseConfidence" value="매우자신없음">
                <span class="checkbox-label"> 매우 자신 없음</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseConfidence" value="조금자신없음">
                <span class="checkbox-label"> 조금 자신 없음</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseConfidence" value="보통">
                <span class="checkbox-label"> 보통</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseConfidence" value="자신있음">
                <span class="checkbox-label"> 자신 있음</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseConfidence" value="매우자신있음">
                <span class="checkbox-label"> 매우 자신 있음</span>
              </label>
            </div>
          </div>

          <div class="form-group">
            <label>선호하는 운동 환경:</label>
            <div class="checkbox-group">
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseEnvironment" value="집">
                <span class="checkbox-label"> 집</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseEnvironment" value="헬스장">
                <span class="checkbox-label"> 헬스장</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseEnvironment" value="야외">
                <span class="checkbox-label"> 야외</span>
              </label>
              <label class="checkbox-item">
                <input type="checkbox" v-model="form.exerciseEnvironment" value="그룹수업">
                <span class="checkbox-label"> 그룹/수업</span>
              </label>
            </div>
          </div>
        </div>
      </div>

      <!-- 제출 버튼 -->
      <div class="submit-section">
        <button @click="submitForm" class="submit-btn">설문 제출</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

// 설문 완료 상태 관리
const isSurveyCompleted = ref(false)
const showSurveyPopup = ref(false)

// 설문 요청 이벤트 리스너
const handleSurveyRequest = (event) => {
  if (event.detail && event.detail.type === 'survey_request') {
    showSurveyPopup.value = true
  }
}

onMounted(() => {
  // 로컬 스토리지에서 설문 완료 상태 확인
  const completed = localStorage.getItem('survey_completed')
  if (completed === 'true') {
    isSurveyCompleted.value = true
  }
  
  // 설문 요청 이벤트 리스너 등록
  window.addEventListener('surveyRequest', handleSurveyRequest)
})

onUnmounted(() => {
  // 이벤트 리스너 제거
  window.removeEventListener('surveyRequest', handleSurveyRequest)
})

const form = reactive({
  // 1. 기본 정보
  name: '',
  gender: [],
  age: '',
  height: '',
  weight: '',
  lifestyle: [],
  
  // 2. 건강 상태
  diseases: [],
  otherDisease: '',
  surgery: [],
  medication: [],
  medicationName: '',
  sleep: [],
  
  // 3. 생활 습관
  smoking: [],
  alcohol: [],
  diet: [],
  
  // 4. 운동 경험 및 체력
  exerciseFrequency: [],
  preferredExercise: [],
  otherExercise: '',
  exerciseGoal: [],
  otherGoal: '',
  fitnessLevel: '',
  
  // 5. 신체적 한계 및 특이사항
  painArea: [],
  otherPainArea: '',
  exerciseRestriction: [],
  
  // 6. 심리/동기 요인
  exerciseReason: [],
  otherReason: '',
  exerciseConfidence: [],
  exerciseEnvironment: []
})

const submitForm = async () => {
  // 이름 필수 입력 확인
  if (!form.name.trim()) {
    alert('이름을 입력해주세요.')
    return
  }
  
  try {
    // 서버 전송
    const res = await axios.post('http://localhost:8000/api/survey', form)
    console.log('서버 응답:', res.data)
    
    // 설문 완료 상태 저장
    localStorage.setItem('survey_completed', 'true')
    isSurveyCompleted.value = true
    
    alert('설문이 제출되었습니다!')
    
    // 결과 페이지로 이동
    router.push({ path: '/user/result', query: { id: res.data.userId } })
  } catch (err) {
    console.error(err)
    alert('서버 전송 중 오류가 발생했습니다.')
  }
}

// 팝업 관련 함수들
const closeSurveyPopup = () => {
  showSurveyPopup.value = false
}

const acceptSurvey = () => {
  showSurveyPopup.value = false
  isSurveyCompleted.value = false
  // 설문 폼 초기화
  Object.keys(form).forEach(key => {
    if (Array.isArray(form[key])) {
      form[key] = []
    } else {
      form[key] = ''
    }
  })
}

const declineSurvey = () => {
  showSurveyPopup.value = false
  // 보호자에게 거부 알림 (실제로는 서버에 알림)
  console.log('사용자가 설문을 거부했습니다.')
}
</script>

<style scoped>
.survey-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Gowun Dodum', sans-serif;
}

.survey-header {
  text-align: center;
  margin-bottom: 30px;
}

.survey-header h1 {
  color: #2c3e50;
  font-size: 2.5rem;
  margin-bottom: 10px;
}

.survey-content {
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
  background: #3498db;
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

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-weight: bold;
  margin-bottom: 10px;
  color: #2c3e50;
}

.inline-input {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 4px 8px;
  margin: 0 5px;
  width: 80px;
  font-size: 14px;
}

.name-input {
  width: 200px;
  margin-left: 10px;
}

.checkbox-group, .radio-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.checkbox-item, .radio-item {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.checkbox-item:hover, .radio-item:hover {
  background-color: #f8f9fa;
}

.checkbox-item input, .radio-item input {
  margin-right: 10px;
  transform: scale(1.2);
}

.checkbox-label, .radio-label {
  font-size: 14px;
  color: #555;
}

.submit-section {
  text-align: center;
  margin-top: 30px;
}

.submit-btn {
  background: #27ae60;
  color: white;
  border: none;
  padding: 15px 40px;
  font-size: 1.1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-weight: bold;
}

.submit-btn:hover {
  background: #229954;
}

.submit-btn:active {
  transform: translateY(1px);
}

/* 설문 완료 상태 스타일 */
.completion-status {
  background: #d4edda;
  border: 1px solid #c3e6cb;
  border-radius: 8px;
  padding: 20px;
  margin: 20px 0;
  text-align: center;
}

.completion-status p {
  margin: 5px 0;
  color: #155724;
  font-size: 1.1rem;
}

/* 설문 요청 팝업 스타일 */
.survey-popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.survey-popup {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  max-width: 400px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.popup-header {
  background: #90fd97;
  color: white;
  padding: 20px;
  border-radius: 12px 12px 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.popup-header h2 {
  margin: 0;
  font-size: 1.3rem;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.popup-content {
  padding: 20px;
  text-align: center;
}

.popup-content p {
  margin: 10px 0;
  color: #555;
  font-size: 1rem;
}

.popup-actions {
  padding: 20px;
  display: flex;
  gap: 15px;
  justify-content: center;
}

.accept-btn, .decline-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: bold;
  transition: all 0.3s;
  min-width: 120px;
}

.accept-btn {
  background: #90EE90;
  color: #2c3e50;
}

.accept-btn:hover {
  background: #7ED321;
}

.decline-btn {
  background: #6c757d;
  color: white;
}

.decline-btn:hover {
  background: #5a6268;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .survey-container {
    padding: 10px;
  }
  
  .survey-header h1 {
    font-size: 2rem;
  }
  
  .card-content {
    padding: 15px;
  }
  
  .checkbox-group, .radio-group {
    gap: 6px;
  }
  
  .checkbox-item, .radio-item {
    padding: 6px;
  }
  
  .popup-actions {
    flex-direction: column;
  }
  
  .accept-btn, .decline-btn {
    width: 100%;
  }
}
</style>