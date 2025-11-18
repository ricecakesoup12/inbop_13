<template>
  <div class="min-h-screen bg-gradient-to-br from-beige to-white flex flex-col items-center justify-center p-6">
    <div class="max-w-md w-full space-y-8">
      <div class="text-center">
        <h1 class="text-4xl font-bold text-text-main mb-2 font-gowun">운동 코칭 앱</h1>
        <p class="text-text-sub font-gowun">건강한 생활을 위한 스마트 운동 관리</p>
      </div>

      <div class="space-y-4">
        <RouterLink to="/guardian" class="block">
          <div class="bg-white rounded-2xl p-8 shadow-soft border-2 border-transparent hover:border-primary transition-all duration-200 cursor-pointer transform hover:-translate-y-1">
            <div class="flex items-center gap-4 mb-3">
              <div class="w-16 h-16 bg-accent rounded-full flex items-center justify-center">
                <svg class="w-8 h-8 text-text-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
              </div>
              <div class="flex-1">
                <h2 class="text-2xl font-semibold text-text-main font-gowun">보호자</h2>
                <p class="text-sm text-text-sub font-gowun">사용자 관리 및 모니터링</p>
              </div>
            </div>
            <p class="text-text-sub text-sm font-gowun">
              사용자의 건강 상태를 실시간으로 모니터링하고 설문을 관리합니다.
            </p>
          </div>
        </RouterLink>

        <RouterLink to="/user/register" class="block">
          <div class="bg-white rounded-2xl p-8 shadow-soft border-2 border-transparent hover:border-primary transition-all duration-200 cursor-pointer transform hover:-translate-y-1">
            <div class="flex items-center gap-4 mb-3">
              <div class="w-16 h-16 bg-primary rounded-full flex items-center justify-center">
                <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <div class="flex-1">
                <h2 class="text-2xl font-semibold text-text-main font-gowun">사용자</h2>
                <p class="text-sm text-text-sub font-gowun">운동 및 건강 관리</p>
              </div>
            </div>
            <p class="text-text-sub text-sm font-gowun">
              운동을 시작하고 코칭 챗봇과 함께 건강한 생활 습관을 만들어갑니다.
            </p>
          </div>
        </RouterLink>
      </div>

      <div class="text-center text-sm text-text-sub font-gowun">
        <p>사용할 역할을 선택해주세요</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts"></script>

