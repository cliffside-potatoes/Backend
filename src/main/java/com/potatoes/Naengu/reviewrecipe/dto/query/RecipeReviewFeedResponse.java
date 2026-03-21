package com.potatoes.Naengu.reviewrecipe.dto.query;

import java.util.List;

public record RecipeReviewFeedResponse(
        long totalCount,
        List<RecipeReviewFeedItemResponse> items,
        boolean hasNext,
        RecipeReviewNextCursorResponse nextCursor
) {

    public static RecipeReviewFeedResponse of(
            long totalCount,
            List<RecipeReviewFeedItemResponse> items,
            boolean hasNext,
            RecipeReviewNextCursorResponse nextCursor
    ) {
        return new RecipeReviewFeedResponse(
                totalCount,
                items,
                hasNext,
                nextCursor
        );
    }

}
