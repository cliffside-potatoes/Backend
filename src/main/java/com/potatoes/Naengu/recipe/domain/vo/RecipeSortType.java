package com.potatoes.Naengu.recipe.domain.vo;

import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.recipe.exception.RecipeErrorCode;

import java.util.Arrays;

public enum RecipeSortType {
    LATEST, MATCH_COUNT, LIKE_COUNT;

    public static RecipeSortType from(String value) {
        return Arrays.stream(values())
                .filter(v -> v.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new ApiException(RecipeErrorCode.INVALID_SORT_TYPE));
    }
}
