# 개발 로드맵 (Development Roadmap)

## 🎯 프로젝트 목표

**ThockPick**: 키보드 커뮤니티를 위한 부품 검색 및 타건음 추천 플랫폼 구축

---

## 📅 전체 개발 일정 (6개월)

```
Month 1-2: Phase 1 (MVP)
Month 3-4: Phase 2 (핵심 기능 고도화)
Month 5-6: Phase 3 (확장 기능)
```

---

## 🚀 Phase 1: MVP (최소 기능 제품) - 2개월

### 목표
Google Sheets 기반 기본 검색 및 추천 기능 구현

### Sprint 1.1: 프로젝트 초기 설정 (1주)

#### Tasks
- [x] Spring Boot 프로젝트 생성
- [ ] 기본 의존성 설정 (JPA, Web, Lombok)
- [ ] Git 저장소 설정
- [ ] 개발 환경 구성 (IDE, DB 도구)
- [ ] README.md 작성
- [ ] 기술 문서화

#### Deliverables
- ✅ 실행 가능한 Spring Boot 애플리케이션
- ✅ docs/ 폴더 내 아키텍처 문서

---

### Sprint 1.2: Database & Domain 설계 (1주)

#### Tasks
- [ ] ERD 설계
- [ ] JPA Entity 클래스 작성
  - `Switch` (스위치)
  - `Plate` (보강판)
  - `Video` (유튜브 영상)
  - `SwitchVideo` (연관 관계)
- [ ] Repository 인터페이스 작성
- [ ] H2 Database 연동 및 테스트
- [ ] DDL 자동 생성 확인

#### Deliverables
- 📦 Entity 클래스 4개
- 📦 Repository 인터페이스 4개
- 📄 데이터베이스 초기 스키마

---

### Sprint 1.3: Google Sheets 연동 (1주)

#### Tasks
- [ ] Google Sheets API 인증 설정
  - Service Account 생성
  - credentials.json 발급
- [ ] Google Sheets 클라이언트 구현
- [ ] 스프레드시트 읽기 기능 구현
- [ ] DTO 매핑 (Sheets Row → Entity)
- [ ] 에러 핸들링 (API 한도, 네트워크 오류)

#### Deliverables
- 🔗 Google Sheets 읽기 가능
- 📊 샘플 데이터 로딩 성공

---

### Sprint 1.4: 동기화 스케줄러 (1주)

#### Tasks
- [ ] Spring Scheduler 설정
- [ ] SyncService 구현
  - 주기적 동기화 (1시간마다)
  - 증분 업데이트 로직
- [ ] 동기화 로그 기록
- [ ] 동기화 상태 엔드포인트 (`GET /api/sync/status`)

#### Deliverables
- ⏰ 1시간마다 자동 동기화
- 📈 동기화 상태 조회 API

---

### Sprint 1.5: 기본 CRUD API (2주)

#### Tasks
- [ ] SwitchController 구현
  - `GET /api/switches` - 전체 조회
  - `GET /api/switches/{id}` - 상세 조회
  - `GET /api/switches/search?keyword={keyword}` - 기본 검색
- [ ] PlateController 구현
  - `GET /api/plates`
  - `GET /api/plates/{id}`
- [ ] VideoController 구현
  - `GET /api/videos`
  - `GET /api/videos/switch/{switchId}` - 스위치별 영상 조회
- [ ] 예외 처리 (ControllerAdvice)
- [ ] 기본 필터링 (타입, 무게, 가격)

#### Deliverables
- 🎯 REST API 10개 이상
- 📝 API 문서 (Swagger/Postman)

---

### Sprint 1.6: 테스트 & 배포 준비 (1주)

#### Tasks
- [ ] 단위 테스트 작성 (Service Layer)
- [ ] 통합 테스트 (Repository Layer)
- [ ] API 테스트 (Controller Layer)
- [ ] application.yml 환경별 분리 (dev/prod)
- [ ] MySQL 연동 (로컬)
- [ ] 배포 스크립트 작성

#### Deliverables
- ✅ 테스트 커버리지 70% 이상
- 🚢 배포 가능한 패키지 (.jar)

---

## 🔥 Phase 2: 핵심 기능 고도화 - 2개월

### 목표
Elasticsearch, Redis 도입으로 검색 및 성능 최적화

### Sprint 2.1: Elasticsearch 연동 (2주)

