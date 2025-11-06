# 네이버 지도 API 신규 버전 마이그레이션 가이드

## 🔄 주요 변경사항

### 1. 파라미터 이름 변경
**변경 전** (구 API):
```javascript
<script src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpClientId=YOUR_CLIENT_ID"></script>
```

**변경 후** (신규 API):
```javascript
<script src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=YOUR_CLIENT_ID"></script>
```

⚠️ **중요**: `ncpClientId` → `ncpKeyId` 변경!

---

## ✅ 적용된 수정사항

### 1. `loadNaverMap.ts` 업데이트
```typescript
// 신규 API 파라미터 사용
script.src = `https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${clientId}&submodules=${submodules}`;
```

### 2. 인증 실패 콜백 추가
```typescript
// 전역 콜백 함수 등록
window.navermap_authFailure = function () {
  console.error('❌ 네이버 지도 API 인증 실패');
  console.error('클라이언트 ID와 서비스 URL을 확인해주세요.');
};
```

---

## 📋 체크리스트

### ✅ 완료된 작업
- [x] `loadNaverMap.ts`에서 `ncpKeyId` 파라미터 사용
- [x] `window.navermap_authFailure` 콜백 함수 등록
- [x] 인증 실패 시 에러 로깅

### 🔍 확인 필요
- [ ] 브라우저 네트워크 탭에서 스크립트 URL 확인
- [ ] 지도 정상 작동 확인
- [ ] 주소 변환(Reverse Geocoding) 작동 확인

---

## 🧪 테스트 방법

### 1. 개발 서버 재시작
```bash
cd Exercise/main
npm run dev
```

### 2. 브라우저 네트워크 탭 확인
1. F12 → Network 탭
2. 페이지 새로고침
3. `maps.js` 파일 찾기
4. URL 확인:
   ```
   https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=yvhdahq4gx&submodules=geocoder
   ```
   ✅ `ncpKeyId=yvhdahq4gx` 확인!

### 3. 콘솔 확인
**정상 작동 시**:
```
🗺️ 네이버 지도 로드 시작... yvhdahq4gx...
네이버 지도 스크립트 로드 시작: https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=yvhdahq4gx&submodules=geocoder
✅ 네이버 지도 스크립트 로드 완료
✅ 네이버 지도 API 초기화 완료!
✅ 지도 생성 완료
```

**인증 실패 시**:
```
❌ 네이버 지도 API 인증 실패 (navermap_authFailure 콜백 호출)
클라이언트 ID와 서비스 URL을 확인해주세요.
```

### 4. 주소 API (Reverse Geocoding) 테스트
1. 실시간 위치 보기 페이지 접속
2. 위치 권한 허용
3. 콘솔에서 주소 변환 로그 확인:
   ```
   📍 현재 위치: 37.5665, 126.9780
   📍 현재 주소 (OSM): 서울특별시 중구 세종대로 110
   ```

---

## 🌐 네이버 클라우드 플랫폼 설정

### 신규 클라이언트 ID 발급
1. [네이버 클라우드 플랫폼](https://console.naver.com/ncloud/application) 접속
2. **Maps > Application 등록**
3. **신규 Web Dynamic Map** 선택
4. 서비스 URL 등록:
   ```
   http://localhost:5173
   http://127.0.0.1:5173
   ```
5. 클라이언트 ID 복사: `yvhdahq4gx`

### .env 파일 업데이트
```env
# 신규 클라이언트 ID (ncpKeyId)
VITE_NAVER_CLIENT_ID=yvhdahq4gx
```

---

## 🔍 Reverse Geocoding (주소 변환) 작동 확인

### 네이버 API 사용 (우선)
```javascript
naver.maps.Service.reverseGeocode(
  { coords: new naver.maps.LatLng(37.5665, 126.9780) },
  function (status, response) {
    if (status === naver.maps.Service.Status.OK) {
      const address = response.v2.addresses[0];
      const result = address.roadAddress || address.jibunAddress;
      console.log('주소:', result);
    }
  }
);
```

### OSM Fallback (인증 실패 시)
현재 코드는 네이버 API 실패 시 자동으로 OpenStreetMap Nominatim API를 사용합니다:
```javascript
// LiveMap.vue에서
const address = await fallbackReverseGeocode(latitude, longitude);
```

---

## 📊 API 비교

| 항목 | 구 API | 신규 API |
|------|--------|----------|
| 파라미터 | `ncpClientId` | `ncpKeyId` |
| URL | 동일 | 동일 |
| 인증 방식 | Referer 헤더 | Referer 헤더 |
| 콜백 함수 | `window.navermap_authFailure` | `window.navermap_authFailure` |
| Reverse Geocoding | 지원 | 지원 |
| 무료 한도 | 10만 건/일 | 10만 건/일 |

---

## 🐛 문제 해결

### 문제: "ncpClientId" 파라미터 사용 중
**원인**: 구 API 파라미터 사용  
**해결**: `loadNaverMap.ts` 업데이트 완료 (자동 반영)

### 문제: 인증 실패 계속 발생
**원인**: 서비스 URL 미등록 또는 클라이언트 ID 불일치  
**해결**:
1. 네이버 클라우드 콘솔에서 서비스 URL 확인
2. `http://localhost:5173` 등록 확인
3. 클라이언트 ID가 `yvhdahq4gx`인지 확인
4. 1~2분 대기 후 재시도

### 문제: 주소 변환 안 됨
**원인**: Reverse Geocoding API 미활성화  
**해결**:
1. 네이버 클라우드 콘솔
2. Maps > Web Dynamic Map
3. **Reverse Geocoding** 활성화 확인
4. OSM Fallback이 자동으로 작동하므로 주소는 표시됨

---

## 📝 변경된 파일

1. ✅ `Exercise/main/src/utils/loadNaverMap.ts`
   - 파라미터 `ncpClientId` → `ncpKeyId` 변경
   - `window.navermap_authFailure` 콜백 추가

2. ✅ `Exercise/main/.env`
   - `VITE_NAVER_CLIENT_ID=yvhdahq4gx` 설정

---

## 🎯 다음 단계

1. **개발 서버 재시작**: `npm run dev`
2. **브라우저 하드 리로드**: `Ctrl + Shift + R`
3. **네트워크 탭 확인**: `ncpKeyId` 파라미터 사용 확인
4. **지도 작동 테스트**: 
   - 사용자: 실시간 위치 보기
   - 보호자: 전체 사용자 위치 보기
5. **주소 변환 테스트**: 콘솔에서 주소 출력 확인

---

**작성일**: 2025년 11월 5일  
**신규 클라이언트 ID**: `yvhdahq4gx`  
**API 버전**: 신규 통합 API (ncpKeyId)

