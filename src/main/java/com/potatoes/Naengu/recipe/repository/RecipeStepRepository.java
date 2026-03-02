package com.potatoes.Naengu.recipe.repository;

import com.potatoes.Naengu.recipe.domain.model.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeStepRepository extends JpaRepository<RecipeStep, Long> {

}
