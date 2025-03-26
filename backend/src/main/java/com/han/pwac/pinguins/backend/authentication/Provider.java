package com.han.pwac.pinguins.backend.authentication;

import com.han.pwac.pinguins.backend.domain.UserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;

public enum Provider {
    GOOGLE,
    GITHUB;

    public UserInfo getUserInfo(OAuth2User principal) {
        return switch (this) {
            case GITHUB -> new UserInfo(
                    this,
                    Integer.toString(principal.getAttribute("id")),
                    principal.getAttribute("login"),
                    principal.getAttribute("avatar_url"),
                    principal.getAttribute("email")
            );
            case GOOGLE -> new UserInfo(
                    this,
                    principal.getAttribute("sub"),
                    principal.getAttribute("name"),
                    principal.getAttribute("picture"),
                    principal.getAttribute("email")
            );
        };

    }
}
