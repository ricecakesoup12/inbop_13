# 운동 코칭 앱 - 전체 프로젝트 구조

## 📁 프로젝트 루트
```
C:\Users\82104\Desktop\jaetest\inbop_13\
```

---

## 🗄️ 데이터베이스 (DB)

### 위치
```
C:\Users\82104\Desktop\jaetest\inbop_13\data\
├─ exercisedb.mv.db      ← H2 데이터베이스 파일 (실제 데이터)
└─ exercisedb.trace.db   ← DB 로그 파일
```

### DB 테이블 구조
```sql
-- 1. users: 사용자 정보
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_code VARCHAR(10) UNIQUE,     -- 로그인 코드 (예: AB12)
    name VARCHAR(255),
    gender VARCHAR(255),
    age INTEGER,
    height DOUBLE,
    weight DOUBLE,
    face_url VARCHAR(100000),         -- 프로필 사진 (Base64)
    chronic_diseases VARCHAR(500)     -- 지병
);

-- 2. survey_requests: 설문 요청
CREATE TABLE survey_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,                   -- 대상 사용자
    message VARCHAR(255),             -- 보호자 요청 메시지
    status VARCHAR(50),               -- PENDING/COMPLETED/DECLINED
    created_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- 3. surveys: 설문 결과
CREATE TABLE surveys (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    request_id BIGINT,
    survey_data VARCHAR(10000),       -- JSON 형태 설문 내용
    created_at TIMESTAMP
);

-- 4. weight_records: 몸무게 기록 (그래프용)
CREATE TABLE weight_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    weight DOUBLE,
    record_date DATE,
    created_at TIMESTAMP
);

-- 5. exercise_status: 운동 상태 (실시간)
CREATE TABLE exercise_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE,
    is_exercising BOOLEAN,            -- 운동 중 여부
    started_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

---

## 🔥 백엔드 (Spring Boot)

### 위치
```
C:\Users\82104\Desktop\jaetest\inbop_13\
```

### 실행 방법
```bash
cd ~/Desktop/jaetest/inbop_13
./mvnw spring-boot:run
```

### 실행 포트
```
http://localhost:8081
```

### 백엔드 파일 구조
```
inbop_13\
├─ pom.xml                           ← Maven 설정 (의존성 관리)
├─ mvnw, mvnw.cmd                    ← Maven Wrapper (빌드 도구)
│
├─ src\main\
│  ├─ java\inbop\_group\
│  │  │
│  │  ├─ Application.java            ← Spring Boot 메인 클래스 (앱 시작점)
│  │  │
│  │  ├─ 👤 사용자 관리
│  │  ├─ User.java                   ← 사용자 Entity (DB 테이블 매핑)
│  │  ├─ UserRepository.java         ← 사용자 DB 접근 인터페이스
│  │  └─ UserController.java         ← 사용자 REST API
│  │     ├─ POST   /users                    - 사용자 등록
│  │     ├─ GET    /users                    - 사용자 목록
│  │     ├─ GET    /users/{id}               - 사용자 상세
│  │     ├─ GET    /users/code/{userCode}    - 코드로 로그인
│  │     ├─ GET    /users/code-check/{code}  - 코드 중복 확인
│  │     ├─ PUT    /users/{id}               - 사용자 수정
│  │     └─ DELETE /users/{id}               - 사용자 삭제
│  │  │
│  │  ├─ 📝 설문 관리
│  │  ├─ SurveyRequest.java          ← 설문 요청 Entity
│  │  ├─ SurveyRequestRepository.java← 설문 요청 DB
│  │  ├─ SurveyRequestController.java← 설문 요청 REST API
│  │  │  ├─ POST /survey-requests                      - 설문 요청 보내기
│  │  │  ├─ GET  /survey-requests/user/{id}/pending    - 대기 중인 설문
│  │  │  ├─ GET  /survey-requests/user/{id}/all-results- 모든 설문 결과
│  │  │  ├─ POST /survey-requests/{id}/submit          - 설문 제출
│  │  │  ├─ GET  /survey-requests/user/{id}/latest-result - 최신 결과
│  │  │  └─ GET  /survey-requests/result/{id}          - 특정 설문 조회
│  │  │
│  │  ├─ Survey.java                 ← 설문 결과 Entity
│  │  ├─ SurveyRepository.java       ← 설문 결과 DB
│  │  ├─ SurveyController.java       ← 기존 설문 API
│  │  ├─ SurveyService.java          ← 설문 비즈니스 로직
│  │  └─ SurveyCreateRequestDto.java ← 설문 데이터 전송 객체
│  │  │
│  │  ├─ ⚖️ 몸무게 관리
│  │  ├─ WeightRecord.java           ← 몸무게 기록 Entity
│  │  ├─ WeightRecordRepository.java ← 몸무게 DB
│  │  └─ WeightRecordController.java ← 몸무게 REST API
│  │     ├─ GET /weight-records/user/{id}        - 몸무게 기록 조회
│  │     └─ GET /weight-records/user/{id}/range  - 기간별 조회
│  │  │
│  │  └─ 🏃 운동 상태 관리
│  │     ├─ ExerciseStatus.java      ← 운동 상태 Entity
│  │     ├─ ExerciseStatusRepository.java ← 운동 상태 DB
│  │     └─ ExerciseStatusController.java ← 운동 상태 REST API
│  │        ├─ POST /exercise-status/user/{id}  - 운동 상태 업데이트
│  │        └─ GET  /exercise-status/user/{id}  - 운동 상태 조회
│  │
│  └─ resources\
│     └─ application.properties      ← Spring Boot 설정 (포트, DB 연결)
│
└─ target\                           ← 빌드 결과물 (자동 생성)
   └─ classes\                       ← 컴파일된 .class 파일
