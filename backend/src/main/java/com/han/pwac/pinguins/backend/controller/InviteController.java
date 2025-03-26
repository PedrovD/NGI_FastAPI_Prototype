package com.han.pwac.pinguins.backend.controller;

import com.han.pwac.pinguins.backend.annotations.OAuth2;
import com.han.pwac.pinguins.backend.domain.DTO.LinkDto;
import com.han.pwac.pinguins.backend.domain.DTO.VerificationDto;
import com.han.pwac.pinguins.backend.domain.InviteKey;
import com.han.pwac.pinguins.backend.domain.UserInfo;
import com.han.pwac.pinguins.backend.domain.VerificationType;
import com.han.pwac.pinguins.backend.exceptions.IncorrectCredentialsException;
import com.han.pwac.pinguins.backend.services.InviteKeyService;
import com.han.pwac.pinguins.backend.services.UserTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/invite")
public class InviteController {
    private final InviteKeyService inviteKeyService;
    private final UserTokenService userTokenService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${backend.url}")
    private String backendUrl;

    @Autowired
    public InviteController(InviteKeyService inviteKeyService, UserTokenService userTokenService) {
        this.inviteKeyService = inviteKeyService;
        this.userTokenService = userTokenService;
    }

    @OAuth2
    @PostMapping
    public LinkDto invite(@RequestBody(required = false) Integer businessId, UserInfo userInfo) {
        VerificationDto verification = userTokenService.getVerificationByProviderId(Optional.of(userInfo.id()));
        if (verification.getType() == VerificationType.SUPERVISOR && businessId != verification.getBusinessId().get()) {
            throw new IncorrectCredentialsException("U bent niet gemachtigd om deze actie uit te voeren.");
        }
        
        InviteKey newKey = inviteKeyService.saveKey(UUID.randomUUID().toString(), businessId);
        return new LinkDto(backendUrl + "/invite?code="+newKey.getKey(), newKey.getDateTime());
    }

    @GetMapping
    public void invite(@RequestParam String code, HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.getSession().setAttribute("code", code);
        response.sendRedirect(frontendUrl);
    }
}
