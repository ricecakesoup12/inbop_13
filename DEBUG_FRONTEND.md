# 프론트엔드 디버깅 가이드

## 1. 브라우저 개발자 도구로 API 응답 확인

### F12를 눌러 개발자 도구 열기

### Network 탭 확인
1. Network 탭 클릭
2. "AI 운동 추천" 버튼 클릭
3. `stretch` 또는 `user-code` 요청 찾기
4. 클릭해서 Response 탭 확인

### Console 탭에서 데이터 확인
Console 탭에 다음 명령어 입력:
```javascript
// 현재 추천 데이터 확인
console.log(window.$vm?.$data)
```

## 2. API 직접 테스트

브라우저 주소창에 입력 (userCode를 실제 값으로 변경):
```
http://localhost:8081/api/stretch/user-code/USER001
```

### 정상 응답 예시:
```json
{
  "사용자코드": "USER001",
  "통증부위": "허리",
  "스트레칭영상": [
    {
      "제목": "5분이면 끝! 허리 통증 풀어주는 허리 스트레칭",
      "영상주소": "https://www.youtube.com/watch?v=CBC025SQ1iQ"
    },
    {
      "제목": "전문가가 알려주는 간단하지만 최고 효과 허리 스트레칭",
      "영상주소": "https://www.youtube.com/watch?v=8RUW6K3YYuA"
    }
  ],
  "인터벌운동": [...],
  "주의사항": [...]
}
```

### 문제 응답 예시:
```json
{
  "사용자코드": null,
  "통증부위": null,
  "스트레칭영상": [],
  "실패이유": "AI 응답 없음",
  "출처": "fallback"
}
```

## 3. 문제 해결

### 스트레칭영상이 비어있는 경우 (`[]`)

**원인:** AI가 통증 부위를 파악하지 못함

**해결방법:**
1. H2 콘솔 접속: http://localhost:8081/h2-console
2. 사용자 데이터 확인:
```sql
SELECT * FROM USERS WHERE user_code = 'USER001';
```

3. 채팅 메시지 확인:
```sql
SELECT * FROM CHAT_MESSAGES WHERE user_id = 'USER001';
```

4. 채팅 메시지가 없다면 추가:
```sql
INSERT INTO CHAT_MESSAGES (user_id, sender, message, timestamp) 
VALUES ('USER001', 'user', '허리가 너무 아파요', CURRENT_TIMESTAMP);

INSERT INTO CHAT_MESSAGES (user_id, sender, message, timestamp) 
VALUES ('USER001', 'user', '앉아있으면 허리 통증이 심해집니다', CURRENT_TIMESTAMP);
```

### OpenAI API 에러

백엔드 콘솔에서 에러 확인:
- `OpenAI API key not found` → application.properties의 openai.api.key 확인
- `401 Unauthorized` → API 키가 만료되었거나 잘못됨
- `429 Too Many Requests` → API 할당량 초과

### 프론트엔드 에러

Console 탭에서 에러 확인:
- `Cannot read property '제목' of undefined` → 데이터 구조 문제
- `Network Error` → 백엔드 서버가 실행 중인지 확인

## 4. 백엔드 로그 확인

백엔드 콘솔에서 다음 로그 찾기:
```
Hibernate: SELECT ... FROM USERS WHERE user_code = ?
Hibernate: SELECT ... FROM CHAT_MESSAGES WHERE user_id = ?
```

만약 쿼리가 실행되지 않으면 데이터베이스 연결 문제입니다.

## 5. 빠른 테스트

PowerShell에서:
```powershell
Invoke-WebRequest -Uri "http://localhost:8081/api/stretch/user-code/USER001" | Select-Object -Expand Content
```

또는:
```powershell
curl http://localhost:8081/api/stretch/user-code/USER001
```



