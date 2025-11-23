<template>
  <div class="UserDashboardPage">
    <!-- 상단 절반 화이트 박스 -->
    <div class="UserDashboardHeaderCard">
      <div class="UserDashboardHeaderContent">
      <!-- 좌측: 아바타 이미지 -->
      <div class="UserAvatarSection">
        <!-- 이미지가 있으면 표시, 없으면 플레이스홀더 -->
        <div v-if="currentAvatarSrc" class="UserAvatarImageWrapper">
          <img 
            :src="currentAvatarSrc" 
            alt="User Avatar" 
            style="image-rendering: pixelated;"
            @error="handleImageError"
          />
        </div>
        <!-- 이미지 로드 실패 시 플레이스홀더 -->
        <div v-else class="UserAvatarPlaceholder">
          <span>👤</span>
        </div>
        <div class="UserAvatarStatus">
          {{ avatarStatus }} (레벨 {{ avatarLevel }})
        </div>
        <div class="UserActivityGoal">
          목표: {{ dailyGoal }}분 | 진행: {{ todayProgress }}분
        </div>
      </div>
      
      <!-- 가운데: 새싹/상점/긴급전화 버튼들 -->
      <div class="UserQuickActions">
        <!-- 새싹 버튼 (보유 새싹 표시) -->
        <button
          class="SproutCountButton"
          title="보유 새싹"
          disabled
        >
          <div class="text-2xl mb-0.5">🌱</div>
          <div class="SproutCountText">{{ sproutCount }}</div>
        </button>
        
        <!-- 상점 버튼 -->
        <a>
          <button
            @click="showShopPopup = true"
            class="OpenShopButton"
            title="상점 열기"
          >
            <svg class="OpenShopButtonIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
            </svg>
          </button>
        </a>
        
        <!-- 119 신고 버튼 -->
        <a href="tel:119">
          <button
            class="EmergencyCallButton"
            title="119 신고"
          >
            <svg class="EmergencyCallButtonIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </button>
        </a>
        
        <!-- 보호자 연락 버튼 -->
        <a v-if="currentUser?.guardianPhone" :href="`tel:${currentUser.guardianPhone}`">
          <button
            class="ContactGuardianButton"
            title="보호자 연락"
          >
            <svg class="ContactGuardianButtonIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
            </svg>
          </button>
        </a>
      </div>
      
      <!-- 우측: 목표치 표시 -->
      <div class="UserDailyGoalSection">
        <!-- 목표치 표시 (처방 수락 후에만) -->
        <div v-if="hasActivePrescription" class="UserDailyGoalContent">
          <div class="UserDailyGoalContentInner">
            <!-- 모든 운동 완료 시 -->
            <div v-if="isAllExercisesCompleted" class="DailyExerciseCompleted">
              <div>🌱</div>
              <div>오늘의 운동 완료!</div>
            </div>

            <!-- 운동 버튼들 -->
            <div v-else-if="activePrescription" class="DailyExerciseActions">
              <!-- 오늘의 운동 목표 제목 (시작 스트레칭 버튼 위에 위치) -->
              <div class="DailyExerciseTitle">오늘의 운동 목표</div>
              
              <!-- 시작 스트레칭 버튼 -->
              <button 
                v-if="!exerciseCompleted.startStretching"
                @click="completeStartStretching"
                class="StartStretchingButton"
              >
                시작 스트레칭 {{ activePrescription.startStretchingMinutes }}분
              </button>
              
              <!-- 인터벌 운동 버튼 (하나만 표시) -->
              <button 
                v-if="hasIncompleteIntervals"
                @click="completeNextInterval"
                class="IntervalWorkoutButton"
              >
                인터벌 운동: 걷기 {{ activePrescription.walkingMinutes }}분 → 뛰기 {{ activePrescription.runningMinutes }}분
                ({{ completedIntervalCount }}/{{ activePrescription.sets }}세트 완료)
              </button>
              
              <!-- 마무리 스트레칭 버튼 -->
              <button 
                v-if="!exerciseCompleted.endStretching"
                @click="completeEndStretching"
                class="EndStretchingButton"
              >
                마무리 스트레칭 {{ activePrescription.endStretchingMinutes }}분
              </button>
            </div>
          </div>
        </div>
      </div>
      </div>
    </div>

    <!-- 하단 3분할 -->
    <div class="UserDashboardMainGrid">
      <!-- 좌측: 실시간 바이탈 + 설문 결과 보기 -->
      <AppCard class="RealtimeVitalsCard">
        <div class="RealtimeVitalsContent">
          <h3 class="RealtimeVitalsTitle">
            <svg class="RealtimeVitalsIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
            </svg>
            실시간 바이탈
          </h3>
          <div class="RealtimeVitalsMetrics">
            <div class="RealtimeHeartRateRow">
              <span class="RealtimeHeartRateLabel">심박수</span>
              <span
                class="RealtimeHeartRateValue"
                :class="(vital.hr && vital.hr > 0) ? 'RealtimeHeartRateValueActive' : 'RealtimeHeartRateValueInactive'"
              >
                {{ (vital.hr && vital.hr > 0) ? vital.hr : '-' }}
              </span>
              <span class="RealtimeHeartRateUnit">bpm</span>
            </div>
          </div>
          <div v-if="!vital.hr || vital.hr === 0" class="SensorWaitingMessage">
            <p>센서 연결 대기 중...</p>
          </div>
          <div class="RealtimeVitalsActions">
            <AppButton
              @click="reconnectBluetooth"
              variant="solid"
              class="ReconnectBluetoothButton"
            >
              블루투스 재연결
            </AppButton>
            <RouterLink to="/user/survey/result">
              <AppButton variant="ghost" class="ViewSurveyResultButton">설문 결과 보기</AppButton>
            </RouterLink>
            
            <!-- 새로운 설문 요청 알림 -->
            <div v-if="pendingSurveyRequests.length > 0" class="PendingSurveyRequests">
              <div
                v-for="surveyRequest in pendingSurveyRequests"
                :key="surveyRequest.id"
                class="PendingSurveyRequestItem"
              >
                <AppButton
                  @click="goToSurvey(surveyRequest.id)"
                  class="NewSurveyRequestButton"
                >
                  <svg class="NewSurveyRequestButtonIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                  </svg>
                  새로운 설문 요청
                </AppButton>
                <span class="PendingSurveyRequestBadge"></span>
              </div>
            </div>
          </div>
        </div>
      </AppCard>

      <!-- 가운데: 운동 시작 + 칼로리/시간 + 위치 -->
      <AppCard class="ExerciseCard">
        <div class="ExerciseContent">
          <h3 class="ExerciseSectionTitle">운동</h3>
          <AppButton
            @click="toggleWorkout"
            :variant="isExercising ? 'outline' : 'solid'"
            class="WorkoutToggleButton"
          >
            {{ isExercising ? '운동 중지' : '운동 시작' }}
          </AppButton>
          <div class="ExerciseStatsGrid">
            <div class="ExerciseCaloriesCard">
              <div class="ExerciseCaloriesLabel">킬로칼로리</div>
              <div class="ExerciseCaloriesValue">{{ caloriesBurned }}</div>
              <div class="ExerciseCaloriesUnit">kcal</div>
            </div>
            <div class="ExerciseTimeCard">
              <div class="ExerciseTimeLabel">시간</div>
              <div class="ExerciseTimeValue">{{ exerciseTimeFormatted }}</div>
            </div>
          </div>
          <div class="ExerciseLocationSection">
            <div class="CurrentLocationMap">
              <NaverUserLocationMap :position="position" small :userName="currentUser?.name" />
            </div>
            <!-- 현재 주소 표시 -->
            <div
              v-if="currentAddress"
              class="CurrentAddressDisplay"
            >
              📍 {{ currentAddress }}
            </div>
            <RouterLink to="/user/location">
              <AppButton variant="ghost" class="ViewLiveLocationButton">
                <svg class="ViewLiveLocationButtonIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                실시간 위치 보기
              </AppButton>
            </RouterLink>
          </div>
        </div>
      </AppCard>

      <!-- 우측: 대화창 -->
      <AppCard class="UserChatCard">
        <div class="UserChatContent">
          <h3 class="ChatSectionTitle">대화창</h3>
          <div class="ChatMessageList">
            <div v-for="(chatMessage, index) in chatMessages" :key="index" class="ChatMessageRow">
              <span
                :class="chatMessage.sender === 'user' ? 'ChatMessageSenderUser' : 'ChatMessageSenderBot'"
                class="ChatMessageSender"
              >
                {{ chatMessage.sender === 'user' ? currentUser?.name || '나' : '운동 선생님' }}:
              </span>
              <span class="ChatMessageText">{{ chatMessage.message }}</span>
            </div>
          </div>
          <div class="ChatInputRow">
            <input
              v-model="chatInput"
              @keyup.enter="sendChatMessage"
              class="ChatInputField"
              placeholder="메시지 입력"
            />
            <AppButton
              @click="sendChatMessage"
              variant="solid"
              class="ChatSendButton"
            >
              전송
            </AppButton>
          </div>
        </div>
      </AppCard>
    </div>

    <!-- 처방 알람 팝업 -->
    <AppModal :open="showPrescriptionPopup" title="새로운 운동 처방" @close="closePrescriptionPopup">
      <div class="PrescriptionPopupContent">
        <p class="PrescriptionPopupMessage">운동 선생님으로부터 새로운 운동 처방이 도착했습니다.</p>
        <div v-if="pendingPrescription" class="PrescriptionDetails">
          <div class="PrescriptionDetailItem"><span class="PrescriptionDetailLabel">시작 스트레칭:</span> {{ pendingPrescription.startStretchingMinutes }}분</div>
          <div class="PrescriptionDetailItem"><span class="PrescriptionDetailLabel">인터벌 운동:</span> 걷기 {{ pendingPrescription.walkingMinutes }}분 → 뛰기 {{ pendingPrescription.runningMinutes }}분 ({{ pendingPrescription.sets }}세트)</div>
          <div class="PrescriptionDetailItem"><span class="PrescriptionDetailLabel">마무리 스트레칭:</span> {{ pendingPrescription.endStretchingMinutes }}분</div>
        </div>
        <p class="PrescriptionPopupQuestion">처방을 수락하시겠습니까?</p>
      </div>
      <template #footer>
        <AppButton variant="ghost" @click="declinePrescriptionHandler">거부</AppButton>
        <AppButton variant="solid" @click="acceptPrescriptionHandler">수락</AppButton>
      </template>
    </AppModal>

    <!-- 설문 팝업 -->
    <AppModal :open="showSurveyPopup" title="새로운 설문 요청" @close="closeSurveyPopup">
      <p class="SurveyPopupMessage1">보호자로부터 새로운 설문 요청이 있습니다.</p>
      <p class="SurveyPopupMessage2">설문을 진행하시겠습니까?</p>
      <template #footer>
        <AppButton variant="ghost" class="SurveyRemindLaterButton" @click="declineSurvey">
          나중에
        </AppButton>
        <AppButton variant="solid" class="SurveyStartButton" @click="acceptSurvey">
          설문 시작
        </AppButton>
      </template>
    </AppModal>

    <!-- 공원 선택 팝업 -->
    <AppModal :open="showParkSelectionPopup" title="공원 선택" @close="showParkSelectionPopup = false">
      <div class="ParkSelectionContent">
        <p class="ParkSelectionMessage">인터벌 운동을 위한 공원을 선택해주세요.</p>
        <div v-if="loadingParks" class="ParkSelectionLoading">
          공원 정보를 불러오는 중...
        </div>
        <div v-else-if="recommendedParks.length === 0" class="ParkSelectionEmpty">
          근처 공원을 찾을 수 없습니다.
        </div>
        <div v-else class="ParkSelectionList">
          <button
            v-for="(park, index) in recommendedParks"
            :key="index"
            @click="selectPark(park)"
            class="ParkSelectionItem"
          >
            <div class="ParkSelectionItemTitle">{{ park.title }}</div>
            <div class="ParkSelectionItemAddress">{{ park.address }}</div>
            <div class="ParkSelectionItemDistance">{{ park.distance }}</div>
          </button>
        </div>
      </div>
    </AppModal>

    <!-- 상점 팝업 -->
    <AppModal :open="showShopPopup" title="상점" @close="closeShopPopup">
      <div class="ShopPopupContent">
        <!-- 건강 상점 -->
        <div class="ShopSection">
          <div class="ShopSectionTitle">건강 상점</div>
          <div class="ShopItemsGrid">
            <div 
              v-for="item in shopItems.slice(0, 4)" 
              :key="item.id"
              @click="buyItem(item)"
              :class="[
                'ShopItem',
                sproutCount >= item.price 
                  ? 'ShopItemAvailable' 
                  : 'ShopItemUnavailable'
              ]"
            >
              <div class="ShopItemImageWrapper">
                <img 
                  :src="item.image" 
                  :alt="item.name"
                  class="ShopItemImage"
                />
              </div>
              <div class="ShopItemName">{{ item.name }}</div>
              <div class="ShopItemPrice">🌱 {{ item.price }}</div>
              <div v-if="sproutCount < item.price" class="ShopItemInsufficient">새싹 부족</div>
            </div>
          </div>
        </div>
      </div>
    </AppModal>

    <!-- 심박수 주의 팝업 -->
    <div v-if="showHeartRateAlertPopup" class="HeartRateAlertPopupOverlay" @click="closeHeartRateAlertPopup">
      <div class="HeartRateAlertPopupContent" @click.stop>
        <div class="HeartRateAlertPopupTitle">심박수 주의</div>
        <div class="HeartRateAlertPopupMessage">
          현재 심박수가 {{ currentHeartRate }} bpm 입니다.
          <br>휴식을 취해주세요.
        </div>
        <div class="HeartRateAlertPopupActions">
          <AppButton variant="solid" @click="closeHeartRateAlertPopup" class="HeartRateAlertPopupConfirmButton">
            확인
          </AppButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import AppCard from '@/components/common/AppCard.vue'
