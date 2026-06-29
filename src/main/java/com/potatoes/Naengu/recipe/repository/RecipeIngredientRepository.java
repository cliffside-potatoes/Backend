package com.potatoes.Naengu.recipe.repository;

import com.potatoes.Naengu.recipe.domain.model.Recipe;
import com.potatoes.Naengu.recipe.domain.model.RecipeIngredient;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {

    List<RecipeIngredient> findByRecipe(Recipe recipe);

    int countByRecipe(Recipe recipe);

    @Query("SELECT ri.recipe.id, COUNT(ri) FROM RecipeIngredient ri WHERE ri.recipe.id IN :recipeIds GROUP BY ri.recipe.id")
    List<Object[]> countByRecipeIds(@Param("recipeIds") List<Long> recipeIds);

    @Query("""
            SELECT ri.recipe.id, COUNT(ri)
            FROM RecipeIngredient ri
            WHERE ri.recipe.id IN :recipeIds
              AND ri.ingredient.id IN :fridgeIngredientIds
            GROUP BY ri.recipe.id
            """)
    List<Object[]> countMatchedByRecipeIds(
            @Param("recipeIds") List<Long> recipeIds,
            @Param("fridgeIngredientIds") Set<Long> fridgeIngredientIds
    );
}
