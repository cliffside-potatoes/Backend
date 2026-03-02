package com.potatoes.Naengu.fridge.dto;

import com.potatoes.Naengu.fridge.dto.UpdateFridgeIngredientCommand;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateFridgeIngredientRequest {

    private Long categoryId;

    private Long ingredientId;

    public UpdateFridgeIngredientCommand toCommand(Long fridgeIngredientId) {
        return new UpdateFridgeIngredientCommand(
                fridgeIngredientId,
                categoryId,
                ingredientId
        );
    }

}
