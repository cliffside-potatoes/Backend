package com.potatoes.Naengu.fridge.dto;

import com.potatoes.Naengu.fridge.dto.CreateFridgeIngredientCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

public record CreateFridgeIngredientRequest(
        @Schema(example = "1")
        @NotNull
        Long categoryId,

        @Schema(example = "10")
        @NotNull
        Long ingredientId
) {

    public CreateFridgeIngredientCommand toCommand() {
        return new CreateFridgeIngredientCommand(categoryId, ingredientId);
    }
}
