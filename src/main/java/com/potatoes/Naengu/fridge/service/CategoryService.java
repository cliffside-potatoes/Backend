package com.potatoes.Naengu.fridge.service;

import static com.potatoes.Naengu.fridge.exception.CategoryErrorCode.*;

import com.potatoes.Naengu.fridge.dto.CreateCategoryCommand;
import com.potatoes.Naengu.fridge.dto.UpdateCategoryCommand;
import com.potatoes.Naengu.fridge.repository.FridgeCategoryRepository;
import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.fridge.domain.model.FridgeCategory;
import com.potatoes.Naengu.fridge.domain.vo.CategoryColor;
import com.potatoes.Naengu.fridge.domain.vo.StorageType;
import com.potatoes.Naengu.global.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final FridgeCategoryRepository fridgeCategoryRepository;

    public CategoryService(FridgeCategoryRepository fridgeCategoryRepository) {
        this.fridgeCategoryRepository = fridgeCategoryRepository;
    }

    @Transactional
    public Long create(Fridge fridge, CreateCategoryCommand command) {
        ensureNotDuplicated(fridge, command.storageType(), command.name());

        int nextOrderIndex = nextOrderIndex(fridge, command.storageType());

        FridgeCategory category = FridgeCategory.create(
                fridge,
                command.name(),
                nextOrderIndex,
                command.storageType(),
                command.color()
        );

        return fridgeCategoryRepository.save(category).getId();
    }

    @Transactional
    public Long update(Fridge fridge, UpdateCategoryCommand command) {
        ensureHasAnyChange(command);

        FridgeCategory category = loadCategory(command.categoryId());
        ensureOwnedByFridge(fridge, category);

        StorageType targetStorageType = resolveStorageType(command, category);
        String targetName = resolveName(command, category);
        CategoryColor targetColor = resolveColor(command, category);

        ensureNameNotBlankIfProvided(command, targetName);

        ensureNotDuplicatedExcludingSelfIfKeyChanged(
                fridge,
                command,
                category,
                targetStorageType,
                targetName
        );

        category.update(targetStorageType, targetName, targetColor);
        return category.getId();
    }

    @Transactional
    public void delete(Fridge fridge, Long categoryId) {
        FridgeCategory category = loadCategory(categoryId);
        ensureOwnedByFridge(fridge, category);

        fridgeCategoryRepository.delete(category);
    }

    private void ensureNotDuplicated(Fridge fridge, StorageType storageType, String name) {
        boolean exists = fridgeCategoryRepository.existsByFridgeAndStorageTypeAndName(fridge, storageType, name);
        if (!exists) {
            return;
        }
        throw new ApiException(CATEGORY_DUPLICATE);
    }

    private int nextOrderIndex(Fridge fridge, StorageType storageType) {
        return fridgeCategoryRepository.findMaxOrderIndexByFridgeAndStorageType(fridge, storageType) + 1;
    }

    private void ensureHasAnyChange(UpdateCategoryCommand command) {
        if (command.hasAnyChange()) {
            return;
        }
        throw new ApiException(CATEGORY_UPDATE_EMPTY);
    }

    private FridgeCategory loadCategory(Long categoryId) {
        return fridgeCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ApiException(CATEGORY_NOT_FOUND));
    }

    private void ensureOwnedByFridge(Fridge fridge, FridgeCategory category) {
        if (category.getFridge().getId().equals(fridge.getId())) {
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
            Fridge fridge,
            UpdateCategoryCommand command,
            FridgeCategory category,
            StorageType targetStorageType,
            String targetName
    ) {
        boolean keyChanged = command.storageType() != null || command.name() != null;
        if (!keyChanged) {
            return;
        }

        boolean duplicate = fridgeCategoryRepository.existsByFridgeAndStorageTypeAndNameAndIdNot(
                fridge,
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
