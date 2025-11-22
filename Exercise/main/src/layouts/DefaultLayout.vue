<template>
  <div class="DefaultLayoutPage">
    <div class="DefaultLayoutContainer">
      <header class="DefaultLayoutHeader">
        <h1 class="DefaultLayoutTitle">보호자 대시보드</h1>
        <RouterLink to="/" class="BackToMainLink">
          메인으로
        </RouterLink>
      </header>
      <main class="DefaultLayoutMain">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts"></script>

