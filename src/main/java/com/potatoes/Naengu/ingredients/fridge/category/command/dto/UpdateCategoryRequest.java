package com.potatoes.Naengu.ingredients.fridge.category.command.dto;

import com.potatoes.Naengu.ingredients.fridge.category.command.command.UpdateCategoryCommand;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.CategoryColor;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.StorageType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateCategoryRequest {

    private StorageType storageType;

    @Size(min = 1, max = 20)
    private String name;

    private CategoryColor color;

    public UpdateCategoryCommand toCommand(Long categoryId) {
        return new UpdateCategoryCommand(
                categoryId,
                storageType,
                parseName(),
                color
        );
    }

    private String parseName() {
        if (name == null) {
            return null;
        }
        return name.trim();
    }
}
