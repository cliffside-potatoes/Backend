package com.potatoes.Naengu.recipe.controller;

import com.potatoes.Naengu.auth.annotation.AuthFridge;
import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.recipe.dto.CreateRecipeCommand;
import com.potatoes.Naengu.recipe.dto.CreateRecipeRequest;
import com.potatoes.Naengu.recipe.dto.CreateRecipeResponse;
import com.potatoes.Naengu.recipe.mapper.CreateRecipeRequestMapper;
import com.potatoes.Naengu.recipe.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "recipe-controller", description = "레시피 API")
@RestController
public class RecipeController {

    private final RecipeService service;

    public RecipeController(RecipeService service) {
        this.service = service;
    }

    @Operation(
            summary = "레시피 생성",
            description = """
    - type=TEXT이면 recipeWithText 필수, recipeWithLink는 null
    - type=LINK이면 recipeWithLink 필수, recipeWithText는 null
    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "TEXT 레시피 예시",
                                            value = """
                    {
                      "title": "김치찌개",
                      "difficulty": "INTERMEDIATE",
                      "servings": 2,
                      "cookingTime": 30,
                      "description": "잘 익은 김치와 돼지고기로 끓이는 김치찌개입니다.",
                      "recipeImage": {
                                       "s3Key": "public/recipe/fa14dc74-bbb6-4b62-8c66-ce4d1b0e321b김치찌개.jpg",
                                       "contentType": "image/jpeg",
                                       "size": 123456,
                                       "accessType": "public"
                                      },
                      "type": "TEXT",
                      "ingredients": ["김치", "돼지고기","두부","양파"],
                      "tags": ["한식", "찌개"],
                      "recipeWithText": {
                        "steps": [
                          { "stepOrder": 1, "content": "김치와 돼지고기를 썬다." },
                          { "stepOrder": 2, "content": "냄비에 재료를 넣고 물을 부은 뒤 끓인다."},
                          { "stepOrder": 3, "content": "두부와 양파를 넣고 10분 더 끓인다."}
                        ]
                      },
                      "recipeWithLink": null
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "LINK 레시피 예시",
                                            value = """
                    {
                      "title": "김치찌개 영상",
                      "difficulty": "INTERMEDIATE",
                      "servings": 2,
                      "cookingTime": 30,
                      "description": "영상으로 보는 김치찌개 레시피입니다.",
                      "recipeImage": {
                                       "s3Key": "public/recipe/fa14dc74-bbb6-4b62-8c66-ce4d1b0e321b김치찌개.jpg",
                                       "contentType": "image/jpeg",
                                       "size": 123456,
                                       "accessType": "public"
                                      },
                      "type": "LINK",
                      "ingredients": ["김치", "돼지고기","두부","양파"],
                      "tags": ["영상레시피"],
                      "recipeWithText": null,
                      "recipeWithLink": {
                        "url": "https://example.com",
                        "urlSource": "릴리쿡 김치찌개 만들기~~~"
                      }
                    }
                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "레시피 생성 성공")
    })
    @PostMapping("/recipes")
    public ResponseEntity<Api<CreateRecipeResponse>> create(
            @Parameter(hidden = true) @AuthFridge Fridge fridge,
            @Valid @RequestBody CreateRecipeRequest request
    ) {
        CreateRecipeCommand command = CreateRecipeRequestMapper.toCommand(request);
        Long id = service.create(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Api.success(new CreateRecipeResponse(id)));
    }
}
