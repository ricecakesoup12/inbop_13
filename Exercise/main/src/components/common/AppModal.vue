<template>
  <Teleport to="body">
    <Transition name="AppModalFade">
      <div
        v-if="open"
        class="AppModalOverlay"
        @click.self="$emit('close')"
      >
        <div :class="['AppModalContainer', wide ? 'AppModalContainerWide' : 'AppModalContainerNormal']">
          <div class="AppModalHeader">
            <h3 class="AppModalTitle">{{ title }}</h3>
            <button
              @click="$emit('close')"
              class="AppModalCloseButton"
            >
              <svg class="AppModalCloseButtonIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <div class="AppModalBody">
            <slot />
          </div>
          <div v-if="$slots.footer" class="AppModalFooter">
            <slot name="footer" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
defineProps<{ open: boolean; title?: string; wide?: boolean }>()
defineEmits<{ close: [] }>()
</script>

<style scoped>
.AppModalFadeEnterActive,
.AppModalFadeLeaveActive {
  transition: opacity 0.3s ease;
}

.AppModalFadeEnterFrom,
.AppModalFadeLeaveTo {
  opacity: 0;
}

.AppModalFadeEnterActive .AppModalContainer,
.AppModalFadeLeaveActive .AppModalContainer {
  transition: transform 0.3s ease;
}

.AppModalFadeEnterFrom .AppModalContainer,
.AppModalFadeLeaveTo .AppModalContainer {
  transform: scale(0.95);
}
</style>

