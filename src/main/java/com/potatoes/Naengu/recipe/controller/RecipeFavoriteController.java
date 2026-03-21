package com.potatoes.Naengu.recipe.controller;

import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import com.potatoes.Naengu.recipe.service.RecipeFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "recipe-favorite-controller",description = "레시피 찜 생성/해제 API")
@RestController
public class RecipeFavoriteController {

    private final RecipeFavoriteService recipeFavoriteService;


    public RecipeFavoriteController(RecipeFavoriteService recipeFavoriteService) {
        this.recipeFavoriteService = recipeFavoriteService;
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
}
