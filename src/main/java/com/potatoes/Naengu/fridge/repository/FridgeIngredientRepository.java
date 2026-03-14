package com.potatoes.Naengu.fridge.repository;

import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.fridge.domain.model.FridgeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FridgeIngredientRepository extends JpaRepository<FridgeIngredient, Long> {

    boolean existsByFridgeCategory_IdAndIngredient_Id(Long fridgeCategoryId, Long ingredientId);

    boolean existsByFridgeCategory_IdAndIngredient_IdAndIdNot(Long fridgeCategoryId, Long ingredientId, Long id);

    List<FridgeIngredient> findAllByFridgeCategory_Fridge(Fridge fridge);
}
