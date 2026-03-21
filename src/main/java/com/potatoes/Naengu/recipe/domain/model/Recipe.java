package com.potatoes.Naengu.recipe.domain.model;

import com.potatoes.Naengu.recipe.domain.vo.Difficulty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DiscriminatorOptions;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "recipe_type")
@DiscriminatorOptions(force = true)
public abstract class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false)
    private int servings;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private Difficulty difficulty;

    @Column(name = "cooking_time", nullable = false)
    private int cookingTime;

    @Column(nullable = false)
    private String description;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_image_id", nullable = false)
    private RecipeImage recipeImage;


    protected final void initBase(
            String title,
            int servings,
            Difficulty difficulty,
            int cookingTime,
            String description,
            RecipeImage recipeImage
    ) {
        validateBase(title, servings, difficulty,cookingTime, description,recipeImage);
        this.title = title.trim();
        this.servings = servings;
        this.difficulty = difficulty;
        this.cookingTime = cookingTime;
        this.description = description.trim();
        this.recipeImage = recipeImage;
    }

    private void validateBase(
            String title,
            int servings,
            Difficulty difficulty,
            int cookingTime,
            String description,
            RecipeImage recipeImage
    ) {
        validateTitle(title);
        validateServings(servings);
        validateDifficulty(difficulty);
        validateCookingTime(cookingTime);
        validateDescription(description);
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("recipe.title must not be blank");
        }
        if (title.trim().length() > 50) {
            throw new IllegalArgumentException("recipe.title must be <= 50 chars");
        }
    }

    private void validateServings(int servings) {
        if (servings <= 0) {
            throw new IllegalArgumentException("recipe.servings must be > 0");
        }
    }

    private void validateDifficulty(Difficulty difficulty) {
        if (difficulty == null) {
            throw new IllegalArgumentException("recipe.difficulty must not be null");
        }
    }

    private void validateCookingTime(int cookingTime) {
        if (cookingTime <= 0) {
            throw new IllegalArgumentException("recipe.cookingTime must be > 0");
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("recipe.description must not be blank");
        }
    }

}
