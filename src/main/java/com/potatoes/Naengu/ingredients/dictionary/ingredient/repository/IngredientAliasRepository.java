package com.potatoes.Naengu.ingredients.dictionary.ingredient.repository;

import com.potatoes.Naengu.ingredients.dictionary.ingredient.domain.IngredientAlias;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientAliasRepository extends JpaRepository<IngredientAlias, Long> {
    Optional<IngredientAlias> findByAliasName(String aliasName);

}
