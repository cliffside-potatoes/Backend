package com.potatoes.Naengu.recipe.command.command;

public record CreateRecipeWithLinkCommand(
        String url,
        String urlSource
) {}
