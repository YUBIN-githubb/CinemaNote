package org.example.cinemanote.domain.archive.dto.response;

import lombok.Getter;
import org.example.cinemanote.domain.archive.entity.Archive;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class ArchiveResponse {

    private final Long id;
    private final Long userId;
    private final String title;
    private final String posterPath;
    private final String overview;
    private final LocalDate releaseDate;
    private final float rating;
    private final String review;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ArchiveResponse(Archive archive) {
        this.id = archive.getId();
        this.userId = archive.getUser().getId();
        this.title = archive.getTitle();
        this.posterPath = archive.getPosterPath();
        this.overview = archive.getOverview();
        this.releaseDate = archive.getReleaseDate();
        this.rating = archive.getRating();
        this.review = archive.getReview();
        this.createdAt = archive.getCreatedAt();
        this.updatedAt = archive.getUpdatedAt();
    }

    public static ArchiveResponse from(Archive archive) {
        return new ArchiveResponse(archive);
    }
}
