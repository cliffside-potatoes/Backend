package com.potatoes.Naengu.recipe.query;

import com.potatoes.Naengu.file.service.FileUploadService;
import com.potatoes.Naengu.fridge.domain.model.FridgeIngredient;
import com.potatoes.Naengu.fridge.repository.FridgeIngredientRepository;
import com.potatoes.Naengu.global.dto.CursorResponse;
import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import com.potatoes.Naengu.recipe.domain.model.Recipe;
import com.potatoes.Naengu.recipe.domain.vo.RecipeSortType;
import com.potatoes.Naengu.recipe.dto.RecipeSearchItemResponse;
import com.potatoes.Naengu.recipe.dto.RecipeSearchRequest;
import com.potatoes.Naengu.recipe.dto.RecipeSearchResponse;
import com.potatoes.Naengu.recipe.exception.RecipeErrorCode;
import com.potatoes.Naengu.recipe.repository.ProfileFavoriteRecipeRepository;
import com.potatoes.Naengu.recipe.repository.RecipeIngredientRepository;
import com.potatoes.Naengu.recipe.repository.RecipeRepository;
import com.potatoes.Naengu.recipe.repository.RecipeTagRepository;
import com.potatoes.Naengu.reviewrecipe.repository.RecipeReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeQueryService {

    private final RecipeRepository recipeRepository;
    private final RecipeTagRepository recipeTagRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final ProfileFavoriteRecipeRepository profileFavoriteRecipeRepository;
    private final RecipeReviewRepository recipeReviewRepository;
    private final ProfileRepository profileRepository;
    private final FridgeIngredientRepository fridgeIngredientRepository;
    private final FileUploadService fileUploadService;

    @Transactional(readOnly = true)
    public RecipeSearchResponse search(Long userId, RecipeSearchRequest request) {
        validateCursor(request);
        RecipeSortType.from(request.sort());

        Profile profile = profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(RecipeErrorCode.RECIPE_NOT_FOUND));

        Set<Long> fridgeIngredientIds = fridgeIngredientRepository
                .findAllByFridgeCategory_Fridge(profile.getFridge())
                .stream()
                .map(fi -> fi.getIngredient().getId())
                .collect(Collectors.toSet());

        List<Recipe> recipes = fetchRecipes(request);

        boolean hasNext = recipes.size() > request.size();
        if (hasNext) {
            recipes = recipes.subList(0, request.size());
        }

        List<RecipeSearchItemResponse> items = recipes.stream()
                .map(recipe -> toItemResponse(recipe, fridgeIngredientIds))
                .toList();

        CursorResponse nextCursor = null;
        if (hasNext && !recipes.isEmpty()) {
            Recipe last = recipes.get(recipes.size() - 1);
            nextCursor = new CursorResponse(
                    last.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    last.getId()
            );
        }

        return new RecipeSearchResponse(items, hasNext, nextCursor);
    }

    private List<Recipe> fetchRecipes(RecipeSearchRequest request) {
        PageRequest pageable = PageRequest.of(0, request.size() + 1);
        boolean hasKeyword = request.keyword() != null && !request.keyword().isBlank();

        if (request.cursorCreatedAt() == null) {
            return hasKeyword
                    ? recipeRepository.findLatestByKeyword(request.keyword(), pageable)
                    : recipeRepository.findLatestAll(pageable);
        }

        LocalDateTime cursorTime = parseCursorTime(request.cursorCreatedAt());
        return hasKeyword
                ? recipeRepository.findLatestByKeywordAfterCursor(request.keyword(), cursorTime, request.cursorId(), pageable)
                : recipeRepository.findLatestAfterCursor(cursorTime, request.cursorId(), pageable);
    }

    private RecipeSearchItemResponse toItemResponse(Recipe recipe, Set<Long> fridgeIngredientIds) {
        String thumbnailUrl = recipe.getRecipeImage() != null
                ? fileUploadService.getPublicUrl(recipe.getRecipeImage().getS3Key())
                : fileUploadService.getDefaultProfileImageUrl();

        List<String> tags = recipeTagRepository.findByRecipe(recipe).stream()
                .map(rt -> rt.getTag().getValue())
                .toList();

        int likeCount = (int) profileFavoriteRecipeRepository.countByRecipe(recipe);
        int reviewCount = (int) recipeReviewRepository.countByRecipeId(recipe.getId());

        Set<Long> recipeIngredientIds = recipeIngredientRepository.findByRecipe(recipe).stream()
                .map(ri -> ri.getIngredient().getId())
                .collect(Collectors.toSet());
        recipeIngredientIds.retainAll(fridgeIngredientIds);
        int matchedIngredientCount = recipeIngredientIds.size();

        return new RecipeSearchItemResponse(
                recipe.getId(),
                recipe.getTitle(),
                thumbnailUrl,
                tags,
                likeCount,
                reviewCount,
                matchedIngredientCount,
                recipe.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

    private void validateCursor(RecipeSearchRequest request) {
        boolean hasCursorCreatedAt = request.cursorCreatedAt() != null;
        boolean hasCursorId = request.cursorId() != null;
        if (hasCursorCreatedAt != hasCursorId) {
            throw new ApiException(RecipeErrorCode.INVALID_CURSOR);
        }
    }

    private LocalDateTime parseCursorTime(String cursorCreatedAt) {
        try {
            return LocalDateTime.parse(cursorCreatedAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new ApiException(RecipeErrorCode.INVALID_CURSOR);
        }
    }
}
