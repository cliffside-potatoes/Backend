package com.potatoes.Naengu.recipe.command.dto;

import jakarta.validation.constraints.NotBlank;


public record CreateRecipeWithLinkRequest(
        @NotBlank
        String url,
        @NotBlank
        String urlSource
) {}
