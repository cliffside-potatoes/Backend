package com.potatoes.Naengu.recipe.dto;

import java.util.List;

public record MatchingDto(
        List<String> fridgeIngredients,
        List<String> missingIngredients
) {}
