package com.potatoes.Naengu.fridge.service;

import static com.potatoes.Naengu.fridge.exception.CategoryErrorCode.*;
import static com.potatoes.Naengu.fridge.exception.FridgeErrorCode.FRIDGE_NOT_FOUND;

import com.potatoes.Naengu.fridge.dto.CategoryOrderItemRequest;
import com.potatoes.Naengu.fridge.dto.CreateCategoryCommand;
import com.potatoes.Naengu.fridge.dto.UpdateCategoryCommand;
import com.potatoes.Naengu.fridge.dto.UpdateCategoryOrderRequest;
import com.potatoes.Naengu.fridge.repository.FridgeCategoryRepository;
import com.potatoes.Naengu.fridge.repository.FridgeIngredientRepository;
import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.fridge.domain.model.FridgeCategory;
import com.potatoes.Naengu.fridge.domain.vo.CategoryColor;
import com.potatoes.Naengu.fridge.domain.vo.StorageType;
import com.potatoes.Naengu.fridge.repository.FridgeRepository;
import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.post.exception.PostErrorCode;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final FridgeRepository fridgeRepository;
    private final ProfileRepository profileRepository;
    private final FridgeCategoryRepository fridgeCategoryRepository;
    private final FridgeIngredientRepository fridgeIngredientRepository;

    public CategoryService(FridgeRepository fridgeRepository, ProfileRepository profileRepository,
                           FridgeCategoryRepository fridgeCategoryRepository,
                           FridgeIngredientRepository fridgeIngredientRepository) {
        this.fridgeRepository = fridgeRepository;
        this.profileRepository = profileRepository;
        this.fridgeCategoryRepository = fridgeCategoryRepository;
        this.fridgeIngredientRepository = fridgeIngredientRepository;
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

        fridgeIngredientRepository.hardDeleteByCategoryId(categoryId);
        fridgeCategoryRepository.hardDeleteById(categoryId);
    }

    @Transactional
    public void updateOrder(Long userId, UpdateCategoryOrderRequest request) {
        Profile profile = loadProfile(userId);
        Fridge fridge = loadOwnedFridge(profile);
        List<CategoryOrderItemRequest> orders = request.orders();

        validateDuplicateCategoryIds(orders);
        validateDuplicatePositions(orders);
        validateSequentialPositions(orders);

        List<FridgeCategory> categories = fridgeCategoryRepository.findAllByFridgeAndStorageTypeOrderByOrderIndexAsc(
                fridge,
                request.storageType()
        );

        validateCategorySetMatched(orders, categories);
        applyOrder(categories, orders);
    }

    private void validateDuplicateCategoryIds(List<CategoryOrderItemRequest> orders) {
        Set<Long> uniqueIds = orders.stream()
                .map(CategoryOrderItemRequest::categoryId)
                .collect(Collectors.toSet());

        if (uniqueIds.size() != orders.size()) {
            throw new ApiException(CATEGORY_ORDER_DUPLICATE_ID);
        }
    }

    private void validateDuplicatePositions(List<CategoryOrderItemRequest> orders) {
        Set<Integer> uniquePositions = orders.stream()
                .map(CategoryOrderItemRequest::position)
                .collect(Collectors.toSet());

        if (uniquePositions.size() != orders.size()) {
            throw new ApiException(CATEGORY_ORDER_DUPLICATE_POSITION);
        }
    }

    private void validateSequentialPositions(List<CategoryOrderItemRequest> orders) {
        List<Integer> sortedPositions = orders.stream()
                .map(CategoryOrderItemRequest::position)
                .sorted()
                .toList();

        for (int i = 0; i < sortedPositions.size(); i++) {
            int expected = i + 1;
            if (!sortedPositions.get(i).equals(expected)) {
                throw new ApiException(CATEGORY_ORDER_INVALID_POSITION);
            }
        }
    }

    private void validateCategorySetMatched(
            List<CategoryOrderItemRequest> orders,
            List<FridgeCategory> categories) {
        Set<Long> requestedIds = orders.stream()
                .map(CategoryOrderItemRequest::categoryId)
                .collect(Collectors.toSet());

        Set<Long> actualIds = categories.stream()
                .map(FridgeCategory::getId)
                .collect(Collectors.toSet());

        if (!requestedIds.equals(actualIds)) {
            throw new ApiException(CATEGORY_NOT_FOUND);
        }
    }

    private void applyOrder(
            List<FridgeCategory> categories,
            List<CategoryOrderItemRequest> orders
    ) {
        Map<Long, Integer> positionMap = orders.stream()
                .collect(Collectors.toMap(
                        CategoryOrderItemRequest::categoryId,
                        CategoryOrderItemRequest::position
                ));

        for (FridgeCategory category : categories) {
            category.changeOrderIndex(positionMap.get(category.getId()));
        }
    }

    private Profile loadProfile(Long userId) {
        return profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(PostErrorCode.PROFILE_NOT_FOUND));
    }

    private Fridge loadOwnedFridge(Profile profile) {
        Fridge fridge = profile.getFridge();
        if (fridge == null) {
            throw new ApiException(FRIDGE_NOT_FOUND);
        }
        return fridge;
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
