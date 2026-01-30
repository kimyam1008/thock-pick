# ThockPick 시스템 아키텍처

## 📋 프로젝트 개요

ThockPick은 Google Sheets를 기반으로 한 키보드 스위치 검색 서비스입니다.

사용자의 취향(리니어/택타일 등)을 입력받아 적합한 스위치를 검색할 수 있으며, Elasticsearch를 활용한 한글 별명 검색을 지원합니다.

## 🏗️ 전체 아키텍처

```
┌─────────────────┐
│  Google Sheets  │  ← 스위치 정보 입력 및 관리
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│                    Spring Boot Server                    │
│                                                           │
│  ┌──────────────────────────────────────────────────┐   │
│  │         1. Data Sync Layer (동기화)               │   │
│  │  - Google Sheets API 호출                         │   │
│  │  - 데이터 동기화 (수동 트리거)                     │   │
│  └──────────────┬───────────────────────────────────┘   │
│                 │                                         │
│                 ▼                                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │      2. Persistence Layer (영속성 저장)           │   │
│  │  - JPA + MariaDB                                  │   │
│  │  - 스위치 정보 저장                                │   │
│  └──────────────┬───────────────────────────────────┘   │
│                 │                                         │
│                 ▼                                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │  3. Search Engine (검색 엔진)                     │   │
│  │  - Elasticsearch + Nori Analyzer                 │   │
│  │  - 한글 별명 검색                                  │   │
│  │  - 전문 검색 및 필터링                             │   │
│  └──────────────┬───────────────────────────────────┘   │
│                 │                                         │
│                 ▼                                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │      4. Service Layer (비즈니스 로직)             │   │
│  │  - SwitchService: 검색 및 조회                    │   │
│  │  - SwitchSyncService: Google Sheets 동기화        │   │
│  │  - SwitchNicknameService: 별명 매핑               │   │
│  └──────────────┬───────────────────────────────────┘   │
│                 │                                         │
│                 ▼                                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │      5. Presentation Layer (컨트롤러)             │   │
│  │  - GET  / (홈페이지)                              │   │
│  │  - GET  /switches (목록)                          │   │
│  │  - GET  /switches/api (Ajax API)                 │   │
│  │  - GET  /switches/{id} (상세)                     │   │
│  │  - POST /api/sync/switches (동기화)               │   │
│  └──────────────┬───────────────────────────────────┘   │
│                 │                                         │
│                 ▼                                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │      6. View Layer (Thymeleaf)                    │   │
│  │  - 서버 사이드 렌더링                              │   │
│  │  - Bootstrap 5 UI                                 │   │
│  └──────────────────────────────────────────────────┘   │
└───────────────────────────┬─────────────────────────────┘
                            │
                            ▼
                    ┌───────────────┐
                    │   사용자       │
                    │  (웹 브라우저)  │
                    └───────────────┘
```

## 🔄 데이터 플로우

### 1. 데이터 동기화 플로우
```
Google Sheets → API Call → Validation → MariaDB 저장
                                            ↓
                                   SwitchNicknameService (별명 조회)
                                            ↓
                                   Elasticsearch 인덱싱
```

### 2. 검색 플로우
```
사용자 요청 → Controller → Service
                             ↓
                 ┌───────────┴──────────┐
                 ▼                      ▼
            MariaDB 조회         Elasticsearch 검색
         (type, manufacturer)    (keyword, 별명)
                 ↓                      ↓
                 └──────────┬───────────┘
                            ▼
                      Response (JSON/HTML)
```

### 3. 페이지 렌더링 플로우
```
GET /switches → Controller → Service → MariaDB
                     ↓
                 Model 생성
                     ↓
           Thymeleaf Template
                     ↓
                HTML Response
```

## 📦 레이어별 상세 설명

