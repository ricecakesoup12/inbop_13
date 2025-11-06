# 데이터베이스 설계 문서

## 📊 데이터베이스 개요

**데이터베이스 종류**: H2 Database (파일 기반)  
**저장 위치**: `./data/exercisedb`  
**목적**: 건강 관리 시스템 - 사용자, 설문, 운동 기록 관리

---

## 🔧 데이터베이스 설정

### application.properties
```properties
spring.application.name=13group
server.port=8081

# H2 Database (파일 기반 - 데이터 유지)
spring.datasource.url=jdbc:h2:file:./data/exercisedb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# H2 Console (웹 기반 DB 관리 도구)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### H2 Console 접속 방법
1. 서버 실행 후 브라우저에서 `http://localhost:8081/h2-console` 접속
2. JDBC URL: `jdbc:h2:file:./data/exercisedb`
3. 사용자명: `sa`
4. 비밀번호: (공백)

---

## 📋 테이블 구조 (5개 테이블)

### 1. users (사용자 정보)

**테이블명**: `users`  
**설명**: 건강 관리 대상 사용자의 기본 정보 저장

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 사용자 고유 ID |
| user_code | VARCHAR(10) | UNIQUE | 사용자 로그인 코드 (4자리, 예: AB12) |
| name | VARCHAR(255) | NOT NULL | 사용자 이름 |
| gender | VARCHAR(255) | NOT NULL | 성별 (남성/여성/기타) |
| age | INTEGER | NOT NULL | 나이 |
| height | DOUBLE | NOT NULL | 키 (cm) |
| weight | DOUBLE | | 몸무게 (kg) |
| face_url | VARCHAR(100000) | | 프로필 이미지 (Base64) |
| chronic_diseases | VARCHAR(500) | | 지병 (쉼표로 구분) |
| guardian_phone | VARCHAR(20) | | 보호자 연락처 |

**인덱스**:
- PRIMARY KEY: `id`
- UNIQUE: `user_code`

**Entity 클래스**: `User.java`

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, length = 10)
    private String userCode;
    
    private String name;
    private String gender;
    private Integer age;
    private Double height;
    private Double weight;
    
    @Column(length = 100000)
    private String faceUrl;
    
    @Column(length = 500)
    private String chronicDiseases;
    
    @Column(length = 20)
    private String guardianPhone;
}
```

---

### 2. survey_requests (설문 요청)

**테이블명**: `survey_requests`  
**설명**: 보호자가 사용자에게 보낸 설문 요청 정보

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 설문 요청 ID |
| user_id | BIGINT | NOT NULL | 대상 사용자 ID |
| message | VARCHAR(255) | | 보호자 메시지 |
| status | VARCHAR(255) | NOT NULL | 상태 (PENDING/COMPLETED/DECLINED) |
| created_at | TIMESTAMP | | 요청 생성 시간 |
| completed_at | TIMESTAMP | | 완료 시간 |

**인덱스**:
- PRIMARY KEY: `id`
- INDEX: `user_id`, `status`

**Entity 클래스**: `SurveyRequest.java`

```java
@Entity
@Table(name = "survey_requests")
public class SurveyRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;
    private String message;
    
    @Column(nullable = false)
    private String status;
    
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
}
```

---

### 3. surveys (설문 결과)

**테이블명**: `surveys`  
**설명**: 사용자가 작성한 설문 결과 (6개 섹션)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 설문 ID |
| user_id | BIGINT | NOT NULL | 사용자 ID |
| request_id | BIGINT | | 설문 요청 ID (외래키) |
| survey_data | VARCHAR(10000) | | 설문 내용 (JSON 형태) |
| created_at | TIMESTAMP | | 작성 시간 |

**인덱스**:
- PRIMARY KEY: `id`
- INDEX: `user_id`, `request_id`

**설문 데이터 구조 (JSON)**:
```json
{
  "sections": [
    {
      "sectionNumber": 1,
      "sectionName": "식사 및 수분 섭취",
      "questions": [
        {"id": "1-1", "question": "오늘 몇 끼를 드셨나요?", "answer": "3"},
        {"id": "1-2", "question": "물을 충분히 마셨나요?", "answer": "예"}
      ]
    },
    // ... 섹션 2~6
  ]
}
```

**Entity 클래스**: `Survey.java`

```java
@Entity
@Table(name = "surveys")
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;
    private Long requestId;
    
    @Column(length = 10000)
    private String surveyData;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

---

### 4. weight_records (몸무게 기록)

**테이블명**: `weight_records`  
**설명**: 사용자의 몸무게 기록 (추이 분석용)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 기록 ID |
| user_id | BIGINT | NOT NULL | 사용자 ID |
| weight | DOUBLE | NOT NULL | 몸무게 (kg) |
| record_date | DATE | NOT NULL | 측정 날짜 |
| created_at | TIMESTAMP | | 기록 생성 시간 |

