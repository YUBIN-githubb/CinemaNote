package org.example.cinemanote.domain.archive.repository;

import org.example.cinemanote.domain.archive.entity.Archive;
import org.example.cinemanote.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchiveRepository extends JpaRepository<Archive, Long> {
    Page<Archive> findAllByUser(User user, Pageable pageable);
}
