package com.potatoes.Naengu.recipe.service;

import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.fridge.repository.FridgeIngredientRepository;
import com.potatoes.Naengu.recipe.repository.RecipeIngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeMatchRankingService {

    private static final String KEY_PREFIX = "match:ranking:";
    private static final Duration TTL = Duration.ofDays(7);

    private final RedisTemplate<String, String> redisTemplate;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final FridgeIngredientRepository fridgeIngredientRepository;

    public void refresh(Fridge fridge) {
        Set<Long> ingredientIds = fridgeIngredientRepository
                .findAllByFridgeCategory_Fridge(fridge)
                .stream()
                .map(fi -> fi.getIngredient().getId())
                .collect(Collectors.toSet());

        String key = KEY_PREFIX + fridge.getId();
        redisTemplate.delete(key);

        if (ingredientIds.isEmpty()) {
            return;
        }

        List<Object[]> rows = recipeIngredientRepository.countMatchedByIngredientIds(ingredientIds);

        Set<ZSetOperations.TypedTuple<String>> tuples = rows.stream()
                .map(row -> ZSetOperations.TypedTuple.of(
                        row[0].toString(),
                        ((Long) row[1]).doubleValue()
                ))
                .collect(Collectors.toSet());

        redisTemplate.opsForZSet().add(key, tuples);
        redisTemplate.expire(key, TTL);
    }
}
