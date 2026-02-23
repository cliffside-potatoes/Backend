package com.potatoes.Naengu.ingredients.fridge.category.command.service;

import static com.potatoes.Naengu.ingredients.fridge.category.command.exception.CategoryErrorCode.*;

import com.potatoes.Naengu.ingredients.fridge.category.command.command.CreateCategoryCommand;
import com.potatoes.Naengu.ingredients.fridge.category.command.command.UpdateCategoryCommand;
import com.potatoes.Naengu.ingredients.fridge.category.repository.FridgeCategoryRepository;
import com.potatoes.Naengu.ingredients.fridge.domain.model.category.FridgeCategory;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.CategoryColor;
import com.potatoes.Naengu.ingredients.fridge.domain.vo.StorageType;
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
    public Long create(Long fridgeId, CreateCategoryCommand command) {
        ensureNotDuplicated(fridgeId, command.storageType(), command.name());

        int nextOrderIndex = nextOrderIndex(fridgeId, command.storageType());

        FridgeCategory category = FridgeCategory.create(
                fridgeId,
                command.name(),
                nextOrderIndex,
                command.storageType(),
                command.color()
        );

        return repository.save(category).getId();
    }

    @Transactional
    public Long update(Long fridgeId, UpdateCategoryCommand command) {
        ensureHasAnyChange(command);

        FridgeCategory category = loadCategory(command.categoryId());
        ensureOwnedByFridge(fridgeId, category);

        StorageType targetStorageType = resolveStorageType(command, category);
        String targetName = resolveName(command, category);
        CategoryColor targetColor = resolveColor(command, category);

        ensureNameNotBlankIfProvided(command, targetName);

        ensureNotDuplicatedExcludingSelfIfKeyChanged(
                fridgeId,
                command,
                category,
                targetStorageType,
                targetName
        );

        category.update(targetStorageType, targetName, targetColor);
        return category.getId();
    }

    @Transactional
    public void delete(Long fridgeId, Long categoryId) {
        FridgeCategory category = loadCategory(categoryId);
        ensureOwnedByFridge(fridgeId, category);

        repository.delete(category);
    }

    private void ensureNotDuplicated(Long fridgeId, StorageType storageType, String name) {
        boolean exists = repository.existsByFridgeIdAndStorageTypeAndName(fridgeId, storageType, name);
        if (!exists) {
            return;
        }
        throw new ApiException(CATEGORY_DUPLICATE);
    }

    private int nextOrderIndex(Long fridgeId, StorageType storageType) {
        return repository.findMaxOrderIndexByFridgeIdAndStorageType(fridgeId, storageType) + 1;
    }

    private void ensureHasAnyChange(UpdateCategoryCommand command) {
        if (command.hasAnyChange()) {
            return;
        }
        throw new ApiException(CATEGORY_UPDATE_EMPTY);
    }

    private FridgeCategory loadCategory(Long categoryId) {
        return repository.findById(categoryId)
                .orElseThrow(() -> new ApiException(CATEGORY_NOT_FOUND));
    }

    private void ensureOwnedByFridge(Long fridgeId, FridgeCategory category) {
        if (category.getFridgeId().equals(fridgeId)) {
            return;
        }
        throw new ApiException(CATEGORY_FORBIDDEN);
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

    private void ensureNameNotBlankIfProvided(UpdateCategoryCommand command, String targetName) {
        if (command.name() == null) {
            return;
        }
        if (!targetName.isBlank()) {
            return;
        }
        throw new ApiException(CATEGORY_NAME_BLANK);
    }

    private void ensureNotDuplicatedExcludingSelfIfKeyChanged(
            Long fridgeId,
            UpdateCategoryCommand command,
            FridgeCategory category,
            StorageType targetStorageType,
            String targetName
    ) {
        boolean keyChanged = command.storageType() != null || command.name() != null;
        if (!keyChanged) {
            return;
        }

        boolean duplicate = repository.existsByFridgeIdAndStorageTypeAndNameAndIdNot(
                fridgeId,
                targetStorageType,
                targetName,
                category.getId()
        );

        if (!duplicate) {
            return;
        }
        throw new ApiException(CATEGORY_DUPLICATE);
    }
}
