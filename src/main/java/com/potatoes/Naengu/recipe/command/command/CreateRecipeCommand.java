package com.potatoes.Naengu.recipe.command.command;

import com.potatoes.Naengu.recipe.domain.exception.RecipeTypeMismatchException;
import com.potatoes.Naengu.recipe.domain.vo.Difficulty;
import com.potatoes.Naengu.recipe.domain.vo.RecipeType;
import java.util.List;

public record CreateRecipeCommand(
        String title,
        Difficulty difficulty,
        int servings,
        int cookingTime,
        String description,
        CreateRecipeImageCommand recipeImage,
        RecipeType type,
        List<String> ingredients,
        List<String> tags,
        CreateRecipeWithTextCommand recipeWithText,
        CreateRecipeWithLinkCommand recipeWithLink
) {
    public CreateRecipeCommand {
        ingredients = safeCopy(ingredients);
        tags = safeCopy(tags);

        validateTypeAndPayload(type, recipeWithText, recipeWithLink);
    }

    private static List<String> safeCopy(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return List.copyOf(values);
    }

    public boolean isTextType() {
        return type == RecipeType.TEXT;
    }

    private static void validateTypeAndPayload(
            RecipeType type,
            CreateRecipeWithTextCommand text,
            CreateRecipeWithLinkCommand link
    ) {
        if (type == null) {
            throw new RecipeTypeMismatchException("recipe.type must not be null");
        }

        if (type == RecipeType.TEXT) {
            validateTextPayload(text, link);
            return;
        }

        if (type == RecipeType.LINK) {
            validateLinkPayload(text, link);
            return;
        }

        throw new RecipeTypeMismatchException("recipe.type is unsupported: " + type);
    }

    private static void validateTextPayload(
            CreateRecipeWithTextCommand text,
            CreateRecipeWithLinkCommand link
    ) {
        if (text == null) {
            throw new RecipeTypeMismatchException("recipe.type TEXT requires recipeWithText");
        }
        if (link != null) {
            throw new RecipeTypeMismatchException("recipe.type TEXT must not include recipeWithLink");
        }
    }

    private static void validateLinkPayload(
            CreateRecipeWithTextCommand text,
            CreateRecipeWithLinkCommand link
    ) {
        if (link == null) {
            throw new RecipeTypeMismatchException("recipe.type LINK requires recipeWithLink");
        }

        if (text != null) {
            throw new RecipeTypeMismatchException("recipe.type LINK must not include recipeWithText");
        }
    }


}
