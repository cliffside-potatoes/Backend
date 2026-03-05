package com.potatoes.Naengu.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProfileImageRequest (
        @Schema(example = "public/profile/uuid1.png")
        @NotBlank
        String s3Key,

        @Schema(example = "image/png")
        @NotBlank
        String contentType,

        @Schema(example = "123456")
        @NotNull
        @Positive
        Long size,

        @Schema(example = "public")
        @NotBlank
        String accessType

){
}