**인덱스**:
- PRIMARY KEY: `id`
- INDEX: `user_id`, `record_date`

**Entity 클래스**: `WeightRecord.java`

```java
@Entity
@Table(name = "weight_records")
public class WeightRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;
    private Double weight;
    private LocalDate recordDate;
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (recordDate == null) {
            recordDate = LocalDate.now();
        }
    }
}
```

---

### 5. exercise_status (운동 상태)

**테이블명**: `exercise_status`  
**설명**: 사용자의 실시간 운동 상태

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 상태 ID |
| user_id | BIGINT | UNIQUE | 사용자 ID |
| is_exercising | BOOLEAN | | 운동 중 여부 |
| started_at | TIMESTAMP | | 운동 시작 시간 |
| updated_at | TIMESTAMP | | 마지막 업데이트 시간 |

**인덱스**:
- PRIMARY KEY: `id`
- UNIQUE: `user_id`

**Entity 클래스**: `ExerciseStatus.java`

```java
@Entity
@Table(name = "exercise_status")
public class ExerciseStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private Long userId;
    
    private Boolean isExercising;
    private LocalDateTime startedAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

## 🔗 테이블 관계도 (ERD)

```
┌─────────────┐
│   users     │
│  (사용자)    │
└──────┬──────┘
       │
       │ 1:N (일대다)
       │
       ├──────────────────┬──────────────────┬──────────────────┐
       │                  │                  │                  │
       ▼                  ▼                  ▼                  ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│survey_requests│  │   surveys    │  │weight_records│  │exercise_status│
│  (설문 요청)   │  │  (설문 결과)  │  │ (몸무게 기록) │  │  (운동 상태)  │
└──────┬───────┘   └──────────────┘   └──────────────┘   └──────────────┘
       │
       │ 1:1 (일대일)
       │
       ▼
┌──────────────┐
│   surveys    │
│  (설문 결과)  │
└──────────────┘
```

**관계 설명**:
1. **users ↔ survey_requests**: 1:N (한 사용자가 여러 설문 요청을 받음)
2. **users ↔ surveys**: 1:N (한 사용자가 여러 설문을 작성)
3. **survey_requests ↔ surveys**: 1:1 (하나의 요청당 하나의 설문 결과)
4. **users ↔ weight_records**: 1:N (한 사용자가 여러 몸무게 기록)
5. **users ↔ exercise_status**: 1:1 (한 사용자당 하나의 운동 상태)

---

## 📦 Repository 인터페이스

### 1. UserRepository
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserCode(String userCode);
    boolean existsByUserCode(String userCode);
}
```

**주요 메서드**:
- `findByUserCode(String userCode)`: 사용자 코드로 검색
- `existsByUserCode(String userCode)`: 코드 중복 확인

---

### 2. SurveyRequestRepository
```java
@Repository
public interface SurveyRequestRepository extends JpaRepository<SurveyRequest, Long> {
    List<SurveyRequest> findByUserIdAndStatus(Long userId, String status);
    List<SurveyRequest> findByUserId(Long userId);
}
```

**주요 메서드**:
- `findByUserIdAndStatus(Long userId, String status)`: 특정 상태의 요청 조회
- `findByUserId(Long userId)`: 사용자의 모든 요청 조회

---

### 3. SurveyRepository
```java
@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    List<Survey> findByUserId(Long userId);
    Optional<Survey> findTopByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Survey> findByRequestId(Long requestId);
}
```

**주요 메서드**:
- `findByUserId(Long userId)`: 사용자의 모든 설문 조회
- `findTopByUserIdOrderByCreatedAtDesc(Long userId)`: 최신 설문 조회
- `findByRequestId(Long requestId)`: 요청 ID로 설문 조회

---

### 4. WeightRecordRepository
```java
@Repository
public interface WeightRecordRepository extends JpaRepository<WeightRecord, Long> {
    List<WeightRecord> findByUserIdOrderByRecordDateDesc(Long userId);
    List<WeightRecord> findByUserIdAndRecordDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
```

**주요 메서드**:
- `findByUserIdOrderByRecordDateDesc(Long userId)`: 최신순 몸무게 기록 조회
- `findByUserIdAndRecordDateBetween(...)`: 특정 기간 기록 조회

---

### 5. ExerciseStatusRepository
```java
@Repository
public interface ExerciseStatusRepository extends JpaRepository<ExerciseStatus, Long> {
    Optional<ExerciseStatus> findByUserId(Long userId);
}
```

**주요 메서드**:
- `findByUserId(Long userId)`: 사용자의 운동 상태 조회

---

## 🗄️ 메모리 저장소 (비-DB)

### LocationStore (위치 정보)
**설명**: 실시간 위치 데이터는 DB가 아닌 메모리에 저장

