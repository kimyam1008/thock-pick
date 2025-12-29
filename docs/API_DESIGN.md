# API 설계 (API Design)

## 📋 API 개요

ThockPick의 REST API 설계 문서입니다. RESTful 원칙을 따르며, JSON 형식으로 데이터를 주고받습니다.

### Base URL
```
개발: http://localhost:8080/api
운영: https://api.thockpick.com/api
```

### 공통 응답 형식

#### 성공 응답
```json
{
  "success": true,
  "data": { ... },
  "message": null
}
```

#### 에러 응답
```json
{
  "success": false,
  "data": null,
  "message": "에러 메시지",
  "errorCode": "ERROR_CODE"
}
```

### HTTP 상태 코드
- `200 OK`: 성공
- `201 Created`: 리소스 생성 성공
- `400 Bad Request`: 잘못된 요청
- `404 Not Found`: 리소스 없음
- `500 Internal Server Error`: 서버 에러

---

## 🔍 Switch API (스위치)

### 1. 스위치 목록 조회

#### Request
```http
GET /api/switches?page=0&size=20&sort=name,asc
```

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 | 기본값 |
|---------|------|------|------|-------|
| page | int | X | 페이지 번호 | 0 |
| size | int | X | 페이지 크기 | 20 |
| sort | string | X | 정렬 (name, price, weight) | name,asc |

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Gateron Yellow",
        "type": "LINEAR",
        "weight": 50,
        "manufacturer": "Gateron",
        "price": 300,
        "actuationForce": 50,
        "bottomOutForce": 62,
        "soundProfile": "NORMAL",
        "description": "부드럽고 저렴한 리니어 스위치"
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "size": 20,
    "number": 0
  }
}
```

---

### 2. 스위치 상세 조회

#### Request
```http
GET /api/switches/{id}
```

#### Path Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| id | long | 스위치 ID |

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Gateron Yellow",
    "type": "LINEAR",
    "weight": 50,
    "manufacturer": "Gateron",
    "price": 300,
    "actuationForce": 50,
    "bottomOutForce": 62,
    "travelDistance": 4.0,
    "preTravel": 2.0,
    "springType": "Progressive",
    "stemMaterial": "POM",
    "housingMaterial": "Nylon",
    "soundProfile": "NORMAL",
    "isLubed": false,
    "description": "부드럽고 저렴한 리니어 스위치",
    "videos": [
      {
        "id": 1,
        "title": "Gateron Yellow 타건음 테스트",
        "url": "https://youtube.com/watch?v=...",
        "thumbnailUrl": "https://...",
        "channelName": "키보드덕후",
        "viewCount": 15000
      }
    ],
    "createdAt": "2025-01-01T00:00:00",
    "updatedAt": "2025-01-15T12:00:00"
  }
}
```

---

### 3. 스위치 검색 (Elasticsearch)

