package org.example.cinemanote.domain.tmdb.service;

import org.example.cinemanote.domain.tmdb.client.TmdbClient;
import org.example.cinemanote.domain.tmdb.dto.response.TmdbMovieDetailResponse;
import org.example.cinemanote.domain.tmdb.dto.response.TmdbMovieSummaryResponse;
import org.example.cinemanote.domain.tmdb.dto.response.TmdbPageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TmdbMovieServiceTest {

    @Mock TmdbClient tmdbClient;
    @InjectMocks TmdbMovieService tmdbMovieService;

    private static final String DEFAULT_LANG = "ko-KR";

    // ─── resolveLanguage (간접 검증) ──────────────────────────

    @Test
    void searchMovies_language_null이면_기본값_koKR_사용() {
        TmdbPageResponse<TmdbMovieSummaryResponse> mockResponse = mock(TmdbPageResponse.class);
        given(tmdbClient.searchMovies("inception", 1, DEFAULT_LANG))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(tmdbMovieService.searchMovies("inception", 1, null))
                .expectNext(mockResponse)
                .verifyComplete();

        verify(tmdbClient).searchMovies("inception", 1, DEFAULT_LANG);
    }

    @Test
    void searchMovies_language_빈문자열이면_기본값_사용() {
        TmdbPageResponse<TmdbMovieSummaryResponse> mockResponse = mock(TmdbPageResponse.class);
        given(tmdbClient.searchMovies("inception", 1, DEFAULT_LANG))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(tmdbMovieService.searchMovies("inception", 1, ""))
                .expectNext(mockResponse)
                .verifyComplete();

        verify(tmdbClient).searchMovies("inception", 1, DEFAULT_LANG);
    }

    @Test
    void searchMovies_language_공백이면_기본값_사용() {
        TmdbPageResponse<TmdbMovieSummaryResponse> mockResponse = mock(TmdbPageResponse.class);
        given(tmdbClient.searchMovies("inception", 1, DEFAULT_LANG))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(tmdbMovieService.searchMovies("inception", 1, "  "))
                .expectNext(mockResponse)
                .verifyComplete();
    }

    @Test
    void searchMovies_language_명시적_지정시_그대로_사용() {
        TmdbPageResponse<TmdbMovieSummaryResponse> mockResponse = mock(TmdbPageResponse.class);
        given(tmdbClient.searchMovies("inception", 1, "en-US"))
                .willReturn(Mono.just(mockResponse));

        StepVerifier.create(tmdbMovieService.searchMovies("inception", 1, "en-US"))
                .expectNext(mockResponse)
                .verifyComplete();

        verify(tmdbClient).searchMovies("inception", 1, "en-US");
    }

    // ─── getMovieDetail ───────────────────────────────────────

    @Test
    void getMovieDetail_정상_호출() {
        TmdbMovieDetailResponse mockDetail = mock(TmdbMovieDetailResponse.class);
        given(tmdbClient.getMovieDetail(123L, DEFAULT_LANG))
                .willReturn(Mono.just(mockDetail));

        StepVerifier.create(tmdbMovieService.getMovieDetail(123L, null))
                .expectNext(mockDetail)
                .verifyComplete();
    }

    @Test
    void getMovieDetail_language_전달_시_그대로_사용() {
        TmdbMovieDetailResponse mockDetail = mock(TmdbMovieDetailResponse.class);
        given(tmdbClient.getMovieDetail(123L, "en-US"))
                .willReturn(Mono.just(mockDetail));

        StepVerifier.create(tmdbMovieService.getMovieDetail(123L, "en-US"))
                .expectNext(mockDetail)
                .verifyComplete();

        verify(tmdbClient).getMovieDetail(123L, "en-US");
    }

    // ─── getPopularMovies ─────────────────────────────────────

    @Test
    void getPopularMovies_language_null이면_기본값_사용() {
        TmdbPageResponse<TmdbMovieSummaryResponse> mockResponse = mock(TmdbPageResponse.class);
        given(tmdbClient.getPopularMovies(1, DEFAULT_LANG)).willReturn(Mono.just(mockResponse));

        StepVerifier.create(tmdbMovieService.getPopularMovies(1, null))
                .expectNext(mockResponse)
                .verifyComplete();

        verify(tmdbClient).getPopularMovies(1, DEFAULT_LANG);
    }

    // ─── getTopRatedMovies ────────────────────────────────────

    @Test
    void getTopRatedMovies_정상_호출() {
        TmdbPageResponse<TmdbMovieSummaryResponse> mockResponse = mock(TmdbPageResponse.class);
        given(tmdbClient.getTopRatedMovies(1, DEFAULT_LANG)).willReturn(Mono.just(mockResponse));

        StepVerifier.create(tmdbMovieService.getTopRatedMovies(1, null))
                .expectNext(mockResponse)
                .verifyComplete();
    }
}
