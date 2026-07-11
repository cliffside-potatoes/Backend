package com.potatoes.Naengu.recipe.repository;

import com.potatoes.Naengu.recipe.domain.model.RecipeStep;
import com.potatoes.Naengu.recipe.domain.model.RecipeWithText;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeStepRepository extends JpaRepository<RecipeStep, Long> {

    List<RecipeStep> findByRecipeWithTextOrderByStepOrderAsc(RecipeWithText recipeWithText);
}
