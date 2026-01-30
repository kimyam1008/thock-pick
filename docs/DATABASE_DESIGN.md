# 데이터베이스 설계 (Database Design)

## 📊 ERD (Entity Relationship Diagram)

```
┌─────────────────────────────────────────────────────────────────┐
│                         Core Entities                           │
└─────────────────────────────────────────────────────────────────┘

    ┌──────────────────┐           ┌──────────────────┐
    │     Switch       │           │      Plate       │
    │==================│           │==================│
    │ id (PK)          │           │ id (PK)          │
    │ name             │           │ name             │
    │ type             │           │ material         │
    │ weight           │           │ type             │
    │ manufacturer     │           │ price            │
    │ price            │           │ compatibility    │
    │ description      │           │ description      │
    │ created_at       │           │ created_at       │
    │ updated_at       │           │ updated_at       │
    └────────┬─────────┘           └──────────────────┘
             │
             │ 1:N
             │
    ┌────────▼─────────┐
    │   SwitchVideo    │  (연관 테이블)
    │==================│
    │ id (PK)          │
    │ switch_id (FK)   │────┐
    │ video_id (FK)    │    │
    │ created_at       │    │
    └──────────────────┘    │
                            │ N:1
                            │
                    ┌───────▼──────┐
                    │    Video     │
                    │==============│
                    │ id (PK)      │
                    │ title        │
                    │ url          │
                    │ thumbnail    │
                    │ channel_name │
                    │ view_count   │
                    │ published_at │
                    │ created_at   │
                    │ updated_at   │
                    └──────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     User & Community (Phase 3)                  │
└─────────────────────────────────────────────────────────────────┘

    ┌──────────────────┐
    │      User        │
    │==================│
    │ id (PK)          │
    │ email            │
    │ password         │
    │ nickname         │
    │ role             │
    │ created_at       │
    │ updated_at       │
    └────────┬─────────┘
             │
             ├─────────┬─────────┬──────────┐
             │         │         │          │
             │ 1:N     │ 1:N     │ 1:N      │
             │         │         │          │
    ┌────────▼───┐ ┌──▼──────┐ ┌▼────────┐ │
    │ Wishlist   │ │ Review  │ │ Build   │ │
    │============│ │=========│ │=========│ │
    │ id         │ │ id      │ │ id      │ │
    │ user_id    │ │ user_id │ │ user_id │ │
    │ switch_id  │ │switch_id│ │ name    │ │
    │ created_at │ │ rating  │ │switch_id│ │
    └────────────┘ │ content │ │ plate_id│ │
                   │created_at│ │ image   │ │
                   └──────────┘ │likes    │ │
                                │created_at│ │
                                └──────────┘ │
                                             │
                                             │ 1:N
                                             │
                                    ┌────────▼───────┐
                                    │  BuildComment  │
                                    │================│
                                    │ id             │
                                    │ build_id       │
                                    │ user_id        │
                                    │ content        │
                                    │ created_at     │
                                    └────────────────┘
```

---

## 📋 테이블 상세 설계

### 1. Switch (스위치)

#### 설명
키보드 스위치 정보를 저장하는 핵심 테이블

#### 컬럼 정의

| 컬럼명 | 타입 | 제약조건 | 설명 |
|-------|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 기본키 |
| name | VARCHAR(100) | NOT NULL | 스위치 이름 (예: "Gateron Yellow") |
| type | VARCHAR(20) | NOT NULL | LINEAR, TACTILE, CLICKY |
| category | VARCHAR(50) | | 탭 정보 (예: 체리, 저소음, HMX) |
| weight | INT | | 무게 (g, 예: 50) |
| manufacturer | VARCHAR(255) | | 제조사 (예: "Gateron", "Cherry") |
| price | INT | | 가격 (원, 1개당) |
| actuation_force | INT | | 작동 압력 (g) |
| bottom_out_force | INT | | 바닥 압력 (g) |
| travel_distance | DECIMAL(3,1) | | 총 이동 거리 (mm) |
| pre_travel | DECIMAL(3,1) | | 작동 거리 (mm) |
| spring_type | VARCHAR(255) | | 스프링 타입 (예: "Progressive") |
| stem_material | VARCHAR(255) | | 스템 재질 (예: "POM") |
| housing_material | VARCHAR(255) | | 하우징 재질 (예: "Nylon") |
| sound_profile | VARCHAR(20) | | 소리 특성 (QUIET, NORMAL, LOUD) |
| is_lubed | BOOLEAN | DEFAULT FALSE | 윤활 여부 |
| description | TEXT | | 상세 설명 |
| google_sheets_row | INT | | Google Sheets 행 번호 (동기화용) |
| created_at | TIMESTAMP | NOT NULL | 생성일시 (BaseEntity 자동 관리) |
| updated_at | TIMESTAMP | NOT NULL | 수정일시 (BaseEntity 자동 관리) |

