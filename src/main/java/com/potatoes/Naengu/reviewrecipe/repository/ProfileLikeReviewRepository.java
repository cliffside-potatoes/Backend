package com.potatoes.Naengu.reviewrecipe.repository;

import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.reviewrecipe.domain.model.ProfileLikeReview;
import com.potatoes.Naengu.reviewrecipe.domain.model.RecipeReview;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileLikeReviewRepository extends JpaRepository<ProfileLikeReview, Long> {

    boolean existsByProfileAndRecipeReview(Profile profile, RecipeReview recipeReview);

    Optional<ProfileLikeReview> findByProfileAndRecipeReview(Profile profile, RecipeReview recipeReview);
}