import AppButton from '@/components/common/AppButton.vue'
import AppModal from '@/components/common/AppModal.vue'
import NaverUserLocationMap from '@/components/map/NaverUserLocationMap.vue'
import { useGeo } from '@/composables/useGeo'
import { useMetricsStore } from '@/stores/metrics.store'
import { getChatMessages, sendChatMessage as sendChatAPI, type ChatMessage } from '@/services/api/chatMessages'
import { useUsersStore } from '@/stores/users.store'
import { getPendingSurveyRequests } from '@/services/api/surveyRequests'
import { updateExerciseStatus } from '@/services/api/exerciseStatus'
import { getPendingPrescription, acceptPrescription, declinePrescription, getPrescriptionsByUser, completePrescription, type ExercisePrescription } from '@/services/api/exercisePrescriptions'
import { createHeartRateAlert } from '@/services/api/heartRateAlerts'
import type { SurveyRequest } from '@/services/api/surveyRequests'
import { getSproutCount, earnSprout, spendSprouts } from '@/services/api/sprouts'
import { upsertLocation } from '@/services/api/locations'
import { getStretchRecommendation, type ParkRecommendation } from '@/services/api/stretch'
import http from '@/services/api/http'

const router = useRouter()
const { position } = useGeo()
const metricsStore = useMetricsStore()
const usersStore = useUsersStore()

