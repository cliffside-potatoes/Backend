package com.potatoes.Naengu.recipe.dto;

import java.util.List;

public record RecipeWithTextDto(
        List<RecipeStepDto> recipeSteps
) {}
