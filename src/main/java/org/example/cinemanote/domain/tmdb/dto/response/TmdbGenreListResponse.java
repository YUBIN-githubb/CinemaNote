package org.example.cinemanote.domain.tmdb.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class TmdbGenreListResponse {

    private List<Genre> genres;

    @Getter
    public static class Genre {
        private Integer id;
        private String name;
    }
}