```java
@Component
public class LocationStore {
    private final Map<String, LocationDto> last = new ConcurrentHashMap<>();
    
    public void upsert(String userId, LocationDto dto) { 
        last.put(userId, dto); 
    }
    
    public List<LocationDto> findAll() { 
        return new ArrayList<>(last.values()); 
    }
}
```

**이유**: 
- 위치 데이터는 일시적이고 자주 변경됨
- 성능 최적화 (DB I/O 감소)
- 서버 재시작 시 초기화됨 (문제없음)

---

## 📊 주요 쿼리 패턴

### 1. 사용자 로그인
```sql
SELECT * FROM users WHERE user_code = ?
```

### 2. 대기 중인 설문 요청 조회
```sql
SELECT * FROM survey_requests 
WHERE user_id = ? AND status = 'PENDING'
ORDER BY created_at DESC
```

### 3. 최신 설문 결과 조회
```sql
SELECT * FROM surveys 
WHERE user_id = ? 
ORDER BY created_at DESC 
LIMIT 1
```

### 4. 7일간 몸무게 기록 조회
```sql
SELECT * FROM weight_records 
WHERE user_id = ? 
  AND record_date BETWEEN ? AND ?
ORDER BY record_date ASC
```

### 5. 운동 상태 조회
```sql
SELECT * FROM exercise_status 
WHERE user_id = ?
```

---

## 🔄 데이터 흐름

### 1. 사용자 등록 플로우
```
보호자 입력 → POST /users → UserRepository.save() → DB 저장
```

### 2. 설문 요청 플로우
```
보호자 요청 → POST /survey-requests → SurveyRequestRepository.save() → DB 저장
           → WebSocket 알림 → 사용자에게 실시간 알림
```

### 3. 설문 작성 플로우
```
사용자 작성 → POST /surveys → SurveyRepository.save() → DB 저장
           → SurveyRequest 상태 업데이트 (COMPLETED)
           → WeightRecord 자동 생성 (몸무게 입력 시)
```

### 4. 위치 추적 플로우
```
사용자 위치 → POST /api/locations/{userId} → LocationStore (메모리)
           → GET /api/locations → 보호자 실시간 조회
```

---

## ⚙️ 데이터베이스 설정 옵션

### ddl-auto 옵션 설명
현재 설정: `spring.jpa.hibernate.ddl-auto=update`

| 옵션 | 설명 | 사용 시기 |
|-----|------|---------|
| **update** | 테이블 구조 자동 업데이트 (데이터 유지) | 개발 환경 (현재 사용 중) |
| create | 시작 시 테이블 재생성 (기존 데이터 삭제) | 테스트 환경 |
| create-drop | 종료 시 테이블 삭제 | 임시 테스트 |
| validate | 스키마 검증만 수행 | 프로덕션 환경 |
| none | 아무 작업 하지 않음 | 프로덕션 환경 |

---

## 📈 데이터 용량 추정

### 사용자당 데이터 크기
- **users**: 약 1KB (프로필 이미지 포함 시 100KB)
- **survey_requests**: 100B × 설문 요청 수
- **surveys**: 5KB × 설문 수 (JSON 데이터)
- **weight_records**: 50B × 기록 수
- **exercise_status**: 100B (고정)

### 100명 사용자, 1년 운영 기준 추정
- users: 10MB (프로필 이미지 포함)
- survey_requests: 1MB (주 1회 × 52주)
- surveys: 260MB (주 1회 × 52주)
- weight_records: 260KB (주 1회 × 52주)
- exercise_status: 10KB

**총 예상 용량**: 약 300MB

---

## 🔒 보안 및 백업

### 데이터 백업
```bash
# H2 Database 파일 백업
cp ./data/exercisedb.mv.db ./backup/exercisedb_$(date +%Y%m%d).mv.db
```

### 데이터 복원
```bash
# 백업 파일로 복원
cp ./backup/exercisedb_20250104.mv.db ./data/exercisedb.mv.db
```

### 보안 권장사항
1. **프로덕션 환경**에서는 H2 Console 비활성화 (`spring.h2.console.enabled=false`)
2. 비밀번호 설정 추가
3. HTTPS 사용
4. 정기 백업 스케줄 설정

---

## 📝 데이터베이스 마이그레이션

### 테이블 추가 시
1. Entity 클래스 생성
2. Repository 인터페이스 생성
3. 서버 재시작 → 자동으로 테이블 생성됨 (`ddl-auto=update`)

### 컬럼 추가/수정 시
1. Entity 클래스 수정
2. 서버 재시작 → 자동으로 컬럼 추가됨
3. **주의**: 컬럼 타입 변경이나 삭제는 자동으로 처리되지 않음

---

**작성일**: 2025년 11월 5일  
**프로젝트**: INBOP 13조 - 건강 관리 시스템  
**데이터베이스**: H2 Database (파일 기반)

