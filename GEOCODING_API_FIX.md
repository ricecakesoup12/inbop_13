# 네이버 주소 API (Geocoding) 인증 실패 해결

## 🔴 현재 문제

```
GET https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc-js
401 (Unauthorized)
```

**에러 원인:**
- Geocoding 서비스가 활성화되지 않음
- 또는 Client ID가 잘못됨

---

## ✅ 해결 방법

### 1️⃣ 네이버 클라우드 콘솔 - Geocoding 서비스 활성화 ⭐⭐⭐

**가장 중요한 단계입니다!**

1. https://console.ncloud.com/ncloud/application 접속

2. **AI·NAVER API** 메뉴 클릭

3. **Application 등록** 클릭

4. `inbop2` Application 찾아서 **클릭** (수정 모드로 진입)

5. **"서비스 선택" 탭 클릭**

6. 다음 항목들을 **모두 체크**:
   ```
   ☑ Maps
     ☑ Web Dynamic Map
   
   ☑ Geocoding  ⭐ 이것도 체크해야 합니다!
     ☑ Geocoding (주소 → 좌표)
     ☑ Reverse Geocoding (좌표 → 주소)  ⭐ 이것도 체크!
   ```

7. **저장** 또는 **수정** 버튼 클릭

8. 확인: 모든 체크박스가 선택되어 있고 저장되었는지 다시 확인

---

### 2️⃣ 서비스 URL 재확인

**서비스 환경 설정** 탭에서:

#### 등록해야 할 URL:
```
http://localhost:5173
http://localhost:5174
http://127.0.0.1:5173
http://127.0.0.1:5174
```

**주의사항:**
- 끝에 슬래시(`/`) 없이
- `http://` 프로토콜 사용 (https 아님)
- 포트 번호 정확히 입력

---

### 3️⃣ Client ID 확인

`.env` 파일 확인:
```bash
cd Exercise/main
type .env
```

**출력되어야 하는 내용:**
```
VITE_NAVER_CLIENT_ID=yvhdahq4gx
```

만약 다르다면:
```bash
echo VITE_NAVER_CLIENT_ID=yvhdahq4gx > .env
```

---

### 4️⃣ 개발 서버 재시작

**.env 파일이 변경되었거나 서비스 설정이 변경되었다면 반드시 재시작해야 합니다!**

```bash
cd Exercise/main
# Ctrl + C (서버 종료)
npm run dev
```

---

### 5️⃣ 브라우저 캐시 삭제

#### 방법 1: 시크릿/InPrivate 모드 (권장)
- Chrome: `Ctrl + Shift + N`
- Edge: `Ctrl + Shift + P`
- 새 창에서 http://localhost:5173 접속

#### 방법 2: 강력 새로고침
- `Ctrl + Shift + R` (Windows)
- `Cmd + Shift + R` (Mac)

---

## 🧪 테스트

### 브라우저 콘솔에서 확인

F12를 누르고 Console 탭:

```javascript
// 1. Client ID 확인
console.log(import.meta.env.VITE_NAVER_CLIENT_ID)
// 출력: yvhdahq4gx

// 2. 네이버 객체 확인
console.log(window.naver?.maps?.Service)
// 정상: {reverseGeocode: ƒ, geocode: ƒ, ...}
// 실패: undefined

// 3. Reverse Geocoding 함수 확인
console.log(typeof window.naver?.maps?.Service?.reverseGeocode)
// 정상: "function"
// 실패: "undefined"
```

### 지도에서 위치 클릭 시 주소 변환 확인

1. 지도에서 위치 클릭
2. Console 탭에서 다음 메시지 확인:
   - ✅ `✅ 네이버 주소 변환 성공` → 정상 작동
   - ❌ `⚠️ 네이버 주소 변환 실패` → 인증 실패

---

## 🎯 네이버 클라우드 콘솔 상세 가이드

### Application 수정 화면 진입 방법

1. https://console.ncloud.com 로그인

2. 상단 메뉴: **Services** → **AI·NAVER API**

3. 왼쪽 메뉴: **Application 등록**

4. Application 목록에서 `inbop2` **클릭**

5. 탭 메뉴 확인:
   - **기본 정보**: Application 이름, Client ID 표시
   - **서비스 선택**: ⭐ **여기서 Maps와 Geocoding 체크!**
   - **서비스 환경 설정**: Web 서비스 URL 등록

### 서비스 선택 탭 - 필수 체크 항목

```
☑ Maps
  ☑ Web Dynamic Map

☑ Geocoding  ⭐ 이것도 반드시 체크!
  ☑ Geocoding (주소 → 좌표)
  ☑ Reverse Geocoding (좌표 → 주소)  ⭐ 이것도 반드시 체크!
```

**중요:** 
- `Maps`만 체크하면 지도는 보이지만 주소 변환은 안 됩니다
- `Geocoding`도 체크해야 주소 변환이 작동합니다
- 특히 `Reverse Geocoding`이 체크되어 있어야 좌표 → 주소 변환이 됩니다

---

## 🔍 일반적인 실수

### ❌ 실수 1: Geocoding 서비스를 체크하지 않음
- Maps만 체크하고 Geocoding은 체크하지 않음
- 지도는 보이지만 주소 변환은 401 에러 발생

**해결:** 콘솔 → Application → 서비스 선택 → Geocoding 체크 → 저장

### ❌ 실수 2: Reverse Geocoding만 체크하지 않음
- Geocoding은 체크했지만 Reverse Geocoding은 체크하지 않음
- 좌표 → 주소 변환이 안 됨

**해결:** Geocoding 하위의 "Reverse Geocoding"도 체크

### ❌ 실수 3: 개발 서버를 재시작하지 않음
- 서비스 설정 변경 후 서버를 재시작하지 않음

**해결:** Ctrl + C 후 npm run dev

---

## ✅ 최종 체크리스트

순서대로 확인하세요:

- [ ] 네이버 클라우드 콘솔 로그인
- [ ] Application `inbop2` 클릭
- [ ] **"서비스 선택" 탭**에서 **Maps** 체크됨
- [ ] **"서비스 선택" 탭**에서 **Web Dynamic Map** 체크됨
- [ ] **"서비스 선택" 탭**에서 **Geocoding** 체크됨 ⭐
- [ ] **"서비스 선택" 탭**에서 **Reverse Geocoding** 체크됨 ⭐
- [ ] 저장 버튼 클릭함
- [ ] Web 서비스 URL에 `http://localhost:5173` 등록됨
- [ ] `.env` 파일에 `VITE_NAVER_CLIENT_ID=yvhdahq4gx` 입력됨
- [ ] 개발 서버 재시작 (Ctrl+C 후 npm run dev)
- [ ] 브라우저 캐시 삭제 또는 시크릿 모드 사용
- [ ] `window.naver.maps.Service.reverseGeocode`가 함수로 존재함
- [ ] 지도에서 위치 클릭 시 주소가 정상적으로 표시됨

---

## 📞 여전히 안 되면

### 브라우저 콘솔에서 전체 디버그 정보 확인

```javascript
console.log({
  clientId: import.meta.env.VITE_NAVER_CLIENT_ID,
  naver: window.naver,
  maps: window.naver?.maps,
  service: window.naver?.maps?.Service,
  reverseGeocode: typeof window.naver?.maps?.Service?.reverseGeocode,
  error: window.naver?.maps?.error || window.naver?.error
})
```

이 출력 결과를 확인하여 어느 부분이 문제인지 파악하세요.

---

**Geocoding 서비스를 활성화하면 주소 API가 정상 작동합니다!** 🎉




