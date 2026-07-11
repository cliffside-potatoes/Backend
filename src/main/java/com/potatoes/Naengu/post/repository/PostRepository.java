package com.potatoes.Naengu.post.repository;

import com.potatoes.Naengu.post.domain.model.Post;
import com.potatoes.Naengu.profile.domain.model.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByProfile(Profile profile);

    // native SQL — @SoftDelete 필터 우회, soft-deleted 행 포함 전체 물리 삭제
    @Modifying(clearAutomatically = true)
    @Query(value = """
            DELETE FROM post
            WHERE profile_id IN (
                SELECT pr.id FROM profile pr
                INNER JOIN user_entity u ON pr.user_id = u.id
                WHERE u.provider_id = :providerId
            )
            """, nativeQuery = true)
    void hardDeleteAllByProviderId(@Param("providerId") Long providerId);

    @Query("""
            SELECT p 
            FROM Post p 
            ORDER BY p.createdAt DESC, p.id DESC""")
    List<Post> findLatestAll(Pageable pageable);

    @Query("""
            SELECT p
            FROM Post p
            WHERE p.createdAt < :cursorCreatedAt OR (p.createdAt = :cursorCreatedAt AND p.id < :cursorId)
            ORDER BY p.createdAt DESC, p.id DESC
    """)
    List<Post> findLatestAfterCursor(
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Post p
            WHERE p.profile.id = :profileId
            ORDER BY p.createdAt DESC, p.id DESC
    """)
    List<Post> findMyLatestAll(@Param("profileId") Long profileId, Pageable pageable);

    @Query("""
            SELECT p
            FROM Post p
            WHERE p.profile.id = :profileId
              AND (p.createdAt < :cursorCreatedAt OR (p.createdAt = :cursorCreatedAt AND p.id < :cursorId))
            ORDER BY p.createdAt DESC, p.id DESC
    """)
    List<Post> findMyLatestAfterCursor(
            @Param("profileId") Long profileId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
