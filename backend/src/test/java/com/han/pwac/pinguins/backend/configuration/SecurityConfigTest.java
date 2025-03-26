package com.han.pwac.pinguins.backend.configuration;

import com.han.pwac.pinguins.backend.authentication.Provider;
import com.han.pwac.pinguins.backend.domain.DTO.StudentRepositoryDto;
import com.han.pwac.pinguins.backend.domain.InviteKey;
import com.han.pwac.pinguins.backend.domain.User;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.services.*;
import com.han.pwac.pinguins.backend.services.contract.IFileService;
import com.han.pwac.pinguins.backend.services.contract.IStudentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityConfigTest {
    @Mock
    private UserTokenService tokenService;
    @Mock
    private IStudentService studentService;
    @Mock
    private UserService userService;
    @Mock
    private IFileService fileService;
    @Mock
    private InviteKeyService inviteKeyService;
    @Mock
    private BusinessService businessService;
    @Mock
    private TeacherService teacherService;
    @InjectMocks
    private SecurityConfigHelper sut;

    @Mock
    private SimpleUrlAuthenticationSuccessHandler successHandler;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private OAuth2AuthenticationToken authentication;
    @Mock
    private HttpSession session;

    private static final String SUB = "12992";
    private static final String NAME = "name";
    private static final String PICTURE = "https://img.com";
    private static final String EMAIL = "https://img.com";

    private static final String CODE_NAME = "code";

    public static class SecurityConfigHelper extends SecurityConfig {

        public SecurityConfigHelper(UserTokenService tokenService, IStudentService studentService, UserService userService, IFileService fileService, InviteKeyService inviteKeyService, BusinessService businessService, TeacherService teacherService) {
            super(tokenService, studentService, userService, fileService, inviteKeyService, businessService, teacherService);
        }

        @Override
        public void onOAuthAuthenticationSuccess(SimpleUrlAuthenticationSuccessHandler successHandler, HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
            super.onOAuthAuthenticationSuccess(successHandler, request, response, authentication);
        }

        @Override
        public void onOAuthFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
            super.onOAuthFailure(request, response, exception);
        }
    }

    private void addAuthenticationMock() {
        when(request.getSession()).thenReturn(session);
        doNothing().when(session).removeAttribute(CODE_NAME);

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
    public void test_onOAuthAuthenticationSuccess_createStudent() throws ServletException, IOException {
        // Arrange
        doNothing().when(successHandler).onAuthenticationSuccess(request, response, authentication);

        addAuthenticationMock();

        when(session.getAttribute(CODE_NAME)).thenReturn(null);

        ArgumentCaptor<StudentRepositoryDto> studentCaptor = ArgumentCaptor.forClass(StudentRepositoryDto.class);

        String avatarImage = "image.png";
        when(userService.findByProviderId(SUB)).thenReturn(Optional.empty());
        when(fileService.downloadFile(new URL(PICTURE))).thenReturn(avatarImage);
        when(studentService.add(studentCaptor.capture())).thenReturn(true);

        // Act
        sut.onOAuthAuthenticationSuccess(successHandler, request, response, authentication);

        // Assert
        StudentRepositoryDto student = studentCaptor.getValue();
        Assertions.assertEquals(student.username(), NAME);
        Assertions.assertEquals(student.providerId(), SUB);
        Assertions.assertEquals(student.email(), EMAIL);
        Assertions.assertEquals(student.profilePicture().path().get(), avatarImage);

        verify(successHandler, times(1)).onAuthenticationSuccess(request, response, authentication);
    }

    @Test
    public void test_onOAuthAuthenticationSuccess_createStudentUserNotEmpty() throws ServletException, IOException {
        // Arrange
        doNothing().when(successHandler).onAuthenticationSuccess(request, response, authentication);

        addAuthenticationMock();

        when(session.getAttribute(CODE_NAME)).thenReturn(null);

        when(userService.findByProviderId(SUB)).thenReturn(Optional.of(new User()));

        // Act
        sut.onOAuthAuthenticationSuccess(successHandler, request, response, authentication);

        // Assert
        verify(studentService, times(0)).add(any(StudentRepositoryDto.class));
        verify(successHandler, times(1)).onAuthenticationSuccess(request, response, authentication);
    }

    @Test
    public void test_onOAuthAuthenticationSuccess_createBusiness() throws ServletException, IOException {
        // Arrange
        doNothing().when(successHandler).onAuthenticationSuccess(request, response, authentication);

        addAuthenticationMock();

        ArgumentCaptor<StudentRepositoryDto> studentCaptor = ArgumentCaptor.forClass(StudentRepositoryDto.class);

        String code = "my code";
        when(session.getAttribute(CODE_NAME)).thenReturn(code);

        User user = new User();
        user.setId(1);

        when(inviteKeyService.getInviteKeyFromCode(code)).thenReturn(new InviteKey(code, 1, Timestamp.valueOf(LocalDateTime.now())));

        String avatarImage = "image.png";
        when(userService.findByProviderId(SUB)).thenReturn(Optional.empty(), Optional.of(user));
        when(fileService.downloadFile(new URL(PICTURE))).thenReturn(avatarImage);
        when(studentService.add(studentCaptor.capture())).thenReturn(true);

        doNothing().when(businessService).addUserToBusiness(1, 1);

        doNothing().when(successHandler).setDefaultTargetUrl(anyString());

        // Act
        sut.onOAuthAuthenticationSuccess(successHandler, request, response, authentication);

        // Assert
        StudentRepositoryDto student = studentCaptor.getValue();
        Assertions.assertEquals(student.username(), NAME);
        Assertions.assertEquals(student.providerId(), SUB);
        Assertions.assertEquals(student.email(), EMAIL);
        Assertions.assertEquals(student.profilePicture().path().get(), avatarImage);

        verify(businessService, times(1)).addUserToBusiness(1, 1);
        verify(inviteKeyService, times(1)).deleteKey(code);
        verify(successHandler, times(1)).onAuthenticationSuccess(request, response, authentication);
    }

    @Test
    public void test_onOAuthAuthenticationSuccess_createTeacher() throws ServletException, IOException {
        // Arrange
        doNothing().when(successHandler).onAuthenticationSuccess(request, response, authentication);

        addAuthenticationMock();

        ArgumentCaptor<StudentRepositoryDto> studentCaptor = ArgumentCaptor.forClass(StudentRepositoryDto.class);

        String code = "my code";
        when(session.getAttribute(CODE_NAME)).thenReturn(code);

        User user = new User();
        user.setId(1);

        when(inviteKeyService.getInviteKeyFromCode(code)).thenReturn(new InviteKey(code, null, Timestamp.valueOf(LocalDateTime.now())));

        String avatarImage = "image.png";
        when(userService.findByProviderId(SUB)).thenReturn(Optional.empty(), Optional.of(user));
        when(fileService.downloadFile(new URL(PICTURE))).thenReturn(avatarImage);
        when(studentService.add(studentCaptor.capture())).thenReturn(true);

        doNothing().when(teacherService).addTeacher(1);

        // Act
        sut.onOAuthAuthenticationSuccess(successHandler, request, response, authentication);

        // Assert
        StudentRepositoryDto student = studentCaptor.getValue();
        Assertions.assertEquals(student.username(), NAME);
        Assertions.assertEquals(student.providerId(), SUB);
        Assertions.assertEquals(student.email(), EMAIL);
        Assertions.assertEquals(student.profilePicture().path().get(), avatarImage);

        verify(teacherService, times(1)).addTeacher(1);
        verify(inviteKeyService, times(1)).deleteKey(code);
        verify(successHandler, times(1)).onAuthenticationSuccess(request, response, authentication);
    }

    @Test
    public void test_onOAuthAuthenticationSuccess_createTeacher_withBusinessIdAs0() throws ServletException, IOException {
        // Arrange
        doNothing().when(successHandler).onAuthenticationSuccess(request, response, authentication);

        addAuthenticationMock();

        ArgumentCaptor<StudentRepositoryDto> studentCaptor = ArgumentCaptor.forClass(StudentRepositoryDto.class);

        String code = "my code";
        when(session.getAttribute(CODE_NAME)).thenReturn(code);

        User user = new User();
        user.setId(1);

        when(inviteKeyService.getInviteKeyFromCode(code)).thenReturn(new InviteKey(code, 0, Timestamp.valueOf(LocalDateTime.now())));

        String avatarImage = "image.png";
        when(userService.findByProviderId(SUB)).thenReturn(Optional.empty(), Optional.of(user));
        when(fileService.downloadFile(new URL(PICTURE))).thenReturn(avatarImage);
        when(studentService.add(studentCaptor.capture())).thenReturn(true);

        doNothing().when(teacherService).addTeacher(1);

        // Act
        sut.onOAuthAuthenticationSuccess(successHandler, request, response, authentication);

        // Assert
        StudentRepositoryDto student = studentCaptor.getValue();
        Assertions.assertEquals(student.username(), NAME);
        Assertions.assertEquals(student.providerId(), SUB);
        Assertions.assertEquals(student.email(), EMAIL);
        Assertions.assertEquals(student.profilePicture().path().get(), avatarImage);

        verify(teacherService, times(1)).addTeacher(1);
        verify(inviteKeyService, times(1)).deleteKey(code);
        verify(successHandler, times(1)).onAuthenticationSuccess(request, response, authentication);
    }

    @Test
    public void test_onOAuthAuthenticationSuccess_withCode_userAlreadyExists() throws ServletException, IOException {
        // Arrange
        doNothing().when(response).sendRedirect(anyString());

        addAuthenticationMock();

        String code = "my code";
        when(session.getAttribute(CODE_NAME)).thenReturn(code);

        User user = new User();
        user.setId(1);

        when(userService.findByProviderId(SUB)).thenReturn(Optional.of(user));

        // Act
        sut.onOAuthAuthenticationSuccess(successHandler, request, response, authentication);

        // Assert
        verify(response, times(1)).sendRedirect(anyString());
    }

    @Test
    public void test_onOAuthAuthenticationSuccess_inviteKeyNotFound() throws ServletException, IOException {
        // Arrange
        doNothing().when(response).sendRedirect(anyString());

        addAuthenticationMock();

        String code = "my code";
        when(session.getAttribute(CODE_NAME)).thenReturn(code);

        when(userService.findByProviderId(SUB)).thenReturn(Optional.empty());
        when(inviteKeyService.getInviteKeyFromCode(code)).thenThrow(new NotFoundException("not found"));

        // Act
        sut.onOAuthAuthenticationSuccess(successHandler, request, response, authentication);

        // Assert
        verify(response, times(1)).sendRedirect(anyString());
    }

    @Test
    public void test_onOAuthAuthenticationSuccess_error() throws ServletException, IOException {
        // Arrange
        doNothing().when(response).sendRedirect(anyString());

        addAuthenticationMock();

        String code = "my code";
        when(session.getAttribute(CODE_NAME)).thenReturn(code);

        when(userService.findByProviderId(SUB)).thenReturn(Optional.empty());
        when(inviteKeyService.getInviteKeyFromCode(code)).thenThrow(new RuntimeException("error"));

        // Act
        sut.onOAuthAuthenticationSuccess(successHandler, request, response, authentication);

        // Assert
        verify(response, times(1)).sendRedirect(anyString());
    }

    @Test
    public void test_onOAuthFailure_valid() throws IOException {
        // Arrange
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        doNothing().when(response).sendRedirect(argumentCaptor.capture());

        AuthenticationException authenticationException = mock(AuthenticationException.class);
        when(authenticationException.getMessage()).thenReturn("my error");

        // Act
        sut.onOAuthFailure(request, response, authenticationException);

        // Assert
        verify(authenticationException, times(1)).getMessage();
        Assertions.assertTrue(argumentCaptor.getValue().contains("Probeer het later opnieuw"));
    }

    @Test
    public void test_onOAuthFailure_accessDenied() throws IOException {
        // Arrange
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        doNothing().when(response).sendRedirect(argumentCaptor.capture());

        AuthenticationException authenticationException = mock(AuthenticationException.class);
        when(authenticationException.getMessage()).thenReturn("[access_denied]");

        // Act
        sut.onOAuthFailure(request, response, authenticationException);

        // Assert
        verify(authenticationException, times(1)).getMessage();
        Assertions.assertTrue(argumentCaptor.getValue().contains("toegang geweigerd"));
    }
}
