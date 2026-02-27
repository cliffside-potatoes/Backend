package com.potatoes.Naengu.recipe.command.dto;

import com.potatoes.Naengu.recipe.domain.vo.Difficulty;
import com.potatoes.Naengu.recipe.domain.vo.RecipeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateRecipeRequest(
        @NotBlank
        @Size(max = 50)
        String title,

        @NotNull
        Difficulty difficulty,

        @NotNull
        @Min(1)
        Integer servings,

        @NotNull
        @Min(1)
        Integer cookingTime,

        @NotBlank
        String description,

        @Valid
        RecipeImageRequest recipeImage,

        @NotNull
        RecipeType type,

        @Size(max = 20)
        List<@NotBlank String> ingredients,

        @Size(max = 5)
        List<@NotBlank String> tags,

        @Valid CreateRecipeWithTextRequest recipeWithText,
        @Valid CreateRecipeWithLinkRequest recipeWithLink
        ) { }
