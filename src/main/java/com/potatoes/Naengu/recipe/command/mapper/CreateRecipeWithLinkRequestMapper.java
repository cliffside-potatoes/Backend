package com.potatoes.Naengu.recipe.command.mapper;

import com.potatoes.Naengu.recipe.command.command.CreateRecipeWithLinkCommand;
import com.potatoes.Naengu.recipe.command.dto.CreateRecipeWithLinkRequest;

public final class CreateRecipeWithLinkRequestMapper {

    private CreateRecipeWithLinkRequestMapper() {}

    public static CreateRecipeWithLinkCommand toCommand(CreateRecipeWithLinkRequest request) {
        if (request == null) {
            return null;
        }
        return new CreateRecipeWithLinkCommand(
                request.url(),
                request.urlSource()
        );
    }
}
