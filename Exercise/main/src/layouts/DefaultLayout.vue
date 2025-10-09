<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-screen-md mx-auto">
      <header class="bg-beige border-b border-accent py-4 px-6 flex items-center justify-between sticky top-0 z-10">
        <h1 class="text-xl font-semibold text-text-main font-gowun">보호자 대시보드</h1>
        <RouterLink to="/" class="text-sm text-primary hover:text-primary-hover transition-colors font-gowun">
          메인으로
        </RouterLink>
      </header>
      <main class="p-6">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts"></script>

