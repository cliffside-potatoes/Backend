package com.potatoes.Naengu.ingredients.fridge.ingredient.command.dto;

import com.potatoes.Naengu.ingredients.fridge.ingredient.command.command.CreateFridgeIngredientCommand;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreateFridgeIngredientRequest(
        @NotNull
        Long categoryId,

        @NotNull
        Long ingredientId
) {

    public CreateFridgeIngredientCommand toCommand() {
        return new CreateFridgeIngredientCommand(categoryId, ingredientId);
    }
}