```

---

## 🎨 프론트엔드 (Vue3)

### 위치
```
C:\Users\82104\Desktop\jaetest\inbop_13\Exercise\main\
```

### 실행 방법
```bash
cd ~/Desktop/jaetest/inbop_13/Exercise/main
npm run dev
```

### 실행 포트
```
http://localhost:5174
```

### 프론트엔드 파일 구조
```
Exercise\main\
├─ package.json                      ← npm 의존성 관리
├─ vite.config.ts                    ← Vite 빌드 설정
├─ tsconfig.json                     ← TypeScript 설정
├─ tailwind.config.cjs               ← Tailwind CSS 설정
├─ postcss.config.cjs                ← PostCSS 설정
├─ index.html                        ← HTML 진입점
│
└─ src\
   ├─ main.ts                        ← Vue 앱 초기화 (진입점)
   ├─ App.vue                        ← 루트 컴포넌트
   │
   ├─ 📂 pages\                      ← 페이지 컴포넌트
   │  ├─ Landing.vue                 ← 메인 화면 (보호자/사용자 선택)
   │  │
   │  ├─ 📂 guardian\                ← 보호자 페이지
   │  │  ├─ GuardianHome.vue         ← 사용자 리스트 + "사용자 추가" 버튼
   │  │  ├─ UserDetail.vue           ← 사용자 상세 정보 + 그래프 + 운동 상태
   │  │  ├─ SurveySend.vue           ← 설문 요청 보내기
   │  │  └─ SurveyResult.vue         ← 설문 결과 보기 (날짜 선택 + 비교)
   │  │
   │  └─ 📂 user\                    ← 사용자 페이지
   │     ├─ UserRegister.vue         ← 코드 로그인 페이지
   │     ├─ UserDashboard.vue        ← 사용자 대시보드 (3분할 레이아웃)
   │     ├─ UserSurvey.vue           ← 설문 작성 페이지 (6개 섹션)
   │     └─ UserSurveyResult.vue     ← 설문 결과 보기 (날짜 선택 + 비교)
   │
   ├─ 📂 components\                 ← 재사용 컴포넌트
   │  ├─ 📂 common\                  ← 공통 UI 컴포넌트
   │  │  ├─ AppButton.vue            ← 버튼 컴포넌트 (solid/outline/ghost)
   │  │  ├─ AppCard.vue              ← 카드 컴포넌트 (footer 슬롯 지원)
   │  │  ├─ AppModal.vue             ← 모달 다이얼로그
   │  │  └─ ConfirmDialog.vue        ← 확인 다이얼로그 (삭제 확인 등)
   │  │
   │  ├─ 📂 charts\                  ← Chart.js 그래프 컴포넌트
   │  │  ├─ _LineChartBase.ts        ← Chart.js 공통 설정
   │  │  ├─ WeightTrendChart.vue     ← 몸무게 그래프
   │  │  ├─ HeartRateTrendChart.vue  ← 심박수 그래프
   │  │  ├─ SpO2TrendChart.vue       ← 산소포화도 그래프
   │  │  └─ ActivityTrendChart.vue   ← 운동량 그래프
   │  │
   │  ├─ 📂 map\
   │  │  └─ UserLocationMap.vue      ← Leaflet 지도 컴포넌트 (위치 표시)
   │  │
   │  └─ 📂 user\
   │     ├─ UserListItem.vue         ← 사용자 목록 아이템 (코드 표시)
   │     └─ UserVitalsNow.vue        ← 실시간 바이탈 위젯 (HR/SpO2)
   │
   ├─ 📂 layouts\                    ← 레이아웃 컴포넌트
   │  ├─ DefaultLayout.vue           ← 보호자 레이아웃 (헤더 + 메인)
   │  └─ AppShell.vue                ← 사용자 레이아웃 (헤더 + 메인)
   │
   ├─ 📂 router\                     ← Vue Router 라우팅
   │  └─ index.ts                    ← 라우트 정의 (URL → 페이지 매핑)
   │
   ├─ 📂 stores\                     ← Pinia 상태 관리
   │  ├─ users.store.ts              ← 사용자 상태 (목록, 상세, CRUD)
   │  ├─ surveys.store.ts            ← 설문 상태
   │  ├─ metrics.store.ts            ← 메트릭 상태 (바이탈, 그래프 데이터)
   │  ├─ auth.store.ts               ← 인증 상태
   │  └─ ui.store.ts                 ← UI 상태 (모달, 로딩 등)
   │
   ├─ 📂 services\                   ← API 통신 레이어
   │  └─ 📂 api\
   │     ├─ http.ts                  ← Axios 인스턴스 (baseURL, 인터셉터)
   │     ├─ users.ts                 ← 사용자 API 함수
   │     ├─ surveyRequests.ts        ← 설문 요청/결과 API 함수
   │     ├─ weightRecords.ts         ← 몸무게 기록 API 함수
   │     ├─ exerciseStatus.ts        ← 운동 상태 API 함수
   │     ├─ surveys.ts               ← 기존 설문 API
   │     └─ metrics.ts               ← 메트릭 API
   │
   ├─ 📂 composables\                ← 재사용 로직 (Composition API)
   │  ├─ useGeo.ts                   ← 위치 정보 가져오기
   │  ├─ useSpeech.ts                ← 음성 인식 (Web Speech API)
   │  ├─ useRealtime.ts              ← WebSocket 실시간 통신
   │  └─ useConfirm.ts               ← 확인 다이얼로그 헬퍼
   │
   ├─ 📂 types\                      ← TypeScript 타입 정의
   │  ├─ index.d.ts                  ← 전역 타입
   │  ├─ user.ts                     ← User, Gender 타입
   │  ├─ survey.ts                   ← Survey 관련 타입
   │  └─ metrics.ts                  ← VitalNow, DailyMetrics 타입
   │
   └─ 📂 assets\                     ← 정적 파일
      ├─ 📂 images\
      │  └─ default-face.png         ← 기본 프로필 이미지
      └─ 📂 styles\
         ├─ tailwind.css             ← Tailwind CSS import
         └─ base.css                 ← 기본 스타일 (폰트, 색상)
