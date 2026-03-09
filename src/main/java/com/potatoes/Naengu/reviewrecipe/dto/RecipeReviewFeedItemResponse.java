package com.potatoes.Naengu.reviewrecipe.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RecipeReviewFeedItemResponse(
        Long reviewId,
        Long profileId,
        String nickName,
        String profileImageUrl,
        String content,
        List<String> recipeReviewImageUrls,
        LocalDateTime updatedAt,
        boolean hideLikeCount,
        Integer likeCount,
        boolean liked

) {

    public static RecipeReviewFeedItemResponse of(
            Long reviewId,
            Long profileId,
            String nickName,
            String profileImageUrl,
            String content,
            List<String> recipeReviewImageUrls,
            LocalDateTime updatedAt,
            boolean hideLikeCount,
            Integer likeCount,
            boolean liked
    ) {
        return new RecipeReviewFeedItemResponse(
                reviewId,
                profileId,
                nickName,
                profileImageUrl,
                content,
                recipeReviewImageUrls,
                updatedAt,
                hideLikeCount,
                likeCount,
                liked
        );
    }
}
