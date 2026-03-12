package org.example.cinemanote.domain.archive.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ArchiveUpdateRequest {

    @DecimalMin("0.0")
    @DecimalMax("10.0")
    private float rating;

    @Size(max = 500)
    private String review;
}
