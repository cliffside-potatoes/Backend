package com.potatoes.Naengu.ingredients.fridge.ingredient.command.service;

import static com.potatoes.Naengu.ingredients.dictionary.ingredient.exception.IngredientErrorCode.INGREDIENT_NOT_FOUND;
import static com.potatoes.Naengu.ingredients.fridge.ingredient.command.exception.FridgeIngredientErrorCode.*;

import com.potatoes.Naengu.ingredients.dictionary.ingredient.domain.Ingredient;
import com.potatoes.Naengu.ingredients.dictionary.ingredient.repository.IngredientRepository;
import com.potatoes.Naengu.ingredients.fridge.category.repository.FridgeCategoryRepository;
import com.potatoes.Naengu.ingredients.fridge.domain.model.category.FridgeCategory;
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

    public FridgeIngredientCommandService(
            FridgeIngredientRepository fridgeIngredientRepository,
            IngredientRepository ingredientRepository,
            FridgeCategoryRepository fridgeCategoryRepository
    ) {
        this.fridgeIngredientRepository = fridgeIngredientRepository;
        this.ingredientRepository = ingredientRepository;
        this.fridgeCategoryRepository = fridgeCategoryRepository;
    }

    @Transactional
    public Long create(Long fridgeId, CreateFridgeIngredientCommand command) {
        FridgeCategory category = loadOwnedCategory(fridgeId, command.categoryId());
        Ingredient ingredient = loadIngredient(command.ingredientId());

        ensureNotDuplicated(fridgeId, category.getId(), ingredient.getId());

        FridgeIngredient entity = FridgeIngredient.create(fridgeId, category, ingredient);
        return fridgeIngredientRepository.save(entity).getId();
    }

    @Transactional
    public Long update(Long fridgeId, UpdateFridgeIngredientCommand command) {
        ensureHasAnyChange(command);

        FridgeIngredient fridgeIngredient = loadFridgeIngredient(command.fridgeIngredientId());
        ensureOwnedByFridge(fridgeId, fridgeIngredient);

        FridgeCategory targetCategory = resolveTargetCategory(fridgeId, command, fridgeIngredient);
        Ingredient targetIngredient = resolveTargetIngredient(command, fridgeIngredient);

        ensureNotDuplicatedExcludingSelf(
                fridgeId,
                targetCategory.getId(),
                targetIngredient.getId(),
                fridgeIngredient.getId()
        );

        fridgeIngredient.update(targetCategory, targetIngredient);
        return fridgeIngredient.getId();
    }

    @Transactional
    public void delete(Long fridgeId, Long fridgeIngredientId) {
        FridgeIngredient fridgeIngredient = loadFridgeIngredient(fridgeIngredientId);
        ensureOwnedByFridge(fridgeId, fridgeIngredient);

        fridgeIngredientRepository.delete(fridgeIngredient);
    }

    private FridgeCategory loadOwnedCategory(Long fridgeId, Long categoryId) {
        return fridgeCategoryRepository.findByIdAndFridgeId(categoryId, fridgeId)
                .orElseThrow(() -> new ApiException(FRIDGE_CATEGORY_NOT_FOUND));
    }

    private Ingredient loadIngredient(Long ingredientId) {
        return ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ApiException(INGREDIENT_NOT_FOUND));
    }

    private FridgeIngredient loadFridgeIngredient(Long fridgeIngredientId) {
        return fridgeIngredientRepository.findById(fridgeIngredientId)
                .orElseThrow(() -> new ApiException(FRIDGE_INGREDIENT_NOT_FOUND));
    }

    private void ensureHasAnyChange(UpdateFridgeIngredientCommand command) {
        if (command.hasAnyChange()) {
            return;
        }
        throw new ApiException(FRIDGE_INGREDIENT_UPDATE_EMPTY);
    }

    private void ensureOwnedByFridge(Long fridgeId, FridgeIngredient fridgeIngredient) {
        if (fridgeIngredient.getFridgeId().equals(fridgeId)) {
            return;
        }
        throw new ApiException(FRIDGE_INGREDIENT_FORBIDDEN);
    }

    private FridgeCategory resolveTargetCategory(
            Long fridgeId,
            UpdateFridgeIngredientCommand command,
            FridgeIngredient fridgeIngredient
    ) {
        if (command.fridgeCategoryId() == null) {
            return fridgeIngredient.getFridgeCategory();
        }
        return loadOwnedCategory(fridgeId, command.fridgeCategoryId());
    }

    private Ingredient resolveTargetIngredient(UpdateFridgeIngredientCommand command, FridgeIngredient fridgeIngredient) {
        if (command.ingredientId() == null) {
            return fridgeIngredient.getIngredient();
        }
        return loadIngredient(command.ingredientId());
    }

    private void ensureNotDuplicated(Long fridgeId, Long categoryId, Long ingredientId) {
        boolean duplicated = fridgeIngredientRepository
                .existsByFridgeIdAndFridgeCategory_IdAndIngredient_Id(fridgeId, categoryId, ingredientId);

        if (!duplicated) {
            return;
        }
        throw new ApiException(FRIDGE_INGREDIENT_DUPLICATE);
    }

    private void ensureNotDuplicatedExcludingSelf(Long fridgeId, Long categoryId, Long ingredientId, Long selfId) {
        boolean duplicated = fridgeIngredientRepository
                .existsByFridgeIdAndFridgeCategory_IdAndIngredient_IdAndIdNot(fridgeId, categoryId, ingredientId, selfId);

        if (!duplicated) {
            return;
        }
        throw new ApiException(FRIDGE_INGREDIENT_DUPLICATE);
    }
}
