package com.han.pwac.pinguins.backend.annotations;

import com.han.pwac.pinguins.backend.authentication.Provider;
import com.han.pwac.pinguins.backend.domain.UserInfo;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OAuth2AspectTest {
    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private OAuth2AuthenticationToken authentication;

    @Mock
    private MethodSignature signature;

    @Mock
    private Method method;

    @Mock
    private OAuth2 annotation;

    private static final String SUB = "12992";
    private static final String NAME = "name";
    private static final String PICTURE = "https://img.com";
    private static final String EMAIL = "https://img.com";

    @BeforeEach
    public void setup() {
        SecurityContextHolder.setContext(securityContext);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(method.getAnnotation(OAuth2.class)).thenReturn(annotation);
    }

    private void addAuthenticationMock() {
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
    }

    @Test
    public void test_inject_valid() throws Throwable {
        // Arrange
        AtomicReference<UserInfo> capturedInfo = new AtomicReference<>();

        when(joinPoint.getArgs()).thenReturn(new Object[]{
                "firstArg",
                new UserInfo(null, null, null, null, null),
                "thirdArg"
        });

        addAuthenticationMock();

        String returnValue = "my value";
        when(joinPoint.proceed(ArgumentMatchers.argThat(value -> {
            if (value.length != 3) {
                return false;
            }

            Object userInfoObject = value[1];
            if (!(userInfoObject instanceof UserInfo)) {
                return false;
            }
            capturedInfo.set((UserInfo) userInfoObject);

            return value[0] instanceof String v && v.equals("firstArg")
                    && value[2] instanceof String v2 && v2.equals("thirdArg");
        }))).thenReturn(returnValue);

        // Act
        Object value = new OAuth2Aspect().inject(joinPoint);

        // Assert
        Assertions.assertInstanceOf(String.class, value);
        Assertions.assertEquals(returnValue, value);

        Assertions.assertEquals(SUB, capturedInfo.get().id());
        Assertions.assertEquals(NAME, capturedInfo.get().name());
        Assertions.assertEquals(PICTURE, capturedInfo.get().avatarUrl());
        Assertions.assertEquals(EMAIL, capturedInfo.get().email());
    }

    @Test
    public void test_inject_requiredButNullToken()  {
        // Arrange
        when(annotation.required()).thenReturn(true);

        // Act + Assert
        Assertions.assertThrows(NotFoundException.class,
                () -> new OAuth2Aspect().inject(joinPoint));
    }

    @Test
    public void test_inject_notRequiredAndNullToken() throws Throwable {
        // Arrange
        when(annotation.required()).thenReturn(false);
        AtomicReference<UserInfo> capturedInfo = new AtomicReference<>();

        when(joinPoint.getArgs()).thenReturn(new Object[]{
                "firstArg",
                new UserInfo(null, null, null, null, null),
                "thirdArg"
        });

        String returnValue = "returnValue";
        when(joinPoint.proceed(ArgumentMatchers.argThat(value -> {
            if (value.length != 3) {
                return false;
            }

            Object userInfoObject = value[1];
            if (!(userInfoObject instanceof UserInfo)) {
                return false;
            }
            capturedInfo.set((UserInfo) userInfoObject);

            return value[0] instanceof String v && v.equals("firstArg")
                    && value[2] instanceof String v2 && v2.equals("thirdArg");
        }))).thenReturn(returnValue);

        // Act
        Object value = new OAuth2Aspect().inject(joinPoint);

        // Assert
        Assertions.assertInstanceOf(String.class, value);
        Assertions.assertEquals(returnValue, value);

        Assertions.assertNull(capturedInfo.get().email());
        Assertions.assertNull(capturedInfo.get().name());
        Assertions.assertNull(capturedInfo.get().id());
        Assertions.assertNull(capturedInfo.get().avatarUrl());
    }

    @Test
    public void test_inject_noUserInfoArg() throws Throwable {
        // Arrange
        when(joinPoint.getArgs()).thenReturn(new Object[]{
                "firstArg",
                "secondArg"
        });

        String returnValue = "returnValue";
        when(joinPoint.proceed(eq(new Object[]{
                "firstArg",
                "secondArg"
        }))).thenReturn(returnValue);

        // Act
        Object value = new OAuth2Aspect().inject(joinPoint);

        // Assert
        Assertions.assertInstanceOf(String.class, value);
        Assertions.assertEquals(returnValue, value);
    }
}
