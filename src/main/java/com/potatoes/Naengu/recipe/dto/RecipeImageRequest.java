package com.potatoes.Naengu.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RecipeImageRequest(
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
