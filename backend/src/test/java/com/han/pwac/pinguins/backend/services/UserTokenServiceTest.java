package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.DTO.*;
import com.han.pwac.pinguins.backend.domain.User;
import com.han.pwac.pinguins.backend.domain.VerificationType;
import com.han.pwac.pinguins.backend.exceptions.InternalException;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.repository.SupervisorDao;
import com.han.pwac.pinguins.backend.repository.TeacherDao;
import com.han.pwac.pinguins.backend.repository.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserTokenServiceTest {

    @InjectMocks
    private UserTokenService sut;

    @Mock
    private UserDao userDao;

    @Mock
    private SupervisorDao supervisorDAO;

    @Mock
    private TeacherDao teacherDao;

    @Mock
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void checkIfProviderIdMatchesBusinessId_happy() {
        // Arrange
        String providerId = "providerId";
        int projectId = 1;

        User user = new User();
        user.setEmail("test@email.com");
        when(userDao.getByProviderId(providerId)).thenReturn(Optional.of(user));
        when(projectService.getProject(projectId)).thenReturn(new GetProjectDto(1, "Hallo", "Hallo", List.of(new GetSkillDto(1, "Hallo", true)), new BusinessDto(1, "Hallo", "Hallo", new FileDto(Optional.of("hallo.png")), "hallo"), new FileDto(Optional.of("hallo.png"))));
        SupervisorDto supervisor = new SupervisorDto();
        supervisor.setBusinessId(1);
        when(supervisorDAO.findById(anyInt())).thenReturn(Optional.of(supervisor));

        // Act
        boolean result = sut.checkIfProviderIdMatchesBusinessId(providerId, projectId);

        // Assert
        verify(userDao, times(1)).getByProviderId(providerId);
        verify(projectService, times(1)).getProject(projectId);
        verify(supervisorDAO, times(1)).findById(anyInt());
        assertTrue(result);
    }

    @Test
    public void checkIfProviderIdMatchesBusinessId_supervisorHasDifferentBusinessId() {
        // Arrange
        String providerId = "providerId";
        int projectId = 1;

        User user = new User();
        user.setEmail("test@email.com");
        when(userDao.getByProviderId(providerId)).thenReturn(Optional.of(user));
        when(projectService.getProject(projectId)).thenReturn(new GetProjectDto(1, "Hallo", "Hallo", List.of(new GetSkillDto(1, "Hallo", true)), new BusinessDto(1, "Hallo", "Hallo", new FileDto(Optional.of("hallo.png")), "hallo"), new FileDto(Optional.of("hallo.png"))));
        SupervisorDto supervisor = new SupervisorDto();
        supervisor.setBusinessId(2);
        when(supervisorDAO.findById(anyInt())).thenReturn(Optional.of(supervisor));

        // Act
        boolean result = sut.checkIfProviderIdMatchesBusinessId(providerId, projectId);

        // Assert
        verify(userDao, times(1)).getByProviderId(providerId);
        verify(projectService, times(1)).getProject(projectId);
        verify(supervisorDAO, times(1)).findById(anyInt());
        assertFalse(result);
    }

    @Test
    public void checkIfProviderIdMatchesBusinessId_providerIdIsNull() {
        // Arrange
        String providerId = null;
        int projectId = 1;

        // Act + Assert
        assertThrows(NotFoundException.class,
                () -> sut.checkIfProviderIdMatchesBusinessId(providerId, projectId));

        verify(userDao, times(0)).getByProviderId(providerId);
    }

    @Test
    void checkIfProviderIdMatchesBusinessIdShouldThrowNotFoundExceptionWhenProviderIdIsNull() {
        // act & assert
        assertThrows(NotFoundException.class, () -> sut.checkIfProviderIdMatchesBusinessId(null, 1));
    }

    @Test
    void checkIfProviderIdMatchesBusinessIdShouldThrowNotFoundExceptionWhenBusinessIdIsEmpty() {
        // arrange
        when(userDao.getByProviderId("providerId")).thenReturn(Optional.of(new User()));
        when(supervisorDAO.findById(1)).thenReturn(Optional.empty());

        // act & assert
        assertThrows(NotFoundException.class, () -> sut.checkIfProviderIdMatchesBusinessId("providerId", 1));
    }

    @Test
    void getVerificationByProviderIdShouldReturnNoneWhenProviderIdIsEmpty() {
        // act
        VerificationDto result = sut.getVerificationByProviderId(Optional.empty());

        // assert
        assertEquals(VerificationType.NONE, result.getType());
    }

    @Test
    void getVerificationByProviderIdShouldReturnTeacherWhenSTeacherExists() {
        // arrange
        User user = new User();
        user.setId(1);
        user.setEmail("email@email.com");
        TeacherDto teacherDto = new TeacherDto(user.getId());

        when(userDao.getByProviderId("providerId")).thenReturn(Optional.of(user));
        when(teacherDao.findById(1)).thenReturn(Optional.of(teacherDto));

        // act
        VerificationDto result = sut.getVerificationByProviderId(Optional.of("providerId"));

        // assert
        assertEquals(VerificationType.TEACHER, result.getType());
    }

    @Test
    void getVerificationByProviderIdShouldReturnSupervisorWhenSupervisorExists() {
        // arrange
        User user = new User();
        user.setId(1);
        user.setEmail("email@email.com");
        SupervisorDto supervisorDto = new SupervisorDto();
        supervisorDto.setBusinessId(1);
        supervisorDto.setSupervisorId(1);

        when(userDao.getByProviderId("providerId")).thenReturn(Optional.of(user));
        when(supervisorDAO.findById(1)).thenReturn(Optional.of(supervisorDto));

        // act
        VerificationDto result = sut.getVerificationByProviderId(Optional.of("providerId"));

        // assert
        assertEquals(VerificationType.SUPERVISOR, result.getType());
    }

    @Test
    void getVerificationByProviderIdShouldReturnStudentWhenSupervisorDoesNotExist() {
        // arrange
        User user = new User();
        user.setId(1);
        user.setEmail("email@email.com");
        when(userDao.getByProviderId("providerId")).thenReturn(Optional.of(user));
        when(supervisorDAO.findById(1)).thenReturn(Optional.empty());

        // act
        VerificationDto result = sut.getVerificationByProviderId(Optional.of("providerId"));

        // assert
        assertEquals(VerificationType.STUDENT, result.getType());
    }

    @Test
    public void getVerificationByProviderId_userisEmpty() {
        // Arrange
        Optional<String> providerId = Optional.of("providerId");

        when(userDao.getByProviderId(providerId.get())).thenReturn(Optional.empty());

        // Act
        VerificationDto result = sut.getVerificationByProviderId(providerId);

        // Assert
        assertEquals(VerificationType.NONE, result.getType());
    }

    @Test
    public void test_setEmail_valid() {
        // Arrange
        int userId = 1;
        String email = "email@email.com";

        when(userDao.setEmail(userId, email)).thenReturn(true);

        // Act
        sut.setEmail(userId, email);

        // Assert
        verify(userDao, times(1)).setEmail(userId, email);
    }

    @Test
    public void test_setEmail_error() {
        // Arrange
        int userId = 1;
        String email = "email@email.com";

        when(userDao.setEmail(userId, email)).thenReturn(false);

        // Act + Assert
        assertThrows(InternalException.class,
                () -> sut.setEmail(userId, email));

        verify(userDao, times(1)).setEmail(userId, email);
    }
}