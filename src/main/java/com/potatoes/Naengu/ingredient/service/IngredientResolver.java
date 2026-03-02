package com.potatoes.Naengu.ingredient.service;

import static com.potatoes.Naengu.global.exception.CommonErrorCode.INVALID_INPUT;
import static com.potatoes.Naengu.global.exception.SystemErrorCode.DATABASE_INCONSISTENCY;

import com.potatoes.Naengu.ingredient.domain.model.Ingredient;
import com.potatoes.Naengu.ingredient.domain.model.IngredientAlias;
import com.potatoes.Naengu.ingredient.repository.IngredientAliasRepository;
import com.potatoes.Naengu.ingredient.repository.IngredientRepository;
import com.potatoes.Naengu.global.exception.ApiException;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class IngredientResolver {

    private final IngredientAliasRepository aliasRepository;
    private final IngredientRepository ingredientRepository;


    public IngredientResolver(IngredientAliasRepository aliasRepository, IngredientRepository ingredientRepository) {
        this.aliasRepository = aliasRepository;
        this.ingredientRepository = ingredientRepository;
    }

    /**
     * 1) alias 조회
     * 2) 없으면 ingredient 조회
     * 3) 없으면 ingredient 생성 -> 이러면 매핑 되지 않는 재료들만 생기는거 아닌가?
     */
    @Transactional
    public Ingredient resolverOrCreateByInput(String input) {
        String token = normalize(input);
        validateToken(token);

        Optional<Ingredient> byAlias = findIngredientByAlias(token);
        if (byAlias.isPresent()) {
            return byAlias.get();
        }

        Optional<Ingredient> byName = ingredientRepository.findByName(token);
        return byName.orElseGet(() -> saveOrLoadExisting(token));

    }

    private Optional<Ingredient> findIngredientByAlias(String token) {
        Optional<IngredientAlias> alias = aliasRepository.findByAliasName(token);
        if (alias.isEmpty()) {
            return Optional.empty();
        }

        Ingredient ingredient = alias.get().getIngredient();
        if (ingredient == null) {
            throw new ApiException(DATABASE_INCONSISTENCY);
        }
        return Optional.of(ingredient);
    }

    private Ingredient saveOrLoadExisting(String token) {
        try {
            return ingredientRepository.save(Ingredient.of(token));
        } catch (DataIntegrityViolationException e) {
            return ingredientRepository.findByName(token)
                    .orElseThrow(() -> new ApiException(DATABASE_INCONSISTENCY));
        }
    }

    private void validateToken(String token) {
        if (token.isBlank()) {
            throw new ApiException(INVALID_INPUT);
        }
    }

    private String normalize(String input) {
        if (input == null) {
            return "";
        }
        return input.trim();
    }

}
