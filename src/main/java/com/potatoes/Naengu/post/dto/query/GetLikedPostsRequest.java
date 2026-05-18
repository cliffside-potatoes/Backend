package com.potatoes.Naengu.post.dto.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public record GetLikedPostsRequest(

        @Min(1)
        @Max(100)
        Integer size,

        @DateTimeFormat(iso = ISO.DATE_TIME)
        LocalDateTime cursorLikedAt,

        Long cursorId

) {

    private static final int DEFAULT_SIZE = 20;

    public int normalizedSize() {
        return size == null ? DEFAULT_SIZE : size;
    }

    public LikedPostsCursor toCursor() {
        LikedPostsCursor cursor = new LikedPostsCursor(cursorLikedAt, cursorId);
        cursor.validate();
        return cursor;
    }
}
