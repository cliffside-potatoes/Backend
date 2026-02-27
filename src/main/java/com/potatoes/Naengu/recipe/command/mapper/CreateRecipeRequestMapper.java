package com.potatoes.Naengu.recipe.command.mapper;

import com.potatoes.Naengu.recipe.command.command.CreateRecipeCommand;
import com.potatoes.Naengu.recipe.command.command.CreateRecipeImageCommand;
import com.potatoes.Naengu.recipe.command.command.CreateRecipeWithLinkCommand;
import com.potatoes.Naengu.recipe.command.command.CreateRecipeWithTextCommand;
import com.potatoes.Naengu.recipe.command.dto.CreateRecipeRequest;
import com.potatoes.Naengu.recipe.command.dto.RecipeImageRequest;

public final class CreateRecipeRequestMapper {

    private CreateRecipeRequestMapper() {}

    public static CreateRecipeCommand toCommand(CreateRecipeRequest request) {
        return new CreateRecipeCommand(
                request.title(),
                request.difficulty(),
                request.servings(),
                request.cookingTime(),
                request.description(),
                toRecipeImageCommand(request),
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

    private static CreateRecipeImageCommand toRecipeImageCommand(CreateRecipeRequest request) {
        return new CreateRecipeImageCommand(
                request.recipeImage().s3Key(),
                request.recipeImage().contentType(),
                request.recipeImage().size(),
                request.recipeImage().accessType()
        );
    }

}
