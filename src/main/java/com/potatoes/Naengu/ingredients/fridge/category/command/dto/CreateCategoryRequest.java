package com.potatoes.Naengu.ingredients.fridge.category.command.dto;

import com.potatoes.Naengu.ingredients.fridge.category.command.command.CreateCategoryCommand;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.CategoryColor;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.StorageType;
import com.potatoes.Naengu.ingredients.shared.validation.EnumValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateCategoryRequest {

    @NotBlank
    @EnumValue(enumClass = StorageType.class, ignoreCase = true)
    private String storageType;

    @NotBlank
    @Size(min = 1, max = 20)
    private String name;

    @NotBlank
    @EnumValue(enumClass = CategoryColor.class, ignoreCase = true)
    private String color;

    public CreateCategoryCommand toCommand() {
        return new CreateCategoryCommand(
                StorageType.valueOf(storageType.trim().toUpperCase()),
                name,
                CategoryColor.valueOf(color.trim().toUpperCase())
        );
    }
}
