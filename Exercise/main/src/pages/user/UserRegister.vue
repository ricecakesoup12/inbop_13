<template>
  <div class="UserRegisterPage">
    <div class="UserRegisterContainer">
      <div class="UserRegisterCard">
        <div class="UserRegisterHeader">
          <div class="UserRegisterIconWrapper">
            <svg class="UserRegisterIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
            </svg>
          </div>
          <h1 class="UserRegisterTitle">사용자 로그인</h1>
          <p class="UserRegisterSubtitle">보호자에게 받은 코드를 입력하세요</p>
        </div>

        <!-- 코드 입력 -->
        <div class="UserRegisterForm">
          <div>
            <label class="UserRegisterCodeLabel">
              로그인 코드
            </label>
            <input
              v-model="loginCode"
              type="text"
              maxlength="4"
              placeholder="AB12"
              class="UserRegisterCodeInput"
              @input="loginCode = loginCode.toUpperCase()"
              @keyup.enter="handleCodeLogin"
            />
            <p class="UserRegisterCodeHint">영문 + 숫자 4자리 (예: AB12)</p>
          </div>
          
          <AppButton @click="handleCodeLogin" :disabled="loading" class="UserRegisterLoginButton">
            {{ loading ? '로그인 중...' : '로그인' }}
          </AppButton>
          
          <button
            type="button"
            @click="$router.push('/')"
            class="UserRegisterBackButton"
          >
            메인으로 돌아가기
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getUserByCode } from '@/services/api/users'
import AppButton from '@/components/common/AppButton.vue'

const router = useRouter()
const loginCode = ref('')
const loading = ref(false)

const handleCodeLogin = async () => {
  if (!loginCode.value || loginCode.value.length !== 4) {
    alert('4자리 코드를 입력해주세요.')
    return
  }

  loading.value = true

  try {
    const user = await getUserByCode(loginCode.value)
    
    // localStorage에 사용자 정보 저장
    localStorage.setItem('userId', user.id)
    localStorage.setItem('userCode', loginCode.value)
    localStorage.setItem('userName', user.name)
    
    alert(`${user.name}님 환영합니다!`)
    router.push('/user')
  } catch (error) {
    console.error('로그인 실패:', error)
    alert('코드를 찾을 수 없습니다. 보호자에게 코드를 확인해주세요.')
  } finally {
    loading.value = false
  }
}
</script>
