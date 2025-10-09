# 운동 코칭 앱

Vue3 + TypeScript + Tailwind CSS 기반의 운동 코칭 앱입니다.

## 🚀 시작하기

### 1. 패키지 설치

```bash
npm install
```

### 2. 환경 변수 설정

`.env` 파일을 생성하고 다음 내용을 추가하세요:

```env
VITE_API_BASE=http://localhost:8080
VITE_WS_BASE=http://localhost:8080
```

### 3. 개발 서버 실행

```bash
npm run dev
```

개발 서버가 `http://localhost:5173`에서 실행됩니다.

### 4. 프로덕션 빌드

```bash
npm run build
```

## 📁 프로젝트 구조

```
main/
├── src/
│   ├── assets/           # 이미지, 스타일
│   ├── components/       # 재사용 가능한 컴포넌트
│   │   ├── common/      # 공통 컴포넌트
│   │   ├── charts/      # 차트 컴포넌트
│   │   ├── map/         # 지도 컴포넌트
│   │   └── user/        # 사용자 컴포넌트
│   ├── composables/     # Vue Composition 함수
│   ├── layouts/         # 레이아웃 컴포넌트
│   ├── pages/           # 페이지 컴포넌트
│   │   ├── guardian/   # 보호자 페이지
│   │   └── user/       # 사용자 페이지
│   ├── router/          # Vue Router 설정
│   ├── services/        # API 및 실시간 서비스
│   ├── stores/          # Pinia 스토어
│   ├── types/           # TypeScript 타입 정의
│   ├── App.vue
│   └── main.ts
├── index.html
├── package.json
├── tsconfig.json
├── tailwind.config.cjs
└── vite.config.ts
```

## 🎨 주요 기능

### 보호자 기능
- 사용자 리스트 관리 (추가/삭제)
- 사용자 상세 정보 및 실시간 모니터링
- 건강 트렌드 차트 (몸무게, 심박수, 산소포화도, 운동량)
- 실시간 위치 확인
- 설문 요청 및 결과 확인

### 사용자 기능
- 실시간 바이탈 모니터링 (심박수, 산소포화도)
- 운동 시작/중지 및 칼로리 추적
- 코칭 챗봇 (음성 인식 지원)
- 실시간 위치 표시
- 설문 응답

## 🛠 기술 스택

- **프레임워크**: Vue 3 (Composition API)
- **언어**: TypeScript
- **상태 관리**: Pinia
- **라우팅**: Vue Router
- **스타일링**: Tailwind CSS
- **차트**: Chart.js + vue-chartjs
- **지도**: Leaflet
- **실시간 통신**: Socket.IO
- **HTTP 클라이언트**: Axios
- **빌드 도구**: Vite

## 📋 라우팅 구조

```
/ (Landing)
├── /guardian (보호자)
│   ├── /guardian/users/:id (사용자 상세)
│   ├── /guardian/users/:id/survey/send (설문 보내기)
│   └── /guardian/users/:id/survey/result (설문 결과)
└── /user (사용자)
```

## 🎨 디자인 시스템

- **폰트**: Gowun Dodum (구운돌담)
- **주요 색상**:
  - Primary: #8BC34A (연두색)
  - Secondary: #9E9E9E (회색)
  - Accent: #D7CCC8 (베이지)
  - Background: #FFFFFF, #F7F5F3 (베이지)
  - Text: #424242, #757575 (회색)

## 📝 개발 가이드

### 컴포넌트 작성

모든 컴포넌트는 `<script setup>` 구문과 TypeScript를 사용합니다:

```vue
<template>
  <!-- 템플릿 -->
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 컴포넌트 로직
</script>
```

### API 연동

API 호출은 `src/services/api/` 디렉토리의 서비스 함수를 통해 수행합니다:

```typescript
import * as usersApi from '@/services/api/users'

const users = await usersApi.getUsers()
```

### 상태 관리

Pinia 스토어를 사용하여 전역 상태를 관리합니다:

```typescript
import { useUsersStore } from '@/stores/users.store'

const usersStore = useUsersStore()
await usersStore.fetchList()
```

## 🔧 백엔드 연동

백엔드 API 엔드포인트는 `.env` 파일에서 설정합니다:

- REST API: `VITE_API_BASE`
- WebSocket: `VITE_WS_BASE`

백엔드가 준비되지 않은 경우에도 프론트엔드 개발을 진행할 수 있도록 모든 API 호출에 에러 핸들링이 포함되어 있습니다.

## 📱 모바일 지원

- 반응형 디자인으로 모바일 기기를 지원합니다
- Tailwind CSS의 `md:` prefix를 사용하여 태블릿/데스크톱에서 다른 레이아웃을 제공합니다
- 터치 최소 크기 (44px)를 고려한 버튼 디자인

## 🤝 기여하기

1. 브랜치 생성 (`git checkout -b feature/AmazingFeature`)
2. 변경사항 커밋 (`git commit -m 'Add some AmazingFeature'`)
3. 브랜치에 푸시 (`git push origin feature/AmazingFeature`)
4. Pull Request 생성

## 📄 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.

