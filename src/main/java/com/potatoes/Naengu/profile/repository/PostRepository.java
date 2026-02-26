package com.potatoes.Naengu.profile.repository;

import com.potatoes.Naengu.post.domain.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

}
