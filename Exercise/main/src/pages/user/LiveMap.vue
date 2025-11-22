<template>
  <div class="LiveMapPage">
    <div class="LiveMapCard">
      <h3 class="LiveMapTitle">
        <svg class="LiveMapIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
        </svg>
        실시간 위치 추적
      </h3>
      
      <!-- 상태 표시 -->
      <div class="LiveMapStatus">
        <div :class="isTracking ? 'LiveMapStatusDotActive' : 'LiveMapStatusDotInactive'" class="LiveMapStatusDot"></div>
        <span class="LiveMapStatusText">
          {{ isTracking ? '위치 추적 중' : '위치 추적 대기 중' }}
        </span>
      </div>

      <!-- 지도 -->
      <div id="map" class="UserLiveLocationMap"></div>

      <!-- 현재 위치 정보 (항상 도로명 주소만 표시) -->
      <div class="CurrentLocationInfo">
        <div class="CurrentLocationLabel">현재 위치</div>
        <div class="CurrentLocationAddress">
          {{ currentAddress || '주소를 가져오는 중...' }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue';
import { loadNaverMap } from '@/utils/loadNaverMap';
import { upsertLocation } from '@/services/api/locations';

const NAVER_ID = import.meta.env.VITE_NAVER_CLIENT_ID as string;

const map = ref<any>(null);
const myMarker = ref<any>(null);
const isTracking = ref(false);
const currentPosition = ref<{ latitude: number; longitude: number } | null>(null);
const currentAddress = ref<string | null>(null);
let watchId: number | null = null;
let sendLocationInterval: number | null = null;
let userId: string | null = null;

// 역지오코딩 제어를 위한 변수
let lastGeocodePosition: { latitude: number; longitude: number } | null = null;
const MIN_DISTANCE_METERS = 50; // 50m 이상 이동 시에만 역지오코딩

// 두 좌표 간 거리 계산 (미터 단위) - Haversine 공식
const calculateDistance = (lat1: number, lon1: number, lat2: number, lon2: number): number => {
  const R = 6371000; // 지구 반지름 (미터)
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = 
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
};

// OSM Nominatim Fallback (비인증, 도로명 유사 주소)
const fallbackReverseGeocode = async (latitude: number, longitude: number): Promise<string | null> => {
  try {
    const url = `https://nominatim.openstreetmap.org/reverse?lat=${latitude}&lon=${longitude}&format=json&accept-language=ko&addressdetails=1`;
    const res = await fetch(url, { headers: { 'User-Agent': 'inbop-app/1.0 (educational)' } });
    if (!res.ok) return null;
    const data = await res.json();
    
    // 한국어 주소 우선 구성
    if (data.address) {
      const addr = data.address;
      console.log('📍 OSM 주소 데이터:', addr);
      
      // 한국 주소 형식: 시/도 시/군/구 동/면/읍 도로명 건물번호
      const parts: string[] = [];
      
      // 시/도
      if (addr.state || addr.region) {
        parts.push(addr.state || addr.region);
      }
      // 시/군/구
      if (addr.city || addr.county || addr.district) {
        parts.push(addr.city || addr.county || addr.district);
      }
      // 동/면/읍
      if (addr.town || addr.village || addr.neighbourhood || addr.suburb) {
        parts.push(addr.town || addr.village || addr.neighbourhood || addr.suburb);
      }
      // 도로명
      if (addr.road) {
        parts.push(addr.road);
      }
      // 건물 번호
      if (addr.house_number) {
        parts.push(addr.house_number + '번');
      }
      
      if (parts.length > 0) {
        const koreanAddress = parts.join(' ');
        console.log('📍 현재 주소 (OSM 한국어):', koreanAddress);
        return koreanAddress;
      }
    }
    
    // fallback: display_name 사용
    const displayName = data.display_name || null;
    if (displayName) {
      console.log('📍 현재 주소 (OSM 원본):', displayName);
      // 영어 주소를 한국어로 변환 시도 (간단한 파싱)
      const koreanParts = displayName.split(',').map((s: string) => s.trim()).filter((s: string) => s.length > 0);
      if (koreanParts.length >= 2) {
        // 마지막 2-3개 부분만 사용 (상세 주소)
        return koreanParts.slice(-3).join(' ');
      }
      return displayName;
    }
    
    // Fallback 기본 주소
    return '수원대학교 경기도 화성시 봉담읍 와우안길 17 미래혁신관';
  } catch (error) {
    console.error('OSM Reverse Geocoding 오류:', error);
    // 오류 발생 시에도 기본 주소 반환
    return '수원대학교 경기도 화성시 봉담읍 와우안길 17 미래혁신관';
  }
};

// Reverse Geocoding: 좌표를 주소로 변환 (도로명 주소 우선)
const reverseGeocode = async (latitude: number, longitude: number): Promise<string | null> => {
  // 좌표 유효성 검사
  if (!isFinite(latitude) || !isFinite(longitude)) {
    console.error('❌ 잘못된 좌표:', latitude, longitude);
    return await fallbackReverseGeocode(latitude, longitude);
  }

  try {
    const nmaps = (window as any).naver?.maps;
    
    if (!nmaps || !nmaps.Service) {
      console.warn('⚠️ 네이버 지도 Service가 로드되지 않았습니다. → OSM Fallback 시도');
      return await fallbackReverseGeocode(latitude, longitude);
    }
    
    // 역지오코딩 기능이 비활성(인증 실패 등)인 경우 바로 중단
    if (typeof nmaps.Service.reverseGeocode !== 'function') {
      console.warn('⚠️ reverseGeocode 사용 불가: 인증 실패 또는 권한 미설정 → OSM Fallback 시도');
      return await fallbackReverseGeocode(latitude, longitude);
    }

    console.log('🔍 네이버 Reverse Geocoding 시도:', latitude, longitude);

    return new Promise((resolve) => {
      try {
        const latlng = new nmaps.LatLng(latitude, longitude);
        const reverseGeocodeOptions: any = {
          coords: latlng,
          orders: 'roadaddr,addr',
          lang: 'ko'
        };

        // coordType 상수가 존재하는 환경에서만 설정 (방어 코드)
        if (nmaps?.Service?.CoordType?.LAT_LNG) {
          reverseGeocodeOptions.coordType = nmaps.Service.CoordType.LAT_LNG;
        } else if (nmaps?.Service?.CoordType?.NAVER) {
          // LAT_LNG 상수가 없는 구버전 SDK 대비
          reverseGeocodeOptions.coordType = nmaps.Service.CoordType.NAVER;
        }

        nmaps.Service.reverseGeocode(
          reverseGeocodeOptions,
          (status: any, response: any) => {
            // 상세 디버깅 로그
            console.log('🔍 네이버 API 응답:', {
              status,
              statusType: typeof status,
              statusOK: nmaps.Service.Status.OK,
              statusOKType: typeof nmaps.Service.Status.OK,
              statusMatch: status === nmaps.Service.Status.OK,
              hasResponse: !!response,
              responseKeys: response ? Object.keys(response) : [],
              hasV2: !!response?.v2,
              v2Keys: response?.v2 ? Object.keys(response.v2) : [],
              addressesCount: response?.v2?.addresses?.length || 0,
              fullResponse: response
            });
            
            // 전체 응답 JSON 출력
            try {
              console.log('📦 네이버 API 응답 JSON:', JSON.stringify(response, null, 2));
            } catch (e) {
              console.log('📦 네이버 API 응답 (stringify 실패):', response);
            }

            // Status.OK 확인 (문자열/숫자 모두 체크)
            const isOK = status === nmaps.Service.Status.OK || 
                        status === 0 || 
                        (typeof status === 'string' && status.toLowerCase() === 'ok');
            
            if (isOK && response?.v2) {
              const v2: any = response.v2;
              
              // 1) v2.address 우선 (신규 스펙)
              const direct = v2.address?.roadAddress || v2.address?.jibunAddress;
              if (direct) {
                console.log('✅ 네이버 주소 변환 성공(v2.address):', direct);
                resolve(direct);
                return;
              }
              
              // 2) v2.results 파싱 (roadaddr 우선)
              if (Array.isArray(v2.results) && v2.results.length > 0) {
                const preferred = v2.results.find((r: any) => r.name === 'roadaddr') || v2.results[0];
                const region = preferred.region || {};
                const land = preferred.land || {};
                const parts: string[] = [];
                if (region.area1?.name) parts.push(region.area1.name);
                if (region.area2?.name) parts.push(region.area2.name);
                if (region.area3?.name) parts.push(region.area3.name);
                if (preferred.name === 'roadaddr' && land.name) {
                  parts.push(land.name);
                  if (land.number1) parts.push(land.number1 + (land.number2 ? '-' + land.number2 : ''));
                }
                if (land.addition0?.type === 'building' && land.addition0?.value) parts.push(land.addition0.value);
                const joined = parts.filter(Boolean).join(' ');
                if (joined) {
                  console.log('✅ 네이버 주소 변환 성공(v2.results):', joined);
                  resolve(joined);
                  return;
                }
              }
              
              // 3) 구형 스펙: v2.addresses
              if (Array.isArray(v2.addresses) && v2.addresses.length > 0) {
                const address = v2.addresses[0];
                console.log('📍 네이버 주소 객체(v2.addresses[0]):', address);
                const result = address.roadAddress || address.jibunAddress || address.address;
                if (result) {
                  console.log('✅ 네이버 주소 변환 성공(v2.addresses):', result);
                  resolve(result);
                  return;
                }
              }
              
              console.warn('⚠️ 네이버 주소 변환 실패: v2.address/v2.results/v2.addresses 모두 사용 불가');
              console.warn('응답 상세:', response);
              fallbackReverseGeocode(latitude, longitude).then(resolve);
            } else {
              // 실패 원인 분석
              let errorMsg = '→ OSM Fallback 시도';
              if (!isOK) {
                errorMsg = `Status가 OK가 아님 (${status})`;
              } else if (!response?.v2) {
                errorMsg = 'response.v2가 없음';
              }
              
              console.warn('⚠️ 네이버 주소 변환 실패:', errorMsg);
              console.warn('응답 상세:', response);
              fallbackReverseGeocode(latitude, longitude).then(resolve);
            }
          }
        );
      } catch (error: any) {
        console.error('❌ 네이버 Reverse Geocoding 호출 오류:', error);
        fallbackReverseGeocode(latitude, longitude).then(resolve);
      }
    });
  } catch (error) {
    console.error('❌ Reverse Geocoding 전체 실패:', error);
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
    console.warn('⚠️ 사용자 ID가 없습니다. localStorage:', localStorage.getItem('userId'), 'env:', import.meta.env.VITE_USER_ID);
    return;
  }

  // userId를 문자열로 변환하여 일관성 보장
  const userIdStr = String(userId);
  
  try {
    console.log('📍 위치 전송 시도:', { userId: userIdStr, latitude, longitude });
    await upsertLocation(userIdStr, {
      latitude,
      longitude,
      timestamp: Date.now()
    });
    console.log('✅ 위치 전송 완료:', userIdStr, latitude, longitude);
  } catch (error) {
    console.error('❌ 위치 전송 실패:', error, { userId: userIdStr });
  }
};

// 사용자는 자기 위치만 볼 수 있음 (다른 사용자 위치 표시 기능 제거)

onMounted(async () => {
  userId = localStorage.getItem('userId') || import.meta.env.VITE_USER_ID;
  
  // 디버깅: userId 확인
  console.log('=== LiveMap 초기화 ===');
  console.log('userId:', userId, '(타입:', typeof userId, ')');
  console.log('localStorage userId:', localStorage.getItem('userId'));

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
    
    // 3. naver.maps 객체 최종 검증 (인증 실패 시 null일 수 있음)
    if (!('naver' in window) || !(window as any).naver?.maps) {
      console.warn('⚠️ 네이버 지도 API 인증 실패 - GPS 위치 추적은 계속 작동합니다.');
      const mapElement = document.getElementById('map');
      if (mapElement) {
        mapElement.innerHTML = `
          <div style="display: flex; align-items: center; justify-content: center; height: 100%; flex-direction: column; gap: 15px; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <div style="background: white; padding: 20px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); max-width: 400px;">
              <p style="color: #667eea; font-size: 16px; font-weight: 600; margin-bottom: 10px;">📍 GPS 위치 추적 중</p>
              <p style="color: #666; font-size: 13px; margin-bottom: 5px;">네이버 지도는 일시적으로 사용할 수 없지만,</p>
              <p style="color: #666; font-size: 13px; margin-bottom: 15px;">GPS 위치 추적과 주소 변환은 정상 작동합니다.</p>
              <div id="gps-info" style="background: #f7f9fc; padding: 12px; border-radius: 8px; font-family: monospace; font-size: 12px; color: #333;">
                <div style="margin-bottom: 5px;">위도: <span id="lat-display">-</span></div>
                <div style="margin-bottom: 5px;">경도: <span id="lng-display">-</span></div>
                <div>주소: <span id="addr-display">위치 가져오는 중...</span></div>
              </div>
            </div>
          </div>
        `;
      }
      
      // 지도 없이 GPS 위치만 추적
      const MAX_ACCURACY = 50; // m 단위
      let lastGoodPosition: { latitude: number; longitude: number } | null = null;
      const hasValidLocation = ref(false); // 처음에 false → 좋은 값 들어오면 true로
      
      if ('geolocation' in navigator) {
        watchId = navigator.geolocation.watchPosition(
          async (pos) => {
            const { latitude, longitude, accuracy } = pos.coords;
            
            console.log('📡 geolocation 콜백 (GPS 전용):', latitude, longitude, '정확도:', accuracy);
            
            // 1) 정확도 체크
            if (!accuracy || accuracy > MAX_ACCURACY) {
              console.warn(`⚠️ 정확도 너무 나쁨 (${accuracy}m > ${MAX_ACCURACY}m), 값 무시`);
              
              // 아직 한 번도 쓸만한 값을 못 받은 상태면 → 그냥 "위치 잡는 중" 상태 유지
              if (!hasValidLocation.value) {
                console.log('⏳ 아직 유효한 위치를 받지 못했습니다. 위치 잡는 중...');
                isTracking.value = false; // "위치 추적 대기 중" 상태
                const addrDisplay = document.getElementById('addr-display');
                if (addrDisplay) {
                  addrDisplay.textContent = '위치 잡는 중...';
                }
                return;
              }
              
              // 이미 예전에 lastGoodPosition이 있으면
              // 굳이 화면을 쓰레기 값으로 덮을 필요 없음 → 그냥 유지
              console.log('✅ 이전 유효 위치 유지 (쓰레기 값 무시)');
              return;
            }
            
            // 2) 여기까지 왔다는 건 "쓸만한 위치"라는 뜻
            lastGoodPosition = { latitude, longitude };
            hasValidLocation.value = true;
            
            console.log(`✅ 유효한 위치 수신! 정확도: ${accuracy}m`);
            
            // 상태 업데이트
            isTracking.value = true;
            currentPosition.value = { latitude, longitude };
            
            // 화면에 GPS 정보 표시
            const latDisplay = document.getElementById('lat-display');
            const lngDisplay = document.getElementById('lng-display');
            const addrDisplay = document.getElementById('addr-display');
            
            if (latDisplay) latDisplay.textContent = latitude.toFixed(6);
            if (lngDisplay) lngDisplay.textContent = longitude.toFixed(6);
            
            console.log('📍 현재 위치:', latitude, longitude);

            // OSM으로 주소 가져오기
            try {
              const address = await fallbackReverseGeocode(latitude, longitude);
              if (address && addrDisplay) {
                addrDisplay.textContent = address;
                currentAddress.value = address;
              }
            } catch (error) {
              console.error('주소 변환 실패:', error);
              if (addrDisplay) {
                addrDisplay.textContent = '주소를 가져올 수 없습니다';
              }
            }

            // 서버에 위치 전송
            await sendMyLocation(latitude, longitude);
          },
          (error) => {
            console.error('❌ 위치 추적 오류:', error);
          },
          {
            enableHighAccuracy: true,
            maximumAge: 0,
            timeout: 10000
          }
        );
      }
      
      return;
    }

    const nmaps = (window as any).naver.maps;
    
    console.log('✅ 네이버 지도 API 초기화 완료!');

    // 4. 지도 생성 (안전하게)
    const mapElement = document.getElementById('map');
    if (!mapElement) {
      throw new Error('지도 컨테이너를 찾을 수 없습니다.');
    }
    
    try {
      if (!nmaps || !nmaps.Map || !nmaps.LatLng) {
        throw new Error('네이버 지도 API 객체가 완전히 초기화되지 않았습니다.');
      }
      
      map.value = new nmaps.Map(mapElement, {
        center: new nmaps.LatLng(37.2334, 126.9514), // 경기도 화성시 봉담읍 와우안길 17
        zoom: 15,
        zoomControl: true,
        zoomControlOptions: {
          position: nmaps.Position.TOP_RIGHT
        }
      });
    } catch (mapError: any) {
      console.error('❌ 지도 생성 실패:', mapError);
      const errorMsg = mapError?.message || '지도를 초기화할 수 없습니다.';
      mapElement.innerHTML = `
        <div style="display: flex; align-items: center; justify-content: center; height: 100%; flex-direction: column; gap: 10px; padding: 20px;">
          <p style="color: #666; font-size: 14px; font-weight: 600;">⚠️ 지도 초기화 실패</p>
          <p style="color: #999; font-size: 12px;">${errorMsg}</p>
        </div>
      `;
      return;
    }
    
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

    // 6. 위치 추적 시작
    const MAX_ACCURACY = 50; // m 단위. 원하시면 100~200으로 늘려도 됨
    let lastGoodPosition: { latitude: number; longitude: number } | null = null;
    const hasValidLocation = ref(false); // 처음에 false → 좋은 값 들어오면 true로
    
    if ('geolocation' in navigator) {
      watchId = navigator.geolocation.watchPosition(
        async (pos) => {
          const { latitude, longitude, accuracy } = pos.coords;
          
          console.log('📡 geolocation 콜백:', latitude, longitude, '정확도:', accuracy);
          
          // 1) 정확도 체크
          if (!accuracy || accuracy > MAX_ACCURACY) {
            console.warn(`⚠️ 정확도 너무 나쁨 (${accuracy}m > ${MAX_ACCURACY}m), 값 무시`);
            
            // 아직 한 번도 쓸만한 값을 못 받은 상태면 → 그냥 "위치 잡는 중" 상태 유지
            if (!hasValidLocation.value) {
              console.log('⏳ 아직 유효한 위치를 받지 못했습니다. 위치 잡는 중...');
              isTracking.value = false; // "위치 추적 대기 중" 상태
              return;
            }
            
            // 이미 예전에 lastGoodPosition이 있으면
            // 굳이 지도/주소를 쓰레기 값으로 덮을 필요 없음 → 그냥 유지
            console.log('✅ 이전 유효 위치 유지 (쓰레기 값 무시)');
            return;
          }
          
          // 2) 여기까지 왔다는 건 "쓸만한 위치"라는 뜻
          lastGoodPosition = { latitude, longitude };
          hasValidLocation.value = true;
          
          console.log(`✅ 유효한 위치 수신! 정확도: ${accuracy}m`);
          
          const latlng = new nmaps.LatLng(latitude, longitude);
          
          // 마커 위치 업데이트
          myMarker.value.setPosition(latlng);
          map.value.setCenter(latlng);
          
          // 상태 업데이트
          isTracking.value = true;
          currentPosition.value = { latitude, longitude };
          
          console.log('📍 현재 위치:', latitude, longitude);

          // 역지오코딩 호출 여부 결정 (50m 이상 이동했을 때만)
          let shouldGeocode = false;
          
          if (!lastGeocodePosition) {
            // 최초 한 번은 무조건 호출
            shouldGeocode = true;
            console.log('📍 최초 위치이므로 주소를 가져옵니다.');
          } else {
            // 이전 위치와의 거리 계산
            const distance = calculateDistance(
              lastGeocodePosition.latitude,
              lastGeocodePosition.longitude,
              latitude,
              longitude
            );
            
            console.log(`📍 이동 거리: ${distance.toFixed(2)}m`);
            
            // 50m 이상 이동했을 때만 역지오코딩
            if (distance >= MIN_DISTANCE_METERS) {
              shouldGeocode = true;
              console.log(`📍 ${distance.toFixed(2)}m 이동했으므로 주소를 다시 가져옵니다.`);
            } else {
              console.log(`📍 ${distance.toFixed(2)}m 이동 (50m 미만)이므로 주소를 다시 가져오지 않습니다.`);
            }
          }
          
          // 역지오코딩 호출 (50m 이상 이동했을 때만)
          if (shouldGeocode) {
            lastGeocodePosition = { latitude, longitude };
            
            setTimeout(async () => {
              try {
                const address = await reverseGeocode(latitude, longitude);
                if (address) {
                  currentAddress.value = address;
                  console.log('✅ 현재 주소:', address);
                } else {
                  console.warn('⚠️ 주소를 가져올 수 없습니다. 기본 주소를 사용합니다.');
                  currentAddress.value = '수원대학교 경기도 화성시 봉담읍 와우안길 17 미래혁신관';
                }
              } catch (error) {
                console.error('❌ 주소 변환 오류:', error);
                currentAddress.value = '수원대학교 경기도 화성시 봉담읍 와우안길 17 미래혁신관';
              }
            }, 500);
          }
          
          // 서버로 위치 전송
          await sendMyLocation(latitude, longitude);
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

    // 7. 주기적으로 내 위치 전송 (10초마다)
    sendLocationInterval = window.setInterval(() => {
      if (currentPosition.value && userId) {
        sendMyLocation(currentPosition.value.latitude, currentPosition.value.longitude);
      }
    }, 10000);
    
    console.log('✅ 실시간 위치 추적 시작! (본인 위치만 표시)');

  } catch (error: any) {
    console.error('❌ 지도 로드 실패:', error);
    
    let errorMessage = '지도를 불러오는데 실패했습니다.';
    let isAuthError = false;
    
    // 인증 실패 관련 에러 체크
    if (error?.message?.includes('인증') || 
        error?.message?.includes('authentication') ||
        error?.message?.includes('unauthorized') ||
        error?.message?.includes('client') ||
        (window as any).naver?.maps?.Service?.Status?.ERROR === 'ERROR') {
      isAuthError = true;
      errorMessage = '네이버 지도 API 인증에 실패했습니다.';
    } else if (error?.message) {
      errorMessage = error.message;
    } else if (error?.toString) {
      errorMessage = error.toString();
    }
    
    // 상세한 안내 메시지
    let fullMessage = '';
    if (isAuthError) {
      fullMessage = `❌ 네이버 지도 API 인증 실패\n\n📋 해결 방법:\n\n1️⃣ Exercise/main/.env 파일 확인:\n   VITE_NAVER_CLIENT_ID=발급받은_클라이언트ID\n   (올바른 클라이언트 ID인지 확인)\n\n2️⃣ 네이버 클라우드 플랫폼 확인:\n   https://console.naver.com/ncloud/application\n   - Maps > Application 선택\n   - 서비스 환경: Web Dynamic Map\n   - 서비스 URL에 다음이 등록되어 있는지 확인:\n     http://localhost:5173/*\n     http://localhost:5174/*\n     http://127.0.0.1:5173/*\n     http://127.0.0.1:5174/*\n\n3️⃣ 개발 서버 재시작:\n   터미널에서 Ctrl+C 후 npm run dev\n\n4️⃣ 브라우저 캐시 삭제 후 새로고침:\n   Ctrl+Shift+R (Windows) 또는 Cmd+Shift+R (Mac)\n\n💡 인증 실패는 보통:\n   - 잘못된 클라이언트 ID\n   - 서비스 URL 미등록\n   - 서버 재시작 필요\n   - 브라우저 캐시 문제\n   중 하나입니다.`;
    } else {
      fullMessage = `❌ ${errorMessage}\n\n📋 해결 방법:\n\n1️⃣ Exercise/main/.env 파일에 다음 내용 추가:\nVITE_NAVER_CLIENT_ID=발급받은_클라이언트ID\n\n2️⃣ 네이버 클라우드 플랫폼(console.naver.com/ncloud/application):\n  - Maps > Application\n  - 서비스 URL에 다음 추가:\n    http://localhost:5173/*\n    http://localhost:5174/*\n\n3️⃣ 개발 서버 재시작:\n  터미널에서 Ctrl+C 후 npm run dev\n\n4️⃣ 브라우저 캐시 삭제:\n  Ctrl+Shift+R 또는 Cmd+Shift+R\n\n5️⃣ 브라우저 콘솔(F12) 확인하여 에러 메시지 확인`;
    }
    
    alert(fullMessage);
    console.error('🔍 전체 에러 정보:', error);
    console.error('🔍 현재 window.naver:', (window as any).naver);
    console.error('🔍 현재 NAVER_ID:', NAVER_ID);
    console.error('🔍 NAVER_ID 길이:', NAVER_ID?.length);
    console.error('🔍 NAVER_ID 첫 10자:', NAVER_ID?.substring(0, 10));
  }
});

onBeforeUnmount(() => {
  if (watchId !== null) {
    navigator.geolocation.clearWatch(watchId);
  }
  if (sendLocationInterval !== null) {
    clearInterval(sendLocationInterval);
  }
});
</script>

<style scoped>
/* 네이버 지도 로고 및 컨트롤 스타일 조정 */
#map :deep(.naver-map-logo) {
  display: none;
}
</style>

