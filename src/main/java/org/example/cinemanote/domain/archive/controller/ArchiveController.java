package org.example.cinemanote.domain.archive.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cinemanote.auth.annotation.AuthUser;
import org.example.cinemanote.domain.archive.dto.request.ArchiveCreateRequest;
import org.example.cinemanote.domain.archive.dto.request.ArchiveUpdateRequest;
import org.example.cinemanote.domain.archive.dto.response.ArchiveResponse;
import org.example.cinemanote.domain.archive.service.ArchiveService;
import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.global.response.ApiResponse;
import org.example.cinemanote.global.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/archives")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;

    @PostMapping
    //@ResponseStatus(HttpStatus.CREATED)
    public Mono<ApiResponse<ArchiveResponse>> createArchive(
            @AuthUser User currentUser,
            @Valid @RequestBody ArchiveCreateRequest request
    ) {
        return archiveService.createArchive(currentUser, request).map(ApiResponse::ok);
    }

    @GetMapping
    public ApiResponse<PageResponse<ArchiveResponse>> getArchives(
            @AuthUser User currentUser,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return ApiResponse.ok(archiveService.getArchives(currentUser, pageable));
    }

    @GetMapping("/{archiveId}")
    public ApiResponse<ArchiveResponse> getArchive(
            @AuthUser User currentUser,
            @PathVariable Long archiveId
    ) {
        return ApiResponse.ok(archiveService.getArchive(currentUser, archiveId));
    }

    @DeleteMapping("/{archiveId}")
    public ApiResponse<Void> deleteArchive(
            @AuthUser User currentUser,
            @PathVariable Long archiveId
    ) {
        archiveService.deleteArchive(currentUser, archiveId);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/{archiveId}")
    public ApiResponse<ArchiveResponse> updateArchive(
            @AuthUser User currentUser,
            @PathVariable Long archiveId,
            @Valid @RequestBody ArchiveUpdateRequest request
    ) {
        return ApiResponse.ok(archiveService.updateArchive(currentUser, archiveId, request));
    }
}
