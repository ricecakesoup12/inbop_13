# 심박수 경고 시스템 백엔드 API 명세서

## 개요
사용자의 심박수가 130bpm을 초과할 때 경고를 기록하고, 보호자가 모니터링할 수 있는 시스템임

## 주요 기능
1. 심박수 130bpm 초과 시 경고 기록 저장
2. 사용자별 일주일 경고 횟수 통계 조회
3. 보호자용 활성 경고 사용자 목록 조회

## 인증/권한
- 모든 API는 인증 필요 (현재 프로젝트의 인증 방식 따름)
- `/api/guardian/heart-rate-alerts/active`는 보호자 권한 필요

## 1. 심박수 경고 기록 생성 API

### 엔드포인트
```
POST /api/heart-rate-alerts
```

### Request Body
```json
{
  "userId": 1,
  "heartRate": 135,
  "alertType": "warning"
}
```

### Request DTO 명세
- `userId` (Long, 필수): 사용자 ID
- `heartRate` (Integer, 필수): 심박수
- `alertType` (String, 필수): 경고 타입, "warning" 값만 허용

### Response
```json
{
  "id": 1,
  "userId": 1,
  "heartRate": 135,
  "alertType": "warning",
  "alertedAt": "2024-01-15T14:30:00"
}
```

### Response DTO 명세
```java
public class HeartRateAlertResponse {
    private Long id;
    private Long userId;
    private Integer heartRate;
    private String alertType;    // "warning"
    private LocalDateTime alertedAt;
}
```

### Request DTO 명세
```java
public class CreateHeartRateAlertRequest {
    @NotNull
    private Long userId;
    
    @NotNull
    @Min(0)
    private Integer heartRate;
    
    @NotBlank
    private String alertType;    // "warning"
}
```

### 비즈니스 로직
1. 같은 사용자의 마지막 경고로부터 1분 이내에 발생한 경고는 중복으로 간주하여 저장하지 않음
2. 경고 기록 저장 후 사용자와 보호자에게 실시간 알림 전송 (선택사항)

### 에러 처리
- 400 Bad Request: userId, heartRate, alertType 중 하나라도 누락된 경우
- 404 Not Found: userId에 해당하는 사용자를 찾을 수 없는 경우

---

## 2. 사용자별 일주일 경고 횟수 조회 API

### 엔드포인트
```
GET /api/users/{userId}/heart-rate-alerts/weekly
```

### Path Parameter
- `userId` (Long): 사용자 ID

### Query Parameter
없음 (기본적으로 최근 7일 조회)

### Response
```json
[
  {
    "date": "2024-01-15",
    "count": 3
  },
  {
    "date": "2024-01-16",
    "count": 5
  },
  {
    "date": "2024-01-17",
    "count": 0
  },
  {
    "date": "2024-01-18",
    "count": 2
  },
  {
    "date": "2024-01-19",
    "count": 0
  },
  {
    "date": "2024-01-20",
    "count": 1
  },
  {
    "date": "2024-01-21",
    "count": 0
  }
]
```

### Response DTO 명세
```java
public class WeeklyAlertCountResponse {
    private String date;    // 날짜 (YYYY-MM-DD 형식, 예: "2024-01-15")
    private Long count;     // 해당 날짜의 경고 횟수
}
```

### 비즈니스 로직
1. 오늘부터 과거 7일간의 데이터 조회 (오늘 포함)
2. 경고가 없는 날도 count: 0으로 포함하여 반환
3. 날짜순 정렬 (과거 → 최근)

### 에러 처리
- 404 Not Found: userId에 해당하는 사용자를 찾을 수 없는 경우

---

## 3. 모든 사용자의 현재 경고 상태 조회 API (보호자용)

### 엔드포인트
```
GET /api/guardian/heart-rate-alerts/active
```

### Query Parameter
없음

