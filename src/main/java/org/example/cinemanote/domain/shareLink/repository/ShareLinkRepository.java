package org.example.cinemanote.domain.shareLink.repository;

import org.example.cinemanote.domain.shareLink.entity.ShareLink;
import org.example.cinemanote.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {
    Optional<ShareLink> findByShareToken(String shareToken);
    Optional<ShareLink> findByUserAndIsActiveTrue(User user);
}
