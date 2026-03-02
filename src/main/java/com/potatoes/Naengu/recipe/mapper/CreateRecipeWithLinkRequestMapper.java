package com.potatoes.Naengu.recipe.mapper;

import com.potatoes.Naengu.recipe.dto.CreateRecipeWithLinkCommand;
import com.potatoes.Naengu.recipe.dto.CreateRecipeWithLinkRequest;

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
