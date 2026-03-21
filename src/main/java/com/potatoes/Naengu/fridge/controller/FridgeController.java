package com.potatoes.Naengu.fridge.controller;

import com.potatoes.Naengu.fridge.dto.query.FridgeOverviewResponse;
import com.potatoes.Naengu.fridge.service.FridgeQueryService;
import com.potatoes.Naengu.global.api.Api;
import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="fridge-controller",description = "냉장고 API")
@RestController
public class FridgeController {

    private final FridgeQueryService fridgeQueryService;

    public FridgeController(FridgeQueryService fridgeQueryService) {
        this.fridgeQueryService = fridgeQueryService;
    }

    @Operation(summary = "내 냉장고 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 냉장고 조회 성공"),
            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음")
    })
    @GetMapping("/me/ingredients")
    public ResponseEntity<Api<FridgeOverviewResponse>> overview(
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        long userId = Long.parseLong(userDetails.getUsername());

        FridgeOverviewResponse fridgeOverviewResponse = fridgeQueryService.overview(userId);

        return ResponseEntity.ok(Api.success(fridgeOverviewResponse));

    }

}