### Domain Layer (도메인 레이어)
- **역할**: 핵심 비즈니스 엔티티 및 리포지토리 인터페이스 정의
- **기술**: JPA Entity, Repository Interface
- **주요 엔티티**:
  - `Switch`: 스위치 정보 (이름, 타입, 무게, 제조사, 가격 등)
  - `SwitchType`: 스위치 타입 Enum (LINEAR, TACTILE, CLICKY)
  - `SwitchRepository`: 스위치 조회 인터페이스

### Application Layer (애플리케이션 레이어)
- **역할**: 비즈니스 로직 처리
- **주요 서비스**:
  - `SwitchService`: 스위치 검색, 조회, 통계
  - `SwitchNicknameService`: 한글 별명 매핑 관리

### Infrastructure Layer (인프라 레이어)
- **역할**: 외부 시스템 연동 및 기술 구현
- **주요 컴포넌트**:
  - **Google Sheets 동기화**:
    - `GoogleSheetsService`: Sheets API 호출
    - `SwitchSyncService`: 데이터 동기화 로직
  - **Elasticsearch 검색**:
    - `SwitchDocument`: ES 인덱스 문서 모델
    - `SwitchSearchRepository`: ES 검색 리포지토리

### Presentation Layer (프레젠테이션 레이어)
- **역할**: 사용자 요청 처리 및 응답
- **기술**: Spring MVC + Thymeleaf
- **주요 컨트롤러**:
  - `HomeController`: 홈 페이지 (통계 표시)
  - `SwitchViewController`: 스위치 목록/상세 페이지
  - `SwitchSyncController`: 동기화 API

## 🎯 핵심 기능

### 1. 스위치 검색
- **키워드 검색**: Elasticsearch를 활용한 한글 별명 검색 (예: "적축", "청축")
- **필터링**: 타입(LINEAR, TACTILE, CLICKY), 제조사
- **정렬**: 이름 순
- **페이지네이션**: 12개씩 페이지 단위 조회

### 2. Google Sheets 동기화
- **수동 트리거**: POST /api/sync/switches 호출
- **데이터 플로우**: Google Sheets → MariaDB → Elasticsearch
- **별명 자동 매핑**: switch_nicknames.json 기반

### 3. 통계 및 대시보드
- **홈 화면**: 전체 스위치 개수 및 타입별 통계 표시
- **타입별 필터링**: LINEAR, TACTILE, CLICKY 별로 카운트 제공

## 🔒 비기능 요구사항

### 성능
- **Elasticsearch 활용**: 빠른 전문 검색
- **JPA 2차 캐시**: 엔티티 캐싱 (예정)
- **페이지네이션**: 대용량 데이터 효율적 처리

### 안정성
- **데이터 원본 분리**: Google Sheets API 장애 시에도 MariaDB로 서비스 유지
- **Elasticsearch 장애 대응**: ES 실패 시 로그만 남기고 서비스 계속
- **환경 변수 관리**: Spring Dotenv로 민감 정보 분리

### 보안
- **HTTPS 적용**: Nginx 프록시로 SSL 종단
- **입력 데이터 검증**: Spring Validation 적용
- **환경 변수 보호**: .env 파일 gitignore 처리

## 🚀 향후 확장 가능성

### Phase 2: 성능 최적화
- **Redis 캐싱**: 인기 스위치, 검색 결과 캐싱
- **실시간 동기화**: JPA Event Listener 활용
- **CDN 도입**: 정적 자산 배포

### Phase 3: 기능 확장
- **유튜브 영상 연동**: 스위치별 타건음 영상 추천
- **보강판 데이터**: 보강판 정보 추가
- **사용자 리뷰**: 커뮤니티 기반 평가 시스템

### Phase 4: 사용자 기능
- **회원 시스템**: 로그인/회원가입
- **위시리스트**: 관심 스위치 저장
- **커스텀 조합**: 스위치 + 보강판 + 키캡 조합 공유

### Phase 5: 파트너십
- **쇼핑몰 연동**: 구매 링크 제공
- **가격 비교**: 여러 쇼핑몰 가격 비교
- **재고 알림**: 품절/입고 알림 서비스
