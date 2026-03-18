package com.potatoes.Naengu.fridge.dto.query;

import com.potatoes.Naengu.fridge.domain.vo.CategoryColor;
import java.util.List;

public record FridgeCategoryResponse(
        Long categoryId,
        String categoryName,
        CategoryColor color,
        int categoryOrder,
        List<FridgeIngredientResponse> ingredients
) {}
