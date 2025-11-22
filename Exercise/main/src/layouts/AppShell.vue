<template>
  <div class="AppShellPage">
    <header class="AppShellHeader">
      <div class="AppShellHeaderContainer">
        <h1 class="AppShellTitle">운동 코칭</h1>
        <RouterLink to="/" class="BackToMainLink">
          메인으로
        </RouterLink>
      </div>
    </header>
    <main class="AppShellMain">
      <div class="AppShellMainContainer">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts"></script>

