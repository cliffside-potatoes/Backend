package com.potatoes.Naengu.post.service;

import com.potatoes.Naengu.file.service.ImageUrlResolver;
import com.potatoes.Naengu.post.domain.model.Post;
import com.potatoes.Naengu.post.domain.model.PostImage;
import com.potatoes.Naengu.post.dto.*;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.domain.model.ProfileImage;
import com.potatoes.Naengu.post.repository.PostRepository;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final ImageUrlResolver imageUrlResolver;

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

    @Transactional(readOnly = true)
    public PostFeedResponse getFeed(Long userId, PostFeedQuery query) {
        query.validate();

        int size = query.sizeOrDefault();
        PageRequest pageRequest = PageRequest.of(0, size + 1);

        LocalDateTime cursorCreatedAt = query.cursorCreatedAtAsDateTime();
        Long cursorId = query.cursorId();

        List<Post> posts;
        if (cursorCreatedAt == null) {
            posts = postRepository.findLatestAll(pageRequest);
        } else {
            posts = postRepository.findLatestAfterCursor(cursorCreatedAt, cursorId, pageRequest);
        }

        boolean hasNext = posts.size() > size;
        List<Post> pageItems = hasNext ? posts.subList(0, size) : posts;

        List<PostFeedResponse.PostItem> items = pageItems.stream().map(post -> {
            Profile profile = post.getProfile();
            ProfileImage profileImage = profile.getProfileImage();

            String profileImageUrl = (profileImage != null)
                    ? imageUrlResolver.resolve(profileImage.getS3Key())
                    : null;

            List<String> imageUrls = post.getImages().stream()
                    .map(img -> imageUrlResolver.resolve(img.getS3Key()))
                    .toList();

            boolean isMine = profile.getUserEntity().getProviderId().equals(userId);

            PostFeedResponse.Writer writer = new PostFeedResponse.Writer(
                    profile.getId(),
                    profile.getNickname(),
                    profileImageUrl
            );

            return new PostFeedResponse.PostItem(
                    post.getId(),
                    imageUrls,
                    post.getContent(),
                    writer,
                    0,
                    false,
                    false,
                    isMine,
                    post.getUpdatedAt().toString(),
                    post.getCreatedAt().toString()
            );
        }).toList();

        PostFeedResponse.NextCursor nextCursor = null;
        if (hasNext) {
            Post last = pageItems.get(pageItems.size() - 1);
            nextCursor = new PostFeedResponse.NextCursor(
                    last.getCreatedAt().toString(),
                    last.getId()
            );
        }

        return new PostFeedResponse(items, hasNext, nextCursor);
    }
}
