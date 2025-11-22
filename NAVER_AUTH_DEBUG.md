# 네이버 지도 API 인증 실패 해결 가이드

## 🔍 현재 문제

```
❌ 네이버 지도 인증 실패 또는 API 미초기화
Error: NAVER_MAPS_AUTH_FAILED
```

- `window.naver`: ✅ 존재함
- `window.naver.maps`: ❌ undefined (인증 실패)

## 📋 브라우저 콘솔에서 확인하기

F12를 누르고 Console 탭에서 다음 명령어를 입력하세요:

```javascript
// 1. window.naver 전체 구조 확인
console.log(window.naver)

// 2. window.naver.maps 확인
console.log(window.naver?.maps)

// 3. 에러 메시지 확인 (인증 실패 시 표시됨)
console.log(window.naver?.maps?.error)
```

### 예상 결과

#### 인증 성공 시:
```javascript
window.naver.maps: {Map: ƒ, LatLng: ƒ, Point: ƒ, ...}
```

#### 인증 실패 시:
```javascript
window.naver: {error: "AUTHENTICATION_FAILED", message: "..."}
window.naver.maps: undefined
```

---

## 🔧 해결 방법

### 1️⃣ 네이버 클라우드 플랫폼 설정 확인

https://console.ncloud.com/ncloud/application 접속

#### 확인 사항:

**A. Application 선택**
- Application 이름: `inbop2`
- Client ID: `yyhdahq4gx`

**B. 서비스 선택 확인** ⭐ 중요!
- `AI·NAVER API` → `Application 등록`
- **Maps** 서비스 선택
- **Web Dynamic Map** 체크되어 있는지 확인

**C. Web 서비스 URL 확인**

다음 URL들이 **정확히** 등록되어 있어야 합니다:

```
http://localhost:5173
http://localhost:5174
http://127.0.0.1:5173
http://127.0.0.1:5174
```

**주의사항:**
- ❌ `http://localhost:5173/` (슬래시 있음) → 작동 안 할 수 있음
- ✅ `http://localhost:5173` (슬래시 없음) → 권장
- ✅ `http://localhost:5173/*` (와일드카드) → 권장

**추천 설정:**
```
http://localhost:5173
http://localhost:5173/*
http://localhost:5174
http://localhost:5174/*
http://127.0.0.1:5173
http://127.0.0.1:5173/*
http://127.0.0.1:5174
http://127.0.0.1:5174/*
```

### 2️⃣ Client ID 재확인

네이버 클라우드 콘솔에서 Client ID를 다시 복사하여 확인:

1. 콘솔에서 Client ID 복사
2. `.env` 파일과 비교

```bash
cd Exercise/main
type .env
```

출력이 다음과 정확히 일치해야 함:
```
VITE_NAVER_CLIENT_ID=yyhdahq4gx
```

만약 다르다면 수정:
```bash
echo VITE_NAVER_CLIENT_ID=여기에_정확한_클라이언트ID > .env
```

### 3️⃣ Maps 서비스 활성화 확인

네이버 클라우드 콘솔에서:

1. **AI·NAVER API** 클릭
2. **Application 등록** 클릭
3. `inbop2` Application 선택
4. **서비스 선택** 탭에서:
   - ✅ **Maps** 체크
   - ✅ **Web Dynamic Map** 체크
5. **저장** 클릭

### 4️⃣ 개발 서버 재시작

```bash
cd Exercise/main

# 서버 종료 (Ctrl + C)
# 서버 시작
npm run dev
```

### 5️⃣ 브라우저 캐시 완전 삭제

**Chrome/Edge:**
1. F12 (개발자 도구)
2. Network 탭
3. "Disable cache" 체크
4. 우클릭 → "Empty Cache and Hard Reload"

**또는:**
- Ctrl + Shift + Delete → 캐시 삭제

### 6️⃣ 시크릿/InPrivate 모드로 테스트

- Chrome: Ctrl + Shift + N
- Edge: Ctrl + Shift + P

새 창에서 `http://localhost:5173` 접속하여 테스트

---

## 🧪 단계별 테스트

