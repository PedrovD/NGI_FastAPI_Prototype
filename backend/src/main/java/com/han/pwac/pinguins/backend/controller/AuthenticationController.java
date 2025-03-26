package com.han.pwac.pinguins.backend.controller;


import com.han.pwac.pinguins.backend.annotations.OAuth2;
import com.han.pwac.pinguins.backend.annotations.Sanitation;
import com.han.pwac.pinguins.backend.domain.UserInfo;
import com.han.pwac.pinguins.backend.services.UserTokenService;
import com.han.pwac.pinguins.backend.domain.DTO.VerificationDto;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
public class AuthenticationController {
    private final UserTokenService userTokenService;

    @Autowired
    public AuthenticationController(UserTokenService userTokenService) {
        this.userTokenService = userTokenService;
    }

    @OAuth2
    @GetMapping("/verify")
    public VerificationDto getCookie(UserInfo info) {
        return userTokenService.getVerificationByProviderId(Optional.ofNullable(info.id()));
    }

    @OAuth2
    @Sanitation
    @PatchMapping("/set-email")
    public void setEmail(UserInfo info, @RequestBody @Email String email) {
        Optional<Integer> userId = userTokenService.getVerificationByProviderId(Optional.ofNullable(info.id())).getUserId();

        // userid should be found as the user should be authenticated by the security config
        userTokenService.setEmail(userId.get(), email);
    }
}
