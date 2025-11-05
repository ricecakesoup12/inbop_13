<template>
  <div :class="wrapperClass">
    <div ref="mapRef" class="w-full h-full rounded-xl border border-gray-200"></div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch, computed } from 'vue'
import { loadNaverMap } from '@/utils/loadNaverMap'

const props = withDefaults(
  defineProps<{
    position?: { lat: number; lng: number } | null
    small?: boolean
    userName?: string
  }>(),
  { small: false, userName: '사용자' }
)

const mapRef = ref<HTMLDivElement | null>(null)
let map: any = null
let marker: any = null

const wrapperClass = computed(() => (props.small ? 'h-40' : 'h-64'))

const NAVER_ID = import.meta.env.VITE_NAVER_CLIENT_ID as string

onMounted(async () => {
  if (!mapRef.value) return

  // 클라이언트 ID 확인
  if (!NAVER_ID || NAVER_ID === '여기에_네이버_클라이언트ID_입력' || NAVER_ID.trim() === '') {
    console.error('VITE_NAVER_CLIENT_ID가 설정되지 않음');
    return;
  }

  try {
    // 네이버 지도 스크립트 로드
    await loadNaverMap(NAVER_ID, 'geocoder');
    
    const nmaps = (window as any).naver?.maps;
    if (!nmaps) {
      console.error('네이버 지도 로드 실패');
      return;
    }

    const defaultLat = props.position?.lat || 37.5665
    const defaultLng = props.position?.lng || 126.978

    // 지도 생성
    map = new nmaps.Map(mapRef.value, {
      center: new nmaps.LatLng(defaultLat, defaultLng),
      zoom: 15,
      zoomControl: true,
      zoomControlOptions: {
        position: nmaps.Position.TOP_RIGHT
      }
    });

    // 마커 생성 (빨간색)
    marker = new nmaps.Marker({
      position: new nmaps.LatLng(defaultLat, defaultLng),
      map: map,
      icon: {
        content: `<div style="background: #FF6B6B; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
        anchor: new nmaps.Point(10, 10)
      },
      title: props.userName
    });

    console.log('✅ 네이버 지도 생성 완료 (UserDetail)');
  } catch (error) {
    console.error('❌ 네이버 지도 로드 실패:', error);
  }
})

watch(
  () => props.position,
  (newPos) => {
    if (map && marker && newPos) {
      const nmaps = (window as any).naver?.maps;
      if (nmaps) {
        const latlng = new nmaps.LatLng(newPos.lat, newPos.lng);
        map.setCenter(latlng);
        marker.setPosition(latlng);
      }
    }
  }
)
</script>

<style scoped>
/* 네이버 지도 로고 숨기기 */
:deep(.naver-map-logo) {
  display: none;
}
</style>

