package com.potatoes.Naengu.auth.controller;

import com.potatoes.Naengu.auth.dto.JoinDTO;
import com.potatoes.Naengu.auth.service.JoinService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class JoinController {
    private final JoinService joinService;


    public JoinController(JoinService joinService) {
        this.joinService = joinService;
    }

    @PostMapping("/auth/join")
    public String joinProcess(JoinDTO joindto){

        joinService.joinProcess(joindto);

        return "ok";
    }
}
