package com.potatoes.Naengu.fridge.domain.model;

import com.potatoes.Naengu.ingredient.domain.model.Ingredient;
import com.potatoes.Naengu.fridge.domain.model.FridgeCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "fridge_ingredient")
public class FridgeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
            FridgeCategory fridgeCategory,
            Ingredient ingredient
    ) {
        this.fridgeCategory = fridgeCategory;
        this.ingredient = ingredient;

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static FridgeIngredient create(FridgeCategory fridgeCategory, Ingredient ingredient) {
        return new FridgeIngredient(fridgeCategory, ingredient);
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
