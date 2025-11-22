<template>
  <div class="RouteMapPage">
    <!-- 출발지/도착지 정보 표시 (읽기 전용) -->
    <div class="RouteMapHeader">
      <div class="RouteMapInfo">
        <div class="RouteMapLabel">출발지</div>
        <div class="RouteMapValue">{{ startAddress || start || '-' }}</div>
      </div>
      <div class="RouteMapInfo">
        <div class="RouteMapLabel">도착지</div>
        <div class="RouteMapValue">{{ endAddress || end || '-' }}</div>
      </div>
      <div v-if="waypoint" class="RouteMapInfo">
        <div class="RouteMapLabel">경유지</div>
        <div class="RouteMapValue">{{ waypointAddress || waypoint }}</div>
      </div>
    <div v-if="loading" class="RouteMapLoading">
      경로 불러오는 중...
    </div>
    </div>

    <!-- 운동 타이머 컨트롤 -->
    <div v-if="walkingMinutes || runningMinutes" class="RouteMapTimerSection">
      <div class="RouteMapTimerTitle">
        인터벌 운동
        <span v-if="totalSets > 0" class="RouteMapTimerSetInfo">
          ({{ currentSet }}/{{ totalSets }}세트)
        </span>
        <span v-if="allSetsCompleted" class="RouteMapTimerAllCompleted">
          🎉 모든 세트 완료!
        </span>
      </div>
      <div class="RouteMapTimerControls">
        <!-- 뛰기 버튼 (주황색) - 먼저 표시 -->
        <div class="RouteMapTimerItem">
          <button
            @click="startRunning"
            :disabled="isWalking || isRunning || runningCompleted || allSetsCompleted"
            :class="['RouteMapTimerButton', 'RouteMapTimerButtonRunning', { 'RouteMapTimerButtonActive': isRunning, 'RouteMapTimerButtonCompleted': runningCompleted }]"
          >
            뛰기
          </button>
          <div v-if="isRunning || runningCompleted" class="RouteMapTimerDisplay">
            <span v-if="runningCompleted" class="RouteMapTimerCompleted">완료</span>
            <span v-else class="RouteMapTimerTime">{{ formatTime(runningTimeLeft) }}</span>
          </div>
        </div>

        <!-- 걷기 버튼 (초록색) - 나중에 표시 -->
        <div class="RouteMapTimerItem">
          <button
            @click="startWalking"
            :disabled="isWalking || isRunning || walkingCompleted || !runningCompleted || allSetsCompleted"
            :class="['RouteMapTimerButton', 'RouteMapTimerButtonWalking', { 'RouteMapTimerButtonActive': isWalking, 'RouteMapTimerButtonCompleted': walkingCompleted }]"
          >
            걷기
          </button>
          <div v-if="isWalking || walkingCompleted" class="RouteMapTimerDisplay">
            <span v-if="walkingCompleted" class="RouteMapTimerCompleted">완료</span>
            <span v-else class="RouteMapTimerTime">{{ formatTime(walkingTimeLeft) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 지도가 들어갈 영역 (화면 거의 꽉 채움) -->
    <div ref="mapEl" class="RouteMapFull"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { loadNaverMap } from '@/utils/loadNaverMap'
import { http } from '@/services/api/http'

// ====== 반응형 상태 ======
const route = useRoute()
const start = ref('')
const end = ref('')
const waypoint = ref('')
const startAddress = ref('')
const endAddress = ref('')
const waypointAddress = ref('')
const mapEl = ref<HTMLDivElement | null>(null)
const map = ref<any>(null)
const polyline = ref<any>(null)
const markers = ref<any[]>([])
const points = ref<Array<{ lat: number; lng: number }>>([])
const loading = ref(false)

// 운동 타이머 관련
const walkingMinutes = ref<number>(0)
const runningMinutes = ref<number>(0)
const totalSets = ref<number>(0)
const currentSet = ref<number>(1)
const isWalking = ref(false)
const isRunning = ref(false)
const walkingTimeLeft = ref<number>(0) // 초 단위
const runningTimeLeft = ref<number>(0) // 초 단위
const walkingCompleted = ref(false)
const runningCompleted = ref(false)
const allSetsCompleted = ref(false)
let walkingTimer: number | null = null
let runningTimer: number | null = null

const NAVER_ID = import.meta.env.VITE_NAVER_CLIENT_ID as string

// ====== 1. 지도 초기화 ======
const initMap = async () => {
  if (!mapEl.value) {
    console.error('지도 컨테이너를 찾을 수 없습니다.')
    return
  }

  if (!NAVER_ID || NAVER_ID === '여기에_네이버_클라이언트ID_입력' || NAVER_ID.trim() === '') {
    console.error('VITE_NAVER_CLIENT_ID가 설정되지 않음')
    if (mapEl.value) {
      mapEl.value.innerHTML = `
        <div style="display: flex; align-items: center; justify-content: center; height: 100%; flex-direction: column; gap: 10px; padding: 20px; background: #f3f4f6; border-radius: 8px;">
          <p style="color: #666; font-size: 14px; font-weight: 600;">⚠️ 네이버 지도 API 설정 필요</p>
          <p style="color: #999; font-size: 12px; text-align: center;">.env 파일에 VITE_NAVER_CLIENT_ID를 설정해주세요</p>
        </div>
      `
    }
    return
  }

  try {
    // 네이버 지도 스크립트 로드
    await loadNaverMap(NAVER_ID, 'geocoder')
    
    const nmaps = (window as any).naver?.maps
    if (!nmaps) {
      console.error('⚠️ 네이버 지도 로드 실패')
      if (mapEl.value) {
        mapEl.value.innerHTML = `
          <div style="display: flex; align-items: center; justify-content: center; height: 100%; flex-direction: column; gap: 10px; padding: 20px; background: #f3f4f6; border-radius: 8px;">
            <p style="color: #666; font-size: 14px; font-weight: 600;">⚠️ 지도 초기화 실패</p>
            <p style="color: #999; font-size: 12px; text-align: center;">네이버 지도 API를 불러올 수 없습니다</p>
          </div>
        `
      }
      return
    }

    // 초기 중심(서울 시청 근처)
    const center = new nmaps.LatLng(37.5665, 126.9780)
    map.value = new nmaps.Map(mapEl.value, {
      center,
      zoom: 13,
      zoomControl: true,
      zoomControlOptions: {
        position: nmaps.Position.TOP_RIGHT
      }
    })

    console.log('✅ 네이버 지도 초기화 완료')
  } catch (error: any) {
    console.error('❌ 네이버 지도 초기화 실패:', error)
    if (mapEl.value) {
      mapEl.value.innerHTML = `
        <div style="display: flex; align-items: center; justify-content: center; height: 100%; flex-direction: column; gap: 10px; padding: 20px; background: #f3f4f6; border-radius: 8px;">
          <p style="color: #666; font-size: 14px; font-weight: 600;">⚠️ 지도 로드 실패</p>
          <p style="color: #999; font-size: 12px; text-align: center;">${error?.message || '네이버 지도 API를 불러올 수 없습니다'}</p>
        </div>
      `
    }
  }
}

// ====== 지오코딩: 주소를 좌표로 변환 ======
const geocode = async (address: string): Promise<string | null> => {
  // 이미 좌표 형식이면 그대로 반환
  if (parseCoordinates(address)) {
    return address
  }

  try {
    const nmaps = (window as any).naver?.maps
    if (!nmaps || !nmaps.Service || typeof nmaps.Service.geocode !== 'function') {
      console.warn('지오코딩 API를 사용할 수 없습니다.')
      return null
    }

    return new Promise((resolve) => {
      nmaps.Service.geocode(
        {
          query: address,
          coordType: nmaps.Service.CoordType.NAVER
        },
        (status: any, response: any) => {
          const isOK = status === nmaps.Service.Status.OK || 
                      status === 0 || 
                      (typeof status === 'string' && status.toLowerCase() === 'ok')
          
          if (isOK && response?.v2?.addresses && response.v2.addresses.length > 0) {
            const addr = response.v2.addresses[0]
            const lng = addr.x
            const lat = addr.y
            resolve(`${lng},${lat}`)
          } else {
            console.warn('지오코딩 실패:', address)
            resolve(null)
          }
        }
      )
    })
  } catch (error) {
    console.error('지오코딩 오류:', error)
    return null
  }
}

// ====== 2. 백엔드에서 경로 가져오기 ======
const loadRoute = async () => {
  if (!start.value || !end.value) {
    console.warn('출발지 또는 도착지가 없습니다.')
    return
  }

  if (!map.value) {
    console.warn('지도가 초기화되지 않았습니다. 잠시 후 다시 시도해주세요.')
    return
  }

  loading.value = true

  try {
    // 주소를 좌표로 변환 (필요한 경우)
    let startCoord = start.value
    let endCoord = end.value
    
    // 출발지가 좌표 형식이 아니면 지오코딩
    if (!parseCoordinates(start.value)) {
      const geocoded = await geocode(start.value)
      if (geocoded) {
        startCoord = geocoded
      }
    }
    
    // 도착지가 좌표 형식이 아니면 지오코딩
    if (!parseCoordinates(end.value)) {
      const geocoded = await geocode(end.value)
      if (geocoded) {
        endCoord = geocoded
      }
    }

    const params: Record<string, string> = {
      start: startCoord,
      goal: endCoord,
      option: 'trafast'
    }

    if (waypoint.value) {
      let waypointCoord = waypoint.value
      if (!parseCoordinates(waypoint.value)) {
        const geocoded = await geocode(waypoint.value)
        if (geocoded) {
          waypointCoord = geocoded
        }
      }
      params.waypoint = waypointCoord
    }

    const response = await http.get('/api/path/driving', { params })
    
    // 백엔드 응답 파싱 (네이버 API 응답 형태)
    const routeData = typeof response.data === 'string' ? JSON.parse(response.data) : response.data
    
    // 경로 좌표 추출
    const extractedPoints = extractPathPoints(routeData)
    
    if (!extractedPoints || extractedPoints.length === 0) {
      console.error('경로를 찾지 못했습니다.')
      if (mapEl.value) {
        mapEl.value.innerHTML = `
          <div style="display: flex; align-items: center; justify-content: center; height: 100%; flex-direction: column; gap: 10px; padding: 20px; background: #f3f4f6;">
            <p style="color: #666; font-size: 14px; font-weight: 600;">⚠️ 경로를 찾을 수 없습니다</p>
            <p style="color: #999; font-size: 12px; text-align: center;">출발지와 도착지를 확인해주세요</p>
          </div>
        `
      }
      loading.value = false
      return
    }

    points.value = extractedPoints
    drawRoute()
  } catch (error: any) {
    console.error('경로 로드 실패:', error)
    if (mapEl.value) {
      mapEl.value.innerHTML = `
        <div style="display: flex; align-items: center; justify-content: center; height: 100%; flex-direction: column; gap: 10px; padding: 20px; background: #f3f4f6;">
          <p style="color: #666; font-size: 14px; font-weight: 600;">⚠️ 경로 로드 실패</p>
          <p style="color: #999; font-size: 12px; text-align: center;">${error?.response?.data || error?.message || '알 수 없는 오류'}</p>
        </div>
      `
    }
  } finally {
    loading.value = false
  }
}

// ====== 좌표 문자열 파싱 ======
const parseCoordinates = (coordString: string): { lat: number; lng: number } | null => {
  if (!coordString) return null
  
  // "126.844856,37.5407361" 형태의 문자열 파싱
  const parts = coordString.split(',').map(s => s.trim())
  if (parts.length !== 2) return null
  
  const lng = parseFloat(parts[0])
  const lat = parseFloat(parts[1])
  
  if (isNaN(lng) || isNaN(lat)) return null
  
  return { lat, lng }
}

// ====== 역지오코딩: 좌표를 주소로 변환 ======
const reverseGeocode = async (latitude: number, longitude: number): Promise<string | null> => {
  if (!isFinite(latitude) || !isFinite(longitude)) {
    return null
  }

  try {
    const nmaps = (window as any).naver?.maps
    
    if (!nmaps || !nmaps.Service) {
      return null
    }
    
    if (typeof nmaps.Service.reverseGeocode !== 'function') {
      return null
    }

    return new Promise((resolve) => {
      try {
        const latlng = new nmaps.LatLng(latitude, longitude)
        const reverseGeocodeOptions: any = {
          coords: latlng,
          orders: 'roadaddr,addr',
          lang: 'ko'
        }

        if (nmaps?.Service?.CoordType?.LAT_LNG) {
          reverseGeocodeOptions.coordType = nmaps.Service.CoordType.LAT_LNG
        } else if (nmaps?.Service?.CoordType?.NAVER) {
          reverseGeocodeOptions.coordType = nmaps.Service.CoordType.NAVER
        }

        nmaps.Service.reverseGeocode(
          reverseGeocodeOptions,
          (status: any, response: any) => {
            const isOK = status === nmaps.Service.Status.OK || 
                        status === 0 || 
                        (typeof status === 'string' && status.toLowerCase() === 'ok')
            
            if (isOK && response?.v2) {
              const v2: any = response.v2
              
              // 1) v2.address 우선 (신규 스펙)
              const direct = v2.address?.roadAddress || v2.address?.jibunAddress
              if (direct) {
                resolve(direct)
                return
              }
              
              // 2) v2.results 파싱 (roadaddr 우선)
              if (Array.isArray(v2.results) && v2.results.length > 0) {
                const preferred = v2.results.find((r: any) => r.name === 'roadaddr') || v2.results[0]
                const region = preferred.region || {}
                const land = preferred.land || {}
                const parts: string[] = []
                if (region.area1?.name) parts.push(region.area1.name)
                if (region.area2?.name) parts.push(region.area2.name)
                if (region.area3?.name) parts.push(region.area3.name)
                if (preferred.name === 'roadaddr' && land.name) {
                  parts.push(land.name)
                  if (land.number1) parts.push(land.number1 + (land.number2 ? '-' + land.number2 : ''))
                }
                if (land.addition0?.type === 'building' && land.addition0?.value) parts.push(land.addition0.value)
                const joined = parts.filter(Boolean).join(' ')
                if (joined) {
                  resolve(joined)
                  return
                }
              }
              
              // 3) 구형 스펙: v2.addresses
              if (Array.isArray(v2.addresses) && v2.addresses.length > 0) {
                const address = v2.addresses[0]
                const result = address.roadAddress || address.jibunAddress || address.address
                if (result) {
                  resolve(result)
                  return
                }
              }
              
              resolve(null)
            } else {
              resolve(null)
            }
          }
        )
      } catch (error) {
        console.error('역지오코딩 오류:', error)
        resolve(null)
      }
    })
  } catch (error) {
    console.error('역지오코딩 오류:', error)
    return null
  }
}

// ====== 좌표를 주소로 변환하는 헬퍼 함수 ======
const convertCoordinatesToAddresses = async () => {
  // 출발지 주소 변환
  if (start.value) {
    const startCoords = parseCoordinates(start.value)
    if (startCoords) {
      const address = await reverseGeocode(startCoords.lat, startCoords.lng)
      startAddress.value = address || start.value
    } else {
      // 좌표 형식이 아니면 그대로 표시 (주소 문자열인 경우)
      startAddress.value = start.value
    }
  }

  // 도착지 주소 변환
  if (end.value) {
    const endCoords = parseCoordinates(end.value)
    if (endCoords) {
      const address = await reverseGeocode(endCoords.lat, endCoords.lng)
      endAddress.value = address || end.value
    } else {
      // 좌표 형식이 아니면 그대로 표시 (주소 문자열인 경우)
      endAddress.value = end.value
    }
  }

  // 경유지 주소 변환
  if (waypoint.value) {
    const waypointCoords = parseCoordinates(waypoint.value)
    if (waypointCoords) {
      const address = await reverseGeocode(waypointCoords.lat, waypointCoords.lng)
      waypointAddress.value = address || waypoint.value
    } else {
      // 좌표 형식이 아니면 그대로 표시 (주소 문자열인 경우)
      waypointAddress.value = waypoint.value
    }
  }
}

// ====== 네이버 API 응답에서 경로 좌표 추출 ======
const extractPathPoints = (routeData: any): Array<{ lat: number; lng: number }> => {
  try {
    // 네이버 길찾기 API 응답 구조:
    // route.trafast[0].path 또는 route.trafast[0].section[].path
    if (!routeData || !routeData.route) {
      return []
    }

    const trafast = routeData.route.trafast
    if (!trafast || !Array.isArray(trafast) || trafast.length === 0) {
      return []
    }

    const firstRoute = trafast[0]
    
    // path 배열이 직접 있는 경우
    if (firstRoute.path && Array.isArray(firstRoute.path)) {
      return firstRoute.path.map((coord: number[]) => ({
        lng: coord[0], // 경도가 먼저
        lat: coord[1]  // 위도가 나중
      }))
    }

    // section 배열에서 path 추출
    if (firstRoute.section && Array.isArray(firstRoute.section)) {
      const allPoints: Array<{ lat: number; lng: number }> = []
      firstRoute.section.forEach((section: any) => {
        if (section.path && Array.isArray(section.path)) {
          section.path.forEach((coord: number[]) => {
            allPoints.push({
              lng: coord[0],
              lat: coord[1]
            })
          })
        }
      })
      return allPoints
    }

    return []
  } catch (error) {
    console.error('경로 좌표 추출 실패:', error)
    return []
  }
}

// ====== 3. 기존 오버레이(마커/라인) 지우기 ======
const clearOverlays = () => {
  markers.value.forEach((m) => m.setMap(null))
  markers.value = []
  if (polyline.value) {
    polyline.value.setMap(null)
    polyline.value = null
  }
}

// ====== 4. 지도에 경로 그리기 ======
const drawRoute = () => {
  if (!map.value || !points.value.length) return

  const nmaps = (window as any).naver?.maps
  if (!nmaps) {
    console.error('네이버 지도 API가 로드되지 않았습니다.')
    return
  }

  clearOverlays()

  // 백엔드에서 받은 좌표 → Naver LatLng 객체 배열로 변환
  const path = points.value.map(
    (p) => new nmaps.LatLng(p.lat, p.lng)
  )

  // --- 출발 마커 ---
  markers.value.push(
    new nmaps.Marker({
      position: path[0],
      map: map.value,
      title: '출발',
      icon: {
        content: `<div style="background: #4CAF50; width: 24px; height: 24px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; font-size: 12px;">S</div>`,
        anchor: new nmaps.Point(12, 12)
      }
    })
  )

  // --- 도착 마커 ---
  markers.value.push(
    new nmaps.Marker({
      position: path[path.length - 1],
      map: map.value,
      title: '도착',
      icon: {
        content: `<div style="background: #F44336; width: 24px; height: 24px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; font-size: 12px;">E</div>`,
        anchor: new nmaps.Point(12, 12)
      }
    })
  )

  // --- 경유지 마커 (중간 점들, 너무 많으면 일부만 표시) ---
  if (path.length > 2) {
    const waypointCount = Math.min(5, path.length - 2) // 최대 5개만 표시
    const step = Math.floor((path.length - 2) / waypointCount)
    
    for (let i = 1; i < path.length - 1; i += step) {
      if (markers.value.length >= 7) break // 출발/도착 포함 최대 7개
      
      markers.value.push(
        new nmaps.Marker({
          position: path[i],
          map: map.value,
          title: `경유지 ${Math.floor(i / step)}`,
          icon: {
            content: `<div style="background: #FF9800; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
            anchor: new nmaps.Point(10, 10)
          }
        })
      )
    }
  }

  // --- 경로 폴리라인(선) ---
  polyline.value = new nmaps.Polyline({
    map: map.value,
    path,
    strokeColor: '#4285F4',
    strokeWeight: 5,
    strokeOpacity: 0.9,
    strokeStyle: 'solid'
  })

  // --- 모든 경로가 화면에 보이게 bounds 맞추기 ---
  const bounds = new nmaps.LatLngBounds()
  path.forEach((pos) => bounds.extend(pos))
  map.value.fitBounds(bounds, { padding: 50 })
}

// ====== 운동 타이머 함수 ======
const formatTime = (seconds: number): string => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

const startRunning = () => {
  if (runningCompleted.value || isWalking.value || allSetsCompleted.value) return
  
  isRunning.value = true
  runningTimeLeft.value = runningMinutes.value * 60
  runningCompleted.value = false
  
  if (runningTimer) {
    clearInterval(runningTimer)
  }
  
  runningTimer = window.setInterval(() => {
    runningTimeLeft.value--
    
    if (runningTimeLeft.value <= 0) {
      clearInterval(runningTimer!)
      runningTimer = null
      isRunning.value = false
      runningCompleted.value = true
      runningTimeLeft.value = 0
      checkSetCompletion()
    }
  }, 1000)
}

const startWalking = () => {
  if (walkingCompleted.value || isRunning.value || !runningCompleted.value || allSetsCompleted.value) return
  
  isWalking.value = true
  walkingTimeLeft.value = walkingMinutes.value * 60
  walkingCompleted.value = false
  
  if (walkingTimer) {
    clearInterval(walkingTimer)
  }
  
  walkingTimer = window.setInterval(() => {
    walkingTimeLeft.value--
    
    if (walkingTimeLeft.value <= 0) {
      clearInterval(walkingTimer!)
      walkingTimer = null
      isWalking.value = false
      walkingCompleted.value = true
      walkingTimeLeft.value = 0
      checkSetCompletion()
    }
  }, 1000)
}

// 세트 완료 확인 및 다음 세트로 진행
const checkSetCompletion = () => {
  // 뛰기와 걷기가 모두 완료되었는지 확인
  if (runningCompleted.value && walkingCompleted.value) {
    // 현재 세트가 마지막 세트인지 확인
    if (currentSet.value >= totalSets.value) {
      // 모든 세트 완료
      allSetsCompleted.value = true
      console.log('🎉 모든 세트 완료!')
    } else {
      // 다음 세트로 진행
      currentSet.value++
      // 타이머 초기화
      runningCompleted.value = false
      walkingCompleted.value = false
      runningTimeLeft.value = 0
      walkingTimeLeft.value = 0
      console.log(`✅ ${currentSet.value - 1}세트 완료! 다음 세트 시작: ${currentSet.value}/${totalSets.value}`)
    }
  }
}

// 컴포넌트 언마운트 시 타이머 정리
onUnmounted(() => {
  if (walkingTimer) {
    clearInterval(walkingTimer)
  }
  if (runningTimer) {
    clearInterval(runningTimer)
  }
})

// 컴포넌트 마운트 시 쿼리 파라미터 읽고 자동으로 경로 로드
onMounted(async () => {
  // 1) 쿼리에서 값 꺼내기
  start.value = (route.query.start as string) || ''
  end.value = (route.query.end as string) || ''
  waypoint.value = (route.query.waypoint as string) || ''
  
  // 처방 정보 가져오기 (쿼리 파라미터 또는 localStorage에서)
  const walkingMins = route.query.walkingMinutes
  const runningMins = route.query.runningMinutes
  const sets = route.query.sets
  
  if (walkingMins) {
    walkingMinutes.value = parseInt(String(walkingMins), 10) || 0
  }
  if (runningMins) {
    runningMinutes.value = parseInt(String(runningMins), 10) || 0
  }
  if (sets) {
    totalSets.value = parseInt(String(sets), 10) || 0
  }
  
  // localStorage에서도 확인 (UserDashboard에서 저장한 경우)
  const userId = localStorage.getItem('userId')
  if (userId && (!walkingMins || !runningMins || !sets)) {
    try {
      const { getPrescriptionsByUser } = await import('@/services/api/exercisePrescriptions')
      const prescriptions = await getPrescriptionsByUser(userId)
      const activePrescription = prescriptions.find(p => p.status === 'ACCEPTED')
      if (activePrescription) {
        walkingMinutes.value = activePrescription.walkingMinutes || 0
        runningMinutes.value = activePrescription.runningMinutes || 0
        totalSets.value = activePrescription.sets || 0
      }
    } catch (error) {
      console.warn('처방 정보를 가져올 수 없습니다:', error)
    }
  }
  
  // 초기화
  currentSet.value = 1
  allSetsCompleted.value = false

  // 2) 지도 먼저 초기화
  await initMap()

  // 3) 좌표를 주소로 변환 (지도 초기화 후에 해야 함)
  await convertCoordinatesToAddresses()

  // 4) 출발/도착 값 있으면 바로 경로 로드
  if (start.value && end.value) {
    await loadRoute()
  }
})
</script>

<style scoped>
.RouteMapPage {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
}

.RouteMapHeader {
  padding: 12px 16px;
  background-color: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.RouteMapInfo {
  min-width: 0;
  flex: 1;
  max-width: 300px;
}

.RouteMapLabel {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 4px;
  font-weight: 500;
}

.RouteMapValue {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  word-break: break-all;
  line-height: 1.4;
}

.RouteMapLoading {
  font-size: 14px;
  color: var(--color-primary);
  font-weight: 500;
  padding: 8px 16px;
  background-color: #f0fdf4;
  border-radius: 8px;
  margin-left: auto;
}

.RouteMapTimerSection {
  padding: 16px;
  background-color: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.RouteMapTimerTitle {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-main);
  margin-bottom: 12px;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}

.RouteMapTimerSetInfo {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-sub);
}

.RouteMapTimerAllCompleted {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-primary);
  margin-top: 4px;
}

.RouteMapTimerControls {
  display: flex;
  gap: 16px;
  justify-content: center;
  align-items: center;
}

.RouteMapTimerItem {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.RouteMapTimerButton {
  padding: 12px 24px;
  border: 2px solid;
  border-radius: 0.75rem;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 100px;
  font-family: 'Gowun Dodum', sans-serif;
}

.RouteMapTimerButton:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.RouteMapTimerButtonWalking {
  background-color: #22C55E;
  color: #FFFFFF;
  border-color: #22C55E;
}

.RouteMapTimerButtonWalking:hover:not(:disabled) {
  background-color: #16A34A;
  border-color: #16A34A;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
}

.RouteMapTimerButtonWalking.RouteMapTimerButtonActive {
  background-color: #16A34A;
  border-color: #16A34A;
  box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.2);
}

.RouteMapTimerButtonWalking.RouteMapTimerButtonCompleted {
  background-color: #D1D5DB;
  border-color: #D1D5DB;
  color: #6B7280;
}

.RouteMapTimerButtonRunning {
  background-color: #FF9800;
  color: #FFFFFF;
  border-color: #FF9800;
}

.RouteMapTimerButtonRunning:hover:not(:disabled) {
  background-color: #F57C00;
  border-color: #F57C00;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
}

.RouteMapTimerButtonRunning.RouteMapTimerButtonActive {
  background-color: #F57C00;
  border-color: #F57C00;
  box-shadow: 0 0 0 3px rgba(255, 152, 0, 0.2);
}

.RouteMapTimerButtonRunning.RouteMapTimerButtonCompleted {
  background-color: #D1D5DB;
  border-color: #D1D5DB;
  color: #6B7280;
}

.RouteMapTimerDisplay {
  font-size: 1.5rem;
  font-weight: 700;
  font-family: 'Gowun Dodum', sans-serif;
  min-height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.RouteMapTimerTime {
  color: var(--text-main);
}

.RouteMapTimerCompleted {
  color: var(--color-primary);
}

.RouteMapFull {
  flex: 1;
  min-height: 0;
  width: 100%;
  position: relative;
  background: #f3f4f6;
}

@media (max-width: 768px) {
  .RouteMapHeader {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .RouteMapInfo {
    max-width: 100%;
  }

  .RouteMapLoading {
    margin-left: 0;
    text-align: center;
  }
}
</style>
