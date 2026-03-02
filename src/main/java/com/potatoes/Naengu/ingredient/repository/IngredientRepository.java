package com.potatoes.Naengu.ingredient.repository;

import com.potatoes.Naengu.ingredient.domain.model.Ingredient;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByName(String name);

}
