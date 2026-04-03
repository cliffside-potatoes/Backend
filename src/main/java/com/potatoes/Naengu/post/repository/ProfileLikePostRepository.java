package com.potatoes.Naengu.post.repository;

import com.potatoes.Naengu.post.domain.model.Post;
import com.potatoes.Naengu.post.domain.model.ProfileLikePost;
import com.potatoes.Naengu.profile.domain.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileLikePostRepository extends JpaRepository<ProfileLikePost, Long> {

    boolean existsByProfileAndPost(Profile profile, Post post);
}
