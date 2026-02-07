package com.potatoes.Naengu.ingredients.fridge.domain.model.category;

import com.potatoes.Naengu.ingredients.fridge.domain.vo.CategoryColor;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.StorageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "fridge_category",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fridge_category_fridge_storage_name_deleted",
                        columnNames = {"fridge_id", "storage_type", "name","deleted"}
                )
        }
)
public class FridgeCategory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fridge_id", nullable = false)
    private Long fridgeId;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false)
    private StorageType storageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "color", nullable = false)
    private CategoryColor color;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static FridgeCategory create(
            Long fridgeId,
            String name,
            int orderIndex,
            StorageType storageType,
            CategoryColor color
    ) {
        FridgeCategory category = new FridgeCategory();
        category.fridgeId = fridgeId;
        category.name = name;
        category.orderIndex = orderIndex;
        category.storageType = storageType;
        category.color = color;
        category.deleted = false;
        category.deletedAt = null;
        return category;
    }

    public void update(StorageType newStorageType, String newName, CategoryColor newColor) {
        if (newStorageType != null) this.storageType = newStorageType;
        if (newName != null) this.name = newName;
        if (newColor != null) this.color = newColor;
    }

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }


}
