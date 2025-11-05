# .env 파일 설정 가이드

## ✅ 현재 상태

### 코드 확인
- ✅ `loadNaverMap.ts`는 이미 올바르게 `ncpClientId` 파라미터 사용 중
- ✅ 스크립트 URL: `https://oapi.map.naver.com/openapi/v3/maps.js?ncpClientId=${clientId}&submodules=geocoder`

---

## 📝 .env 파일 생성 방법

### 1. 파일 위치
```
Exercise/main/.env
```

### 2. 파일 내용
다음 내용을 복사해서 `.env` 파일에 붙여넣으세요:

```env
# 네이버 지도 API 클라이언트 ID (Web Dynamic Map)
# Client ID: yvhdahq4gx
VITE_NAVER_CLIENT_ID=yvhdahq4gx

# 백엔드 API 기본 URL
VITE_API_BASE=http://localhost:8081

# 기본 사용자 ID (테스트용)
VITE_USER_ID=1
```

### 3. 파일 생성 방법 (Windows)
1. `Exercise/main/` 폴더로 이동
2. 메모장 열기
3. 위 내용 붙여넣기
4. **다른 이름으로 저장** → 파일명: `.env` (앞에 점 포함!)
5. 파일 형식: **모든 파일 (*.*)**
6. 저장

### 4. 파일 생성 방법 (Mac/Linux)
```bash
cd Exercise/main
cat > .env << EOF
VITE_NAVER_CLIENT_ID=yvhdahq4gx
VITE_API_BASE=http://localhost:8081
VITE_USER_ID=1
EOF
```

---

## 🔍 확인 방법

### 1. 환경변수 확인
```bash
cd Exercise/main
grep VITE_NAVER_CLIENT_ID .env
```
**출력 예시**: `VITE_NAVER_CLIENT_ID=yvhdahq4gx`

### 2. 개발 서버 재시작
```bash
# 기존 서버 중지 (Ctrl+C)
cd Exercise/main
npm run dev
```

### 3. 브라우저 콘솔에서 확인
개발 서버 시작 후 브라우저 콘솔(F12)에 다음 입력:
```javascript
console.log('VITE_NAVER_CLIENT_ID =', import.meta.env.VITE_NAVER_CLIENT_ID);
```
**출력 예시**: `VITE_NAVER_CLIENT_ID = yvhdahq4gx`

### 4. 네트워크 탭에서 스크립트 URL 확인
1. 브라우저 F12 → Network 탭
2. 페이지 새로고침
3. `maps.js` 파일 찾기
4. URL 확인:
   ```
   https://oapi.map.naver.com/openapi/v3/maps.js?ncpClientId=yvhdahq4gx&submodules=geocoder
   ```
   ✅ `ncpClientId=yvhdahq4gx`가 보이면 성공!

---

## ⚠️ 주의사항

### 1. 다른 .env 파일 확인
다음 위치에 다른 `.env` 파일이 있는지 확인:
- `Exercise/main/.env.local` → 삭제 또는 수정
- `Exercise/main/.env.development` → 삭제 또는 수정
- `Exercise/main/.env.production` → 삭제 또는 수정
- 루트 폴더의 `.env` → 삭제 또는 수정

**이전 클라이언트 ID가 있으면 제거**:
```env
# ❌ 이전 ID (삭제)
VITE_NAVER_CLIENT_ID=9qiIZYGa6W8yzC7LxqOzNjld12ap71PgHBpWSsZ6
```

### 2. 파일 인코딩
- `.env` 파일은 **UTF-8** 인코딩으로 저장
- Windows 메모장에서 저장 시 "UTF-8" 선택

### 3. 공백 확인
```env
# ✅ 올바름
VITE_NAVER_CLIENT_ID=yvhdahq4gx

# ❌ 잘못됨 (공백 있음)
VITE_NAVER_CLIENT_ID = yvhdahq4gx
VITE_NAVER_CLIENT_ID= yvhdahq4gx
VITE_NAVER_CLIENT_ID =yvhdahq4gx
```

### 4. 따옴표 확인
```env
# ✅ 올바름 (따옴표 없음)
VITE_NAVER_CLIENT_ID=yvhdahq4gx

# ❌ 잘못됨 (따옴표 있음)
VITE_NAVER_CLIENT_ID="yvhdahq4gx"
VITE_NAVER_CLIENT_ID='yvhdahq4gx'
```

---

## 🌐 네이버 클라우드 플랫폼 설정

### 1. 서비스 URL(Referer) 등록
1. [네이버 클라우드 플랫폼 콘솔](https://console.naver.com/ncloud/application) 접속
2. **AI·NAVER API > Application > 내 애플리케이션** 선택
3. **Maps 탭** 클릭
4. **Web Dynamic Map** 섹션에서
5. **Service URL(Referer)**에 다음 **만** 등록:
   ```
   http://localhost:5173
   http://127.0.0.1:5173
   ```
   ⚠️ **주의**: `/*` 붙이지 않음! (예: `http://localhost:5173/*` ❌)

6. **저장** 또는 **변경** 버튼 클릭
7. **1~2분 대기** (설정 전파 시간)

### 2. 클라이언트 ID 확인
- **Client ID (X-NCP-APIGW-API-KEY-ID)**: `yvhdahq4gx` ✅
- **Client Secret**: 사용 안 함 (JS에서 사용하지 않음)
- **상품**: Maps → Web Dynamic Map ✅

---

## 🧪 테스트 체크리스트

### ✅ 완료 확인
- [ ] `.env` 파일 생성 완료
- [ ] `VITE_NAVER_CLIENT_ID=yvhdahq4gx` 설정 확인
- [ ] 다른 `.env` 파일에 이전 ID 없음 확인
- [ ] 개발 서버 재시작 완료
- [ ] 브라우저 콘솔에서 `import.meta.env.VITE_NAVER_CLIENT_ID` 확인
- [ ] 네트워크 탭에서 스크립트 URL에 `ncpClientId=yvhdahq4gx` 확인
- [ ] 네이버 클라우드에서 서비스 URL 등록 완료
- [ ] 브라우저 하드 리로드 (`Ctrl + F5`)
- [ ] Referer 차단 확장 프로그램 비활성화

---

## 🐛 문제 해결

### 문제: 환경변수가 읽히지 않음
**해결**:
1. `.env` 파일 위치 확인: `Exercise/main/.env`
2. 파일명 확인: `.env` (앞에 점 포함)
3. 인코딩 확인: UTF-8
4. 개발 서버 완전 재시작 (프로세스 종료 후 재시작)

### 문제: 여전히 이전 ID 사용 중
**해결**:
1. 다른 `.env` 파일 확인 및 삭제
2. 브라우저 캐시 삭제 (`Ctrl + Shift + Delete`)
3. 하드 리로드 (`Ctrl + F5`)

### 문제: 네이버 지도 인증 실패
**해결**:
1. 네이버 클라우드 콘솔에서 서비스 URL 확인
2. `/*` 없이 `http://localhost:5173`만 등록되어 있는지 확인
3. 저장 버튼 클릭 확인
4. 1~2분 대기 후 재시도

---

**작성일**: 2025년 11월 5일  
**클라이언트 ID**: `yvhdahq4gx`

