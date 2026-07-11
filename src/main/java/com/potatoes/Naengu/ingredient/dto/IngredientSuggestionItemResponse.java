package com.potatoes.Naengu.ingredient.dto;

public record IngredientSuggestionItemResponse(
        Long ingredientId,
        String ingredientName
) {
    public static IngredientSuggestionItemResponse from(
            Long ingredientId,
            String ingredientName
    ) {
        return new IngredientSuggestionItemResponse(ingredientId, ingredientName);
    }

}
