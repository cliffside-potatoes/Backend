package com.potatoes.Naengu.fridge.repository;

import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.fridge.domain.model.FridgeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FridgeIngredientRepository extends JpaRepository<FridgeIngredient, Long> {

    boolean existsByFridgeCategory_IdAndIngredient_Id(Long fridgeCategoryId, Long ingredientId);

    boolean existsByFridgeCategory_IdAndIngredient_IdAndIdNot(Long fridgeCategoryId, Long ingredientId, Long id);

    List<FridgeIngredient> findAllByFridgeCategory_Fridge(Fridge fridge);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            DELETE FROM fridge_ingredient
            WHERE fridge_category_id IN (
                SELECT fc.id FROM fridge_category fc
                INNER JOIN fridge f ON fc.fridge_id = f.id
                INNER JOIN profile pr ON pr.fridge_id = f.id
                INNER JOIN user_entity u ON pr.user_id = u.id
                WHERE u.provider_id = :providerId
            )
            """, nativeQuery = true)
    void hardDeleteAllByProviderId(@Param("providerId") Long providerId);
}
