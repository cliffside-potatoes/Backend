package com.potatoes.Naengu.recipe.domain.model;

import com.potatoes.Naengu.ingredient.domain.model.Ingredient;
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
        name = "recipe_ingredient",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_recipe_ingredient_recipe_ingredient",
                        columnNames = {"recipe_id", "ingredient_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_recipe_ingredient_recipe_id",
                        columnList = "recipe_id"
                ),
                @Index(
                        name = "idx_recipe_ingredient_ingredient_id",
                        columnList = "ingredient_id"
                )
        }
)
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipe_id",nullable = false)
    private Recipe recipe;

    @ManyToOne
    @JoinColumn(name = "ingredient_id",nullable = false)
    private Ingredient ingredient;

    @Column(nullable = true)
    private String amount;

    private RecipeIngredient(Recipe recipe, Ingredient ingredient) {
        validate(recipe, ingredient);
        this.recipe = recipe;
        this.ingredient = ingredient;
    }

    public static RecipeIngredient link(Recipe recipe, Ingredient ingredient) {
        return new RecipeIngredient(recipe, ingredient);
    }

    private static void validate(Recipe recipe, Ingredient ingredient) {
        if (recipe == null) {
            throw new IllegalArgumentException("recipeIngredient.recipe must not be null");
        }
        if (ingredient == null) {
            throw new IllegalArgumentException("recipeIngredient.ingredient must not be null");
        }
    }
}
