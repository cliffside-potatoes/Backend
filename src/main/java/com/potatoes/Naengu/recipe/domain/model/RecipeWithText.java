package com.potatoes.Naengu.recipe.domain.model;

import com.potatoes.Naengu.recipe.domain.vo.Difficulty;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "recipe_with_text")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeWithText extends Recipe {

    private RecipeWithText(
            String title,
            int servings,
            Difficulty difficulty,
            int cookingTime,
            String description,
            RecipeImage recipeImage
    ) {
        initBase(title, servings, difficulty, cookingTime, description, recipeImage);
    }

    public static RecipeWithText of(
            String title,
            int servings,
            Difficulty difficulty,
            int cookingTime,
            String description,
            RecipeImage recipeImage
    ) {
        return new RecipeWithText(title, servings, difficulty, cookingTime, description, recipeImage);
    }

}
