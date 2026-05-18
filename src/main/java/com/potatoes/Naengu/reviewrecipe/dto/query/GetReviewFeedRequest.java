package com.potatoes.Naengu.reviewrecipe.dto.query;

import com.potatoes.Naengu.reviewrecipe.domain.vo.ReviewSortType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@Schema(
        example = """
            {
                "sort": "LATEST"
            }
            """

)
public record GetReviewFeedRequest(
        @Min(1)
        @Max(100)
        Integer size,

        ReviewSortType sort,

        @DateTimeFormat(iso = ISO.DATE_TIME)
        LocalDateTime cursorUpdatedAt,

        Long cursorId,

        Integer cursorLikeCount
) {

    private static final int DEFAULT_SIZE = 20;

    public int normalizedSize() {
        return size == null ? DEFAULT_SIZE : size;
    }

    public ReviewSortType normalizedSort() {
        if (sort == null) {
            return ReviewSortType.LATEST;
        }
        return sort;
    }

    public RecipeReviewCursor toCursor() {
        return new RecipeReviewCursor(cursorLikeCount, cursorUpdatedAt, cursorId);
    }
}
