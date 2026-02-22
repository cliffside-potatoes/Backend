package com.potatoes.Naengu.post.service;

import com.potatoes.Naengu.post.domain.model.Post;
import com.potatoes.Naengu.post.domain.model.PostImage;
import com.potatoes.Naengu.post.dto.PostCreateRequest;
import com.potatoes.Naengu.post.dto.PostCreateResponse;
import com.potatoes.Naengu.post.dto.PostImageRequest;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.repository.PostRepository;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;

    @Transactional
    public PostCreateResponse createPost(Long userId, PostCreateRequest req){
        Profile profile = profileRepository.findByUserEntityProviderId(userId).orElse(null);

        Post post = new Post(profile, req.content());

        //이미지 넣기
        for(PostImageRequest img :req.images()){
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
