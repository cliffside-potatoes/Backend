package com.potatoes.Naengu.ingredients.fridge.domain.model.ingredient;

import com.potatoes.Naengu.ingredients.dictionary.ingredient.domain.Ingredient;
import com.potatoes.Naengu.ingredients.fridge.domain.model.category.FridgeCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SoftDelete;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@SoftDelete
@Table(
        name = "fridge_ingredient",
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

    @ManyToOne
    @JoinColumn(name = "fridge_category_id", nullable = false)
    private FridgeCategory fridgeCategory;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "created_at", nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private FridgeIngredient(
            Long fridgeId,
            FridgeCategory fridgeCategory,
            Ingredient ingredient
    ) {
        this.fridgeId = fridgeId;
        this.fridgeCategory = fridgeCategory;
        this.ingredient = ingredient;

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static FridgeIngredient create(Long fridgeId, FridgeCategory fridgeCategory, Ingredient ingredient) {
        return new FridgeIngredient(fridgeId, fridgeCategory, ingredient);
    }

    public void update(FridgeCategory newFridgeCategory, Ingredient newIngredient) {
        if (newFridgeCategory != null) {
            this.fridgeCategory = newFridgeCategory;
        }
        if (newIngredient != null) {
            this.ingredient = newIngredient;
        }
        this.updatedAt = LocalDateTime.now();
    }

}
