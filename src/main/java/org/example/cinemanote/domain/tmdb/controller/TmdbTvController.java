package org.example.cinemanote.domain.tmdb.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.example.cinemanote.domain.tmdb.dto.response.*;
import org.example.cinemanote.domain.tmdb.service.TmdbTvService;
import org.example.cinemanote.global.response.ApiResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/1/tv")
@RequiredArgsConstructor
public class TmdbTvController {

    private final TmdbTvService tmdbTvService;

    @GetMapping("/search")
    public Mono<ApiResponse<TmdbPageResponse<TmdbTvSummaryResponse>>> searchTv(
            @RequestParam @NotBlank @Size(max=100) String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String language) {
        return tmdbTvService.searchTv(query, page, language)
                .map(ApiResponse::ok);
    }

    @GetMapping("/genres")
    public Mono<ApiResponse<TmdbGenreListResponse>> getTvGenres(
            @RequestParam(required = false) String language) {
        return tmdbTvService.getTvGenres(language)
                .map(ApiResponse::ok);
    }

    @GetMapping("/popular")
    public Mono<ApiResponse<TmdbPageResponse<TmdbTvSummaryResponse>>> getPopularTv(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String language) {
        return tmdbTvService.getPopularTv(page, language)
                .map(ApiResponse::ok);
    }

    @GetMapping("/top-rated")
    public Mono<ApiResponse<TmdbPageResponse<TmdbTvSummaryResponse>>> getTopRatedTv(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String language) {
        return tmdbTvService.getTopRatedTv(page, language)
                .map(ApiResponse::ok);
    }

    @GetMapping("/{tvId}")
    public Mono<ApiResponse<TmdbTvDetailResponse>> getTvDetail(
            @PathVariable Long tvId,
            @RequestParam(required = false) String language) {
        return tmdbTvService.getTvDetail(tvId, language)
                .map(ApiResponse::ok);
    }
}