// 현재 주소
const currentAddress = ref<string>('')

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
        return parts.join(' ');
      }
    }
    
    // fallback: display_name 사용
    const displayName = data.display_name || null;
    if (displayName) {
      const koreanParts = displayName.split(',').map((s: string) => s.trim()).filter((s: string) => s.length > 0);
      if (koreanParts.length >= 2) {
        return koreanParts.slice(-3).join(' ');
      }
      return displayName;
    }
    
    // Fallback 기본 주소
    return '수원대학교 경기도 화성시 봉담읍 와우안길 17 미래혁신관';
  } catch (error) {
    console.error('OSM Reverse Geocoding 오류:', error);
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
          reverseGeocodeOptions.coordType = nmaps.Service.CoordType.NAVER;
        }

        nmaps.Service.reverseGeocode(
          reverseGeocodeOptions,
          (status: any, response: any) => {
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
                const result = address.roadAddress || address.jibunAddress || address.address;
                if (result) {
                  console.log('✅ 네이버 주소 변환 성공(v2.addresses):', result);
                  resolve(result);
                  return;
                }
              }
              
              console.warn('⚠️ 네이버 주소 변환 실패: v2.address/v2.results/v2.addresses 모두 사용 불가');
              fallbackReverseGeocode(latitude, longitude).then(resolve);
            } else {
              console.warn('⚠️ 네이버 주소 변환 실패:', !isOK ? `Status가 OK가 아님 (${status})` : 'response.v2가 없음');
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

// 주소 변환 함수
const getAddressFromPosition = async (lat: number, lng: number) => {
  try {
    // 먼저 네이버 지도 API로 시도, 실패하면 OSM 사용
    const address = await reverseGeocode(lat, lng);
    if (address) {
      currentAddress.value = address;
      console.log('✅ 주소 변환 성공:', address);
    } else {
      currentAddress.value = `위도: ${lat.toFixed(4)}, 경도: ${lng.toFixed(4)}`;
      console.warn('⚠️ 주소를 가져올 수 없어 좌표만 표시');
    }
  } catch (error) {
    console.error('주소 변환 실패:', error);
    currentAddress.value = `위도: ${lat.toFixed(4)}, 경도: ${lng.toFixed(4)}`;
  }
}

// 위치 변경 감지
watch(position, (newPos) => {
  if (newPos) {
    getAddressFromPosition(newPos.lat, newPos.lng)
  }
}, { immediate: true })

const vital = computed(() => metricsStore.vitalNow)
const currentUser = computed(() => usersStore.detail)
const isExercising = ref(false)

// 심박수 경고 관련
const showHeartRateAlertPopup = ref(false)
const currentHeartRate = computed(() => vital.value.hr)
const lastAlertTime = ref<number>(0)
let heartRateAlertCheckInterval: number | null = null

// 디버깅용: 콘솔에서 확인할 수 있도록 전역 함수 등록
if (typeof window !== 'undefined') {
  (window as any).checkGuardianPhone = () => {
    console.log('=== 보호자 연락처 확인 ===')
    console.log('currentUser:', currentUser.value)
    console.log('보호자 연락처:', currentUser.value?.guardianPhone)
    console.log('usersStore.detail:', usersStore.detail)
    console.log('usersStore.detail?.guardianPhone:', usersStore.detail?.guardianPhone)
    return currentUser.value?.guardianPhone
  }
}
const caloriesBurned = ref(0)
const exerciseTimeInSeconds = ref(0)
const chatMessages = ref<ChatMessage[]>([])

// 처방 관련
const showPrescriptionPopup = ref(false)
const showParkSelectionPopup = ref(false)
const recommendedParks = ref<ParkRecommendation[]>([])
const loadingParks = ref(false)
const pendingPrescription = ref<ExercisePrescription | null>(null)
const activePrescription = ref<ExercisePrescription | null>(null)
const hasActivePrescription = computed(() => activePrescription.value !== null)

// 운동 완료 상태
const exerciseCompleted = ref({
  startStretching: false,
  intervals: [] as boolean[], // 세트별 완료 상태
  endStretching: false
})

// 완료된 인터벌 세트 수
const completedIntervalCount = computed(() => {
  if (!activePrescription.value) return 0
  return exerciseCompleted.value.intervals.filter(completed => completed).length
})

// 미완료 인터벌이 있는지 확인
const hasIncompleteIntervals = computed(() => {
  if (!activePrescription.value) return false
  return exerciseCompleted.value.intervals.length < activePrescription.value.sets ||
    exerciseCompleted.value.intervals.some(completed => !completed)
})

// 모든 운동 완료 여부 확인
const isAllExercisesCompleted = computed(() => {
  if (!activePrescription.value) return false
  
  // 시작 스트레칭 완료 확인
  if (!exerciseCompleted.value.startStretching) return false
  
  // 모든 인터벌 세트 완료 확인
  const allIntervalsCompleted = activePrescription.value.sets > 0 && 
    exerciseCompleted.value.intervals.length === activePrescription.value.sets &&
    exerciseCompleted.value.intervals.every(completed => completed)
  if (!allIntervalsCompleted) return false
  
  // 마무리 스트레칭 완료 확인
  if (!exerciseCompleted.value.endStretching) return false
  
  return true
})

// 아바타 시스템
const avatarLevel = ref(3) // 1=건강, 5=살찜
const dailyGoal = ref(30) // 일일 목표 (분)
const todayProgress = ref(0) // 오늘 진행 (분)

// 성별 매핑 (사용자의 Gender 타입을 아바타 이미지 타입으로 변환)
const userGender = computed(() => {
  if (!currentUser.value) return 'female'
  // '남성' -> 'male', '여성' -> 'female'
  return currentUser.value.gender === '남성' ? 'male' : 'female'
})

// 아바타 이미지 매핑 (1=건강, 5=살찜)
const avatarImages = {
  female: [
    null,
    '/images/female-1.png', // 1: 건강
    '/images/female-2.png',  // 2: 마른
    '/images/female-3.png',  // 3: 보통
    '/images/female-4.png',  // 4: 덜 찐
    '/images/female-5.png',  // 5: 살찐
  ],
  male: [
    null,
    '/images/male-1.png',  // 1: 건강
    '/images/male-2.png',  // 2: 마른
    '/images/male-3.png',  // 3: 보통
    '/images/male-4.png',  // 4: 덜 찐
    '/images/male-5.png',  // 5: 살찐
  ],
}

const currentAvatarSrc = computed(() => {
  const gender = userGender.value
  const level = avatarLevel.value
  return avatarImages[gender]?.[level] || ''
})

const handleImageError = () => {
  console.error('아바타 이미지 로드 실패')
}

const avatarStatus = computed(() => {
  const statuses = ['', '건강', '마른', '보통', '덜 찐', '살찐']
  return statuses[avatarLevel.value]
})

// 운동량 업데이트
const updateExerciseProgress = () => {
  todayProgress.value = Math.floor(exerciseTimeInSeconds.value / 60)
}

// 아바타 레벨 체크 및 업데이트
const checkDailyGoal = () => {
  const today = new Date().toISOString().slice(0, 10)
  const lastChecked = localStorage.getItem('lastAvatarCheck')
  
  if (lastChecked === today) return // 오늘 이미 체크함
  
  const achieved = todayProgress.value >= dailyGoal.value
  
  if (achieved) {
    // 목표 달성: 1쪽으로 이동 (더 건강)
    avatarLevel.value = Math.max(1, avatarLevel.value - 1)
  } else {
    // 목표 미달: 5쪽으로 이동 (더 살찜)
    avatarLevel.value = Math.min(5, avatarLevel.value + 1)
  }
  
  // 아바타 상태 저장
  localStorage.setItem('avatarLevel', avatarLevel.value.toString())
  localStorage.setItem('lastAvatarCheck', today)
}

// 아바타 상태 로드 및 하루 체크
const loadAvatarState = () => {
  const savedLevel = localStorage.getItem('avatarLevel')
  if (savedLevel) {
    avatarLevel.value = parseInt(savedLevel)
  }
  
  // 하루동안 새싹을 못 얻었는지 체크
  checkDailySproutStatus()
}

// 하루동안 새싹 획득 여부 체크 및 레벨 조정
const checkDailySproutStatus = () => {
  const today = new Date().toISOString().slice(0, 10)
  const lastSproutDate = localStorage.getItem('lastSproutEarnedDate')
  const lastCheckDate = localStorage.getItem('lastSproutCheckDate')
  
  // 오늘 이미 체크했으면 스킵
  if (lastCheckDate === today) {
    return
  }
  
  // 어제 새싹을 얻었는지 확인
  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)
  const yesterdayStr = yesterday.toISOString().slice(0, 10)
  
  // 어제 새싹을 못 얻었다면 레벨 +1 (더 살찜)
  // lastSproutDate가 어제 날짜가 아니고 오늘 날짜도 아니면 어제 새싹을 못 얻은 것
  if (lastSproutDate && lastSproutDate !== yesterdayStr && lastSproutDate !== today) {
    avatarLevel.value = Math.min(5, avatarLevel.value + 1)
    localStorage.setItem('avatarLevel', avatarLevel.value.toString())
    console.log('⚠️ 어제 새싹을 못 얻어서 레벨 증가:', avatarLevel.value, '(어제:', yesterdayStr, ', 마지막 새싹:', lastSproutDate, ')')
  } else if (!lastSproutDate) {
    // 한번도 새싹을 얻지 못한 경우도 체크 (첫 방문일 수 있으므로 건너뜀)
    console.log('ℹ️ 새싹 획득 기록이 없습니다.')
  }
  
  // 오늘 체크 완료 표시
  localStorage.setItem('lastSproutCheckDate', today)
}

