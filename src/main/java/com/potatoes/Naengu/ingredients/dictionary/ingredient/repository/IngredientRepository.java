package com.potatoes.Naengu.ingredients.dictionary.ingredient.repository;

import com.potatoes.Naengu.ingredients.dictionary.ingredient.domain.Ingredient;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByName(String name);

}
