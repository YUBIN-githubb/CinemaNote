package org.example.cinemanote.domain.archive.service;

import org.example.cinemanote.domain.archive.dto.request.ArchiveCreateRequest;
import org.example.cinemanote.domain.archive.dto.request.ArchiveCreateRequest.ContentType;
import org.example.cinemanote.domain.archive.dto.request.ArchiveUpdateRequest;
import org.example.cinemanote.domain.archive.dto.response.ArchiveResponse;
import org.example.cinemanote.domain.archive.entity.Archive;
import org.example.cinemanote.domain.archive.repository.ArchiveRepository;
import org.example.cinemanote.domain.tmdb.dto.response.TmdbMovieDetailResponse;
import org.example.cinemanote.domain.tmdb.dto.response.TmdbTvDetailResponse;
import org.example.cinemanote.domain.tmdb.service.TmdbMovieService;
import org.example.cinemanote.domain.tmdb.service.TmdbTvService;
import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.global.common.UserRole;
import org.example.cinemanote.global.exception.CustomException;
import org.example.cinemanote.global.exception.ErrorCode;
import org.example.cinemanote.global.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArchiveServiceTest {

    @Mock ArchiveRepository archiveRepository;
    @Mock TmdbMovieService tmdbMovieService;
    @Mock TmdbTvService tmdbTvService;
    @InjectMocks ArchiveService archiveService;

    private User owner;
    private User other;

    @BeforeEach
    void setUp() {
        owner = User.of("owner@test.com", "pw", "owner", UserRole.USER);
        ReflectionTestUtils.setField(owner, "id", 1L);

        other = User.of("other@test.com", "pw", "other", UserRole.USER);
        ReflectionTestUtils.setField(other, "id", 2L);
    }

    // ─── createArchive ────────────────────────────────────────

    @Test
    void createArchive_MOVIE_정상_생성() {
        ArchiveCreateRequest req = buildCreateRequest(123L, ContentType.MOVIE, 8.0f, "good");
        given(archiveRepository.existsByUserAndTmdbIdAndContentType(owner, 123L, ContentType.MOVIE))
                .willReturn(false);

        TmdbMovieDetailResponse tmdb = mock(TmdbMovieDetailResponse.class);
        given(tmdb.getTitle()).willReturn("Inception");
        given(tmdb.getPosterPath()).willReturn("/poster.jpg");
        given(tmdb.getOverview()).willReturn("꿈 속의 꿈");
        given(tmdb.getReleaseDate()).willReturn("2010-07-16");
        given(tmdbMovieService.getMovieDetail(123L, null)).willReturn(Mono.just(tmdb));

        Archive saved = Archive.of(owner, 123L, ContentType.MOVIE,
                "Inception", "/poster.jpg", "꿈 속의 꿈", LocalDate.of(2010, 7, 16), 8.0f, "good");
        given(archiveRepository.save(any())).willReturn(saved);

        StepVerifier.create(archiveService.createArchive(owner, req))
                .assertNext(response -> {
                    assertThat(response.getTitle()).isEqualTo("Inception");
                    assertThat(response.getRating()).isEqualTo(8.0f);
                    assertThat(response.getReview()).isEqualTo("good");
                })
                .verifyComplete();
    }

    @Test
    void createArchive_TV_정상_생성() {
        ArchiveCreateRequest req = buildCreateRequest(456L, ContentType.TV, 9.0f, "최고");
        given(archiveRepository.existsByUserAndTmdbIdAndContentType(owner, 456L, ContentType.TV))
                .willReturn(false);

        TmdbTvDetailResponse tmdb = mock(TmdbTvDetailResponse.class);
        given(tmdb.getName()).willReturn("Breaking Bad");
        given(tmdb.getPosterPath()).willReturn("/tv-poster.jpg");
        given(tmdb.getOverview()).willReturn("마약 드라마");
        given(tmdb.getFirstAirDate()).willReturn("2008-01-20");
        given(tmdbTvService.getTvDetail(456L, null)).willReturn(Mono.just(tmdb));

        Archive saved = Archive.of(owner, 456L, ContentType.TV,
                "Breaking Bad", "/tv-poster.jpg", "마약 드라마", LocalDate.of(2008, 1, 20), 9.0f, "최고");
        given(archiveRepository.save(any())).willReturn(saved);

        StepVerifier.create(archiveService.createArchive(owner, req))
                .assertNext(response -> {
                    assertThat(response.getTitle()).isEqualTo("Breaking Bad");
                    assertThat(response.getRating()).isEqualTo(9.0f);
                })
                .verifyComplete();

        verify(tmdbTvService).getTvDetail(456L, null);
        verify(tmdbMovieService, never()).getMovieDetail(any(), any());
    }

    @Test
    void createArchive_중복_아카이브_동기_예외() {
        ArchiveCreateRequest req = buildCreateRequest(123L, ContentType.MOVIE, 8.0f, "good");
        given(archiveRepository.existsByUserAndTmdbIdAndContentType(owner, 123L, ContentType.MOVIE))
                .willReturn(true);

        assertThatThrownBy(() -> archiveService.createArchive(owner, req))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ARCHIVE_ALREADY_EXISTS));
    }

    @Test
    void createArchive_releaseDate가_null이어도_정상_생성() {
        ArchiveCreateRequest req = buildCreateRequest(789L, ContentType.MOVIE, 7.0f, null);
        given(archiveRepository.existsByUserAndTmdbIdAndContentType(owner, 789L, ContentType.MOVIE))
                .willReturn(false);

        TmdbMovieDetailResponse tmdb = mock(TmdbMovieDetailResponse.class);
        given(tmdb.getTitle()).willReturn("Test Movie");
        given(tmdb.getPosterPath()).willReturn(null);
        given(tmdb.getOverview()).willReturn(null);
        given(tmdb.getReleaseDate()).willReturn(null);
        given(tmdbMovieService.getMovieDetail(789L, null)).willReturn(Mono.just(tmdb));

        Archive saved = Archive.of(owner, 789L, ContentType.MOVIE, "Test Movie", null, null, null, 7.0f, null);
        given(archiveRepository.save(any())).willReturn(saved);

        StepVerifier.create(archiveService.createArchive(owner, req))
                .assertNext(response -> assertThat(response.getTitle()).isEqualTo("Test Movie"))
                .verifyComplete();
    }

    // ─── getArchive ───────────────────────────────────────────

    @Test
    void getArchive_정상_조회() {
        Archive archive = buildArchive(1L, owner);
        given(archiveRepository.findById(1L)).willReturn(Optional.of(archive));

        ArchiveResponse result = archiveService.getArchive(owner, 1L);

        assertThat(result.getTitle()).isEqualTo("Inception");
    }

    @Test
    void getArchive_존재하지_않는_ID() {
        given(archiveRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> archiveService.getArchive(owner, 999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ARCHIVE_NOT_FOUND));
    }

    @Test
    void getArchive_타인_아카이브_접근_금지() {
        Archive archive = buildArchive(1L, owner);
        given(archiveRepository.findById(1L)).willReturn(Optional.of(archive));

        assertThatThrownBy(() -> archiveService.getArchive(other, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ARCHIVE_ACCESS_DENIED));
    }

    // ─── getArchives ──────────────────────────────────────────

    @Test
    void getArchives_목록_정상_반환() {
        Archive a1 = buildArchive(1L, owner);
        Archive a2 = buildArchive(2L, owner);
        PageRequest pageable = PageRequest.of(0, 10);
        given(archiveRepository.findAllByUser(owner, pageable))
                .willReturn(new PageImpl<>(List.of(a1, a2), pageable, 2));

        PageResponse<ArchiveResponse> result = archiveService.getArchives(owner, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void getArchives_빈_목록() {
        PageRequest pageable = PageRequest.of(0, 10);
        given(archiveRepository.findAllByUser(owner, pageable))
                .willReturn(new PageImpl<>(List.of(), pageable, 0));

        PageResponse<ArchiveResponse> result = archiveService.getArchives(owner, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    // ─── deleteArchive ────────────────────────────────────────

    @Test
    void deleteArchive_정상_삭제() {
        Archive archive = buildArchive(1L, owner);
        given(archiveRepository.findById(1L)).willReturn(Optional.of(archive));

        archiveService.deleteArchive(owner, 1L);

        verify(archiveRepository).delete(archive);
    }

    @Test
    void deleteArchive_존재하지_않는_ID() {
        given(archiveRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> archiveService.deleteArchive(owner, 999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ARCHIVE_NOT_FOUND));

        verify(archiveRepository, never()).delete(any());
    }

    @Test
    void deleteArchive_타인_아카이브_삭제_금지() {
        Archive archive = buildArchive(1L, owner);
        given(archiveRepository.findById(1L)).willReturn(Optional.of(archive));

        assertThatThrownBy(() -> archiveService.deleteArchive(other, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ARCHIVE_ACCESS_DENIED));

        verify(archiveRepository, never()).delete(any());
    }

    // ─── updateArchive ────────────────────────────────────────

    @Test
    void updateArchive_정상_수정() {
        Archive archive = buildArchive(1L, owner);
        given(archiveRepository.findById(1L)).willReturn(Optional.of(archive));
        ArchiveUpdateRequest req = buildUpdateRequest(9.5f, "수정된 리뷰");

        ArchiveResponse result = archiveService.updateArchive(owner, 1L, req);

        assertThat(result.getRating()).isEqualTo(9.5f);
        assertThat(result.getReview()).isEqualTo("수정된 리뷰");
    }

    @Test
    void updateArchive_존재하지_않는_ID() {
        given(archiveRepository.findById(999L)).willReturn(Optional.empty());
        ArchiveUpdateRequest req = buildUpdateRequest(9.0f, "리뷰");

        assertThatThrownBy(() -> archiveService.updateArchive(owner, 999L, req))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ARCHIVE_NOT_FOUND));
    }

    @Test
    void updateArchive_타인_아카이브_수정_금지() {
        Archive archive = buildArchive(1L, owner);
        given(archiveRepository.findById(1L)).willReturn(Optional.of(archive));
        ArchiveUpdateRequest req = buildUpdateRequest(9.0f, "리뷰");

        assertThatThrownBy(() -> archiveService.updateArchive(other, 1L, req))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ARCHIVE_ACCESS_DENIED));
    }

    // ─── 헬퍼 메서드 ─────────────────────────────────────────

    private ArchiveCreateRequest buildCreateRequest(Long tmdbId, ContentType contentType, float rating, String review) {
        ArchiveCreateRequest req = new ArchiveCreateRequest();
        ReflectionTestUtils.setField(req, "tmdbId", tmdbId);
        ReflectionTestUtils.setField(req, "contentType", contentType);
        ReflectionTestUtils.setField(req, "rating", rating);
        ReflectionTestUtils.setField(req, "review", review);
        return req;
    }

    private ArchiveUpdateRequest buildUpdateRequest(float rating, String review) {
        ArchiveUpdateRequest req = new ArchiveUpdateRequest();
        ReflectionTestUtils.setField(req, "rating", rating);
        ReflectionTestUtils.setField(req, "review", review);
        return req;
    }

    private Archive buildArchive(Long archiveId, User user) {
        Archive archive = Archive.of(user, 123L, ContentType.MOVIE,
                "Inception", "/poster.jpg", "overview", LocalDate.of(2010, 7, 16), 8.0f, "original");
        ReflectionTestUtils.setField(archive, "id", archiveId);
        return archive;
    }
}