### Response
```json
[
  {
    "userId": 1,
    "userName": "홍길동",
    "heartRate": 135,
    "alertedAt": "2024-01-15T14:30:00"
  },
  {
    "userId": 2,
    "userName": "김철수",
    "heartRate": 142,
    "alertedAt": "2024-01-15T14:25:00"
  }
]
```

### Response DTO 명세
```java
public class ActiveAlertResponse {
    private Long userId;
    private String userName;     // 사용자 이름
    private Integer heartRate;   // 현재 심박수
    private LocalDateTime alertedAt;  // 마지막 경고 시간
}
```

### 비즈니스 로직
1. 마지막 경고가 5분 이내인 사용자만 반환 (현재 활성 경고)
2. 심박수 내림차순 정렬 (높은 순서대로)
3. 경고가 없는 경우 빈 배열 반환

### 에러 처리
없음 (빈 배열 반환)

---

## 데이터베이스 스키마

### 테이블: heart_rate_alerts

```sql
CREATE TABLE heart_rate_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    heart_rate INT NOT NULL,
    alert_type VARCHAR(20) NOT NULL,
    alerted_at DATETIME NOT NULL,
    INDEX idx_user_alerted (user_id, alerted_at),
    INDEX idx_alerted_at (alerted_at)
);
```

### Entity 클래스 예시

```java
@Entity
@Table(name = "heart_rate_alerts")
@Getter
@Setter
public class HeartRateAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Integer heartRate;
    
    @Column(nullable = false, length = 20)
    private String alertType;  // "warning"
    
    @Column(nullable = false)
    private LocalDateTime alertedAt;
    
    @PrePersist
    protected void onCreate() {
        if (alertedAt == null) {
            alertedAt = LocalDateTime.now();
        }
    }
}
```

---

## Repository 명세

```java
@Repository
public interface HeartRateAlertRepository extends JpaRepository<HeartRateAlert, Long> {
    
    // 사용자의 일주일 경고 횟수 조회 (날짜별)
    @Query("SELECT DATE(a.alertedAt) as date, COUNT(a) as count " +
           "FROM HeartRateAlert a " +
           "WHERE a.userId = :userId " +
           "AND a.alertedAt >= :startDate " +
           "GROUP BY DATE(a.alertedAt) " +
           "ORDER BY date")
    List<AlertCountByDate> countAlertsByDate(
        @Param("userId") Long userId, 
        @Param("startDate") LocalDate startDate
    );
    
    // 사용자의 최근 경고 조회
    HeartRateAlert findFirstByUserIdOrderByAlertedAtDesc(Long userId);
    
    // 활성 경고가 있는 사용자 조회 (최근 5분 이내)
    @Query("SELECT DISTINCT a.userId FROM HeartRateAlert a " +
           "WHERE a.alertedAt >= :since " +
           "GROUP BY a.userId " +
           "HAVING MAX(a.alertedAt) >= :since")
    List<Long> findUsersWithRecentAlerts(@Param("since") LocalDateTime since);
    
    // 특정 사용자의 활성 경고 조회
    @Query("SELECT a FROM HeartRateAlert a " +
           "WHERE a.userId = :userId " +
           "AND a.alertedAt >= :since " +
           "ORDER BY a.alertedAt DESC")
    List<HeartRateAlert> findActiveAlertsByUser(
        @Param("userId") Long userId,
        @Param("since") LocalDateTime since
    );
}

// 인터페이스 프로젝션
public interface AlertCountByDate {
    LocalDate getDate();
    Long getCount();
}
```

---

## 비즈니스 로직 상세

### 1. 중복 경고 방지 로직
같은 사용자가 1분 이내에 여러 번 경고가 발생해도 한 번만 저장함

