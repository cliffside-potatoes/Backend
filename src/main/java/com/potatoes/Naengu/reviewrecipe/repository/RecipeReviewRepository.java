package com.potatoes.Naengu.reviewrecipe.repository;

import com.potatoes.Naengu.reviewrecipe.domain.model.RecipeReview;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeReviewRepository extends JpaRepository<RecipeReview, Long> {

    boolean existsByProfileIdAndRecipeId(Long profileId, Long recipeId);

    boolean existsByRecipeId(Long recipeId);

    long countByRecipeId(Long recipeId);

    List<RecipeReview> findByRecipeIdOrderByCreatedAtDescIdDesc(Long recipeId, Pageable pageable);

    @Query("""
        select rr
        from RecipeReview rr
        where rr.recipe.id = :recipeId
          and (
                rr.createdAt < :cursorUpdatedAt
                or (rr.createdAt = :cursorUpdatedAt and rr.id < :cursorId)
          )
        order by rr.createdAt desc, rr.id desc
        """)
    List<RecipeReview> findLatestNextPage(
            @Param("recipeId") Long recipeId,
            @Param("cursorUpdatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

}
