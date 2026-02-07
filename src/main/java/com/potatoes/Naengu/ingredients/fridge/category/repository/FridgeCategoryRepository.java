package com.potatoes.Naengu.ingredients.fridge.category.repository;

import com.potatoes.Naengu.ingredients.fridge.domain.model.category.FridgeCategory;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.StorageType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FridgeCategoryRepository extends JpaRepository<FridgeCategory, Long> {

    Optional<FridgeCategory> findByIdAndDeletedFalse(Long id);

    boolean existsByFridgeIdAndStorageTypeAndNameAndDeletedFalse(
            Long fridgeId,
            StorageType storageType,
            String name
    );

    boolean existsByFridgeIdAndStorageTypeAndNameAndIdNotAndDeletedFalse(
            Long fridgeId,
            StorageType storageType,
            String name,
            Long id
    );

    @Query("""
             select coalesce(max(c.orderIndex), 0)
             from FridgeCategory c
             where c.fridgeId = :fridgeId
               and c.storageType = :storageType
               and c.deleted = false
            """)
    int findMaxOrderIndexByFridgeIdAndStorageType(Long fridgeId, StorageType storageType);
}
