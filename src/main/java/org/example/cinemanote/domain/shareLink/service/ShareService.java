package org.example.cinemanote.domain.shareLink.service;

import lombok.RequiredArgsConstructor;
import org.example.cinemanote.domain.archive.repository.ArchiveRepository;
import org.example.cinemanote.domain.archive.entity.Archive;
import org.example.cinemanote.domain.shareLink.dto.response.ShareLinkResponse;
import org.example.cinemanote.domain.shareLink.dto.response.SharedArchivesPageResponse;
import org.example.cinemanote.domain.shareLink.entity.ShareLink;
import org.example.cinemanote.domain.shareLink.repository.ShareLinkRepository;
import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.global.exception.CustomException;
import org.example.cinemanote.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShareService {

    private final ShareLinkRepository shareLinkRepository;
    private final ArchiveRepository archiveRepository;

    @Transactional
    public ShareLinkResponse createShareLink(User user) {
        return shareLinkRepository.findByUserAndIsActiveTrue(user)
                .map(ShareLinkResponse::from)
                .orElseGet(() -> {
                    String token = UUID.randomUUID().toString();
                    ShareLink link = ShareLink.of(token, user, null);
                    return ShareLinkResponse.from(shareLinkRepository.save(link));
                });
    }

    public SharedArchivesPageResponse getSharedArchives(String shareToken, Pageable pageable) {
        ShareLink link = shareLinkRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARE_LINK_NOT_FOUND));
        if (!link.isActive()) {
            throw new CustomException(ErrorCode.SHARE_LINK_INACTIVE);
        }
        if (link.getExpiresAt() != null && link.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.SHARE_LINK_EXPIRED);
        }
        Page<Archive> archivePage = archiveRepository.findAllByUser(link.getUser(), pageable);
        return SharedArchivesPageResponse.of(link.getUser().getNickname(), archivePage);
    }

    @Transactional
    public void deactivateShareLink(User user) {
        ShareLink link = shareLinkRepository.findByUserAndIsActiveTrue(user)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARE_LINK_NOT_FOUND));
        link.deactivate();
    }
}
