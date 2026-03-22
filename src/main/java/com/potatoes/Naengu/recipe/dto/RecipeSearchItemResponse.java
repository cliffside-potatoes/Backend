package com.potatoes.Naengu.recipe.dto;

public record RecipeSearchItemResponse(
        Long id,
        String title,
        String thumbnailUrl,
        String source,
        int cookingTime,
        int servings,
        String difficulty,
        int likeCount,
        int reviewCount,
        int totalIngredientCount,
        int matchedIngredientCount,
        boolean liked
) {}
