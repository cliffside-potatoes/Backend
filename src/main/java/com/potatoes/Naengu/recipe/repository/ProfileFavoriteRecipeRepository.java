package com.potatoes.Naengu.recipe.repository;

import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.recipe.domain.model.ProfileFavoriteRecipe;
import com.potatoes.Naengu.recipe.domain.model.Recipe;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfileFavoriteRecipeRepository extends JpaRepository<ProfileFavoriteRecipe, Long> {

    boolean existsByProfileAndRecipe(Profile profile, Recipe recipe);

    Optional<ProfileFavoriteRecipe> findByProfileAndRecipe(Profile profile, Recipe recipe);

    long countByRecipe(Recipe recipe);

    @Query("""
            SELECT pfr.recipe.id FROM ProfileFavoriteRecipe pfr
            WHERE pfr.profile = :profile AND pfr.recipe.id IN :recipeIds
            """)
    Set<Long> findLikedRecipeIds(
            @Param("profile") Profile profile,
            @Param("recipeIds") List<Long> recipeIds
    );

    @Query("""
            SELECT pfr FROM ProfileFavoriteRecipe pfr
            WHERE pfr.profile = :profile
            ORDER BY pfr.createdAt DESC, pfr.id DESC
            """)
    List<ProfileFavoriteRecipe> findByProfileLatest(
            @Param("profile") Profile profile,
            Pageable pageable
    );

    @Query("""
            SELECT pfr FROM ProfileFavoriteRecipe pfr
            WHERE pfr.profile = :profile
              AND (pfr.createdAt < :cursorCreatedAt
                   OR (pfr.createdAt = :cursorCreatedAt AND pfr.id < :cursorId))
            ORDER BY pfr.createdAt DESC, pfr.id DESC
            """)
    List<ProfileFavoriteRecipe> findByProfileAfterCursor(
            @Param("profile") Profile profile,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
