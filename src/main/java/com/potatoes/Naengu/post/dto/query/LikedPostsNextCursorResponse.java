package com.potatoes.Naengu.post.dto.query;

import java.time.LocalDateTime;

public record LikedPostsNextCursorResponse(
        LocalDateTime cursorLikedAt,
        Long cursorId
) {
    public static LikedPostsNextCursorResponse from(LikedPostsCursor cursor) {
        if (cursor == null) {
            return null;
        }

        return new LikedPostsNextCursorResponse(
                cursor.likedAt(),
                cursor.id()
        );
    }
}
