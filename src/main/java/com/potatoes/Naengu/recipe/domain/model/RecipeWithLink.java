package com.potatoes.Naengu.recipe.domain.model;

import com.potatoes.Naengu.recipe.domain.vo.Difficulty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "recipe_with_link")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeWithLink extends Recipe {

    @Column(nullable = false,length = 1000)
    private String url;

    @Column(name = "url_source",nullable = false,length = 100)
    private String urlSource;

    private RecipeWithLink(
            String title,
            int servings,
            Difficulty difficulty,
            int cookingTime,
            String description,
            RecipeImage recipeImage,
            String url,
            String urlSource
    ) {
        initBase(title, servings, difficulty, cookingTime, description, recipeImage);
        initLink(url, urlSource);
    }

    public static RecipeWithLink of(
            String title,
            int servings,
            Difficulty difficulty,
            int cookingTime,
            String description,
            RecipeImage recipeImage,
            String url,
            String urlSource
    ) {
        return new RecipeWithLink(title, servings, difficulty, cookingTime, description, recipeImage, url, urlSource);
    }

    private void initLink(String url, String urlSource) {
        validateLink(url, urlSource);
        this.url = url.trim();
        this.urlSource = urlSource.trim();
    }

    private void validateLink(String url, String urlSource) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("recipeWithLink.url must not be blank");
        }
        if (urlSource == null || urlSource.isBlank()) {
            throw new IllegalArgumentException("recipeWithLink.urlSource must not be blank");
        }
        if (url.trim().length() > 500) {
            throw new IllegalArgumentException("recipeWithLink.url must be <= 500 chars");
        }
        if (urlSource.trim().length() > 100) {
            throw new IllegalArgumentException("recipeWithLink.urlSource must be <= 100 chars");
        }
    }

}
