<template>
  <div class="GuardianAllUsersMapPage">
    <div class="GuardianAllUsersMapCard">
      <div class="GuardianAllUsersMapHeader">
        <h3 class="GuardianAllUsersMapTitle">
          <svg class="GuardianAllUsersMapIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
          전체 사용자 실시간 위치
        </h3>
        <div class="GuardianAllUsersMapStatus">
          <div
            :class="isTracking ? 'GuardianAllUsersMapStatusDotActive' : 'GuardianAllUsersMapStatusDotInactive'"
            class="GuardianAllUsersMapStatusDot"
          ></div>
          <span class="GuardianAllUsersMapStatusText">
            {{ usersCount }}명 추적 중
          </span>
        </div>
      </div>
      
      <!-- 지도 -->
      <div id="guardian-map" class="GuardianAllUsersMap"></div>
      
      <!-- 사용자 목록 -->
      <div v-if="trackedUsers.length > 0" class="GuardianTrackedUsers">
        <div class="GuardianTrackedUsersTitle">추적 중인 사용자</div>
        <div class="GuardianTrackedUsersList">
          <div
            v-for="user in trackedUsers"
            :key="user.userId"
            class="GuardianTrackedUserChip"
          >
            <span class="GuardianTrackedUserChipDot"></span>
            <span>{{ user.userName || user.userId }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue';
import { loadNaverMap } from '@/utils/loadNaverMap';
import { getAllLocations } from '@/services/api/locations';
import type { LocationDto } from '@/services/api/locations';

interface TrackedUser {
  userId: string;
  userName?: string;
  latitude: number;
  longitude: number;
}

const props = defineProps<{
  users?: Array<{ id: string; name: string }>;
}>();

const NAVER_ID = import.meta.env.VITE_NAVER_CLIENT_ID as string;

const map = ref<any>(null);
const userMarkers = ref<Map<string, any>>(new Map());
const isTracking = ref(true);
const usersCount = ref(0);
const trackedUsers = ref<TrackedUser[]>([]);
let fetchLocationsInterval: number | null = null;

// 사용자 이름 가져오기
const getUserName = (userId: string): string => {
  const user = props.users?.find(u => String(u.id) === String(userId));
  return user?.name || userId;
};

// 모든 사용자 위치를 가져와서 마커 표시
const fetchAndDisplayAllUsers = async () => {
  try {
    const locations = await getAllLocations();
    
    if (!map.value) return;

    // naver.maps 객체 안전하게 가져오기
    const nmaps = (window as any).naver?.maps;
    
    if (!nmaps) {
      console.warn('⚠️ 네이버 지도 API가 로드되지 않았습니다. 인증 실패 또는 API 미설정 가능.');
      isTracking.value = false;
      return;
    }
    
    // 추적 중인 사용자 목록 업데이트
    trackedUsers.value = locations.map((loc: LocationDto) => ({
      userId: String(loc.userId),
      userName: getUserName(String(loc.userId)),
      latitude: loc.latitude,
      longitude: loc.longitude
    }));
    
    usersCount.value = locations.length;

    // 기존 마커 제거 (삭제된 사용자들)
    userMarkers.value.forEach((marker, key) => {
      if (!locations.find(loc => String(loc.userId) === String(key))) {
        marker.setMap(null);
        userMarkers.value.delete(key);
      }
    });

    // 새 마커 추가 또는 위치 업데이트 (좌표 검증 포함)
    locations.forEach((location: LocationDto) => {
      // 좌표 검증: NaN 또는 유효하지 않은 좌표 방지
      const lat = Number(location.latitude);
      const lng = Number(location.longitude);
      
      if (!isFinite(lat) || !isFinite(lng)) {
        console.warn(`⚠️ 잘못된 좌표 무시: userId=${location.userId}, lat=${location.latitude}, lng=${location.longitude}`);
        return; // 잘못된 좌표는 건너뜀
      }
      
      // 좌표 범위 검증 (한국 지역: 대략 33~43도, 124~132도)
      if (lat < 33 || lat > 43 || lng < 124 || lng > 132) {
        console.warn(`⚠️ 한국 지역 범위를 벗어난 좌표: userId=${location.userId}, lat=${lat}, lng=${lng}`);
        // 한국 범위를 벗어나도 표시는 하지만 경고 출력
      }
      
      const latlng = new nmaps.LatLng(lat, lng);
      const userIdStr = String(location.userId);
      const userName = getUserName(userIdStr);
      
      if (userMarkers.value.has(userIdStr)) {
        // 기존 마커 위치 업데이트
        userMarkers.value.get(userIdStr).setPosition(latlng);
      } else {
        // 새 마커 생성 (빨간색 - 모든 사용자)
        const marker = new nmaps.Marker({
          position: latlng,
          map: map.value,
          icon: {
            content: `<div style="background: #FF6B6B; width: 24px; height: 24px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 6px rgba(0,0,0,0.3);"></div>`,
            anchor: new nmaps.Point(12, 12)
          },
          title: userName
        });
        
        // 정보창 추가
        const infoWindow = new nmaps.InfoWindow({
          content: `<div style="padding: 10px; font-size: 13px; font-weight: 600;">${userName}</div>`
        });
        
        marker.addListener('click', () => {
          infoWindow.open(map.value, marker);
        });
        
        userMarkers.value.set(userIdStr, marker);
      }
    });
  } catch (error) {
    console.error('위치 조회 실패:', error);
    isTracking.value = false;
  }
};

onMounted(async () => {
  // 1. 클라이언트 ID 확인
  if (!NAVER_ID || NAVER_ID === '여기에_네이버_클라이언트ID_입력' || NAVER_ID.trim() === '') {
    alert('❌ 네이버 지도 API 클라이언트 ID가 설정되지 않았습니다.\n\nExercise/main/.env 파일에 VITE_NAVER_CLIENT_ID를 설정해주세요.');
    console.error('VITE_NAVER_CLIENT_ID가 설정되지 않음');
    isTracking.value = false;
    return;
  }

  try {
    console.log('🗺️ 네이버 지도 로드 시작 (보호자 대시보드)...');
    
    // 2. 네이버 지도 스크립트 로드
    await loadNaverMap(NAVER_ID, 'geocoder');
    
    console.log('✅ 네이버 지도 스크립트 로드 완료');
    
    // 3. naver.maps 객체 최종 검증 (인증 실패 시 null일 수 있음)
    if (!('naver' in window) || !(window as any).naver?.maps) {
      console.error('❌ 네이버 지도 인증 실패: 클라이언트ID/서비스URL을 확인하세요.');
      isTracking.value = false;
      const mapElement = document.getElementById('guardian-map');
      if (mapElement) {
        mapElement.innerHTML = `
          <div style="display: flex; align-items: center; justify-content: center; height: 100%; flex-direction: column; gap: 10px; padding: 20px;">
            <p style="color: #666; font-size: 14px; font-weight: 600;">⚠️ 네이버 지도 API 인증 실패</p>
            <p style="color: #999; font-size: 12px; text-align: center; line-height: 1.6;">
              네이버 클라우드 플랫폼에서 다음을 확인해주세요:<br/>
              • 서비스 URL 등록: http://localhost:5173<br/>
              • Web Dynamic Map 활성화<br/>
              • 클라이언트 ID 확인
            </p>
          </div>
        `;
      }
      return;
    }

    const nmaps = (window as any).naver.maps;

    // 4. 지도 생성 (기본 위치: 서울 시청)
    const mapElement = document.getElementById('guardian-map');
    if (!mapElement) {
      throw new Error('지도 컨테이너를 찾을 수 없습니다.');
    }
    
    // 4. 지도 생성 (안전하게)
    try {
      if (!nmaps || !nmaps.Map || !nmaps.LatLng) {
        throw new Error('네이버 지도 API 객체가 완전히 초기화되지 않았습니다.');
      }
      
      map.value = new nmaps.Map(mapElement, {
        center: new nmaps.LatLng(37.5665, 126.9780),
        zoom: 14,
        zoomControl: true,
        zoomControlOptions: {
          position: nmaps.Position.TOP_RIGHT
        }
      });
      
      console.log('✅ 지도 생성 완료');
    } catch (mapError: any) {
      console.error('❌ 지도 생성 실패:', mapError);
      isTracking.value = false;
      const errorMsg = mapError?.message || '지도를 초기화할 수 없습니다.';
      mapElement.innerHTML = `
        <div style="display: flex; align-items: center; justify-content: center; height: 100%; flex-direction: column; gap: 10px; padding: 20px;">
          <p style="color: #666; font-size: 14px; font-weight: 600;">⚠️ 지도 초기화 실패</p>
          <p style="color: #999; font-size: 12px;">${errorMsg}</p>
        </div>
      `;
      return;
    }

    // 5. 초기 위치 로드
    await fetchAndDisplayAllUsers();

    // 6. 주기적으로 위치 가져오기 (5초마다)
    fetchLocationsInterval = window.setInterval(() => {
      fetchAndDisplayAllUsers();
    }, 5000);
    
    console.log('✅ 실시간 위치 추적 시작 (보호자 대시보드)');

  } catch (error: any) {
    console.error('❌ 지도 로드 실패:', error);
    isTracking.value = false;
    
    let errorMessage = '지도를 불러오는데 실패했습니다.';
    if (error?.message) {
      errorMessage = error.message;
    }
    
    alert(`❌ ${errorMessage}\n\n네이버 지도 API 설정을 확인해주세요.`);
  }
});

onBeforeUnmount(() => {
  if (fetchLocationsInterval !== null) {
    clearInterval(fetchLocationsInterval);
  }
  
  // 모든 마커 제거
  userMarkers.value.forEach(marker => {
    marker.setMap(null);
  });
  userMarkers.value.clear();
});
</script>

<style scoped>
/* 네이버 지도 로고 및 컨트롤 스타일 조정 */
#guardian-map :deep(.naver-map-logo) {
  display: none;
}
</style>

