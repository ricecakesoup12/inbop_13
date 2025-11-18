<template>
  <button :type="type" :class="buttonClasses" :disabled="disabled">
    <slot />
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    type?: 'button' | 'submit' | 'reset'
    variant?: 'solid' | 'outline' | 'ghost'
    disabled?: boolean
  }>(),
  {
    type: 'button',
    variant: 'solid',
    disabled: false,
  }
)

const buttonClasses = computed(() => {
  const base = 'inline-flex items-center justify-center gap-2 px-4 py-3 rounded-xl font-semibold text-sm transition-all duration-200 min-h-[44px] font-gowun'
  
  const variants = {
    solid: 'bg-primary text-white hover:bg-primary-hover hover:shadow-md active:scale-95',
    outline: 'border-2 border-primary text-primary hover:bg-primary hover:text-white',
    ghost: 'border border-gray-300 text-text-main hover:bg-gray-100',
  }
  
  const disabledClass = props.disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'
  
  return `${base} ${variants[props.variant]} ${disabledClass}`
})
</script>

