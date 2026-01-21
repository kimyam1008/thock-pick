# Elasticsearch 통합 가이드

## 개요

ThockPick 프로젝트에 Elasticsearch를 도입하여 스위치 검색 기능을 구현했습니다. 한글 형태소 분석(Nori Analyzer)과 별명 기반 검색을 통해 사용자 친화적인 검색 경험을 제공합니다.

### 도입 목적
- **한글 검색 최적화**: "적축", "체리적축" 등 한글 별명으로 검색 가능
- **빠른 전문 검색**: MariaDB의 LIKE 쿼리 대비 성능 개선
- **형태소 분석**: Nori 플러그인을 통한 정확한 한글 토큰화

## 기술 스택

| 항목 | 버전 | 설명 |
|------|------|------|
| Elasticsearch | 8.11.1 | 검색 엔진 |
| Spring Data Elasticsearch | 3.5.9 (Spring Boot Starter) | Spring 통합 라이브러리 |
| Nori Analyzer | 8.11.1 | 한글 형태소 분석 플러그인 |
| MariaDB | 10.11 | 주 데이터베이스 |

## 아키텍처

### 인프라 구조

```
┌─────────────────┐
│ Spring Boot App │
├─────────────────┤
│  SwitchSync     │──┐
│   Service       │  │
└─────────────────┘  │
         │           │
         ↓           ↓
┌─────────────┐  ┌──────────────┐
│  MariaDB    │  │ Elasticsearch│
│  (Primary)  │  │  (Search)    │
└─────────────┘  └──────────────┘
```

- **MariaDB**: 스위치 데이터의 원본 저장소
- **Elasticsearch**: 검색 인덱스 저장소
- **동기화**: Google Sheets → MariaDB → Elasticsearch 순서로 데이터 동기화

### 애플리케이션 구조

```
src/main/java/com/thockpick/
├── application/switches/
│   └── SwitchNicknameService.java          # 별명 매핑 서비스
├── infrastructure/search/
│   ├── document/
│   │   └── SwitchDocument.java             # ES 문서 모델
│   └── repository/
│       └── SwitchSearchRepository.java     # ES 리포지토리
└── infrastructure/sync/googlesheets/service/
    └── SwitchSyncService.java              # 동기화 서비스
```

## 주요 컴포넌트

### 1. SwitchDocument

Elasticsearch에 저장되는 스위치 검색 문서입니다.

**위치**: `src/main/java/com/thockpick/infrastructure/search/document/SwitchDocument.java`

```java
@Document(indexName = "switches")
public class SwitchDocument {
    @Id
    private Long id;                        // MariaDB ID와 동일

    @Field(type = FieldType.Text, analyzer = "nori")
    private String name;                    // 스위치 이름 (형태소 분석)

    @Field(type = FieldType.Keyword)
    private String brand;                   // 제조사 (정확한 매칭)

    @Field(type = FieldType.Text, analyzer = "nori")
    private List<String> nicknames;         // 한글 별명 리스트
}
```

**특징**:
- `name`, `nicknames` 필드에 Nori Analyzer 적용
- MariaDB의 ID를 그대로 사용하여 일대일 매핑

### 2. SwitchSearchRepository

Spring Data Elasticsearch를 사용한 검색 리포지토리입니다.

**위치**: `src/main/java/com/thockpick/infrastructure/search/repository/SwitchSearchRepository.java`

```java
public interface SwitchSearchRepository
    extends ElasticsearchRepository<SwitchDocument, Long> {

    // 이름 또는 별명으로 검색
    List<SwitchDocument> findByNameContainsOrNicknamesContains(
        String name, String nickname
    );
}
```

**제공 기능**:
- 기본 CRUD 작업 (상속)
- 이름/별명 복합 검색

### 3. SwitchNicknameService

스위치 이름에 따라 한글 별명을 매핑하는 서비스입니다.

**위치**: `src/main/java/com/thockpick/application/switches/SwitchNicknameService.java`

**데이터 파일**: `src/main/resources/data/switch_nicknames.json`

```json
[
  { "keyword": "Silent Red", "nicknames": ["저적", "저소음적축", "체리저적"] },
  { "keyword": "Black", "nicknames": ["흑축", "삼신흑", "사신흑"] },
  { "keyword": "Blue", "nicknames": ["청축", "클릭"] },
  { "keyword": "Holy Panda", "nicknames": ["홀판", "홀리판다"] }
]
```

**동작 방식**:
1. 애플리케이션 시작 시 JSON 파일 로드 (`@PostConstruct`)
2. 스위치 이름에 키워드가 포함되어 있으면 해당 별명 리스트 반환
3. 대소문자 구분 없이 검색

### 4. SwitchSyncService (수정됨)

Google Sheets 데이터를 MariaDB와 Elasticsearch에 동기화하는 서비스입니다.

**위치**: `src/main/java/com/thockpick/infrastructure/sync/googlesheets/service/SwitchSyncService.java:119`

**주요 로직**:

```java
private void saveToElasticsearch(Switch switchEntity) {
    // 1. 별명 가져오기
    List<String> nicknames = switchNicknameService.getNicknames(
        switchEntity.getName()
    );

    // 2. ES 문서 생성
    SwitchDocument doc = SwitchDocument.builder()
        .id(switchEntity.getId())
        .name(switchEntity.getName())
        .brand(switchEntity.getManufacturer())
        .nicknames(nicknames)
        .build();

    // 3. ES 저장
    switchSearchRepository.save(doc);
}
```

**에러 처리**:
- MariaDB 저장과 ES 저장이 분리되어 있음
- ES 저장 실패 시 로그만 남기고 MariaDB는 정상 커밋
- 데이터 유실 방지 우선

