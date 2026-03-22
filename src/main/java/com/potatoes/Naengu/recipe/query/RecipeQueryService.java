package com.potatoes.Naengu.recipe.query;

import com.potatoes.Naengu.file.service.FileUploadService;
import com.potatoes.Naengu.fridge.repository.FridgeIngredientRepository;
import com.potatoes.Naengu.global.dto.MatchCountCursorResponse;
import com.potatoes.Naengu.global.dto.CursorResponse;
import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import com.potatoes.Naengu.recipe.domain.model.Recipe;
import com.potatoes.Naengu.recipe.domain.model.RecipeWithLink;
import com.potatoes.Naengu.recipe.domain.vo.RecipeSortType;
import com.potatoes.Naengu.recipe.dto.RecipeMatchResponse;
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
        validateLatestCursor(request);

        Profile profile = profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(RecipeErrorCode.RECIPE_NOT_FOUND));

        Set<Long> fridgeIngredientIds = fridgeIngredientRepository
                .findAllByFridgeCategory_Fridge(profile.getFridge())
                .stream()
                .map(fi -> fi.getIngredient().getId())
                .collect(Collectors.toSet());

        List<Recipe> recipes = fetchLatestRecipes(request);

        boolean hasNext = recipes.size() > request.size();
        if (hasNext) {
            recipes = recipes.subList(0, request.size());
        }

        List<RecipeSearchItemResponse> items = recipes.stream()
                .map(recipe -> toItemResponse(recipe, fridgeIngredientIds, profile))
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

    @Transactional(readOnly = true)
    public RecipeMatchResponse searchByMatchCount(Long userId, RecipeSearchRequest request) {
        validateMatchCountCursor(request);

        Profile profile = profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(RecipeErrorCode.RECIPE_NOT_FOUND));

        Set<Long> fridgeIngredientIds = fridgeIngredientRepository
                .findAllByFridgeCategory_Fridge(profile.getFridge())
                .stream()
                .map(fi -> fi.getIngredient().getId())
                .collect(Collectors.toSet());

        List<Recipe> recipes = fetchMatchCountRecipes(request, fridgeIngredientIds);

        boolean hasNext = recipes.size() > request.size();
        if (hasNext) {
            recipes = recipes.subList(0, request.size());
        }

        List<RecipeSearchItemResponse> items = recipes.stream()
                .map(recipe -> toItemResponse(recipe, fridgeIngredientIds, profile))
                .toList();

        MatchCountCursorResponse nextCursor = null;
        if (hasNext && !recipes.isEmpty()) {
            Recipe last = recipes.get(recipes.size() - 1);
            Set<Long> lastIngIds = recipeIngredientRepository.findByRecipe(last).stream()
                    .map(ri -> ri.getIngredient().getId())
                    .collect(Collectors.toSet());
            lastIngIds.retainAll(fridgeIngredientIds);
            nextCursor = new MatchCountCursorResponse(lastIngIds.size(), last.getId());
        }

        return new RecipeMatchResponse(items, hasNext, nextCursor);
    }

    private List<Recipe> fetchLatestRecipes(RecipeSearchRequest request) {
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

    private List<Recipe> fetchMatchCountRecipes(RecipeSearchRequest request, Set<Long> fridgeIngredientIds) {
        PageRequest pageable = PageRequest.of(0, request.size() + 1);
        boolean hasKeyword = request.keyword() != null && !request.keyword().isBlank();
        boolean hasCursor = request.cursorMatchCount() != null;

        if (!hasCursor && !hasKeyword) {
            return recipeRepository.findTopByMatchCount(fridgeIngredientIds, pageable);
        } else if (!hasCursor) {
            return recipeRepository.findTopByMatchCountWithKeyword(fridgeIngredientIds, request.keyword(), pageable);
        } else if (!hasKeyword) {
            return recipeRepository.findNextByMatchCount(fridgeIngredientIds, request.cursorMatchCount(), request.cursorId(), pageable);
        } else {
            return recipeRepository.findNextByMatchCountWithKeyword(fridgeIngredientIds, request.keyword(), request.cursorMatchCount(), request.cursorId(), pageable);
        }
    }

    private RecipeSearchItemResponse toItemResponse(Recipe recipe, Set<Long> fridgeIngredientIds, Profile profile) {
        String thumbnailUrl = recipe.getRecipeImage() != null
                ? fileUploadService.getPublicUrl(recipe.getRecipeImage().getS3Key())
                : fileUploadService.getDefaultProfileImageUrl();

        String source = recipe instanceof RecipeWithLink rwl ? rwl.getUrlSource() : null;

        int totalIngredientCount = recipeIngredientRepository.countByRecipe(recipe);

        Set<Long> recipeIngredientIds = recipeIngredientRepository.findByRecipe(recipe).stream()
                .map(ri -> ri.getIngredient().getId())
                .collect(Collectors.toSet());
        recipeIngredientIds.retainAll(fridgeIngredientIds);
        int matchedIngredientCount = recipeIngredientIds.size();

        int likeCount = (int) profileFavoriteRecipeRepository.countByRecipe(recipe);
        int reviewCount = (int) recipeReviewRepository.countByRecipeId(recipe.getId());
        boolean liked = profileFavoriteRecipeRepository.existsByProfileAndRecipe(profile, recipe);

        return new RecipeSearchItemResponse(
                recipe.getId(),
                recipe.getTitle(),
                thumbnailUrl,
                source,
                recipe.getCookingTime(),
                recipe.getServings(),
                recipe.getDifficulty().getDescription(),
                likeCount,
                reviewCount,
                totalIngredientCount,
                matchedIngredientCount,
                liked
        );
    }

    private void validateLatestCursor(RecipeSearchRequest request) {
        boolean hasCursorCreatedAt = request.cursorCreatedAt() != null;
        boolean hasCursorId = request.cursorId() != null;
        if (hasCursorCreatedAt != hasCursorId) {
            throw new ApiException(RecipeErrorCode.INVALID_CURSOR);
        }
    }

    private void validateMatchCountCursor(RecipeSearchRequest request) {
        boolean hasCursorMatchCount = request.cursorMatchCount() != null;
        boolean hasCursorId = request.cursorId() != null;
        if (hasCursorMatchCount != hasCursorId) {
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
