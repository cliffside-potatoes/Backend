package com.potatoes.Naengu.post.dto.query;

import static com.potatoes.Naengu.post.exception.PostErrorCode.INVALID_CURSOR;

import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.post.domain.model.ProfileLikePost;
import java.time.LocalDateTime;

public record LikedPostsCursor(
        LocalDateTime likedAt,
        Long id
) {

    public boolean isFirstPage() {
        return likedAt == null && id == null;
    }

    public void validate(){
        boolean bothNull = (likedAt == null && id == null);
        boolean bothNotNull = (likedAt != null && id != null);

        if (!(bothNull || bothNotNull)) {
            throw new ApiException(INVALID_CURSOR);
        }
    }

    public static LikedPostsCursor from(ProfileLikePost profileLikePost) {
        return new LikedPostsCursor(
                profileLikePost.getCreatedAt(),
                profileLikePost.getId()
        );
    }
}
