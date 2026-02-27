package com.potatoes.Naengu.ingredients.fridge.category.repository;

import com.potatoes.Naengu.ingredients.fridge.domain.model.Fridge;
import com.potatoes.Naengu.ingredients.fridge.domain.model.category.FridgeCategory;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.StorageType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FridgeCategoryRepository extends JpaRepository<FridgeCategory, Long> {

    boolean existsByFridgeAndStorageTypeAndName(
            Fridge fridge,
            StorageType storageType,
            String name
    );

    boolean existsByFridgeAndStorageTypeAndNameAndIdNot(
            Fridge fridge,
            StorageType storageType,
            String name,
            Long id
    );

    @Query("""
             select coalesce(max(c.orderIndex), 0)
             from FridgeCategory c
             where c.fridge = :fridge
               and c.storageType = :storageType
         
            """)
    int findMaxOrderIndexByFridgeAndStorageType(Fridge fridge, StorageType storageType);

    Optional<FridgeCategory> findByIdAndFridge(Long id, Fridge fridge);
}
