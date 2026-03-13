package org.example.cinemanote.domain.shareLink.dto.response;

import lombok.Getter;
import org.example.cinemanote.domain.archive.dto.request.ArchiveCreateRequest.ContentType;
import org.example.cinemanote.domain.archive.entity.Archive;

import java.time.LocalDate;

@Getter
public class SharedArchiveResponse {

    private final String title;
    private final String posterPath;
    private final String overview;
    private final LocalDate releaseDate;
    private final float rating;
    private final String review;
    private final ContentType contentType;

    private SharedArchiveResponse(Archive archive) {
        this.title = archive.getTitle();
        this.posterPath = archive.getPosterPath();
        this.overview = archive.getOverview();
        this.releaseDate = archive.getReleaseDate();
        this.rating = archive.getRating();
        this.review = archive.getReview();
        this.contentType = archive.getContentType();
    }

    public static SharedArchiveResponse from(Archive archive) {
        return new SharedArchiveResponse(archive);
    }
}