```java
public HeartRateAlert createAlert(Long userId, Integer heartRate) {
    // 마지막 경고 확인
    HeartRateAlert lastAlert = alertRepository
        .findFirstByUserIdOrderByAlertedAtDesc(userId);
    
    if (lastAlert != null) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        if (lastAlert.getAlertedAt().isAfter(oneMinuteAgo)) {
            // 1분 이내 중복 경고는 저장하지 않음
            return lastAlert;
        }
    }
    
    // 새로운 경고 저장
    HeartRateAlert alert = new HeartRateAlert();
    alert.setUserId(userId);
    alert.setHeartRate(heartRate);
    alert.setAlertType("warning");
    alert.setAlertedAt(LocalDateTime.now());
    
    return alertRepository.save(alert);
}
```

### 2. 일주일 경고 횟수 조회 로직
오늘 포함 7일간의 경고 횟수를 날짜별로 조회 (경고 없는 날도 0으로 포함)

```java
public List<WeeklyAlertCountResponse> getWeeklyCounts(Long userId) {
    LocalDate today = LocalDate.now();
    LocalDate weekAgo = today.minusDays(6);  // 오늘 포함 7일
    
    // 실제 데이터 조회
    List<AlertCountByDate> counts = alertRepository
        .countAlertsByDate(userId, weekAgo.atStartOfDay());
    
    // 모든 날짜 채우기 (경고가 없는 날도 0으로)
    Map<LocalDate, Long> countMap = counts.stream()
        .collect(Collectors.toMap(
            AlertCountByDate::getDate,
            AlertCountByDate::getCount
        ));
    
    List<WeeklyAlertCountResponse> result = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
        LocalDate date = weekAgo.plusDays(i);
        Long count = countMap.getOrDefault(date, 0L);
        result.add(new WeeklyAlertCountResponse(
            date.toString(),
            count
        ));
    }
    
    return result;
}
```

### 3. 활성 경고 조회 로직
최근 5분 이내 경고가 있는 사용자 목록을 심박수 내림차순으로 반환

```java
public List<ActiveAlertResponse> getActiveAlerts() {
    LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
    
    // 최근 5분 이내 경고가 있는 사용자 ID 목록
    List<Long> userIds = alertRepository.findUsersWithRecentAlerts(fiveMinutesAgo);
    
    // 각 사용자의 최신 경고 조회 + 사용자 정보 조인
    return userIds.stream()
        .map(userId -> {
            HeartRateAlert latestAlert = alertRepository
                .findFirstByUserIdOrderByAlertedAtDesc(userId);
            User user = userRepository.findById(userId).orElse(null);
            
            return new ActiveAlertResponse(
                userId,
                user != null ? user.getName() : "알 수 없음",
                latestAlert.getHeartRate(),
                latestAlert.getAlertedAt()
            );
        })
        .sorted((a, b) -> b.getHeartRate().compareTo(a.getHeartRate()))  // 심박수 내림차순
        .collect(Collectors.toList());
}
```

---

## 에러 처리

### 응답 코드
- 200 OK: 성공
- 400 Bad Request: 잘못된 요청 (userId, heartRate 누락 등)
- 404 Not Found: 사용자를 찾을 수 없음
- 500 Internal Server Error: 서버 오류

### 에러 응답 형식
```json
{
  "error": "Bad Request",
  "message": "userId는 필수입니다.",
  "timestamp": "2024-01-15T14:30:00"
}
```

---

## 프론트엔드에서 사용할 API 호출 코드 예시

백엔드 개발자가 참고할 프론트엔드 코드임

