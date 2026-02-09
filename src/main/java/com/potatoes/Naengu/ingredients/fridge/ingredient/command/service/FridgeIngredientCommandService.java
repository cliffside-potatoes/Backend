package com.potatoes.Naengu.ingredients.fridge.ingredient.command.service;

import static com.potatoes.Naengu.ingredients.fridge.ingredient.command.exception.IngredientErrorCode.*;

import com.potatoes.Naengu.ingredients.dictionary.ingredient.repository.IngredientRepository;
import com.potatoes.Naengu.ingredients.fridge.category.repository.FridgeCategoryRepository;
import com.potatoes.Naengu.ingredients.fridge.domain.model.ingredient.FridgeIngredient;
import com.potatoes.Naengu.ingredients.fridge.ingredient.command.command.CreateFridgeIngredientCommand;
import com.potatoes.Naengu.ingredients.fridge.ingredient.command.exception.IngredientErrorCode;
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
                    INGREDIENT_NOT_FOUND.code(),
                    INGREDIENT_NOT_FOUND.message(),
                    INGREDIENT_NOT_FOUND.status()
            );
        }
    }

    private void validateNotDuplicated(Long fridgeId, CreateFridgeIngredientCommand command) {
        boolean duplicated = fridgeIngredientRepository
                .existsByFridgeIdAndFridgeCategoryIdAndIngredientIdAndDeletedFalse(
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


}
