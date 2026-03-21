package com.potatoes.Naengu.recipe.repository;

import com.potatoes.Naengu.recipe.domain.model.Recipe;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

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
}