// 운동 시간 추적을 위한 watch
watch(exerciseTimeInSeconds, () => {
  updateExerciseProgress()
})
const chatInput = ref('')
const showSurveyPopup = ref(false)
const showShopPopup = ref(false)
const pendingSurveyRequests = ref<SurveyRequest[]>([])

// 새싹 관련
const sproutCount = ref(0)
const isProcessingSproutEarn = ref(false) // 새싹 획득 중복 방지

// 상점 아이템 데이터
const shopItems = ref([
  { id: 1, name: '광동쌍화탕', image: '/images/kwangdong-ssanghwatang.png', price: 20 },
  { id: 2, name: '까스활명수', image: '/images/cas-hwalmyeongsu.png', price: 15 },
  { id: 3, name: '레모나', image: '/images/lemona.png', price: 5 },
  { id: 4, name: '박카스', image: '/images/bacchus.png', price: 10 },
])
const currentChatPage = ref(0)
const messagesPerPage = 4
const chatPages = computed(() => Math.max(1, Math.ceil(chatMessages.value.length / messagesPerPage)))

const displayedMessages = computed(() => {
  const start = currentChatPage.value * messagesPerPage
  const end = start + messagesPerPage
  return chatMessages.value.slice(start, end)
})

// 컨텐츠 영역 대화창에는 운동 선생님 메시지만 표시
const botMessages = computed(() => {
  return chatMessages.value.filter(msg => msg.sender === 'guardian')
})

const currentBotChatPage = ref(0)
const botMessagesPerPage = 3
const botChatPages = computed(() => Math.max(1, Math.ceil(botMessages.value.length / botMessagesPerPage)))

const displayedBotMessages = computed(() => {
  const start = currentBotChatPage.value * botMessagesPerPage
  const end = start + botMessagesPerPage
  return botMessages.value.slice(start, end)
})

const nextBotChatPage = () => {
  if (currentBotChatPage.value < botChatPages.value - 1) {
    currentBotChatPage.value++
  }
}

const previousBotChatPage = () => {
  if (currentBotChatPage.value > 0) {
    currentBotChatPage.value--
  }
}

// 채팅 메시지 로드
const loadChatMessages = async (userId: string) => {
  try {
    chatMessages.value = await getChatMessages(userId)
    console.log('✅ 채팅 메시지 로드 완료:', chatMessages.value.length, '개')
  } catch (error) {
    console.error('❌ 채팅 메시지 로드 실패:', error)
  }
}

const nextChatPage = () => {
  if (currentChatPage.value < chatPages.value - 1) {
    currentChatPage.value++
  }
}

const previousChatPage = () => {
  if (currentChatPage.value > 0) {
    currentChatPage.value--
  }
}

let exerciseTimer: number | null = null

// 심박수 경고 체크
const checkHeartRateAlert = async () => {
  const hr = currentHeartRate.value
  const userId = localStorage.getItem('userId')
  
  if (!hr || hr <= 0 || !userId) return
  
  // 130bpm 초과 시 경고
  if (hr > 130) {
    const now = Date.now()
    // 마지막 경고로부터 1분 이내면 중복 방지
    if (now - lastAlertTime.value < 60000) {
      return
    }
    
    lastAlertTime.value = now
    
    // 팝업 표시
    showHeartRateAlertPopup.value = true
    
    // 백엔드에 경고 기록 (에러 발생해도 팝업은 표시)
    try {
      await createHeartRateAlert(Number(userId), hr)
      console.log('✅ 심박수 경고 기록 완료:', hr, 'bpm')
    } catch (error) {
      console.error('❌ 심박수 경고 기록 실패:', error)
      // 백엔드 기록 실패해도 팝업은 표시
    }
  }
}

// 심박수 모니터링 시작
const startHeartRateMonitoring = () => {
  // 5초마다 심박수 체크
  heartRateAlertCheckInterval = window.setInterval(() => {
    checkHeartRateAlert()
  }, 5000)
  
  // 초기 체크
  checkHeartRateAlert()
}

// 심박수 모니터링 중지
const stopHeartRateMonitoring = () => {
  if (heartRateAlertCheckInterval) {
    clearInterval(heartRateAlertCheckInterval)
    heartRateAlertCheckInterval = null
  }
}

// 심박수 경고 팝업 닫기
const closeHeartRateAlertPopup = () => {
  showHeartRateAlertPopup.value = false
}

