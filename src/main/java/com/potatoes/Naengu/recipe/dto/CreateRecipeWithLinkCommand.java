package com.potatoes.Naengu.recipe.dto;

public record CreateRecipeWithLinkCommand(
        String url,
        String urlSource
) {}
