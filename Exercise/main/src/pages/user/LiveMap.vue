<template>
  <div class="space-y-4">
    <div class="bg-white rounded-2xl shadow-soft border border-gray-200 p-6">
      <h3 class="font-semibold text-text-main mb-4 flex items-center gap-2 font-gowun">
        <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
        </svg>
        실시간 위치 추적
      </h3>
      
      <!-- 상태 표시 -->
      <div class="mb-4 flex items-center gap-2">
        <div :class="isTracking ? 'bg-green-500' : 'bg-gray-400'" class="w-3 h-3 rounded-full animate-pulse"></div>
        <span class="text-sm text-text-sub font-gowun">
          {{ isTracking ? '위치 추적 중' : '위치 추적 대기 중' }}
        </span>
        <span class="text-xs text-gray-400 font-gowun ml-auto">
          {{ otherUsersCount }}명의 다른 사용자
        </span>
      </div>

      <!-- 지도 -->
      <div id="map" class="w-full h-[60vh] rounded-xl border border-gray-200 shadow-sm"></div>

      <!-- 현재 위치 정보 (항상 도로명 주소만 표시) -->
      <div class="mt-4 p-3 bg-beige rounded-lg">
        <div class="text-xs text-text-sub font-gowun">현재 위치</div>
        <div class="text-sm text-text-main font-gowun mt-1">
          {{ currentAddress || '주소를 가져오는 중...' }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue';
import { loadNaverMap } from '@/utils/loadNaverMap';
import { upsertLocation, getAllLocations } from '@/services/api/locations';

const NAVER_ID = import.meta.env.VITE_NAVER_CLIENT_ID as string;

const map = ref<any>(null);
const myMarker = ref<any>(null);
const otherMarkers = ref<Map<string, any>>(new Map());
const isTracking = ref(false);
const currentPosition = ref<{ latitude: number; longitude: number } | null>(null);
const currentAddress = ref<string | null>(null);
const otherUsersCount = ref(0);
let watchId: number | null = null;
let sendLocationInterval: number | null = null;
let fetchLocationsInterval: number | null = null;
let userId: string | null = null;

// OSM Nominatim Fallback (비인증, 도로명 유사 주소)
const fallbackReverseGeocode = async (latitude: number, longitude: number): Promise<string | null> => {
  try {
    const url = `https://nominatim.openstreetmap.org/reverse?lat=${latitude}&lon=${longitude}&format=json&accept-language=ko`;
    const res = await fetch(url, { headers: { 'User-Agent': 'inbop-app/1.0 (educational)' } });
    if (!res.ok) return null;
    const data = await res.json();
    return data.display_name || null;
  } catch {
    return null;
  }
};

// Reverse Geocoding: 좌표를 주소로 변환 (도로명 주소 우선)
const reverseGeocode = async (latitude: number, longitude: number): Promise<string | null> => {
  try {
    const nmaps = (window as any).naver?.maps;
    
    if (!nmaps || !nmaps.Service) {
      console.error('네이버 지도 Service가 로드되지 않았습니다. (인증 실패 가능)');
      return null;
    }
    
    // 역지오코딩 기능이 비활성(인증 실패 등)인 경우 바로 중단
    if (typeof nmaps.Service.reverseGeocode !== 'function') {
      console.warn('reverseGeocode 사용 불가: 인증 실패 또는 권한 미설정 → OSM Fallback 시도');
      return await fallbackReverseGeocode(latitude, longitude);
    }

    return new Promise((resolve) => {
      nmaps.Service.reverseGeocode(
        { coords: new nmaps.LatLng(latitude, longitude) },
        (status: any, response: any) => {
          if (status === nmaps.Service.Status.OK && response.v2.addresses.length > 0) {
            const address = response.v2.addresses[0];
            // 도로명 주소를 우선적으로 사용, 없으면 지번 주소 사용
            const result = address.roadAddress || address.jibunAddress;
            console.log('주소 변환 성공:', result);
            resolve(result);
          } else {
            console.warn('주소 변환 실패:', status, '→ OSM Fallback 시도');
            fallbackReverseGeocode(latitude, longitude).then(resolve);
          }
        }
      );
    });
  } catch (error) {
    console.error('Reverse Geocoding 실패:', error);
    // 네이버 실패 시 OSM로 재시도
    return await fallbackReverseGeocode(latitude, longitude);
  }
};

// 내 위치를 서버에 전송
const sendMyLocation = async (latitude: number, longitude: number) => {
  if (!userId) {
    userId = localStorage.getItem('userId') || import.meta.env.VITE_USER_ID;
  }
  
  if (!userId) {
    console.warn('사용자 ID가 없습니다.');
    return;
  }

  try {
    await upsertLocation(userId, {
      latitude,
      longitude,
      timestamp: Date.now()
    });
    console.log('위치 전송 완료:', latitude, longitude);
  } catch (error) {
    console.error('위치 전송 실패:', error);
  }
};

// 다른 사용자들의 위치를 가져와서 마커 표시
const fetchAndDisplayOtherUsers = async () => {
  try {
    const locations = await getAllLocations();
    
    if (!map.value || !userId) {
      userId = localStorage.getItem('userId') || import.meta.env.VITE_USER_ID;
      if (!userId) return;
    }

    const nmaps = (window as any).naver.maps;
    
    // 내 위치를 제외한 다른 사용자들만 표시
    const otherLocations = locations.filter(loc => loc.userId !== userId);
    otherUsersCount.value = otherLocations.length;

    // 기존 마커 제거 (삭제된 사용자들)
    otherMarkers.value.forEach((marker, key) => {
      if (!otherLocations.find(loc => loc.userId === key)) {
        marker.setMap(null);
        otherMarkers.value.delete(key);
      }
    });

    // 새 마커 추가 또는 위치 업데이트
    otherLocations.forEach((location) => {
      const latlng = new nmaps.LatLng(location.latitude, location.longitude);
      
      if (otherMarkers.value.has(location.userId)) {
        // 기존 마커 위치 업데이트
        otherMarkers.value.get(location.userId).setPosition(latlng);
      } else {
        // 새 마커 생성 (파란색)
        const marker = new nmaps.Marker({
          position: latlng,
          map: map.value,
          icon: {
            content: `<div style="background: #4ECDC4; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
            anchor: new nmaps.Point(10, 10)
          },
          title: location.userId
        });
        
        // 정보창 추가
        const infoWindow = new nmaps.InfoWindow({
          content: `<div style="padding: 8px; font-size: 12px;">${location.userId}</div>`
        });
        
        marker.addListener('click', () => {
          infoWindow.open(map.value, marker);
        });
        
        otherMarkers.value.set(location.userId, marker);
      }
    });
  } catch (error) {
    console.error('위치 조회 실패:', error);
  }
};

onMounted(async () => {
  userId = localStorage.getItem('userId') || import.meta.env.VITE_USER_ID;

  // 1. 클라이언트 ID 확인
  if (!NAVER_ID || NAVER_ID === '여기에_네이버_클라이언트ID_입력' || NAVER_ID.trim() === '') {
    const errorMsg = '❌ 네이버 지도 API 클라이언트 ID가 설정되지 않았습니다.\n\n📝 해결 방법:\n\n1️⃣ Exercise/main/.env 파일을 만들고 다음 내용 추가:\nVITE_NAVER_CLIENT_ID=발급받은_클라이언트ID\n\n2️⃣ 네이버 클라우드 플랫폼(console.naver.com/ncloud/application)에서:\n  - Maps > Application\n  - 서비스 URL에 다음 추가:\n    http://localhost:5173/*\n    http://localhost:5174/*\n\n3️⃣ 개발 서버 재시작:\n  npm run dev';
    alert(errorMsg);
    console.error('VITE_NAVER_CLIENT_ID가 설정되지 않음:', NAVER_ID);
    return;
  }

  try {
    console.log('🗺️ 네이버 지도 로드 시작...', NAVER_ID.substring(0, 10) + '...');
    
    // 2. 네이버 지도 스크립트 로드
    await loadNaverMap(NAVER_ID, 'geocoder');
    
    console.log('✅ 네이버 지도 스크립트 로드 완료');
    
    // 3. naver.maps 객체 재확인 (추가 안전 장치)
    let nmaps = (window as any).naver?.maps;
    
    if (!nmaps) {
      console.warn('⚠️ naver.maps가 즉시 로드되지 않음. 재시도 중...');
      
      // 최대 3초 동안 0.1초마다 확인
      let retryCount = 0;
      while (!nmaps && retryCount < 30) {
        await new Promise(resolve => setTimeout(resolve, 100));
        nmaps = (window as any).naver?.maps;
        retryCount++;
      }
      
      if (!nmaps) {
        throw new Error('네이버 지도 객체가 로드되지 않았습니다. 페이지를 새로고침해주세요.');
      }
      
      console.log('✅ 재시도 후 네이버 지도 로드 성공 (시도 횟수:', retryCount + ')');
    }
    
    console.log('✅ 네이버 지도 API 초기화 완료!');

    // 4. 지도 생성 (기본 위치: 서울 시청)
    const mapElement = document.getElementById('map');
    if (!mapElement) {
      throw new Error('지도 컨테이너를 찾을 수 없습니다.');
    }
    
    map.value = new nmaps.Map(mapElement, {
      center: new nmaps.LatLng(37.5665, 126.9780),
      zoom: 15,
      zoomControl: true,
      zoomControlOptions: {
        position: nmaps.Position.TOP_RIGHT
      }
    });
    
    console.log('✅ 지도 생성 완료');

    // 5. 내 마커 생성
    myMarker.value = new nmaps.Marker({
      position: map.value.getCenter(),
      map: map.value,
      icon: {
        content: `<div style="background: #FF6B6B; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
        anchor: new nmaps.Point(10, 10)
      }
    });
    
    console.log('✅ 마커 생성 완료');

    // 6. 초기 위치 로드
    fetchAndDisplayOtherUsers();

    // 7. 위치 추적 시작
    if ('geolocation' in navigator) {
      watchId = navigator.geolocation.watchPosition(
        async (pos) => {
          const { latitude, longitude } = pos.coords;
          const latlng = new nmaps.LatLng(latitude, longitude);
          
          // 마커 위치 업데이트
          myMarker.value.setPosition(latlng);
          map.value.setCenter(latlng);
          
          // 상태 업데이트
          isTracking.value = true;
          currentPosition.value = { latitude, longitude };
          
          console.log('📍 현재 위치:', latitude, longitude);

          // Reverse Geocoding으로 도로명 주소 가져오기
          setTimeout(async () => {
            const address = await reverseGeocode(latitude, longitude);
            if (address) {
              currentAddress.value = address;
              console.log('📍 현재 주소:', address);
            } else {
              console.warn('주소를 가져올 수 없습니다.');
            }
          }, 500);
        },
        (err) => {
          console.warn('위치 접근 오류:', err);
          isTracking.value = false;
          alert('위치 권한을 허용해 주세요.\n설정 > 개인정보 보호 > 위치 서비스에서 브라우저의 위치 접근을 허용해주세요.');
        },
        { 
          enableHighAccuracy: true, 
          maximumAge: 10000, 
          timeout: 20000 
        }
      );
    } else {
      alert('이 브라우저는 위치 서비스를 지원하지 않습니다.');
    }

    // 8. 주기적으로 내 위치 전송 (10초마다)
    sendLocationInterval = window.setInterval(() => {
      if (currentPosition.value && userId) {
        sendMyLocation(currentPosition.value.latitude, currentPosition.value.longitude);
      }
    }, 10000);

    // 9. 주기적으로 다른 사용자 위치 가져오기 (5초마다)
    fetchLocationsInterval = window.setInterval(() => {
      fetchAndDisplayOtherUsers();
    }, 5000);
    
    console.log('✅ 실시간 위치 추적 시작!');

  } catch (error: any) {
    console.error('❌ 지도 로드 실패:', error);
    
    let errorMessage = '지도를 불러오는데 실패했습니다.';
    
    if (error?.message) {
      errorMessage = error.message;
    } else if (error?.toString) {
      errorMessage = error.toString();
    }
    
    // 상세한 안내 메시지
    const fullMessage = `❌ ${errorMessage}\n\n📋 해결 방법:\n\n1️⃣ Exercise/main/.env 파일에 다음 내용 추가:\nVITE_NAVER_CLIENT_ID=발급받은_클라이언트ID\n\n2️⃣ 네이버 클라우드 플랫폼(console.naver.com/ncloud/application):\n  - Maps > Application\n  - 서비스 URL에 다음 추가:\n    http://localhost:5173/*\n    http://localhost:5174/*\n\n3️⃣ 개발 서버 재시작:\n  터미널에서 Ctrl+C 후 npm run dev\n\n4️⃣ 브라우저 캐시 삭제:\n  Ctrl+Shift+R 또는 Cmd+Shift+R\n\n5️⃣ 브라우저 콘솔(F12) 확인하여 에러 메시지 확인`;
    
    alert(fullMessage);
    console.error('🔍 전체 에러 정보:', error);
    console.error('🔍 현재 window.naver:', (window as any).naver);
    console.error('🔍 현재 NAVER_ID:', NAVER_ID);
  }
});

onBeforeUnmount(() => {
  if (watchId !== null) {
    navigator.geolocation.clearWatch(watchId);
  }
  if (sendLocationInterval !== null) {
    clearInterval(sendLocationInterval);
  }
  if (fetchLocationsInterval !== null) {
    clearInterval(fetchLocationsInterval);
  }
});
</script>

<style scoped>
/* 네이버 지도 로고 및 컨트롤 스타일 조정 */
#map :deep(.naver-map-logo) {
  display: none;
}
</style>

