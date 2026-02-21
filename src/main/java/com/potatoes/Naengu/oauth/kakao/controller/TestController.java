package com.potatoes.Naengu.oauth.kakao.controller;

import com.potatoes.Naengu.oauth.kakao.details.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//인가작업 확인용 controller
@RestController
public class TestController {

    @GetMapping("/me")
    public String me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return "my id = " + userDetails.getUsername();
    }

}
