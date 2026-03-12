package org.example.cinemanote.domain.archive.service;

import lombok.RequiredArgsConstructor;
import org.example.cinemanote.domain.archive.dto.request.ArchiveCreateRequest;
import org.example.cinemanote.domain.archive.dto.request.ArchiveCreateRequest.ContentType;
import org.example.cinemanote.domain.archive.dto.request.ArchiveUpdateRequest;
import org.example.cinemanote.domain.archive.dto.response.ArchiveResponse;
import org.example.cinemanote.domain.archive.entity.Archive;
import org.example.cinemanote.domain.archive.repository.ArchiveRepository;
import org.example.cinemanote.domain.tmdb.service.TmdbMovieService;
import org.example.cinemanote.global.response.PageResponse;
import org.example.cinemanote.domain.tmdb.service.TmdbTvService;
import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.global.exception.CustomException;
import org.example.cinemanote.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArchiveService {

    private final ArchiveRepository archiveRepository;
    private final TmdbMovieService tmdbMovieService;
    private final TmdbTvService tmdbTvService;

    public Mono<ArchiveResponse> createArchive(User user, ArchiveCreateRequest request) {
        Mono<Archive> archiveMono;

        if (request.getContentType() == ContentType.MOVIE) {
            archiveMono = tmdbMovieService.getMovieDetail(request.getTmdbId(), null)
                    .map(tmdb -> Archive.of(
                            user,
                            tmdb.getTitle(),
                            tmdb.getPosterPath(),
                            tmdb.getOverview(),
                            parseDate(tmdb.getReleaseDate()),
                            request.getRating(),
                            request.getReview()
                    ));
        } else {
            archiveMono = tmdbTvService.getTvDetail(request.getTmdbId(), null)
                    .map(tmdb -> Archive.of(
                            user,
                            tmdb.getName(),
                            tmdb.getPosterPath(),
                            tmdb.getOverview(),
                            parseDate(tmdb.getFirstAirDate()),
                            request.getRating(),
                            request.getReview()
                    ));
        }

        return archiveMono
                .flatMap(archive -> Mono.fromCallable(() -> archiveRepository.save(archive))
                        .subscribeOn(Schedulers.boundedElastic()))
                .map(ArchiveResponse::from);
    }

    public ArchiveResponse getArchive(User user, Long archiveId) {
        Archive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARCHIVE_NOT_FOUND));
        validateOwnership(user, archive);
        return ArchiveResponse.from(archive);
    }

    public PageResponse<ArchiveResponse> getArchives(User user, Pageable pageable) {
        return PageResponse.from(archiveRepository.findAllByUser(user, pageable).map(ArchiveResponse::from));
    }

    @Transactional
    public void deleteArchive(User user, Long archiveId) {
        Archive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARCHIVE_NOT_FOUND));
        validateOwnership(user, archive);
        archiveRepository.delete(archive);
    }

    @Transactional
    public ArchiveResponse updateArchive(User user, Long archiveId, ArchiveUpdateRequest request) {
        Archive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARCHIVE_NOT_FOUND));
        validateOwnership(user, archive);
        archive.update(request.getRating(), request.getReview());
        return ArchiveResponse.from(archive);
    }

    private void validateOwnership(User user, Archive archive) {
        if (!archive.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.ARCHIVE_ACCESS_DENIED);
        }
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) return null;
        return LocalDate.parse(date);
    }
}
