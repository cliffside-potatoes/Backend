package com.potatoes.Naengu.profile.dto;

import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.domain.model.ProfileImage;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProfileGetResponse(
        @Schema(example = "3")
        Long profileId,
        @Schema(example = "chulsoo")
        String nickname,
        @Schema(example = "안녕하세요. 철수입니다.")
        String bio,
        ProfileImageResponse profileImage
) {
    public record ProfileImageResponse(
            @Schema(example = "public/profile/uuid1.png")
            String s3Key,
            @Schema(example = "image/png")
            String contentType,
            @Schema(example = "123456")
            Long size,
            @Schema(example = "public")
            String accessType
    ) {
        public static ProfileImageResponse from(ProfileImage image) {
            return new ProfileImageResponse(
                    image.getS3Key(),
                    image.getContentType(),
                    image.getSize(),
                    image.getAccessType()
            );
        }
    }

    public static ProfileGetResponse from(Profile profile) {
        ProfileImageResponse imageResponse = profile.getProfileImage() != null
                ? ProfileImageResponse.from(profile.getProfileImage())
                : null;

        return new ProfileGetResponse(
                profile.getId(),
                profile.getNickname(),
                profile.getBio(),
                imageResponse
        );
    }
}
