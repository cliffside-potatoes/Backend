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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SoftDelete;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@SoftDelete
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

    private FridgeCategory(
            Long fridgeId,
            String name,
            int orderIndex,
            StorageType storageType,
            CategoryColor color) {

        this.fridgeId = fridgeId;
        this.name = name;
        this.orderIndex = orderIndex;
        this.storageType = storageType;
        this.color = color;
    }

    public static FridgeCategory create(
            Long fridgeId,
            String name,
            int orderIndex,
            StorageType storageType,
            CategoryColor color
    ) {
        return new FridgeCategory(fridgeId,name,orderIndex,storageType,color);
    }

    public void update(StorageType newStorageType, String newName, CategoryColor newColor) {
        if (newStorageType != null) this.storageType = newStorageType;
        if (newName != null) this.name = newName;
        if (newColor != null) this.color = newColor;
    }

}