onMounted(async () => {
  // localStorage에서 사용자 ID 확인
  const userId = localStorage.getItem('userId')
  const userCode = localStorage.getItem('userCode')

  if (!userId || !userCode) {
    // 로그인 정보가 없으면 등록 페이지로 이동
    alert('로그인이 필요합니다.')
    router.push('/user/register')
    return
  }

  // 아바타 상태 로드
  loadAvatarState()

  // 사용자 정보 로드
  await usersStore.fetchDetail(userId)
  
  // 디버깅: 사용자 데이터 확인
  console.log('=== 사용자 대시보드 - 사용자 정보 ===')
  console.log('currentUser:', currentUser.value)
  console.log('보호자 연락처:', currentUser.value?.guardianPhone)

  // 실시간 바이탈 구독
  metricsStore.subscribeRealtime(userId)

  // 채팅 메시지 로드 (에러 발생 시에도 계속 진행)
  try {
    await loadChatMessages(userId)
  } catch (error) {
    console.warn('⚠️ 채팅 메시지 로드 실패 (무시하고 계속):', error)
  }

  // 새싹 개수 로드
  try {
    await loadSproutCount(userId)
  } catch (error) {
    console.warn('⚠️ 새싹 개수 로드 실패 (무시하고 계속):', error)
  }

  // 대기 중인 설문 요청 조회
  await loadPendingSurveyRequests(userId)

  // 주기적으로 설문 요청 확인 (30초마다)
  setInterval(() => {
    loadPendingSurveyRequests(userId)
  }, 30000)

  // 설문 요청 이벤트 리스너
  window.addEventListener('surveyRequest', () => {
    showSurveyPopup.value = true
  })

  // 처방 확인 및 로드
  await checkPendingPrescription(userId)
  
  // 주기적으로 처방 확인 (30초마다)
  setInterval(() => {
    checkPendingPrescription(userId)
  }, 30000)
  
  // 자동 위치 추적 및 전송 (백그라운드)
  startLocationTracking(userId)
  
  // 심박수 모니터링 시작
  startHeartRateMonitoring()
})

onBeforeUnmount(() => {
  // 심박수 모니터링 중지
  stopHeartRateMonitoring()
})

// 위치 추적 시작
const startLocationTracking = (userId: string) => {
  if (!('geolocation' in navigator)) {
    console.warn('⚠️ 이 브라우저는 위치 서비스를 지원하지 않습니다.')
    return
  }

  // 위치 전송 함수
  const sendLocation = async (latitude: number, longitude: number) => {
    try {
      await upsertLocation(userId, {
        latitude,
        longitude,
        timestamp: Date.now()
      })
      console.log('✅ 위치 전송 완료:', { userId, latitude, longitude })
    } catch (error) {
      console.error('❌ 위치 전송 실패:', error)
    }
  }

  // 실시간 위치 추적
  const MAX_ACCURACY = 50 // m 단위. 원하시면 100~200으로 늘려도 됨
  let lastGoodPosition: { latitude: number; longitude: number } | null = null
  let hasValidLocation = false // 처음에 false → 좋은 값 들어오면 true로
  
  navigator.geolocation.watchPosition(
    (pos) => {
      const { latitude, longitude, accuracy } = pos.coords
      console.log('📡 geolocation 콜백 (UserDashboard):', latitude, longitude, '정확도:', accuracy)
      
      // 1) 정확도 체크
      if (!accuracy || accuracy > MAX_ACCURACY) {
        console.warn(`⚠️ 정확도 너무 나쁨 (${accuracy}m > ${MAX_ACCURACY}m), 값 무시`)
        
        // 아직 한 번도 쓸만한 값을 못 받은 상태면 → 그냥 무시하고 대기
        if (!hasValidLocation) {
          console.log('⏳ 아직 유효한 위치를 받지 못했습니다. 위치 잡는 중...')
          return
        }
        
        // 이미 예전에 lastGoodPosition이 있으면
        // 굳이 서버 전송을 쓰레기 값으로 덮을 필요 없음 → 그냥 유지
        console.log('✅ 이전 유효 위치 유지 (쓰레기 값 무시)')
        return
      }
      
      // 2) 여기까지 왔다는 건 "쓸만한 위치"라는 뜻
      lastGoodPosition = { latitude, longitude }
      hasValidLocation = true
      
      console.log(`✅ 유효한 위치 수신! 정확도: ${accuracy}m - 위치 전송`)
      // 즉시 한 번 전송
      sendLocation(latitude, longitude)
    },
    (err) => {
      console.warn('⚠️ 위치 접근 오류 (무시됨):', err.message)
      // 에러가 발생해도 다른 기능에는 영향 없음
    },
    { 
      enableHighAccuracy: true,  // GPS 정확도 향상
      maximumAge: 5000,  // 5초 이내의 위치만 사용 (최신 위치 보장)
      timeout: 15000  // 15초 타임아웃 (더 긴 시간 허용)
    }
  )

  // 주기적으로 위치 전송 (30초마다)
  // 주의: setInterval의 getCurrentPosition은 독립적이므로 위의 hasValidLocation을 공유
  setInterval(() => {
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const { latitude, longitude, accuracy } = pos.coords
        console.log('📡 주기적 위치 업데이트 (UserDashboard):', latitude, longitude, '정확도:', accuracy, 'm')
        
        // 1) 정확도 체크
        if (!accuracy || accuracy > MAX_ACCURACY) {
          console.warn(`⚠️ 정확도 너무 나쁨 (${accuracy}m > ${MAX_ACCURACY}m), 값 무시`)
          
          // 아직 한 번도 쓸만한 값을 못 받은 상태면 → 그냥 무시하고 대기
          if (!hasValidLocation) {
            console.log('⏳ 아직 유효한 위치를 받지 못했습니다. 위치 잡는 중...')
            return
          }
          
          // 이미 예전에 lastGoodPosition이 있으면
          // 굳이 서버 전송을 쓰레기 값으로 덮을 필요 없음 → 그냥 유지
          console.log('✅ 이전 유효 위치 유지 (쓰레기 값 무시)')
          return
        }
        
        // 2) 여기까지 왔다는 건 "쓸만한 위치"라는 뜻
        lastGoodPosition = { latitude, longitude }
        hasValidLocation = true
        
        console.log(`✅ 유효한 위치 수신! 정확도: ${accuracy}m - 주기 전송`)
        sendLocation(latitude, longitude)
      },
      (err) => {
        console.warn('⚠️ 위치 가져오기 실패 (무시됨):', err.message)
      },
      { 
        enableHighAccuracy: true,  // GPS 정확도 향상
        maximumAge: 5000,  // 5초 이내의 위치만 사용
        timeout: 15000  // 15초 타임아웃
      }
    )
  }, 30000)
  
  console.log('✅ 백그라운드 위치 추적 시작 (30초마다 자동 전송)')
}

