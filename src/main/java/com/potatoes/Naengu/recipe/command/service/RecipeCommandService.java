package com.potatoes.Naengu.recipe.command.service;

import static com.potatoes.Naengu.recipe.command.exception.RecipeIngredientErrorCode.RECIPE_INGREDIENT_DUPLICATE;
import static com.potatoes.Naengu.recipe.command.exception.RecipeStepErrorCode.RECIPE_STEP_DUPLICATE_ORDER;
import static com.potatoes.Naengu.recipe.command.exception.RecipeTagErrorCode.RECIPE_TAG_DUPLICATE;

import com.potatoes.Naengu.ingredients.dictionary.ingredient.domain.Ingredient;
import com.potatoes.Naengu.ingredients.dictionary.ingredient.service.IngredientResolver;
import com.potatoes.Naengu.ingredients.shared.exception.ApiException;
import com.potatoes.Naengu.recipe.command.command.CreateRecipeCommand;
import com.potatoes.Naengu.recipe.command.command.CreateRecipeStepCommand;
import com.potatoes.Naengu.recipe.domain.model.Recipe;
import com.potatoes.Naengu.recipe.domain.model.RecipeIngredient;
import com.potatoes.Naengu.recipe.domain.model.RecipeStep;
import com.potatoes.Naengu.recipe.domain.model.RecipeTag;
import com.potatoes.Naengu.recipe.domain.model.RecipeWithLink;
import com.potatoes.Naengu.recipe.domain.model.RecipeWithText;
import com.potatoes.Naengu.recipe.domain.model.Tag;
import com.potatoes.Naengu.recipe.repository.RecipeIngredientRepository;
import com.potatoes.Naengu.recipe.repository.RecipeRepository;
import com.potatoes.Naengu.recipe.repository.RecipeStepRepository;
import com.potatoes.Naengu.recipe.repository.RecipeTagRepository;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class RecipeCommandService {

    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeTagRepository recipeTagRepository;
    private final RecipeStepRepository recipeStepRepository;

    private final IngredientResolver ingredientResolver;
    private final TagResolver tagResolver;


    public RecipeCommandService(RecipeRepository recipeRepository,
                                RecipeIngredientRepository recipeIngredientRepository,
                                RecipeTagRepository recipeTagRepository, RecipeStepRepository recipeStepRepository,
                                IngredientResolver ingredientResolver, TagResolver tagResolver) {
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.recipeTagRepository = recipeTagRepository;
        this.recipeStepRepository = recipeStepRepository;
        this.ingredientResolver = ingredientResolver;
        this.tagResolver = tagResolver;
    }

    @Transactional
    public Long create(CreateRecipeCommand command) {
        Recipe recipe = buildRecipe(command);
        Recipe persisted = recipeRepository.save(recipe);

        saveIngredients(persisted, command.ingredients());
        saveTags(persisted, command.tags());
        saveStepsIfText(persisted,command);

        return persisted.getId();
    }



    private Recipe buildRecipe(CreateRecipeCommand command) {
        if (command.isTextType()) {
            return RecipeWithText.of(
                    command.title(),
                    command.servings(),
                    command.difficulty(),
                    command.cookingTime(),
                    command.description(),
                    command.thumbnailImage()
            );
        }

        return RecipeWithLink.of(
                command.title(),
                command.servings(),
                command.difficulty(),
                command.cookingTime(),
                command.description(),
                command.thumbnailImage(),
                command.recipeWithLink().url(),
                command.recipeWithLink().urlSource()
        );
    }

    private void saveIngredients(Recipe recipe, List<String> ingredientInputs) {
        Set<Long> uniqueIngredientIds = new HashSet<>();
        List<RecipeIngredient> links = ingredientInputs.stream()
                .map(ingredientResolver::resolverOrCreateByInput)
                .map(ingredient -> toRecipeIngredient(recipe, uniqueIngredientIds, ingredient))
                .toList();

        recipeIngredientRepository.saveAll(links);
    }

    private RecipeIngredient toRecipeIngredient(Recipe recipe, Set<Long> uniqueIds, Ingredient ingredient) {
        if (!uniqueIds.add(ingredient.getId())) {
            throw new ApiException(RECIPE_INGREDIENT_DUPLICATE);
        }
        return RecipeIngredient.link(recipe, ingredient);
    }

    private void saveTags(Recipe recipe, List<String> tagValues) {
        Set<Long> uniqueTagIds = new HashSet<>();

        List<RecipeTag> links = tagValues.stream()
                .map(tagResolver::resolveOrCreate)
                .map(tag -> toRecipeTag(recipe, uniqueTagIds, tag))
                .toList();

        recipeTagRepository.saveAll(links);
    }

    private RecipeTag toRecipeTag(Recipe recipe, Set<Long> uniqueIds, Tag tag) {
        if (!uniqueIds.add(tag.getId())) {
            throw new ApiException(RECIPE_TAG_DUPLICATE);
        }
        return RecipeTag.link(recipe, tag);
    }

    private void saveStepsIfText(Recipe persisted, CreateRecipeCommand command) {
        if (!(persisted instanceof RecipeWithText recipeWithText)) {
            return;
        }

        saveSteps(recipeWithText, command.recipeWithText().steps());
    }

    private void saveSteps(RecipeWithText recipeWithText, List<CreateRecipeStepCommand> steps) {
        Set<Integer> uniqueOrders = new HashSet<>();
        List<RecipeStep> entities = steps.stream()
                .map(step -> toRecipeStep(recipeWithText, uniqueOrders, step))
                .toList();

        recipeStepRepository.saveAll(entities);
    }

    private RecipeStep toRecipeStep(RecipeWithText recipeWithText, Set<Integer> uniqueOrders, CreateRecipeStepCommand step) {
        if (!uniqueOrders.add(step.stepOrder())) {
            throw new ApiException(RECIPE_STEP_DUPLICATE_ORDER);
        }
        return RecipeStep.of(step.stepOrder(), step.content(), recipeWithText);
    }

}
