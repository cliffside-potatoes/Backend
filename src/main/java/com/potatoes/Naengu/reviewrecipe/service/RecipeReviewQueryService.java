package com.potatoes.Naengu.reviewrecipe.service;

import com.potatoes.Naengu.file.service.FileUploadService;
import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.post.exception.PostErrorCode;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import com.potatoes.Naengu.recipe.exception.RecipeErrorCode;
import com.potatoes.Naengu.recipe.repository.RecipeRepository;
import com.potatoes.Naengu.reviewrecipe.domain.model.RecipeReview;
import com.potatoes.Naengu.reviewrecipe.domain.vo.ReviewSortType;
import com.potatoes.Naengu.reviewrecipe.dto.query.RecipeReviewFeedItemResponse;
import com.potatoes.Naengu.reviewrecipe.dto.query.RecipeReviewFeedResponse;
import com.potatoes.Naengu.reviewrecipe.dto.query.RecipeReviewCursor;
import com.potatoes.Naengu.reviewrecipe.dto.query.RecipeReviewNextCursorResponse;
import com.potatoes.Naengu.reviewrecipe.repository.RecipeReviewImageRepository;
import com.potatoes.Naengu.reviewrecipe.repository.RecipeReviewRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipeReviewQueryService {

    private final RecipeRepository recipeRepository;
    private final RecipeReviewRepository recipeReviewRepository;
    private final RecipeReviewImageRepository recipeReviewImageRepository;
    private final ProfileRepository profileRepository;
    private final FileUploadService fileUploadService;

    public RecipeReviewQueryService(RecipeRepository recipeRepository, RecipeReviewRepository recipeReviewRepository,
                                    RecipeReviewImageRepository recipeReviewImageRepository,
                                    ProfileRepository profileRepository, FileUploadService fileUploadService) {
        this.recipeRepository = recipeRepository;
        this.recipeReviewRepository = recipeReviewRepository;
        this.recipeReviewImageRepository = recipeReviewImageRepository;
        this.profileRepository = profileRepository;
        this.fileUploadService = fileUploadService;
    }


    @Transactional(readOnly = true)
    public RecipeReviewFeedResponse getFeed(Long userId, int size, Long recipeId, ReviewSortType sort,
                                            RecipeReviewCursor cursor) {
        validateRecipeExists(recipeId);

        Profile currentProfile = loadProfile(userId);
        List<RecipeReview> reviews = loadReviews(size, recipeId, sort, cursor);
        boolean hasNext = reviews.size() > size;

        List<RecipeReview> content = sliceCount(reviews, size);
        RecipeReviewCursor nextCursor = createNextCursor(hasNext, content);

        long totalCount = recipeReviewRepository.countByRecipeId(recipeId);

        List<RecipeReviewFeedItemResponse> items = content.stream()
                .map(review -> toItemResponse(currentProfile.getId(), review))
                .toList();

        return RecipeReviewFeedResponse.of(totalCount, items, hasNext, RecipeReviewNextCursorResponse.from(nextCursor));
    }

    private void validateRecipeExists(Long recipeId) {
        if (!recipeRepository.existsById(recipeId)) {
            throw new ApiException(RecipeErrorCode.RECIPE_NOT_FOUND);
        }
    }

    private Profile loadProfile(Long userId) {
        return profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(PostErrorCode.PROFILE_NOT_FOUND));
    }

    private List<RecipeReview> loadReviews(int size, Long recipeId, ReviewSortType sort, RecipeReviewCursor cursor) {
        if (sort == ReviewSortType.LATEST) {
            return loadLatestReviews(size, recipeId, cursor);
        }
        if (sort == ReviewSortType.LIKE) {
            return loadLikeReviews(size, recipeId, cursor);
        }
        throw new IllegalArgumentException("지원하지 않는 정렬 방식입니다.");
    }

    private List<RecipeReview> loadLatestReviews(int size, Long recipeId, RecipeReviewCursor cursor) {
        Pageable pageable = PageRequest.of(0, size + 1);

        if (cursor.isFirstPage()) {
            return recipeReviewRepository.findByRecipeIdOrderByUpdatedAtDescIdDesc(recipeId, pageable);
        }

        return recipeReviewRepository.findLatestNextPage(recipeId, cursor.updatedAt(), cursor.id(), pageable);
    }

    private List<RecipeReview> loadLikeReviews(int size, Long recipeId, RecipeReviewCursor cursor) {
        Pageable pageable = PageRequest.of(0, size + 1);

        if (cursor.isFirstPage()) {
            return recipeReviewRepository.findByRecipeIdOrderByLikeCountDescUpdatedAtDescIdDesc(recipeId, pageable);
        }

        return recipeReviewRepository.findLikeNextPage(
                recipeId,
                cursor.likeCount(),
                cursor.updatedAt(),
                cursor.id(),
                pageable
        );
    }


    private List<RecipeReview> sliceCount(List<RecipeReview> reviews, int size) {
        if (reviews.size() <= size) {
            return reviews;
        }
        return reviews.subList(0, size);
    }

    private RecipeReviewCursor createNextCursor(boolean hasNext, List<RecipeReview> content) {
        if (!hasNext || content.isEmpty()) {
            return null;
        }
        return RecipeReviewCursor.from(content.get(content.size() - 1));
    }

    private RecipeReviewFeedItemResponse toItemResponse(long currentProfileId, RecipeReview review) {

        List<String> recipeReviewImageUrls = recipeReviewImageRepository.findAllByRecipeReviewId(review.getId())
                .stream()
                .map(image -> fileUploadService.getPublicUrl(image.getS3Key()))
                .toList();

        // 내가 좋아요 했는 지 여부 검사 로직 추가 필요 , currentProfileId 사용
        boolean liked = false;

        Integer likeCount = review.isHideLikeCount() ? null : review.getLikeCount();

        Profile profile = review.getProfile();
        String profileImageUrl = profile.getProfileImage() != null
                ? fileUploadService.getPublicUrl(profile.getProfileImage().getS3Key())
                : fileUploadService.getDefaultProfileImageUrl();

        return RecipeReviewFeedItemResponse.of(
                review.getId(),
                profile.getId(),
                profile.getNickname(),
                profileImageUrl,
                review.getContent(),
                recipeReviewImageUrls,
                review.getUpdatedAt(),
                review.isHideLikeCount(),
                likeCount,
                liked
        );
    }

}
