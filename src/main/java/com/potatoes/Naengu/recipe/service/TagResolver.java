package com.potatoes.Naengu.recipe.service;

import static com.potatoes.Naengu.global.exception.SystemErrorCode.DATABASE_INCONSISTENCY;
import static com.potatoes.Naengu.recipe.exception.RecipeTagErrorCode.TAG_NOT_FOUND;

import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.recipe.domain.model.Tag;
import com.potatoes.Naengu.recipe.repository.TagRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class TagResolver {

    private final TagRepository tagRepository;

    public TagResolver(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional
    public Tag resolveOrCreate(String value) {
        String token = normalize(value);
        validateToken(token);

        Optional<Tag> found = tagRepository.findByValue(token);
        return found.orElseGet(() -> saveOrLoadExisting(token));

    }

    private Tag saveOrLoadExisting(String token) {
        try {
            return tagRepository.save(Tag.of(token));
        } catch (DataIntegrityViolationException e) {
            return tagRepository.findByValue(token)
                    .orElseThrow(() -> new ApiException(DATABASE_INCONSISTENCY));
        }
    }

    private void validateToken(String token) {
        if (token.isBlank()) {
            throw new ApiException(TAG_NOT_FOUND);
        }
    }

    private String normalize(String input) {
        if (input == null) {
            return "";
        }
        return input.trim();
    }
}
