# CinemaNote MVP 1.0 — 테스트 계획

---

# Part 1. 백엔드 유닛 테스트

## 테스트 환경

**기술 스택:**
- JUnit 5 (`@ExtendWith(MockitoExtension.class)`) — 서비스 단위 테스트
- Mockito (`@Mock`, `@InjectMocks`) — 의존성 격리
- MockMvc (`@WebMvcTest`) — 컨트롤러 슬라이스 테스트
- StepVerifier (Reactor Test) — `Mono<T>` 반환 메서드 검증
- H2 인메모리 DB (`@DataJpaTest`) — 리포지토리 테스트

**추가 의존성 (build.gradle):**
```groovy
testImplementation 'io.projectreactor:reactor-test'
testImplementation 'org.springframework.security:spring-security-test'
```

---

## 1. 서비스 레이어

### 1-1. `AuthService`

> **전략:** `UserRepository`, `PasswordEncoder`, `AuthenticationManager`를 Mock으로 주입.

#### `signup(SignupRequest)`

| 테스트 케이스 | 입력 | 예상 결과 |
|---|---|---|
| 정상 회원가입 | 유효한 email/password/nickname | `User` 반환, `userRepository.save()` 1회 호출 |
| 이메일 중복 | `existsByEmail()` → true | `CustomException(EMAIL_ALREADY_EXISTS)` throw |
| 비밀번호 인코딩 확인 | 정상 입력 | `passwordEncoder.encode()` 1회 호출 |
| 저장된 유저의 role | 정상 입력 | `UserRole.USER`로 저장됨 |

```java
// test/auth/service/AuthServiceTest.java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @InjectMocks AuthService authService;

    @Test
    void signup_정상_회원가입() {
        // given
        SignupRequest req = new SignupRequest("a@b.com", "Password1!", "nick");
        given(userRepository.existsByEmail("a@b.com")).willReturn(false);
        given(passwordEncoder.encode("Password1!")).willReturn("encoded");
        User saved = User.of("a@b.com", "encoded", "nick", UserRole.USER);
        given(userRepository.save(any())).willReturn(saved);

        // when
        User result = authService.signup(req);

        // then
        assertThat(result.getEmail()).isEqualTo("a@b.com");
        assertThat(result.getUserRole()).isEqualTo(UserRole.USER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_이메일_중복_예외() {
        given(userRepository.existsByEmail(any())).willReturn(true);
        assertThatThrownBy(() -> authService.signup(new SignupRequest("a@b.com", "P@ssw0rd", "nick")))
            .isInstanceOf(CustomException.class)
            .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS));
    }
}
```

#### `signin(SigninRequest, HttpSession)`

| 테스트 케이스 | 입력 | 예상 결과 |
|---|---|---|
| 정상 로그인 | 유효한 email/password | `User` 반환, session에 userId 저장 |
| 비밀번호 불일치 | `authenticate()` → `BadCredentialsException` | `CustomException(INVALID_PASSWORD)` throw |
| 인증 실패 (기타) | `authenticate()` → `AuthenticationException` | `CustomException(AUTHENTICATION_FAILED)` throw |
| 세션 저장 확인 | 정상 로그인 | `session.setAttribute(SESSION_USER_KEY, userId)` 호출됨 |

---

### 1-2. `ArchiveService`

> **전략:** `ArchiveRepository`, `TmdbMovieService`, `TmdbTvService`를 Mock으로 주입.
> Reactive 메서드는 `StepVerifier`로 검증.

#### `createArchive(User, ArchiveCreateRequest)`

| 테스트 케이스 | 입력 | 예상 결과 |
|---|---|---|
| 정상 생성 (MOVIE) | contentType=MOVIE, 유효한 tmdbId | `Mono<ArchiveResponse>` 반환, `save()` 호출 |
| 정상 생성 (TV) | contentType=TV, 유효한 tmdbId | `TmdbTvService.getTvDetail()` 호출 |
| 중복 아카이브 | `existsByUserAndTmdbIdAndContentType()` → true | `Mono.error(CustomException(ARCHIVE_ALREADY_EXISTS))` |
| TMDB API 오류 | `getMovieDetail()` → error Mono | `Mono.error(CustomException(TMDB_API_ERROR))` |

