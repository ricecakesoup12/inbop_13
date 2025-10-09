<template>
  <div class="bg-white rounded-xl p-4 flex items-center gap-4 shadow-soft border border-gray-200 hover:shadow-medium transition-shadow">
    <img
      :src="user.faceUrl || defaultFace"
      :alt="user.name"
      class="w-14 h-14 rounded-full object-cover border-2 border-accent"
    />
    <div class="flex-1 grid grid-cols-6 gap-2 text-sm">
      <div class="col-span-2 font-semibold text-text-main cursor-pointer hover:text-primary transition-colors font-gowun" @click="$emit('open')">
        {{ user.name }}
      </div>
      <div class="text-text-sub">
        <span class="text-xs text-gray-500">코드:</span>
        <span class="font-bold text-primary">{{ user.userCode || '-' }}</span>
      </div>
      <div class="text-text-sub">{{ user.height }}cm</div>
      <div class="text-text-sub">{{ user.age }}세</div>
      <div class="text-text-sub">{{ user.gender }}</div>
    </div>
    <button
      @click="$emit('delete')"
      class="px-3 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg transition-colors font-gowun"
    >
      회원 삭제
    </button>
  </div>
</template>

<script setup lang="ts">
import type { User } from '@/types/user'
import defaultFace from '@/assets/images/default-face.png'

defineProps<{ user: User }>()
defineEmits<{ open: []; delete: [] }>()
</script>

