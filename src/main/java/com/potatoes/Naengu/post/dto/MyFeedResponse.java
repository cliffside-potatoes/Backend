package com.potatoes.Naengu.post.dto;

import java.util.List;

public record MyFeedResponse(
        List<MyFeedItemResponse> items,
        boolean hasNext,
        CursorResponse nextCursor
) {}
