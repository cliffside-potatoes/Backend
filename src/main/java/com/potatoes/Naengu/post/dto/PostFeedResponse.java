package com.potatoes.Naengu.post.dto;

import java.util.List;

public record PostFeedResponse (
        List<PostItem> items,
        boolean hasNext,
        NextCursor nextCursor
){
    public record NextCursor(String cursorCreatedAt, Long cursorId) {}

    public record Writer(
            Long profileId,
            String nickname,
            String profileImageUrl
    ) {}

    public record PostItem(
            Long id,
            List<String> images,
            String content,
            Writer writer,
            Integer likeCount,
            Boolean hideLikeCount,
            Boolean liked,
            Boolean isMine,
            String updatedAt,
            String createdAt
    ) {}

}
