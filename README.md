# ThockPick ⌨️

Google Sheets 기반의 키보드 부품 검색 서비스

![ThockPick 홈페이지](docs/images/homepage.png)

## 📋 프로젝트 소개

사용자의 취향(리니어/택타일 등)을 입력받아 적합한 키보드 스위치를 검색할 수 있는 서비스입니다.

### 📝 개발 회고
- [ThockPick 개발 회고: 키보드 덕후의 문제 해결기](https://yoondeveloper.tistory.com/41)

## 🛠️ 기술 스택

- **Backend**: Spring Boot 3.5.9, Java 17
- **ORM**: Spring Data JPA
- **Database**: MariaDB
- **Template Engine**: Thymeleaf
- **CSS Framework**: Bootstrap 5
- **External API**: Google Sheets API
- **Search Engine**: Elasticsearch
- **Web Server**: Nginx (HTTPS, Reverse Proxy)
- **Environment Config**: Spring Dotenv
- **Cache** (예정): Redis

## 🏗️ 아키텍처

### 레이어 구조
```
Presentation Layer (Controller)
        ↓
Application Layer (Service)
        ↓
Domain Layer (Entity, Repository Interface)
        ↓
Infrastructure Layer (Repository Impl, External API)
```

### 데이터 흐름
```
Google Sheets API → Sync Service → MariaDB ↔ Elasticsearch
                                      ↓
                                  Controller → View (Thymeleaf)
```

### 배포 구조
```
Client → Nginx (HTTPS) → Spring Boot → MariaDB
                              ↓
                         Elasticsearch
```

## 📚 문서

- [ARCHITECTURE.md](docs/ARCHITECTURE.md) - 시스템 아키텍처 상세
- [API_DESIGN.md](docs/API_DESIGN.md) - API 명세
- [ELASTICSEARCH_INTEGRATION.md](docs/ELASTICSEARCH_INTEGRATION.md) - Elasticsearch 통합 가이드
- [TECH_STACK.md](docs/TECH_STACK.md) - 기술 스택 설명
- [DATABASE_DESIGN.md](docs/DATABASE_DESIGN.md) - 데이터베이스 설계
- [DEVELOPMENT_ROADMAP.md](docs/DEVELOPMENT_ROADMAP.md) - 개발 로드맵

## 🚀 빠른 시작

### 요구사항
- Java 17 이상
- Gradle 8.14.3

### 실행 방법
```bash
# 프로젝트 클론
git clone https://github.com/yourusername/thock-pick-server.git
cd thock-pick-server

# 빌드 & 실행
./gradlew bootRun
```

### 접속
- 애플리케이션: https://thockpick.com/

## 🤝 기여

1인 개발 프로젝트이지만 피드백과 제안은 언제나 환영합니다!

## 📄 라이선스

MIT License
