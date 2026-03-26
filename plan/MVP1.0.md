# CinemaNote MVP 1.0 구현 정리

## 개요

영화/TV 시청 기록을 남기는 개인 아카이브 서비스의 첫 번째 버전.
핵심 CRUD와 외부 API 연동, 기본 인증을 구현하고 각 도메인에 테스트를 작성하는 것을 목표로 했다.

---

## 구현 도메인

### 인증 (Auth)

- 회원가입 / 로그인 / 로그아웃
- Spring Security + 서블릿 세션 기반 인증 (`SessionCreationPolicy.IF_REQUIRED`)
- `@AuthUser` 커스텀 어노테이션 + `AuthUserArgumentResolver`로 컨트롤러에서 인증 사용자 편리하게 주입
- `BCryptPasswordEncoder`로 비밀번호 암호화
- 미인증 접근 시 401, 권한 없는 접근 시 403을 JSON으로 응답

### 아카이브 (Archive)

- 영화 또는 TV 시청 기록 생성 / 단건 조회 / 목록 조회 / 수정 / 삭제
- TMDB ID와 콘텐츠 타입(`MOVIE` / `TV`), 제목, 포스터 경로, 평점, 리뷰 저장
- 자신의 아카이브만 조회/수정/삭제 가능 (타인의 아카이브 접근 시 403)
- 오프셋 기반 페이지네이션 (`PageResponse<T>` 공통 래퍼)

### TMDB 연동 (TMDB API)

- Spring WebFlux의 `WebClient`를 사용하여 TMDB External API 호출
- 영화 검색 / 영화 상세 조회
- TV 검색 / TV 상세 조회
- 장르 목록 조회

### 공유 링크 (ShareLink)

- 특정 아카이브에 대한 공유 링크(UUID) 생성
- 공유 링크로 아카이브 단건 조회 (비로그인 접근 허용)
- 공유 링크로 해당 사용자의 공개 아카이브 목록 조회

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| ORM | Spring Data JPA (Hibernate) |
| DB | MySQL 8 |
| 인증 | Spring Security (Session) |
| 외부 API | TMDB API (WebClient) |
| 빌드 | Gradle |
| 테스트 | JUnit 5, Mockito, MockMvc |

---

## 공통 구조

- `ApiResponse<T>` : 모든 API 응답을 `{ success, message, data }` 형태로 통일
- `PageResponse<T>` : 페이지네이션 응답 래퍼
- `BaseEntity` : `createdAt`, `updatedAt` JPA Auditing
- `GlobalExceptionHandler` : `@RestControllerAdvice`로 `CustomException` 및 기타 예외 일괄 처리
- `ErrorCode` : 에러 코드와 메시지를 Enum으로 관리

---

## 테스트 구성

각 도메인마다 레이어별 테스트를 작성했다.

| 테스트 대상 | 테스트 방식 |
|-------------|-------------|
| 서비스 레이어 | Mockito 단위 테스트 (Repository Mock) |
| 컨트롤러 레이어 | MockMvc + Service Mock |
| 엔티티 | 순수 단위 테스트 |
| 공통 (ApiResponse, PageResponse, CustomException) | 순수 단위 테스트 |

**테스트 대상 도메인**: auth, archive, shareLink, tmdb, user, global

---

## 브랜치 히스토리 (주요 작업)

- `#2` : 프로젝트 초기 세팅
- `#3` : 인증 도메인 구현
- `#7` : 아카이브 도메인 구현
- `#8` : TMDB API 연동
- `#10` : 공유 링크 도메인 구현
- `#15` : MVP 1.0 테스트 작성

---

## MVP 1.0에서 남긴 과제

- 세션이 WAS 메모리에 저장되어 수평 확장 불가 → **Redis 세션 전환 (2.0)**
- 아카이브가 모두 비공개 → **공개/비공개 설정 + 피드 (2.0)**
- TMDB API 매번 외부 호출 → **캐싱 전략 도입 (2.0)**
- 로컬 환경 의존적 실행 → **Docker Compose 도입 (2.0)**
- 수동 빌드/배포 → **CI/CD 구축 (2.0)**
