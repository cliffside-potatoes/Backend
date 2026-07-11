package com.potatoes.Naengu.recipe.dto;

public record CreateRecipeStepCommand(
        int stepOrder,
        String content
) {}