// 처방 확인
const checkPendingPrescription = async (userId: string) => {
  try {
    console.log('🔍 대기 중인 처방 확인 중... userId:', userId)
    const prescription = await getPendingPrescription(userId)
    if (prescription) {
      pendingPrescription.value = prescription
      showPrescriptionPopup.value = true
      console.log('✅ 새로운 처방 발견:', prescription)
    } else {
      console.log('⚠️ 대기 중인 처방 없음')
    }
  } catch (error: any) {
    console.error('❌ 처방 확인 실패:', error)
    console.error('❌ 에러 상세:', {
      message: error?.message,
      status: error?.response?.status,
      data: error?.response?.data
    })
  }
  
  // 활성화된 처방 확인 (ACCEPTED 상태)
  try {
    console.log('🔍 활성 처방 확인 중... userId:', userId)
    const prescriptions = await getPrescriptionsByUser(userId)
    console.log('📋 모든 처방:', prescriptions)
    
    const accepted = prescriptions.find(p => p.status === 'ACCEPTED' && !p.completedAt)
    if (accepted) {
      // 새로운 처방이거나 이전 처방과 다른 경우 완료 상태 초기화
      if (!activePrescription.value || activePrescription.value.id !== accepted.id) {
        activePrescription.value = accepted
        resetExerciseCompleted()
      } else {
        activePrescription.value = accepted
      }
      hasActivePrescription.value = true
      console.log('✅ 활성 처방:', accepted)
      console.log('🔗 시작 스트레칭 URL:', accepted.startStretchingUrl)
      console.log('🔗 마무리 스트레칭 URL:', accepted.endStretchingUrl)
    } else {
      hasActivePrescription.value = false
      console.log('⚠️ 활성 처방 없음')
    }
  } catch (error: any) {
    console.error('❌ 활성 처방 확인 실패:', error)
    console.error('❌ 에러 상세:', {
      message: error?.message,
      status: error?.response?.status,
      data: error?.response?.data
    })
  }
}

// 처방 수락
const acceptPrescriptionHandler = async () => {
  if (!pendingPrescription.value?.id) {
    console.error('❌ pendingPrescription.value.id가 없습니다')
    return
  }
  
  try {
    console.log('🔍 처방 수락 시도:', pendingPrescription.value.id)
    const accepted = await acceptPrescription(pendingPrescription.value.id)
    console.log('✅ 처방 수락 API 응답:', accepted)
    
    activePrescription.value = accepted
    hasActivePrescription.value = true
    showPrescriptionPopup.value = false
    pendingPrescription.value = null
    
    // 새로운 처방 수락 시 운동 완료 상태 초기화
    resetExerciseCompleted()
    
    alert('처방을 수락했습니다!')
    console.log('✅ 처방 수락 완료')
  } catch (error: any) {
    console.error('❌ 처방 수락 실패:', error)
    console.error('❌ 에러 상세:', {
      message: error?.message,
      status: error?.response?.status,
      data: error?.response?.data
    })
    alert('처방 수락에 실패했습니다.')
  }
}

// 운동 완료 상태 초기화
const resetExerciseCompleted = () => {
  exerciseCompleted.value = {
    startStretching: false,
    intervals: [],
    endStretching: false
  }
  
  // 인터벌 배열 초기화 (세트 수만큼)
  if (activePrescription.value) {
    exerciseCompleted.value.intervals = new Array(activePrescription.value.sets).fill(false)
  }
}

// 모든 운동 완료 처리 (공통 함수)
const handleAllExercisesCompleted = async () => {
  // 중복 실행 방지
  if (isProcessingSproutEarn.value) {
    console.log('⚠️ 새싹 획득 처리 중입니다. 대기...')
    return
  }
  
  if (!activePrescription.value) {
    console.warn('⚠️ 활성 처방이 없습니다')
    return
  }
  
  const allCompleted = 
    exerciseCompleted.value.startStretching &&
    exerciseCompleted.value.intervals.length === activePrescription.value.sets &&
    exerciseCompleted.value.intervals.every(completed => completed) &&
    exerciseCompleted.value.endStretching
  
  if (!allCompleted) {
    console.log('📊 운동 완료 상태:', {
      startStretching: exerciseCompleted.value.startStretching,
      intervals: exerciseCompleted.value.intervals,
      endStretching: exerciseCompleted.value.endStretching,
      requiredSets: activePrescription.value.sets,
      intervalsLength: exerciseCompleted.value.intervals.length
    })
    return
  }
  
  // 중복 실행 방지 플래그 설정
  isProcessingSproutEarn.value = true
  
  try {
    console.log('🎉 오늘의 운동 완료!')
    
    const userId = localStorage.getItem('userId')
    if (!userId) {
      console.error('❌ userId가 없습니다')
      return
    }
    
    // 1. 처방 상태를 COMPLETED로 변경 (DB에 저장)
    if (activePrescription.value?.id) {
      try {
        await completePrescription(activePrescription.value.id)
        console.log('✅ 처방 완료 상태로 변경 완료 (DB 저장됨)')
      } catch (error) {
        console.error('❌ 처방 완료 상태 변경 실패:', error)
      }
    }
    
    // 2. 새싹 획득 (하루 1회 제한은 백엔드에서 처리)
    try {
      console.log('🌱 새싹 획득 시도... userId:', userId)
      const result = await earnSprout(userId)
      sproutCount.value = result.sproutCount
      console.log('✅ 새싹 획득 완료! 현재 새싹:', sproutCount.value)
      
      // 새싹 획득 시 아바타 레벨 -1 (더 건강해짐)
      avatarLevel.value = Math.max(1, avatarLevel.value - 1)
      localStorage.setItem('avatarLevel', avatarLevel.value.toString())
      
      // 오늘 새싹 획득 날짜 저장
      const today = new Date().toISOString().slice(0, 10)
      localStorage.setItem('lastSproutEarnedDate', today)
      
      console.log('✅ 아바타 레벨 변경:', avatarLevel.value)
      alert(`🎉 오늘의 운동 완료!\n🌱 새싹 +1 획득! (보유: ${sproutCount.value}개)\n레벨이 ${avatarLevel.value}로 변경되었습니다!`)
    } catch (error: any) {
      console.error('❌ 새싹 획득 실패:', error)
      console.error('❌ 에러 상세:', {
        message: error?.message,
        response: error?.response?.data,
        status: error?.response?.status,
        fullError: error
      })
      // 오늘 이미 받았으면 에러 메시지만 표시
      if (error.message?.includes('이미')) {
        alert('오늘은 이미 새싹을 받았습니다.')
      } else {
        alert('새싹 획득에 실패했습니다. 다시 시도해주세요.')
      }
    }
  } finally {
    // 플래그 해제
    isProcessingSproutEarn.value = false
  }
}

// 시작 스트레칭 완료
const completeStartStretching = async () => {
  console.log('🔍 시작 스트레칭 버튼 클릭')
  console.log('📋 activePrescription:', activePrescription.value)
  console.log('🔗 startStretchingUrl:', activePrescription.value?.startStretchingUrl)
  
  // URL이 있으면 새 창으로 열기
  if (activePrescription.value?.startStretchingUrl) {
    const url = activePrescription.value.startStretchingUrl
    console.log('✅ URL이 있음, 새 창으로 열기:', url)
    const newWindow = window.open(url, '_blank')
    if (!newWindow) {
      console.warn('⚠️ 팝업이 차단되었을 수 있습니다')
      alert('팝업 차단을 해제해주세요')
    } else {
      console.log('✅ 새 창 열림')
    }
  } else {
    console.log('ℹ️ URL이 없어서 새 창 열지 않음')
  }
  
  exerciseCompleted.value.startStretching = true
  
  // 운동 시작 상태 업데이트
  const userId = localStorage.getItem('userId')
  if (userId) {
    try {
      await updateExerciseStatus(userId, true)
      console.log('✅ 운동 시작 상태 업데이트 완료')
    } catch (error) {
      console.error('❌ 운동 상태 업데이트 실패:', error)
    }
  }
  
  console.log('✅ 시작 스트레칭 완료')
  await handleAllExercisesCompleted()
}

