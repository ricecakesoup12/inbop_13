<template>
  <div class="AppCard">
    <slot />
    <div v-if="$slots.footer" class="AppCardFooter">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts"></script>

