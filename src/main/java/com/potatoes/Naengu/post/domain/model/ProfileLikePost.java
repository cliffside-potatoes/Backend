package com.potatoes.Naengu.post.domain.model;

import com.potatoes.Naengu.profile.domain.model.Profile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "profile_like_post",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_profile_like_post_profile_id_post_id",
                        columnNames = {"profile_id", "post_id"}
                )
        }
)
public class ProfileLikePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private ProfileLikePost(Profile profile, Post post) {
        validate(profile, post);
        this.profile = profile;
        this.post = post;
        this.createdAt = LocalDateTime.now();
    }

    public static ProfileLikePost create(Profile profile, Post post) {
        return new ProfileLikePost(profile, post);
    }

    private static void validate(Profile profile, Post post) {
        if (profile == null) {
            throw new IllegalArgumentException("profileLikePost.profile must not be null");
        }
        if (post == null) {
            throw new IllegalArgumentException("profileLikePost.post must not be null");
        }
    }
}