#### 인덱스

```sql
CREATE INDEX idx_switch_type ON switches(type);
CREATE INDEX idx_switch_manufacturer ON switches(manufacturer);
CREATE INDEX idx_switch_price ON switches(price);
CREATE INDEX idx_switch_name ON switches(name);
CREATE INDEX idx_google_sheets_row ON switches(google_sheets_row);
```

#### JPA Entity 예시

```java
@Entity
@Table(name = "switches", indexes = {
    @Index(name = "idx_switch_type", columnList = "type"),
    @Index(name = "idx_switch_manufacturer", columnList = "manufacturer"),
    @Index(name = "idx_switch_price", columnList = "price"),
    @Index(name = "idx_switch_name", columnList = "name"),
    @Index(name = "idx_google_sheets_row", columnList = "googleSheetsRow")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Switch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SwitchType type;

    @Column(length = 50)
    private String category;

    private Integer weight;

    @Column(length = 255)
    private String manufacturer;

    private Integer price;
    private Integer actuationForce;
    private Integer bottomOutForce;

    @Column(precision = 3, scale = 1)
    private BigDecimal travelDistance;

    @Column(precision = 3, scale = 1)
    private BigDecimal preTravel;

    @Column(length = 255)
    private String springType;

    @Column(length = 255)
    private String stemMaterial;

    @Column(length = 255)
    private String housingMaterial;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SoundProfile soundProfile;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isLubed;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer googleSheetsRow;

    @OneToMany(mappedBy = "switchEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SwitchVideo> switchVideos = new ArrayList<>();

    // createdAt, updatedAt은 BaseEntity에서 관리
}
```

**참고**: `BaseEntity`를 상속받아 createdAt, updatedAt을 자동으로 관리합니다.

---

### 2. Plate (보강판)

#### 설명
키보드 보강판 정보를 저장하는 테이블 (현재 미사용, 향후 확장 예정)

#### 컬럼 정의

| 컬럼명 | 타입 | 제약조건 | 설명 |
|-------|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 기본키 |
| name | VARCHAR(100) | NOT NULL | 보강판 이름 |
| material | VARCHAR(20) | NOT NULL | ALUMINUM, BRASS, POLYCARBONATE, FR4, CARBON_FIBER |
| type | VARCHAR(20) | | FULL, HALF, GASKET |
| price | INT | | 가격 (원) |
| compatibility | VARCHAR(200) | | 호환 키보드 (예: "60%, 65%") |
| flexibility | VARCHAR(20) | | 유연성 (RIGID, MEDIUM, FLEXIBLE) |
| sound_profile | VARCHAR(50) | | 소리 특성 (CLACKY, THOCKY, MUTED) |
| description | TEXT | | 상세 설명 |
| google_sheets_row | INT | | Google Sheets 행 번호 |
| created_at | TIMESTAMP | NOT NULL | 생성일시 (BaseEntity 자동 관리) |
| updated_at | TIMESTAMP | NOT NULL | 수정일시 (BaseEntity 자동 관리) |

#### 인덱스

```sql
CREATE INDEX idx_plate_material ON plates(material);
CREATE INDEX idx_plate_type ON plates(type);
```

---

### 3. Video (유튜브 영상)

#### 설명
타건음 유튜브 영상 정보를 저장하는 테이블 (현재 미사용, 향후 확장 예정)

#### 컬럼 정의

| 컬럼명 | 타입 | 제약조건 | 설명 |
|-------|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 기본키 |
| title | VARCHAR(200) | NOT NULL | 영상 제목 |
| url | VARCHAR(500) | NOT NULL, UNIQUE | 유튜브 URL |
| youtube_id | VARCHAR(20) | UNIQUE | 유튜브 비디오 ID |
| thumbnail_url | VARCHAR(500) | | 썸네일 URL |
| channel_name | VARCHAR(100) | | 채널명 |
| view_count | INT | DEFAULT 0 | 조회수 |
| published_at | TIMESTAMP | | 업로드 일시 |
| duration | INT | | 영상 길이 (초) |
| description | TEXT | | 영상 설명 |
| google_sheets_row | INT | | Google Sheets 행 번호 |
| created_at | TIMESTAMP | NOT NULL | 생성일시 (BaseEntity 자동 관리) |
| updated_at | TIMESTAMP | NOT NULL | 수정일시 (BaseEntity 자동 관리) |

#### 인덱스

```sql
CREATE INDEX idx_video_youtube_id ON videos(youtube_id);
CREATE INDEX idx_video_view_count ON videos(view_count);
```

---

