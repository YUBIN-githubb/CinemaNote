package org.example.cinemanote.domain.archive.entity;

import org.example.cinemanote.domain.archive.dto.request.ArchiveCreateRequest.ContentType;
import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.global.common.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ArchiveTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.of("test@test.com", "pw", "tester", UserRole.USER);
    }

    @Test
    void of_모든_필드_정상_설정() {
        LocalDate releaseDate = LocalDate.of(2010, 7, 16);

        Archive archive = Archive.of(user, 123L, ContentType.MOVIE,
                "Inception", "/poster.jpg", "꿈 속의 꿈", releaseDate, 9.0f, "명작");

        assertThat(archive.getUser()).isEqualTo(user);
        assertThat(archive.getTmdbId()).isEqualTo(123L);
        assertThat(archive.getContentType()).isEqualTo(ContentType.MOVIE);
        assertThat(archive.getTitle()).isEqualTo("Inception");
        assertThat(archive.getPosterPath()).isEqualTo("/poster.jpg");
        assertThat(archive.getOverview()).isEqualTo("꿈 속의 꿈");
        assertThat(archive.getReleaseDate()).isEqualTo(releaseDate);
        assertThat(archive.getRating()).isEqualTo(9.0f);
        assertThat(archive.getReview()).isEqualTo("명작");
    }

    @Test
    void of_poster와_releaseDate가_null인_경우() {
        Archive archive = Archive.of(user, 456L, ContentType.TV,
                "Breaking Bad", null, "마약 드라마", null, 10.0f, "최고");

        assertThat(archive.getPosterPath()).isNull();
        assertThat(archive.getReleaseDate()).isNull();
    }

    @Test
    void update_rating과_review가_변경된다() {
        Archive archive = Archive.of(user, 123L, ContentType.MOVIE,
                "Inception", "/poster.jpg", "overview", LocalDate.now(), 5.0f, "처음 리뷰");

        archive.update(9.5f, "수정된 리뷰");

        assertThat(archive.getRating()).isEqualTo(9.5f);
        assertThat(archive.getReview()).isEqualTo("수정된 리뷰");
    }

    @Test
    void update_rating_경계값_0() {
        Archive archive = Archive.of(user, 1L, ContentType.MOVIE,
                "title", null, null, null, 5.0f, null);

        archive.update(0.0f, null);

        assertThat(archive.getRating()).isEqualTo(0.0f);
    }

    @Test
    void update_rating_경계값_10() {
        Archive archive = Archive.of(user, 1L, ContentType.MOVIE,
                "title", null, null, null, 5.0f, null);

        archive.update(10.0f, "perfect");

        assertThat(archive.getRating()).isEqualTo(10.0f);
        assertThat(archive.getReview()).isEqualTo("perfect");
    }

    @Test
    void update_다른_필드는_변경되지_않는다() {
        Archive archive = Archive.of(user, 123L, ContentType.MOVIE,
                "Inception", "/poster.jpg", "overview", LocalDate.of(2010, 7, 16), 5.0f, "original");

        archive.update(8.0f, "updated");

        assertThat(archive.getTitle()).isEqualTo("Inception");
        assertThat(archive.getTmdbId()).isEqualTo(123L);
        assertThat(archive.getContentType()).isEqualTo(ContentType.MOVIE);
    }
}
