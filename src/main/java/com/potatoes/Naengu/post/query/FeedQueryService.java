package com.potatoes.Naengu.post.query;

import com.potatoes.Naengu.file.service.FileUploadService;
import com.potatoes.Naengu.global.exception.ApiException;
import com.potatoes.Naengu.post.domain.model.Post;
import com.potatoes.Naengu.post.domain.vo.SortType;
import com.potatoes.Naengu.post.dto.CursorResponse;
import com.potatoes.Naengu.post.dto.FeedItemResponse;
import com.potatoes.Naengu.post.dto.FeedQueryRequest;
import com.potatoes.Naengu.post.dto.FeedResponse;
import com.potatoes.Naengu.post.dto.WriterResponse;
import com.potatoes.Naengu.post.exception.PostErrorCode;
import com.potatoes.Naengu.post.repository.PostRepository;
import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedQueryService {

    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final FileUploadService fileUploadService;

    @Transactional(readOnly = true)
    public FeedResponse getFeed(Long providerId, FeedQueryRequest request) {
        validateCursor(request);
        SortType.from(request.sort());

        Long currentProfileId = profileRepository.findByUserEntityProviderId(providerId)
                .map(Profile::getId)
                .orElse(null);

        List<Post> posts = fetchPosts(request);

        boolean hasNext = posts.size() > request.size();
        if (hasNext) {
            posts = posts.subList(0, request.size());
        }

        List<FeedItemResponse> items = posts.stream()
                .map(post -> toFeedItemResponse(post, currentProfileId))
                .toList();

        CursorResponse nextCursor = null;
        if (hasNext && !posts.isEmpty()) {
            Post last = posts.get(posts.size() - 1);
            nextCursor = new CursorResponse(
                    last.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    last.getId()
            );
        }

        return new FeedResponse(items, hasNext, nextCursor);
    }

    private List<Post> fetchPosts(FeedQueryRequest request) {
        PageRequest pageable = PageRequest.of(0, request.size() + 1);
        if (request.cursorCreatedAt() == null) {
            return postRepository.findLatestAll(pageable);
        }
        LocalDateTime cursorTime = parseCursorTime(request.cursorCreatedAt());
        return postRepository.findLatestAfterCursor(cursorTime, request.cursorId(), pageable);
    }

    private FeedItemResponse toFeedItemResponse(Post post, Long currentProfileId) {
        List<String> imageUrls = post.getImages().stream()
                .map(img -> fileUploadService.getPublicUrl(img.getS3Key()))
                .toList();

        String profileImageUrl = post.getProfile().getProfileImage() != null
                ? fileUploadService.getPublicUrl(post.getProfile().getProfileImage().getS3Key())
                : fileUploadService.getDefaultProfileImageUrl();

        WriterResponse writer = new WriterResponse(
                post.getProfile().getId(),
                post.getProfile().getNickname(),
                profileImageUrl
        );

        Integer likeCount = post.isHideLikeCount() ? null : 0;
        boolean isMine = currentProfileId != null && post.getProfile().getId().equals(currentProfileId);

        return new FeedItemResponse(
                post.getId(),
                imageUrls,
                post.getContent(),
                writer,
                likeCount,
                post.isHideLikeCount(),
                false,
                isMine,
                post.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                post.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

    private void validateCursor(FeedQueryRequest request) {
        boolean hasCursorCreatedAt = request.cursorCreatedAt() != null;
        boolean hasCursorId = request.cursorId() != null;
        if (hasCursorCreatedAt != hasCursorId) {
            throw new ApiException(PostErrorCode.INVALID_CURSOR);
        }
    }

    private LocalDateTime parseCursorTime(String cursorCreatedAt) {
        try {
            return LocalDateTime.parse(cursorCreatedAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new ApiException(PostErrorCode.INVALID_CURSOR);
        }
    }
}
