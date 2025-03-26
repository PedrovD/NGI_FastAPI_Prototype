package com.han.pwac.pinguins.backend.annotations;

import com.han.pwac.pinguins.backend.authentication.Provider;
import com.han.pwac.pinguins.backend.domain.UserInfo;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OAuth2Aspect {
    @Around("@annotation(OAuth2)")
    public Object inject(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        OAuth2 annotation = methodSignature.getMethod().getAnnotation(OAuth2.class);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken)authentication;
        if (token == null && !annotation.required()) {
            return joinPoint.proceed(joinPoint.getArgs());
        } else if (token == null) {
            throw new NotFoundException("OAuth login niet gevonden");
        }

        OAuth2User principal = token.getPrincipal();

        Provider provider = Provider.valueOf(token.getAuthorizedClientRegistrationId().toUpperCase());

        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++ ){
            if (args[i] instanceof UserInfo) {
                args[i] = provider.getUserInfo(principal);
            }
        }

        return joinPoint.proceed(args);  // Proceed with the controller method execution
    }
}