#### Tasks
- [ ] Elasticsearch 설치 및 설정
- [ ] Spring Data Elasticsearch 의존성 추가
- [ ] Elasticsearch Document 정의
- [ ] 인덱싱 로직 구현
  - RDB → Elasticsearch 데이터 동기화
- [ ] 검색 API 고도화
  - 전문 검색 (Full-text search)
  - 필터링 (복합 조건)
  - 자동완성
- [ ] 한글 형태소 분석기 (nori) 적용

#### Deliverables
- 🔍 고성능 검색 API
- 🚀 검색 속도 < 100ms

---

### Sprint 2.2: Redis 캐싱 (1주)

#### Tasks
- [ ] Redis 설치 및 설정
- [ ] Spring Data Redis 의존성 추가
- [ ] 캐싱 전략 구현
  - @Cacheable, @CacheEvict 적용
  - TTL 설정
- [ ] 캐싱 대상
  - 인기 스위치 Top 10
  - 스위치 상세 정보
  - 검색 결과 (단기 캐싱)
- [ ] 캐시 무효화 로직 (동기화 시)

#### Deliverables
- ⚡ API 응답 속도 50% 개선
- 📊 캐시 히트율 모니터링

---

### Sprint 2.3: 추천 알고리즘 (2주)

#### Tasks
- [ ] 사용자 취향 DTO 설계
  - 선호 타입 (리니어/택타일/클릭키)
  - 무게 범위
  - 가격대
  - 소리 특성 (조용함/보통/큼)
- [ ] 매칭 알고리즘 구현
  - 점수 기반 추천
  - 가중치 적용
- [ ] 추천 API
  - `POST /api/recommendations` - 취향 기반 추천
  - `GET /api/recommendations/popular` - 인기 추천
- [ ] 유튜브 영상 함께 반환

#### Deliverables
- 🎁 개인화 추천 API
- 📹 스위치 + 영상 통합 응답

---

### Sprint 2.4: 관리자 기능 (1주)

#### Tasks
- [ ] 수동 동기화 트리거 API
  - `POST /api/admin/sync` - 즉시 동기화
- [ ] 통계 API
  - `GET /api/admin/stats` - 데이터 통계
  - 스위치 개수, 영상 개수, 인기 검색어
- [ ] 헬스 체크 API
  - `GET /actuator/health` - 시스템 상태

#### Deliverables
- 🔧 관리자 도구 API
- 📊 시스템 모니터링 대시보드 준비

---

### Sprint 2.5: 성능 최적화 & 리팩토링 (1주)

#### Tasks
- [ ] N+1 쿼리 해결 (Fetch Join)
- [ ] 인덱스 최적화 (MySQL)
- [ ] API 응답 속도 측정 및 개선
- [ ] 코드 리팩토링
  - 중복 코드 제거
  - 패턴 적용 (Strategy, Factory 등)
- [ ] 로깅 체계 정립

#### Deliverables
- 🚀 평균 API 응답 < 200ms
- 📚 클린 코드

---

## 🌟 Phase 3: 확장 기능 - 2개월

### 목표
사용자 기능 추가 및 커뮤니티 활성화

### Sprint 3.1: 사용자 인증/인가 (2주)

#### Tasks
- [ ] Spring Security 설정
- [ ] JWT 기반 인증 구현
- [ ] 회원가입/로그인 API
- [ ] 사용자 정보 관리
- [ ] 권한 관리 (USER, ADMIN)

#### Deliverables
- 🔐 보안 강화
- 👤 사용자 관리 시스템

---

### Sprint 3.2: 위시리스트 & 북마크 (1주)

#### Tasks
- [ ] Wishlist 엔티티 설계
- [ ] 위시리스트 CRUD API
  - `POST /api/users/me/wishlist`
  - `GET /api/users/me/wishlist`
  - `DELETE /api/users/me/wishlist/{switchId}`
- [ ] 북마크한 스위치 개수 표시

#### Deliverables
- ❤️ 사용자별 위시리스트 기능

---

### Sprint 3.3: 리뷰 & 평점 (2주)

#### Tasks
- [ ] Review 엔티티 설계
- [ ] 리뷰 작성/조회/수정/삭제 API
- [ ] 평점 집계 (평균)
- [ ] 리뷰 정렬 (최신순, 추천순)
- [ ] 이미지 업로드 (AWS S3 or Cloudinary)

