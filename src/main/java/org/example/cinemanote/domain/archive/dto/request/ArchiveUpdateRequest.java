package org.example.cinemanote.domain.archive.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ArchiveUpdateRequest {

    private float raiting;

    @Size(max = 500)
    private String review;
}
