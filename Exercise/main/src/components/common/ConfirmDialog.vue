<template>
  <AppModal :open="open" :title="title" @close="$emit('cancel')">
    <p class="text-text-sub mb-4 font-gowun">{{ message }}</p>
    <template #footer>
      <AppButton variant="ghost" @click="$emit('cancel')">취소</AppButton>
      <AppButton variant="solid" @click="$emit('confirm')">확인</AppButton>
    </template>
  </AppModal>
</template>

<script setup lang="ts">
import AppModal from './AppModal.vue'
import AppButton from './AppButton.vue'

defineProps<{
  open: boolean
  title: string
  message: string
}>()

defineEmits<{
  confirm: []
  cancel: []
}>()
</script>

