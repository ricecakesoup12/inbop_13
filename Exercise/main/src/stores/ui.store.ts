import { defineStore } from 'pinia'

export const useUiStore = defineStore('ui', {
  state: () => ({
    loading: false,
    surveyModalOpen: false,
    confirmDialog: {
      open: false,
      title: '',
      message: '',
      onConfirm: null as (() => void) | null,
      onCancel: null as (() => void) | null,
    },
    snackbar: {
      open: false,
      message: '',
      type: 'info' as 'info' | 'success' | 'error' | 'warning',
    },
  }),

  actions: {
    setLoading(loading: boolean) {
      this.loading = loading
    },

    openSurveyModal() {
      this.surveyModalOpen = true
    },

    closeSurveyModal() {
      this.surveyModalOpen = false
    },

    showConfirm(title: string, message: string, onConfirm: () => void, onCancel?: () => void) {
      this.confirmDialog = {
        open: true,
        title,
        message,
        onConfirm,
        onCancel: onCancel || null,
      }
    },

    hideConfirm() {
      this.confirmDialog.open = false
    },

    showSnackbar(message: string, type: 'info' | 'success' | 'error' | 'warning' = 'info') {
      this.snackbar = { open: true, message, type }
      setTimeout(() => {
        this.snackbar.open = false
      }, 3000)
    },
  },
})

