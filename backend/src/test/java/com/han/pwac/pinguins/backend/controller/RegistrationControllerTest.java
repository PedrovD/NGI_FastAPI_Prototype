package com.han.pwac.pinguins.backend.controller;

import com.han.pwac.pinguins.backend.authentication.Provider;
import com.han.pwac.pinguins.backend.domain.DTO.*;
import com.han.pwac.pinguins.backend.domain.RegistrationId;
import com.han.pwac.pinguins.backend.domain.UserInfo;
import com.han.pwac.pinguins.backend.domain.VerificationType;
import com.han.pwac.pinguins.backend.services.BusinessService;
import com.han.pwac.pinguins.backend.services.RegistrationService;
import com.han.pwac.pinguins.backend.services.TaskService;
import com.han.pwac.pinguins.backend.services.UserTokenService;
import com.han.pwac.pinguins.backend.services.contract.IBaseService;
import com.han.pwac.pinguins.backend.services.contract.IMailService;
import com.han.pwac.pinguins.backend.services.contract.IStudentService;
import net.bytebuddy.implementation.bind.annotation.Super;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import javax.naming.AuthenticationException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
public class RegistrationControllerTest {
    @Mock
    private RegistrationService registrationService;
    @Mock
    private UserTokenService userTokenService;
    @Mock
    private IMailService mailService;
    @Mock
    private IStudentService studentService;
    @InjectMocks
    private RegistrationController sut;
    @Mock
    private BusinessService businessService;
    @Mock
    private TaskService taskService;

    @BeforeEach
    public void setUp() {
        sut.frontendUrl = "";
    }

    @Test
    public void test_registrationController_ValidUpdate() {
        // arrange
        PatchRegistrationDto patchRegistrationDto = new PatchRegistrationDto(1, 1, true, "");

        SupervisorDto supervisorDto = new SupervisorDto();
        supervisorDto.setSupervisorId(1);
        RegistrationId id = new RegistrationId(1, 1);
        when(registrationService.findById(eq(id))).thenReturn(Optional.of(new RegistrationDto(
                1,
                1,
                1,
                "my description",
                Optional.empty(),
                "my response"
        )));

        when(registrationService.update(eq(id), any())).thenReturn(true);

        when(userTokenService.getVerificationByProviderId(Optional.of("sessionId"))).thenReturn(new VerificationDto(VerificationType.SUPERVISOR, 1, 1));

        when(studentService.findById(1)).thenReturn(Optional.of(new StudentRepositoryDto(1, "", "name", "description", new FileDto(Optional.empty()), new FileDto(Optional.empty()), "email@mail.com")));

        when(businessService.getAllSupervisors(1)).thenReturn(List.of(
                supervisorDto,
                new SupervisorDto()
        ));

        when(taskService.findById(1)).thenReturn(Optional.of(new TaskDto(1, 1, "title", "description", 1)));

        // act
        ResponseEntity<Object> response = sut.updateRegistration(patchRegistrationDto, new UserInfo(Provider.GITHUB, "sessionId", null, null, null));

        // assert
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());

