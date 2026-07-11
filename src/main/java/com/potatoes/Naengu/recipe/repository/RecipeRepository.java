package com.potatoes.Naengu.recipe.repository;

import com.potatoes.Naengu.recipe.domain.model.Recipe;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Recipe r SET r.likeCount = r.likeCount + 1 WHERE r.id = :id")
    void increaseLikeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Recipe r SET r.likeCount = r.likeCount - 1 WHERE r.id = :id AND r.likeCount > 0")
    void decreaseLikeCount(@Param("id") Long id);

    @Query("SELECT r FROM Recipe r ORDER BY r.createdAt DESC, r.id DESC")
    List<Recipe> findLatestAll(Pageable pageable);

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

    // FULLTEXT 키워드 검색 — ID만 반환 (Recipe가 JOINED 상속이라 native query로 엔티티 직접 반환 불가)
    @Query(value = """
            SELECT r.id FROM recipe r
            WHERE MATCH(r.title) AGAINST(:keyword IN BOOLEAN MODE)
            ORDER BY r.created_at DESC, r.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Long> findIdsByKeywordLatest(
            @Param("keyword") String keyword,
            @Param("limit") int limit
    );

    @Query(value = """
            SELECT r.id FROM recipe r
            WHERE MATCH(r.title) AGAINST(:keyword IN BOOLEAN MODE)
              AND (r.created_at < :cursorCreatedAt
                   OR (r.created_at = :cursorCreatedAt AND r.id < :cursorId))
            ORDER BY r.created_at DESC, r.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Long> findIdsByKeywordLatestAfterCursor(
            @Param("keyword") String keyword,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit
    );

    @Query(value = """
            SELECT r.id FROM recipe r
            WHERE MATCH(r.title) AGAINST(:keyword IN BOOLEAN MODE)
            ORDER BY r.like_count DESC, r.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Long> findIdsByKeywordLikeCount(
            @Param("keyword") String keyword,
            @Param("limit") int limit
    );

    @Query(value = """
            SELECT r.id FROM recipe r
            WHERE MATCH(r.title) AGAINST(:keyword IN BOOLEAN MODE)
              AND (r.like_count < :cursorLikeCount
                   OR (r.like_count = :cursorLikeCount AND r.id < :cursorId))
            ORDER BY r.like_count DESC, r.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Long> findIdsByKeywordLikeCountAfterCursor(
            @Param("keyword") String keyword,
            @Param("cursorLikeCount") int cursorLikeCount,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit
    );

    // ID로 엔티티 로드 — 정렬 순서 보존
    @Query("SELECT r FROM Recipe r WHERE r.id IN :ids ORDER BY r.createdAt DESC, r.id DESC")
    List<Recipe> findByIdsOrderByLatest(@Param("ids") List<Long> ids);

    @Query("SELECT r FROM Recipe r WHERE r.id IN :ids ORDER BY r.likeCount DESC, r.id DESC")
    List<Recipe> findByIdsOrderByLikeCount(@Param("ids") List<Long> ids);

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
            ORDER BY r.likeCount DESC, r.id DESC
            """)
    List<Recipe> findTopByLikeCount(Pageable pageable);

    @Query("""
            SELECT r FROM Recipe r
            WHERE r.likeCount < :cursorLikeCount
                OR (r.likeCount = :cursorLikeCount AND r.id < :cursorId)
            ORDER BY r.likeCount DESC, r.id DESC
            """)
    List<Recipe> findNextByLikeCount(
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
            WHERE t.value = :category
            ORDER BY r.likeCount DESC, r.id DESC
            """)
    List<Recipe> findByLikeCountAndCategory(
            @Param("category") String category,
            Pageable pageable
    );

    @Query("""
            SELECT r FROM Recipe r
            JOIN RecipeTag rt ON rt.recipe = r
            JOIN Tag t ON rt.tag = t
            WHERE t.value = :category
              AND (r.likeCount < :cursorLikeCount
                OR (r.likeCount = :cursorLikeCount AND r.id < :cursorId))
            ORDER BY r.likeCount DESC, r.id DESC
            """)
    List<Recipe> findNextByLikeCountAndCategory(
            @Param("category") String category,
            @Param("cursorLikeCount") int cursorLikeCount,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
