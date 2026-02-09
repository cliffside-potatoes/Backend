package com.potatoes.Naengu.ingredients.dictionary.ingredient.repository;

import com.potatoes.Naengu.ingredients.dictionary.ingredient.domain.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

}
