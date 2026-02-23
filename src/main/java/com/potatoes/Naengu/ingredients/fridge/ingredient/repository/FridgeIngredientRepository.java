package com.potatoes.Naengu.ingredients.fridge.ingredient.repository;

import com.potatoes.Naengu.ingredients.fridge.domain.model.ingredient.FridgeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FridgeIngredientRepository extends JpaRepository<FridgeIngredient, Long> {

    boolean existsByFridgeIdAndFridgeCategory_IdAndIngredient_Id(
            Long fridgeId,
            Long fridgeCategoryId,
            Long ingredientId
    );

    boolean existsByFridgeIdAndFridgeCategory_IdAndIngredient_IdAndIdNot(
            Long fridgeId,
            Long fridgeCategoryId,
            Long ingredientId,
            Long id
    );


}