### 4. SwitchVideo (스위치-영상 연관 테이블)

#### 설명
스위치와 유튜브 영상의 N:M 관계를 표현하는 중간 테이블 (현재 미사용, 향후 확장 예정)

#### 컬럼 정의

| 컬럼명 | 타입 | 제약조건 | 설명 |
|-------|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 기본키 |
| switch_id | BIGINT | FK (switches.id), NOT NULL | 스위치 ID |
| video_id | BIGINT | FK (videos.id), NOT NULL | 영상 ID |
| relevance_score | INT | DEFAULT 0 | 관련도 점수 (추천 순서) |
| created_at | TIMESTAMP | NOT NULL | 생성일시 (BaseEntity 자동 관리) |
| updated_at | TIMESTAMP | NOT NULL | 수정일시 (BaseEntity 자동 관리) |

#### 인덱스

```sql
CREATE UNIQUE INDEX idx_switch_video_unique ON switch_videos(switch_id, video_id);
CREATE INDEX idx_switch_video_switch ON switch_videos(switch_id);
CREATE INDEX idx_switch_video_video ON switch_videos(video_id);
```

---

### 5. User (사용자) - Phase 3

#### 컬럼 정의

| 컬럼명 | 타입 | 제약조건 | 설명 |
|-------|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 기본키 |
| email | VARCHAR(100) | NOT NULL, UNIQUE | 이메일 |
| password | VARCHAR(255) | NOT NULL | 암호화된 비밀번호 |
| nickname | VARCHAR(50) | NOT NULL, UNIQUE | 닉네임 |
| role | ENUM | DEFAULT USER | USER, ADMIN |
| profile_image | VARCHAR(500) | | 프로필 이미지 URL |
| is_active | BOOLEAN | DEFAULT TRUE | 활성화 여부 |
| last_login_at | TIMESTAMP | | 마지막 로그인 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

---

### 6. Wishlist (위시리스트) - Phase 3

#### 컬럼 정의

| 컬럼명 | 타입 | 제약조건 | 설명 |
|-------|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 기본키 |
| user_id | BIGINT | FK (users.id) | 사용자 ID |
| switch_id | BIGINT | FK (switches.id) | 스위치 ID |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성일시 |

---

### 7. Review (리뷰) - Phase 3

#### 컬럼 정의

| 컬럼명 | 타입 | 제약조건 | 설명 |
|-------|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 기본키 |
| user_id | BIGINT | FK (users.id) | 사용자 ID |
| switch_id | BIGINT | FK (switches.id) | 스위치 ID |
| rating | INT | NOT NULL, CHECK(1-5) | 평점 (1-5) |
| content | TEXT | | 리뷰 내용 |
| image_url | VARCHAR(500) | | 리뷰 이미지 |
| likes | INT | DEFAULT 0 | 좋아요 수 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

---

### 8. Build (커스텀 빌드) - Phase 3

#### 컬럼 정의

| 컬럼명 | 타입 | 제약조건 | 설명 |
|-------|------|---------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 기본키 |
| user_id | BIGINT | FK (users.id) | 사용자 ID |
| name | VARCHAR(100) | NOT NULL | 빌드 이름 |
| switch_id | BIGINT | FK (switches.id) | 스위치 ID |
| plate_id | BIGINT | FK (plates.id) | 보강판 ID |
| keycaps | VARCHAR(100) | | 키캡 정보 |
| description | TEXT | | 설명 |
| image_url | VARCHAR(500) | | 이미지 URL |
| likes | INT | DEFAULT 0 | 좋아요 수 |
| views | INT | DEFAULT 0 | 조회수 |
| is_public | BOOLEAN | DEFAULT TRUE | 공개 여부 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

---

## 🔗 관계 정의

### 1:N 관계
- `Switch` (1) ↔ (N) `SwitchVideo`
- `Video` (1) ↔ (N) `SwitchVideo`
- `User` (1) ↔ (N) `Wishlist`
- `User` (1) ↔ (N) `Review`
- `User` (1) ↔ (N) `Build`

### N:M 관계 (중간 테이블 사용)
- `Switch` (N) ↔ (M) `Video` (중간: `SwitchVideo`)

---

## 📦 Enum 타입 정의

### Switch Type Enum
```java
public enum SwitchType {
    LINEAR,      // 리니어
    TACTILE,     // 택타일
    CLICKY       // 클릭키
}
```

### Sound Profile Enum
```java
public enum SoundProfile {
    QUIET,       // 조용함
    NORMAL,      // 보통
    LOUD         // 시끄러움
}
```

### Plate Material Enum
```java
public enum PlateMaterial {
    ALUMINUM,          // 알루미늄
    BRASS,             // 놋쇠
    POLYCARBONATE,     // 폴리카보네이트
    FR4,               // FR4
    CARBON_FIBER       // 탄소섬유
}
```

