package com.potatoes.Naengu.recipe.controller;

import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import com.potatoes.Naengu.recipe.dto.FavoriteRecipesResponse;
import com.potatoes.Naengu.recipe.query.RecipeQueryService;
import com.potatoes.Naengu.recipe.service.RecipeFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "recipe-favorite-controller",description = "레시피 찜 생성/해제 API")
@RestController
public class RecipeFavoriteController {

    private final RecipeFavoriteService recipeFavoriteService;
    private final RecipeQueryService recipeQueryService;

    public RecipeFavoriteController(RecipeFavoriteService recipeFavoriteService,
                                    RecipeQueryService recipeQueryService) {
        this.recipeFavoriteService = recipeFavoriteService;
        this.recipeQueryService = recipeQueryService;
    }

    @Operation(summary = "내 찜 레시피 목록",
    description = """
            - 찜한 레시피를 최신순(찜한 시각 기준)으로 커서 페이징 조회
            - 최초: cursorCreatedAt, cursorId 없이 요청
            - 이후: 응답의 nextCursor 값을 그대로 QueryString으로 전달
            - cursorCreatedAt과 cursorId는 항상 함께 전달해야 함
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "커서 값이 올바르지 않습니다."),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me/favorites/recipes")
    public ResponseEntity<Api<FavoriteRecipesResponse>> getFavorites(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String cursorCreatedAt,
            @RequestParam(required = false) Long cursorId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        FavoriteRecipesResponse result = recipeQueryService.getFavorites(userId, size, cursorCreatedAt, cursorId);
        return ResponseEntity.ok(Api.success(result));
    }

    @Operation(summary = "레시피 찜 생성",
    description = """
            - 레시피 찜 생성 API
            - 찜 되어 있지 않으면 찜 생성
            - 이미 찜 해둔 상태여도, 성공 처리
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "레시피 찜 생성 성공")
    })
    @PostMapping("/recipes/{recipeId}/favorites")
    public ResponseEntity<Api<Void>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recipeId

    ) {
        long userId = Long.parseLong(userDetails.getUsername());
        recipeFavoriteService.createFavorite(userId, recipeId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Api.success());
    }

    @Operation(summary = "레시피 찜 해제",
    description = """
            - 레시피 찜 해제 API
            - 찜 되어 있으면 해제
            - 찜 안된 상태여도 성공 처리 (멱등성)
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레시피 찜 해제 성공")
    })
    @DeleteMapping("/recipes/{recipeId}/favorites")
    public ResponseEntity<Api<Void>> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recipeId
    ) {
        long userId = Long.parseLong(userDetails.getUsername());
        recipeFavoriteService.deleteFavorite(userId, recipeId);

        return ResponseEntity.ok(Api.success());

    }
}