## Docker 설정

### docker-compose.yml

```yaml
elasticsearch:
  image: docker.elastic.co/elasticsearch/elasticsearch:8.11.1
  container_name: thockpick-es
  environment:
    - discovery.type=single-node      # 단일 노드 모드
    - xpack.security.enabled=false    # 개발 환경용 보안 비활성화
    - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
  ports:
    - "9200:9200"
  volumes:
    - es_data:/usr/share/elasticsearch/data
  command: >
    /bin/sh -c "./bin/elasticsearch-plugin list | grep -q analysis-nori ||
    ./bin/elasticsearch-plugin install analysis-nori;
    /usr/local/bin/docker-entrypoint.sh elasticsearch"
```

**특징**:
- 컨테이너 시작 시 Nori 플러그인 자동 설치
- 개발 환경용 설정 (보안 비활성화)
- 메모리 제한: 512MB

### build.gradle

```gradle
dependencies {
    // Elasticsearch
    implementation 'org.springframework.boot:spring-boot-starter-data-elasticsearch'
}
```

## 주요 기능

### 1. 한글 형태소 분석

Nori Analyzer를 통해 한글 텍스트를 형태소 단위로 분석합니다.

**예시**:
```
입력: "체리 저소음 적축"
토큰: ["체리", "저소음", "적축"]
```

### 2. 별명 기반 검색

사용자가 일상적으로 사용하는 별명으로 검색할 수 있습니다.

**검색 시나리오**:
```
검색어: "적축"
→ nicknames 필드에서 매칭
→ "Cherry MX Silent Red" 스위치 검색됨
```

### 3. 데이터 동기화 흐름

```
1. Google Sheets 데이터 읽기
   ↓
2. MariaDB에 저장 (신규/업데이트)
   ↓
3. 별명 서비스로 별명 조회
   ↓
4. Elasticsearch에 인덱싱
   ↓
5. 검색 가능 상태
```

## 사용 방법

### 1. Elasticsearch 실행

```bash
docker-compose up -d elasticsearch
```

### 2. 인덱스 상태 확인

```bash
curl http://localhost:9200/_cat/indices?v
```

### 3. 데이터 동기화

Google Sheets 데이터를 동기화하면 자동으로 Elasticsearch에도 인덱싱됩니다.

```java
@Autowired
private SwitchSyncService switchSyncService;

// 전체 동기화
int count = switchSyncService.syncAllSwitches();
```

### 4. 검색 예제

```java
@Autowired
private SwitchSearchRepository searchRepository;

// "적축"으로 검색
List<SwitchDocument> results = searchRepository
    .findByNameContainsOrNicknamesContains("적축", "적축");
```

## 검색 쿼리 예시

### 직접 REST API 호출

```bash
# 전체 문서 조회
curl http://localhost:9200/switches/_search?pretty

# 특정 별명으로 검색
curl -X GET "http://localhost:9200/switches/_search?pretty" \
  -H 'Content-Type: application/json' \
  -d '{
    "query": {
      "match": {
        "nicknames": "적축"
      }
    }
  }'
```

## 현재 제약사항

1. **별명 데이터 관리**
   - 별명은 `switch_nicknames.json` 파일에 하드코딩
   - 별명 추가/수정 시 애플리케이션 재시작 필요

2. **동기화 방식**
   - 실시간 동기화 아님 (수동 트리거)
   - Google Sheets → MariaDB → ES 순차 처리

3. **에러 복구**
   - ES 인덱싱 실패 시 재시도 로직 없음
   - 로그만 남기고 계속 진행

## 향후 개선 사항

### 1. 실시간 동기화
```java
// JPA Event Listener 활용
@EntityListeners(SwitchEntityListener.class)
public class Switch {
    // ...
}

public class SwitchEntityListener {
    @PostPersist
    @PostUpdate
    public void onSwitchChange(Switch entity) {
        // ES 자동 업데이트
    }
}
```

### 2. 별명 관리 API
- 관리자 페이지에서 별명 추가/수정
- 데이터베이스에 별명 테이블 생성
- 실시간 반영

### 3. 고급 검색 기능
- 퍼지 검색 (오타 허용)
- 가중치 기반 랭킹
- 필터링 (제조사, 타입 등)

### 4. 검색 성능 최적화
- 캐싱 적용
- 페이지네이션 구현
- 인덱스 샤딩 (데이터 증가 시)

### 5. 모니터링
- Elasticsearch 상태 체크
- 동기화 실패 알림
- 검색 쿼리 분석

## 참고 자료

- [Spring Data Elasticsearch 공식 문서](https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/)
- [Elasticsearch Nori Analyzer](https://www.elastic.co/guide/en/elasticsearch/plugins/current/analysis-nori.html)
- [Elasticsearch 한글 가이드](https://esbook.kimjmin.net/)

## 문제 해결

### Elasticsearch 연결 실패
```
Could not connect to Elasticsearch at localhost:9200
```
**해결**: Docker 컨테이너 상태 확인
```bash
docker ps | grep elasticsearch
docker-compose restart elasticsearch
```

### Nori 플러그인 미설치
```
No analyzer found for [nori]
```
**해결**: docker-compose.yml의 command 섹션 확인 및 컨테이너 재생성
```bash
docker-compose down
docker-compose up -d
```

### 인덱스 데이터 초기화
```bash
# 인덱스 삭제
curl -X DELETE http://localhost:9200/switches

# 애플리케이션 재시작 시 자동 재생성
```

---

**작성일**: 2026-01-21
**작성자**: ThockPick Team
**최종 수정일**: 2026-01-21
