<template>
  <div class="bg-white rounded-xl shadow-soft border border-gray-200 overflow-hidden">
    <slot />
    <div v-if="$slots.footer" class="border-t border-gray-200 bg-beige p-4">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts"></script>