// 인터벌 세트 완료 (기존 함수 - 호환성 유지)
const completeInterval = async (setIndex: number) => {
  exerciseCompleted.value.intervals[setIndex] = true
  console.log(`✅ 인터벌 ${setIndex + 1}세트 완료`)
  await handleAllExercisesCompleted()
}

// 위치 가져오기 (명시적으로 권한 요청)
const getCurrentLocation = (): Promise<{ lat: number; lng: number }> => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('이 브라우저는 위치 서비스를 지원하지 않습니다.'))
      return
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const { latitude, longitude } = pos.coords
        resolve({ lat: latitude, lng: longitude })
      },
      (err) => {
        let errorMessage = '위치를 가져올 수 없습니다.'
        
        switch (err.code) {
          case err.PERMISSION_DENIED:
            errorMessage = '위치 권한이 거부되었습니다.\n브라우저 설정에서 위치 권한을 허용해주세요.'
            break
          case err.POSITION_UNAVAILABLE:
            errorMessage = '위치 정보를 사용할 수 없습니다.\nGPS 또는 네트워크 위치 서비스를 확인해주세요.'
            break
          case err.TIMEOUT:
            errorMessage = '위치 요청 시간이 초과되었습니다.\n잠시 후 다시 시도해주세요.'
            break
          default:
            errorMessage = `위치 오류: ${err.message}`
        }
        
        reject(new Error(errorMessage))
      },
      {
        enableHighAccuracy: false, // 노트북에서는 false가 더 빠름
        timeout: 10000,
        maximumAge: 60000, // 1분 이내 캐시된 위치 사용 가능
      }
    )
  })
}

// 공원 추천 가져오기
const loadRecommendedParks = async () => {
  if (!currentUser.value?.userCode) return
  
  loadingParks.value = true
  try {
    // 먼저 기존 위치 확인
    let lat = position.value?.lat
    let lon = position.value?.lng
    
    // 위치가 없으면 명시적으로 요청
    if (!lat || !lon) {
      try {
        const location = await getCurrentLocation()
        lat = location.lat
        lon = location.lng
        // 위치를 position에도 저장
        position.value = { lat, lng: lon }
      } catch (error: any) {
        alert(error.message || '현재 위치를 가져올 수 없습니다.')
        loadingParks.value = false
        return
      }
    }
    
    if (!lat || !lon) {
      alert('현재 위치를 가져올 수 없습니다. 위치 권한을 확인해주세요.')
      loadingParks.value = false
      return
    }
    
    const recommendation = await getStretchRecommendation(currentUser.value.userCode, lat, lon)
    
    // 공원추천이 배열인지 확인
    if (Array.isArray(recommendation.공원추천)) {
      recommendedParks.value = recommendation.공원추천.filter((park: any) => 
        park && park.title && park.address
      )
    } else {
      recommendedParks.value = []
    }
    
    if (recommendedParks.value.length === 0) {
      alert('근처 공원을 찾을 수 없습니다.')
    }
  } catch (error) {
    console.error('공원 추천 로드 실패:', error)
    alert('공원 추천을 불러오는 중 오류가 발생했습니다.')
  } finally {
    loadingParks.value = false
  }
}

// 공원 선택 시 경로 표시
const selectPark = (park: ParkRecommendation) => {
  if (!position.value?.lat || !position.value?.lng) {
    alert('현재 위치를 가져올 수 없습니다.')
    return
  }
  
  // 현재 위치를 출발지로, 공원 주소를 도착지로 설정
  const start = `${position.value.lng},${position.value.lat}`
  
  // 공원 주소를 좌표로 변환해야 하는데, 일단 주소 문자열로 전달
  // (RouteMap에서 주소를 좌표로 변환하거나, 백엔드에서 처리)
  const end = park.address
  
  // 모든 인터벌 세트를 한 번에 완료 처리
  if (exerciseCompleted.value.intervals.length !== activePrescription.value?.sets) {
    exerciseCompleted.value.intervals = new Array(activePrescription.value?.sets || 0).fill(false)
  }
  exerciseCompleted.value.intervals = new Array(activePrescription.value?.sets || 0).fill(true)
  console.log(`✅ 모든 인터벌 세트 완료`)
  
  // 처방 정보를 쿼리 파라미터로 전달
  const walkingMins = activePrescription.value?.walkingMinutes || 0
  const runningMins = activePrescription.value?.runningMinutes || 0
  const sets = activePrescription.value?.sets || 0
  
  // RouteMap 페이지를 새 창으로 열기
  const routeUrl = `/user/route?start=${encodeURIComponent(start)}&end=${encodeURIComponent(end)}&walkingMinutes=${walkingMins}&runningMinutes=${runningMins}&sets=${sets}`
  const newWindow = window.open(routeUrl, '_blank', 'width=1200,height=800')
  if (!newWindow) {
    console.warn('⚠️ 팝업이 차단되었을 수 있습니다')
    alert('팝업 차단을 해제해주세요')
  } else {
    console.log('✅ RouteMap 새 창 열림')
  }
  
  showParkSelectionPopup.value = false
  handleAllExercisesCompleted()
}

// 다음 미완료 인터벌 세트 완료 처리
const completeNextInterval = async () => {
  if (!activePrescription.value) return
  
  // 공원 추천 가져오기
  await loadRecommendedParks()
  
  // 공원이 있으면 선택 모달 표시, 없으면 바로 완료 처리
  if (recommendedParks.value.length > 0) {
    showParkSelectionPopup.value = true
  } else {
    // 공원이 없으면 바로 완료 처리
    if (exerciseCompleted.value.intervals.length !== activePrescription.value.sets) {
      exerciseCompleted.value.intervals = new Array(activePrescription.value.sets).fill(false)
    }
    exerciseCompleted.value.intervals = new Array(activePrescription.value.sets).fill(true)
    await handleAllExercisesCompleted()
  }
}

// 마무리 스트레칭 완료
const completeEndStretching = async () => {
  console.log('🔍 마무리 스트레칭 버튼 클릭')
  console.log('📋 activePrescription:', activePrescription.value)
  console.log('🔗 endStretchingUrl:', activePrescription.value?.endStretchingUrl)
  
  // URL이 있으면 새 창으로 열기
  if (activePrescription.value?.endStretchingUrl) {
    const url = activePrescription.value.endStretchingUrl
    console.log('✅ URL이 있음, 새 창으로 열기:', url)
    const newWindow = window.open(url, '_blank')
    if (!newWindow) {
      console.warn('⚠️ 팝업이 차단되었을 수 있습니다')
      alert('팝업 차단을 해제해주세요')
    } else {
      console.log('✅ 새 창 열림')
    }
  } else {
    console.log('ℹ️ URL이 없어서 새 창 열지 않음')
  }
  
  exerciseCompleted.value.endStretching = true
  
  // 운동 종료 상태 업데이트
  const userId = localStorage.getItem('userId')
  if (userId) {
    try {
      await updateExerciseStatus(userId, false)
      console.log('✅ 운동 종료 상태 업데이트 완료')
    } catch (error) {
      console.error('❌ 운동 상태 업데이트 실패:', error)
    }
  }
  
  console.log('✅ 마무리 스트레칭 완료')
  await handleAllExercisesCompleted()
}