```

---

## 📋 주요 파일 역할 설명

### 🔥 백엔드 (Java Spring Boot)

#### 📌 Entity (DB 테이블 매핑)
| 파일 | 역할 |
|------|------|
| `User.java` | 사용자 정보 (이름, 코드, 나이, 키, 몸무게 등) |
| `SurveyRequest.java` | 설문 요청 (보호자 → 사용자) |
| `Survey.java` | 설문 결과 (JSON 형태로 저장) |
| `WeightRecord.java` | 몸무게 기록 (날짜별 그래프용) |
| `ExerciseStatus.java` | 운동 상태 (실시간 운동 중 여부) |

#### 📌 Repository (DB 접근)
| 파일 | 역할 |
|------|------|
| `UserRepository.java` | 사용자 CRUD, 코드로 찾기 |
| `SurveyRequestRepository.java` | 설문 요청 조회 (대기/완료) |
| `SurveyRepository.java` | 설문 결과 조회 (최신/전체) |
| `WeightRecordRepository.java` | 몸무게 기록 조회 (날짜별) |
| `ExerciseStatusRepository.java` | 운동 상태 조회 |

#### 📌 Controller (REST API)
| 파일 | 역할 |
|------|------|
| `UserController.java` | 사용자 CRUD API |
| `SurveyRequestController.java` | 설문 요청/제출/조회 API |
| `WeightRecordController.java` | 몸무게 조회 API |
| `ExerciseStatusController.java` | 운동 상태 업데이트/조회 API |
| `SurveyController.java` | 기존 설문 API |
| `SurveyService.java` | 설문 비즈니스 로직 |

---

### 🎨 프론트엔드 (Vue3 TypeScript)

#### 📌 페이지 (Pages)
| 파일 | 역할 |
|------|------|
| **공통** |
| `Landing.vue` | 메인 화면 (보호자/사용자 선택) |
| **보호자** |
| `GuardianHome.vue` | 사용자 리스트 + "사용자 추가" 모달 |
| `UserDetail.vue` | 사용자 상세 (정보 카드 + 그래프 + 지도 + 실시간 바이탈 + 운동 상태) |
| `SurveySend.vue` | 설문 요청 보내기 (메시지 입력) |
| `SurveyResult.vue` | 설문 결과 보기 (날짜 선택 + 이전 설문 비교) |
| **사용자** |
| `UserRegister.vue` | 코드 로그인 (4자리 코드 입력) |
| `UserDashboard.vue` | 사용자 대시보드 (3분할: 바이탈/운동/챗봇 + 설문 알림) |
| `UserSurvey.vue` | 설문 작성 (6개 섹션, 보호자 메시지 표시) |
| `UserSurveyResult.vue` | 설문 결과 보기 (날짜 선택 + 이전 설문 비교) |

#### 📌 컴포넌트 (Components)
| 파일 | 역할 |
|------|------|
| **공통 UI** |
| `AppButton.vue` | 재사용 버튼 (solid/outline/ghost 스타일) |
| `AppCard.vue` | 카드 컨테이너 (header/footer 슬롯) |
| `AppModal.vue` | 모달 다이얼로그 |
| `ConfirmDialog.vue` | 확인 다이얼로그 (삭제 등) |
| **차트** |
| `WeightTrendChart.vue` | 몸무게 추이 그래프 |
| `HeartRateTrendChart.vue` | 심박수 추이 그래프 |
| `SpO2TrendChart.vue` | 산소포화도 추이 그래프 |
| `ActivityTrendChart.vue` | 운동량 추이 그래프 |
| **지도** |
| `UserLocationMap.vue` | Leaflet 지도 (실시간 위치 표시) |
| **사용자** |
| `UserListItem.vue` | 사용자 목록 아이템 (프로필 + 정보 + 코드 + 삭제 버튼) |
| `UserVitalsNow.vue` | 실시간 바이탈 표시 (HR/SpO2/운동 여부) |

#### 📌 서비스 (Services) - API 통신
| 파일 | 역할 |
|------|------|
| `http.ts` | Axios 인스턴스 (baseURL, 인터셉터, 에러 처리) |
| `users.ts` | 사용자 API (등록, 조회, 삭제, 코드 로그인) |
| `surveyRequests.ts` | 설문 요청/제출/조회 API |
| `weightRecords.ts` | 몸무게 기록 조회 API |
| `exerciseStatus.ts` | 운동 상태 업데이트/조회 API |
| `surveys.ts` | 기존 설문 API |
| `metrics.ts` | 메트릭 API (바이탈, 그래프 데이터) |

#### 📌 스토어 (Stores) - 상태 관리
| 파일 | 역할 |
|------|------|
| `users.store.ts` | 사용자 상태 (list, detail, createUser, deleteUser) |
| `surveys.store.ts` | 설문 상태 |
| `metrics.store.ts` | 메트릭 상태 (바이탈, 그래프 데이터) |
| `auth.store.ts` | 인증 상태 |
| `ui.store.ts` | UI 상태 (모달, 로딩 스피너) |

#### 📌 Composables - 재사용 로직
| 파일 | 역할 |
|------|------|
| `useGeo.ts` | 위치 정보 가져오기 (Geolocation API) |
| `useSpeech.ts` | 음성 인식 (Web Speech API) |
| `useRealtime.ts` | WebSocket 실시간 통신 |
| `useConfirm.ts` | 확인 다이얼로그 헬퍼 |

#### 📌 타입 (Types)
| 파일 | 역할 |
|------|------|
| `user.ts` | User, Gender 타입 정의 |
| `survey.ts` | Survey 관련 타입 |
| `metrics.ts` | VitalNow, DailyMetrics 타입 |

#### 📌 레이아웃 (Layouts)
| 파일 | 역할 |
|------|------|
| `DefaultLayout.vue` | 보호자 레이아웃 (헤더 + 메인) |
| `AppShell.vue` | 사용자 레이아웃 (헤더 + 메인) |

---

## 🔗 데이터 흐름

### 사용자 등록 흐름
```
보호자: GuardianHome.vue
  ↓ "사용자 추가" 버튼
  ↓ 코드(AB12) + 정보 입력
  ↓ usersStore.createUser()
  ↓ services/api/users.ts → POST /users
  ↓ UserController.java
  ↓ UserRepository.java
  ↓ DB: users 테이블 저장 ✅
