package org.example.cinemanote.domain.tmdb.controller;

import lombok.RequiredArgsConstructor;
import org.example.cinemanote.domain.tmdb.dto.response.*;
import org.example.cinemanote.domain.tmdb.service.TmdbMovieService;
import org.example.cinemanote.global.response.ApiResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/1/movies")
@RequiredArgsConstructor
public class TmdbMovieController {

    private final TmdbMovieService tmdbMovieService;

    @GetMapping("/search")
    public Mono<ApiResponse<TmdbPageResponse<TmdbMovieSummaryResponse>>> searchMovies(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String language) {
        return tmdbMovieService.searchMovies(query, page, language)
                .map(ApiResponse::ok);
    }

    @GetMapping("/genres")
    public Mono<ApiResponse<TmdbGenreListResponse>> getMovieGenres(
            @RequestParam(required = false) String language) {
        return tmdbMovieService.getMovieGenres(language)
                .map(ApiResponse::ok);
    }

    @GetMapping("/popular")
    public Mono<ApiResponse<TmdbPageResponse<TmdbMovieSummaryResponse>>> getPopularMovies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String language) {
        return tmdbMovieService.getPopularMovies(page, language)
                .map(ApiResponse::ok);
    }

    @GetMapping("/top-rated")
    public Mono<ApiResponse<TmdbPageResponse<TmdbMovieSummaryResponse>>> getTopRatedMovies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String language) {
        return tmdbMovieService.getTopRatedMovies(page, language)
                .map(ApiResponse::ok);
    }

    @GetMapping("/{movieId}")
    public Mono<ApiResponse<TmdbMovieDetailResponse>> getMovieDetail(
            @PathVariable Long movieId,
            @RequestParam(required = false) String language) {
        return tmdbMovieService.getMovieDetail(movieId, language)
                .map(ApiResponse::ok);
    }
}
