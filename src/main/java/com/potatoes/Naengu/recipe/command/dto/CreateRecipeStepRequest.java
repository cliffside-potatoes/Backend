package com.potatoes.Naengu.recipe.command.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreateRecipeStepRequest(
        @Min(1)
        int stepOrder,
        @NotBlank
        String content
) {}
