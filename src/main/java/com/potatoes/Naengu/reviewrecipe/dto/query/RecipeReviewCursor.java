package com.potatoes.Naengu.reviewrecipe.dto.query;

import static com.potatoes.Naengu.reviewrecipe.exception.RecipeReviewErrorCode.INVALID_CURSOR;

import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.reviewrecipe.domain.model.RecipeReview;
import com.potatoes.Naengu.reviewrecipe.domain.vo.ReviewSortType;
import java.time.LocalDateTime;

public record RecipeReviewCursor(
        Integer likeCount,
        LocalDateTime updatedAt,
        Long id
) {

    public boolean isFirstPage() {
        return likeCount == null && updatedAt == null && id == null;
    }

    public void validate(ReviewSortType sort) {
        if (sort == ReviewSortType.LATEST) {
            boolean invalid = (updatedAt == null) != (id == null);
            if (invalid) {
                throw new IllegalArgumentException("cursorUpdatedAt 또는 cursorId 중 하나만 전달될 수 없습니다. 두 값은 함께 전달되어야 합니다.");
            }
            return;
        }

        if (sort == ReviewSortType.LIKE) {
            boolean allNull = likeCount == null && updatedAt == null && id == null;
            boolean allNotNull = likeCount != null && updatedAt != null && id != null;

            if (!(allNull || allNotNull)) {
                throw new IllegalArgumentException("좋아요순 커서는 cursorLikeCount, cursorUpdatedAt, cursorId를 함께 전달해야 합니다.");
            }
        }
    }

    public static RecipeReviewCursor from(RecipeReview review) {
        return new RecipeReviewCursor(
                review.getLikeCount(),
                review.getUpdatedAt(),
                review.getId()
        );
    }
}
