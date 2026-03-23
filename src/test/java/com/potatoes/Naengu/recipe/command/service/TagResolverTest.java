package com.potatoes.Naengu.recipe.command.service;


import static com.potatoes.Naengu.global.exception.SystemErrorCode.DATABASE_INCONSISTENCY;
import static com.potatoes.Naengu.recipe.exception.RecipeTagErrorCode.TAG_NOT_FOUND;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.recipe.domain.model.Tag;
import com.potatoes.Naengu.recipe.repository.TagRepository;
import com.potatoes.Naengu.recipe.service.TagResolver;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

class TagResolverTest {

    @Mock
    TagRepository tagRepository;

    @InjectMocks
    TagResolver tagResolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("value가 null이면 TAG_NOT_FOUND 예외")
    void nullValue_throwsTagNotFound() {
        assertThatThrownBy(() -> tagResolver.resolveOrCreate(null))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(TAG_NOT_FOUND);

        verifyNoInteractions(tagRepository);
    }

    @Test
    @DisplayName("value가 공백이면 TAG_NOT_FOUND 예외")
    void blankValue_throwsTagNotFound() {
        assertThatThrownBy(() -> tagResolver.resolveOrCreate("   "))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(TAG_NOT_FOUND);

        verifyNoInteractions(tagRepository);
    }

    @Test
    @DisplayName("이미 존재하면 저장하지 않고 그대로 반환한다")
    void found_returnsExisting_withoutSave() {
        Tag existing = Tag.of("한식");

        when(tagRepository.findByValue("한식"))
                .thenReturn(Optional.of(existing));

        Tag result = tagResolver.resolveOrCreate(" 한식 ");

        assertThat(result).isSameAs(existing);
        verify(tagRepository).findByValue("한식");
        verify(tagRepository, never()).save(any());
    }

    @Test
    @DisplayName("없으면 저장해서 반환한다")
    void notFound_saveNew() {
        Tag saved = Tag.of("한식");

        when(tagRepository.findByValue("한식"))
                .thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class)))
                .thenReturn(saved);

        Tag result = tagResolver.resolveOrCreate("한식");
        assertThat(result).isSameAs(saved);
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    @DisplayName("저장 시 유니크 충돌 발생하면 재조회해서 기존 값을 반환한다")
    void saveConflict_thenLoadExisting() {
        Tag existing = Tag.of("한식");

        when(tagRepository.findByValue("한식"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existing));

        when(tagRepository.save(any(Tag.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        Tag result = tagResolver.resolveOrCreate("한식");

        assertThat(result).isSameAs(existing);
        verify(tagRepository).save(any(Tag.class));
        verify(tagRepository, times(2)).findByValue("한식");
    }

    @Test
    @DisplayName("저장 충돌 후 재조회도 실패하면 DATABASE_INCONSISTENCY 예외")
    void saveConflict_stillNotFound_throwsInconsistency() {
        when(tagRepository.findByValue("한식"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());

        when(tagRepository.save(any(Tag.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> tagResolver.resolveOrCreate("한식"))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(DATABASE_INCONSISTENCY);
    }
}