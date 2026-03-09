package com.potatoes.Naengu.reviewrecipe.domain.model;

import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.recipe.domain.model.Recipe;
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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_recipe_review_profile_recipe",
                columnNames = {"profile_id", "recipe_id"}
        )
)
public class RecipeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at",nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "hide_like_count", nullable = false)
    private boolean hideLikeCount;

    @Column(nullable = false)
    private boolean pinned;

    private RecipeReview(Profile profile, Recipe recipe, String content) {
        this.profile = profile;
        this.recipe = recipe;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.likeCount = 0;
        this.hideLikeCount = false;
        this.pinned = false;
    }

    public static RecipeReview create(Profile profile, Recipe recipe, String content) {
        return new RecipeReview(profile, recipe, content);
    }

}
