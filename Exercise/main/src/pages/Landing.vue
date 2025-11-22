<template>
  <div class="LandingPage">
    <div class="LandingContainer">
      <div class="LandingTitleSection">
        <h1 class="LandingTitle">운동 코칭 앱</h1>
        <p class="LandingSubtitle">건강한 생활을 위한 스마트 운동 관리</p>
      </div>

      <div class="LandingRoleSelection">
        <RouterLink to="/guardian" class="LandingRoleLink">
          <div class="GuardianRoleCard">
            <div class="GuardianRoleHeader">
              <div class="GuardianRoleIconWrapper">
                <svg class="GuardianRoleIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
              </div>
              <div class="GuardianRoleContent">
                <h2 class="GuardianRoleTitle">보호자</h2>
                <p class="GuardianRoleSubtitle">사용자 관리 및 모니터링</p>
              </div>
            </div>
            <p class="GuardianRoleDescription">
              사용자의 건강 상태를 실시간으로 모니터링하고 설문을 관리합니다.
            </p>
          </div>
        </RouterLink>

        <RouterLink to="/user/register" class="LandingRoleLink">
          <div class="UserRoleCard">
            <div class="UserRoleHeader">
              <div class="UserRoleIconWrapper">
                <svg class="UserRoleIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <div class="UserRoleContent">
                <h2 class="UserRoleTitle">사용자</h2>
                <p class="UserRoleSubtitle">운동 및 건강 관리</p>
              </div>
            </div>
            <p class="UserRoleDescription">
              운동을 시작하고 코칭 챗봇과 함께 건강한 생활 습관을 만들어갑니다.
            </p>
          </div>
        </RouterLink>
      </div>

      <div class="LandingPrompt">
        <p>사용할 역할을 선택해주세요</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts"></script>

