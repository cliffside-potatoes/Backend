package com.potatoes.Naengu.recipe.repository;

import com.potatoes.Naengu.recipe.domain.model.Recipe;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Query("SELECT r FROM Recipe r ORDER BY r.createdAt DESC, r.id DESC")
    List<Recipe> findLatestAll(Pageable pageable);

    @Query("SELECT r FROM Recipe r WHERE r.title LIKE %:keyword% ORDER BY r.createdAt DESC, r.id DESC")
    List<Recipe> findLatestByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT r FROM Recipe r
            WHERE r.createdAt < :cursorCreatedAt
               OR (r.createdAt = :cursorCreatedAt AND r.id < :cursorId)
            ORDER BY r.createdAt DESC, r.id DESC
            """)
    List<Recipe> findLatestAfterCursor(
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT r FROM Recipe r
            WHERE r.title LIKE %:keyword%
              AND (r.createdAt < :cursorCreatedAt
                   OR (r.createdAt = :cursorCreatedAt AND r.id < :cursorId))
            ORDER BY r.createdAt DESC, r.id DESC
            """)
    List<Recipe> findLatestByKeywordAfterCursor(
            @Param("keyword") String keyword,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT r FROM Recipe r
            LEFT JOIN RecipeIngredient ri ON ri.recipe = r AND ri.ingredient.id IN :fridgeIngredientIds
            GROUP BY r
            ORDER BY COUNT(ri) DESC, r.id DESC
            """)
    List<Recipe> findTopByMatchCount(
            @Param("fridgeIngredientIds") Set<Long> fridgeIngredientIds,
            Pageable pageable
    );

    @Query("""
            SELECT r FROM Recipe r
            LEFT JOIN RecipeIngredient ri ON ri.recipe = r AND ri.ingredient.id IN :fridgeIngredientIds
            WHERE r.title LIKE %:keyword%
            GROUP BY r
            ORDER BY COUNT(ri) DESC, r.id DESC
            """)
    List<Recipe> findTopByMatchCountWithKeyword(
            @Param("fridgeIngredientIds") Set<Long> fridgeIngredientIds,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT r FROM Recipe r
            LEFT JOIN RecipeIngredient ri ON ri.recipe = r AND ri.ingredient.id IN :fridgeIngredientIds
            GROUP BY r
            HAVING COUNT(ri) < :cursorMatchCount
                OR (COUNT(ri) = :cursorMatchCount AND r.id < :cursorId)
            ORDER BY COUNT(ri) DESC, r.id DESC
            """)
    List<Recipe> findNextByMatchCount(
            @Param("fridgeIngredientIds") Set<Long> fridgeIngredientIds,
            @Param("cursorMatchCount") int cursorMatchCount,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT r FROM Recipe r
            LEFT JOIN RecipeIngredient ri ON ri.recipe = r AND ri.ingredient.id IN :fridgeIngredientIds
            WHERE r.title LIKE %:keyword%
            GROUP BY r
            HAVING COUNT(ri) < :cursorMatchCount
                OR (COUNT(ri) = :cursorMatchCount AND r.id < :cursorId)
            ORDER BY COUNT(ri) DESC, r.id DESC
            """)
    List<Recipe> findNextByMatchCountWithKeyword(
            @Param("fridgeIngredientIds") Set<Long> fridgeIngredientIds,
            @Param("keyword") String keyword,
            @Param("cursorMatchCount") int cursorMatchCount,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT r FROM Recipe r
            LEFT JOIN ProfileFavoriteRecipe pfr ON pfr.recipe = r
            GROUP BY r
            ORDER BY COUNT(pfr) DESC, r.id DESC
            """)
    List<Recipe> findTopByLikeCount(Pageable pageable);

    @Query("""
            SELECT r FROM Recipe r
            LEFT JOIN ProfileFavoriteRecipe pfr ON pfr.recipe = r
            WHERE r.title LIKE %:keyword%
            GROUP BY r
            ORDER BY COUNT(pfr) DESC, r.id DESC
            """)
    List<Recipe> findTopByLikeCountWithKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT r FROM Recipe r
            LEFT JOIN ProfileFavoriteRecipe pfr ON pfr.recipe = r
            GROUP BY r
            HAVING COUNT(pfr) < :cursorLikeCount
                OR (COUNT(pfr) = :cursorLikeCount AND r.id < :cursorId)
            ORDER BY COUNT(pfr) DESC, r.id DESC
            """)
    List<Recipe> findNextByLikeCount(
            @Param("cursorLikeCount") int cursorLikeCount,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT r FROM Recipe r
            LEFT JOIN ProfileFavoriteRecipe pfr ON pfr.recipe = r
            WHERE r.title LIKE %:keyword%
            GROUP BY r
            HAVING COUNT(pfr) < :cursorLikeCount
                OR (COUNT(pfr) = :cursorLikeCount AND r.id < :cursorId)
            ORDER BY COUNT(pfr) DESC, r.id DESC
            """)
    List<Recipe> findNextByLikeCountWithKeyword(
            @Param("keyword") String keyword,
            @Param("cursorLikeCount") int cursorLikeCount,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT r FROM Recipe r
            JOIN RecipeTag rt ON rt.recipe = r
            JOIN Tag t ON rt.tag = t
            WHERE t.value = :category
            ORDER BY r.createdAt DESC, r.id DESC
            """)
    List<Recipe> findLatestByCategory(
            @Param("category") String category,
            Pageable pageable
    );

    @Query("""
            SELECT r FROM Recipe r
            JOIN RecipeTag rt ON rt.recipe = r
            JOIN Tag t ON rt.tag = t
            WHERE t.value = :category
              AND (r.createdAt < :cursorCreatedAt
                   OR (r.createdAt = :cursorCreatedAt AND r.id < :cursorId))
            ORDER BY r.createdAt DESC, r.id DESC
            """)
    List<Recipe> findLatestByCategoryAfterCursor(
            @Param("category") String category,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT r FROM Recipe r
            JOIN RecipeTag rt ON rt.recipe = r
            JOIN Tag t ON rt.tag = t
            LEFT JOIN ProfileFavoriteRecipe pfr ON pfr.recipe = r
            WHERE t.value = :category
            GROUP BY r
            ORDER BY COUNT(pfr) DESC, r.id DESC
            """)
    List<Recipe> findByLikeCountAndCategory(
            @Param("category") String category,
            Pageable pageable
    );

    @Query("""
            SELECT r FROM Recipe r
            JOIN RecipeTag rt ON rt.recipe = r
            JOIN Tag t ON rt.tag = t
            LEFT JOIN ProfileFavoriteRecipe pfr ON pfr.recipe = r
            WHERE t.value = :category
            GROUP BY r
            HAVING COUNT(pfr) < :cursorLikeCount
                OR (COUNT(pfr) = :cursorLikeCount AND r.id < :cursorId)
            ORDER BY COUNT(pfr) DESC, r.id DESC
            """)
    List<Recipe> findNextByLikeCountAndCategory(
            @Param("category") String category,
            @Param("cursorLikeCount") int cursorLikeCount,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
