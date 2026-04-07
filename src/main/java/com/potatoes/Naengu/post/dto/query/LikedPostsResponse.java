package com.potatoes.Naengu.post.dto.query;

import java.util.List;

public record LikedPostsResponse(
        List<LikedPostItemResponse> items,
        boolean hasNext,
        LikedPostsNextCursorResponse nextCursor
) {

    public static LikedPostsResponse of(
            List<LikedPostItemResponse> items,
            boolean hasNext,
            LikedPostsNextCursorResponse nextCursor
    ) {
        return new LikedPostsResponse(
                items,
                hasNext,
                nextCursor
        );
    }
}
