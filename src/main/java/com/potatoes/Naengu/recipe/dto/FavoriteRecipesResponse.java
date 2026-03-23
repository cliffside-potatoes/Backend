package com.potatoes.Naengu.recipe.dto;

import com.potatoes.Naengu.global.dto.CursorResponse;
import java.util.List;

public record FavoriteRecipesResponse(
        List<FavoriteRecipeItem> Recipes,
        boolean hasNext,
        CursorResponse nextCursor
) {}
