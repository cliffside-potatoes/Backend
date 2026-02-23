package com.potatoes.Naengu.recipe.command.dto;

import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreateRecipeWithLinkRequest(
        @NotBlank
        String url,
        @NotBlank
        String urlSource
) {}
