package com.potatoes.Naengu.recipe.query;

import com.potatoes.Naengu.fridge.repository.FridgeIngredientRepository;
import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import com.potatoes.Naengu.recipe.domain.model.Recipe;
import com.potatoes.Naengu.recipe.domain.model.RecipeWithLink;
import com.potatoes.Naengu.recipe.domain.model.RecipeWithText;
import com.potatoes.Naengu.recipe.dto.MatchingDto;
import com.potatoes.Naengu.recipe.dto.RecipeDetailDto;
import com.potatoes.Naengu.recipe.dto.RecipeDetailResponse;
import com.potatoes.Naengu.recipe.dto.RecipeStepDto;
import com.potatoes.Naengu.recipe.dto.RecipeWithLinkDto;
import com.potatoes.Naengu.recipe.dto.RecipeWithTextDto;
import com.potatoes.Naengu.recipe.exception.RecipeErrorCode;
import com.potatoes.Naengu.recipe.repository.ProfileFavoriteRecipeRepository;
import com.potatoes.Naengu.recipe.repository.RecipeIngredientRepository;
import com.potatoes.Naengu.recipe.repository.RecipeRepository;
import com.potatoes.Naengu.recipe.repository.RecipeStepRepository;
import com.potatoes.Naengu.recipe.repository.RecipeTagRepository;
import com.potatoes.Naengu.reviewrecipe.repository.RecipeReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeDetailQueryService {

    private final RecipeRepository recipeRepository;
    private final RecipeTagRepository recipeTagRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final ProfileFavoriteRecipeRepository profileFavoriteRecipeRepository;
    private final RecipeReviewRepository recipeReviewRepository;
    private final ProfileRepository profileRepository;
    private final FridgeIngredientRepository fridgeIngredientRepository;

    public RecipeDetailResponse getDetail(Long userId, Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ApiException(RecipeErrorCode.RECIPE_NOT_FOUND));

        Profile profile = profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(RecipeErrorCode.RECIPE_NOT_FOUND));

        Set<String> fridgeIngredientNames = fridgeIngredientRepository
                .findAllByFridgeCategory_Fridge(profile.getFridge()).stream()
                .map(fi -> fi.getIngredient().getName())
                .collect(Collectors.toSet());

        List<String> recipeIngredientNames = recipeIngredientRepository.findByRecipe(recipe).stream()
                .map(ri -> ri.getIngredient().getName())
                .toList();

        List<String> fridgeMatched = recipeIngredientNames.stream()
                .filter(fridgeIngredientNames::contains)
                .toList();
        List<String> missing = recipeIngredientNames.stream()
                .filter(name -> !fridgeIngredientNames.contains(name))
                .toList();

        return new RecipeDetailResponse(
                toRecipeDetailDto(recipe),
                new MatchingDto(fridgeMatched, missing)
        );
    }

    private RecipeDetailDto toRecipeDetailDto(Recipe recipe) {
        List<String> tags = recipeTagRepository.findByRecipe(recipe).stream()
                .map(rt -> rt.getTag().getValue())
                .toList();

        int likeCount = (int) profileFavoriteRecipeRepository.countByRecipe(recipe);
        int reviewCount = (int) recipeReviewRepository.countByRecipeId(recipe.getId());

        RecipeWithLinkDto linkDto = null;
        RecipeWithTextDto textDto = null;

        if (recipe instanceof RecipeWithLink rwl) {
            linkDto = new RecipeWithLinkDto(rwl.getUrlSource(), rwl.getUrl());
        } else if (recipe instanceof RecipeWithText rwt) {
            List<RecipeStepDto> steps = recipeStepRepository
                    .findByRecipeWithTextOrderByStepOrderAsc(rwt).stream()
                    .map(s -> new RecipeStepDto(s.getStepOrder(), s.getContent()))
                    .toList();
            textDto = new RecipeWithTextDto(steps);
        }

        return new RecipeDetailDto(
                recipe.getTitle(),
                recipe instanceof RecipeWithLink ? "LINK" : "TEXT",
                tags,
                likeCount,
                reviewCount,
                recipe.getDescription(),
                recipe.getServings(),
                recipe.getCookingTime(),
                recipe.getDifficulty().getDescription(),
                linkDto,
                textDto
        );
    }
}
