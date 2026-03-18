package com.potatoes.Naengu.oauth.kakao.controller;

import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import com.potatoes.Naengu.oauth.kakao.service.UserHardDeleteService;
import com.potatoes.Naengu.oauth.kakao.service.UserWithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "회원 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserWithdrawalService userWithdrawalService;
    private final UserHardDeleteService userHardDeleteService;

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자를 탈퇴 처리한다. 탈퇴 후 해당 계정의 JWT는 더 이상 사용할 수 없다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long providerId = Long.parseLong(userDetails.getUsername());
        userWithdrawalService.withdraw(providerId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "회원 완전 삭제", description = "계정과 모든 연관 데이터를 삭제 처리한다. UserEntity는 즉시 물리 삭제, 나머지는 배치로 정리된다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    @DeleteMapping("/me/permanent")
    public ResponseEntity<Void> hardDelete(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long providerId = Long.parseLong(userDetails.getUsername());
        userHardDeleteService.hardDelete(providerId);
        return ResponseEntity.noContent().build();
    }
}
