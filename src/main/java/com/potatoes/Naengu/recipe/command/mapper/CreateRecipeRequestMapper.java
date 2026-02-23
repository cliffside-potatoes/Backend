package com.potatoes.Naengu.recipe.command.mapper;

import com.potatoes.Naengu.recipe.command.command.CreateRecipeCommand;
import com.potatoes.Naengu.recipe.command.command.CreateRecipeWithLinkCommand;
import com.potatoes.Naengu.recipe.command.command.CreateRecipeWithTextCommand;
import com.potatoes.Naengu.recipe.command.dto.CreateRecipeRequest;

public final class CreateRecipeRequestMapper {

    private CreateRecipeRequestMapper() {}

    public static CreateRecipeCommand toCommand(CreateRecipeRequest request) {
        return new CreateRecipeCommand(
                request.title(),
                request.difficulty(),
                request.servings(),
                request.cookingTime(),
                request.description(),
                request.thumbnailImage(),
                request.type(),
                request.ingredients(),
                request.tags(),
                toTextCommand(request),
                toLinkCommand(request)
        );
    }

    private static CreateRecipeWithTextCommand toTextCommand(CreateRecipeRequest request) {
        return CreateRecipeWithTextRequestMapper.toCommand(request.recipeWithText());
    }

    private static CreateRecipeWithLinkCommand toLinkCommand(CreateRecipeRequest request) {
        return CreateRecipeWithLinkRequestMapper.toCommand(request.recipeWithLink());
    }

}
