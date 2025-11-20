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
        const { latitude, longitude, accuracy } = pos.coords
        
        // ✅ 정확도 필터링: 50m 이상은 무시 (더 엄격한 기준)
        const MAX_ACCURACY = 50
        if (accuracy && accuracy > MAX_ACCURACY) {
          console.warn(`⚠️ useGeo: 정확도 너무 낮음 (${accuracy}m > ${MAX_ACCURACY}m) - 위치 업데이트 스킵`)
          loading.value = false
          return
        }
        
        console.log(`✅ useGeo: 정확도 양호 - ${accuracy}m`)
        position.value = {
          lat: latitude,
          lng: longitude,
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

    let initialPositionSet = false // 최초 위치 설정 플래그
    let bestAccuracy = Infinity // 최고 정확도 기록

    const watchId = navigator.geolocation.watchPosition(
      (pos) => {
        const { latitude, longitude, accuracy } = pos.coords
        
        // ✅ 정확도 필터링: 최초 1회는 무조건 표시, 이후는 50m 이내 또는 정확도 개선 시만
        const MAX_ACCURACY = 50
        
        if (!initialPositionSet) {
          // 최초 1회는 정확도와 관계없이 표시
          console.log(`🎯 useGeo: 최초 위치 설정 - 정확도: ${accuracy}m (정확도 무관하게 표시)`)
          initialPositionSet = true
          bestAccuracy = accuracy || Infinity
        } else if (accuracy && accuracy > MAX_ACCURACY && accuracy >= bestAccuracy) {
          // 이미 위치가 설정되었고, 정확도가 기준보다 나쁘고, 개선되지도 않았으면 스킵
          console.warn(`⚠️ useGeo.watchPosition: 정확도 너무 낮음 (${accuracy}m > ${MAX_ACCURACY}m) - 위치 업데이트 스킵`)
          return
        } else if (accuracy && accuracy < bestAccuracy) {
          // 정확도가 개선되었으면 기록 갱신
          console.log(`📈 useGeo: 정확도 개선! ${bestAccuracy}m → ${accuracy}m`)
          bestAccuracy = accuracy
        } else {
          console.log(`✅ useGeo.watchPosition: 정확도 양호 - ${accuracy}m`)
        }
        
        const newPos = {
          lat: latitude,
          lng: longitude,
        }
        position.value = newPos
        callback(newPos)
      },
      (err) => {
        error.value = err.message
      },
      {
        enableHighAccuracy: true,
        timeout: 15000,
        maximumAge: 5000,
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

