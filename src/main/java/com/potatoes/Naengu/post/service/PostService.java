package com.potatoes.Naengu.post.service;

import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.post.domain.model.Post;
import com.potatoes.Naengu.post.domain.model.PostImage;
import com.potatoes.Naengu.post.dto.PostCreateRequest;
import com.potatoes.Naengu.post.dto.PostCreateResponse;
import com.potatoes.Naengu.post.dto.PostImageRequest;
import com.potatoes.Naengu.post.exception.PostErrorCode;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.post.repository.PostRepository;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;

    @Transactional
    public PostCreateResponse createPost(Long userId, PostCreateRequest req){
        Profile profile = profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(PostErrorCode.PROFILE_NOT_FOUND));

        Post post = new Post(profile, req.content());

        //이미지 넣기
        List<PostImageRequest> images = req.images() != null ? req.images() : List.of();
        for (PostImageRequest img : images) {
            post.addImage(new PostImage(
                    img.s3Key(),
                    img.contentType(),
                    img.size(),
                    img.accessType()
            ));
        }

        Post saved = postRepository.save(post);
        return new PostCreateResponse(saved.getId());
    }
}
