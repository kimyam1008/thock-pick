# 패키지 구조 리팩토링 히스토리

**일자**: 2025-12-30
**작업자**: Yoon
**목적**: 계층형 구조 → 기능 기반 구조 → 하이브리드 구조로 단계적 개선

---

## 📋 목차

1. [Before: 계층형 구조 (Layered Architecture)](#1-before-계층형-구조-layered-architecture)
2. [Step 1: 기능 기반 구조 (Package by Feature)](#2-step-1-기능-기반-구조-package-by-feature)
3. [Step 2: 하이브리드 구조 (Hybrid Architecture)](#3-step-2-하이브리드-구조-hybrid-architecture)
4. [변경 상세 내역](#4-변경-상세-내역)
5. [추가된 핵심 클래스](#5-추가된-핵심-클래스)
6. [의존성 변경](#6-의존성-변경)
7. [빌드 검증](#7-빌드-검증)
8. [마이그레이션 가이드](#8-마이그레이션-가이드)

---

## 1. Before: 계층형 구조 (Layered Architecture)

### 기존 구조 (Classic Layered)

```
src/main/java/com/thockpick/
├── domain/
│   ├── entity/              # 모든 엔티티를 여기에
│   │   ├── Switch.java
│   │   ├── Plate.java
│   │   ├── Video.java
│   │   └── SwitchVideo.java
│   │
│   ├── enums/               # 모든 Enum을 여기에
│   │   ├── SwitchType.java
│   │   ├── SoundProfile.java
│   │   ├── PlateType.java
│   │   ├── PlateMaterial.java
│   │   └── Flexibility.java
│   │
│   └── repository/          # 모든 Repository를 여기에
│       ├── SwitchRepository.java
│       ├── PlateRepository.java
│       ├── VideoRepository.java
│       └── SwitchVideoRepository.java
│
└── ThockPickServerApplication.java
```

### 문제점

1. **낮은 도메인 응집도**
   - Switch 관련 코드가 `entity/`, `enums/`, `repository/` 3곳에 분산
   - 도메인 로직 파악을 위해 여러 패키지를 넘나들어야 함

2. **확장성 문제**
   - 파일이 많아질수록 각 패키지 내부가 비대해짐
   - 새로운 도메인 추가 시 모든 레이어에 파일 추가 필요

3. **낮은 모듈화**
   - 도메인 간 경계가 불명확
   - 하나의 도메인을 독립적으로 테스트하기 어려움

---

## 2. Step 1: 기능 기반 구조 (Package by Feature)

### 변경된 구조 (Domain-Driven)

```
src/main/java/com/thockpick/
├── domain/
│   ├── switches/            # 스위치 도메인 - 모든 것이 여기에
│   │   ├── Switch.java          (Entity)
│   │   ├── SwitchRepository.java (Repository)
│   │   └── SwitchType.java      (Enum - 스위치 전용)
│   │
│   ├── videos/              # 영상 도메인
│   │   ├── Video.java
│   │   ├── SwitchVideo.java
│   │   ├── VideoRepository.java
│   │   └── SwitchVideoRepository.java
│   │
│   └── plates/              # 보강판 도메인
│       ├── Plate.java
│       ├── PlateRepository.java
│       ├── PlateType.java       (Enum - 보강판 전용)
│       └── PlateMaterial.java   (Enum - 보강판 전용)
│
├── global/                  # 공통 요소
│   └── enums/
│       ├── SoundProfile.java    (여러 도메인에서 사용)
│       └── Flexibility.java     (여러 도메인에서 사용)
│
└── ThockPickServerApplication.java
```

### 개선 효과

✅ **도메인 응집도 향상**
- 스위치 관련 모든 코드가 `switches/` 패키지에 응집
- 도메인 이해와 수정이 용이

✅ **모듈화 강화**
- 각 도메인이 독립적인 패키지로 분리
- 도메인별 독립 테스트 가능

✅ **확장성 확보**
- 새 도메인 추가 시 `domain/` 아래 새 패키지만 추가
- Service, Controller 추가 시에도 같은 패키지 내 배치 가능

### 주요 변경 작업

1. **패키지 이동** (git mv 사용)
   ```bash
   # Switch 도메인
   git mv domain/entity/Switch.java → domain/switches/Switch.java
   git mv domain/repository/SwitchRepository.java → domain/switches/SwitchRepository.java
   git mv domain/enums/SwitchType.java → domain/switches/SwitchType.java

   # Video 도메인
   git mv domain/entity/Video.java → domain/videos/Video.java
   git mv domain/entity/SwitchVideo.java → domain/videos/SwitchVideo.java
   git mv domain/repository/VideoRepository.java → domain/videos/VideoRepository.java
   git mv domain/repository/SwitchVideoRepository.java → domain/videos/SwitchVideoRepository.java

   # Plate 도메인
   git mv domain/entity/Plate.java → domain/plates/Plate.java
   git mv domain/repository/PlateRepository.java → domain/plates/PlateRepository.java
   git mv domain/enums/PlateType.java → domain/plates/PlateType.java
   git mv domain/enums/PlateMaterial.java → domain/plates/PlateMaterial.java

   # Global Enum
   git mv domain/enums/SoundProfile.java → global/enums/SoundProfile.java
   git mv domain/enums/Flexibility.java → global/enums/Flexibility.java
   ```

2. **패키지 선언 수정**
   ```java
   // Before
   package com.thockpick.domain.entity;
   import com.thockpick.domain.enums.SwitchType;

   // After
   package com.thockpick.domain.switches;
   // SwitchType이 같은 패키지에 있어서 import 불필요
   ```

3. **Import 경로 수정**
   ```java
   // Switch.java
   import com.thockpick.global.enums.SoundProfile;  // 공용 Enum
   import com.thockpick.domain.videos.SwitchVideo;  // 다른 도메인 참조

   // SwitchVideo.java
   import com.thockpick.domain.switches.Switch;  // Switch 도메인 참조
   ```

---

## 3. Step 2: 하이브리드 구조 (Hybrid Architecture)

### 최종 구조 (Domain-Driven + Infrastructure)

```
src/main/java/com/thockpick/
├── domain/                  # 비즈니스 도메인
│   ├── switches/
│   │   ├── Switch.java
│   │   ├── SwitchRepository.java
│   │   └── SwitchType.java
│   │
│   ├── videos/
│   │   ├── Video.java
│   │   ├── SwitchVideo.java
│   │   ├── VideoRepository.java
│   │   └── SwitchVideoRepository.java
│   │
│   └── plates/
│       ├── Plate.java
│       ├── PlateRepository.java
│       ├── PlateType.java
│       └── PlateMaterial.java
│
├── infrastructure/          # ⭐ 새로 추가 - 인프라 계층
│   └── sync/                # Google Sheets 동기화 (Phase 1)
│       └── (추후 추가 예정)
│   # Phase 2에 추가 예정:
│   # ├── search/           # Elasticsearch
│   # ├── cache/            # Redis
│   # └── security/         # Spring Security
│
├── global/                  # ⭐ 보강됨 - 공통 요소
│   ├── config/              # 설정 클래스
│   │   └── JpaConfig.java
│   │
│   ├── exception/           # 예외 처리
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   └── ErrorCode.java
│   │
│   ├── common/              # 공통 클래스
│   │   ├── BaseEntity.java
│   │   └── ApiResponse.java
│   │
│   └── enums/               # 공통 Enum
│       ├── SoundProfile.java
│       └── Flexibility.java
│
└── ThockPickServerApplication.java
```

### 추가 개선 효과

✅ **도메인과 인프라 명확한 분리**
- Google Sheets, Elasticsearch, Redis는 인프라
- `infrastructure/` 패키지로 분리하여 관심사 분리

✅ **전역 기능 체계화**
- `global/config/` - 설정
- `global/exception/` - 예외 처리
- `global/common/` - 공통 클래스

✅ **코드 중복 제거**
- `BaseEntity`로 createdAt, updatedAt 공통화
- `ApiResponse`로 API 응답 형식 통일

✅ **확장 준비 완료**
- Phase 2-3의 Elasticsearch, Redis, Security 추가 위치 확보

---

## 4. 변경 상세 내역

### 4.1 패키지 구조 생성

```bash
mkdir -p src/main/java/com/thockpick/infrastructure/sync
mkdir -p src/main/java/com/thockpick/global/config
mkdir -p src/main/java/com/thockpick/global/exception
mkdir -p src/main/java/com/thockpick/global/common
```

### 4.2 Entity 리팩토링 (BaseEntity 상속)

#### Before: 각 Entity에 중복 코드

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Switch {
    // ... 필드들 ...

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**문제점**: Switch, Video, Plate, SwitchVideo 모두 동일한 코드 중복

#### After: BaseEntity 상속

```java
// BaseEntity.java (global/common/)
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

// Switch.java
@Entity
public class Switch extends BaseEntity {
    // createdAt, updatedAt 제거
    // 다른 필드들만 정의
}
```

**개선 효과**:
- ✅ 4개 Entity에서 중복 코드 제거
- ✅ 날짜 필드 관리 일원화
- ✅ BaseEntity만 수정하면 모든 Entity에 반영

### 4.3 JPA Auditing 설정 개선

#### Before: Application 클래스에 설정

```java
@SpringBootApplication
@EnableJpaAuditing  // 여기에 있었음
public class ThockPickServerApplication {
    // ...
}
```

**문제점**:
- 설정이 Application 클래스에 혼재
- 테스트 시 JPA Auditing 제어 어려움

#### After: 별도 Config 클래스 분리

```java
// JpaConfig.java (global/config/)
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}

// ThockPickServerApplication.java
@SpringBootApplication  // @EnableJpaAuditing 제거
public class ThockPickServerApplication {
    // ...
}
```

**개선 효과**:
- ✅ 관심사 분리 (설정 vs 애플리케이션)
- ✅ 테스트 시 JpaConfig 제외 가능
- ✅ 설정 관리 용이

---

## 5. 추가된 핵심 클래스

### 5.1 BaseEntity.java

**위치**: `global/common/BaseEntity.java`

**목적**: 모든 엔티티의 공통 필드 관리

```java
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**사용처**: Switch, Video, Plate, SwitchVideo

---

### 5.2 JpaConfig.java

**위치**: `global/config/JpaConfig.java`

**목적**: JPA Auditing 활성화

```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
```

---

### 5.3 ErrorCode.java

**위치**: `global/exception/ErrorCode.java`

**목적**: 에러 코드 및 메시지 정의

```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메소드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 오류가 발생했습니다."),

    // Switch
    SWITCH_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "스위치를 찾을 수 없습니다."),
    SWITCH_ALREADY_EXISTS(HttpStatus.CONFLICT, "S002", "이미 존재하는 스위치입니다."),

    // Plate
    PLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "보강판을 찾을 수 없습니다."),
    PLATE_ALREADY_EXISTS(HttpStatus.CONFLICT, "P002", "이미 존재하는 보강판입니다."),

    // Video
    VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "V001", "영상을 찾을 수 없습니다."),
    VIDEO_ALREADY_EXISTS(HttpStatus.CONFLICT, "V002", "이미 존재하는 영상입니다."),

    // Sync
    SYNC_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SY001", "동기화에 실패했습니다."),
    GOOGLE_SHEETS_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "SY002", "Google Sheets API 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
```

**특징**:
- HTTP 상태 코드 + 커스텀 에러 코드 + 메시지
- 도메인별 에러 코드 체계화 (S001, P001, V001, SY001)

---

### 5.4 BusinessException.java

**위치**: `global/exception/BusinessException.java`

**목적**: 비즈니스 로직 예외

```java
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
```

**사용 예**:
```java
// Service 계층
public Switch findById(Long id) {
    return switchRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.SWITCH_NOT_FOUND));
}
```

---

### 5.5 GlobalExceptionHandler.java

**위치**: `global/exception/GlobalExceptionHandler.java`

**목적**: 전역 예외 처리

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException 처리
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse<Void> response = ApiResponse.error(errorCode.getCode(), errorCode.getMessage());
        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    /**
     * Validation 예외 처리 (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage());
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(ErrorCode.INVALID_INPUT_VALUE.getMessage());

        ApiResponse<Void> response = ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE.getCode(), message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Exception: ", e);
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

**처리하는 예외**:
- `BusinessException` - 비즈니스 로직 예외
- `MethodArgumentNotValidException` - @Valid 검증 실패
- `MethodArgumentTypeMismatchException` - 타입 불일치
- `HttpRequestMethodNotSupportedException` - 잘못된 HTTP 메소드
- `Exception` - 기타 모든 예외

---

### 5.6 ApiResponse.java

**위치**: `global/common/ApiResponse.java`

**목적**: 통일된 API 응답 형식

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ErrorResponse error;

    private ApiResponse(boolean success, T data, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    /**
     * 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * 성공 응답 생성 (데이터 없음)
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, null, null);
    }

    /**
     * 실패 응답 생성
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorResponse(code, message));
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ErrorResponse {
        private String code;
        private String message;

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
```

**응답 형식**:

```json
// 성공 응답
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Cherry MX Red"
  }
}

// 실패 응답
{
  "success": false,
  "error": {
    "code": "S001",
    "message": "스위치를 찾을 수 없습니다."
  }
}
```

**사용 예**:
```java
// Controller
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<SwitchResponse>> getSwitch(@PathVariable Long id) {
    SwitchResponse data = switchService.findById(id);
    return ResponseEntity.ok(ApiResponse.success(data));
}
```

---

## 6. 의존성 변경

### build.gradle 변경 내역

```gradle
dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'  // ⭐ 추가

    // Google Sheets API (Phase 1 - Sprint 1.3 대비)  // ⭐ 추가
    implementation 'com.google.api-client:google-api-client:2.2.0'
    implementation 'com.google.oauth-client:google-oauth-client-jetty:1.34.1'
    implementation 'com.google.apis:google-api-services-sheets:v4-rev20230227-2.0.0'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Database
    runtimeOnly 'com.h2database:h2'
    runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'

    // Dev Tools
    developmentOnly 'org.springframework.boot:spring-boot-devtools'

    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

### 추가된 의존성

1. **spring-boot-starter-validation**
   - 목적: `@Valid`, `@NotNull` 등 검증 애노테이션 사용
   - 사용처: Controller의 DTO 검증

2. **Google Sheets API 라이브러리** (Sprint 1.3 대비)
   - `google-api-client` - Google API 클라이언트
   - `google-oauth-client-jetty` - OAuth 인증
   - `google-api-services-sheets` - Sheets API

---

## 7. 빌드 검증

### 빌드 결과

```bash
./gradlew clean build

BUILD SUCCESSFUL in 10s
8 actionable tasks: 8 executed
```

✅ **컴파일 성공**
✅ **테스트 통과**
✅ **패키지 생성 완료** (build/libs/thock-pick-server-0.0.1-SNAPSHOT.jar)

### 테스트 확인 사항

- [x] JPA Auditing 정상 동작 (BaseEntity)
- [x] Entity 관계 매핑 정상 (Switch ↔ SwitchVideo ↔ Video)
- [x] Repository 쿼리 정상 동작
- [x] 애플리케이션 구동 정상

---

## 8. 마이그레이션 가이드

### 8.1 기존 프로젝트에서 이 구조로 마이그레이션하는 방법

#### Step 1: 패키지 구조 생성

```bash
mkdir -p src/main/java/com/yourproject/infrastructure/sync
mkdir -p src/main/java/com/yourproject/global/config
mkdir -p src/main/java/com/yourproject/global/exception
mkdir -p src/main/java/com/yourproject/global/common
```

#### Step 2: BaseEntity 추가

`global/common/BaseEntity.java` 파일 생성 후 모든 Entity가 상속

#### Step 3: JpaConfig 추가

`global/config/JpaConfig.java` 파일 생성
Application 클래스에서 `@EnableJpaAuditing` 제거

#### Step 4: 예외 처리 체계 추가

1. `ErrorCode.java` 생성
2. `BusinessException.java` 생성
3. `GlobalExceptionHandler.java` 생성
4. `ApiResponse.java` 생성

#### Step 5: build.gradle 의존성 추가

Validation, Google Sheets API 의존성 추가

#### Step 6: 빌드 및 테스트

```bash
./gradlew clean build
./gradlew test
```

### 8.2 기존 코드 영향 최소화 방법

✅ **하위 호환성 유지**
- 기존 import 경로를 새 경로로 변경만 하면 됨
- Entity의 동작은 완전히 동일 (BaseEntity 상속 후에도)

✅ **점진적 적용 가능**
- 먼저 패키지 구조만 변경
- 이후 BaseEntity, 예외 처리 등 추가

✅ **테스트 코드 수정 최소화**
- 패키지 import만 변경
- 테스트 로직은 변경 불필요

---

## 9. 향후 확장 계획

### Phase 2 추가 예정 (Elasticsearch, Redis)

```
infrastructure/
├── sync/                    # Phase 1 ✅
│   ├── GoogleSheetsClient.java
│   ├── SyncScheduler.java
│   └── SyncService.java
│
├── search/                  # Phase 2 예정
│   ├── ElasticsearchConfig.java
│   ├── SwitchDocument.java
│   └── SwitchSearchRepository.java
│
└── cache/                   # Phase 2 예정
    └── CacheConfig.java
```

### Phase 3 추가 예정 (사용자, 커뮤니티)

```
domain/
├── switches/
├── videos/
├── plates/
├── user/                    # Phase 3 예정
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── controller/
│
├── review/                  # Phase 3 예정
└── wishlist/                # Phase 3 예정

infrastructure/
└── security/                # Phase 3 예정
    ├── JwtTokenProvider.java
    └── SecurityConfig.java
```

---

## 10. 참고 자료

### Package by Feature vs Package by Layer

**Package by Layer (계층형)** - Before
```
controller/
service/
repository/
entity/
```
- 장점: 전통적, 학습 용이
- 단점: 낮은 응집도, 확장 어려움

**Package by Feature (기능 기반)** - After
```
user/
product/
order/
```
- 장점: 높은 응집도, 독립성, 확장 용이
- 단점: 초기 설계 중요

### 관련 문서

- [Martin Fowler - Package by Feature](https://www.martinfowler.com/bliki/PackageByFeature.html)
- [DDD - Domain-Driven Design](https://martinfowler.com/tags/domain%20driven%20design.html)
- [Spring Boot Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.structuring-your-code)

---

## 11. 변경 이력

| 일자 | 변경 내용 | 작업자 |
|------|----------|--------|
| 2025-12-30 | 계층형 → 기능 기반 구조 변경 | Yoon |
| 2025-12-30 | infrastructure/, global/ 패키지 추가 | Yoon |
| 2025-12-30 | BaseEntity, 예외 처리 체계 추가 | Yoon |
| 2025-12-30 | Google Sheets API 의존성 추가 | Yoon |

---

## 12. 체크리스트

### 리팩토링 완료 확인

- [x] 패키지 구조 변경 완료
- [x] 모든 import 경로 수정 완료
- [x] BaseEntity 상속 완료 (Switch, Video, Plate, SwitchVideo)
- [x] JpaConfig 분리 완료
- [x] 예외 처리 체계 추가 완료
- [x] ApiResponse 통일 완료
- [x] build.gradle 의존성 추가 완료
- [x] 빌드 성공 확인
- [x] 테스트 통과 확인

### 다음 단계 (Sprint 1.3)

- [ ] `infrastructure/sync/GoogleSheetsClient.java` 구현
- [ ] `infrastructure/sync/SyncScheduler.java` 구현
- [ ] `infrastructure/sync/SyncService.java` 구현
- [ ] Google Sheets 인증 설정
- [ ] 동기화 테스트

---

**문서 작성일**: 2025-12-30
**문서 버전**: 1.0
**다음 업데이트 예정**: Sprint 1.3 완료 시 (Google Sheets 동기화 구현)
