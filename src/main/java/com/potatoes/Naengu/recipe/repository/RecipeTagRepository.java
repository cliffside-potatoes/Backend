package com.potatoes.Naengu.recipe.repository;

import com.potatoes.Naengu.recipe.domain.model.RecipeTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeTagRepository extends JpaRepository<RecipeTag, Long> {
}
