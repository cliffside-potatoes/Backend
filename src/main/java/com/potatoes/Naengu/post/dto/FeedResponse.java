package com.potatoes.Naengu.post.dto;

import com.potatoes.Naengu.global.dto.CursorResponse;
import java.util.List;

public record FeedResponse(
        List<FeedItemResponse> items,
        boolean hasNext,
        CursorResponse nextCursor
) {}
