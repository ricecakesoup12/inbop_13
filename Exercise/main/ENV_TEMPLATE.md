# 환경변수 설정 가이드

프로젝트 루트(`Exercise/main/`)에 `.env` 파일을 생성하고 아래 내용을 복사하여 붙여넣으세요.

```env
# 네이버 지도 API 클라이언트 ID
# 네이버 클라우드 플랫폼(https://www.ncloud.com/)에서 발급받으세요
# AI·NAVER API > Application 등록 > Maps > Web Dynamic Map 선택
VITE_NAVER_CLIENT_ID=여기에_네이버_클라이언트ID_입력

# 백엔드 API 기본 URL
VITE_API_BASE=http://localhost:8081

# 기본 사용자 ID (테스트용)
VITE_USER_ID=여기에_사용자ID_입력
```

## 설정 방법

1. `Exercise/main/` 폴더에 `.env` 파일 생성
2. 위 내용을 복사하여 붙여넣기
3. `VITE_NAVER_CLIENT_ID`에 네이버 클라우드에서 발급받은 클라이언트 ID 입력
4. `VITE_USER_ID`에 원하는 사용자 ID 입력 (예: jiyoon)
5. 개발 서버 재시작

## 네이버 지도 API 발급 방법

1. [네이버 클라우드 플랫폼](https://www.ncloud.com/) 접속
2. 로그인 후 콘솔로 이동
3. Services > AI·NAVER API > Application 등록
4. Maps > Web Dynamic Map 선택
5. 서비스 URL에 `http://localhost:5173` 추가
6. 발급받은 Client ID를 `.env` 파일에 입력







