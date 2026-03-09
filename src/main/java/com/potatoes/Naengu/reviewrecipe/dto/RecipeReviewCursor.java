package com.potatoes.Naengu.reviewrecipe.dto;

import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.reviewrecipe.domain.model.RecipeReview;
import com.potatoes.Naengu.reviewrecipe.domain.vo.ReviewSortType;
import java.time.LocalDateTime;

public record RecipeReviewCursor(
        LocalDateTime updatedAt,
        Long id
) {

    public boolean isFirstPage() {
        return updatedAt == null && id == null;
    }

    public void validate(ReviewSortType sort) {
        boolean bothNull = (updatedAt == null && id == null);
        boolean bothNotNull = (updatedAt != null && id != null);

        if (!(bothNull || bothNotNull)) {
            throw new ApiException(INVALID_CURSOR);
        }
    }

    public static RecipeReviewCursor from(RecipeReview review) {
        return new RecipeReviewCursor(
                review.getUpdatedAt(),
                review.getId()
        );
    }
}
