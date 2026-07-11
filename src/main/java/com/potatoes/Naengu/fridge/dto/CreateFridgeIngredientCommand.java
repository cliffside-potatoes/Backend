package com.potatoes.Naengu.fridge.dto;

public record CreateFridgeIngredientCommand(
        Long categoryId,
        Long ingredientId
) {
}
