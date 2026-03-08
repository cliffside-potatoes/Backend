package com.potatoes.Naengu.post.controller;

import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import com.potatoes.Naengu.post.dto.FeedQueryRequest;
import com.potatoes.Naengu.post.dto.FeedResponse;
import com.potatoes.Naengu.post.query.FeedQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Feed", description = "피드 API")
@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedQueryService feedQueryService;

    @Operation(summary = "피드 조회", description = "커서 기반 무한 스크롤 피드를 조회합니다. 최초 요청은 커서 없이, 이후 응답의 nextCursor를 그대로 사용합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "피드 조회 성공"),
            @ApiResponse(responseCode = "400", description = "cursorCreatedAt과 cursorId 중 하나만 전달되거나 정렬 방식이 올바르지 않습니다."),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/feed")
    public ResponseEntity<Api<FeedResponse>> getFeed(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String cursorCreatedAt,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "LATEST") String sort
    ) {
        Long providerId = Long.parseLong(userDetails.getUsername());
        FeedQueryRequest request = new FeedQueryRequest(size, cursorCreatedAt, cursorId, sort);
        FeedResponse response = feedQueryService.getFeed(providerId, request);
        return ResponseEntity.ok(Api.success(response));
    }
}
