package com.potatoes.Naengu.post.repository;

import com.potatoes.Naengu.post.domain.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    //JPQL
    //pagenation 적용
    @Query("""
            SELECT p 
            FROM Post p 
            JOIN FETCH p.profile pr 
            LEFT JOIN FETCH pr.profileImage 
            ORDER BY p.createdAt DESC, p.id DESC
    """)
    List<Post> findLatestAll(Pageable pageable);

    //createdAt/ id를 커서로 사용해서, 커서보다 이전 게시글들만 최신순으로 정렬해 pageable.size 만큼 가져온다.
    @Query("""
           SELECT p 
           FROM Post p 
           JOIN FETCH p.profile pr 
           LEFT JOIN FETCH pr.profileImage 
           WHERE p.createdAt < :createdAt OR (p.createdAt = :createdAt AND p.id < :cursorId) 
           ORDER BY p.createdAt DESC, p.id DESC""")
    List<Post> findLatestAfterCursor(@Param("createdAt") LocalDateTime createdAt,
                                     @Param("cursorId") Long cursorId,
                                     Pageable pageable);
}
