package com.han.pwac.pinguins.backend.authentication;

import com.han.pwac.pinguins.backend.domain.UserInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProviderTest {
    @Mock
    private OAuth2User principal;

    private static final String ID = "1223";
    private static final String NAME = "name";
    private static final String AVATAR_URL = "https://url.com";
    private static final String EMAIL = "email@email.com";


    @BeforeEach
    public void setup() {
        when(principal.getAttribute(anyString())).thenCallRealMethod();
    }

    @Test
    public void test_getUserInfo_github() {
        // Arrange
        Provider provider = Provider.GITHUB;

        HashMap<String, Object> map = new HashMap<>();
        map.put("id", Integer.parseInt(ID));
        map.put("login", NAME);
        map.put("avatar_url", AVATAR_URL);
        map.put("email", EMAIL);

        when(principal.getAttributes()).thenReturn(map);

        // Act
        UserInfo userInfo = provider.getUserInfo(principal);

        // Assert
        Assertions.assertEquals(new UserInfo(
                Provider.GITHUB,
                ID,
                NAME,
                AVATAR_URL,
                EMAIL
        ), userInfo);
    }

    @Test
    public void test_getUserInfo_google() {
        // Arrange
        Provider provider = Provider.GOOGLE;

        HashMap<String, Object> map = new HashMap<>();
        map.put("sub", ID);
        map.put("name", NAME);
        map.put("picture", AVATAR_URL);
        map.put("email", EMAIL);

        when(principal.getAttributes()).thenReturn(map);

        // Act
        UserInfo userInfo = provider.getUserInfo(principal);

        // Assert
        Assertions.assertEquals(new UserInfo(
                Provider.GOOGLE,
                ID,
                NAME,
                AVATAR_URL,
                EMAIL
        ), userInfo);
    }
}
