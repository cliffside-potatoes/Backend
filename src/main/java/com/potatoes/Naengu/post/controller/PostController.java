package com.potatoes.Naengu.post.controller;

import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import com.potatoes.Naengu.post.dto.PostCreateRequest;
import com.potatoes.Naengu.post.dto.PostCreateResponse;
import com.potatoes.Naengu.post.dto.PostUpdateRequest;
import com.potatoes.Naengu.post.dto.PostUpdateResponse;
import com.potatoes.Naengu.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Post", description = "게시글 API")
@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @Operation(summary = "게시글 생성", description = "새로운 게시글을 작성합니다. 이미지는 꼭 1장만 첨부할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시글 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (내용 없음, 이미지 5장 초과 등)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "프로필 없음")
    })
    @PostMapping("/posts")
    public ResponseEntity<Api<PostCreateResponse>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PostCreateRequest req
    ) {
        String userId = userDetails.getUsername();
        PostCreateResponse post = postService.createPost(Long.parseLong(userId), req);

        return ResponseEntity.status(HttpStatus.CREATED).body(Api.success(post));
    }

    @Operation(summary = "게시글 수정", description = "본인 게시글의 내용과 이미지를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (내용 없음, 이미지 5장 초과 등)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "본인 게시글이 아님"),
            @ApiResponse(responseCode = "404", description = "게시글 없음")
    })
    @PatchMapping("/posts/{postId}")
    public ResponseEntity<Api<PostUpdateResponse>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest req
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        PostUpdateResponse result = postService.updatePost(userId, postId, req);
        return ResponseEntity.ok(Api.success(result));
    }
}