        verify(mailService, times(1)).sendMail(any());
    }

    @Test
    public void test_registrationController_IdNotFound() {
        // arrange
        PatchRegistrationDto patchRegistrationDto = new PatchRegistrationDto(1, 1, true, "");

        RegistrationId id = new RegistrationId(1, 1);
        when(registrationService.findById(id)).thenReturn(Optional.empty());

        when(userTokenService.getVerificationByProviderId(Optional.of("sessionId"))).thenReturn(new VerificationDto(VerificationType.SUPERVISOR, 1, 1));

        // act
        ResponseEntity<Object> response = sut.updateRegistration(patchRegistrationDto, new UserInfo(Provider.GITHUB, "sessionId", null, null, null));

        // assert
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void test_registrationController_BadData() {

        SupervisorDto supervisorDto = new SupervisorDto();
        supervisorDto.setSupervisorId(1);
        // arrange
        PatchRegistrationDto patchRegistrationDto = new PatchRegistrationDto(1, 1, true, "");

        RegistrationId id = new RegistrationId(1, 1);
        when(registrationService.findById(eq(id))).thenReturn(Optional.of(new RegistrationDto(
                1,
                1,
                1,
                null,
                Optional.empty(),
                null
        )));


        when(registrationService.update(eq(id), any())).thenReturn(false);

        when(userTokenService.getVerificationByProviderId(Optional.of("sessionId"))).thenReturn(new VerificationDto(VerificationType.SUPERVISOR, 1, 1));

        when(businessService.getAllSupervisors(1)).thenReturn(List.of(
                supervisorDto,
                new SupervisorDto()
        ));

        // act
        ResponseEntity<Object> response = sut.updateRegistration(patchRegistrationDto, new UserInfo(Provider.GITHUB, "sessionId", null, null, null));

        // assert
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void createRegistration_successfulAttemptStudent() throws AuthenticationException {
        // Arrange
        int taskId = 1;
        String reason = "Test motivation";
        String authToken = "niceToken";
        int userId = 1;

        VerificationDto verification = new VerificationDto(VerificationType.STUDENT, userId, null);
        when(userTokenService.getVerificationByProviderId(Optional.of(authToken))).thenReturn(verification);
        when(registrationService.addRegistration(taskId, userId, reason)).thenReturn(true);

        // Act
        ResponseEntity<?> response = sut.createRegistration(taskId, reason, new UserInfo(Provider.GITHUB, authToken, null, null, null));

        // Assert
        verify(registrationService).addRegistration(taskId, userId, reason);
        assertEquals(201, response.getStatusCode().value());
    }

    @Test
    public void createRegistration_supervisorAttempt() throws AuthenticationException {
        // Arrange
        int taskId = 1;
        String reason = "Test motivation";
        String authToken = "token";

        VerificationDto verification = new VerificationDto(VerificationType.SUPERVISOR, 1, null);
        when(userTokenService.getVerificationByProviderId(Optional.of(authToken))).thenReturn(verification);

        // Act & Assert
        assertThrows(AuthenticationException.class,
                () -> sut.createRegistration(taskId, reason, new UserInfo(Provider.GITHUB, authToken, null, null, null)));
    }

    @Test
    public void createRegistration_failedRegistration() throws AuthenticationException {
        // Arrange
        int taskId = 1;
        String reason = "Test motivation";
        String authToken = "token";
        int userId = 1;

        VerificationDto verification = new VerificationDto(VerificationType.STUDENT, userId, null);
        when(userTokenService.getVerificationByProviderId(Optional.of(authToken))).thenReturn(verification);
        when(registrationService.addRegistration(taskId, userId, reason)).thenReturn(false);

        // Act
        ResponseEntity<?> response = sut.createRegistration(taskId, reason, new UserInfo(Provider.GITHUB, authToken, null, null, null));

        // Assert
        verify(registrationService).addRegistration(taskId, userId, reason);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void test_registrationController_InvalidSupervisor() {
        // arrange
        PatchRegistrationDto patchRegistrationDto = new PatchRegistrationDto(1, 1, true, "");

        RegistrationId id = new RegistrationId(1, 1);
        when(registrationService.findById(eq(id))).thenReturn(Optional.of(new RegistrationDto(
                1,
                1,
                1,
                "my description",
                Optional.empty(),
                "my response"
        )));

        when(registrationService.update(eq(id), any())).thenReturn(true);

        when(userTokenService.getVerificationByProviderId(Optional.of("sessionId"))).thenReturn(new VerificationDto(VerificationType.SUPERVISOR, 2, 1));

        // act
        ResponseEntity<Object> response = sut.updateRegistration(patchRegistrationDto, new UserInfo(Provider.GITHUB, "sessionId", null, null, null));

        // assert
        Assertions.assertEquals(403, response.getStatusCode().value());
    }

    @Test
    public void getRegistrationsForUser_successfulStudent() throws AuthenticationException {
        // Arrange
        String authToken = "token";
        int userId = 1;
        Collection<Integer> testTaskIds = Arrays.asList(1, 2, 3);

        VerificationDto verification = new VerificationDto(VerificationType.STUDENT, userId, null);
        when(userTokenService.getVerificationByProviderId(Optional.of(authToken))).thenReturn(verification);
        when(registrationService.getRegistrationsForUser(userId)).thenReturn(testTaskIds);

        // Act
        ResponseEntity<Collection<Integer>> response = sut.getRegistrationsForUser(new UserInfo(Provider.GITHUB, authToken, null, null, null));

        // Assert
        verify(registrationService).getRegistrationsForUser(userId);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(testTaskIds, response.getBody());
    }

    @Test
    public void getRegistrationsForUser_noRegistrations() throws AuthenticationException {
        // Arrange
        String authToken = "token";
        int userId = 1;
        Collection<Integer> noTaskIds = Arrays.asList();

        VerificationDto verification = new VerificationDto(VerificationType.STUDENT, userId, null);
        when(userTokenService.getVerificationByProviderId(Optional.of(authToken))).thenReturn(verification);
        when(registrationService.getRegistrationsForUser(userId)).thenReturn(noTaskIds);

        // Act
        ResponseEntity<Collection<Integer>> response = sut.getRegistrationsForUser(new UserInfo(Provider.GITHUB, authToken, null, null, null));

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(noTaskIds, response.getBody());
        verify(registrationService).getRegistrationsForUser(userId);
    }

    @Test
    public void getRegistrationsForUser_supervisorAttempt() {
        // Arrange
        String authToken = "token";
        VerificationDto verification = new VerificationDto(VerificationType.SUPERVISOR, 1, null);
        when(userTokenService.getVerificationByProviderId(Optional.of(authToken))).thenReturn(verification);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> sut.getRegistrationsForUser(new UserInfo(Provider.GITHUB, authToken, null, null, null)));
        assertEquals("U bent geen student.", exception.getMessage());
    }
}
