package com.potatoes.Naengu.ingredient.controller;

import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.ingredient.dto.IngredientSuggestionResponse;
import com.potatoes.Naengu.ingredient.service.IngredientSuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ingredient-suggestion-controller", description = "재료 API")
@RestController
public class IngredientSuggestionController {

    private final IngredientSuggestionService ingredientSuggestionService;

    public IngredientSuggestionController(IngredientSuggestionService ingredientSuggestionService) {
        this.ingredientSuggestionService = ingredientSuggestionService;
    }

    @Operation(
            summary = "재료 연관 검색 조회",
            description = """
                    사용자가 입력 중인 query를 기준으로,
                    재료명이 해당 query로 시작하는 재료 목록을 조회합니다.
                    
                    - 예: query=토 -> 토마토, 토란
                    - 예: query=토마 -> 토마토
                    - 최대 10개까지 반환합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "재료 연관 검색 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "재료 연관 검색 조회 성공 예시",
                                    value = """
                                            {
                                              "result": "SUCCESS",
                                              "data": {
                                                "items": [
                                                  {
                                                    "ingredientId": 1,
                                                    "ingredientName": "토마토"
                                                  },
                                                  {
                                                    "ingredientId": 2,
                                                    "ingredientName": "토마토주스"
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/ingredients/suggestions")
    public ResponseEntity<Api<IngredientSuggestionResponse>> getSuggestions(
            @RequestParam String query
    ) {
        IngredientSuggestionResponse response = ingredientSuggestionService.getSuggestions(query);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Api.success(response));
    }

}
