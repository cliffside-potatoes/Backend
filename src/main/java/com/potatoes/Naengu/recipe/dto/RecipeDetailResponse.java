package com.potatoes.Naengu.recipe.dto;

public record RecipeDetailResponse(
        RecipeDetailDto Recipe,
        MatchingDto Matching
) {}
