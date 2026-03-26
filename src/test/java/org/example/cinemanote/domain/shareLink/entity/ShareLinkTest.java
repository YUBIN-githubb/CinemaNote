package org.example.cinemanote.domain.shareLink.entity;

import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.global.common.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ShareLinkTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.of("test@test.com", "pw", "tester", UserRole.USER);
    }

    @Test
    void of_모든_필드_정상_설정() {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        ShareLink link = ShareLink.of("test-token", user, expiresAt);

        assertThat(link.getShareToken()).isEqualTo("test-token");
        assertThat(link.getUser()).isEqualTo(user);
        assertThat(link.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(link.isActive()).isTrue();
    }

    @Test
    void of_expiresAt이_null인_경우_만료없음() {
        ShareLink link = ShareLink.of("token", user, null);

        assertThat(link.getExpiresAt()).isNull();
        assertThat(link.isActive()).isTrue();
    }

    @Test
    void of_생성시_isActive는_true() {
        ShareLink link = ShareLink.of("token", user, null);

        assertThat(link.isActive()).isTrue();
    }

    @Test
    void deactivate_isActive가_false로_변경() {
        ShareLink link = ShareLink.of("token", user, null);

        link.deactivate();

        assertThat(link.isActive()).isFalse();
    }

    @Test
    void deactivate_다른_필드는_변경되지_않는다() {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
        ShareLink link = ShareLink.of("token", user, expiresAt);

        link.deactivate();

        assertThat(link.getShareToken()).isEqualTo("token");
        assertThat(link.getUser()).isEqualTo(user);
        assertThat(link.getExpiresAt()).isEqualTo(expiresAt);
    }
}
