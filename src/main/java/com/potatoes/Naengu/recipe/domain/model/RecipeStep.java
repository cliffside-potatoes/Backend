package com.potatoes.Naengu.recipe.domain.model;

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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "recipe_step",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_recipe_step_parent_step",
                        columnNames = {"recipe_with_text_id", "step_order"}
                )
        },
        indexes = {
                @Index(name = "idx_recipe_step_parent", columnList = "recipe_with_text_id")
        }
)
public class RecipeStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "recipe_with_text_id",nullable = false)
    private RecipeWithText recipeWithText;

    private RecipeStep(int stepOrder, String content, RecipeWithText recipeWithText) {
        validate(stepOrder, content, recipeWithText);
        this.stepOrder = stepOrder;
        this.content = content;
        this.recipeWithText = recipeWithText;
    }

    public static RecipeStep of(int stepOrder, String content, RecipeWithText recipeWithText) {
        return new RecipeStep(stepOrder, content, recipeWithText);
    }

    private static void validate(int stepOrder, String content, RecipeWithText recipeWithText) {
        if (stepOrder <= 0){
            throw new IllegalArgumentException("recipeStep.stepOrder must be > 0");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("recipeStep.content must not be blank");
        }
        if (recipeWithText == null) {
            throw new IllegalArgumentException("recipeStep.recipeWithText must not be null");
        }
    }
}
