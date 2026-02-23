package com.potatoes.Naengu.ingredients.dictionary.ingredient.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "ingredient_alias",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ingredient_alias_alias_name",
                        columnNames = "alias_name"
                )
        },
        indexes = {
                @Index(
                        name = "idx_ingredient_alias_ingredient_id",
                        columnList = "ingredient_id"
                )
        }
)
public class IngredientAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alias_name", nullable = false, length = 50)
    private String aliasName;

    @ManyToOne
    @JoinColumn(name = "ingredient_id",nullable = false)
    private Ingredient ingredient;

    private IngredientAlias(String aliasName, Ingredient ingredient) {
        validate(aliasName, ingredient);
        this.aliasName = normalize(aliasName);
        this.ingredient = ingredient;
    }

    public static IngredientAlias of(String aliasName, Ingredient ingredient) {
        return new IngredientAlias(aliasName, ingredient);
    }

    private static void validate(String aliasName, Ingredient ingredient) {
        if (aliasName == null || aliasName.isBlank()) {
            throw new IllegalArgumentException("ingredientAlias.aliasName must not be blank");
        }
        if (aliasName.trim().length() > 50) {
            throw new IllegalArgumentException("ingredientAlias.aliasName must be <= 50 chars");
        }
        if (ingredient == null) {
            throw new IllegalArgumentException("ingredientAlias.ingredient must not be null");
        }
    }

    private static String normalize(String aliasName) {
        return aliasName.trim();
    }

}
