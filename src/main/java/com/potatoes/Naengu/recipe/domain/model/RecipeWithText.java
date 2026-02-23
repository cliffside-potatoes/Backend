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
            String thumbnailImage
    ) {
        initBase(title, servings, difficulty, cookingTime, description, thumbnailImage);
    }

    public static RecipeWithText of(
            String title,
            int servings,
            Difficulty difficulty,
            int cookingTime,
            String description,
            String thumbnailImage
    ) {
        return new RecipeWithText(title, servings, difficulty, cookingTime, description, thumbnailImage);
    }

}
