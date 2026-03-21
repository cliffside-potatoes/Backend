package com.potatoes.Naengu.recipe.controller;

import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import com.potatoes.Naengu.recipe.dto.RecipeSearchRequest;
import com.potatoes.Naengu.recipe.dto.RecipeSearchResponse;
import com.potatoes.Naengu.recipe.query.RecipeQueryService;
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

@Tag(name = "recipe-query-controller", description = "레시피 검색 조회 API")
@RestController
@RequiredArgsConstructor
public class RecipeQueryController {

    private final RecipeQueryService recipeQueryService;

    @Operation(summary = "레시피 검색 조회",
            description = """
                    - 커서 기반 무한 스크롤 레시피 조회
                    - keyword 없으면 전체 조회
                    - sort 미입력 시 기본값 LATEST
                    - cursorCreatedAt과 cursorId는 항상 함께 전달해야 함
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레시피 조회 성공"),
            @ApiResponse(responseCode = "400", description = "커서 값 또는 정렬 방식이 올바르지 않습니다."),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/recipes")
    public ResponseEntity<Api<RecipeSearchResponse>> search(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "LATEST") String sort,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String cursorCreatedAt,
            @RequestParam(required = false) Long cursorId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        RecipeSearchRequest request = new RecipeSearchRequest(size, keyword, cursorCreatedAt, cursorId, sort);
        RecipeSearchResponse response = recipeQueryService.search(userId, request);
        return ResponseEntity.ok(Api.success(response));
    }
}
