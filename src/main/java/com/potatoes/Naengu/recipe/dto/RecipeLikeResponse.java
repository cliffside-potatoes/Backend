package com.potatoes.Naengu.recipe.dto;

import com.potatoes.Naengu.global.dto.LikeCountCursorResponse;

import java.util.List;

public record RecipeLikeResponse(
        List<RecipeSearchItemResponse> items,
        boolean hasNext,
        LikeCountCursorResponse nextCursor
) {}
