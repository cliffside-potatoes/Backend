package com.potatoes.Naengu.recipe.dto;

public record RecipeSearchRequest(
        int size,
        String keyword,
        String cursorCreatedAt,
        Long cursorId,
        String sort
) {}
