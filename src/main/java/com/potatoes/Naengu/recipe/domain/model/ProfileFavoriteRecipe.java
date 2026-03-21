package com.potatoes.Naengu.recipe.domain.model;

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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "profile_favorite_recipe",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_profile_favorite_recipe_profile_id_recipe_id",
                        columnNames = {"profile_id", "recipe_id"}
                )
        }
)
public class ProfileFavoriteRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;


    private ProfileFavoriteRecipe(Profile profile, Recipe recipe) {
        validate(profile, recipe);
        this.profile = profile;
        this.recipe = recipe;
    }

    public static ProfileFavoriteRecipe create(Profile profile, Recipe recipe) {
        return new ProfileFavoriteRecipe(profile, recipe);
    }

    private static void validate(Profile profile, Recipe recipe) {
        if (profile == null) {
            throw new IllegalArgumentException("profileFavoriteRecipe.profile must not be null");
        }
        if (recipe == null) {
            throw new IllegalArgumentException("profileFavoriteRecipe.recipe must not be null");
        }
    }


}
