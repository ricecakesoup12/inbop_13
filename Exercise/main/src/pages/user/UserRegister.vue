<template>
  <div class="min-h-screen bg-gradient-to-br from-beige to-white flex items-center justify-center p-6">
    <div class="max-w-md w-full">
      <div class="bg-white rounded-2xl shadow-soft border border-gray-200 p-8">
        <div class="text-center mb-8">
          <div class="w-20 h-20 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-10 h-10 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
            </svg>
          </div>
          <h1 class="text-3xl font-bold text-text-main font-gowun mb-2">사용자 로그인</h1>
          <p class="text-text-sub font-gowun">보호자에게 받은 코드를 입력하세요</p>
        </div>

        <!-- 코드 입력 -->
        <div class="space-y-5">
          <div>
            <label class="block font-semibold mb-3 font-gowun text-center">
              로그인 코드
            </label>
            <input
              v-model="loginCode"
              type="text"
              maxlength="4"
              placeholder="AB12"
              class="w-full px-6 py-4 border-2 border-gray-200 rounded-xl focus:border-primary outline-none font-gowun uppercase text-center text-3xl tracking-[0.5em] font-bold"
              @input="loginCode = loginCode.toUpperCase()"
              @keyup.enter="handleCodeLogin"
            />
            <p class="text-xs text-text-sub mt-2 text-center font-gowun">영문 + 숫자 4자리 (예: AB12)</p>
          </div>
          
          <AppButton @click="handleCodeLogin" :disabled="loading" class="w-full py-4">
            {{ loading ? '로그인 중...' : '로그인' }}
          </AppButton>
          
          <button
            type="button"
            @click="$router.push('/')"
            class="w-full text-sm text-text-sub hover:text-text-main font-gowun transition-colors"
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