```java
@Test
void createArchive_중복_아카이브_예외() {
    // given
    User user = createMockUser();
    ArchiveCreateRequest req = new ArchiveCreateRequest(123L, ContentType.MOVIE, 8.0f, "good");
    given(archiveRepository.existsByUserAndTmdbIdAndContentType(user, 123L, ContentType.MOVIE))
        .willReturn(true);

    // when & then
    StepVerifier.create(archiveService.createArchive(user, req))
        .expectErrorSatisfies(e -> {
            assertThat(e).isInstanceOf(CustomException.class);
            assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.ARCHIVE_ALREADY_EXISTS);
        })
        .verify();
}
```

#### `getArchive(User, Long archiveId)`

| 테스트 케이스 | 입력 | 예상 결과 |
|---|---|---|
| 정상 조회 | 본인 아카이브 ID | `ArchiveResponse` 반환 |
| 존재하지 않는 ID | `findById()` → empty | `CustomException(ARCHIVE_NOT_FOUND)` |
| 타인 아카이브 접근 | 다른 유저 소유 | `CustomException(ARCHIVE_ACCESS_DENIED)` |

#### `getArchives(User, Pageable)`

| 테스트 케이스 | 예상 결과 |
|---|---|
| 아카이브 목록 존재 | `PageResponse` 반환, content 크기 일치 |
| 빈 목록 | `PageResponse` 반환, content 빈 리스트 |

#### `deleteArchive(User, Long archiveId)`

| 테스트 케이스 | 예상 결과 |
|---|---|
| 정상 삭제 | `archiveRepository.delete()` 1회 호출 |
| 존재하지 않는 ID | `CustomException(ARCHIVE_NOT_FOUND)` |
| 타인 아카이브 | `CustomException(ARCHIVE_ACCESS_DENIED)` |

#### `updateArchive(User, Long archiveId, ArchiveUpdateRequest)`

| 테스트 케이스 | 예상 결과 |
|---|---|
| 정상 수정 | 수정된 `ArchiveResponse` 반환 (rating, review 변경 확인) |
| 존재하지 않는 ID | `CustomException(ARCHIVE_NOT_FOUND)` |
| 타인 아카이브 | `CustomException(ARCHIVE_ACCESS_DENIED)` |

#### `parseDate(String)` (private → 간접 검증)

| 입력 | 예상 결과 |
|---|---|
| `"2024-01-15"` | `LocalDate.of(2024, 1, 15)` |
| `null` | `null` |
| `""` | `null` |
| 잘못된 형식 `"2024/01/15"` | `null` 또는 예외 처리 확인 |

---

### 1-3. `ShareService`

> **전략:** `ShareLinkRepository`, `ArchiveRepository`를 Mock으로 주입.

#### `createShareLink(User)`

| 테스트 케이스 | 예상 결과 |
|---|---|
| 활성 링크 없음 | 신규 `ShareLink` 생성, UUID 토큰 포함, `save()` 호출 |
| 활성 링크 이미 존재 | 기존 링크 반환, `save()` 미호출 |
| 반환값 검증 | `isActive=true`, `shareToken` 비어있지 않음 |

```java
@Test
void createShareLink_기존_활성_링크_반환() {
    // given
    User user = createMockUser();
    ShareLink existing = ShareLink.of("existing-token", user, null);
    given(shareLinkRepository.findByUserAndIsActiveTrue(user)).willReturn(Optional.of(existing));

    // when
    ShareLinkResponse result = shareService.createShareLink(user);

    // then
    assertThat(result.getShareToken()).isEqualTo("existing-token");
    verify(shareLinkRepository, never()).save(any());
}
```

#### `getSharedArchives(String shareToken, Pageable)`

| 테스트 케이스 | 예상 결과 |
|---|---|
| 정상 조회 | `SharedArchivesPageResponse` 반환, nickname 포함 |
| 존재하지 않는 토큰 | `CustomException(SHARE_LINK_NOT_FOUND)` |
| 비활성 링크 | `CustomException(SHARE_LINK_INACTIVE)` |
| 만료된 링크 | `expiresAt`이 현재 시각 이전 → `CustomException(SHARE_LINK_EXPIRED)` |
| 만료 없는 링크 | `expiresAt=null` → 정상 조회 |

#### `deactivateShareLink(User)`

| 테스트 케이스 | 예상 결과 |
|---|---|
| 정상 비활성화 | `shareLink.deactivate()` 호출, `isActive=false` |
| 활성 링크 없음 | `CustomException(SHARE_LINK_NOT_FOUND)` |

---

### 1-4. `TmdbMovieService` / `TmdbTvService`

