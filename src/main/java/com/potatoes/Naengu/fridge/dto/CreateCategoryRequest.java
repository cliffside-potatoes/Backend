package com.potatoes.Naengu.fridge.dto;

import com.potatoes.Naengu.fridge.dto.CreateCategoryCommand;
import com.potatoes.Naengu.fridge.domain.vo.CategoryColor;
import com.potatoes.Naengu.fridge.domain.vo.StorageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private StorageType storageType;

    @NotBlank
    @Size(min = 1, max = 20)
    private String name;

    @NotNull
    private CategoryColor color;

    public CreateCategoryCommand toCommand() {
        return new CreateCategoryCommand(
                storageType,
                name,
                color
        );
    }
}
