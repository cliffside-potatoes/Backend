package com.potatoes.Naengu.reviewrecipe.controller;

import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import com.potatoes.Naengu.reviewrecipe.dto.CreateRecipeReviewRequest;
import com.potatoes.Naengu.reviewrecipe.dto.CreateRecipeReviewResponse;
import com.potatoes.Naengu.reviewrecipe.service.RecipeReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "recipe-review-controller", description = "레시피 리뷰 API")
@RestController
public class RecipeReviewController {

    private final RecipeReviewService recipeReviewService;


    public RecipeReviewController(RecipeReviewService recipeReviewService) {
        this.recipeReviewService = recipeReviewService;
    }

    @Operation(
            summary = "레시피 리뷰글 생성",
            description = """
                    - content : 1자 이상 500자 이하
                    - images : 사진 최대 5장 (필수 x)
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "레시피 리뷰 예시",
                                    value = """
                                            {
                                                "images" : [
                                                    {
                                                "s3Key": "public/recipe/fa14dc74-bbb6-4b62-8c66-ce4d1b0e321b김치찌개.jpg",
                                                "contentType": "image/jpeg",
                                                "size": 123456,
                                                "accessType": "public"
                                              }
                                                ],
                                                "content" : "너무 맛있었다!"
                                            }
                                    """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "레시피 리뷰글 생성 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 레시피의 리뷰글 작성 시도"),
            @ApiResponse(responseCode = "409",description = "사용자가 이미 해당 레시피에 대한 리뷰를 작성한 경우")
    })
    @PostMapping("/reviewRecipes/{recipeId}")
    public ResponseEntity<Api<CreateRecipeReviewResponse>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recipeId,
            @Valid @RequestBody CreateRecipeReviewRequest request
    ) {
        String userId = userDetails.getUsername();
        Long id = recipeReviewService.create(Long.parseLong(userId), recipeId,request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Api.success(new CreateRecipeReviewResponse(id)));
    }
}
