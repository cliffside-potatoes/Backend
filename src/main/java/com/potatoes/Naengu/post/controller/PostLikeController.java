package com.potatoes.Naengu.post.controller;

import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import com.potatoes.Naengu.post.dto.query.GetLikedPostsRequest;
import com.potatoes.Naengu.post.dto.query.LikedPostsResponse;
import com.potatoes.Naengu.post.service.LikedPostQueryService;
import com.potatoes.Naengu.post.service.PostLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "post-like-controller", description = "게시글 좋아요 생성/해제 API")
@RestController
public class PostLikeController {

    private final PostLikeService postLikeService;
    private final LikedPostQueryService likedPostQueryService;

    public PostLikeController(PostLikeService postLikeService, LikedPostQueryService likedPostQueryService) {
        this.postLikeService = postLikeService;
        this.likedPostQueryService = likedPostQueryService;
    }


    @Operation(summary = "게시글 좋아요 생성",
            description = """
                    - 게시글 좋아요 생성 api,
                    - 좋아요 안 되어 있으면, 좋아요 생성,
                    - 이미 좋아요 되어 있는 상태여도 성공 처리.
                    - 좋아요 카운트 증가
                    """)
    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<Api<Void>> crate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        long userId = Long.parseLong(userDetails.getUsername());
        postLikeService.createLike(userId, postId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Api.success());
    }

    @Operation(summary = "게시글 좋아요 해제",
            description = """
                    - 게시글 좋아요 해제 API,
                    - 좋아요 되어 있으면 해제, 좋아요 수 감소
                    - 좋아요 안된 상태여도 멱등하게 성공 처리
                    """)
    @DeleteMapping("/posts/{postId}/likes")
    public ResponseEntity<Api<Void>> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        long userId = Long.parseLong(userDetails.getUsername());
        postLikeService.deleteLike(userId, postId);

        return ResponseEntity.ok(Api.success());
    }

    @Operation(
            summary = "내가 좋아요한 게시글 목록 조회",
            description = """
                    사용자가 좋아요한 게시글 목록을 커서 기반 페이지네이션으로 조회합니다.
                                        
                    - 첫 페이지 조회 시: cursorLikedAt, cursorId 없이 요청
                    - 다음 페이지 조회 시: 이전 응답의 nextCursor 값을 사용
                    - 정렬 기준: likedAt desc, likeId desc
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요한 게시글 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 커서 요청"),
            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음")
    })
    @GetMapping("/me/liked/posts")
    public ResponseEntity<Api<LikedPostsResponse>> getLikedPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute GetLikedPostsRequest request

    ) {
        long userId = Long.parseLong(userDetails.getUsername());
        LikedPostsResponse response = likedPostQueryService.getLikedPosts(userId, request);
        return ResponseEntity.ok(Api.success(response));
    }
}
