package com.potatoes.Naengu.post.dto;

import com.potatoes.Naengu.global.dto.CursorResponse;
import java.util.List;

public record MyFeedResponse(
        List<MyFeedItemResponse> items,
        boolean hasNext,
        CursorResponse nextCursor
) {}
