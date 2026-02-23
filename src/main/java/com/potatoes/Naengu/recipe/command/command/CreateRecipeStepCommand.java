package com.potatoes.Naengu.recipe.command.command;

public record CreateRecipeStepCommand(
        int stepOrder,
        String content
) {}
