package com.potatoes.Naengu.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record MyFeedItemResponse(
        @Schema(example = "101")
        Long id,

        List<String> images,

        @Schema(example = "대파는 이렇게 보관하면 오래 갑니다.")
        String content,

        @Schema(example = "12")
        Integer likeCount,

        @Schema(example = "false")
        boolean hideLikeCount,

        @Schema(example = "2026-01-19T12:30:00")
        String updatedAt,

        @Schema(example = "2026-01-19T12:30:00")
        String createdAt
) {}
