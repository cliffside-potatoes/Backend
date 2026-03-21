package com.potatoes.Naengu.recipe.repository;

import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.recipe.domain.model.ProfileFavoriteRecipe;
import com.potatoes.Naengu.recipe.domain.model.Recipe;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileFavoriteRecipeRepository extends JpaRepository<ProfileFavoriteRecipe, Long> {

    boolean existsByProfileAndRecipe(Profile profile, Recipe recipe);

    Optional<ProfileFavoriteRecipe> findByProfileAndRecipe(Profile profile, Recipe recipe);
}
