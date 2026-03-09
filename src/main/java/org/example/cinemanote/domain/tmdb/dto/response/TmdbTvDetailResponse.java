package org.example.cinemanote.domain.tmdb.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class TmdbTvDetailResponse {

    private Long id;
    private String name;
    private String overview;
    private String status;
    private String tagline;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("first_air_date")
    private String firstAirDate;

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("number_of_seasons")
    private Integer numberOfSeasons;

    @JsonProperty("number_of_episodes")
    private Integer numberOfEpisodes;

    private List<Genre> genres;

    @Getter
    public static class Genre {
        private Integer id;
        private String name;
    }
}
