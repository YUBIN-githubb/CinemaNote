package org.example.cinemanote.domain.archive.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ArchiveCreateRequest {

    public enum ContentType { MOVIE, TV }

    @NotNull
    private Long tmdbId;

    @NotNull
    private ContentType contentType;

    private float rating;

    @Size(max = 500)
    private String review;
}
