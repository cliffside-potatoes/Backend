package com.potatoes.Naengu.reviewrecipe.service;

import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import com.potatoes.Naengu.recipe.domain.model.Recipe;
import com.potatoes.Naengu.recipe.exception.RecipeErrorCode;
import com.potatoes.Naengu.recipe.repository.RecipeRepository;
import com.potatoes.Naengu.reviewrecipe.domain.model.RecipeReview;
import com.potatoes.Naengu.reviewrecipe.domain.model.RecipeReviewImage;
import com.potatoes.Naengu.reviewrecipe.dto.CreateRecipeReviewRequest;
import com.potatoes.Naengu.reviewrecipe.dto.RecipeReviewImageRequest;
import com.potatoes.Naengu.reviewrecipe.exception.RecipeReviewErrorCode;
import com.potatoes.Naengu.reviewrecipe.repository.RecipeReviewImageRepository;
import com.potatoes.Naengu.reviewrecipe.repository.RecipeReviewRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class RecipeReviewService {

    private final RecipeReviewRepository recipeReviewRepository;
    private final ProfileRepository profileRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeReviewImageRepository recipeReviewImageRepository;

    public RecipeReviewService(RecipeReviewRepository recipeReviewRepository, ProfileRepository profileRepository,
                               RecipeRepository recipeRepository,
                               RecipeReviewImageRepository recipeReviewImageRepository) {
        this.recipeReviewRepository = recipeReviewRepository;
        this.profileRepository = profileRepository;
        this.recipeRepository = recipeRepository;
        this.recipeReviewImageRepository = recipeReviewImageRepository;
    }


    @Transactional
    public Long create(long userId, long recipeId, CreateRecipeReviewRequest request) {
        Profile profile = loadProfile(userId);
        Recipe recipe = loadRecipe(recipeId);

        ensureNotDuplicated(profile,recipe);

        RecipeReview recipeReview = RecipeReview.create(profile, recipe, request.content());
        RecipeReview saved = recipeReviewRepository.save(recipeReview);

        List<RecipeReviewImageRequest> imageRequests =
                Optional.ofNullable(request.images()).orElse(List.of());
        List<RecipeReviewImage> images = createReviewImages(imageRequests, saved);

        recipeReviewImageRepository.saveAll(images);

        return saved.getId();

    }

    private static List<RecipeReviewImage> createReviewImages(
            List<RecipeReviewImageRequest> requests,
            RecipeReview recipeReview
    ) {
        return requests.stream()
                .map(img -> new RecipeReviewImage(
                        img.s3Key(),
                        img.contentType(),
                        img.size(),
                        img.accessType(),
                        recipeReview
                ))
                .toList();
    }

    private Recipe loadRecipe(long recipeId) {
        return recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ApiException(RecipeErrorCode.RECIPE_NOT_FOUND));
    }

    private Profile loadProfile(long userId) {
        return profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(RecipeReviewErrorCode.PROFILE_NOT_FOUND));

    }

    private void ensureNotDuplicated(Profile profile, Recipe recipe) {
        boolean exists = recipeReviewRepository.existsByProfileIdAndRecipeId(profile.getId(), recipe.getId());
        if (!exists) {
            return;
        }
        throw new ApiException(RecipeReviewErrorCode.RECIPE_REVIEW_DUPLICATE);
    }
}