#### Request
```http
GET /api/switches/search?keyword=gateron&type=LINEAR&minPrice=200&maxPrice=500&page=0&size=10
```

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| keyword | string | X | 검색 키워드 (이름, 제조사) |
| type | string | X | 스위치 타입 (LINEAR, TACTILE, CLICKY) |
| minPrice | int | X | 최소 가격 |
| maxPrice | int | X | 최대 가격 |
| minWeight | int | X | 최소 무게 |
| maxWeight | int | X | 최대 무게 |
| manufacturer | string | X | 제조사 |
| soundProfile | string | X | 소리 특성 (QUIET, NORMAL, LOUD) |
| page | int | X | 페이지 번호 (기본: 0) |
| size | int | X | 페이지 크기 (기본: 10) |

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Gateron Yellow",
        "type": "LINEAR",
        "weight": 50,
        "manufacturer": "Gateron",
        "price": 300,
        "score": 0.95
      }
    ],
    "totalElements": 15,
    "totalPages": 2,
    "page": 0,
    "size": 10
  }
}
```

---

### 4. 스위치 자동완성

#### Request
```http
GET /api/switches/autocomplete?query=gat
```

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| query | string | O | 검색어 (2자 이상) |

#### Response (200 OK)
```json
{
  "success": true,
  "data": [
    "Gateron Yellow",
    "Gateron Red",
    "Gateron Black",
    "Gateron Brown",
    "Gateron Blue"
  ]
}
```

---

### 5. 인기 스위치 Top 10

#### Request
```http
GET /api/switches/popular
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Gateron Yellow",
      "type": "LINEAR",
      "price": 300,
      "viewCount": 5000
    }
  ]
}
```

---

## 🔧 Plate API (보강판)

### 1. 보강판 목록 조회

#### Request
```http
GET /api/plates?page=0&size=20
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Aluminum Plate",
        "material": "ALUMINUM",
        "type": "FULL",
        "price": 25000,
        "compatibility": "60%, 65%",
        "flexibility": "RIGID",
        "soundProfile": "CLACKY"
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "page": 0
  }
}
```

---

### 2. 보강판 상세 조회

#### Request
```http
GET /api/plates/{id}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Aluminum Plate",
    "material": "ALUMINUM",
    "type": "FULL",
    "price": 25000,
    "compatibility": "60%, 65%",
    "flexibility": "RIGID",
    "soundProfile": "CLACKY",
    "description": "알루미늄 풀 플레이트"
  }
}
```

---

### 3. 보강판 검색

#### Request
```http
GET /api/plates/search?material=ALUMINUM&type=FULL
```

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| material | string | X | 재질 |
| type | string | X | 타입 |
| minPrice | int | X | 최소 가격 |
| maxPrice | int | X | 최대 가격 |

---

## 🎬 Video API (유튜브 영상)

### 1. 영상 목록 조회

#### Request
```http
GET /api/videos?page=0&size=20
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Gateron Yellow 타건음",
        "url": "https://youtube.com/watch?v=...",
        "thumbnailUrl": "https://...",
        "channelName": "키보드덕후",
        "viewCount": 15000,
        "publishedAt": "2025-01-01T00:00:00"
      }
    ]
  }
}
```

---

### 2. 스위치별 영상 조회

#### Request
```http
GET /api/videos/switch/{switchId}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "Gateron Yellow 타건음 테스트",
      "url": "https://youtube.com/watch?v=...",
      "thumbnailUrl": "https://...",
      "channelName": "키보드덕후",
      "viewCount": 15000,
      "relevanceScore": 95
    }
  ]
}
```

---

## 🎁 Recommendation API (추천)

### 1. 취향 기반 추천

#### Request
```http
POST /api/recommendations
Content-Type: application/json
```

#### Request Body
```json
{
  "preferredType": "LINEAR",
  "weightRange": {
    "min": 45,
    "max": 55
  },
  "priceRange": {
    "min": 200,
    "max": 500
  },
  "soundProfile": "QUIET",
  "isLubed": false
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "recommendations": [
      {
        "switch": {
          "id": 1,
          "name": "Gateron Yellow",
          "type": "LINEAR",
          "weight": 50,
          "price": 300
        },
        "matchScore": 95,
        "reason": "무게와 가격이 선호도와 일치",
        "videos": [
          {
            "id": 1,
            "title": "타건음 테스트",
            "url": "https://..."
          }
        ]
      }
    ]
  }
}
```

---

### 2. 인기 추천

#### Request
```http
GET /api/recommendations/popular?type=LINEAR
```

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| type | string | X | 스위치 타입 |

#### Response (200 OK)
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Gateron Yellow",
      "type": "LINEAR",
      "price": 300,
      "recommendCount": 1500
    }
  ]
}
```

---

## 🔄 Sync API (동기화) - 관리자

### 1. 동기화 상태 조회

#### Request
```http
GET /api/sync/status
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "lastSyncAt": "2025-01-15T12:00:00",
    "status": "SUCCESS",
    "switchCount": 100,
    "plateCount": 50,
    "videoCount": 500,
    "nextSyncAt": "2025-01-15T13:00:00"
  }
}
```

---

### 2. 수동 동기화 트리거 (관리자 전용)

#### Request
```http
POST /api/admin/sync
Authorization: Bearer {token}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "message": "동기화가 시작되었습니다.",
    "jobId": "sync-20250115-120000"
  }
}
```

---

### 3. 동기화 로그 조회 (관리자 전용)

#### Request
```http
GET /api/admin/sync/logs?page=0&size=20
Authorization: Bearer {token}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "startedAt": "2025-01-15T12:00:00",
        "completedAt": "2025-01-15T12:05:00",
        "status": "SUCCESS",
        "insertedCount": 5,
        "updatedCount": 10,
        "errorMessage": null
      }
    ]
  }
}
```

---

## 📊 Stats API (통계) - 관리자

### 1. 전체 통계

#### Request
```http
GET /api/admin/stats
Authorization: Bearer {token}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "totalSwitches": 100,
    "totalPlates": 50,
    "totalVideos": 500,
    "totalUsers": 1000,
    "popularKeywords": [
      { "keyword": "gateron", "count": 500 },
      { "keyword": "cherry", "count": 300 }
    ]
  }
}
```

---

## 👤 User API (사용자) - Phase 3

### 1. 회원가입

#### Request
```http
POST /api/auth/register
Content-Type: application/json
```

