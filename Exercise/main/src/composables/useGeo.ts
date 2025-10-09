import { ref, onMounted } from 'vue'

export function useGeo() {
  const position = ref<{ lat: number; lng: number } | null>(null)
  const error = ref<string | null>(null)
  const loading = ref(false)

  const getCurrentPosition = () => {
    if (!navigator.geolocation) {
      error.value = '위치 정보를 지원하지 않는 브라우저입니다.'
      return
    }

    loading.value = true

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        position.value = {
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
        }
        loading.value = false
      },
      (err) => {
        error.value = err.message
        loading.value = false
      },
      {
        enableHighAccuracy: true,
        timeout: 5000,
        maximumAge: 0,
      }
    )
  }

  const watchPosition = (callback: (pos: { lat: number; lng: number }) => void) => {
    if (!navigator.geolocation) {
      error.value = '위치 정보를 지원하지 않는 브라우저입니다.'
      return null
    }

    const watchId = navigator.geolocation.watchPosition(
      (pos) => {
        const newPos = {
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
        }
        position.value = newPos
        callback(newPos)
      },
      (err) => {
        error.value = err.message
      }
    )

    return watchId
  }

  onMounted(() => {
    getCurrentPosition()
  })

  return {
    position,
    error,
    loading,
    getCurrentPosition,
    watchPosition,
  }
}

