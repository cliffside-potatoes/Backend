package com.potatoes.Naengu.reviewrecipe.dto.query;

import java.time.LocalDateTime;

public record RecipeReviewNextCursorResponse(
        LocalDateTime cursorUpdatedAt,
        Long cursorId
) {
    public static RecipeReviewNextCursorResponse from(RecipeReviewCursor cursor) {
        if (cursor == null) {
            return null;
        }

        return new RecipeReviewNextCursorResponse(
                cursor.updatedAt(),
                cursor.id()
        );
    }
}
