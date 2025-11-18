# 네이버 지도 API 설정 가이드

## 현재 상황
- **Client ID**: `yvhdahq4gx`
- **프론트엔드 URL**: `http://localhost:5174`
- **에러**: 401 Unauthorized / 500 Internal Server Error

## 해결 방법

### 1. 네이버 클라우드 플랫폼 콘솔 설정

1. **https://console.naver.com/ncloud/application** 접속
2. 애플리케이션 선택 (Client ID: `yvhdahq4gx`)
3. **Application 정보 확인**:
   - Application 이름: `inbop2` (또는 사용자 지정 이름)
   - Client ID: `yvhdahq4gx`
   - Client Secret: (표시된 값 확인)

### 2. 서비스 환경 설정

**서비스 > Web Dynamic Map** 선택 후:

#### ✅ 반드시 추가해야 할 URL (각각 개별 등록):

```
http://localhost:5174
http://localhost:5174/*
http://127.0.0.1:5174
http://127.0.0.1:5174/*
```

#### ⚠️ 주의사항:
- **와일드카드 (`/*`)** 사용 필수
- `/user`, `/guardian` 같은 특정 경로는 등록하지 않아도 됨
- 각 URL을 **개별적으로 추가**해야 함
- 추가 후 반드시 **저장** 버튼 클릭

### 3. API 사용량 확인

**Application > 사용량 통계**에서:
- 일일 호출 제한: 보통 **10,000 ~ 100,000** 건
- 현재 사용량 확인
- 500 에러는 종종 **사용량 초과**를 의미

### 4. 문제 해결 체크리스트

- [ ] 서비스 URL에 `http://localhost:5174/*` 추가했는가?
- [ ] 저장 버튼을 눌렀는가?
- [ ] 브라우저 캐시를 삭제했는가?
- [ ] 시크릿 모드로 테스트했는가?
- [ ] Client ID가 `yvhdahq4gx`인가?
- [ ] API 사용량을 초과하지 않았는가?

### 5. 여전히 안 된다면

#### 옵션 A: API 키 재발급
1. 네이버 클라우드 콘솔에서
2. Application 삭제 후 재생성
3. 새 Client ID로 `.env` 파일 업데이트

#### 옵션 B: 무료 플랜 확인
1. "Web Dynamic Map" 서비스가 **활성화**되어 있는지 확인
2. 무료 플랜이 만료되지 않았는지 확인

#### 옵션 C: 네이버 고객센터 문의
- 500 Internal Server Error는 서버측 문제일 수 있음
- 네이버 클라우드 고객센터에 문의

## 정상 작동 시 콘솔 로그

```
네이버 지도 스크립트 로드 시작: ...
네이버 지도 스크립트 로드됨. 초기화 대기 중...
네이버 지도 API 초기화 완료!
✅ 네이버 지도 생성 완료
```

## 추가 참고

- 네이버 지도 API 문서: https://navermaps.github.io/maps.js.ncp/docs/
- 네이버 클라우드 지원: https://www.ncloud.com/support/question



