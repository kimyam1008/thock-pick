# 기술 스택 (Tech Stack)

## 📚 전체 기술 스택 개요

```
┌─────────────────────────────────────────────────────────┐
│                    Backend Framework                     │
│                   Spring Boot 3.5.9                      │
│                       (Java 17)                          │
└─────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│  Persistence │   │    Search    │   │    Cache     │
│              │   │              │   │              │
│  Spring JPA  │   │Elasticsearch │   │    Redis     │
│  MySQL/      │   │              │   │              │
│  PostgreSQL  │   │              │   │              │
└──────────────┘   └──────────────┘   └──────────────┘
```

## 🛠️ 핵심 기술 스택

### 1. Backend Framework: Spring Boot 3.5.9

#### 선택 이유
- **생산성**: 자동 설정(Auto Configuration)으로 빠른 개발 가능
- **생태계**: Spring Data JPA, Spring Cache 등 다양한 모듈 통합 용이
- **안정성**: 엔터프라이즈급 서비스에 검증된 프레임워크
- **커뮤니티**: 방대한 레퍼런스와 활발한 커뮤니티

#### 주요 모듈
```groovy
dependencies {
    // Spring Boot Web - REST API 구현
    implementation 'org.springframework.boot:spring-boot-starter-web'

    // Spring Boot Data JPA - ORM
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    // Spring Boot DevTools - 개발 편의성
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
}
```

#### 활용 기능
- **REST API**: @RestController, @RequestMapping
- **의존성 주입**: @Autowired, @Service, @Repository
- **AOP**: 로깅, 트랜잭션 관리
- **스케줄링**: @Scheduled (Google Sheets 동기화)
- **검증**: @Valid, Custom Validator

---

### 2. ORM & Database: Spring Data JPA + RDBMS

#### Spring Data JPA
- **선택 이유**:
  - 반복적인 CRUD 코드 자동 생성
  - 타입 안전한 쿼리 (Query Methods)
  - N+1 문제 해결 용이 (Fetch Join, EntityGraph)
  - 트랜잭션 관리 자동화

- **활용 기능**:
  ```java
  // Repository 인터페이스 예시
  public interface SwitchRepository extends JpaRepository<Switch, Long> {
      List<Switch> findByType(SwitchType type);
      List<Switch> findByNameContaining(String keyword);

      @Query("SELECT s FROM Switch s JOIN FETCH s.videos")
      List<Switch> findAllWithVideos();
  }
  ```

#### RDBMS: MySQL / PostgreSQL
- **선택 기준**:
  - **개발 환경**: H2 Database (인메모리, 빠른 테스트)
  - **운영 환경**: MySQL or PostgreSQL

- **MySQL 선택 시**:
  - 간단한 설정과 운영
  - 넓은 호스팅 지원
  - JSON 컬럼 타입 지원

- **PostgreSQL 선택 시**:
  - 더 강력한 JSONB 지원
  - 복잡한 쿼리 최적화
  - 풀텍스트 검색 내장 (Elasticsearch 보완)

#### 현재 의존성
```groovy
dependencies {
    // JPA
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    // H2 (개발용)
    runtimeOnly 'com.h2database:h2'

    // MariaDB/MySQL (운영용)
    runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'
}
```

---

### 3. Search Engine: Elasticsearch

#### 선택 이유
- **전문 검색**: RDB보다 훨씬 빠른 텍스트 검색
- **다국어 지원**: 한글 형태소 분석기 (nori) 지원
- **확장성**: 샤딩을 통한 수평 확장
- **집계 기능**: 통계, 인기 검색어 분석

#### 활용 계획
```java
// 검색 예시
- 키워드 검색: "체리 리니어" → Cherry MX Red, Gateron Red 등
- 필터 검색: 타입=리니어 AND 무게=45~50g
- 자동완성: "gat" → "Gateron", "Gateron Yellow" 등
```

#### 인덱싱 전략
```json
{
  "switch_index": {
    "mappings": {
      "properties": {
        "name": { "type": "text", "analyzer": "nori" },
        "type": { "type": "keyword" },
        "weight": { "type": "integer" },
        "manufacturer": { "type": "keyword" },
        "price": { "type": "integer" }
      }
    }
  }
}
```

#### 추가 의존성 (예정)
```groovy
dependencies {
    // Spring Data Elasticsearch
    implementation 'org.springframework.boot:spring-boot-starter-data-elasticsearch'
}
```

---

### 4. Cache: Redis

#### 선택 이유
- **성능**: 메모리 기반 초고속 읽기/쓰기
- **TTL 지원**: 자동 만료로 캐시 관리 용이
- **자료구조**: String, List, Set, Hash 등 다양한 자료구조
- **Pub/Sub**: 실시간 알림 기능 확장 가능

#### 캐싱 전략
1. **Cache-Aside Pattern**
   ```
   1. 캐시 조회
   2. 캐시 미스 → DB 조회 → 캐시 저장
   3. 캐시 히트 → 즉시 반환
   ```