### Step 1: API 키 로드 확인

브라우저 콘솔:
```javascript
console.log(import.meta.env.VITE_NAVER_CLIENT_ID)
```

출력: `yyhdahq4gx`

### Step 2: 네이버 스크립트 로드 확인

브라우저 콘솔:
```javascript
// 스크립트 태그 확인
document.querySelector('script[src*="oapi.map.naver.com"]')
```

출력: `<script src="https://oapi.map.naver.com/openapi/v3/maps.js?ncpClientId=yyhdahq4gx&submodules=geocoder">`

### Step 3: 인증 상태 확인

브라우저 콘솔:
```javascript
console.log('naver 객체:', window.naver)
console.log('maps 객체:', window.naver?.maps)
console.log('에러:', window.naver?.maps?.error || 'No error')
```

**정상:**
```
naver 객체: Object {maps: Object, ...}
maps 객체: Object {Map: ƒ, LatLng: ƒ, ...}
에러: No error
```

**실패:**
```
naver 객체: Object {error: "AUTHENTICATION_FAILED"}
maps 객체: undefined
에러: AUTHENTICATION_FAILED
```

### Step 4: 네트워크 요청 확인

F12 → Network 탭:
1. 개발 서버 재시작
2. 페이지 새로고침
3. `oapi.map.naver.com` 검색
4. 응답 상태 확인:
   - ✅ 200 OK: 정상
   - ❌ 403 Forbidden: 인증 실패
   - ❌ 404 Not Found: Client ID 오류

---

## 🔍 일반적인 원인

### 1. Maps 서비스 미선택 (가장 흔한 원인)
- 네이버 클라우드에서 Application만 만들고
- **Maps 서비스를 선택하지 않음**

**해결:** 콘솔 → Application → 서비스 선택 → Maps → Web Dynamic Map 체크

### 2. 서비스 URL 미등록
- 서비스 URL에 `http://localhost:5173`이 없음

**해결:** Web 서비스 URL에 추가

### 3. 잘못된 Client ID
- Client ID를 잘못 복사했거나
- 다른 Application의 ID를 사용

**해결:** 콘솔에서 다시 확인 및 복사

### 4. 캐시 문제
- 이전 실패한 스크립트가 캐시됨

**해결:** 브라우저 캐시 삭제

### 5. 개발 서버 미재시작
- `.env` 파일 수정 후 서버를 재시작하지 않음

**해결:** 서버 재시작

---

## ✅ 최종 체크리스트

- [ ] 네이버 클라우드에서 **Maps** 서비스 선택됨
- [ ] **Web Dynamic Map** 체크됨
- [ ] Web 서비스 URL에 `http://localhost:5173` 등록됨
- [ ] Client ID가 `yyhdahq4gx`로 정확함
- [ ] `.env` 파일에 `VITE_NAVER_CLIENT_ID=yyhdahq4gx` 입력됨
- [ ] 개발 서버 재시작 완료
- [ ] 브라우저 캐시 삭제 완료
- [ ] `window.naver.maps`가 undefined가 아님

---

## 🆘 그래도 안 되면

### 마지막 수단: 새 Application 생성

1. 네이버 클라우드 콘솔
2. AI·NAVER API → Application 등록 → **새로 만들기**
3. Application 이름 입력
4. **Maps** 체크 → **Web Dynamic Map** 체크
5. Web 서비스 URL 입력:
   ```
   http://localhost:5173
   http://localhost:5174
   ```
6. 생성 후 새 Client ID 복사
7. `.env` 파일 업데이트
8. 서버 재시작

---

## 📞 추가 도움

위 방법으로도 해결되지 않으면 브라우저 콘솔에서:

```javascript
// 전체 디버그 정보 출력
console.log({
  clientId: import.meta.env.VITE_NAVER_CLIENT_ID,
  naver: window.naver,
  maps: window.naver?.maps,
  error: window.naver?.maps?.error || window.naver?.error,
  script: document.querySelector('script[src*="oapi.map.naver.com"]')?.src
})
```

이 출력 결과를 공유해주세요!





