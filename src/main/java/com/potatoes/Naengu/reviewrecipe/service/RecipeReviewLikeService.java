package com.potatoes.Naengu.reviewrecipe.service;

import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.exception.ProfileErrorCode;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import com.potatoes.Naengu.reviewrecipe.domain.model.ProfileLikeReview;
import com.potatoes.Naengu.reviewrecipe.domain.model.RecipeReview;
import com.potatoes.Naengu.reviewrecipe.exception.RecipeReviewErrorCode;
import com.potatoes.Naengu.reviewrecipe.repository.ProfileLikeReviewRepository;
import com.potatoes.Naengu.reviewrecipe.repository.RecipeReviewRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipeReviewLikeService {

    private final ProfileLikeReviewRepository profileLikeReviewRepository;
    private final ProfileRepository profileRepository;
    private final RecipeReviewRepository recipeReviewRepository;

    public RecipeReviewLikeService(ProfileLikeReviewRepository profileLikeReviewRepository,
                                   ProfileRepository profileRepository, RecipeReviewRepository recipeReviewRepository) {
        this.profileLikeReviewRepository = profileLikeReviewRepository;
        this.profileRepository = profileRepository;
        this.recipeReviewRepository = recipeReviewRepository;
    }

    @Transactional
    public void createLike(Long userId, Long recipeReviewId) {
        Profile profile = loadProfile(userId);
        RecipeReview recipeReview = loadRecipeReview(recipeReviewId);

        boolean exists = profileLikeReviewRepository.existsByProfileAndRecipeReview(profile, recipeReview);
        if (exists) {
            return;
        }

        try {
            ProfileLikeReview like = ProfileLikeReview.create(profile, recipeReview);
            profileLikeReviewRepository.saveAndFlush(like);
            recipeReview.plusLikeCount();
        } catch (DataIntegrityViolationException e) {}
    }

    private Profile loadProfile(long userId) {
        return profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(ProfileErrorCode.PROFILE_NOT_FOUND));
    }

    private RecipeReview loadRecipeReview(long recipeReviewId) {
        return recipeReviewRepository.findById(recipeReviewId)
                .orElseThrow(() -> new ApiException(RecipeReviewErrorCode.RECIPE_REVIEW_NOT_FOUND));
    }

}
