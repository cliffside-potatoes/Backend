package com.potatoes.Naengu.fridge.dto;

import com.potatoes.Naengu.fridge.dto.UpdateFridgeIngredientCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@NoArgsConstructor
public class UpdateFridgeIngredientRequest {

    @Schema(example = "1")
    private Long categoryId;

    @Schema(example = "12")
    private Long ingredientId;

    public UpdateFridgeIngredientCommand toCommand(Long fridgeIngredientId) {
        return new UpdateFridgeIngredientCommand(
                fridgeIngredientId,
                categoryId,
                ingredientId
        );
    }

}
