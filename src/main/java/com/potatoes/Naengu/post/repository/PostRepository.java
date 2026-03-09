package com.potatoes.Naengu.post.repository;

import com.potatoes.Naengu.post.domain.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
            SELECT p 
            FROM Post p 
            ORDER BY p.createdAt DESC, p.id DESC""")
    List<Post> findLatestAll(Pageable pageable);

    @Query("""
            SELECT p 
            FROM Post p 
            WHERE p.createdAt < :cursorUpdatedAt OR (p.createdAt = :cursorUpdatedAt AND p.id < :cursorId) 
            ORDER BY p.createdAt DESC, p.id DESC
    """)
    List<Post> findLatestAfterCursor(
            @Param("cursorUpdatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
