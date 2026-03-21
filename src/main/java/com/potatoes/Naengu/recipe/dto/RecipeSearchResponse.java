package com.potatoes.Naengu.recipe.dto;

import com.potatoes.Naengu.global.dto.CursorResponse;
import java.util.List;

public record RecipeSearchResponse(
        List<RecipeSearchItemResponse> items,
        boolean hasNext,
        CursorResponse nextCursor
) {}
