package org.example.cinemanote.domain.shareLink.service;

import org.example.cinemanote.domain.archive.dto.request.ArchiveCreateRequest.ContentType;
import org.example.cinemanote.domain.archive.entity.Archive;
import org.example.cinemanote.domain.archive.repository.ArchiveRepository;
import org.example.cinemanote.domain.shareLink.dto.response.ShareLinkResponse;
import org.example.cinemanote.domain.shareLink.dto.response.SharedArchivesPageResponse;
import org.example.cinemanote.domain.shareLink.entity.ShareLink;
import org.example.cinemanote.domain.shareLink.repository.ShareLinkRepository;
import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.global.common.UserRole;
import org.example.cinemanote.global.exception.CustomException;
import org.example.cinemanote.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShareServiceTest {

    @Mock ShareLinkRepository shareLinkRepository;
    @Mock ArchiveRepository archiveRepository;
    @InjectMocks ShareService shareService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.of("test@test.com", "pw", "tester", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    // ─── createShareLink ──────────────────────────────────────

    @Test
    void createShareLink_신규_링크_생성() {
        given(shareLinkRepository.findByUserAndIsActiveTrue(user)).willReturn(Optional.empty());
        ShareLink newLink = ShareLink.of("new-token", user, null);
        given(shareLinkRepository.save(any())).willReturn(newLink);

        ShareLinkResponse result = shareService.createShareLink(user);

        assertThat(result.getShareToken()).isEqualTo("new-token");
        assertThat(result.isActive()).isTrue();
        verify(shareLinkRepository).save(any(ShareLink.class));
    }

    @Test
    void createShareLink_기존_활성_링크_반환() {
        ShareLink existing = ShareLink.of("existing-token", user, null);
        given(shareLinkRepository.findByUserAndIsActiveTrue(user)).willReturn(Optional.of(existing));

        ShareLinkResponse result = shareService.createShareLink(user);

        assertThat(result.getShareToken()).isEqualTo("existing-token");
        verify(shareLinkRepository, never()).save(any());
    }

    @Test
    void createShareLink_shareUrl_형식_검증() {
        given(shareLinkRepository.findByUserAndIsActiveTrue(user)).willReturn(Optional.empty());
        ShareLink link = ShareLink.of("abc-123", user, null);
        given(shareLinkRepository.save(any())).willReturn(link);

        ShareLinkResponse result = shareService.createShareLink(user);

        assertThat(result.getShareUrl()).isEqualTo("/api/share/abc-123");
    }

    // ─── getSharedArchives ────────────────────────────────────

    @Test
    void getSharedArchives_정상_조회() {
        ShareLink link = ShareLink.of("valid-token", user, null);
        given(shareLinkRepository.findByShareToken("valid-token")).willReturn(Optional.of(link));
        Archive archive = buildArchive(user);
        PageRequest pageable = PageRequest.of(0, 10);
        given(archiveRepository.findAllByUser(user, pageable))
                .willReturn(new PageImpl<>(List.of(archive), pageable, 1));

        SharedArchivesPageResponse result = shareService.getSharedArchives("valid-token", pageable);

        assertThat(result.getNickname()).isEqualTo("tester");
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getSharedArchives_존재하지_않는_토큰() {
        given(shareLinkRepository.findByShareToken("bad-token")).willReturn(Optional.empty());

        assertThatThrownBy(() -> shareService.getSharedArchives("bad-token", PageRequest.of(0, 10)))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SHARE_LINK_NOT_FOUND));
    }

    @Test
    void getSharedArchives_비활성_링크() {
        ShareLink link = ShareLink.of("inactive-token", user, null);
        link.deactivate();
        given(shareLinkRepository.findByShareToken("inactive-token")).willReturn(Optional.of(link));

        assertThatThrownBy(() -> shareService.getSharedArchives("inactive-token", PageRequest.of(0, 10)))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SHARE_LINK_INACTIVE));
    }

    @Test
    void getSharedArchives_만료된_링크() {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        ShareLink link = ShareLink.of("expired-token", user, pastTime);
        given(shareLinkRepository.findByShareToken("expired-token")).willReturn(Optional.of(link));

        assertThatThrownBy(() -> shareService.getSharedArchives("expired-token", PageRequest.of(0, 10)))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SHARE_LINK_EXPIRED));
    }

    @Test
    void getSharedArchives_만료시간이_null이면_만료없이_정상_조회() {
        ShareLink link = ShareLink.of("no-expiry-token", user, null);
        given(shareLinkRepository.findByShareToken("no-expiry-token")).willReturn(Optional.of(link));
        PageRequest pageable = PageRequest.of(0, 10);
        given(archiveRepository.findAllByUser(user, pageable))
                .willReturn(new PageImpl<>(List.of(), pageable, 0));

        SharedArchivesPageResponse result = shareService.getSharedArchives("no-expiry-token", pageable);

        assertThat(result.getNickname()).isEqualTo("tester");
    }

    @Test
    void getSharedArchives_미래_만료시간은_유효() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(7);
        ShareLink link = ShareLink.of("future-token", user, futureTime);
        given(shareLinkRepository.findByShareToken("future-token")).willReturn(Optional.of(link));
        PageRequest pageable = PageRequest.of(0, 10);
        given(archiveRepository.findAllByUser(user, pageable))
                .willReturn(new PageImpl<>(List.of(), pageable, 0));

        SharedArchivesPageResponse result = shareService.getSharedArchives("future-token", pageable);

        assertThat(result.getNickname()).isEqualTo("tester");
    }

    // ─── deactivateShareLink ──────────────────────────────────

    @Test
    void deactivateShareLink_정상_비활성화() {
        ShareLink link = ShareLink.of("token", user, null);
        given(shareLinkRepository.findByUserAndIsActiveTrue(user)).willReturn(Optional.of(link));

        shareService.deactivateShareLink(user);

        assertThat(link.isActive()).isFalse();
    }

    @Test
    void deactivateShareLink_활성_링크_없음() {
        given(shareLinkRepository.findByUserAndIsActiveTrue(user)).willReturn(Optional.empty());

        assertThatThrownBy(() -> shareService.deactivateShareLink(user))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.SHARE_LINK_NOT_FOUND));
    }

    // ─── 헬퍼 메서드 ─────────────────────────────────────────

    private Archive buildArchive(User user) {
        return Archive.of(user, 1L, ContentType.MOVIE,
                "Inception", "/poster.jpg", "overview", LocalDate.of(2010, 7, 16), 8.0f, "good");
    }
}
