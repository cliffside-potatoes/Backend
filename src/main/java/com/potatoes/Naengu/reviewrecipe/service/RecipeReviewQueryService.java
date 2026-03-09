package com.potatoes.Naengu.reviewrecipe.service;

import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.post.exception.PostErrorCode;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import com.potatoes.Naengu.recipe.exception.RecipeErrorCode;
import com.potatoes.Naengu.recipe.repository.RecipeRepository;
import com.potatoes.Naengu.reviewrecipe.domain.model.RecipeReview;
import com.potatoes.Naengu.reviewrecipe.domain.vo.ReviewSortType;
import com.potatoes.Naengu.reviewrecipe.dto.RecipeReviewFeedItemResponse;
import com.potatoes.Naengu.reviewrecipe.dto.RecipeReviewFeedResponse;
import com.potatoes.Naengu.reviewrecipe.dto.RecipeReviewCursor;
import com.potatoes.Naengu.reviewrecipe.dto.RecipeReviewNextCursorResponse;
import com.potatoes.Naengu.reviewrecipe.repository.RecipeReviewImageRepository;
import com.potatoes.Naengu.reviewrecipe.repository.RecipeReviewRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public class RecipeReviewQueryService {

    private final String S3_BASE_URL = "임시값";

    private final RecipeRepository recipeRepository;
    private final RecipeReviewRepository recipeReviewRepository;
    private final RecipeReviewImageRepository recipeReviewImageRepository;
    private final ProfileRepository profileRepository;

    public RecipeReviewQueryService(RecipeRepository recipeRepository, RecipeReviewRepository recipeReviewRepository,
                                    RecipeReviewImageRepository recipeReviewImageRepository,
                                    ProfileRepository profileRepository) {
        this.recipeRepository = recipeRepository;
        this.recipeReviewRepository = recipeReviewRepository;
        this.recipeReviewImageRepository = recipeReviewImageRepository;
        this.profileRepository = profileRepository;
    }


    @Transactional(readOnly = true)
    public RecipeReviewFeedResponse getFeed(Long userId, int size, Long recipeId, ReviewSortType sort,
                                            RecipeReviewCursor cursor) {

        validateRecipeExists(recipeId);

        Profile currentProfile = profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(PostErrorCode.PROFILE_NOT_FOUND));

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

    private List<RecipeReview> loadReviews(int size, Long recipeId, ReviewSortType sort, RecipeReviewCursor cursor) {
        if (sort == ReviewSortType.LATEST) {
            return loadLatestReviews(size, recipeId, cursor);
        }
        throw new IllegalArgumentException("지원하지 않는 정렬 방식입니다.");

    }

    private List<RecipeReview> loadLatestReviews(int size, Long recipeId, RecipeReviewCursor cursor) {
        Pageable pageable = PageRequest.of(0, size + 1);

        if (cursor.isFirstPage()) {
            return recipeReviewRepository.findByRecipeIdOrderByCreatedAtDescIdDesc(recipeId, pageable);
        }

        return recipeReviewRepository.findLatestNextPage(recipeId, cursor.updatedAt(), cursor.id(), pageable);
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
        //  RecipeReview - RecipeReviewImage 1:N 단방향 관계
        // 특정 review가 가지고 있는 사진 모두를 가져와야함. (ImageUrls : baseUrl + s3key)
        List<String> recipeReviewImageUrls = recipeReviewImageRepository.findAllByRecipeReviewId(review.getId())
                .stream()
                .map(image -> S3_BASE_URL + image.getS3Key())
                .toList();

        // 내가 좋아요 했는 지 여부 검사 로직 추가 필요 , currentProfileId 사용
        boolean liked = false;

        Integer likeCount = review.isHideLikeCount() ? null : review.getLikeCount();

        return RecipeReviewFeedItemResponse.of(
                review.getId(),
                review.getProfile().getId(),
                review.getProfile().getNickname(),
                "profileImageUrl 추후 구현",
                review.getContent(),
                recipeReviewImageUrls,
                review.getUpdatedAt(),
                review.isHideLikeCount(),
                likeCount,
                liked
        );
    }

}
