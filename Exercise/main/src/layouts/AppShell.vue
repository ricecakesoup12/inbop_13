<template>
  <div class="min-h-screen bg-gray-50">
    <header class="bg-white border-b border-gray-200 py-4 px-6 shadow-sm sticky top-0 z-10">
      <div class="max-w-screen-md mx-auto flex items-center justify-between">
        <h1 class="text-xl font-semibold text-text-main font-gowun">운동 코칭</h1>
        <RouterLink to="/" class="text-sm text-primary hover:text-primary-hover transition-colors font-gowun">
          메인으로
        </RouterLink>
      </div>
    </header>
    <main class="p-4">
      <div class="max-w-screen-lg mx-auto">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts"></script>

