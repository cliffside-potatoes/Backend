package com.potatoes.Naengu.ingredients.fridge.category.command.dto;

import com.potatoes.Naengu.ingredients.fridge.category.command.command.UpdateCategoryCommand;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.CategoryColor;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.StorageType;
import com.potatoes.Naengu.ingredients.shared.validation.EnumValue;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateCategoryRequest {

    @EnumValue(enumClass = StorageType.class, ignoreCase = true)
    private String storageType;

    @Size(min = 1, max = 20)
    private String name;

    @EnumValue(enumClass = CategoryColor.class, ignoreCase = true)
    private String color;

    public UpdateCategoryCommand toCommand(Long categoryId) {
        return new UpdateCategoryCommand(
                categoryId,
                parseStorageType(),
                parseName(),
                parseColor()
        );
    }

    private StorageType parseStorageType() {
        if (storageType == null) {
            return null;
        }
        return StorageType.valueOf(storageType.trim().toUpperCase());
    }

    private String parseName() {
        if (name == null) {
            return null;
        }
        return name.trim();
    }

    private CategoryColor parseColor() {
        if (color == null) {
            return null;
        }
        return CategoryColor.valueOf(color.trim().toUpperCase());
    }


}
