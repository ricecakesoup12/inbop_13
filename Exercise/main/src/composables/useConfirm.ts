import { useUiStore } from '@/stores/ui.store'

export function useConfirm() {
  const uiStore = useUiStore()

  const confirm = (
    title: string,
    message: string,
    onConfirm: () => void,
    onCancel?: () => void
  ) => {
    uiStore.showConfirm(title, message, onConfirm, onCancel)
  }

  return {
    confirm,
  }
}

