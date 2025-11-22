<template>
  <div class="UserListItem">
    <img
      :src="user.faceUrl || defaultFace"
      :alt="user.name"
      class="UserListItemImage"
    />
    <div class="UserListItemInfo">
      <div class="UserListItemName" @click="$emit('open')">
        {{ user.name }}
      </div>
      <div class="UserListItemCode">
        <span class="UserListItemCodeLabel">코드:</span>
        <span class="UserListItemCodeValue">{{ user.userCode || '-' }}</span>
      </div>
      <div class="UserListItemHeight">{{ user.height }}cm</div>
      <div class="UserListItemAge">{{ user.age }}세</div>
      <div class="UserListItemGender">{{ user.gender }}</div>
    </div>
    <!-- 위치 정보 표시 -->
    <div v-if="userLocation" class="UserListItemLocation">
      <div class="UserListItemLocationContent">
        <span class="UserListItemLocationDot"></span>
        <span class="UserListItemLocationText">위치 추적 중</span>
      </div>
    </div>
    <div v-else class="UserListItemNoLocation">
      <span>위치 없음</span>
    </div>
    <button
      @click="$emit('delete')"
      class="UserListItemDeleteButton"
    >
      회원 삭제
    </button>
  </div>
</template>

<script setup lang="ts">
import type { User } from '@/types/user'
import defaultFace from '@/assets/images/default-face.png'

defineProps<{ user: User; userLocation?: { lat: number; lng: number } | null }>()
defineEmits<{ open: []; delete: [] }>()
</script>

