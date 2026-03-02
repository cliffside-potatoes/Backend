package com.potatoes.Naengu.fridge.dto;

import com.potatoes.Naengu.fridge.domain.vo.CategoryColor;
import com.potatoes.Naengu.fridge.domain.vo.StorageType;

public record UpdateCategoryCommand(
        Long categoryId,
        StorageType storageType,
        String name,
        CategoryColor color
) {

    public boolean hasAnyChange() {
        return isStorageTypeChanged()
                || isNameChanged()
                || isColorChanged();
    }

    private boolean isStorageTypeChanged() {
        return storageType != null;
    }

    private boolean isNameChanged() {
        return name != null;
    }

    private boolean isColorChanged() {
        return color != null;
    }

}