> **전략:** `TmdbClient`를 Mock으로 주입. `StepVerifier`로 Mono 검증.

#### `resolveLanguage(String)` (private → 간접 검증)

| 입력 | 예상 결과 |
|---|---|
| `"en-US"` | `"en-US"` (입력값 그대로) |
| `null` | `"ko-KR"` (기본값) |
| `""` | `"ko-KR"` (기본값) |
| `"  "` (공백) | `"ko-KR"` (기본값) |

```java
@Test
void searchMovies_language_null이면_기본값_사용() {
    // given
    given(tmdbClient.searchMovies("inception", 1, "ko-KR"))
        .willReturn(Mono.just(mockPageResponse()));

    // when & then
    StepVerifier.create(tmdbMovieService.searchMovies("inception", 1, null))
        .expectNextMatches(res -> res.getResults() != null)
        .verifyComplete();

    verify(tmdbClient).searchMovies("inception", 1, "ko-KR");
}
```

---

## 2. 도메인 엔티티

> **전략:** 외부 의존성 없이 순수 단위 테스트.

### `Archive`

| 테스트 케이스 | 검증 내용 |
|---|---|
| `Archive.of(...)` | 모든 필드가 올바르게 설정됨 |
| `archive.update(rating, review)` | rating, review 변경, 다른 필드 불변 |
| `update()` — rating 경계값 | 0.0, 10.0 정상 저장 |

### `ShareLink`

| 테스트 케이스 | 검증 내용 |
|---|---|
| `ShareLink.of(token, user, expiresAt)` | 필드 설정, `isActive=true` 초기값 |
| `shareLink.deactivate()` | `isActive=false`로 변경 |

### `User`

| 테스트 케이스 | 검증 내용 |
|---|---|
| `User.of(...)` | 모든 필드 설정, role 확인 |
| `user.changeNickname(newNickname)` | nickname 변경 |
| `user.changePassword(newPassword)` | password 변경 |

---

## 3. DTO 변환 메서드

> **전략:** 실제 엔티티 인스턴스를 생성해 `from()` / `of()` 결과를 검증.

### `ArchiveResponse.from(Archive)`

| 검증 필드 | 내용 |
|---|---|
| `id`, `userId`, `title`, `posterPath` | Archive 필드와 일치 |
| `rating`, `review`, `releaseDate` | Archive 필드와 일치 |
| `createdAt`, `updatedAt` | BaseEntity 필드와 일치 |

### `SignupResponse.from(User)` / `SigninResponse.from(User)`

| 검증 필드 | 내용 |
|---|---|
| `id`, `email`, `nickname`, `userRole` | User 필드와 일치 |

### `ShareLinkResponse.from(ShareLink)`

| 검증 필드 | 내용 |
|---|---|
| `shareToken` | ShareLink 토큰과 일치 |
| `shareUrl` | `"/api/share/{token}"` 형식 |
| `isActive` | true |

### `PageResponse.from(Page<T>)`

| 검증 내용 |
|---|
| `content`, `page`, `size`, `totalElements`, `totalPages`, `last` 모두 Spring Page 값과 일치 |

### `ApiResponse`

| 테스트 케이스 | 검증 내용 |
|---|---|
| `ApiResponse.ok(data)` | `success=true`, `data` 존재, `timestamp` 비어있지 않음 |
| `ApiResponse.ok(message, data)` | `message` 포함 |
| `ApiResponse.fail(message)` | `success=false`, `data=null` |

---

## 4. 예외 처리

### `GlobalExceptionHandler`

> **전략:** MockMvc + `@WebMvcTest`로 실제 HTTP 응답 코드/바디 검증.

| 예외 유형 | 검증 내용 |
|---|---|
| `CustomException(EMAIL_ALREADY_EXISTS)` | HTTP 409, `success=false`, 메시지 포함 |
| `CustomException(ARCHIVE_NOT_FOUND)` | HTTP 404, `success=false` |
| `MethodArgumentNotValidException` | HTTP 400, 필드 에러 메시지 포함 |
| `HttpMessageNotReadableException` | HTTP 400 |
| 미처리 `Exception` | HTTP 500 |

```java
// 컨트롤러 슬라이스 테스트에서 함께 검증
mockMvc.perform(post("/signup").contentType(APPLICATION_JSON).content("{}"))
    .andExpect(status().isBadRequest())
    .andExpect(jsonPath("$.success").value(false))
    .andExpect(jsonPath("$.message").isNotEmpty());
```

