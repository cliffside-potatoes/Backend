package com.potatoes.Naengu.fridge.dto;

public record UpdateFridgeIngredientCommand(
        Long fridgeIngredientId,
        Long fridgeCategoryId,
        Long ingredientId
) {

    public boolean hasAnyChange() {
        return isCategoryChanged()
                || isIngredientChanged();
    }

    private boolean isCategoryChanged() {
        return fridgeCategoryId != null;
    }

    private boolean isIngredientChanged() {
        return ingredientId != null;
    }
}
