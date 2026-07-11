package com.potatoes.Naengu.recipe.dto;

public record FavoriteRecipeItem(
        Long recipeId,
        String title,
        String thumbnailImage,
        String source,
        int cookingTime,
        String difficulty,
        int likeCount,
        int reviewCount,
        int totalIngredientCount,
        int matchedIngredientCount,
        boolean liked
) {}