#### Request Body
```json
{
  "email": "user@example.com",
  "password": "password123!",
  "nickname": "키보드덕후"
}
```

#### Response (201 Created)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "키보드덕후",
    "createdAt": "2025-01-15T12:00:00"
  }
}
```

---

### 2. 로그인

#### Request
```http
POST /api/auth/login
Content-Type: application/json
```

#### Request Body
```json
{
  "email": "user@example.com",
  "password": "password123!"
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "nickname": "키보드덕후"
    }
  }
}
```

---

### 3. 내 정보 조회

#### Request
```http
GET /api/users/me
Authorization: Bearer {token}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "키보드덕후",
    "profileImage": "https://...",
    "createdAt": "2025-01-01T00:00:00"
  }
}
```

---

## ❤️ Wishlist API (위시리스트) - Phase 3

### 1. 위시리스트 조회

#### Request
```http
GET /api/users/me/wishlist
Authorization: Bearer {token}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "switch": {
        "id": 1,
        "name": "Gateron Yellow",
        "type": "LINEAR",
        "price": 300
      },
      "createdAt": "2025-01-15T12:00:00"
    }
  ]
}
```

---

### 2. 위시리스트 추가

#### Request
```http
POST /api/users/me/wishlist
Authorization: Bearer {token}
Content-Type: application/json
```

#### Request Body
```json
{
  "switchId": 1
}
```

#### Response (201 Created)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "switchId": 1,
    "createdAt": "2025-01-15T12:00:00"
  }
}
```

---

### 3. 위시리스트 삭제

#### Request
```http
DELETE /api/users/me/wishlist/{switchId}
Authorization: Bearer {token}
```

#### Response (204 No Content)

---

## ⭐ Review API (리뷰) - Phase 3

### 1. 리뷰 목록 조회

#### Request
```http
GET /api/switches/{switchId}/reviews?page=0&size=10
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "user": {
          "id": 1,
          "nickname": "키보드덕후"
        },
        "rating": 5,
        "content": "정말 좋은 스위치입니다!",
        "imageUrl": "https://...",
        "likes": 15,
        "createdAt": "2025-01-15T12:00:00"
      }
    ],
    "averageRating": 4.5,
    "totalReviews": 100
  }
}
```

---

### 2. 리뷰 작성

#### Request
```http
POST /api/switches/{switchId}/reviews
Authorization: Bearer {token}
Content-Type: application/json
```

#### Request Body
```json
{
  "rating": 5,
  "content": "정말 좋은 스위치입니다!",
  "imageUrl": "https://..."
}
```

#### Response (201 Created)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "rating": 5,
    "content": "정말 좋은 스위치입니다!",
    "createdAt": "2025-01-15T12:00:00"
  }
}
```

---

## 🔒 인증 & 권한

### JWT 토큰 사용
- **Header**: `Authorization: Bearer {token}`
- **토큰 만료 시간**:
  - Access Token: 1시간
  - Refresh Token: 7일

### 권한 레벨
- **USER**: 일반 사용자
- **ADMIN**: 관리자 (동기화, 통계 API 접근 가능)

---

## 📝 에러 코드

| 코드 | 설명 |
|------|------|
| `SWITCH_NOT_FOUND` | 스위치를 찾을 수 없음 |
| `PLATE_NOT_FOUND` | 보강판을 찾을 수 없음 |
| `VIDEO_NOT_FOUND` | 영상을 찾을 수 없음 |
| `INVALID_REQUEST` | 잘못된 요청 |
| `UNAUTHORIZED` | 인증 실패 |
| `FORBIDDEN` | 권한 없음 |
| `SYNC_FAILED` | 동기화 실패 |
| `ELASTICSEARCH_ERROR` | 검색 엔진 오류 |

---

## 🚀 API 버전 관리

### 버전 표기
```
/api/v1/switches
/api/v2/switches
```

현재는 v1만 사용, 추후 v2 추가 예정

---

## 📊 Rate Limiting

### 제한 정책
- **일반 사용자**: 100 req/min
- **인증된 사용자**: 300 req/min
- **관리자**: 1000 req/min

### 응답 헤더
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1642262400
```

---

## 🧪 테스트 도구

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### Postman Collection
`docs/postman/ThockPick.postman_collection.json` (별도 제공 예정)

---

## 📚 추가 문서

- [ARCHITECTURE.md](./ARCHITECTURE.md) - 시스템 아키텍처
- [DATABASE_DESIGN.md](./DATABASE_DESIGN.md) - 데이터베이스 설계
- [TECH_STACK.md](./TECH_STACK.md) - 기술 스택
- [DEVELOPMENT_ROADMAP.md](./DEVELOPMENT_ROADMAP.md) - 개발 로드맵
