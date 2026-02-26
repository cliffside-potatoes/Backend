package com.potatoes.Naengu.post.controller;

import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import com.potatoes.Naengu.post.dto.PostCreateRequest;
import com.potatoes.Naengu.post.dto.PostCreateResponse;
import com.potatoes.Naengu.post.dto.PostFeedQuery;
import com.potatoes.Naengu.post.dto.PostFeedResponse;
import com.potatoes.Naengu.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("/posts")
    public ResponseEntity<PostCreateResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PostCreateRequest req
            ){
        String userId = userDetails.getUsername();
        PostCreateResponse post = postService.createPost(Long.parseLong(userId), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @GetMapping("/feed")
    public ResponseEntity<PostFeedResponse> getFeed(
            @ModelAttribute PostFeedQuery query,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        PostFeedResponse response = postService.getFeed(userId, query);
        return ResponseEntity.ok(response);
    }
}
