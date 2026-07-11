package com.potatoes.Naengu.fridge.controller;

import com.potatoes.Naengu.fridge.dto.CreateCategoryRequest;
import com.potatoes.Naengu.fridge.dto.CreateCategoryResponse;
import com.potatoes.Naengu.fridge.dto.UpdateCategoryOrderRequest;
import com.potatoes.Naengu.fridge.dto.UpdateCategoryRequest;
import com.potatoes.Naengu.fridge.dto.UpdateCategoryResponse;
import com.potatoes.Naengu.fridge.service.CategoryService;
import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.auth.annotation.AuthFridge;
import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "category-controller", description = "냉장고 카테고리 API")
@RestController
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @Operation(summary = "카테고리 생성",
            description = """
            새로운 냉장고 카테고리를 생성합니다.
            - 색상:
                   
                   COLOR_1("#90caf9"),
                   COLOR_2("#ce93d8"),
                   COLOR_3("#a5d6a7"),
                   COLOR_4("#fff59d"),
                   COLOR_5("#ffab91"),
                    
                   COLOR_6("#b0bec5"),
                   COLOR_7("#80cbc4"),
                   COLOR_8("#f48fb1"),
                   COLOR_9("#e1bee7"),
                   COLOR_10("#c5e1a5"),
                    
                   COLOR_11("#ffe082"),
                   COLOR_12("#ffcc80"),
                   COLOR_13("#b39ddb"),
                   COLOR_14("#81d4fa"),
                   COLOR_15("#f8bbd0"),
                    
                   COLOR_16("#d7ccc8"),
                   COLOR_17("#cfd8dc"),
                   COLOR_18("#ffcdd2"),
                   COLOR_19("#c8e6c9"),
                   COLOR_20("#bbdefb")
            - 저장 타입 : REFRIGERATED, FROZEN
            - 동일한 (냉장고 + 저장 타입 + 이름) 조합은 중복 생성할 수 없습니다.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "카테고리 생성 성공"),
            @ApiResponse(responseCode = "409", description = "중복 생성 시도"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (null 값, 정의되지 않은 색상/저장타입 입력 등)")
    })
    @PostMapping("/ingredients/categories")
    public ResponseEntity<Api<CreateCategoryResponse>> create(
            @Parameter(hidden = true) @AuthFridge Fridge fridge,
            @Valid @RequestBody CreateCategoryRequest request)
    {
        Long id = service.create(fridge, request.toCommand());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Api.success(new CreateCategoryResponse(id)));
    }

    @Operation(summary = "카테고리 수정",
            description = """
            카테고리의 저장 타입, 이름, 색상 중 원하는 값을 수정합니다.
            - 색상: COLOR_1 ~ COLOR_20 (UI 순서대로)
            - 저장 타입 : REFRIGERATED, FROZEN
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 수정 성공"),
            @ApiResponse(responseCode = "400", description = "변경 사항이 없는 경우(모두 null), 이름 수정 시 빈 값인 경우"),
            @ApiResponse(responseCode = "409", description = "수정한 카테고리가 이미 존재하는 카테고리와 중복인 경우")
    })
    @PatchMapping("/ingredients/categories/{fridgeCategoryId}")
    public ResponseEntity<Api<UpdateCategoryResponse>> update(
            @Parameter(hidden = true) @AuthFridge Fridge fridge,
            @PathVariable Long fridgeCategoryId,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        Long updatedId = service.update(fridge, request.toCommand(fridgeCategoryId));
        return ResponseEntity.ok(
                Api.success(new UpdateCategoryResponse(updatedId))
        );
    }

    @Operation(summary = "카테고리 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리 삭제")
    })
    @DeleteMapping("/ingredients/categories/{fridgeCategoryId}")
    public ResponseEntity<Api<Void>> delete(
            @Parameter(hidden = true) @AuthFridge Fridge fridge,
            @PathVariable Long fridgeCategoryId
    ) {
        service.delete(fridge, fridgeCategoryId);
        return ResponseEntity.ok(Api.success());
    }

    @Operation(
            summary = "카테고리 순서 수정",
            description = """
                드래그 앤 드롭 결과를 반영하여 storageType별 카테고리 순서를 수정합니다.
                - storageType 내 전체 카테고리 순서를 모두 전달해야 합니다.
                - position은 1부터 시작하며 연속되어야 합니다.
                """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "냉장 카테고리 순서 수정 예시",
                                    value = """
                                        {
                                          "storageType": "REFRIGERATED",
                                          "orders": [
                                            {
                                              "categoryId": 12,
                                              "position": 1
                                            },
                                            {
                                              "categoryId": 7,
                                              "position": 2
                                            },
                                            {
                                              "categoryId": 3,
                                              "position": 3
                                            }
                                          ]
                                        }
                                        """
                            )
                    }
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 순서 수정 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                        잘못된 요청
                        - 요청에 중복된 카테고리 ID가 포함된 경우
                        - 요청에 중복된 순서 값이 포함된 경우
                        - 요청의 순서 값이 1부터 연속되지 않은 경우
                        """
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않거나 접근할 수 없는 카테고리가 포함된 경우"
            )
    })
    @PutMapping("/ingredients/categories/order")
    public ResponseEntity<Api<Void>> updateOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateCategoryOrderRequest request
    ) {
        long userId = Long.parseLong(userDetails.getUsername());
        service.updateOrder(userId, request);
        return ResponseEntity.ok(Api.success());
    }

}
