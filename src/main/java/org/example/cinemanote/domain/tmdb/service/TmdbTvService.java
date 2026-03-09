package org.example.cinemanote.domain.tmdb.service;

import lombok.RequiredArgsConstructor;
import org.example.cinemanote.domain.tmdb.client.TmdbClient;
import org.example.cinemanote.domain.tmdb.dto.response.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TmdbTvService {

    private final TmdbClient tmdbClient;

    private static final String DEFAULT_LANGUAGE = "ko-KR";

    public Mono<TmdbPageResponse<TmdbTvSummaryResponse>> searchTv(String query, int page, String language) {
        return tmdbClient.searchTv(query, page, resolveLanguage(language));
    }

    public Mono<TmdbGenreListResponse> getTvGenres(String language) {
        return tmdbClient.getTvGenres(resolveLanguage(language));
    }

    public Mono<TmdbPageResponse<TmdbTvSummaryResponse>> getPopularTv(int page, String language) {
        return tmdbClient.getPopularTv(page, resolveLanguage(language));
    }

    public Mono<TmdbPageResponse<TmdbTvSummaryResponse>> getTopRatedTv(int page, String language) {
        return tmdbClient.getTopRatedTv(page, resolveLanguage(language));
    }

    public Mono<TmdbTvDetailResponse> getTvDetail(Long tvId, String language) {
        return tmdbClient.getTvDetail(tvId, resolveLanguage(language));
    }

    private String resolveLanguage(String language) {
        return (language != null && !language.isBlank()) ? language : DEFAULT_LANGUAGE;
    }
}
