package com.potatoes.Naengu.recipe.query;

import com.potatoes.Naengu.file.service.FileUploadService;
import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.fridge.repository.FridgeIngredientRepository;
import com.potatoes.Naengu.global.dto.LikeCountCursorResponse;
import com.potatoes.Naengu.global.dto.MatchCountCursorResponse;
import com.potatoes.Naengu.global.dto.CursorResponse;
import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import com.potatoes.Naengu.recipe.domain.model.ProfileFavoriteRecipe;
import com.potatoes.Naengu.recipe.domain.model.Recipe;
import com.potatoes.Naengu.recipe.domain.model.RecipeWithLink;
import com.potatoes.Naengu.recipe.domain.vo.RecipeSortType;
import com.potatoes.Naengu.recipe.dto.FavoriteRecipeItem;
import com.potatoes.Naengu.recipe.dto.FavoriteRecipesResponse;
import com.potatoes.Naengu.recipe.dto.RecipeLikeResponse;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeQueryService {

    private static final String MATCH_RANKING_KEY_PREFIX = "match:ranking:";

    private final RecipeRepository recipeRepository;
    private final RecipeTagRepository recipeTagRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final ProfileFavoriteRecipeRepository profileFavoriteRecipeRepository;
    private final RecipeReviewRepository recipeReviewRepository;
    private final ProfileRepository profileRepository;
    private final FridgeIngredientRepository fridgeIngredientRepository;
    private final FileUploadService fileUploadService;
    private final RedisTemplate<String, String> redisTemplate;

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

        List<Long> recipeIds = recipes.stream().map(Recipe::getId).toList();
        Map<Long, Integer> ingredientCountMap = batchCountIngredients(recipeIds);
        Map<Long, Integer> matchedCountMap    = batchCountMatched(recipeIds, fridgeIngredientIds);
        Map<Long, Integer> reviewCountMap     = batchCountReviews(recipeIds);
        Set<Long> likedRecipeIds              = batchFindLiked(profile, recipeIds);

        List<RecipeSearchItemResponse> items = recipes.stream()
                .map(recipe -> toItemResponse(recipe, ingredientCountMap, matchedCountMap, reviewCountMap, likedRecipeIds))
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

        boolean hasKeyword = request.keyword() != null && !request.keyword().isBlank();
        if (!hasKeyword) {
            RecipeMatchResponse cached = searchByMatchCountFromRedis(profile, request);
            if (cached != null) return cached;
        }

        return searchByMatchCountFromDb(profile, request);
    }

    private RecipeMatchResponse searchByMatchCountFromRedis(Profile profile, RecipeSearchRequest request) {
        Fridge fridge = profile.getFridge();
        String key = MATCH_RANKING_KEY_PREFIX + fridge.getId();
        long count = request.size() + 1L;

        Set<ZSetOperations.TypedTuple<String>> tuples;
        if (request.cursorMatchCount() != null) {
            tuples = redisTemplate.opsForZSet()
                    .reverseRangeByScoreWithScores(key, 0, request.cursorMatchCount() - 1, 0, count);
        } else {
            tuples = redisTemplate.opsForZSet()
                    .reverseRangeWithScores(key, 0, count - 1);
        }

        if (tuples == null || tuples.isEmpty()) return null;

        List<Long> recipeIds = tuples.stream()
                .map(t -> Long.valueOf(t.getValue()))
                .toList();

        Map<Long, Double> scoreMap = tuples.stream()
                .collect(Collectors.toMap(
                        t -> Long.valueOf(t.getValue()),
                        ZSetOperations.TypedTuple::getScore
                ));

        Map<Long, Recipe> recipeMap = recipeRepository.findAllById(recipeIds)
                .stream()
                .collect(Collectors.toMap(Recipe::getId, r -> r));

        List<Recipe> recipes = recipeIds.stream()
                .map(recipeMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        boolean hasNext = recipes.size() > request.size();
        if (hasNext) recipes = recipes.subList(0, request.size());

        Set<Long> fridgeIngredientIds = fridgeIngredientRepository
                .findAllByFridgeCategory_Fridge(fridge)
                .stream()
                .map(fi -> fi.getIngredient().getId())
                .collect(Collectors.toSet());

        List<Long> finalIds = recipes.stream().map(Recipe::getId).toList();
        Map<Long, Integer> ingredientCountMap = batchCountIngredients(finalIds);
        Map<Long, Integer> matchedCountMap    = batchCountMatched(finalIds, fridgeIngredientIds);
        Map<Long, Integer> reviewCountMap     = batchCountReviews(finalIds);
        Set<Long> likedRecipeIds              = batchFindLiked(profile, finalIds);

        List<RecipeSearchItemResponse> items = recipes.stream()
                .map(r -> toItemResponse(r, ingredientCountMap, matchedCountMap, reviewCountMap, likedRecipeIds))
                .toList();

        MatchCountCursorResponse nextCursor = null;
        if (hasNext && !recipes.isEmpty()) {
            Recipe last = recipes.get(recipes.size() - 1);
            double lastScore = scoreMap.getOrDefault(last.getId(), 0.0);
            nextCursor = new MatchCountCursorResponse((int) lastScore, last.getId());
        }

        return new RecipeMatchResponse(items, hasNext, nextCursor);
    }

    private RecipeMatchResponse searchByMatchCountFromDb(Profile profile, RecipeSearchRequest request) {
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

        List<Long> recipeIds = recipes.stream().map(Recipe::getId).toList();
        Map<Long, Integer> ingredientCountMap = batchCountIngredients(recipeIds);
        Map<Long, Integer> matchedCountMap    = batchCountMatched(recipeIds, fridgeIngredientIds);
        Map<Long, Integer> reviewCountMap     = batchCountReviews(recipeIds);
        Set<Long> likedRecipeIds              = batchFindLiked(profile, recipeIds);

        List<RecipeSearchItemResponse> items = recipes.stream()
                .map(recipe -> toItemResponse(recipe, ingredientCountMap, matchedCountMap, reviewCountMap, likedRecipeIds))
                .toList();

        MatchCountCursorResponse nextCursor = null;
        if (hasNext && !recipes.isEmpty()) {
            Recipe last = recipes.get(recipes.size() - 1);
            nextCursor = new MatchCountCursorResponse(
                    matchedCountMap.getOrDefault(last.getId(), 0),
                    last.getId()
            );
        }

        return new RecipeMatchResponse(items, hasNext, nextCursor);
    }

    @Transactional(readOnly = true)
    public RecipeLikeResponse searchByLikeCount(Long userId, RecipeSearchRequest request) {
        validateLikeCountCursor(request);

        Profile profile = profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(RecipeErrorCode.RECIPE_NOT_FOUND));

        Set<Long> fridgeIngredientIds = fridgeIngredientRepository
                .findAllByFridgeCategory_Fridge(profile.getFridge())
                .stream()
                .map(fi -> fi.getIngredient().getId())
                .collect(Collectors.toSet());

        List<Recipe> recipes = fetchLikeCountRecipes(request);

        boolean hasNext = recipes.size() > request.size();
        if (hasNext) {
            recipes = recipes.subList(0, request.size());
        }

        List<Long> recipeIds = recipes.stream().map(Recipe::getId).toList();
        Map<Long, Integer> ingredientCountMap = batchCountIngredients(recipeIds);
        Map<Long, Integer> matchedCountMap    = batchCountMatched(recipeIds, fridgeIngredientIds);
        Map<Long, Integer> reviewCountMap     = batchCountReviews(recipeIds);
        Set<Long> likedRecipeIds              = batchFindLiked(profile, recipeIds);

        List<RecipeSearchItemResponse> items = recipes.stream()
                .map(recipe -> toItemResponse(recipe, ingredientCountMap, matchedCountMap, reviewCountMap, likedRecipeIds))
                .toList();

        LikeCountCursorResponse nextCursor = null;
        if (hasNext && !recipes.isEmpty()) {
            Recipe last = recipes.get(recipes.size() - 1);
            nextCursor = new LikeCountCursorResponse(last.getLikeCount(), last.getId());
        }

        return new RecipeLikeResponse(items, hasNext, nextCursor);
    }

    @Transactional(readOnly = true)
    public RecipeLikeResponse searchByLikeCountAnonymous(RecipeSearchRequest request) {
        validateLikeCountCursor(request);

        List<Recipe> recipes = fetchLikeCountRecipes(request);

        boolean hasNext = recipes.size() > request.size();
        if (hasNext) {
            recipes = recipes.subList(0, request.size());
        }

        List<Long> recipeIds = recipes.stream().map(Recipe::getId).toList();
        Map<Long, Integer> ingredientCountMap = batchCountIngredients(recipeIds);
        Map<Long, Integer> reviewCountMap     = batchCountReviews(recipeIds);

        List<RecipeSearchItemResponse> items = recipes.stream()
                .map(recipe -> toItemResponseAnonymous(recipe, ingredientCountMap, reviewCountMap))
                .toList();

        LikeCountCursorResponse nextCursor = null;
        if (hasNext && !recipes.isEmpty()) {
            Recipe last = recipes.get(recipes.size() - 1);
            nextCursor = new LikeCountCursorResponse(last.getLikeCount(), last.getId());
        }

        return new RecipeLikeResponse(items, hasNext, nextCursor);
    }

    private RecipeSearchItemResponse toItemResponseAnonymous(
            Recipe recipe,
            Map<Long, Integer> ingredientCountMap,
            Map<Long, Integer> reviewCountMap) {

        String thumbnailUrl = recipe.getRecipeImage() != null
                ? fileUploadService.getPublicUrl(recipe.getRecipeImage().getS3Key())
                : fileUploadService.getDefaultProfileImageUrl();

        String source = recipe instanceof RecipeWithLink rwl ? rwl.getUrlSource() : null;

        int totalIngredientCount = ingredientCountMap.getOrDefault(recipe.getId(), 0);
        int likeCount = recipe.getLikeCount();
        int reviewCount = reviewCountMap.getOrDefault(recipe.getId(), 0);

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
                0,
                false
        );
    }

    private String sanitizeKeyword(String keyword) {
        return keyword.replaceAll("[+\\-><()~*\"@]", " ").trim();
    }

    private List<Recipe> fetchLikeCountRecipes(RecipeSearchRequest request) {
        int limit = request.size() + 1;
        boolean hasKeyword = request.keyword() != null && !request.keyword().isBlank();
        boolean hasCursor = request.cursorLikeCount() != null;

        if (!hasKeyword) {
            PageRequest pageable = PageRequest.of(0, limit);
            if (!hasCursor) return recipeRepository.findTopByLikeCount(pageable);
            return recipeRepository.findNextByLikeCount(request.cursorLikeCount(), request.cursorId(), pageable);
        }

        String keyword = sanitizeKeyword(request.keyword());
        List<Long> ids;
        if (!hasCursor) {
            ids = recipeRepository.findIdsByKeywordLikeCount(keyword, limit);
        } else {
            ids = recipeRepository.findIdsByKeywordLikeCountAfterCursor(keyword, request.cursorLikeCount(), request.cursorId(), limit);
        }
        if (ids.isEmpty()) return List.of();
        return recipeRepository.findByIdsOrderByLikeCount(ids);
    }

    private List<Recipe> fetchLatestRecipes(RecipeSearchRequest request) {
        int limit = request.size() + 1;
        boolean hasKeyword = request.keyword() != null && !request.keyword().isBlank();

        if (!hasKeyword) {
            PageRequest pageable = PageRequest.of(0, limit);
            if (request.cursorCreatedAt() == null) return recipeRepository.findLatestAll(pageable);
            LocalDateTime cursorTime = parseCursorTime(request.cursorCreatedAt());
            return recipeRepository.findLatestAfterCursor(cursorTime, request.cursorId(), pageable);
        }

        String keyword = sanitizeKeyword(request.keyword());
        List<Long> ids;
        if (request.cursorCreatedAt() == null) {
            ids = recipeRepository.findIdsByKeywordLatest(keyword, limit);
        } else {
            LocalDateTime cursorTime = parseCursorTime(request.cursorCreatedAt());
            ids = recipeRepository.findIdsByKeywordLatestAfterCursor(keyword, cursorTime, request.cursorId(), limit);
        }
        if (ids.isEmpty()) return List.of();
        return recipeRepository.findByIdsOrderByLatest(ids);
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

    private RecipeSearchItemResponse toItemResponse(
            Recipe recipe,
            Map<Long, Integer> ingredientCountMap,
            Map<Long, Integer> matchedCountMap,
            Map<Long, Integer> reviewCountMap,
            Set<Long> likedRecipeIds) {

        String thumbnailUrl = recipe.getRecipeImage() != null
                ? fileUploadService.getPublicUrl(recipe.getRecipeImage().getS3Key())
                : fileUploadService.getDefaultProfileImageUrl();

        String source = recipe instanceof RecipeWithLink rwl ? rwl.getUrlSource() : null;

        int totalIngredientCount   = ingredientCountMap.getOrDefault(recipe.getId(), 0);
        int matchedIngredientCount = matchedCountMap.getOrDefault(recipe.getId(), 0);
        int likeCount              = recipe.getLikeCount();
        int reviewCount            = reviewCountMap.getOrDefault(recipe.getId(), 0);
        boolean liked              = likedRecipeIds.contains(recipe.getId());

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

    private void validateLikeCountCursor(RecipeSearchRequest request) {
        boolean hasCursorLikeCount = request.cursorLikeCount() != null;
        boolean hasCursorId = request.cursorId() != null;
        if (hasCursorLikeCount != hasCursorId) {
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

    @Transactional(readOnly = true)
    public RecipeSearchResponse searchByCategoryLatest(Long userId, RecipeSearchRequest request) {
        validateLatestCursor(request);

        Profile profile = profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(RecipeErrorCode.RECIPE_NOT_FOUND));

        Set<Long> fridgeIngredientIds = loadFridgeIngredientIds(profile);

        PageRequest pageable = PageRequest.of(0, request.size() + 1);
        List<Recipe> recipes = (request.cursorCreatedAt() == null)
                ? recipeRepository.findLatestByCategory(request.category(), pageable)
                : recipeRepository.findLatestByCategoryAfterCursor(
                        request.category(), parseCursorTime(request.cursorCreatedAt()), request.cursorId(), pageable);

        boolean hasNext = recipes.size() > request.size();
        if (hasNext) {
            recipes = recipes.subList(0, request.size());
        }

        List<Long> recipeIds = recipes.stream().map(Recipe::getId).toList();
        Map<Long, Integer> ingredientCountMap = batchCountIngredients(recipeIds);
        Map<Long, Integer> matchedCountMap    = batchCountMatched(recipeIds, fridgeIngredientIds);
        Map<Long, Integer> reviewCountMap     = batchCountReviews(recipeIds);
        Set<Long> likedRecipeIds              = batchFindLiked(profile, recipeIds);

        List<RecipeSearchItemResponse> items = recipes.stream()
                .map(recipe -> toItemResponse(recipe, ingredientCountMap, matchedCountMap, reviewCountMap, likedRecipeIds))
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
    public RecipeLikeResponse searchByCategoryByLikeCount(Long userId, RecipeSearchRequest request) {
        validateLikeCountCursor(request);

        Profile profile = profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(RecipeErrorCode.RECIPE_NOT_FOUND));

        Set<Long> fridgeIngredientIds = loadFridgeIngredientIds(profile);

        PageRequest pageable = PageRequest.of(0, request.size() + 1);
        List<Recipe> recipes = (request.cursorLikeCount() == null)
                ? recipeRepository.findByLikeCountAndCategory(request.category(), pageable)
                : recipeRepository.findNextByLikeCountAndCategory(
                        request.category(), request.cursorLikeCount(), request.cursorId(), pageable);

        boolean hasNext = recipes.size() > request.size();
        if (hasNext) {
            recipes = recipes.subList(0, request.size());
        }

        List<Long> recipeIds = recipes.stream().map(Recipe::getId).toList();
        Map<Long, Integer> ingredientCountMap = batchCountIngredients(recipeIds);
        Map<Long, Integer> matchedCountMap    = batchCountMatched(recipeIds, fridgeIngredientIds);
        Map<Long, Integer> reviewCountMap     = batchCountReviews(recipeIds);
        Set<Long> likedRecipeIds              = batchFindLiked(profile, recipeIds);

        List<RecipeSearchItemResponse> items = recipes.stream()
                .map(recipe -> toItemResponse(recipe, ingredientCountMap, matchedCountMap, reviewCountMap, likedRecipeIds))
                .toList();

        LikeCountCursorResponse nextCursor = null;
        if (hasNext && !recipes.isEmpty()) {
            Recipe last = recipes.get(recipes.size() - 1);
            nextCursor = new LikeCountCursorResponse(last.getLikeCount(), last.getId());
        }

        return new RecipeLikeResponse(items, hasNext, nextCursor);
    }

    @Transactional(readOnly = true)
    public FavoriteRecipesResponse getFavorites(Long userId, int size, String cursorCreatedAt, Long cursorId) {
        if ((cursorCreatedAt == null) != (cursorId == null)) {
            throw new ApiException(RecipeErrorCode.INVALID_CURSOR);
        }

        Profile profile = profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(RecipeErrorCode.RECIPE_NOT_FOUND));

        Set<Long> fridgeIngredientIds = fridgeIngredientRepository
                .findAllByFridgeCategory_Fridge(profile.getFridge())
                .stream()
                .map(fi -> fi.getIngredient().getId())
                .collect(Collectors.toSet());

        PageRequest pageable = PageRequest.of(0, size + 1);
        List<ProfileFavoriteRecipe> favorites = (cursorCreatedAt == null)
                ? profileFavoriteRecipeRepository.findByProfileLatest(profile, pageable)
                : profileFavoriteRecipeRepository.findByProfileAfterCursor(
                        profile, parseCursorTime(cursorCreatedAt), cursorId, pageable);

        boolean hasNext = favorites.size() > size;
        if (hasNext) {
            favorites = favorites.subList(0, size);
        }

        List<Long> recipeIds = favorites.stream().map(pfr -> pfr.getRecipe().getId()).toList();
        Map<Long, Integer> ingredientCountMap = batchCountIngredients(recipeIds);
        Map<Long, Integer> matchedCountMap    = batchCountMatched(recipeIds, fridgeIngredientIds);
        Map<Long, Integer> reviewCountMap     = batchCountReviews(recipeIds);

        List<FavoriteRecipeItem> items = favorites.stream()
                .map(pfr -> toFavoriteItem(pfr.getRecipe(), ingredientCountMap, matchedCountMap, reviewCountMap))
                .toList();

        CursorResponse nextCursor = null;
        if (hasNext && !favorites.isEmpty()) {
            ProfileFavoriteRecipe last = favorites.get(favorites.size() - 1);
            nextCursor = new CursorResponse(
                    last.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    last.getId()
            );
        }

        return new FavoriteRecipesResponse(items, hasNext, nextCursor);
    }

    private FavoriteRecipeItem toFavoriteItem(
            Recipe recipe,
            Map<Long, Integer> ingredientCountMap,
            Map<Long, Integer> matchedCountMap,
            Map<Long, Integer> reviewCountMap) {

        String thumbnailUrl = recipe.getRecipeImage() != null
                ? fileUploadService.getPublicUrl(recipe.getRecipeImage().getS3Key())
                : fileUploadService.getDefaultProfileImageUrl();

        String source = recipe instanceof RecipeWithLink rwl ? rwl.getUrlSource() : null;

        int totalIngredientCount   = ingredientCountMap.getOrDefault(recipe.getId(), 0);
        int matchedIngredientCount = matchedCountMap.getOrDefault(recipe.getId(), 0);
        int likeCount              = recipe.getLikeCount();
        int reviewCount            = reviewCountMap.getOrDefault(recipe.getId(), 0);

        return new FavoriteRecipeItem(
                recipe.getId(),
                recipe.getTitle(),
                thumbnailUrl,
                source,
                recipe.getCookingTime(),
                recipe.getDifficulty().getDescription(),
                likeCount,
                reviewCount,
                totalIngredientCount,
                matchedIngredientCount,
                true
        );
    }

    private Map<Long, Integer> batchCountIngredients(List<Long> recipeIds) {
        if (recipeIds.isEmpty()) return Map.of();
        return recipeIngredientRepository.countByRecipeIds(recipeIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));
    }

    private Map<Long, Integer> batchCountMatched(List<Long> recipeIds, Set<Long> fridgeIngredientIds) {
        if (recipeIds.isEmpty() || fridgeIngredientIds.isEmpty()) return Map.of();
        return recipeIngredientRepository.countMatchedByRecipeIds(recipeIds, fridgeIngredientIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));
    }

    private Map<Long, Integer> batchCountReviews(List<Long> recipeIds) {
        if (recipeIds.isEmpty()) return Map.of();
        return recipeReviewRepository.countByRecipeIds(recipeIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));
    }

    private Set<Long> batchFindLiked(Profile profile, List<Long> recipeIds) {
        if (recipeIds.isEmpty()) return Set.of();
        return profileFavoriteRecipeRepository.findLikedRecipeIds(profile, recipeIds);
    }

    private Set<Long> loadFridgeIngredientIds(Profile profile) {
        return fridgeIngredientRepository
                .findAllByFridgeCategory_Fridge(profile.getFridge())
                .stream()
                .map(fi -> fi.getIngredient().getId())
                .collect(Collectors.toSet());
    }

    private LocalDateTime parseCursorTime(String cursorCreatedAt) {
        try {
            return LocalDateTime.parse(cursorCreatedAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new ApiException(RecipeErrorCode.INVALID_CURSOR);
        }
    }
}