### `CustomException`

| 테스트 케이스 | 검증 내용 |
|---|---|
| `UNAUTHORIZED` | `getHttpStatus()` → 401 |
| `FORBIDDEN` | `getHttpStatus()` → 403 |
| `EMAIL_ALREADY_EXISTS` | `getHttpStatus()` → 409 |
| `ARCHIVE_NOT_FOUND` | `getHttpStatus()` → 404 |
| `SHARE_LINK_EXPIRED` | `getHttpStatus()` → 410 |
| `INTERNAL_SERVER_ERROR` | `getHttpStatus()` → 500 |

---

## 5. 컨트롤러 레이어

> **전략:** `@WebMvcTest` + `@MockBean` 서비스. `@AuthUser` 파라미터는 `with(user(...))` 또는 MockMvc security 설정으로 처리.

### `AuthController`

#### `POST /signup`

| 테스트 케이스 | HTTP Status | 검증 내용 |
|---|---|---|
| 정상 입력 | 200 | `success=true`, `data.email` 포함 |
| email 형식 오류 | 400 | `success=false`, 검증 메시지 |
| nickname 빈값 | 400 | `success=false` |
| password 패턴 불일치 | 400 | `success=false` |
| 이메일 중복 (서비스 예외) | 409 | `success=false`, 에러 메시지 |

#### `POST /signin`

| 테스트 케이스 | HTTP Status | 검증 내용 |
|---|---|---|
| 정상 로그인 | 200 | `success=true`, `data.nickname` 포함 |
| 비밀번호 불일치 | 400 | `success=false` |

#### `POST /signout`

| 테스트 케이스 | HTTP Status | 검증 내용 |
|---|---|---|
| 정상 로그아웃 | 200 | `success=true`, `data=null` |

---

### `ArchiveController`

#### `POST /api/archives`

| 테스트 케이스 | HTTP Status | 검증 내용 |
|---|---|---|
| 정상 생성 | 201 | `success=true`, `data.title` 포함 |
| `tmdbId` 누락 | 400 | `success=false` |
| `contentType` 누락 | 400 | `success=false` |
| `rating` 범위 초과 (11.0) | 400 | `success=false` |
| `review` 500자 초과 | 400 | `success=false` |
| 중복 아카이브 | 409 | `success=false` |
| 미인증 접근 | 401 | `success=false` |

#### `GET /api/archives`

| 테스트 케이스 | HTTP Status | 검증 내용 |
|---|---|---|
| 정상 조회 | 200 | `data.content` 배열, `data.totalElements` 포함 |
| 미인증 | 401 | `success=false` |

#### `GET /api/archives/{archiveId}`

| 테스트 케이스 | HTTP Status | 검증 내용 |
|---|---|---|
| 정상 조회 | 200 | `data.id` 일치 |
| 존재하지 않는 ID | 404 | `success=false` |
| 타인 아카이브 | 403 | `success=false` |

#### `DELETE /api/archives/{archiveId}`

| 테스트 케이스 | HTTP Status | 검증 내용 |
|---|---|---|
| 정상 삭제 | 200 | `success=true` |
| 존재하지 않는 ID | 404 | `success=false` |
| 타인 아카이브 | 403 | `success=false` |

#### `PATCH /api/archives/{archiveId}`

| 테스트 케이스 | HTTP Status | 검증 내용 |
|---|---|---|
| 정상 수정 | 200 | `data.rating`, `data.review` 변경 반영 |
| `rating` 음수 | 400 | `success=false` |

---

### `ShareController`

#### `POST /api/share`

| 테스트 케이스 | HTTP Status | 검증 내용 |
|---|---|---|
| 정상 생성 | 201 | `data.shareToken` 비어있지 않음, `data.isActive=true` |
| 미인증 | 401 | `success=false` |

#### `DELETE /api/share`

| 테스트 케이스 | HTTP Status | 검증 내용 |
|---|---|---|
| 정상 비활성화 | 200 | `success=true` |
| 활성 링크 없음 | 404 | `success=false` |

#### `GET /api/share/{shareToken}` (인증 불필요)

| 테스트 케이스 | HTTP Status | 검증 내용 |
|---|---|---|
| 유효한 토큰 | 200 | `data.nickname`, `data.content` 포함 |
| 존재하지 않는 토큰 | 404 | `success=false` |
| 비활성 토큰 | 410 | `success=false` |
| 만료된 토큰 | 410 | `success=false` |

