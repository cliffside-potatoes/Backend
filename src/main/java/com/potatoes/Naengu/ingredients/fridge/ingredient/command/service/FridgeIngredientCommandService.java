package com.potatoes.Naengu.ingredients.fridge.ingredient.command.service;

import static com.potatoes.Naengu.ingredients.fridge.ingredient.command.exception.IngredientErrorCode.*;

import com.potatoes.Naengu.ingredients.dictionary.ingredient.repository.IngredientRepository;
import com.potatoes.Naengu.ingredients.fridge.category.repository.FridgeCategoryRepository;
import com.potatoes.Naengu.ingredients.fridge.domain.model.ingredient.FridgeIngredient;
import com.potatoes.Naengu.ingredients.fridge.ingredient.command.command.CreateFridgeIngredientCommand;
import com.potatoes.Naengu.ingredients.fridge.ingredient.command.command.UpdateFridgeIngredientCommand;
import com.potatoes.Naengu.ingredients.fridge.ingredient.repository.FridgeIngredientRepository;
import com.potatoes.Naengu.ingredients.shared.exception.ApiException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class FridgeIngredientCommandService {

    private final FridgeIngredientRepository fridgeIngredientRepository;
    private final IngredientRepository ingredientRepository;
    private final FridgeCategoryRepository fridgeCategoryRepository;

    public FridgeIngredientCommandService(FridgeIngredientRepository fridgeIngredientRepository,
                                          IngredientRepository ingredientRepository,
                                          FridgeCategoryRepository fridgeCategoryRepository) {
        this.fridgeIngredientRepository = fridgeIngredientRepository;
        this.ingredientRepository = ingredientRepository;
        this.fridgeCategoryRepository = fridgeCategoryRepository;
    }

    @Transactional
    public Long create(Long fridgeId, CreateFridgeIngredientCommand command) {
        validateCategoryOwnedByFridge(fridgeId, command.categoryId());
        validateIngredientExists(command.ingredientId());
        validateNotDuplicated(fridgeId, command);

        FridgeIngredient entity = FridgeIngredient.create(
                fridgeId,
                command.categoryId(),
                command.ingredientId()
        );

        return fridgeIngredientRepository.save(entity).getId();
    }

    private void validateCategoryOwnedByFridge(Long fridgeId, Long categoryId) {
        boolean exists = fridgeCategoryRepository.existsByIdAndFridgeIdAndDeletedFalse(categoryId, fridgeId);
        if (!exists) {
            throw new ApiException(
                    FRIDGE_CATEGORY_NOT_FOUND.code(),
                    FRIDGE_CATEGORY_NOT_FOUND.message(),
                    FRIDGE_CATEGORY_NOT_FOUND.status()
            );
        }
    }

    private void validateIngredientExists(Long ingredientId) {
        boolean exists = ingredientRepository.existsById(ingredientId);
        if (!exists) {
            throw new ApiException(
                    FRIDGE_INGREDIENT_NOT_FOUND.code(),
                    FRIDGE_INGREDIENT_NOT_FOUND.message(),
                    FRIDGE_INGREDIENT_NOT_FOUND.status()
            );
        }
    }

    private void validateNotDuplicated(Long fridgeId, CreateFridgeIngredientCommand command) {
        boolean duplicated = fridgeIngredientRepository
                .existsByFridgeIdAndFridgeCategoryIdAndIngredientId(
                        fridgeId,
                        command.categoryId(),
                        command.ingredientId()
                );

        if (duplicated) {
            throw new ApiException(
                    FRIDGE_INGREDIENT_DUPLICATE.code(),
                    FRIDGE_INGREDIENT_DUPLICATE.message(),
                    FRIDGE_INGREDIENT_DUPLICATE.status()
            );

        }
    }

    @Transactional
    public Long update(Long fridgeId, UpdateFridgeIngredientCommand command) {
        if (!command.hasAnyChange()) {
            throw new ApiException(
                    FRIDGE_INGREDIENT_UPDATE_EMPTY.code(),
                    FRIDGE_INGREDIENT_UPDATE_EMPTY.message(),
                    FRIDGE_INGREDIENT_UPDATE_EMPTY.status()
            );
        }

        FridgeIngredient fridgeIngredient = fridgeIngredientRepository.findById(command.fridgeIngredientId())
                .orElseThrow(() -> new ApiException(
                        FRIDGE_INGREDIENT_NOT_FOUND.code(),
                        FRIDGE_INGREDIENT_NOT_FOUND.message(),
                        FRIDGE_INGREDIENT_NOT_FOUND.status()
                ));

        if (!fridgeIngredient.getFridgeId().equals(fridgeId)) {
            throw new ApiException(
                    FRIDGE_INGREDIENT_FORBIDDEN.code(),
                    FRIDGE_INGREDIENT_DUPLICATE.message(),
                    FRIDGE_INGREDIENT_FORBIDDEN.status()
            );
        }

        Long targetCategoryId = resolveCategoryId(command, fridgeIngredient);
        Long targetIngredientId = resolveIngredientId(command, fridgeIngredient);

        boolean duplicate = fridgeIngredientRepository.existsByFridgeIdAndFridgeCategoryIdAndIngredientIdAndIdNot(
                fridgeId,
                targetCategoryId,
                targetIngredientId,
                fridgeIngredient.getId()
        );

        if (duplicate) {
            throw new ApiException(
                    FRIDGE_INGREDIENT_DUPLICATE.code(),
                    FRIDGE_INGREDIENT_DUPLICATE.message(),
                    FRIDGE_INGREDIENT_DUPLICATE.status()
            );
        }

        fridgeIngredient.update(targetCategoryId, targetIngredientId);
        return fridgeIngredient.getId();

    }

    private Long resolveCategoryId(UpdateFridgeIngredientCommand command, FridgeIngredient fridgeIngredient) {
        if (command.fridgeCategoryId() != null) {
            return command.fridgeCategoryId();
        }
        return fridgeIngredient.getFridgeCategoryId();
    }

    private Long resolveIngredientId(UpdateFridgeIngredientCommand command, FridgeIngredient fridgeIngredient) {
        if (command.ingredientId() != null) {
            return command.ingredientId();
        }
        return fridgeIngredient.getIngredientId();

    }

    @Transactional
    public void delete(Long fridgeId, Long fridgeIngredientId) {
        FridgeIngredient fridgeIngredient = fridgeIngredientRepository.findById(fridgeIngredientId)
                .orElseThrow(() -> new ApiException(
                        FRIDGE_INGREDIENT_NOT_FOUND.code(),
                        FRIDGE_INGREDIENT_NOT_FOUND.message(),
                        FRIDGE_INGREDIENT_NOT_FOUND.status()
                ));

        if (!fridgeIngredient.getFridgeId().equals(fridgeId)) {
            throw new ApiException(
                    FRIDGE_INGREDIENT_FORBIDDEN.code(),
                    FRIDGE_INGREDIENT_FORBIDDEN.message(),
                    FRIDGE_INGREDIENT_FORBIDDEN.status()
            );
        }

        fridgeIngredientRepository.delete(fridgeIngredient);
    }
}
