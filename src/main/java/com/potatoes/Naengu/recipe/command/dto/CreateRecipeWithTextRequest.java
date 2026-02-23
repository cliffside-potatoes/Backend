package com.potatoes.Naengu.recipe.command.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreateRecipeWithTextRequest(
        @NotNull
        @Size(min = 1)
        List<@Valid CreateRecipeStepRequest> steps

) {}
