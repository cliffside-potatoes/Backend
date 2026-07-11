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

    long countByRecipeId(Long recipeId);

    @Query("SELECT r.recipe.id, COUNT(r) FROM RecipeReview r WHERE r.recipe.id IN :recipeIds GROUP BY r.recipe.id")
    List<Object[]> countByRecipeIds(@Param("recipeIds") List<Long> recipeIds);

    List<RecipeReview> findByRecipeIdOrderByUpdatedAtDescIdDesc(Long recipeId, Pageable pageable);

    @Query("""
        select rr
        from RecipeReview rr
        where rr.recipe.id = :recipeId
          and (
                rr.updatedAt < :cursorUpdatedAt
                or (rr.updatedAt = :cursorUpdatedAt and rr.id < :cursorId)
          )
        order by rr.updatedAt desc, rr.id desc
        """)
    List<RecipeReview> findLatestNextPage(
            @Param("recipeId") Long recipeId,
            @Param("cursorUpdatedAt") LocalDateTime cursorUpdatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    List<RecipeReview> findByRecipeIdOrderByLikeCountDescUpdatedAtDescIdDesc(Long recipeId, Pageable pageable);

    @Query("""
        select rr
        from RecipeReview rr
        where rr.recipe.id = :recipeId
          and (
                rr.likeCount < :cursorLikeCount
                or (rr.likeCount = :cursorLikeCount and rr.updatedAt < :cursorUpdatedAt)
                or (rr.likeCount = :cursorLikeCount and rr.updatedAt = :cursorUpdatedAt and rr.id < :cursorId)
          )
        order by rr.likeCount desc, rr.updatedAt desc, rr.id desc
        """)
    List<RecipeReview> findLikeNextPage(
            @Param("recipeId") Long recipeId,
            @Param("cursorLikeCount") Integer cursorLikeCount,
            @Param("cursorUpdatedAt") LocalDateTime cursorUpdatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

}
