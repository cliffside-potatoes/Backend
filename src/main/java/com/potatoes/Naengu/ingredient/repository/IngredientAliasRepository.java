package com.potatoes.Naengu.ingredient.repository;

import com.potatoes.Naengu.ingredient.domain.model.IngredientAlias;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientAliasRepository extends JpaRepository<IngredientAlias, Long> {
    Optional<IngredientAlias> findByAliasName(String aliasName);

}
