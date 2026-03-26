package org.example.cinemanote.domain.shareLink.dto.response;

import lombok.Getter;
import org.example.cinemanote.domain.archive.entity.Archive;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class SharedArchivesPageResponse {
    private final String nickname;
    private final List<SharedArchiveResponse> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean last;

    private SharedArchivesPageResponse(String nickname, Page<Archive> archivePage) {
        this.nickname = nickname;
        this.content = archivePage.getContent().stream()
                .map(SharedArchiveResponse::from)
                .toList();
        this.page = archivePage.getNumber();
        this.size = archivePage.getSize();
        this.totalElements = archivePage.getTotalElements();
        this.totalPages = archivePage.getTotalPages();
        this.last = archivePage.isLast();
    }

    public static SharedArchivesPageResponse of(String nickname, Page<Archive> archivePage) {
        return new SharedArchivesPageResponse(nickname, archivePage);
    }
}
