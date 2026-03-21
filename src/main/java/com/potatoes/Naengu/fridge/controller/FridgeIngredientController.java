package com.potatoes.Naengu.fridge.controller;

import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.fridge.dto.CreateFridgeIngredientRequest;
import com.potatoes.Naengu.fridge.dto.CreateFridgeIngredientResponse;
import com.potatoes.Naengu.fridge.dto.UpdateFridgeIngredientRequest;
import com.potatoes.Naengu.fridge.dto.UpdateFridgeIngredientResponse;
import com.potatoes.Naengu.fridge.service.FridgeIngredientService;
import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.auth.annotation.AuthFridge;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "fridge-ingredient-controller", description = "냉장고 재료 API")
@RestController
public class FridgeIngredientController {

    private final FridgeIngredientService service;

    public FridgeIngredientController(FridgeIngredientService service) {
        this.service = service;
    }

    @Operation(summary = "내 냉장고의 카테고리에 새로운 재료 저장")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "재료 저장 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리에 재료 저장 시도, 서비스에 미등록된 재료 저장 시도"),
            @ApiResponse(responseCode = "409", description = "(카테고리 + 재료) 중복")
    })
    @PostMapping("/ingredients")
    public ResponseEntity<Api<CreateFridgeIngredientResponse>> create(
            @Parameter(hidden = true) @AuthFridge Fridge fridge,
            @Valid @RequestBody CreateFridgeIngredientRequest request
    ) {
        Long id = service.create(fridge, request.toCommand());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Api.success(new CreateFridgeIngredientResponse(id)));
    }

    @Operation(summary = "냉장고 재료 수정",
    description = "카테고리(위치)와 재료 중 원하는 값을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "냉장고 재료 수정 성공"),
            @ApiResponse(responseCode = "400", description = "수정하는 값이 없는 경우"),
            @ApiResponse(responseCode = "409", description = "수정 시도 값(카테고리 + 재료) 중복"),
            @ApiResponse(responseCode = "404", description = "없는 카테고리로 수정 시도, 서비스에 미등록된 재료로 수정 시도")
    })
    @PatchMapping("/ingredients/{fridgeIngredientId}")
    public ResponseEntity<Api<UpdateFridgeIngredientResponse>> update(
            @Parameter(hidden = true) @AuthFridge Fridge fridge,
            @PathVariable Long fridgeIngredientId,
            @Valid @RequestBody UpdateFridgeIngredientRequest request
    ) {
        Long updatedId = service.update(fridge, request.toCommand(fridgeIngredientId));
        return ResponseEntity.ok(
                Api.success(new UpdateFridgeIngredientResponse(updatedId))
        );
    }

    @Operation(summary = "냉장고 재료 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "냉장고 재료 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 냉장고 재료 삭제")
    })
    @DeleteMapping("/ingredients/{ingredientsId}")
    public ResponseEntity<Api<Void>> delete(
            @Parameter(hidden = true) @AuthFridge Fridge fridge,
            @PathVariable Long ingredientsId
    ) {
        service.delete(fridge, ingredientsId);
        return ResponseEntity.ok(Api.success());
    }
}
