package com.potatoes.Naengu.recipe.dto;

import java.util.List;

public record RecipeSearchItemResponse(
        Long id,
        String title,
        String thumbnailUrl,
        List<String> tags,
        int likeCount,
        int reviewCount,
        int matchedIngredientCount,
        String createdAt
) {}
