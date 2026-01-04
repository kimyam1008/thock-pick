# 2026-01-04 Google Sheets API 데이터 동기화 구현

## 변경 사항

### 신규 추가
- `infrastructure/sync/googlesheets` 패키지 생성
  - `GoogleSheetsService`: Google Sheets API 연동 및 데이터 읽기
  - `SwitchSyncService`: DB 동기화 로직 처리
  - `SwitchSyncController`: 동기화 REST API 엔드포인트
  - `SwitchSheetRow`: 시트 데이터 매핑 DTO

### 주요 기능
- 스프레드시트의 모든 탭(시트) 순회하며 데이터 수집
- 동적 헤더 매핑으로 컬럼 순서 무관하게 파싱
- 행 번호 + 카테고리 기반 중복 방지
- 개별 행 처리로 에러 격리 (한 행 실패 시에도 다음 행 계속 처리)

### 기술 스택
- Google Sheets API v4
- API Key 기반 인증 (OAuth 불필요)

### 엔드포인트
```
POST /api/sync/switches
```

## 설정 추가
- `.env.example`에 Google Sheets 관련 환경변수 추가
  - `GOOGLE_SHEETS_SPREADSHEET_ID`
  - `GOOGLE_SHEETS_RANGE`
  - `GOOGLE_SHEETS_API_KEY`

## 엔티티 변경
- `Switch`: `category`, `googleSheetsRow` 필드 추가
- `SwitchRepository`: `findByGoogleSheetsRowAndCategory`, `findByName` 메서드 추가

## 개선점
- 복합 데이터(키압, 소재) 파싱 로직 구현
- 시트 탭 이름을 카테고리로 활용하여 데이터 분류
- 필수값 검증으로 DB 에러 방지
