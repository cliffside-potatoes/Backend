package com.potatoes.Naengu.fridge.dto;

import com.potatoes.Naengu.fridge.dto.UpdateCategoryCommand;
import com.potatoes.Naengu.fridge.domain.vo.CategoryColor;
import com.potatoes.Naengu.fridge.domain.vo.StorageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@NoArgsConstructor
public class UpdateCategoryRequest {

    private StorageType storageType;

    @Schema(example = "야채")
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
