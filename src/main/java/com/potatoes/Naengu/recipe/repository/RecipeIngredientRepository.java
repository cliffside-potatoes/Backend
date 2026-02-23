package com.potatoes.Naengu.recipe.repository;

import com.potatoes.Naengu.recipe.domain.model.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {
}
