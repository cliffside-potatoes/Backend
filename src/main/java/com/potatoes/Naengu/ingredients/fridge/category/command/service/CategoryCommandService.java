package com.potatoes.Naengu.ingredients.fridge.category.command.service;

import static com.potatoes.Naengu.ingredients.fridge.category.command.exception.CategoryErrorCode.*;

import com.potatoes.Naengu.ingredients.fridge.category.command.command.CreateCategoryCommand;
import com.potatoes.Naengu.ingredients.fridge.category.repository.FridgeCategoryRepository;
import com.potatoes.Naengu.ingredients.fridge.domain.model.category.FridgeCategory;
import com.potatoes.Naengu.ingredients.shared.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryCommandService {

    private final FridgeCategoryRepository repository;

    public CategoryCommandService(FridgeCategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Long create(Long fridgeId,CreateCategoryCommand command) {

        boolean exists = repository.existsByFridgeIdAndStorageTypeAndName(
                fridgeId,
                command.storageType(),
                command.name()
        );

        if (exists) {
            throw new ApiException(
                    CATEGORY_DUPLICATE.code(),
                    CATEGORY_DUPLICATE.message(),
                    CATEGORY_DUPLICATE.status()
            );
        }

        int nextOrderIndex =
                repository.findMaxOrderIndexByFridgeIdAndStorageType(fridgeId,command.storageType()) + 1;

        FridgeCategory category = FridgeCategory.create(
                fridgeId,
                command.name(),
                nextOrderIndex,
                command.storageType(),
                command.color()
        );

        repository.save(category);
        return category.getId();

    }

}
