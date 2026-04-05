package com.potatoes.Naengu.post.dto.query;

import com.potatoes.Naengu.post.domain.model.Post;
import com.potatoes.Naengu.post.domain.model.ProfileLikePost;
import java.time.LocalDateTime;
import java.util.List;

public record LikedPostItemResponse(
        Long postId,
        Long authorProfileId,
        String content,
        LocalDateTime postCreatedAt,
        LocalDateTime postUpdatedAt,
        LocalDateTime likedAt,
        boolean hideLikeCount,
        Integer likeCount,
        List<String> postImageUrls
) {
    public static LikedPostItemResponse of(
            ProfileLikePost profileLikePost,
            List<String> postImageUrls)
    {
        Post post = profileLikePost.getPost();

        return new LikedPostItemResponse(
                post.getId(),
                post.getProfile().getId(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                profileLikePost.getCreatedAt(),
                post.isHideLikeCount(),
                post.isHideLikeCount() ? null : post.getLikeCount(),
                postImageUrls
        );
    }
}
