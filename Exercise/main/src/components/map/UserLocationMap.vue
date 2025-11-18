<template>
  <div :class="wrapperClass">
    <div ref="mapRef" class="w-full h-full rounded-xl border border-gray-200"></div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch, computed } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const props = withDefaults(
  defineProps<{
    position?: { lat: number; lng: number } | null
    small?: boolean
  }>(),
  { small: false }
)

const mapRef = ref<HTMLDivElement | null>(null)
let map: L.Map | null = null
let marker: L.Marker | null = null

const wrapperClass = computed(() => (props.small ? 'h-40' : 'h-64'))

onMounted(() => {
  if (!mapRef.value) return

  const defaultLat = props.position?.lat || 37.5665
  const defaultLng = props.position?.lng || 126.978

  map = L.map(mapRef.value).setView([defaultLat, defaultLng], 15)

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '© OpenStreetMap',
  }).addTo(map)

  // 마커 아이콘 설정 (Leaflet 기본 아이콘 경로 문제 해결)
  const icon = L.icon({
    iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
    iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
    shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
  })

  marker = L.marker([defaultLat, defaultLng], { icon }).addTo(map)
})

watch(
  () => props.position,
  (newPos) => {
    if (map && newPos) {
      map.setView([newPos.lat, newPos.lng], map.getZoom())
      if (marker) {
        marker.setLatLng([newPos.lat, newPos.lng])
      }
    }
  }
)
</script>

<style>
@import 'leaflet/dist/leaflet.css';
</style>

