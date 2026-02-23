package com.potatoes.Naengu.post.dto;

import java.time.LocalDateTime;

public record PostFeedQuery (
        Integer size,
        String cursorCreatedAt,
        Long cursorId,
        PostFeedSort sort
){

    public int sizeOrDefault() {
        return (size == null || size <= 0) ? 20 : Math.min(size, 50);
    }

    public PostFeedSort sortOrDefault() {
        return (sort == null) ? PostFeedSort.LATEST : sort;
    }

    public LocalDateTime cursorCreatedAtAsDateTime() {
        return (cursorCreatedAt == null) ? null : LocalDateTime.parse(cursorCreatedAt);
    }

    public void validate() {
        // 현재는 LATEST만 지원
        if (sortOrDefault() != PostFeedSort.LATEST) {
            throw new IllegalArgumentException("현재는 sort=LATEST만 지원합니다.");
        }

        // 커서 규칙: cursorCreatedAt + cursorId 같이 와야 함
        boolean onlyOne = (cursorCreatedAt == null) ^ (cursorId == null);
        if (onlyOne) {
            throw new IllegalArgumentException("cursorCreatedAt과 cursorId는 함께 와야 합니다.");
        }
    }
}
