package com.potatoes.Naengu.recipe.command.dto;

import com.potatoes.Naengu.recipe.domain.vo.Difficulty;
import com.potatoes.Naengu.recipe.domain.vo.RecipeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreateRecipeRequest(
        @NotBlank
        @Size(max = 50)
        String title,

        @NotNull
        Difficulty difficulty,

        @Min(1)
        int servings,

        @Min(1)
        int cookingTime,

        @NotBlank
        String description,

        @NotBlank
        @Size(max = 300)
        String thumbnailImage,

        @NotNull
        RecipeType type,

        @Size(max = 20)
        List<@NotBlank String> ingredients,

        @Size(max = 5)
        List<@NotBlank String> tags,

        @Valid CreateRecipeWithTextRequest recipeWithText,
        @Valid CreateRecipeWithLinkRequest recipeWithLink
        ) { }
