package com.potatoes.Naengu.fridge.dto;

import com.potatoes.Naengu.fridge.domain.vo.CategoryColor;
import com.potatoes.Naengu.fridge.domain.vo.StorageType;

public record CreateCategoryCommand(
        StorageType storageType,
        String name,
        CategoryColor color
) {}
