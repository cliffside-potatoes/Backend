package com.potatoes.Naengu.post.service;

import com.potatoes.Naengu.file.service.FileUploadService;
import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.post.domain.model.PostImage;
import com.potatoes.Naengu.post.domain.model.ProfileLikePost;
import com.potatoes.Naengu.post.dto.query.GetLikedPostsRequest;
import com.potatoes.Naengu.post.dto.query.LikedPostItemResponse;
import com.potatoes.Naengu.post.dto.query.LikedPostsCursor;
import com.potatoes.Naengu.post.dto.query.LikedPostsNextCursorResponse;
import com.potatoes.Naengu.post.dto.query.LikedPostsResponse;
import com.potatoes.Naengu.post.repository.ProfileLikePostRepository;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.exception.ProfileErrorCode;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikedPostQueryService {

    private final ProfileRepository profileRepository;
    private final ProfileLikePostRepository profileLikePostRepository;
    private final FileUploadService fileUploadService;

    public LikedPostsResponse getLikedPosts(
            long userId, GetLikedPostsRequest request
    ) {
        Profile profile = loadProfile(userId);

        int size = request.normalizedSize();
        LikedPostsCursor cursor = request.toCursor();
        PageRequest pageable = PageRequest.of(0, size + 1);

        List<ProfileLikePost> likedPosts = getLikedPosts(profile.getId(), cursor, pageable);
        boolean hasNext = hasNext(likedPosts, size);
        List<ProfileLikePost> currentPagePosts = sliceCurrentPage(likedPosts, size, hasNext);

        List<LikedPostItemResponse> items = currentPagePosts.stream()
                .map(this::toLikedPostItemResponse)
                .toList();

        LikedPostsNextCursorResponse nextCursor = extractNextCursor(currentPagePosts, hasNext);

        return LikedPostsResponse.of(items, hasNext, nextCursor);
    }

    private Profile loadProfile(Long userId) {
        return profileRepository.findByUserEntityProviderId(userId)
                .orElseThrow(() -> new ApiException(ProfileErrorCode.PROFILE_NOT_FOUND));
    }

    private List<ProfileLikePost> getLikedPosts(
            Long profileId,
            LikedPostsCursor cursor,
            Pageable pageable
    ) {
        if (cursor.isFirstPage()) {
            return profileLikePostRepository.findByProfileIdOrderByCreatedAtDescIdDesc(profileId, pageable);
        }

        return profileLikePostRepository.findNextPage(
                profileId,
                cursor.likedAt(),
                cursor.id(),
                pageable
        );

    }

    private boolean hasNext(List<ProfileLikePost> likedPosts, int size) {
        return likedPosts.size() > size;
    }

    private List<ProfileLikePost> sliceCurrentPage(
            List<ProfileLikePost> likedPost,
            int size,
            boolean hasNext
    ) {
        if (!hasNext) {
            return likedPost;
        }
        return likedPost.subList(0, size);
    }

    private LikedPostItemResponse toLikedPostItemResponse(
            ProfileLikePost profileLikePost
    ) {
        List<String> postImageUrls = profileLikePost.getPost().getImages().stream()
                .map(PostImage::getS3Key)
                .map(fileUploadService::getPublicUrl)
                .toList();

        return LikedPostItemResponse.of(profileLikePost, postImageUrls);
    }

    private LikedPostsNextCursorResponse extractNextCursor(
            List<ProfileLikePost> currentPagePosts,
            boolean hasNext
    ) {
        if (!hasNext || currentPagePosts.isEmpty()) {
            return null;
        }

        ProfileLikePost lastProfileLikePost = currentPagePosts.get(currentPagePosts.size() - 1);
        LikedPostsCursor nextCursor = LikedPostsCursor.from(lastProfileLikePost);
        return LikedPostsNextCursorResponse.from(nextCursor);
    }
}
