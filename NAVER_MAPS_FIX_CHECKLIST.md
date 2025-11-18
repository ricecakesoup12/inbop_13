# 네이버 지도 인증 실패 해결 체크리스트

## 🔴 현재 상태
```
window.naver: {maps: null}
Client ID: yvhdahq4gx (올바름)
```

**문제:** Client ID는 맞지만 `maps`가 `null` → **Maps 서비스 미활성화**

---

## ✅ 해결 방법 (순서대로 진행)

### 1️⃣ 네이버 클라우드 콘솔 - Maps 서비스 활성화 ⭐⭐⭐

**가장 중요한 단계입니다!**

1. https://console.ncloud.com/ncloud/application 접속

2. **AI·NAVER API** 메뉴 클릭

3. **Application 등록** 클릭

4. `inbop2` Application 찾아서 **클릭** (수정 모드로 진입)

5. **중요: "서비스 선택" 탭 클릭**

6. 다음 항목들을 **체크**:
   ```
   ☑ Maps
     ☑ Web Dynamic Map
   ```

7. **저장** 또는 **수정** 버튼 클릭

8. 확인: 체크가 되어 있고 저장되었는지 다시 확인

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

### 3️⃣ 개발 서버 완전 재시작

**.env 파일이 변경되었으므로 반드시 재시작해야 합니다!**

#### Windows 명령 프롬프트:
```cmd
cd C:\Users\82104\Desktop\jaetest\inbop_13\Exercise\main
# 현재 실행 중인 서버에서 Ctrl + C
npm run dev
```

#### PowerShell:
```powershell
cd C:\Users\82104\Desktop\jaetest\inbop_13\Exercise\main
# 현재 실행 중인 서버에서 Ctrl + C
npm run dev
```

---

### 4️⃣ 브라우저 완전 초기화

#### 방법 1: 시크릿/InPrivate 모드 (권장)
- Chrome: `Ctrl + Shift + N`
- Edge: `Ctrl + Shift + P`
- 새 창에서 http://localhost:5173 접속

#### 방법 2: 캐시 삭제
1. `Ctrl + Shift + Delete` 누르기
2. "캐시된 이미지 및 파일" 체크
3. "데이터 삭제" 클릭
4. 브라우저 닫았다가 다시 열기

#### 방법 3: 강력 새로고침
- `Ctrl + Shift + R` (Windows)
- `Cmd + Shift + R` (Mac)

---

### 5️⃣ .env 파일 최종 확인

```cmd
cd C:\Users\82104\Desktop\jaetest\inbop_13\Exercise\main
type .env
```

**출력되어야 하는 내용:**
```
VITE_NAVER_CLIENT_ID=yvhdahq4gx
```

만약 다르다면:
```cmd
echo VITE_NAVER_CLIENT_ID=yvhdahq4gx > .env
```

---

### 6️⃣ 브라우저 콘솔 확인

F12를 누르고 Console 탭:

```javascript
// 1. Client ID 확인
console.log(import.meta.env.VITE_NAVER_CLIENT_ID)
// 출력: yvhdahq4gx

// 2. 네이버 객체 확인
console.log(window.naver)
// 정상: {maps: {Map: ƒ, LatLng: ƒ, ...}}
// 실패: {maps: null}

// 3. 전체 디버그 정보
console.log({
  clientId: import.meta.env.VITE_NAVER_CLIENT_ID,
  naver: window.naver,
  maps: window.naver?.maps,
  script: document.querySelector('script[src*="oapi.map.naver.com"]')?.src
})
```

---

## 🎯 가장 흔한 실수

### ❌ 실수 1: Maps 서비스를 체크하지 않음
- Application은 만들었지만
- **"서비스 선택" 탭에서 Maps를 체크하지 않음**

**해결:** 콘솔 → Application → 서비스 선택 → Maps 체크 → 저장

### ❌ 실수 2: 개발 서버를 재시작하지 않음
- .env 파일 수정 후
- **서버를 재시작하지 않으면 변경사항이 적용되지 않음**

**해결:** Ctrl + C 후 npm run dev

### ❌ 실수 3: 브라우저 캐시
- 이전 실패한 스크립트가 캐시됨

**해결:** 시크릿 모드로 테스트 또는 캐시 삭제

---

## 🔍 네이버 클라우드 콘솔 상세 가이드

### Application 수정 화면 진입 방법

1. https://console.ncloud.com 로그인

2. 상단 메뉴: **Services** → **AI·NAVER API**

3. 왼쪽 메뉴: **Application 등록**

4. Application 목록에서 `inbop2` **클릭**

5. 탭 메뉴 확인:
   - **기본 정보**: Application 이름, Client ID 표시
   - **서비스 선택**: ⭐ **여기서 Maps 체크!**
   - **서비스 환경 설정**: Web 서비스 URL 등록

### 서비스 선택 탭 확인

```
☑ Maps
  ☑ Web Dynamic Map
  ☐ Static Map
  ☐ Directions
  ☐ Geocoding
```

**중요:** 
- `Maps` 체크박스 선택
- `Web Dynamic Map` 하위 체크박스 선택
- 저장 버튼 클릭

---

## 🧪 테스트 방법

### Step 1: 개발 서버 재시작 확인
```
VITE v5.x.x ready in xxx ms
➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
```

### Step 2: 시크릿 모드로 테스트
- Ctrl + Shift + N (Chrome)
- http://localhost:5173 접속
- F12 → Console 확인

### Step 3: 정상 동작 확인
```javascript
window.naver.maps
// 출력: {Map: ƒ, LatLng: ƒ, Point: ƒ, Size: ƒ, ...}
```

### Step 4: 지도 표시 확인
- 지도가 화면에 표시됨
- 마커(위치 표시)가 나타남
- Console에 에러 없음

---

## 📞 여전히 안 되면

### 새 Application 생성 (최후의 수단)

1. 네이버 클라우드 콘솔
2. AI·NAVER API → Application 등록
3. **새로 만들기** 버튼
4. Application 이름 입력: `inbop3`
5. **서비스 선택**에서:
   - ☑ Maps
   - ☑ Web Dynamic Map
6. **서비스 환경 설정**:
   ```
   http://localhost:5173
   http://localhost:5174
   ```
7. 생성 완료 → 새 Client ID 복사
8. `.env` 파일 업데이트:
   ```
   VITE_NAVER_CLIENT_ID=새로받은클라이언트ID
   ```
9. 서버 재시작

---

## ✅ 최종 체크리스트

순서대로 확인하세요:

- [ ] 네이버 클라우드 콘솔 로그인
- [ ] Application `inbop2` 클릭
- [ ] **"서비스 선택" 탭**에서 **Maps** 체크됨
- [ ] **"서비스 선택" 탭**에서 **Web Dynamic Map** 체크됨
- [ ] 저장 버튼 클릭함
- [ ] Web 서비스 URL에 `http://localhost:5173` 등록됨
- [ ] `.env` 파일에 `VITE_NAVER_CLIENT_ID=yvhdahq4gx` 입력됨
- [ ] 개발 서버 재시작 (Ctrl+C 후 npm run dev)
- [ ] 브라우저 캐시 삭제 또는 시크릿 모드 사용
- [ ] `window.naver.maps`가 null이 아님
- [ ] 지도가 화면에 표시됨

---

**모든 단계를 순서대로 진행한 후에도 안 되면 새 Application을 만드세요!**
