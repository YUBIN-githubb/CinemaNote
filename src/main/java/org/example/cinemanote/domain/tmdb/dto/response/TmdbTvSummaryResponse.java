package org.example.cinemanote.domain.tmdb.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class TmdbTvSummaryResponse {

    private Long id;
    private String name;
    private String overview;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("first_air_date")
    private String firstAirDate;

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("genre_ids")
    private List<Integer> genreIds;
}
