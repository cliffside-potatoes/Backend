package com.potatoes.Naengu.recipe.repository;

import com.potatoes.Naengu.recipe.domain.model.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByValue(String value);
}
