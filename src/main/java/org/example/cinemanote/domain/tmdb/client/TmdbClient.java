package org.example.cinemanote.domain.tmdb.client;

import lombok.RequiredArgsConstructor;
import org.example.cinemanote.domain.tmdb.dto.response.*;
import org.example.cinemanote.global.exception.CustomException;
import org.example.cinemanote.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TmdbClient {

    private final WebClient tmdbWebClient;

    @Value("${tmdb.api-key}")
    private String apiKey;

    private static final String DEFAULT_LANGUAGE = "ko-KR";

    public Mono<TmdbPageResponse<TmdbMovieSummaryResponse>> searchMovies(String query, int page, String language) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/movie")
                        .queryParam("api_key", apiKey)
                        .queryParam("query", query)
                        .queryParam("page", page)
                        .queryParam("language", language)
                        .build())
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response ->
                        Mono.error(new CustomException(ErrorCode.MOVIE_NOT_FOUND)))
                .onStatus(status -> status.isError(), response ->
                        Mono.error(new CustomException(ErrorCode.TMDB_API_ERROR)))
                .bodyToMono(new ParameterizedTypeReference<TmdbPageResponse<TmdbMovieSummaryResponse>>() {})
                .onErrorMap(e -> !(e instanceof CustomException), e -> new CustomException(ErrorCode.TMDB_API_ERROR));
    }

    public Mono<TmdbGenreListResponse> getMovieGenres(String language) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/genre/movie/list")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", language)
                        .build())
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        Mono.error(new CustomException(ErrorCode.TMDB_API_ERROR)))
                .bodyToMono(TmdbGenreListResponse.class)
                .onErrorMap(e -> !(e instanceof CustomException), e -> new CustomException(ErrorCode.TMDB_API_ERROR));
    }

    public Mono<TmdbPageResponse<TmdbMovieSummaryResponse>> getPopularMovies(int page, String language) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/popular")
                        .queryParam("api_key", apiKey)
                        .queryParam("page", page)
                        .queryParam("language", language)
                        .build())
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        Mono.error(new CustomException(ErrorCode.TMDB_API_ERROR)))
                .bodyToMono(new ParameterizedTypeReference<TmdbPageResponse<TmdbMovieSummaryResponse>>() {})
                .onErrorMap(e -> !(e instanceof CustomException), e -> new CustomException(ErrorCode.TMDB_API_ERROR));
    }

    public Mono<TmdbPageResponse<TmdbMovieSummaryResponse>> getTopRatedMovies(int page, String language) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/top_rated")
                        .queryParam("api_key", apiKey)
                        .queryParam("page", page)
                        .queryParam("language", language)
                        .build())
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        Mono.error(new CustomException(ErrorCode.TMDB_API_ERROR)))
                .bodyToMono(new ParameterizedTypeReference<TmdbPageResponse<TmdbMovieSummaryResponse>>() {})
                .onErrorMap(e -> !(e instanceof CustomException), e -> new CustomException(ErrorCode.TMDB_API_ERROR));
    }

    public Mono<TmdbMovieDetailResponse> getMovieDetail(Long movieId, String language) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{movie_id}")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", language)
                        .build(movieId))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response ->
                        Mono.error(new CustomException(ErrorCode.MOVIE_NOT_FOUND)))
                .onStatus(status -> status.isError(), response ->
                        Mono.error(new CustomException(ErrorCode.TMDB_API_ERROR)))
                .bodyToMono(TmdbMovieDetailResponse.class)
                .onErrorMap(e -> !(e instanceof CustomException), e -> new CustomException(ErrorCode.TMDB_API_ERROR));
    }

    public Mono<TmdbPageResponse<TmdbTvSummaryResponse>> searchTv(String query, int page, String language) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/tv")
                        .queryParam("api_key", apiKey)
                        .queryParam("query", query)
                        .queryParam("page", page)
                        .queryParam("language", language)
                        .build())
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response ->
                        Mono.error(new CustomException(ErrorCode.TV_NOT_FOUND)))
                .onStatus(status -> status.isError(), response ->
                        Mono.error(new CustomException(ErrorCode.TMDB_API_ERROR)))
                .bodyToMono(new ParameterizedTypeReference<TmdbPageResponse<TmdbTvSummaryResponse>>() {})
                .onErrorMap(e -> !(e instanceof CustomException), e -> new CustomException(ErrorCode.TMDB_API_ERROR));
    }

    public Mono<TmdbGenreListResponse> getTvGenres(String language) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/genre/tv/list")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", language)
                        .build())
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        Mono.error(new CustomException(ErrorCode.TMDB_API_ERROR)))
                .bodyToMono(TmdbGenreListResponse.class)
                .onErrorMap(e -> !(e instanceof CustomException), e -> new CustomException(ErrorCode.TMDB_API_ERROR));
    }

    public Mono<TmdbPageResponse<TmdbTvSummaryResponse>> getPopularTv(int page, String language) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/popular")
                        .queryParam("api_key", apiKey)
                        .queryParam("page", page)
                        .queryParam("language", language)
                        .build())
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        Mono.error(new CustomException(ErrorCode.TMDB_API_ERROR)))
                .bodyToMono(new ParameterizedTypeReference<TmdbPageResponse<TmdbTvSummaryResponse>>() {})
                .onErrorMap(e -> !(e instanceof CustomException), e -> new CustomException(ErrorCode.TMDB_API_ERROR));
    }

    public Mono<TmdbPageResponse<TmdbTvSummaryResponse>> getTopRatedTv(int page, String language) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/top_rated")
                        .queryParam("api_key", apiKey)
                        .queryParam("page", page)
                        .queryParam("language", language)
                        .build())
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        Mono.error(new CustomException(ErrorCode.TMDB_API_ERROR)))
                .bodyToMono(new ParameterizedTypeReference<TmdbPageResponse<TmdbTvSummaryResponse>>() {})
                .onErrorMap(e -> !(e instanceof CustomException), e -> new CustomException(ErrorCode.TMDB_API_ERROR));
    }

    public Mono<TmdbTvDetailResponse> getTvDetail(Long tvId, String language) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/{tv_id}")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", language)
                        .build(tvId))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, response ->
                        Mono.error(new CustomException(ErrorCode.TV_NOT_FOUND)))
                .onStatus(status -> status.isError(), response ->
                        Mono.error(new CustomException(ErrorCode.TMDB_API_ERROR)))
                .bodyToMono(TmdbTvDetailResponse.class)
                .onErrorMap(e -> !(e instanceof CustomException), e -> new CustomException(ErrorCode.TMDB_API_ERROR));
    }
}
