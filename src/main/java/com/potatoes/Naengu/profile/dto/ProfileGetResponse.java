package com.potatoes.Naengu.profile.dto;

import com.potatoes.Naengu.profile.domain.model.Profile;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProfileGetResponse(
        @Schema(example = "3")
        Long profileId,
        @Schema(example = "chulsoo")
        String nickname,
        @Schema(example = "안녕하세요. 철수입니다.")
        String bio,
        @Schema(example = "https://s3.ap-northeast-2.amazonaws.com/naenggu-bucket/public/profile/uuid1.png")
        String profileImageUrl
) {
    public static ProfileGetResponse from(Profile profile, String imageUrl) {
        return new ProfileGetResponse(
                profile.getId(),
                profile.getNickname(),
                profile.getBio(),
                imageUrl
        );
    }
}
