package org.example.cinemanote.domain.shareLink.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.global.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "share_links")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShareLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String shareToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean isActive = true;

    private ShareLink(String shareToken, User user, LocalDateTime expiresAt) {
        this.shareToken = shareToken;
        this.user = user;
        this.expiresAt = expiresAt;
        this.isActive = true;
    }

    public static ShareLink of(String shareToken, User user, LocalDateTime expiresAt) {
        return new ShareLink(shareToken, user, expiresAt);
    }

    public void deactivate() {
        this.isActive = false;
    }
}
