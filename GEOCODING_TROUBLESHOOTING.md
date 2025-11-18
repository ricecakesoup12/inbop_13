# 주소 API (Reverse Geocoding) 문제 해결 가이드

## 🔍 문제 증상
- 실시간 위치 보기에서 "주소를 가져오는 중..." 이 계속 표시됨
- 좌표는 나오지만 주소가 변환되지 않음

---

## 🛠️ 해결 방법

### 1️⃣ 네이버 클라우드 플랫폼 설정 확인

#### Geocoding API 권한 활성화
1. [네이버 클라우드 플랫폼 콘솔](https://console.naver.com/ncloud/application) 접속
2. **AI·NAVER API > Application > 내 애플리케이션** 선택
3. **Maps 탭** 클릭
4. 다음 서비스가 **모두 활성화**되어 있는지 확인:
   - ✅ **Web Dynamic Map** (지도 표시)
   - ✅ **Geocoding** (주소 → 좌표 변환)
   - ✅ **Reverse Geocoding** (좌표 → 주소 변환) ⭐ **필수!**

5. 서비스 URL 등록 확인:
   ```
   http://localhost:5173
   http://localhost:5174
   http://127.0.0.1:5173
   http://127.0.0.1:5174
   ```

6. 변경 사항이 있다면 **저장** 클릭

---

### 2️⃣ 브라우저 콘솔 확인

1. 브라우저에서 **F12** 키를 눌러 개발자 도구 열기
2. **Console** 탭 확인
3. 다음 메시지 중 하나가 나타나는지 확인:

#### 🟢 정상 작동
```
✅ 네이버 지도 API 초기화 완료!
📍 현재 위치: 37.5665, 126.9780
주소 변환 성공: 서울특별시 중구 세종대로 110
📍 현재 주소: 서울특별시 중구 세종대로 110
```

#### 🔴 네이버 API 인증 실패
```
⚠️ reverseGeocode 사용 불가: 인증 실패 또는 권한 미설정 → OSM Fallback 시도
```
👉 **해결**: 네이버 클라우드에서 Reverse Geocoding API 활성화

#### 🟡 네이버 API 실패 → OSM 사용
```
주소 변환 실패: ERROR → OSM Fallback 시도
📍 현재 주소: South Korea, Seoul, Jung-gu...
```
👉 정상 작동 (OSM으로 대체됨), 하지만 주소 형식이 다름

---

### 3️⃣ 코드 수정 (필요 시)

현재 코드에는 이미 Fallback이 구현되어 있습니다:
- **1차 시도**: 네이버 Reverse Geocoding API
- **2차 시도**: OpenStreetMap Nominatim (무료, 인증 불필요)

만약 네이버 API가 계속 실패한다면, OSM만 사용하도록 수정할 수 있습니다.

#### 네이버 API 스킵하고 OSM만 사용 (옵션)

**파일**: `Exercise/main/src/pages/user/LiveMap.vue`

**수정 전** (287-306줄):
```typescript
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
```

**수정 후** (네이버 API 스킵):
```typescript
// Reverse Geocoding으로 도로명 주소 가져오기 (OSM 직접 사용)
setTimeout(async () => {
  // 네이버 API 대신 OSM 직접 사용
  const address = await fallbackReverseGeocode(latitude, longitude);
  if (address) {
    currentAddress.value = address;
    console.log('📍 현재 주소 (OSM):', address);
  } else {
    console.warn('주소를 가져올 수 없습니다.');
  }
}, 500);
```

---

### 4️⃣ 환경 변수 확인

**파일**: `Exercise/main/.env`

```env
# 네이버 지도 API 클라이언트 ID (필수)
VITE_NAVER_CLIENT_ID=여기에_실제_클라이언트ID_입력

# 백엔드 API 기본 URL
VITE_API_BASE=http://localhost:8081

# 기본 사용자 ID (테스트용)
VITE_USER_ID=1
```

확인 사항:
- `VITE_NAVER_CLIENT_ID`가 올바른 값인지 확인
- 앞뒤 공백이 없는지 확인
- 따옴표가 없는지 확인

---

### 5️⃣ 서버 재시작

설정 변경 후 **반드시 재시작** 필요:

```bash
# 프론트엔드 서버 재시작
cd Exercise/main
# Ctrl+C로 중지
npm run dev
```

```bash
# 백엔드 서버 재시작
# Ctrl+C로 중지
.\mvnw.cmd spring-boot:run
```

---

### 6️⃣ 브라우저 캐시 삭제

1. **Chrome/Edge**: `Ctrl + Shift + R` (강력 새로고침)
2. **Firefox**: `Ctrl + Shift + R`
3. **Safari**: `Cmd + Option + R`

또는:
1. `F12` 개발자 도구 열기
2. 네트워크 탭
3. "Disable cache" 체크박스 활성화

---

## 🔍 디버깅 단계별 확인

### 1단계: 지도가 표시되는가?
- ✅ 예 → 2단계로
- ❌ 아니오 → 네이버 지도 API 인증 실패 (CLIENT_ID 확인)

### 2단계: 위치 좌표가 나오는가? (예: 37.5665, 126.9780)
- ✅ 예 → 3단계로
- ❌ 아니오 → 위치 권한 거부 또는 GPS 문제

### 3단계: 콘솔에 "주소 변환 성공" 또는 "OSM Fallback" 메시지가 있는가?
- ✅ "주소 변환 성공" → 정상 작동 중
- ✅ "OSM Fallback" → OSM으로 작동 중 (정상)
- ❌ "주소를 가져올 수 없습니다" → 4단계로

### 4단계: 네트워크 탭에서 API 요청 확인
- `nominatim.openstreetmap.org` 요청이 있는가?
  - ✅ 200 OK → OSM 정상 작동
  - ❌ 에러 → 네트워크 문제 또는 OSM 서버 다운

---

## 🌐 API 대안

### OpenStreetMap Nominatim (현재 Fallback으로 사용 중)
- **장점**: 무료, 인증 불필요, 한국어 지원
- **단점**: 주소 형식이 네이버와 다름 (예: "South Korea, Seoul, Jung-gu...")
- **속도**: 네이버보다 약간 느림
- **사용 제한**: 1초에 1번 요청 (현재 코드에서는 10초마다 위치 전송하므로 문제없음)

### 네이버 Reverse Geocoding API
- **장점**: 한국 주소 형식 정확, 도로명 주소 제공
- **단점**: 인증 필요, 일일 무료 한도 있음 (10만 건)
- **속도**: 빠름

---

## 📊 현재 구현된 로직

```
사용자 위치 업데이트
    ↓
네이버 Reverse Geocoding API 시도
    ↓
성공? ──Yes→ 주소 표시
    ↓ No
OSM Nominatim API 시도
    ↓
성공? ──Yes→ 주소 표시
    ↓ No
"주소를 가져올 수 없습니다" 표시
```

이 로직 덕분에 네이버 API가 실패해도 OSM으로 자동 전환되어 주소가 표시됩니다.

---

## ✅ 권장 사항

### 개발 환경
- OSM Nominatim만 사용 (인증 불필요, 설정 간단)
- 주소 형식이 조금 다르지만 충분히 사용 가능

### 프로덕션 환경
- 네이버 Reverse Geocoding API 사용 (한국 주소 형식 정확)
- Fallback으로 OSM 유지 (네이버 API 장애 대비)

---

## 🆘 문제가 계속되면

1. **브라우저 콘솔 로그 캡처**해서 확인
2. **네트워크 탭**에서 API 요청/응답 확인
3. **네이버 클라우드 콘솔**에서 API 사용량 확인 (무료 한도 초과 여부)

---

**작성일**: 2025년 11월 5일  
**관련 파일**: `Exercise/main/src/pages/user/LiveMap.vue`

