# API 설계 (API Design)

## 📋 API 개요

ThockPick의 API 설계 문서입니다. 현재는 Thymeleaf 기반 서버 사이드 렌더링을 주로 사용하며, 일부 Ajax API를 제공합니다.

### Base URL
```
개발: http://localhost:8080
운영: https://thockpick.com
```

---

## 🏠 View Pages (Thymeleaf)

### 1. 홈 페이지

#### Request
```http
GET /
```

#### Response
- HTML 페이지 (Thymeleaf 렌더링)
- 전체 스위치 개수 표시
- 타입별(LINEAR, TACTILE, CLICKY) 개수 통계

---

### 2. 스위치 목록 페이지

#### Request
```http
GET /switches
```

#### Response
- HTML 페이지 (Thymeleaf 렌더링)
- 타입 필터 드롭다운 제공
- Ajax로 스위치 데이터 로드

---

### 3. 스위치 상세 페이지

#### Request
```http
GET /switches/{id}
```

#### Path Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| id | long | 스위치 ID |

#### Response
- HTML 페이지 (Thymeleaf 렌더링)
- 스위치 상세 정보 표시

---

## 🔍 Ajax API

### 1. 스위치 목록 조회 (Ajax)

#### Request
```http
GET /switches/api?type=LINEAR&manufacturer=Cherry&keyword=red&page=0&size=12
```

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 | 기본값 |
|---------|------|------|------|-------|
| type | string | X | 스위치 타입 (LINEAR, TACTILE, CLICKY) | - |
| manufacturer | string | X | 제조사 | - |
| keyword | string | X | 검색 키워드 (Elasticsearch) | - |
| page | int | X | 페이지 번호 | 0 |
| size | int | X | 페이지 크기 | 12 |

#### Response (200 OK)
```json
{
  "switches": [
    {
      "id": 1,
      "name": "Cherry MX Red",
      "manufacturer": "Cherry",
      "type": "LINEAR",
      "actuationForce": 45,
      "bottomOutForce": 60,
      "preTravel": 2.0,
      "totalTravel": 4.0,
      "springType": "Standard",
      "stemMaterial": "POM",
      "topHousingMaterial": "Nylon",
      "bottomHousingMaterial": "Nylon",
      "soundLevel": "NORMAL",
      "price": 500,
      "isFactoryLubed": false,
      "releaseDate": "2025-01-01",
      "features": "부드러운 리니어",
      "productUrl": "https://example.com",
      "dataSource": "GOOGLE_SHEETS"
    }
  ],
  "currentPage": 0,
  "totalPages": 5,
  "totalElements": 60,
  "hasNext": true,
  "hasPrevious": false
}
```

---

## 🔄 Sync API (동기화)

### 1. 스위치 동기화

Google Sheets 데이터를 MariaDB와 Elasticsearch로 동기화합니다.

#### Request
```http
POST /api/sync/switches
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": 42,
  "message": null
}
```

**data**: 동기화된 스위치 개수

---

## 📊 공통 응답 형식

### ApiResponse<T>

```java
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
}
```

#### 성공 응답 예시
```json
{
  "success": true,
  "data": { ... },
  "message": null
}
```

#### 에러 응답 예시
```json
{
  "success": false,
  "data": null,
  "message": "스위치를 찾을 수 없습니다."
}
```

---

## 📝 HTTP 상태 코드

| 코드 | 설명 |
|------|------|
| 200 OK | 요청 성공 |
| 400 Bad Request | 잘못된 요청 |
| 404 Not Found | 리소스를 찾을 수 없음 |
| 500 Internal Server Error | 서버 오류 |

---

## 🔮 향후 추가 예정 API

### Phase 2: 검색 기능 개선
- `GET /api/switches/search` - Elasticsearch 전문 검색 API
- `GET /api/switches/autocomplete` - 자동완성 API
- `GET /api/switches/popular` - 인기 스위치 Top 10

### Phase 3: 유튜브 영상
- `GET /api/videos/switch/{switchId}` - 스위치별 유튜브 영상 조회
- `GET /api/videos` - 전체 영상 목록

### Phase 4: 보강판
- `GET /api/plates` - 보강판 목록 조회
- `GET /api/plates/{id}` - 보강판 상세 조회
- `GET /api/plates/search` - 보강판 검색

### Phase 5: 추천
- `POST /api/recommendations` - 취향 기반 스위치 추천
- `GET /api/recommendations/popular` - 인기 추천

### Phase 6: 사용자
- `POST /api/auth/register` - 회원가입
- `POST /api/auth/login` - 로그인
- `GET /api/users/me` - 내 정보 조회

### Phase 7: 위시리스트
- `GET /api/users/me/wishlist` - 위시리스트 조회
- `POST /api/users/me/wishlist` - 위시리스트 추가
- `DELETE /api/users/me/wishlist/{switchId}` - 위시리스트 삭제

---

## 📚 추가 문서

- [ARCHITECTURE.md](./ARCHITECTURE.md) - 시스템 아키텍처
- [DATABASE_DESIGN.md](./DATABASE_DESIGN.md) - 데이터베이스 설계
- [TECH_STACK.md](./TECH_STACK.md) - 기술 스택
- [ELASTICSEARCH_INTEGRATION.md](./ELASTICSEARCH_INTEGRATION.md) - Elasticsearch 통합 가이드
