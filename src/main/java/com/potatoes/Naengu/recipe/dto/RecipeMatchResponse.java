package com.potatoes.Naengu.recipe.dto;

import com.potatoes.Naengu.global.dto.MatchCountCursorResponse;

import java.util.List;

public record RecipeMatchResponse(
        List<RecipeSearchItemResponse> items,
        boolean hasNext,
        MatchCountCursorResponse nextCursor
) {}
