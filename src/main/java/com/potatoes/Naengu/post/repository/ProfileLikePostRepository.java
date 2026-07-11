package com.potatoes.Naengu.post.repository;

import com.potatoes.Naengu.post.domain.model.Post;
import com.potatoes.Naengu.post.domain.model.ProfileLikePost;
import com.potatoes.Naengu.profile.domain.model.Profile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfileLikePostRepository extends JpaRepository<ProfileLikePost, Long> {

    boolean existsByProfileAndPost(Profile profile, Post post);

    Optional<ProfileLikePost> findByProfileAndPost(Profile profile, Post post);


    List<ProfileLikePost> findByProfileIdOrderByCreatedAtDescIdDesc(
            Long profileId,
            Pageable pageable
    );

    @Query("""
        select plp
        from ProfileLikePost plp
        where plp.profile.id = :profileId
          and (
                plp.createdAt < :cursorLikedAt
                or (plp.createdAt = :cursorLikedAt and plp.id < :cursorId)
          )
        order by plp.createdAt desc, plp.id desc
        """)
    List<ProfileLikePost> findNextPage(
            @Param("profileId") Long profileId,
            @Param("cursorLikedAt") LocalDateTime cursorLikedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
