package com.potatoes.Naengu.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PostImageRequest (
        @Schema(example = "posts/images/sample.jpg")
        @NotBlank
        String s3Key,

        @Schema(example = "image/jpeg")
        @NotBlank
        String contentType,

        @Schema(example = "204800")
        @NotNull
        @Positive
        Long size,

        @Schema(example = "public")
        @NotBlank
        String accessType
){
}
