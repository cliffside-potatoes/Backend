package com.potatoes.Naengu.ingredients.fridge.ingredient.command.command;

public record CreateFridgeIngredientCommand(
        Long categoryId,
        Long ingredientId
) {
}
