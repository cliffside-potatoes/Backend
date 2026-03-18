package com.potatoes.Naengu.post.repository;

import com.potatoes.Naengu.post.domain.model.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    // native SQL — @SoftDelete 필터 우회, soft-deleted 행 포함 전체 물리 삭제
    @Modifying
    @Query(value = """
            DELETE pi FROM post_image pi
            INNER JOIN post p ON pi.post_id = p.id
            INNER JOIN profile pr ON p.profile_id = pr.id
            INNER JOIN user_entity u ON pr.user_id = u.id
            WHERE u.provider_id = :providerId
            """, nativeQuery = true)
    void hardDeleteAllByProviderId(@Param("providerId") Long providerId);
}