### Plate Type Enum
```java
public enum PlateType {
    FULL,       // 풀 플레이트
    HALF,       // 하프 플레이트
    GASKET      // 개스킷 마운트
}
```

### Flexibility Enum
```java
public enum Flexibility {
    RIGID,      // 단단함
    MEDIUM,     // 중간
    FLEXIBLE    // 유연함
}
```

---

## 🚀 마이그레이션 전략

### Phase 1 (현재 완료)
- ✅ Switch 테이블 생성 및 인덱스 적용
- ✅ Plate, Video, SwitchVideo 테이블 생성 (엔티티만, 미사용)
- ✅ BaseEntity를 통한 자동 타임스탬프 관리
- ✅ Google Sheets 동기화 기능 구현

### Phase 2 (향후 계획)
- Video, SwitchVideo 테이블 실제 사용
- 유튜브 영상 연동 기능 구현
- 인덱스 성능 모니터링 및 최적화

### Phase 3 (향후 계획)
- User, Wishlist, Review, Build 테이블 추가
- 회원 시스템 구현

---

## 💾 데이터 크기 예상

| 테이블 | 예상 레코드 수 | 예상 크기 |
|-------|--------------|----------|
| Switch | 1,000 | 500 KB |
| Plate | 100 | 50 KB |
| Video | 5,000 | 2 MB |
| SwitchVideo | 10,000 | 500 KB |
| User (Phase 3) | 10,000 | 2 MB |
| Review (Phase 3) | 50,000 | 20 MB |

**총 예상 크기 (Phase 3 완료 시)**: ~30 MB (데이터만)

---

## 🔧 최적화 전략

### 인덱스 전략
- **검색 쿼리가 많은 컬럼**: 인덱스 필수 (type, manufacturer, price)
- **조인이 많은 FK**: 인덱스 필수
- **UNIQUE 제약조건**: 자동 인덱스 생성

### 파티셔닝 (선택)
- 데이터가 100만 건 이상일 경우 고려
- 예: `created_at` 기준 월별 파티셔닝

### 정규화 vs 역정규화
- **정규화 유지**: 중복 최소화, 데이터 일관성
- **역정규화 고려 대상**:
  - 조회 빈도가 매우 높은 데이터
  - 예: Switch에 `video_count` 컬럼 추가 (캐싱 대체)

---

## 📝 DDL 스크립트 예시

```sql
-- Switch 테이블
CREATE TABLE switches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    category VARCHAR(50),
    weight INT,
    manufacturer VARCHAR(255),
    price INT,
    actuation_force INT,
    bottom_out_force INT,
    travel_distance DECIMAL(3,1),
    pre_travel DECIMAL(3,1),
    spring_type VARCHAR(255),
    stem_material VARCHAR(255),
    housing_material VARCHAR(255),
    sound_profile VARCHAR(20),
    is_lubed BOOLEAN DEFAULT FALSE,
    description TEXT,
    google_sheets_row INT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_switch_type (type),
    INDEX idx_switch_manufacturer (manufacturer),
    INDEX idx_switch_price (price),
    INDEX idx_switch_name (name),
    INDEX idx_google_sheets_row (google_sheets_row)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## ✅ 데이터 검증 규칙

### Switch
- `name`: 2~100자, 중복 불가
- `type`: LINEAR, TACTILE, CLICKY 중 하나
- `weight`: 0~200 (g)
- `price`: 0 이상

### Video
- `url`: 유튜브 URL 형식 (정규식 검증)
- `youtube_id`: 11자리 영문/숫자

---

## 🔄 동기화 시 데이터 처리

### Google Sheets → MariaDB
1. **신규 데이터**: INSERT
2. **기존 데이터 변경**: UPDATE (google_sheets_row 기준으로 판단)
3. **삭제된 데이터**: 현재는 처리하지 않음 (향후 구현 예정)

### MariaDB → Elasticsearch
1. MariaDB 저장 직후 자동 인덱싱
2. 별명 서비스(SwitchNicknameService)를 통한 한글 별명 자동 매핑
3. ES 저장 실패 시 로그만 남기고 계속 진행 (데이터 유실 방지)

## 🗄️ BaseEntity 공통 필드

모든 엔티티는 `BaseEntity`를 상속받아 타임스탬프를 자동 관리합니다.

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

**특징:**
- `@CreatedDate`: 엔티티 생성 시 자동으로 현재 시각 저장
- `@LastModifiedDate`: 엔티티 수정 시 자동으로 현재 시각 업데이트
- JPA Auditing 활성화 필요 (`@EnableJpaAuditing`)
