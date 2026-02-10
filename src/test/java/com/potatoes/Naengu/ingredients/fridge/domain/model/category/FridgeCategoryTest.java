package com.potatoes.Naengu.ingredients.fridge.domain.model.category;

import static org.assertj.core.api.Assertions.*;

import com.potatoes.Naengu.ingredients.fridge.domain.vo.CategoryColor;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.StorageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FridgeCategoryTest {

    @Test
    @DisplayName("카테고리를 생성하면 전달된 값으로 필드가 초기화된다")
    void create_success() {
        Long fridgeId = 1L;
        String name = "고기";
        int orderIndex = 3;
        StorageType storageType = StorageType.REFRIGERATED;
        CategoryColor color = CategoryColor.RED;

        FridgeCategory category = FridgeCategory.create(fridgeId, name, orderIndex, storageType, color);

        assertThat(category.getId()).isNull();
        assertThat(category.getFridgeId()).isEqualTo(fridgeId);
        assertThat(category.getName()).isEqualTo(name);
        assertThat(category.getOrderIndex()).isEqualTo(orderIndex);
        assertThat(category.getStorageType()).isEqualTo(storageType);
        assertThat(category.getColor()).isEqualTo(color);
    }
}