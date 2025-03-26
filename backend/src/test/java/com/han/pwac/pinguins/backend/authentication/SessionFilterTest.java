package com.han.pwac.pinguins.backend.authentication;

import com.han.pwac.pinguins.backend.domain.DTO.VerificationDto;
import com.han.pwac.pinguins.backend.domain.VerificationType;
import com.han.pwac.pinguins.backend.services.UserTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionFilterTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private OAuth2AuthenticationToken authentication;

    @Mock
    private UserTokenService tokenService;

    @InjectMocks
    private SessionFilter sut;

    @Mock
    private ServletRequest request;

    @Mock
    private ServletResponse response;

    @Mock
    private FilterChain filterChain;

    private static final String SUB = "12992";
    private static final String NAME = "name";
    private static final String PICTURE = "https://img.com";
    private static final String EMAIL = "https://img.com";

    private static final List<GrantedAuthority> ALREADY_ADDED_AUTHORITIES = List.of(new SimpleGrantedAuthority("ROLE1"), new SimpleGrantedAuthority("ROLE2"));

    @BeforeEach
    public void setup() {
        SecurityContextHolder.setContext(securityContext);
    }

    private void addAuthenticationMock() throws ServletException, IOException {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(new OAuth2User() {
            @Override
            public Map<String, Object> getAttributes() {
                HashMap<String, Object> map = new HashMap<>();
                map.put("sub", SUB);
                map.put("name", NAME);
                map.put("picture", PICTURE);
                map.put("email", EMAIL);

                return map;
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }
        });
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn(Provider.GOOGLE.name());
        when(authentication.getAuthorities()).thenReturn(ALREADY_ADDED_AUTHORITIES);
        doNothing().when(securityContext).setAuthentication(ArgumentMatchers.argThat(value -> {
            if (!(value instanceof OAuth2AuthenticationToken)) {
                return false;
            }
            authentication = (OAuth2AuthenticationToken)value;
            return true;
        }));

        doNothing().when(filterChain).doFilter(request, response);
    }

    @Test
    public void test_SessionFilter_authenticationAdded() throws ServletException, IOException {
        // Arrange

        addAuthenticationMock();

        VerificationType verificationType = VerificationType.STUDENT;
        when(tokenService.getVerificationByProviderId(Optional.of(SUB))).thenReturn(new VerificationDto(verificationType, 1, null));

        OAuth2AuthenticationToken oldAuth = authentication;

        // Act
        sut.doFilter(request, response, filterChain);

        // Assert
        Authentication newAuthentication = authentication;
        Assertions.assertNotEquals(oldAuth, newAuthentication);
        Assertions.assertInstanceOf(OAuth2AuthenticationToken.class, newAuthentication);

        Collection<? extends GrantedAuthority> grantedAuthorities = newAuthentication.getAuthorities();
        for (GrantedAuthority alreadyAddedAuthority : ALREADY_ADDED_AUTHORITIES) {
            Assertions.assertTrue(() -> grantedAuthorities.contains(alreadyAddedAuthority));
        }

        Assertions.assertTrue(grantedAuthorities.contains(new SimpleGrantedAuthority(verificationType.name())));
    }

    @Test
    public void test_SessionFilter_noAuthenticationFound() throws ServletException, IOException {
        // Arrange

        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        sut.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
