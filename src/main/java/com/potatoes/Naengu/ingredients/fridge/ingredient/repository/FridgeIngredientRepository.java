package com.potatoes.Naengu.ingredients.fridge.ingredient.repository;

import com.potatoes.Naengu.ingredients.fridge.domain.model.ingredient.FridgeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FridgeIngredientRepository extends JpaRepository<FridgeIngredient, Long> {

    boolean existsByFridgeIdAndFridgeCategoryIdAndIngredientId(
            Long fridgeId,
            Long fridgeCategoryId,
            Long ingredientId
    );

    boolean existsByFridgeIdAndFridgeCategoryIdAndIngredientIdAndIdNot(
            Long fridgeId,
            Long fridgeCategoryId,
            Long ingredientId,
            Long id
    );


}
