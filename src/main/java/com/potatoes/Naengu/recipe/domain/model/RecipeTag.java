package com.potatoes.Naengu.recipe.domain.model;

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
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "recipe_tag",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_recipe_tag_recipe_tag",
                        columnNames = {"recipe_id", "tag_id"}
                )
        },
        indexes = {
                @Index(name = "idx_recipe_tag_recipe_id", columnList = "recipe_id"),
                @Index(name = "idx_recipe_tag_tag_id", columnList = "tag_id")
        }
)
public class RecipeTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipe_id",nullable = false)
    private Recipe recipe;

    @ManyToOne
    @JoinColumn(name = "tag_id",nullable = false)
    private Tag tag;

    private RecipeTag(Recipe recipe, Tag tag) {
        validate(recipe, tag);
        this.recipe = recipe;
        this.tag = tag;
    }

    public static RecipeTag link(Recipe recipe, Tag tag) {
        return new RecipeTag(recipe, tag);
    }

    private static void validate(Recipe recipe, Tag tag) {
        if (recipe == null) {
            throw new IllegalArgumentException("recipeTag.recipe must not be null");
        }
        if (tag == null) {
            throw new IllegalArgumentException("recipeTag.tag must not be null");
        }
    }

}
