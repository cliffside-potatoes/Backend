package com.potatoes.Naengu.profile.repository;

import com.potatoes.Naengu.profile.domain.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUserEntityProviderId(Long providerId);

    // native SQL — @SoftDelete 필터 우회, soft-deleted 행 포함 전체 물리 삭제
    @Modifying
    @Query(value = """
            DELETE pr FROM profile pr
            INNER JOIN user_entity u ON pr.user_id = u.id
            WHERE u.provider_id = :providerId
            """, nativeQuery = true)
    void hardDeleteByProviderId(@Param("providerId") Long providerId);

    // recipe_review_image → recipe_review 순서로 삭제 (Profile 삭제 전 선행 필요)
    @Modifying
    @Query(value = """
            DELETE rri FROM recipe_review_image rri
            INNER JOIN recipe_review rr ON rri.recipe_review_id = rr.id
            INNER JOIN profile pr ON rr.profile_id = pr.id
            INNER JOIN user_entity u ON pr.user_id = u.id
            WHERE u.provider_id = :providerId
            """, nativeQuery = true)
    void hardDeleteRecipeReviewImagesByProviderId(@Param("providerId") Long providerId);

    @Modifying
    @Query(value = """
            DELETE rr FROM recipe_review rr
            INNER JOIN profile pr ON rr.profile_id = pr.id
            INNER JOIN user_entity u ON pr.user_id = u.id
            WHERE u.provider_id = :providerId
            """, nativeQuery = true)
    void hardDeleteRecipeReviewsByProviderId(@Param("providerId") Long providerId);
}
