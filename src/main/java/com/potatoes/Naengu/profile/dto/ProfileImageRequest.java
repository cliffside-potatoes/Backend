package com.potatoes.Naengu.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProfileImageRequest (
        @NotBlank
        String s3Key,

        @NotBlank
        String contentType,

        @NotNull
        @Positive
        Long size,

        @NotBlank
        String accessType

){
}
