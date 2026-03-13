package org.example.cinemanote.domain.shareLink.repository;

import org.example.cinemanote.domain.shareLink.entity.ShareLink;
import org.example.cinemanote.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {
    @Query("SELECT s FROM ShareLink s JOIN FETCH s.user WHERE s.shareToken = :shareToken")
    Optional<ShareLink> findByShareToken(@Param("shareToken") String shareToken);
    Optional<ShareLink> findByUserAndIsActiveTrue(User user);
}
