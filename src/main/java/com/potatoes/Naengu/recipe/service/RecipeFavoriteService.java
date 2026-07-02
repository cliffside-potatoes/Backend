package com.potatoes.Naengu.recipe.service;

import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import com.potatoes.Naengu.recipe.domain.model.Recipe;
import com.potatoes.Naengu.recipe.exception.RecipeErrorCode;
import com.potatoes.Naengu.recipe.repository.ProfileFavoriteRecipeRepository;
import com.potatoes.Naengu.recipe.repository.RecipeRepository;
import com.potatoes.Naengu.reviewrecipe.exception.RecipeReviewErrorCode;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class RecipeFavoriteService {

    private final ProfileFavoriteRecipeRepository profileFavoriteRecipeRepository;
    private final ProfileRepository profileRepository;
    private final RecipeRepository recipeRepository;

    public RecipeFavoriteService(ProfileFavoriteRecipeRepository profileFavoriteRecipeRepository,
                                 ProfileRepository profileRepository, RecipeRepository recipeRepository) {
        this.profileFavoriteRecipeRepository = profileFavoriteRecipeRepository;
        this.profileRepository = profileRepository;
        this.recipeRepository = recipeRepository;
    }

    @Transactional
    public void createFavorite(Long userId, Long recipeId) {
        Profile profile = loadProfile(userId);
        Recipe recipe = loadRecipe(recipeId);

        int inserted = profileFavoriteRecipeRepository.insertIgnore(profile.getId(), recipe.getId());
        if (inserted > 0) {
            recipeRepository.increaseLikeCount(recipe.getId());
        }
    }

    @Transactional
    public void deleteFavorite(long userId, Long recipeId) {
        Profile profile = loadProfile(userId);
        Recipe recipe = loadRecipe(recipeId);

        int deleted = profileFavoriteRecipeRepository.deleteByProfileAndRecipe(profile, recipe);
        if (deleted > 0) {
            recipeRepository.decreaseLikeCount(recipe.getId());
        }
    }

    private Profile loadProfile(long userId) {
        return profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(RecipeReviewErrorCode.PROFILE_NOT_FOUND));

    }

    private Recipe loadRecipe(long recipeId) {
        return recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ApiException(RecipeErrorCode.RECIPE_NOT_FOUND));
    }
}
