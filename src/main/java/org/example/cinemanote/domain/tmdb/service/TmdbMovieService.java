package org.example.cinemanote.domain.tmdb.service;

import lombok.RequiredArgsConstructor;
import org.example.cinemanote.domain.tmdb.client.TmdbClient;
import org.example.cinemanote.domain.tmdb.dto.response.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TmdbMovieService {

    private final TmdbClient tmdbClient;

    private static final String DEFAULT_LANGUAGE = "ko-KR";

    public Mono<TmdbPageResponse<TmdbMovieSummaryResponse>> searchMovies(String query, int page, String language) {
        return tmdbClient.searchMovies(query, page, resolveLanguage(language));
    }

    public Mono<TmdbGenreListResponse> getMovieGenres(String language) {
        return tmdbClient.getMovieGenres(resolveLanguage(language));
    }

    public Mono<TmdbPageResponse<TmdbMovieSummaryResponse>> getPopularMovies(int page, String language) {
        return tmdbClient.getPopularMovies(page, resolveLanguage(language));
    }

    public Mono<TmdbPageResponse<TmdbMovieSummaryResponse>> getTopRatedMovies(int page, String language) {
        return tmdbClient.getTopRatedMovies(page, resolveLanguage(language));
    }

    public Mono<TmdbMovieDetailResponse> getMovieDetail(Long movieId, String language) {
        return tmdbClient.getMovieDetail(movieId, resolveLanguage(language));
    }

    private String resolveLanguage(String language) {
        return (language != null && !language.isBlank()) ? language : DEFAULT_LANGUAGE;
    }
}
