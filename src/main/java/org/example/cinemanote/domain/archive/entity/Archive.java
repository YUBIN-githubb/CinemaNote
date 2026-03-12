package org.example.cinemanote.domain.archive.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.cinemanote.domain.user.entity.User;
import org.example.cinemanote.global.common.BaseEntity;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "archives")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Archive extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    private String posterPath;

    @Column(length = 500)
    private String overview;

    private LocalDate releaseDate;

    private float rating;

    @Column(length = 500)
    private String review;

    private Archive(User user, String title, String posterPath, String overview, LocalDate releaseDate, float rating, String review) {
        this.user = user;
        this.title = title;
        this.posterPath = posterPath;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.review = review;
    }

    public static Archive of(User user, String title, String posterPath, String overview, LocalDate releaseDate, float rating, String review) {
        return new Archive(user, title, posterPath, overview, releaseDate, rating, review);
    }

    public void update(float rating, String review) {
        this.rating = rating;
        this.review = review;
    }
}
