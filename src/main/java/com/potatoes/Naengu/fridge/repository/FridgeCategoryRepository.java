package com.potatoes.Naengu.fridge.repository;

import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.fridge.domain.model.FridgeCategory;
import com.potatoes.Naengu.fridge.domain.vo.StorageType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    List<FridgeCategory> findAllByFridge(Fridge fridge);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            DELETE FROM fridge_category
            WHERE fridge_id IN (
                SELECT f.id FROM fridge f
                INNER JOIN profile pr ON pr.fridge_id = f.id
                INNER JOIN user_entity u ON pr.user_id = u.id
                WHERE u.provider_id = :providerId
            )
            """, nativeQuery = true)
    void hardDeleteAllByProviderId(@Param("providerId") Long providerId);
}
