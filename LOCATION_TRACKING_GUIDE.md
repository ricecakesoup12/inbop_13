# 실시간 위치 추적 시스템 구축 가이드

이 프로젝트에 네이버 지도 기반 실시간 위치 추적 기능이 추가되었습니다.

## 📁 프로젝트 구조

### 백엔드 (Java Spring Boot)
```
src/main/java/inbop/_group/
├── LocationDto.java          # 위치 데이터 DTO
├── LocationStore.java        # 메모리 기반 위치 저장소
├── LocationController.java   # 위치 API 컨트롤러
└── WebConfig.java           # CORS 설정
```

### 프론트엔드 (Vue 3 + TypeScript)
```
Exercise/main/src/
├── utils/
│   └── loadNaverMap.ts      # 네이버 지도 스크립트 로더
├── pages/user/
│   └── LiveMap.vue          # 실시간 위치 추적 페이지
└── router/index.ts          # 라우터 설정 (추가됨)
```

## 🚀 시작하기

### 1단계: 환경 변수 설정

`Exercise/main/` 폴더에 `.env` 파일을 생성하고 다음 내용을 추가하세요:

```env
# 네이버 지도 API 클라이언트 ID
VITE_NAVER_CLIENT_ID=여기에_네이버_클라이언트ID_입력

# 백엔드 API 기본 URL
VITE_API_BASE=http://localhost:8081

# 기본 사용자 ID (테스트용)
VITE_USER_ID=여기에_사용자ID_입력
```

### 2단계: 네이버 지도 API 발급

1. [네이버 클라우드 플랫폼](https://www.ncloud.com/) 접속
2. 로그인 후 콘솔로 이동
3. **Services > AI·NAVER API > Application 등록**
4. **Maps > Web Dynamic Map** 선택
5. 서비스 URL에 `http://localhost:5173` 추가
6. 발급받은 **Client ID**를 `.env` 파일의 `VITE_NAVER_CLIENT_ID`에 입력

### 3단계: 백엔드 서버 실행

프로젝트 루트에서:

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Mac/Linux
./mvnw spring-boot:run
```

서버가 `http://localhost:8081`에서 실행됩니다.

### 4단계: 프론트엔드 개발 서버 실행

```bash
cd Exercise/main
npm install
npm run dev
```

개발 서버가 `http://localhost:5173`에서 실행됩니다.

## 📡 API 엔드포인트

### 위치 저장
```http
POST /api/locations/{userId}
Content-Type: application/json

{
  "latitude": 37.5665,
  "longitude": 126.9780,
  "timestamp": 0
}
```

### 전체 위치 조회
```http
GET /api/locations
```

응답:
```json
[
  {
    "userId": "jiyoon",
    "latitude": 37.5665,
    "longitude": 126.9780,
    "timestamp": 1699999999999
  }
]
```

## 🧪 테스트 방법

### 백엔드 API 테스트

```bash
# 위치 저장
curl -X POST http://localhost:8081/api/locations/jiyoon \
  -H "Content-Type: application/json" \
  -d '{"latitude":37.5665,"longitude":126.9780,"timestamp":0}'

# 전체 조회
curl http://localhost:8081/api/locations
```

### 프론트엔드 테스트

1. 브라우저에서 `http://localhost:5173` 접속
2. 사용자 등록 후 로그인
3. 대시보드에서 **"실시간 위치 보기"** 버튼 클릭
4. 위치 권한 허용
5. 지도에 내 위치가 빨간 마커로 표시됨

## 🗺️ 페이지 구성

### 사용자 대시보드 (`/user`)
- 운동 섹션에 **"실시간 위치 보기"** 버튼 추가
- 클릭 시 LiveMap 페이지로 이동

### 실시간 위치 페이지 (`/user/location`)
- 네이버 지도 표시
- 내 위치를 빨간 마커로 실시간 표시
- 위치 추적 상태 표시 (추적 중/대기 중)
- 현재 좌표 (위도/경도) 표시

## 🔧 주요 기능

### 현재 구현된 기능 (1~3단계)
✅ Java 백엔드 API (위치 저장/조회)
✅ CORS 설정
✅ 네이버 지도 스크립트 로더
✅ 실시간 위치 추적 (Geolocation API)
✅ 지도에 내 위치 마커 표시
✅ 자동 지도 중심 이동

### 다음 단계 구현 예정 (4~6단계)
⏳ 내 좌표를 백엔드에 주기적으로 전송 (POST)
⏳ 백엔드에서 전체 좌표 가져오기 (GET)
⏳ 다른 사용자 마커도 지도에 표시
⏳ 주소 표시 (Reverse Geocoding)

## 📱 모바일 테스트

PC에서는 Wi-Fi 기반 위치로 정확도가 낮을 수 있습니다.
더 정확한 테스트를 위해:

1. **ngrok** 또는 **localtunnel**로 로컬 서버를 HTTPS로 터널링
2. 스마트폰에서 HTTPS URL 접속
3. GPS 기반 정확한 위치 확인

```bash
# ngrok 사용 예시
ngrok http 5173
```

## 🛠️ 트러블슈팅

### 지도가 로드되지 않을 때
- `.env` 파일의 `VITE_NAVER_CLIENT_ID` 확인
- 네이버 클라우드 콘솔에서 서비스 URL 등록 확인
- 개발 서버 재시작 (`npm run dev`)

### 위치 권한이 거부될 때
- 브라우저 설정 > 개인정보 보호 > 위치 서비스 확인
- HTTPS가 아닌 경우 일부 브라우저에서 제한될 수 있음

### CORS 에러가 발생할 때
- 백엔드 서버가 8081 포트에서 실행 중인지 확인
- `WebConfig.java`의 allowedOrigins 확인

## 📚 참고 자료

- [네이버 지도 API 문서](https://navermaps.github.io/maps.js.ncp/)
- [Geolocation API MDN](https://developer.mozilla.org/ko/docs/Web/API/Geolocation_API)
- [Vue Router 공식 문서](https://router.vuejs.org/)

## 🎯 다음 할 일

1. **4단계**: 내 좌표를 백엔드에 주기적으로 전송
   - `setInterval`로 10초마다 POST 요청
   - `Exercise/main/src/services/api/locations.ts` 생성

2. **5단계**: 다른 사용자 위치도 지도에 표시
   - 백엔드에서 전체 위치 GET
   - 각 사용자별 마커 생성 및 업데이트

3. **6단계**: 주소 표시 (Reverse Geocoding)
   - 네이버 지도 geocoder 사용
   - 좌표를 주소로 변환하여 표시

---

**작성일**: 2025-11-02
**버전**: 1.0.0 (1~3단계 완료)





