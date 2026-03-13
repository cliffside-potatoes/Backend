package com.potatoes.Naengu.reviewrecipe.repository;

import com.potatoes.Naengu.reviewrecipe.domain.model.RecipeReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeReviewRepository extends JpaRepository<RecipeReview, Long> {

    boolean existsByProfileIdAndRecipeId(Long profileId, Long recipeId);
}