```typescript
// Exercise/main/src/services/api/heartRateAlerts.ts

export interface HeartRateAlert {
  id?: number
  userId: number
  heartRate: number
  alertType: 'warning'
  alertedAt?: string
}

export interface WeeklyAlertCount {
  date: string  // "YYYY-MM-DD"
  count: number
}

export interface ActiveAlert {
  userId: number
  userName: string
  heartRate: number
  alertedAt: string
}

// API 1: 심박수 경고 기록 생성
export async function createHeartRateAlert(
  userId: number, 
  heartRate: number
): Promise<HeartRateAlert> {
  const { data } = await http.post('/api/heart-rate-alerts', {
    userId,
    heartRate,
    alertType: 'warning'
  })
  return data
}

// API 2: 사용자별 일주일 경고 횟수 조회
export async function getWeeklyAlertCounts(
  userId: string
): Promise<WeeklyAlertCount[]> {
  const { data } = await http.get(`/api/users/${userId}/heart-rate-alerts/weekly`)
  return data
}

// API 3: 모든 사용자의 현재 경고 상태 조회 (보호자용)
export async function getActiveAlerts(): Promise<ActiveAlert[]> {
  const { data } = await http.get('/api/guardian/heart-rate-alerts/active')
  return data
}
```

---

## 체크리스트 (백엔드 개발자 확인용)

### 구현 필수 사항
- [ ] HeartRateAlert Entity 생성
- [ ] HeartRateAlertRepository 생성
- [ ] POST /api/heart-rate-alerts API 구현
- [ ] GET /api/users/{userId}/heart-rate-alerts/weekly API 구현
- [ ] GET /api/guardian/heart-rate-alerts/active API 구현
- [ ] 중복 경고 방지 로직 (1분 이내 중복 제거)
- [ ] 일주일 경고 횟수 조회 시 모든 날짜 포함 (경고 없는 날도 0)
- [ ] 활성 경고 조회 시 심박수 내림차순 정렬
- [ ] 활성 경고는 최근 5분 이내만 조회

---

## 컨트롤러 클래스 구조 예시

```java
@RestController
@RequestMapping("/api")
public class HeartRateAlertController {
    
    @Autowired
    private HeartRateAlertService alertService;
    
    @PostMapping("/heart-rate-alerts")
    public ResponseEntity<HeartRateAlertResponse> createAlert(
        @RequestBody CreateHeartRateAlertRequest request
    ) {
        // 구현
    }
    
    @GetMapping("/users/{userId}/heart-rate-alerts/weekly")
    public ResponseEntity<List<WeeklyAlertCountResponse>> getWeeklyCounts(
        @PathVariable Long userId
    ) {
        // 구현
    }
    
    @GetMapping("/guardian/heart-rate-alerts/active")
    public ResponseEntity<List<ActiveAlertResponse>> getActiveAlerts() {
        // 구현
    }
}
```

## 서비스 클래스 구조 예시

```java
@Service
public class HeartRateAlertService {
    
    @Autowired
    private HeartRateAlertRepository alertRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public HeartRateAlert createAlert(Long userId, Integer heartRate) {
        // 중복 방지 로직 포함
    }
    
    public List<WeeklyAlertCountResponse> getWeeklyCounts(Long userId) {
        // 일주일 경고 횟수 조회
    }
    
    public List<ActiveAlertResponse> getActiveAlerts() {
        // 활성 경고 조회
    }
}
```

## 추가 고려사항 (선택사항)

### 실시간 알림 (Socket.IO)
경고 발생 시 보호자 대시보드에 실시간 알림 전송
현재 Socket.IO 연결 구조 확인 필요
프론트엔드에서 이미 사용 중이라면 동일한 방식으로 알림 전송

### 성능 최적화
활성 경고 조회 시 인덱스 활용 (idx_user_alerted, idx_alerted_at)
대량 사용자 환경을 위한 페이징 고려 (현재는 전체 조회)

## 주의사항
1. 날짜 형식: `date` 필드는 반드시 "YYYY-MM-DD" 형식의 String으로 반환 (예: "2024-01-15")
2. 시간대: `alertedAt`은 서버 시간대 기준으로 저장
3. 중복 방지: 같은 사용자의 1분 이내 중복 경고는 반드시 저장하지 않음
4. 일주일 경고 횟수: 경고가 없는 날도 반드시 count: 0으로 포함하여 7개의 항목 모두 반환

