# 프로젝트 사용 라이브러리 및 오픈소스 목록

## 📦 프론트엔드 (Vue.js)

### Core 프레임워크
| 라이브러리 | 버전 | 라이선스 | 용도 |
|-----------|------|---------|------|
| [Vue.js](https://vuejs.org/) | 3.5.0 | MIT | 프론트엔드 프레임워크 |
| [Vue Router](https://router.vuejs.org/) | 4.3.0 | MIT | 라우팅 관리 |
| [Pinia](https://pinia.vuejs.org/) | 2.1.7 | MIT | 상태 관리 |
| [TypeScript](https://www.typescriptlang.org/) | 5.5.4 | Apache 2.0 | 타입 안전성 |

### 빌드 도구
| 라이브러리 | 버전 | 라이선스 | 용도 |
|-----------|------|---------|------|
| [Vite](https://vitejs.dev/) | 5.4.2 | MIT | 빌드 도구 |
| [@vitejs/plugin-vue](https://github.com/vitejs/vite-plugin-vue) | 5.1.2 | MIT | Vue 플러그인 |
| [vue-tsc](https://github.com/vuejs/language-tools) | 2.0.29 | MIT | Vue TypeScript 컴파일러 |

### UI 및 스타일링
| 라이브러리 | 버전 | 라이선스 | 용도 |
|-----------|------|---------|------|
| [Tailwind CSS](https://tailwindcss.com/) | 3.4.10 | MIT | CSS 프레임워크 |
| [PostCSS](https://postcss.org/) | 8.4.47 | MIT | CSS 후처리 도구 |
| [Autoprefixer](https://github.com/postcss/autoprefixer) | 10.4.20 | MIT | CSS 벤더 프리픽스 자동 추가 |

### 차트 및 시각화
| 라이브러리 | 버전 | 라이선스 | 용도 |
|-----------|------|---------|------|
| [Chart.js](https://www.chartjs.org/) | 4.4.3 | MIT | 차트 라이브러리 |
| [vue-chartjs](https://vue-chartjs.org/) | 5.3.1 | MIT | Vue용 Chart.js 래퍼 |

### 지도
| 라이브러리 | 버전 | 라이선스 | 용도 |
|-----------|------|---------|------|
| [Leaflet](https://leafletjs.com/) | 1.9.4 | BSD-2-Clause | 지도 라이브러리 (일부 사용) |
| [@types/leaflet](https://www.npmjs.com/package/@types/leaflet) | 1.9.12 | MIT | Leaflet TypeScript 타입 정의 |
| [네이버 지도 API](https://www.ncloud.com/product/applicationService/maps) | v3 | 상용 (무료 티어) | 실시간 위치 추적 메인 지도 |

### 통신
| 라이브러리 | 버전 | 라이선스 | 용도 |
|-----------|------|---------|------|
| [Axios](https://axios-http.com/) | 1.7.2 | MIT | HTTP 클라이언트 |
| [Socket.IO Client](https://socket.io/) | 4.7.5 | MIT | 실시간 양방향 통신 |

---

## 🔧 백엔드 (Spring Boot)

### Core 프레임워크
| 라이브러리 | 버전 | 라이선스 | 용도 |
|-----------|------|---------|------|
| [Spring Boot](https://spring.io/projects/spring-boot) | 3.5.6 | Apache 2.0 | 백엔드 프레임워크 |
| [Spring Boot Starter](https://spring.io/projects/spring-boot) | 3.5.6 | Apache 2.0 | Spring Boot 기본 설정 |
| [Spring Boot Starter Web](https://spring.io/projects/spring-boot) | 3.5.6 | Apache 2.0 | RESTful API 웹 애플리케이션 |
| [Spring Boot Starter Data JPA](https://spring.io/projects/spring-data-jpa) | 3.5.6 | Apache 2.0 | JPA 데이터 액세스 |
| [Java](https://www.java.com/) | 21 | Oracle No-Fee | 프로그래밍 언어 |

### 데이터베이스
| 라이브러리 | 버전 | 라이선스 | 용도 |
|-----------|------|---------|------|
| [H2 Database](https://www.h2database.com/) | (Spring Boot 관리) | EPL 1.0, MPL 2.0 | 인메모리 데이터베이스 |

### 유틸리티
| 라이브러리 | 버전 | 라이선스 | 용도 |
|-----------|------|---------|------|
| [Lombok](https://projectlombok.org/) | (Spring Boot 관리) | MIT | Java 보일러플레이트 코드 감소 |
| [Jackson](https://github.com/FasterXML/jackson) | (Spring Boot 포함) | Apache 2.0 | JSON 직렬화/역직렬화 |

### 빌드 도구
| 라이브러리 | 버전 | 라이선스 | 용도 |
|-----------|------|---------|------|
| [Apache Maven](https://maven.apache.org/) | 3.x | Apache 2.0 | 빌드 및 의존성 관리 |

### 테스트
| 라이브러리 | 버전 | 라이선스 | 용도 |
|-----------|------|---------|------|
| [Spring Boot Starter Test](https://spring.io/projects/spring-boot) | 3.5.6 | Apache 2.0 | 테스트 프레임워크 (JUnit 5, Mockito 등 포함) |

---

## 🌐 외부 API 및 서비스

| 서비스 | 용도 | 라이선스/비용 |
|--------|------|--------------|
| [네이버 지도 API](https://www.ncloud.com/product/applicationService/maps) | 실시간 위치 추적 지도 | 무료 티어 제공 (사용량 기반 과금) |
| [네이버 Reverse Geocoding API](https://www.ncloud.com/product/applicationService/maps) | 좌표 → 주소 변환 | 무료 티어 제공 |
| [OpenStreetMap Nominatim](https://nominatim.openstreetmap.org/) | Fallback 주소 변환 | ODbL (무료) |
| [OpenStreetMap Tiles](https://www.openstreetmap.org/) | Leaflet 지도 타일 (일부 사용) | ODbL (무료) |

---

## 📊 웹 브라우저 API

| API | 용도 |
|-----|------|
| [Geolocation API](https://developer.mozilla.org/ko/docs/Web/API/Geolocation_API) | GPS 위치 추적 |
| [Web Speech API](https://developer.mozilla.org/en-US/docs/Web/API/Web_Speech_API) | 음성 인식 (설문 작성) |
| [LocalStorage API](https://developer.mozilla.org/ko/docs/Web/API/Window/localStorage) | 클라이언트 데이터 저장 |
| [Canvas API](https://developer.mozilla.org/ko/docs/Web/API/Canvas_API) | 이미지 리사이징 및 압축 |
| [FileReader API](https://developer.mozilla.org/ko/docs/Web/API/FileReader) | 파일 업로드 및 Base64 변환 |

---

## 📄 라이선스 요약

### MIT 라이선스 (가장 많이 사용)
- Vue.js, Vue Router, Pinia
- Vite, Tailwind CSS
- Axios, Socket.IO
- Chart.js, Leaflet
- Lombok

### Apache 2.0 라이선스
- Spring Boot 전체 스택
- TypeScript
- Jackson

### 기타 라이선스
- H2 Database: EPL 1.0 / MPL 2.0 (이중 라이선스)
- OpenStreetMap: ODbL (Open Database License)
- 네이버 지도 API: 상용 서비스 (무료 티어 제공)

---

## 🔍 주요 특징

### 오픈소스 라이브러리
- **프론트엔드**: 100% 오픈소스 (MIT 라이선스 중심)
- **백엔드**: 100% 오픈소스 (Apache 2.0 라이선스 중심)

### 상용 서비스
- **네이버 지도 API**: 무료 티어 제공, 사용량 초과 시 과금
  - 일일 무료 한도: API 호출 10만 건

### 보안 및 프라이버시
- 모든 위치 데이터는 인메모리 저장 (H2 Database)
- CORS 설정으로 허가된 도메인만 접근 가능
- HTTPS 사용 권장

---

## 📝 출처 및 공식 문서

- [Vue.js 공식 문서](https://vuejs.org/)
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [네이버 클라우드 플랫폼](https://www.ncloud.com/)
- [Tailwind CSS 공식 문서](https://tailwindcss.com/)
- [Chart.js 공식 문서](https://www.chartjs.org/)
- [Leaflet 공식 문서](https://leafletjs.com/)
- [Socket.IO 공식 문서](https://socket.io/)

---

**생성일**: 2025년 11월 4일  
**프로젝트**: INBOP 13조 - 건강 관리 시스템

