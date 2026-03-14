package com.potatoes.Naengu.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record WriterResponse(
        @Schema(example = "3")
        Long profileId,

        @Schema(example = "다빈")
        String nickname,

        @Schema(example = "https://s3.ap-northeast-2.amazonaws.com/bucket/public/profile/uuid.png")
        String profileImageUrl
) {}