```

### 설문 요청 흐름
```
보호자: UserDetail.vue → "설문 보내기"
  ↓ SurveySend.vue (메시지 입력)
  ↓ createSurveyRequest()
  ↓ POST /survey-requests
  ↓ SurveyRequestController.java
  ↓ DB: survey_requests 테이블 저장 (status: PENDING) ✅
  ↓
사용자: UserDashboard.vue
  ↓ 30초마다 getPendingSurveyRequests() 호출
  ↓ "새로운 설문 요청" 버튼 표시 🔴
  ↓ 클릭 → UserSurvey.vue
  ↓ 설문 작성 후 제출
  ↓ submitSurvey()
  ↓ POST /survey-requests/{id}/submit
  ↓ DB: surveys 테이블 저장 (JSON) ✅
  ↓ DB: weight_records 테이블 저장 (몸무게) ✅
  ↓ DB: survey_requests 상태 업데이트 (COMPLETED) ✅
```

### 운동 상태 흐름
```
사용자: UserDashboard.vue → "운동 시작" 버튼
  ↓ toggleWorkout()
  ↓ updateExerciseStatus(userId, true)
  ↓ POST /exercise-status/user/{id}
  ↓ ExerciseStatusController.java
  ↓ DB: exercise_status 테이블 저장 ✅
  ↓
보호자: UserDetail.vue
  ↓ 10초마다 getExerciseStatus() 호출
  ↓ 운동 상태: 🏃 운동 중 (초록색) 표시 ✅
```

---

## 🎨 디자인 시스템

### 색상
- **Primary**: `#8BC34A` (연두색)
- **Beige**: `#F5F5DC` (베이지)
- **Text Main**: `#333333` (진한 회색)
- **Text Sub**: `#666666` (회색)
- **Accent**: `#E0E0E0` (연한 회색)

### 폰트
- **Gowun Dodum** (고운돋움)

### 컴포넌트 스타일
- **Card**: `rounded-2xl shadow-soft border`
- **Button**: `rounded-xl px-4 py-3`
- **Input**: `rounded-xl border-2 focus:border-primary`

---


