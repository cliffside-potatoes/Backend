package com.potatoes.Naengu.ingredients.dictionary.ingredient.service;


import static com.potatoes.Naengu.global.exception.CommonErrorCode.INVALID_INPUT;
import static com.potatoes.Naengu.global.exception.SystemErrorCode.DATABASE_INCONSISTENCY;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.potatoes.Naengu.ingredient.domain.model.Ingredient;
import com.potatoes.Naengu.ingredient.domain.model.IngredientAlias;
import com.potatoes.Naengu.ingredient.repository.IngredientAliasRepository;
import com.potatoes.Naengu.ingredient.repository.IngredientRepository;
import com.potatoes.Naengu.ingredient.service.IngredientResolver;
import com.potatoes.Naengu.global.exception.ApiException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

class IngredientResolverTest {

    @Mock
    IngredientAliasRepository aliasRepository;

    @Mock
    IngredientRepository ingredientRepository;

    @InjectMocks
    IngredientResolver resolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("input이 null이면 INVALID_INPUT 예외")
    void nullInput_throwsInvalidInput() {
        assertThatThrownBy(() -> resolver.resolverOrCreateByInput(null))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(INVALID_INPUT);

        verifyNoInteractions(aliasRepository, ingredientRepository);

    }

    @Test
    @DisplayName("input이 공백이면 INVALID_INPUT 예외")
    void blankInput_throwsInvalidInput() {
        assertThatThrownBy(() -> resolver.resolverOrCreateByInput("   "))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(INVALID_INPUT);

        verifyNoInteractions(aliasRepository, ingredientRepository);
    }

    @Test
    @DisplayName("alias로 재료를 찾으면 바로 반환")
    void foundByAlias_returnsIngredient() {
        Ingredient ingredient = Ingredient.of("토마토");

        IngredientAlias alias = mock(IngredientAlias.class);
        when(alias.getIngredient()).thenReturn(ingredient);
        when(aliasRepository.findByAliasName("토마토"))
                .thenReturn(Optional.of(alias));

        Ingredient result = resolver.resolverOrCreateByInput("  토마토  ");

        assertThat(result).isSameAs(ingredient);
        verify(aliasRepository).findByAliasName("토마토");
        verifyNoInteractions(ingredientRepository);
    }

    @Test
    @DisplayName("alias는 있는데 ingredient가 null이면 DATABASE_INCONSISTENCY")
    void aliasIngredientNull_throwsInconsistency() {
        IngredientAlias alias = mock(IngredientAlias.class);
        when(alias.getIngredient()).thenReturn(null);
        when(aliasRepository.findByAliasName("토마토"))
                .thenReturn(Optional.of(alias));

        assertThatThrownBy(() -> resolver.resolverOrCreateByInput("토마토"))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(DATABASE_INCONSISTENCY);
    }

    @Test
    @DisplayName("alias 없고 name으로 찾으면 반환")
    void foundByName_returnsIngredient() {
        Ingredient ingredient = Ingredient.of("토마토");

        when(aliasRepository.findByAliasName("토마토"))
                .thenReturn(Optional.empty());
        when(ingredientRepository.findByName("토마토"))
                .thenReturn(Optional.of(ingredient));

        Ingredient result = resolver.resolverOrCreateByInput("토마토");

        assertThat(result).isSameAs(ingredient);
        verify(ingredientRepository).findByName("토마토");
        verify(ingredientRepository, never()).save(any());
    }

    @Test
    @DisplayName("alias도 없고 name도 없으면 저장")
    void notFount_saveNewIngredient() {
        Ingredient saved = Ingredient.of("토마토");

        when(aliasRepository.findByAliasName("토마토"))
                .thenReturn(Optional.empty());
        when(ingredientRepository.findByName("토마토"))
                .thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class)))
                .thenReturn(saved);

        Ingredient result = resolver.resolverOrCreateByInput("토마토");

        assertThat(result).isSameAs(saved);
        verify(ingredientRepository).save(any(Ingredient.class));
    }

    @Test
    @DisplayName("저장 시 유니크 충돌 발생하면 재조회 후 반환")
    void saveConflict_thenLoadExisting() {
        Ingredient existing = Ingredient.of("토마토");

        when(aliasRepository.findByAliasName("토마토"))
                .thenReturn(Optional.empty());

        when(ingredientRepository.findByName("토마토"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existing));

        when(ingredientRepository.save(any(Ingredient.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        Ingredient result = resolver.resolverOrCreateByInput("토마토");

        assertThat(result).isSameAs(existing);
        verify(ingredientRepository, times(2)).findByName("토마토");

    }

    @Test
    @DisplayName("저장 충돌 후 재조회도 실패하면 DATABASE_INCONSISTENCY")
    void saveConflict_stillNotFound_throwsInconsistency() {
        when(aliasRepository.findByAliasName("토마토"))
                .thenReturn(Optional.empty());

        when(ingredientRepository.findByName("토마토"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());

        when(ingredientRepository.save(any(Ingredient.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> resolver.resolverOrCreateByInput("토마토"))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(DATABASE_INCONSISTENCY);
    }
}