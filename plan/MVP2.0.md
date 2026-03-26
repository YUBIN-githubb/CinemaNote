# CinemaNote MVP 2.0 구현 계획

## 개요

MVP 1.0에서 완성한 핵심 기능(아카이브 CRUD, TMDB 연동, 공유 링크, 세션 기반 인증)을 기반으로,
MVP 2.0에서는 **아키텍처 고도화**와 **소셜 기능 확장**을 목표로 한다.

---

## 아키텍처 고도화

### 1. Redis 세션 저장소 전환

**현재 문제점**
- Spring Security의 기본 세션은 WAS 메모리에 저장되어 서버가 여러 대로 늘어나면 세션 불일치 발생
- 서버 재시작 시 로그인이 풀리는 문제

**목표**
- `spring-session-data-redis`를 도입하여 세션을 Redis에 저장
- 애플리케이션을 Stateless하게 만들어 수평 확장 가능한 구조로 전환
- TTL 기반 세션 만료 관리

**학습 포인트**
- Redis의 `SET`, `EXPIRE` 명령과 Spring Session의 동작 원리
- Stateful vs Stateless 아키텍처의 트레이드오프

---

### 2. 캐싱 전략 도입

**캐싱 대상 선정 기준**: 읽기 빈도가 높고, 변경이 드문 데이터

| 대상 | 전략 | TTL |
|------|------|-----|
| TMDB 영화/TV 상세 정보 | Look-aside Cache | 1시간 |
| 트렌딩 아카이브 목록 | Write-through / Scheduled Refresh | 10분 |
| 장르 목록 (TMDB) | 애플리케이션 시작 시 로드 | 24시간 |

**구현 방식**
- `@Cacheable`, `@CacheEvict` 등 Spring Cache Abstraction 활용
- 캐시 저장소: Redis (세션 저장소와 동일 인스턴스, 다른 DB 번호 또는 키 prefix 분리)

**학습 포인트**
- Cache Hit/Miss 비율 관찰
- Cache Stampede 문제와 해결책 (확률적 조기 만료 등)
- 캐시 무효화 타이밍 설계

---

### 3. Docker + Docker Compose 도입

**목표**
- 애플리케이션, MySQL, Redis를 컨테이너로 묶어 로컬/배포 환경 일치
- `docker-compose up` 한 번으로 전체 스택 실행

**구성**

```
docker-compose.yml
├── app       (Spring Boot)
├── db        (MySQL 8)
└── redis     (Redis 7)
```

**고려 사항**
- 환경 변수를 `.env` 파일로 분리하여 시크릿 관리
- 헬스체크 설정: app이 db/redis ready 이후 기동되도록 `depends_on` + `healthcheck`
- 볼륨 마운트로 MySQL 데이터 영속화

**학습 포인트**
- 멀티 스테이지 빌드로 이미지 크기 최적화
- 네트워크 브릿지와 서비스 디스커버리 (컨테이너 간 hostname 통신)

---

### 4. CI/CD 구축

**파이프라인 도구**: GitHub Actions

**파이프라인 구성**

```
PR 생성 시 (CI)
├── 빌드 (./gradlew build)
├── 테스트 (./gradlew test)
└── PR 코멘트로 결과 리포트

main 브랜치 머지 시 (CD)
├── Docker 이미지 빌드
├── 이미지 푸시 (Docker Hub 또는 GitHub Container Registry)
└── (선택) 배포 서버에 SSH 접속 후 docker compose pull & up
```

**학습 포인트**
- GitHub Actions의 workflow, job, step 구조
- Secrets 관리 (DB 비밀번호, Docker 토큰 등)
- 캐싱으로 Gradle 의존성 다운로드 시간 단축

---

## 기능 확장

### 5. 공개 피드 (Public Feed)

**요구사항**
- Archive에 `isPublic` 필드 추가 (기본값: `false`)
- 작성자가 아카이브를 공개/비공개로 전환 가능
- 공개된 아카이브를 모아보는 피드 API (`GET /api/feed`)

**정렬 옵션**
- 최신순: `createdAt` 기준 내림차순
- 인기순: `likeCount` 기준 내림차순 (좋아요 기능과 연동)

**페이지네이션**: 커서 기반 (Cursor-based)
- Offset 방식의 문제(데이터 추가 시 중복/누락)를 커서 방식으로 해결
- 최신순: 마지막으로 받은 아카이브의 `id`를 커서로 사용 (`WHERE id < :cursor`)
- 인기순: `(likeCount, id)` 복합 커서 사용 (동점 처리)

