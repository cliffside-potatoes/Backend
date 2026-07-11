package com.potatoes.Naengu.reviewrecipe.controller;

import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import com.potatoes.Naengu.reviewrecipe.domain.vo.ReviewSortType;
import com.potatoes.Naengu.reviewrecipe.dto.CreateRecipeReviewRequest;
import com.potatoes.Naengu.reviewrecipe.dto.CreateRecipeReviewResponse;
import com.potatoes.Naengu.reviewrecipe.dto.query.GetReviewFeedRequest;
import com.potatoes.Naengu.reviewrecipe.dto.query.RecipeReviewFeedResponse;
import com.potatoes.Naengu.reviewrecipe.dto.query.RecipeReviewCursor;
import com.potatoes.Naengu.reviewrecipe.service.RecipeReviewQueryService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "recipe-review-controller", description = "레시피 리뷰 API")
@RestController
public class RecipeReviewController {

    private final RecipeReviewService recipeReviewService;
    private final RecipeReviewQueryService recipeReviewQueryService;

    public RecipeReviewController(RecipeReviewService recipeReviewService,
                                  RecipeReviewQueryService recipeReviewQueryService) {
        this.recipeReviewService = recipeReviewService;
        this.recipeReviewQueryService = recipeReviewQueryService;
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
                                                "s3Key": "public/recipeReview/7cb23ed2-daa0-433b-96e8-7255edb91da7리뷰사진.jpg",
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

    @Operation(summary = "레시피 리뷰 조회",
    description = """
            - Query String : size, sort, cursorLikeCount, cursorUpdatedAt, cursorId
            - 최초 진입은 커서 없이 요청
            - default size = 20 , default sort = LATEST
            - sort = LATEST : cursorUpdatedAt, cursorId 사용
            - sort = LIKE : cursorLikeCount, cursorUpdatedAt, cursorId 사용
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레시피 리뷰글 조회 성공"),
            @ApiResponse(responseCode = "400", description = "cursorUpdatedAt 또는 cursorId 중 하나만 전달될 수 없습니다. 두 값은 함께 전달되어야 합니다.")
    })
    @GetMapping("/reviewRecipes/{recipeId}")
    public ResponseEntity<Api<RecipeReviewFeedResponse>> getFeed(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recipeId,
            @ModelAttribute GetReviewFeedRequest request
    ) {

        Long userId = Long.parseLong(userDetails.getUsername());

        ReviewSortType sort = request.normalizedSort();
        RecipeReviewCursor cursor = request.toCursor();
        cursor.validate(sort);

        RecipeReviewFeedResponse recipeReviewFeedResponse =
                recipeReviewQueryService.getFeed(
                        userId,
                        request.normalizedSize(),
                        recipeId,
                        sort,
                        cursor
                );

        return ResponseEntity.ok(Api.success(recipeReviewFeedResponse));
    }
}