---

### `TmdbMovieController` / `TmdbTvController`

#### `GET /api/1/movies/search`

| 테스트 케이스 | HTTP Status | 검증 내용 |
|---|---|---|
| query 정상 | 200 | `data.results` 배열 |
| query 누락 | 400 | `success=false` |
| query 100자 초과 | 400 | `success=false` |
| language 생략 | 200 | 기본 언어(`ko-KR`)로 조회 |

#### `GET /api/1/movies/{movieId}`

| 테스트 케이스 | HTTP Status | 검증 내용 |
|---|---|---|
| 유효한 movieId | 200 | `data.title` 포함 |
| 존재하지 않는 movieId | 404 | `success=false` |

> TV 엔드포인트는 동일한 패턴으로 검증 (title 대신 name 필드 확인)

---

## 6. `AuthUserArgumentResolver`

> **전략:** `MethodParameter`, `NativeWebRequest`, `ModelAndViewContainer`를 Mock으로 주입.

| 테스트 케이스 | 검증 내용 |
|---|---|
| `supportsParameter` — `@AuthUser User` | `true` 반환 |
| `supportsParameter` — annotation 없음 | `false` 반환 |
| `supportsParameter` — 다른 타입 파라미터 | `false` 반환 |
| `resolveArgument` — 정상 세션 | `User` 반환 |
| `resolveArgument` — session=null | `CustomException(SESSION_NOT_FOUND)` |
| `resolveArgument` — session에 userId 없음 | `CustomException(USERID_NOT_FOUND)` |
| `resolveArgument` — userId 있으나 DB에 없음 | `CustomException(USER_NOT_FOUND)` |

```java
@Test
void resolveArgument_정상_세션() throws Exception {
    // given
    HttpServletRequest httpReq = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    given(httpReq.getSession(false)).willReturn(session);
    given(session.getAttribute(Const.SESSION_USER_KEY)).willReturn(1L);
    User user = User.of("a@b.com", "pw", "nick", UserRole.USER);
    given(userRepository.findById(1L)).willReturn(Optional.of(user));

    // when
    Object result = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

    // then
    assertThat(result).isEqualTo(user);
}
```

---

## 7. 테스트 파일 구조

```
src/test/java/org/example/cinemanote/
├── auth/
│   ├── service/
│   │   └── AuthServiceTest.java
│   └── resolver/
│       └── AuthUserArgumentResolverTest.java
├── domain/
│   ├── archive/
│   │   ├── entity/
│   │   │   └── ArchiveTest.java
│   │   ├── service/
│   │   │   └── ArchiveServiceTest.java
│   │   └── controller/
│   │       └── ArchiveControllerTest.java
│   ├── shareLink/
│   │   ├── entity/
│   │   │   └── ShareLinkTest.java
│   │   ├── service/
│   │   │   └── ShareServiceTest.java
│   │   └── controller/
│   │       └── ShareControllerTest.java
│   ├── tmdb/
│   │   ├── service/
│   │   │   ├── TmdbMovieServiceTest.java
│   │   │   └── TmdbTvServiceTest.java
│   │   └── controller/
│   │       ├── TmdbMovieControllerTest.java
│   │       └── TmdbTvControllerTest.java
│   └── user/
│       └── entity/
│           └── UserTest.java
└── global/
    ├── exception/
    │   ├── CustomExceptionTest.java
    │   └── GlobalExceptionHandlerTest.java
    └── response/
        ├── ApiResponseTest.java
        └── PageResponseTest.java
```

---

## 8. 우선순위

| 순위 | 대상 | 이유 |
|---|---|---|
| 1 | `AuthService` | 인증은 모든 기능의 전제 조건 |
| 2 | `ArchiveService` | 핵심 비즈니스 로직, 소유권 검증 포함 |
| 3 | `ShareService` | 만료/비활성 등 엣지케이스 多 |
| 4 | `AuthUserArgumentResolver` | 모든 인증 필요 엔드포인트에서 실행 |
| 5 | `ArchiveController` | 입력 검증 케이스 多 |
| 6 | `CustomException` / `GlobalExceptionHandler` | 에러 응답 일관성 보장 |
| 7 | 엔티티 / DTO | 단순하지만 변환 오류 방지 |
| 8 | TMDB 서비스/컨트롤러 | 외부 API 의존, Mock 처리 필요 |

---

# Part 2. 프론트엔드 유닛 테스트

*추후 작성 예정*