**학습 포인트**
- Offset vs Cursor 페이지네이션의 트레이드오프
- 커서 값을 Base64로 인코딩하는 관례와 그 이유
- 복합 정렬 기준에서의 커서 설계

---

### 6. 좋아요 (Like)

**요구사항**
- 로그인한 사용자가 공개 아카이브에 좋아요/취소 가능
- 동일 아카이브에 중복 좋아요 불가
- Archive에 `likeCount` 필드 추가 (인기순 정렬 기준)

**ERD 변경**
- `archive_likes` 테이블 추가: `(user_id, archive_id)` 복합 유니크 키

**동시성 이슈 처리**
- 여러 사용자가 동시에 같은 아카이브에 좋아요를 누를 때 `likeCount` 카운터 정합성 문제
- 해결책 비교:
  1. DB 레벨: `UPDATE archives SET like_count = like_count + 1 WHERE id = ?` (atomic 연산)
  2. 비관적 락 (`@Lock(LockModeType.PESSIMISTIC_WRITE)`)
  3. Redis `INCR` 명령으로 카운터 관리 후 주기적으로 DB 동기화

- MVP 2.0에서는 DB atomic 업데이트로 시작하고, 부하 테스트 후 Redis 카운터 방식 검토

**학습 포인트**
- Lost Update 문제와 atomic 연산
- 낙관적 락 vs 비관적 락의 선택 기준
- 좋아요 취소 시 `likeCount` 정합성 보장

---

### 7. 검색 (Search)

**요구사항**
- 공개 아카이브를 영화 제목, 장르, 작성자 닉네임으로 검색
- `GET /api/feed/search?q=...` 형태

**단계별 접근**

1. **Phase 1 (MVP 2.0)**: LIKE 쿼리
   - `WHERE title LIKE '%keyword%'`
   - 단순하지만 인덱스를 활용 못하는 문제를 직접 경험
   - 검색 결과도 커서 기반 페이지네이션 적용

2. **Phase 2 (향후)**: Full-Text Search
   - MySQL의 FULLTEXT 인덱스로 `MATCH ... AGAINST` 쿼리 사용
   - 한국어 지원 시 `ngram` 파서 필요

3. **Phase 3 (향후)**: Elasticsearch
   - 역색인(Inverted Index) 기반의 빠른 검색
   - Spring Data Elasticsearch 또는 직접 REST API 연동

**학습 포인트**
- LIKE `%word%` vs LIKE `word%`의 인덱스 활용 차이
- 검색 쿼리 플랜 분석 (`EXPLAIN`)

---

### 8. 인기/트렌딩 (Trending)

**요구사항**
- 최근 N일간 좋아요 수가 많은 공개 아카이브 목록 반환
- `GET /api/feed/trending?days=7` 형태

**쿼리 전략**
- `archive_likes` 테이블에서 `createdAt >= NOW() - INTERVAL N DAY` 조건으로 집계
- `GROUP BY archive_id ORDER BY COUNT(*) DESC`

**캐싱 연동**
- 트렌딩 결과는 실시간일 필요가 없으므로 Redis에 10분 캐싱
- `@Cacheable`과 `@Scheduled`를 조합하여 주기적으로 캐시 갱신하는 방식도 검토

**학습 포인트**
- 집계 쿼리 (`GROUP BY`, `COUNT`)의 성능과 인덱스 설계
- 결과가 달라지는 시점에 캐시를 어떻게 무효화할 것인가

---

## 구현 순서 (권장)

```
Phase 1: 인프라 기반 구축
  ├── Docker Compose 로컬 환경 구성 (MySQL + Redis)
  └── Redis 세션 전환

Phase 2: 핵심 기능
  ├── Archive 공개/비공개 설정
  ├── 좋아요 기능 (동시성 처리 포함)
  └── 공개 피드 API (커서 기반 페이지네이션)

Phase 3: 확장 기능
  ├── 검색 API (LIKE)
  ├── 트렌딩 API + 캐싱
  └── TMDB 캐싱 적용

Phase 4: 운영 기반
  └── GitHub Actions CI/CD 파이프라인
```

---

## 브랜치 전략 (참고)

```
main              ← 최종 배포 브랜치
└── #번호-feature-이름   ← 기능 단위 브랜치
```
