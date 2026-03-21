package com.potatoes.Naengu.post.dto;

import java.util.List;

public record FeedResponse(
        List<FeedItemResponse> items,
        boolean hasNext,
        CursorResponse nextCursor
) {}
