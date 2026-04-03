package com.potatoes.Naengu.post.controller;

import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import com.potatoes.Naengu.post.service.PostLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "post-like-controller", description = "게시글 좋아요 생성/해제 API")
@RestController
public class PostLikeController {

    private final PostLikeService postLikeService;

    public PostLikeController(PostLikeService postLikeService) {
        this.postLikeService = postLikeService;
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
}