// 처방 거부
const declinePrescriptionHandler = async () => {
  if (!pendingPrescription.value?.id) {
    console.error('❌ pendingPrescription.value.id가 없습니다')
    return
  }
  
  try {
    console.log('🔍 처방 거부 시도:', pendingPrescription.value.id)
    await declinePrescription(pendingPrescription.value.id)
    showPrescriptionPopup.value = false
    pendingPrescription.value = null
    console.log('✅ 처방 거부 완료')
  } catch (error: any) {
    console.error('❌ 처방 거부 실패:', error)
    console.error('❌ 에러 상세:', {
      message: error?.message,
      status: error?.response?.status,
      data: error?.response?.data
    })
    alert('처방 거부에 실패했습니다.')
  }
}

// 처방 팝업 닫기
const closePrescriptionPopup = () => {
  showPrescriptionPopup.value = false
}

const loadPendingSurveyRequests = async (userId: string) => {
  try {
    pendingSurveyRequests.value = await getPendingSurveyRequests(userId)
  } catch (error) {
    console.error('설문 요청 조회 실패:', error)
  }
}

const goToSurvey = (requestId: string) => {
  router.push(`/user/survey/${requestId}`)
}

const toggleWorkout = async () => {
  const wasExercising = isExercising.value
  isExercising.value = !isExercising.value
  
  // 백엔드에 운동 상태 전송
  const userId = localStorage.getItem('userId')
  if (userId) {
    try {
      await updateExerciseStatus(userId, isExercising.value)
    } catch (error) {
      console.error('운동 상태 업데이트 실패:', error)
    }
  }

  if (isExercising.value) {
    // 운동 시작
    exerciseTimer = window.setInterval(() => {
      exerciseTimeInSeconds.value += 1
      // 간단한 칼로리 계산 (1분당 5kcal 가정)
      caloriesBurned.value = Math.floor((exerciseTimeInSeconds.value / 60) * 5)
    }, 1000)
  } else {
    // 운동 중지
    if (exerciseTimer) {
      clearInterval(exerciseTimer)
      exerciseTimer = null
    }
    
    // 운동 종료 시 일일 목표 체크
    if (wasExercising) {
      checkDailyGoal()
    }
  }
}

const exerciseTimeFormatted = computed(() => {
  const minutes = Math.floor(exerciseTimeInSeconds.value / 60)
  const seconds = exerciseTimeInSeconds.value % 60
  return `${minutes}분 ${seconds}초`
})

const sendChatMessage = async () => {
  if (!chatInput.value.trim()) return

  const userId = localStorage.getItem('userId')
  if (!userId || !currentUser.value) {
    alert('사용자 정보를 확인할 수 없습니다.')
    return
  }

  const messageText = chatInput.value
  chatInput.value = ''

  try {
    // API로 메시지 전송
    const newMessage = await sendChatAPI({
      userId: userId,
      sender: 'user',
      senderName: currentUser.value.name,
      message: messageText
    })

    // 로컬 채팅에 추가
    chatMessages.value.push(newMessage)
    console.log('✅ 메시지 전송 완료:', newMessage)

    // 새 메시지가 추가되면 마지막 페이지로 이동
    currentChatPage.value = chatPages.value - 1
  } catch (error) {
    console.error('❌ 메시지 전송 실패:', error)
    alert('메시지 전송에 실패했습니다.')
    chatInput.value = messageText // 실패 시 입력 복원
  }
}


const closeSurveyPopup = () => {
  showSurveyPopup.value = false
}

const acceptSurvey = () => {
  showSurveyPopup.value = false
  router.push('/user/survey')
}

const declineSurvey = () => {
  showSurveyPopup.value = false
  console.log('사용자가 설문을 거부했습니다.')
}

// 새싹 개수 로드
const loadSproutCount = async (userId: string) => {
  try {
    sproutCount.value = await getSproutCount(userId)
    console.log('✅ 새싹 개수 로드 완료:', sproutCount.value)
  } catch (error) {
    console.error('❌ 새싹 개수 로드 실패:', error)
    sproutCount.value = 0
  }
}

// 상점 아이템 구매
const buyItem = async (item: { id: number; name: string; price: number }) => {
  if (sproutCount.value < item.price) {
    alert(`보유 새싹이 부족합니다.\n보유: ${sproutCount.value}개\n필요: ${item.price}개`)
    return
  }

  const userId = localStorage.getItem('userId')
  if (!userId) {
    alert('사용자 정보를 확인할 수 없습니다.')
    return
  }

  if (!confirm(`${item.name}을(를) 구매하시겠습니까?\n🌱 ${item.price}개 차감`)) {
    return
  }

  try {
    const result = await spendSprouts(userId, item.price)
    sproutCount.value = result.sproutCount
    alert(`✅ ${item.name} 구매 완료!\n남은 새싹: ${sproutCount.value}개`)
    console.log('✅ 아이템 구매 완료:', item.name, '남은 새싹:', sproutCount.value)
  } catch (error: any) {
    console.error('❌ 아이템 구매 실패:', error)
    alert(error.message || '구매에 실패했습니다.')
  }
}

const closeShopPopup = () => {
  showShopPopup.value = false
}

// 블루투스 재연결
const reconnectBluetooth = async () => {
  const userId = localStorage.getItem('userId')
  if (!userId) {
    alert('사용자 정보를 확인할 수 없습니다.')
    return
  }

  try {
    console.log('🔵 블루투스 재연결 요청 중...')
    await http.post(`/api/bluetooth/reconnect`, { userId })
    console.log('✅ 블루투스 재연결 요청 완료')
    alert('블루투스 재연결 요청이 완료되었습니다.')
  } catch (error: any) {
    console.error('❌ 블루투스 재연결 실패:', error)
    alert('블루투스 재연결에 실패했습니다.')
  }
}
</script>

<style scoped>
/* 심박수 주의 팝업 */
.HeartRateAlertPopupOverlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 152, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(4px);
}

.HeartRateAlertPopupContent {
  background: white;
  border-radius: 16px;
  padding: 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  min-width: 300px;
  max-width: 400px;
}

.HeartRateAlertPopupTitle {
  font-size: 24px;
  font-weight: bold;
  color: #FF9800;
  font-family: 'Gowun Dodum', sans-serif;
  text-align: center;
}

.HeartRateAlertPopupMessage {
  font-size: 16px;
  color: #333;
  font-family: 'Gowun Dodum', sans-serif;
  text-align: center;
  line-height: 1.6;
}

.HeartRateAlertPopupActions {
  margin-top: 8px;
  width: 100%;
  display: flex;
  justify-content: center;
}

.HeartRateAlertPopupConfirmButton {
  padding: 12px 32px;
  background-color: #FF9800;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  font-family: 'Gowun Dodum', sans-serif;
  cursor: pointer;
  transition: all 0.2s;
}

.HeartRateAlertPopupConfirmButton:hover {
  background-color: #F57C00;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
}

.HeartRateAlertPopupConfirmButton:active {
  transform: translateY(0);
}

@media (max-width: 768px) {
  .HeartRateAlertPopupContent {
    padding: 24px;
    min-width: 250px;
    margin: 16px;
  }

  .HeartRateAlertPopupTitle {
    font-size: 20px;
  }

  .HeartRateAlertPopupMessage {
    font-size: 14px;
  }
}
</style>

