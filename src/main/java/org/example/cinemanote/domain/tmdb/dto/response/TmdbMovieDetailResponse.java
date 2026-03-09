package org.example.cinemanote.domain.tmdb.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class TmdbMovieDetailResponse {

    private Long id;
    private String title;
    private String overview;
    private String tagline;
    private String status;
    private Integer runtime;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("vote_average")
    private Double voteAverage;

    private List<Genre> genres;

    @Getter
    public static class Genre {
        private Integer id;
        private String name;
    }
}
