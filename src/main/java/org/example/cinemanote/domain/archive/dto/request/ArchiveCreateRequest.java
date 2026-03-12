package org.example.cinemanote.domain.archive.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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

    @DecimalMin("0.0")
    @DecimalMax("10.0")
    private float rating;

    @Size(max = 500)
    private String review;
}
