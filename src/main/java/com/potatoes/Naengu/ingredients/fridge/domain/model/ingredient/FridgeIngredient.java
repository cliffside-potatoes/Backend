package com.potatoes.Naengu.ingredients.fridge.domain.model.ingredient;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "fridge_ingredient",
        indexes = {
                @Index(
                        name = "idx_fridge_ingredient_fridge",
                        columnList = "fridge_id, is_deleted"
                ),
                @Index(
                        name = "idx_fridge_ingredient_unique",
                        columnList = "fridge_id, fridge_category_id, ingredient_id, is_deleted"
                )
        }
)
public class FridgeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fridge_id", nullable = false)
    private Long fridgeId;

    @Column(name = "fridge_category_id", nullable = false)
    private Long fridgeCategoryId;

    @Column(name = "ingredient_id", nullable = false)
    private Long ingredientId;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static FridgeIngredient create(Long fridgeId, Long categoryId, Long ingredientId) {
        FridgeIngredient entity = new FridgeIngredient();
        entity.fridgeId = fridgeId;
        entity.fridgeCategoryId = categoryId;
        entity.ingredientId = ingredientId;
        entity.createdAt = LocalDateTime.now();
        entity.updatedAt = LocalDateTime.now();
        return entity;
    }


}
