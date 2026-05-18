package com.potatoes.Naengu.post.service;

import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.post.domain.model.Post;
import com.potatoes.Naengu.post.domain.model.ProfileLikePost;
import com.potatoes.Naengu.post.exception.PostErrorCode;
import com.potatoes.Naengu.post.repository.PostRepository;
import com.potatoes.Naengu.post.repository.ProfileLikePostRepository;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.exception.ProfileErrorCode;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostLikeService {

    private final ProfileLikePostRepository profileLikePostRepository;
    private final ProfileRepository profileRepository;
    private final PostRepository postRepository;


    public PostLikeService(ProfileLikePostRepository profileLikePostRepository, ProfileRepository profileRepository,
                           PostRepository postRepository) {
        this.profileLikePostRepository = profileLikePostRepository;
        this.profileRepository = profileRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public void createLike(Long userId, Long postId) {
        Profile profile = loadProfile(userId);
        Post post = loadPost(postId);

        boolean exists = profileLikePostRepository.existsByProfileAndPost(profile, post);
        if (exists) {
            return;
        }

        try {
            ProfileLikePost like = ProfileLikePost.create(profile, post);
            profileLikePostRepository.saveAndFlush(like);
            post.plusLikeCount();
        } catch (DataIntegrityViolationException e) {}
    }

    @Transactional
    public void deleteLike(long userId, Long postId) {
        Profile profile = loadProfile(userId);
        Post post = loadPost(postId);

        profileLikePostRepository
                .findByProfileAndPost(profile, post)
                .ifPresent(profileLikePost -> {
                    profileLikePostRepository.delete(profileLikePost);
                    post.minusLikeCount();
                });

    }



    private Profile loadProfile(long userId) {
        return profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(ProfileErrorCode.PROFILE_NOT_FOUND));
    }

    private Post loadPost(long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(PostErrorCode.POST_NOT_FOUND));
    }
}
