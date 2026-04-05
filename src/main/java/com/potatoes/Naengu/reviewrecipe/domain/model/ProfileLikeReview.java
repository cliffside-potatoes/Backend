package com.potatoes.Naengu.reviewrecipe.domain.model;

import com.potatoes.Naengu.profile.domain.model.Profile;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "profile_like_review",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_profile_like_review_profile_id_recipe_review_id",
                        columnNames = {"profile_id", "recipe_review_id"}
                )
        }
)
public class ProfileLikeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne
    @JoinColumn(name = "recipe_review_id", nullable = false)
    private RecipeReview recipeReview;

    private ProfileLikeReview(Profile profile, RecipeReview review) {
        validate(profile, review);
        this.profile = profile;
        this.recipeReview = review;
    }

    public static ProfileLikeReview create(Profile profile, RecipeReview review) {
        return new ProfileLikeReview(profile, review);
    }

    private static void validate(Profile profile, RecipeReview review) {
        if (profile == null) {
            throw new IllegalArgumentException("profileLikeReview.profile must not be null");
        }
        if (review == null) {
            throw new IllegalArgumentException("profileLikeReview.recipeReview must not be null");
        }
    }




}
