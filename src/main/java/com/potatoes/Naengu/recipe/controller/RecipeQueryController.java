package com.potatoes.Naengu.recipe.controller;

import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import com.potatoes.Naengu.recipe.domain.vo.RecipeSortType;
import com.potatoes.Naengu.recipe.dto.RecipeLikeResponse;
import com.potatoes.Naengu.recipe.dto.RecipeMatchResponse;
import com.potatoes.Naengu.recipe.dto.RecipeSearchRequest;
import com.potatoes.Naengu.recipe.dto.RecipeSearchResponse;
import com.potatoes.Naengu.recipe.dto.RecipeDetailResponse;
import com.potatoes.Naengu.recipe.query.RecipeDetailQueryService;
import com.potatoes.Naengu.recipe.query.RecipeQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "recipe-query-controller", description = "레시피 검색 조회 API")
@RestController
@RequiredArgsConstructor
public class RecipeQueryController {

    private final RecipeQueryService recipeQueryService;
    private final RecipeDetailQueryService recipeDetailQueryService;

    @Operation(summary = "레시피 검색 조회",
            description = """
                    - 커서 기반 무한 스크롤 레시피 조회
                    - keyword 없으면 전체 조회
                    - sort: LATEST(기본값) | MATCH_COUNT(냉장고 재료 매칭 순) | LIKE_COUNT(좋아요 순)
                    - LATEST: cursorCreatedAt과 cursorId는 항상 함께 전달해야 함
                    - MATCH_COUNT: cursorMatchCount와 cursorId는 항상 함께 전달해야 함
                    - LIKE_COUNT: cursorLikeCount와 cursorId는 항상 함께 전달해야 함
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레시피 조회 성공"),
            @ApiResponse(responseCode = "400", description = "커서 값 또는 정렬 방식이 올바르지 않습니다."),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/recipes")
    public ResponseEntity<Api<?>> search(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "LATEST") String sort,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String cursorCreatedAt,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) Integer cursorMatchCount,
            @RequestParam(required = false) Integer cursorLikeCount,
            @RequestParam(required = false) String category
    ) {
        RecipeSearchRequest request = new RecipeSearchRequest(size, keyword, cursorCreatedAt, cursorId, sort, cursorMatchCount, cursorLikeCount, category);
        RecipeSortType sortType = RecipeSortType.from(sort);

        if (userDetails == null) {
            if (sortType != RecipeSortType.LIKE_COUNT) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Api.error("UNAUTHORIZED", "인증이 필요합니다."));
            }
            return ResponseEntity.ok(Api.success(recipeQueryService.searchByLikeCountAnonymous(request)));
        }

        Long userId = Long.parseLong(userDetails.getUsername());

        if (category != null && !category.isBlank()) {
            if (sortType == RecipeSortType.LIKE_COUNT) {
                return ResponseEntity.ok(Api.success(recipeQueryService.searchByCategoryByLikeCount(userId, request)));
            }
            return ResponseEntity.ok(Api.success(recipeQueryService.searchByCategoryLatest(userId, request)));
        }

        if (sortType == RecipeSortType.MATCH_COUNT) {
            RecipeMatchResponse response = recipeQueryService.searchByMatchCount(userId, request);
            return ResponseEntity.ok(Api.success(response));
        }

        if (sortType == RecipeSortType.LIKE_COUNT) {
            RecipeLikeResponse response = recipeQueryService.searchByLikeCount(userId, request);
            return ResponseEntity.ok(Api.success(response));
        }

        RecipeSearchResponse response = recipeQueryService.search(userId, request);
        return ResponseEntity.ok(Api.success(response));
    }

    @Operation(summary = "레시피 상세 조회",
            description = "레시피 기본 정보, 타입별 상세(링크/텍스트), 냉장고 재료 매칭 정보를 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레시피 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "레시피를 찾을 수 없습니다.")
    })
    @GetMapping("/recipes/details/{recipeId}")
    public ResponseEntity<Api<RecipeDetailResponse>> getDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recipeId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(Api.success(recipeDetailQueryService.getDetail(userId, recipeId)));
    }
}
