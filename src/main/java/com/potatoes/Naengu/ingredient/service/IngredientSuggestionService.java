package com.potatoes.Naengu.ingredient.service;

import com.potatoes.Naengu.ingredient.domain.model.Ingredient;
import com.potatoes.Naengu.ingredient.dto.IngredientSuggestionItemResponse;
import com.potatoes.Naengu.ingredient.dto.IngredientSuggestionResponse;
import com.potatoes.Naengu.ingredient.repository.IngredientRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IngredientSuggestionService {

    private final IngredientRepository ingredientRepository;

    public IngredientSuggestionService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    public IngredientSuggestionResponse getSuggestions(String query) {
        if (isBlank(query)) {
            return IngredientSuggestionResponse.from(List.of());
        }

        List<IngredientSuggestionItemResponse> items = ingredientRepository
                .findTop10ByNameStartingWithOrderByNameAsc(query)
                .stream()
                .map(this::toResponse)
                .toList();

        return IngredientSuggestionResponse.from(items);
    }

    private IngredientSuggestionItemResponse toResponse(Ingredient ingredient) {
        return IngredientSuggestionItemResponse.from(
                ingredient.getId(),
                ingredient.getName()
        );
    }

    private boolean isBlank(String query) {
        return query == null || query.isBlank();
    }
}
