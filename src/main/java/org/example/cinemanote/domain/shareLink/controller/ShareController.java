package org.example.cinemanote.domain.shareLink.controller;

import lombok.RequiredArgsConstructor;
import org.example.cinemanote.auth.annotation.AuthUser;
import org.example.cinemanote.domain.shareLink.dto.response.ShareLinkResponse;
import org.example.cinemanote.domain.shareLink.dto.response.SharedArchivesPageResponse;
import org.example.cinemanote.domain.shareLink.service.ShareService;
import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.global.response.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @PostMapping("/api/share")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ShareLinkResponse> createShareLink(@AuthUser User currentUser) {
        return ApiResponse.ok(shareService.createShareLink(currentUser));
    }

    @DeleteMapping("/api/share")
    public ApiResponse<Void> deactivateShareLink(@AuthUser User currentUser) {
        shareService.deactivateShareLink(currentUser);
        return ApiResponse.ok(null);
    }

    @GetMapping("/api/share/{shareToken}")
    public ApiResponse<SharedArchivesPageResponse> getSharedArchives(
            @PathVariable String shareToken,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return ApiResponse.ok(shareService.getSharedArchives(shareToken, pageable));
    }
}
