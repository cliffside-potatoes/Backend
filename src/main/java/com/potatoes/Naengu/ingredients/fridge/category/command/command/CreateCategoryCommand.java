package com.potatoes.Naengu.ingredients.fridge.category.command.command;

import com.potatoes.Naengu.ingredients.fridge.domain.vo.CategoryColor;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.StorageType;

public record CreateCategoryCommand(
        StorageType storageType,
        String name,
        CategoryColor color
) {}
