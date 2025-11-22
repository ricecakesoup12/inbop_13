# AI 운동 추천 테스트 방법

## 1. H2 데이터베이스 확인
1. 브라우저에서 접속: http://localhost:8081/h2-console
2. 로그인 정보:
   - JDBC URL: `jdbc:h2:file:./data/exercisedb`
   - User Name: `sa`
   - Password: (비어있음)

## 2. 필요한 데이터 확인

### 사용자 데이터 확인
```sql
SELECT * FROM USERS;
```

### 채팅 메시지 확인
```sql
SELECT * FROM CHAT_MESSAGES;
```

## 3. 테스트 데이터가 없다면 추가

### 테스트 사용자 추가
```sql
INSERT INTO USERS (id, user_code, name, gender, age, height, weight, chronic_diseases, guardian_phone, created_at, updated_at) 
VALUES (1, 'USER001', '김철수', 'male', 70, 170, 70, '고혈압,당뇨', '010-1234-5678', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
```

### 테스트 채팅 메시지 추가 (통증 관련)
```sql
INSERT INTO CHAT_MESSAGES (user_id, sender, message, timestamp) 
VALUES ('USER001', 'user', '허리가 아파요', CURRENT_TIMESTAMP);

INSERT INTO CHAT_MESSAGES (user_id, sender, message, timestamp) 
VALUES ('USER001', 'user', '앉아있으면 허리 통증이 심합니다', CURRENT_TIMESTAMP);

INSERT INTO CHAT_MESSAGES (user_id, sender, message, timestamp) 
VALUES ('USER001', 'user', '무릎도 가끔 아픕니다', CURRENT_TIMESTAMP);
```

## 4. API 테스트

PowerShell에서 테스트:
```powershell
Invoke-WebRequest -Uri "http://localhost:8081/api/stretch/user-code/USER001" -Method GET | Select-Object -Expand Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

또는 브라우저에서:
```
http://localhost:8081/api/stretch/user-code/USER001
```

## 5. 문제 해결

### OpenAI API 키 오류
백엔드 콘솔에서 "OpenAI API key not found" 에러가 나면:
- application.properties의 `openai.api.key` 확인
- 또는 환경 변수 `OPENAI_API_KEY` 설정

### 사용자 없음 오류
- H2 콘솔에서 사용자 데이터 확인
- 프론트엔드에서 사용하는 userCode가 데이터베이스에 있는지 확인

### AI 응답 없음
- OpenAI API 키가 유효한지 확인
- 인터넷 연결 확인
- OpenAI API 할당량 확인

## 6. 프론트엔드에서 테스트

1. 사용자 상세 페이지로 이동
2. "AI 운동 추천" 버튼 클릭
3. 브라우저 개발자 도구(F12) → Network 탭에서 API 호출 확인
4. Console 탭에서 에러 메시지 확인