#### Deliverables
- ⭐ 스위치 리뷰 시스템
- 📸 이미지 첨부 기능

---

### Sprint 3.4: 조합 공유 기능 (2주)

#### Tasks
- [ ] Build 엔티티 설계
  - 스위치 + 보강판 + 키캡 조합
- [ ] 조합 공유 API
  - `POST /api/builds` - 조합 등록
  - `GET /api/builds` - 조합 목록
  - `GET /api/builds/{id}` - 조합 상세
- [ ] 좋아요/댓글 기능
- [ ] 인기 조합 TOP 10

#### Deliverables
- 🛠️ 사용자 커스텀 빌드 공유
- 💬 커뮤니티 활성화

---

### Sprint 3.5: 알림 & 구독 (1주)

#### Tasks
- [ ] 신규 스위치 알림
- [ ] 가격 변동 알림
- [ ] 좋아하는 제조사 신제품 알림
- [ ] 이메일/푸시 알림 (Firebase Cloud Messaging)

#### Deliverables
- 🔔 실시간 알림 시스템

---

### Sprint 3.6: 최종 테스트 & 배포 (1주)

#### Tasks
- [ ] 전체 기능 통합 테스트
- [ ] 부하 테스트 (JMeter)
- [ ] 보안 점검
- [ ] Docker 이미지 생성
- [ ] AWS/GCP 배포
- [ ] 도메인 연결
- [ ] HTTPS 설정

#### Deliverables
- 🌐 운영 서버 오픈
- 📱 베타 테스트 시작

---

## 📊 마일스톤 (Milestones)

| 마일스톤 | 완료 일정 | 주요 기능 |
|---------|----------|----------|
| M1: MVP | Month 2 | 기본 검색, Google Sheets 동기화 |
| M2: 고도화 | Month 4 | Elasticsearch, Redis, 추천 알고리즘 |
| M3: 커뮤니티 | Month 6 | 사용자 인증, 리뷰, 조합 공유 |

---

## 🎯 우선순위 (Priority)

### High Priority (반드시 구현)
- ✅ Google Sheets 동기화
- ✅ 스위치/보강판 기본 CRUD
- ✅ 검색 API (Elasticsearch)
- ✅ 캐싱 (Redis)
- ✅ 유튜브 추천

### Medium Priority (가능하면 구현)
- 🟡 사용자 인증/인가
- 🟡 위시리스트
- 🟡 리뷰 & 평점

### Low Priority (추후 고려)
- 🔵 조합 공유
- 🔵 알림 시스템
- 🔵 AI 추천

---

## 🛠️ 기술 부채 관리

### 정기 리팩토링 (매 Phase 종료 시)
- 코드 리뷰
- 테스트 커버리지 개선
- 문서 업데이트
- 성능 최적화

### 모니터링 항목
- API 응답 시간
- 에러율
- 캐시 히트율
- 동기화 성공률

---

## 📚 학습 계획

### Phase 1
- Spring Boot, JPA 기초
- Google Sheets API

### Phase 2
- Elasticsearch 쿼리 최적화
- Redis 캐싱 전략

### Phase 3
- Spring Security
- AWS 배포 (EC2, RDS, S3)

---

## 🚦 위험 요소 & 대응 방안

### 위험 요소
1. **Google Sheets API 한도 초과**
   - 대응: 호출 빈도 조절, Batch 요청

2. **Elasticsearch 학습 곡선**
   - 대응: 단계적 도입, 초기엔 RDB 검색 병행

3. **N+1 쿼리 문제**
   - 대응: Fetch Join, EntityGraph 적극 활용

4. **배포 경험 부족**
   - 대응: Docker 사용, PaaS (Heroku, Railway) 고려

---

## ✅ 체크리스트 (Phase 1 완료 기준)

- [ ] Google Sheets 자동 동기화 동작
- [ ] 스위치 검색 API 정상 동작
- [ ] 유튜브 영상 추천 API 정상 동작
- [ ] 테스트 커버리지 70% 이상
- [ ] API 문서화 완료
- [ ] 로컬 환경에서 정상 실행

---

## 🎉 다음 단계

Phase 1 완료 후:
1. 베타 테스터 모집 (키보드 커뮤니티)
2. 피드백 수렴
3. Phase 2 우선순위 조정
4. 기술 블로그 작성 (회고)
