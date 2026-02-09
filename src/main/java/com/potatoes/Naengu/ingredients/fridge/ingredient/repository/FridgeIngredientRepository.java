package com.potatoes.Naengu.ingredients.fridge.ingredient.repository;

import com.potatoes.Naengu.ingredients.fridge.domain.model.ingredient.FridgeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FridgeIngredientRepository extends JpaRepository<FridgeIngredient, Long> {

    boolean existsByFridgeIdAndFridgeCategoryIdAndIngredientIdAndDeletedFalse(
            Long fridgeId,
            Long fridgeCategoryId,
            Long ingredientId
    );

    boolean existsByFridgeIdAndFridgeCategoryIdAndIngredientIdAndDeletedFalseAndIdNot(
            Long fridgeId,
            Long fridgeCategoryId,
            Long ingredientId,
            Long id
    );


}
