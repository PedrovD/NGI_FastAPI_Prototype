package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.DTO.*;
import com.han.pwac.pinguins.backend.domain.EmailPart;
import com.han.pwac.pinguins.backend.domain.RegistrationId;
import com.han.pwac.pinguins.backend.domain.StudentEmailSelection;
import com.han.pwac.pinguins.backend.repository.contract.ICronJobDao;
import com.han.pwac.pinguins.backend.repository.contract.IRegistrationDao;
import com.han.pwac.pinguins.backend.repository.contract.base.IBaseDao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class RegistrationServiceTest {
    private IRegistrationDao registrationRepository;
    private StudentService studentService;
    private TaskService taskService;
    private ICronJobDao cronJobDao;
    private RegistrationService sut;

    @BeforeEach
    public void setUp() {
        registrationRepository = mock(IRegistrationDao.class);
        studentService = mock(StudentService.class);
        taskService = mock(TaskService.class);
        cronJobDao = mock(ICronJobDao.class);
        sut = new RegistrationService(registrationRepository, studentService, taskService, cronJobDao);
    }

    @Test
    public void test_RegistrationService_getAllRegistrationsForTask() {
        // arrange
        StudentDto student = new StudentDto(
                1,
                "name",
                "description",
                new FileDto(Optional.of("image.png")),
                "email@email.com",
                new FileDto(Optional.of("cv.pdf")),
                new ArrayList<>()
        );
        when(studentService.getStudentById(1)).thenReturn(student);

        ArrayList<RegistrationDto> list = new ArrayList<>();
        String description = "registration description";
        String response = "registration response";
        list.add(new RegistrationDto(1, 1, 1, description, Optional.empty(), response));
        list.add(new RegistrationDto(1, 2, 1, "hallo", Optional.empty(), "niks"));

        when(registrationRepository.getAll()).thenReturn(list);

        // act
        Collection<GetRegistrationDto> registrations = sut.getAllRegistrationsForTask(1);

        // assert
        assertEquals(1, registrations.size());

        GetRegistrationDto first = registrations.stream().findFirst().get();
        assertEquals(student, first.student());
        assertEquals(description, first.reason());
    }

    @Test
    public void addRegistration_successfulAttempt() {
        // Arrange
        int taskId = 1;
        int userId = 1;
        String reason = "Test motivation";
        when(registrationRepository.findById(any(RegistrationId.class))).thenReturn(Optional.empty());
        when(registrationRepository.add(any(RegistrationDto.class))).thenReturn(true);

        // Act
        boolean response = sut.addRegistration(taskId, userId, reason);

        // Assert
        assertTrue(response);
        verify(registrationRepository).findById(new RegistrationId(taskId, userId));
        verify(registrationRepository).add(any(RegistrationDto.class));
    }

    @Test
    public void addRegistration_alreadyExists() {
        // Arrange
        int taskId = 1;
        int userId = 1;
        String reason = "Test motivation";
        when(registrationRepository.findById(any(RegistrationId.class))).thenReturn(Optional.of(new RegistrationDto(taskId, userId, 5, reason, Optional.empty(), "")));

        // Act
        boolean response = sut.addRegistration(taskId, userId, reason);

        // Assert
        assertFalse(response);
        verify(registrationRepository).findById(new RegistrationId(taskId, userId));
    }

    @Test
    public void addRegistration_registrationNotValid() {
        // Arrange
        int taskId = 1;
        int userId = 1;
        String reason = "a".repeat(4001);
        when(registrationRepository.findById(any(RegistrationId.class))).thenReturn(Optional.empty());
        when(registrationRepository.add(any(RegistrationDto.class))).thenReturn(false);

        // Act
        boolean response = sut.addRegistration(taskId, userId, reason);

        // Assert
        assertFalse(response);
        verify(registrationRepository).findById(new RegistrationId(taskId, userId));
    }

    @Test
    public void addRegistration_registrationDaoReturnsFalse() {
        // Arrange
        int taskId = 1;
        int userId = 1;
        String reason = "Test motivation";
        when(registrationRepository.findById(any(RegistrationId.class))).thenReturn(Optional.empty());
        when(registrationRepository.add(any(RegistrationDto.class))).thenReturn(false);

        // Act
        boolean response = sut.addRegistration(taskId, userId, reason);

        // Assert
        assertFalse(response);
        verify(registrationRepository).findById(new RegistrationId(taskId, userId));
        verify(registrationRepository).add(any(RegistrationDto.class));
    }

    @Test
    public void getRegistrationsForUser_returnsListWithTasks() {
        // Arrange
        int userId = 1;
        Collection<Integer> testTaskIds = Arrays.asList(1, 2, 3);
        when(registrationRepository.getRegistrationsForUser(userId)).thenReturn(testTaskIds);

        // Act
        Collection<Integer> taskIds = sut.getRegistrationsForUser(userId);

        // Assert
        assertEquals(testTaskIds, taskIds);
        verify(registrationRepository).getRegistrationsForUser(userId);
    }

    @Test
    public void getRegistrationsForUser_returnsEmptyList() {
        // Arrange
        int userId = 1;
        Collection<Integer> noTaskIds = Arrays.asList();
        when(registrationRepository.getRegistrationsForUser(userId)).thenReturn(noTaskIds);

        // Act
        Collection<Integer> taskIds = sut.getRegistrationsForUser(userId);

        // Assert
        assertTrue(taskIds.isEmpty());
        verify(registrationRepository).getRegistrationsForUser(userId);
    }

    @Test
    public void test_getBatchedMails_valid() {
        // Arrange
        Timestamp d = new Timestamp(142192930);
        ArgumentCaptor<Timestamp> timestampArgumentCaptor = ArgumentCaptor.forClass(Timestamp.class);

        when(cronJobDao.getPreviousRunDate()).thenReturn(d);
        when(registrationRepository.getRegistrationsAfter(timestampArgumentCaptor.capture())).thenReturn(List.of(
                new RegistrationDto(1, 3, 2, "description", Optional.empty(), "response"),
                new RegistrationDto(1, 500, 3, "description", Optional.empty(), "response")
        ));

        when(studentService.getStudentById(2)).thenReturn(new StudentDto(2, "my name", "my description", new FileDto(Optional.empty()), "email@email.com", new FileDto(Optional.empty()), Collections.emptyList()));
        when(studentService.getStudentById(3)).thenReturn(new StudentDto(2, "my name test hallo 234567890", "my description", new FileDto(Optional.empty()), "email@email.com", new FileDto(Optional.empty()), Collections.emptyList()));

        when(taskService.findById(3)).thenReturn(Optional.of(new TaskDto(3, 4, "my title", "my task description", 1)));

        // Act
        Map<Integer, EmailPart> parts = sut.getBatchedMails();

        // Assert

        assertEquals(1, parts.size());

        EmailPart part = parts.get(1);
        assertNotNull(part);

        assertTrue(part.toString().contains("projects/4"));
        assertTrue(part.toString().contains("my name test hallo 234567890"));
        assertTrue(part.toString().contains("Nieuwe aanmeldingen:"));

        assertEquals(d, timestampArgumentCaptor.getValue());
    }

    @Test
    public void test_getBatchedMails_empty() {
        // Arrange
        Timestamp d = new Timestamp(142192930);
        ArgumentCaptor<Timestamp> timestampArgumentCaptor = ArgumentCaptor.forClass(Timestamp.class);

        when(cronJobDao.getPreviousRunDate()).thenReturn(d);
        when(registrationRepository.getRegistrationsAfter(timestampArgumentCaptor.capture())).thenReturn(Collections.emptyList());

        // Act
        Map<Integer, EmailPart> parts = sut.getBatchedMails();

        // Assert
        assertEquals(0, parts.size());

        assertEquals(d, timestampArgumentCaptor.getValue());
    }

    @Test
    public void test_getEmailAddressesForRegistrations_Rejected_valid() {
        // Arrange
        int businessId = 1;
        int studentId = 1;
        String email = "email@email.com";

        when(registrationRepository.getAll()).thenReturn(List.of(
                new RegistrationDto(1, 1, studentId, "desc", Optional.of(false), "resp"),
                new RegistrationDto(1, 1, studentId + 1, "desc", Optional.of(true), "resp"),
                new RegistrationDto(1, 2, studentId + 1, "desc", Optional.of(false), "resp")
        ));
        when(studentService.getStudentById(studentId)).thenReturn(new StudentDto(studentId, "name", "desc", new FileDto(Optional.empty()), email, new FileDto(Optional.empty()), Collections.emptyList()));

        // Act
        Collection<String> emails = sut.getEmailAddressesForRegistrations(StudentEmailSelection.REJECTED.getFlagValue(), businessId);

        // Assert
        Assertions.assertEquals(1, emails.size());

        Assertions.assertEquals(email, emails.stream().findFirst().get());

        verify(registrationRepository, times(1)).getAll();
        verify(studentService, times(1)).getStudentById(studentId);
    }

    @Test
    public void test_getEmailAddressesForRegistrations_Accepted_valid() {
        // Arrange
        int businessId = 1;
        int studentId = 1;
        String email = "email@email.com";

        when(registrationRepository.getAll()).thenReturn(List.of(new RegistrationDto(1, 1, studentId, "desc", Optional.of(true), "resp"), new RegistrationDto(1, 1, studentId + 1, "desc", Optional.of(false), "resp"), new RegistrationDto(1, 1, studentId + 2, "desc", Optional.empty(), "resp")));
        when(studentService.getStudentById(studentId)).thenReturn(new StudentDto(studentId, "name", "desc", new FileDto(Optional.empty()), email, new FileDto(Optional.empty()), Collections.emptyList()));

        // Act
        Collection<String> emails = sut.getEmailAddressesForRegistrations(StudentEmailSelection.ACCEPTED.getFlagValue(), businessId);

        // Assert
        Assertions.assertEquals(1, emails.size());

        Assertions.assertEquals(email, emails.stream().findFirst().get());

        verify(registrationRepository, times(1)).getAll();
        verify(studentService, times(1)).getStudentById(studentId);
    }

    @Test
    public void test_getEmailAddressesForRegistrations_Registered_valid() {
        // Arrange
        int businessId = 1;
        int studentId = 1;
        String email = "email@email.com";

        when(registrationRepository.getAll()).thenReturn(List.of(new RegistrationDto(1, 1, studentId, "desc", Optional.empty(), "resp"), new RegistrationDto(1, 1, studentId + 1, "desc", Optional.of(false), "resp"), new RegistrationDto(1, 1, studentId + 2, "desc", Optional.of(true), "resp")));
        when(studentService.getStudentById(studentId)).thenReturn(new StudentDto(studentId, "name", "desc", new FileDto(Optional.empty()), email, new FileDto(Optional.empty()), Collections.emptyList()));

        // Act
        Collection<String> emails = sut.getEmailAddressesForRegistrations(StudentEmailSelection.REGISTERED.getFlagValue(), businessId);

        // Assert
        Assertions.assertEquals(1, emails.size());

        Assertions.assertEquals(email, emails.stream().findFirst().get());

        verify(registrationRepository, times(1)).getAll();
        verify(studentService, times(1)).getStudentById(studentId);
    }

    @Test
    public void test_getEmailAddressesForRegistrations_Rejected_multiple() {
        // Arrange
        int businessId = 1;
        int studentId = 1;
        int studentId2 = 3;
        String email = "email@email.com";
        String email2 = "email2@email2.com";

        when(registrationRepository.getAll()).thenReturn(List.of(new RegistrationDto(1, 1, studentId, "desc", Optional.of(false), "resp"), new RegistrationDto(1, 1, studentId + 1, "desc", Optional.empty(), "resp"), new RegistrationDto(1, 1, studentId2, "desc", Optional.of(false), "resp")));
        when(studentService.getStudentById(studentId)).thenReturn(new StudentDto(studentId, "name", "desc", new FileDto(Optional.empty()), email, new FileDto(Optional.empty()), Collections.emptyList()));
        when(studentService.getStudentById(studentId2)).thenReturn(new StudentDto(studentId2, "name", "desc", new FileDto(Optional.empty()), email2, new FileDto(Optional.empty()), Collections.emptyList()));

        // Act
        Collection<String> emails = sut.getEmailAddressesForRegistrations(StudentEmailSelection.REJECTED.getFlagValue(), businessId);

        // Assert
        Assertions.assertEquals(2, emails.size());

        Assertions.assertTrue(emails.contains(email));
        Assertions.assertTrue(emails.contains(email2));

        verify(registrationRepository, times(1)).getAll();
        verify(studentService, times(1)).getStudentById(studentId);
        verify(studentService, times(1)).getStudentById(studentId2);
    }

    @Test
    public void test_getEmailAddressesForRegistrations_Accepted_multiple() {
        // Arrange
        int businessId = 1;
        int studentId = 1;
        int studentId2 = 3;
        String email = "email@email.com";
        String email2 = "email2@email2.com";

        when(registrationRepository.getAll()).thenReturn(List.of(new RegistrationDto(1, 1, studentId, "desc", Optional.of(true), "resp"), new RegistrationDto(1, 1, studentId + 1, "desc", Optional.of(false), "resp"), new RegistrationDto(1, 1, studentId2, "desc", Optional.of(true), "resp")));
        when(studentService.getStudentById(studentId)).thenReturn(new StudentDto(studentId, "name", "desc", new FileDto(Optional.empty()), email, new FileDto(Optional.empty()), Collections.emptyList()));
        when(studentService.getStudentById(studentId2)).thenReturn(new StudentDto(studentId2, "name", "desc", new FileDto(Optional.empty()), email2, new FileDto(Optional.empty()), Collections.emptyList()));

        // Act
        Collection<String> emails = sut.getEmailAddressesForRegistrations(StudentEmailSelection.ACCEPTED.getFlagValue(), businessId);

        // Assert
        Assertions.assertEquals(2, emails.size());

        Assertions.assertTrue(emails.contains(email));
        Assertions.assertTrue(emails.contains(email2));

        verify(registrationRepository, times(1)).getAll();
        verify(studentService, times(1)).getStudentById(studentId);
        verify(studentService, times(1)).getStudentById(studentId2);
    }

    @Test
    public void test_getEmailAddressesForRegistrations_Registered_multiple() {
        // Arrange
        int businessId = 1;
        int studentId = 1;
        int studentId2 = 3;
        String email = "email@email.com";
        String email2 = "email2@email2.com";

        when(registrationRepository.getAll()).thenReturn(List.of(new RegistrationDto(1, 1, studentId, "desc", Optional.empty(), "resp"), new RegistrationDto(1, 1, studentId + 1, "desc", Optional.of(false), "resp"), new RegistrationDto(1, 1, studentId2, "desc", Optional.empty(), "resp")));
        when(studentService.getStudentById(studentId)).thenReturn(new StudentDto(studentId, "name", "desc", new FileDto(Optional.empty()), email, new FileDto(Optional.empty()), Collections.emptyList()));
        when(studentService.getStudentById(studentId2)).thenReturn(new StudentDto(studentId2, "name", "desc", new FileDto(Optional.empty()), email2, new FileDto(Optional.empty()), Collections.emptyList()));

        // Act
        Collection<String> emails = sut.getEmailAddressesForRegistrations(StudentEmailSelection.REGISTERED.getFlagValue(), businessId);

        // Assert
        Assertions.assertEquals(2, emails.size());

        Assertions.assertTrue(emails.contains(email));
        Assertions.assertTrue(emails.contains(email2));

        verify(registrationRepository, times(1)).getAll();
        verify(studentService, times(1)).getStudentById(studentId);
        verify(studentService, times(1)).getStudentById(studentId);
    }
}