2. **캐싱 대상 및 TTL**
   | 데이터 타입 | TTL | 갱신 시점 |
   |------------|-----|----------|
   | 인기 스위치 Top 10 | 6시간 | Google Sheets 동기화 시 |
   | 스위치 상세 정보 | 1시간 | 요청 시 or 동기화 시 |
   | 검색 결과 | 30분 | 동적 갱신 |
   | 추천 매핑 | 12시간 | 동기화 시 |

#### 활용 예시
```java
@Cacheable(value = "switches", key = "#id")
public Switch getSwitchById(Long id) {
    return switchRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Switch not found"));
}

@CacheEvict(value = "switches", allEntries = true)
public void syncDataFromGoogleSheets() {
    // Google Sheets 동기화 후 캐시 전체 삭제
}
```

#### 추가 의존성 (예정)
```groovy
dependencies {
    // Redis
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'

    // Lettuce (Redis 클라이언트, Spring Boot 기본)
    // 또는 Jedis
}
```

---

### 5. External API: Google Sheets API

#### 선택 이유
- **접근성**: 커뮤니티가 쉽게 데이터 입력 가능
- **버전 관리**: 변경 이력 자동 추적
- **무료**: API 할당량 내 무료 사용 가능
- **협업**: 여러 사용자가 동시 편집 가능

#### 활용 계획
```java
// Sheets API 호출 예시
Sheets service = new Sheets.Builder(...)
    .setApplicationName("ThockPick")
    .build();

ValueRange response = service.spreadsheets().values()
    .get(SPREADSHEET_ID, RANGE)
    .execute();

List<List<Object>> values = response.getValues();
```

#### 동기화 전략
- **스케줄링**: @Scheduled(cron = "0 0 * * * *") // 1시간마다
- **증분 동기화**: 변경된 행만 업데이트 (timestamp 비교)
- **에러 핸들링**:
  - API 할당량 초과 시 대기
  - 네트워크 오류 시 재시도 (Exponential Backoff)
  - 동기화 실패 시 알림

#### 추가 의존성 (예정)
```groovy
dependencies {
    // Google Sheets API
    implementation 'com.google.apis:google-api-services-sheets:v4-rev20220927-2.0.0'
    implementation 'com.google.auth:google-auth-library-oauth2-http:1.19.0'
}
```

---

### 6. 유틸리티 및 도구

#### Lombok
- **목적**: Boilerplate 코드 감소
- **사용**: @Getter, @Setter, @Builder, @NoArgsConstructor 등

```groovy
dependencies {
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

#### Validation
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-validation'
}
```
- **활용**: @NotNull, @Size, @Pattern 등으로 입력 검증

#### Testing
```groovy
dependencies {
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```
- JUnit 5, Mockito, AssertJ 포함

---

## 🔧 개발 환경

### Build Tool: Gradle 8.14.3
- **선택 이유**:
  - Maven보다 빠른 빌드 속도
  - Kotlin DSL 지원
  - 의존성 캐싱

### Java Version: 17 (LTS)
- **선택 이유**:
  - Spring Boot 3.x 최소 요구사항
  - 장기 지원 버전 (2029년까지)
  - Record, Sealed Class 등 최신 문법 지원

---

## 📊 아키텍처별 기술 매핑

| 레이어 | 기술 스택 | 역할 |
|-------|---------|------|
| Data Entry | Google Sheets | 데이터 수집 |
| Sync Layer | Spring Scheduler + Google Sheets API | 동기화 |
| Persistence | Spring Data JPA + MySQL/PostgreSQL | 영구 저장 |
| Search | Elasticsearch | 전문 검색 |
| Cache | Redis | 성능 향상 |
| Service | Spring Service | 비즈니스 로직 |
| API | Spring Web MVC | REST API |

---

## 🚀 향후 추가 예정 기술

### Phase 2
- **Spring Security**: 인증/인가 (JWT)
- **Spring Cloud Config**: 설정 중앙 관리

### Phase 3
- **Kafka**: 이벤트 스트리밍 (실시간 알림)
- **Docker**: 컨테이너화
- **Kubernetes**: 오케스트레이션

### Phase 4
- **Prometheus + Grafana**: 모니터링
- **ELK Stack**: 로그 수집 및 분석

---

## 💡 기술 선택 시 고려사항

### 학습 곡선
- Spring Boot: 중간 (자바 개발자에게 친숙)
- JPA: 높음 (N+1, 지연로딩 등 주의사항 많음)
- Elasticsearch: 높음 (인덱싱 전략, 쿼리 최적화)
- Redis: 낮음 (간단한 key-value)

### 운영 비용
- Spring Boot: 무료 (오픈소스)
- MySQL: 무료 (오픈소스) / AWS RDS 유료
- Redis: 무료 (오픈소스) / AWS ElastiCache 유료
- Elasticsearch: 무료 (오픈소스) / Elastic Cloud 유료
- Google Sheets API: 무료 (할당량 내)

### 확장성
- 모든 기술 스택이 수평 확장 지원
- Stateless 아키텍처로 설계
