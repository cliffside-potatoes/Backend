package com.potatoes.Naengu.fridge.service;

import static com.potatoes.Naengu.ingredient.exception.IngredientErrorCode.INGREDIENT_NOT_FOUND;
import static com.potatoes.Naengu.fridge.exception.FridgeIngredientErrorCode.*;

import com.potatoes.Naengu.ingredient.domain.model.Ingredient;
import com.potatoes.Naengu.ingredient.repository.IngredientRepository;
import com.potatoes.Naengu.fridge.repository.FridgeCategoryRepository;
import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.fridge.domain.model.FridgeCategory;
import com.potatoes.Naengu.fridge.domain.model.FridgeIngredient;
import com.potatoes.Naengu.fridge.dto.CreateFridgeIngredientCommand;
import com.potatoes.Naengu.fridge.dto.UpdateFridgeIngredientCommand;
import com.potatoes.Naengu.fridge.repository.FridgeIngredientRepository;
import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.recipe.service.RecipeMatchRankingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FridgeIngredientService {

    private final FridgeIngredientRepository fridgeIngredientRepository;
    private final IngredientRepository ingredientRepository;
    private final FridgeCategoryRepository fridgeCategoryRepository;
    private final RecipeMatchRankingService recipeMatchRankingService;

    public FridgeIngredientService(
            FridgeIngredientRepository fridgeIngredientRepository,
            IngredientRepository ingredientRepository,
            FridgeCategoryRepository fridgeCategoryRepository,
            RecipeMatchRankingService recipeMatchRankingService
    ) {
        this.fridgeIngredientRepository = fridgeIngredientRepository;
        this.ingredientRepository = ingredientRepository;
        this.fridgeCategoryRepository = fridgeCategoryRepository;
        this.recipeMatchRankingService = recipeMatchRankingService;
    }

    @Transactional
    public Long create(Fridge fridge, CreateFridgeIngredientCommand command) {
        FridgeCategory category = loadOwnedCategory(fridge, command.categoryId());
        Ingredient ingredient = loadIngredient(command.ingredientId());

        ensureNotDuplicated(category.getId(), ingredient.getId());

        FridgeIngredient entity = FridgeIngredient.create(category, ingredient);
        Long id = fridgeIngredientRepository.save(entity).getId();
        recipeMatchRankingService.refresh(fridge);
        return id;
    }

    @Transactional
    public Long update(Fridge fridge, UpdateFridgeIngredientCommand command) {
        ensureHasAnyChange(command);

        FridgeIngredient fridgeIngredient = loadFridgeIngredient(command.fridgeIngredientId());
        ensureOwnedByFridge(fridge, fridgeIngredient);

        FridgeCategory targetCategory = resolveTargetCategory(fridge, command, fridgeIngredient);
        Ingredient targetIngredient = resolveTargetIngredient(command, fridgeIngredient);

        ensureNotDuplicatedExcludingSelf(
                targetCategory.getId(),
                targetIngredient.getId(),
                fridgeIngredient.getId()
        );

        fridgeIngredient.update(targetCategory, targetIngredient);
        recipeMatchRankingService.refresh(fridge);
        return fridgeIngredient.getId();
    }

    @Transactional
    public void delete(Fridge fridge, Long fridgeIngredientId) {
        FridgeIngredient fridgeIngredient = loadFridgeIngredient(fridgeIngredientId);
        ensureOwnedByFridge(fridge, fridgeIngredient);

        fridgeIngredientRepository.delete(fridgeIngredient);
        recipeMatchRankingService.refresh(fridge);
    }

    private FridgeCategory loadOwnedCategory(Fridge fridge, Long categoryId) {
        return fridgeCategoryRepository.findByIdAndFridge(categoryId, fridge)
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

    private void ensureOwnedByFridge(Fridge fridge, FridgeIngredient fridgeIngredient) {
        if (fridgeIngredient.getFridgeCategory().getFridge().getId().equals(fridge.getId())) {
            return;
        }
        throw new ApiException(FRIDGE_INGREDIENT_FORBIDDEN);
    }

    private FridgeCategory resolveTargetCategory(
            Fridge fridge,
            UpdateFridgeIngredientCommand command,
            FridgeIngredient fridgeIngredient
    ) {
        if (command.fridgeCategoryId() == null) {
            return fridgeIngredient.getFridgeCategory();
        }
        return loadOwnedCategory(fridge, command.fridgeCategoryId());
    }

    private Ingredient resolveTargetIngredient(UpdateFridgeIngredientCommand command, FridgeIngredient fridgeIngredient) {
        if (command.ingredientId() == null) {
            return fridgeIngredient.getIngredient();
        }
        return loadIngredient(command.ingredientId());
    }

    private void ensureNotDuplicated(Long categoryId, Long ingredientId) {
        boolean duplicated = fridgeIngredientRepository
                .existsByFridgeCategory_IdAndIngredient_Id(categoryId, ingredientId);

        if (!duplicated) {
            return;
        }
        throw new ApiException(FRIDGE_INGREDIENT_DUPLICATE);
    }

    private void ensureNotDuplicatedExcludingSelf(Long categoryId, Long ingredientId, Long selfId) {
        boolean duplicated = fridgeIngredientRepository
                .existsByFridgeCategory_IdAndIngredient_IdAndIdNot(categoryId, ingredientId, selfId);

        if (!duplicated) {
            return;
        }
        throw new ApiException(FRIDGE_INGREDIENT_DUPLICATE);
    }
}
