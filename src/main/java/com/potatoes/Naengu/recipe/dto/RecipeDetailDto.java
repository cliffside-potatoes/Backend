package com.potatoes.Naengu.recipe.dto;

import java.util.List;

public record RecipeDetailDto(
        String title,
        String type,
        List<String> tag,
        int likeCount,
        int reviewCount,
        String description,
        int servings,
        int cookingTime,
        String difficulty,
        RecipeWithLinkDto recipewithLink,
        RecipeWithTextDto recipewithText
) {}
