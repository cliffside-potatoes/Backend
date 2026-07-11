package com.potatoes.Naengu.recipe.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;


public record CreateRecipeStepRequest(
        @Min(1)
        int stepOrder,
        @NotBlank
        String content
) {}
