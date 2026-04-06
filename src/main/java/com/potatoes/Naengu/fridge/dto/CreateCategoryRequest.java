package com.potatoes.Naengu.fridge.dto;

import com.potatoes.Naengu.fridge.domain.vo.CategoryColor;
import com.potatoes.Naengu.fridge.domain.vo.StorageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {

    @Schema(example = "REFRIGERATED")
    @NotNull
    private StorageType storageType;

    @Schema(example = "채소")
    @NotBlank
    @Size(min = 1, max = 20)
    private String name;

    @Schema(example = "COLOR_1")
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
