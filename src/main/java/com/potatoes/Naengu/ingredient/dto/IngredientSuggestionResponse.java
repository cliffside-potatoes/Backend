package com.potatoes.Naengu.ingredient.dto;

import java.util.List;

public record IngredientSuggestionResponse(
        List<IngredientSuggestionItemResponse> items
) {
    public static IngredientSuggestionResponse from(
            List<IngredientSuggestionItemResponse> items
    ) {
        return new IngredientSuggestionResponse(items);
    }
}
