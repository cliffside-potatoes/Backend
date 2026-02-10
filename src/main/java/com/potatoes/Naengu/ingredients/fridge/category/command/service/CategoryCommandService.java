package com.potatoes.Naengu.ingredients.fridge.category.command.service;

import static com.potatoes.Naengu.ingredients.fridge.category.command.exception.CategoryErrorCode.*;

import com.potatoes.Naengu.ingredients.fridge.category.command.command.CreateCategoryCommand;
import com.potatoes.Naengu.ingredients.fridge.category.command.command.UpdateCategoryCommand;
import com.potatoes.Naengu.ingredients.fridge.category.repository.FridgeCategoryRepository;
import com.potatoes.Naengu.ingredients.fridge.domain.model.category.FridgeCategory;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.CategoryColor;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.StorageType;
import com.potatoes.Naengu.ingredients.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryCommandService {

    private final FridgeCategoryRepository repository;

    public CategoryCommandService(FridgeCategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Long create(Long fridgeId, CreateCategoryCommand command) {

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
                repository.findMaxOrderIndexByFridgeIdAndStorageType(fridgeId, command.storageType()) + 1;

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

    @Transactional
    public Long update(Long fridgeId, UpdateCategoryCommand command) {
        if (!command.hasAnyChange()) {
            throw new ApiException(
                    CATEGORY_UPDATE_EMPTY.code(),
                    CATEGORY_UPDATE_EMPTY.message(),
                    CATEGORY_UPDATE_EMPTY.status()
            );
        }

        FridgeCategory category = repository.findById(command.categoryId())
                .orElseThrow(() -> new ApiException(
                        CATEGORY_NOT_FOUND.code(),
                        CATEGORY_NOT_FOUND.message(),
                        CATEGORY_NOT_FOUND.status()
                ));

        if (!category.getFridgeId().equals(fridgeId)) {
            throw new ApiException(
                    CATEGORY_FORBIDDEN.code(),
                    CATEGORY_FORBIDDEN.message(),
                    CATEGORY_FORBIDDEN.status()
            );
        }

        StorageType targetStorageType = resolveStorageType(command, category);
        String targetName = resolveName(command, category);
        CategoryColor targetColor = resolveColor(command, category);

        if (command.name() != null && targetName.isBlank()) {
            throw new ApiException(
                    "VALIDATION_ERROR",
                    "name은 공백일 수 없습니다.",
                    HttpStatus.BAD_REQUEST
            );
        }

        boolean duplicate = repository.existsByFridgeIdAndStorageTypeAndNameAndIdNot(
                fridgeId,
                targetStorageType,
                targetName,
                category.getId()
        );

        if (duplicate) {
            throw new ApiException(
                    CATEGORY_DUPLICATE.code(),
                    CATEGORY_DUPLICATE.message(),
                    CATEGORY_DUPLICATE.status()
            );
        }

        category.update(targetStorageType, targetName, targetColor);
        return category.getId();

    }

    private StorageType resolveStorageType(UpdateCategoryCommand command, FridgeCategory category) {
        if (command.storageType() != null) {
            return command.storageType();
        }
        return category.getStorageType();
    }

    private String resolveName(UpdateCategoryCommand command, FridgeCategory category) {
        if (command.name() != null) {
            return command.name();
        }
        return category.getName();
    }

    private CategoryColor resolveColor(UpdateCategoryCommand command, FridgeCategory category) {
        if (command.color() != null) {
            return command.color();
        }
        return category.getColor();
    }

    @Transactional
    public void delete(Long fridgeId, Long categoryId) {
        FridgeCategory category = repository.findById(categoryId)
                .orElseThrow(() -> new ApiException(
                        CATEGORY_NOT_FOUND.code(),
                        CATEGORY_NOT_FOUND.message(),
                        CATEGORY_NOT_FOUND.status()
                ));

        if (!category.getFridgeId().equals(fridgeId)) {
            throw new ApiException(
                    CATEGORY_FORBIDDEN.code(),
                    CATEGORY_FORBIDDEN.message(),
                    CATEGORY_FORBIDDEN.status()
            );
        }

        repository.delete(category);
    }
}
