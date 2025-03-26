package com.han.pwac.pinguins.backend.authentication;

import com.han.pwac.pinguins.backend.domain.UserInfo;
import com.han.pwac.pinguins.backend.services.UserTokenService;
import com.han.pwac.pinguins.backend.domain.DTO.VerificationDto;
import jakarta.servlet.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SessionFilter implements Filter {
    private final UserTokenService tokenService;

    public SessionFilter(UserTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken)SecurityContextHolder.getContext().getAuthentication();
        if (oAuth2AuthenticationToken ==  null) {
            filterChain.doFilter(request, response);
            return;
        }

        Provider provider = Provider.valueOf(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId().toUpperCase());
        UserInfo userInfo = provider.getUserInfo(oAuth2AuthenticationToken.getPrincipal());

        VerificationDto verificationDTO = tokenService.getVerificationByProviderId(Optional.ofNullable(userInfo.id()));

        Collection<GrantedAuthority> authorities = oAuth2AuthenticationToken.getAuthorities();
        List<GrantedAuthority> newAuthoritiesList = new ArrayList<>(authorities);
        newAuthoritiesList.add(new SimpleGrantedAuthority(verificationDTO.getType().name()));

        OAuth2AuthenticationToken newAuthentication = new OAuth2AuthenticationToken(
                oAuth2AuthenticationToken.getPrincipal(),
                newAuthoritiesList,
                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId()
        );
        SecurityContextHolder.getContext().setAuthentication(newAuthentication);

        filterChain.doFilter(request, response);
    }
}
